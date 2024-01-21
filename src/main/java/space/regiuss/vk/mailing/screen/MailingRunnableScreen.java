package space.regiuss.vk.mailing.screen;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.RunnablePane;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.messenger.VkMessenger;
import space.regiuss.vk.mailing.model.*;
import space.regiuss.vk.mailing.node.ProgressPageListItem;
import space.regiuss.vk.mailing.node.SelectAccountButton;
import space.regiuss.vk.mailing.popup.KitImportPopup;
import space.regiuss.vk.mailing.popup.SelectMessagePopup;
import space.regiuss.vk.mailing.service.MessageService;
import space.regiuss.vk.mailing.task.MailingTask;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class MailingRunnableScreen extends RunnablePane {

    private final VkMailingApp app;
    private final MessageService messageService;

    private Task<?> task;
    private int currentMessageKit = -1;

    @FXML
    @Getter
    private SelectAccountButton selectAccountButton;

    @FXML
    private TextArea exclusionArea;

    @FXML
    private Button selectMessageButton;

    @FXML
    private Text countText;

    @FXML
    private TextField messageDelayField;

    @FXML
    private ListView<ProgressItemWrapper<Page>> kitListView;

    @FXML
    private TextField dialogDelayField;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/mailing.fxml"));
    }

    @PostConstruct
    public void init() {
        kitListView.setCellFactory(pageListView -> new ProgressPageListItem(app.getHostServices()));
        load();
    }

    @FXML
    public void onUploadKitClick(ActionEvent event) {
        KitImportPopup popup = app.getBean(KitImportPopup.class);
        popup.setOnClose(() -> app.hideModal(popup));
        app.showModal(popup);
    }

    @Override
    public void onStart(ActionEvent event) {
        Account account = selectAccountButton.getCurrentAccount().get();
        if (account == null) {
            app.showAlert(new SimpleAlert("Выберите аккаунт", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        if (currentMessageKit == -1) {
            app.showAlert(new SimpleAlert("Выберите сообщение", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        if (kitListView.getItems().isEmpty()) {
            app.showAlert(new SimpleAlert("Текущий набор страниц пуст", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        List<Message> messages = messageService.getMessagesByKit(currentMessageKit);
        if (messages.isEmpty()) {
            app.showAlert(new SimpleAlert("Набор сообщений пуст", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        kitListView.getItems().forEach(item -> {
            item.setTotal(-1);
            item.setProgress(-1);
        });
        kitListView.refresh();

        int messageDelay = 0;
        try {
            messageDelay = Integer.parseInt(messageDelayField.getText());
        } catch (Exception e) {
            log.warn("messageDelay number convert error", e);
            app.showAlert(
                    new SimpleAlert(
                            "Неверный формат поля Задержка между сообщениями\nиспользовано значение по умолчанию - 0",
                            AlertVariant.WARN
                    ),
                    Duration.seconds(5)
            );
        }

        int dialogDelay = 0;
        try {
            dialogDelay = Integer.parseInt(dialogDelayField.getText());
        } catch (Exception e) {
            log.warn("minSubCount number convert error", e);
            app.showAlert(
                    new SimpleAlert(
                            "Неверный формат поля Задержка между диалогами\nиспользовано значение по умолчанию - 0",
                            AlertVariant.WARN
                    ),
                    Duration.seconds(5)
            );
        }
        start();
        save();
        MailingData mailingData = new MailingData();
        mailingData.setMessages(messages);
        mailingData.setMessageDelay(messageDelay * 1000);
        mailingData.setDialogDelay(dialogDelay * 1000);
        mailingData.setItems(kitListView.getItems());
        Messenger messenger = new VkMessenger(account.getToken());
        task = new MailingTask(messenger, mailingData, kitListView);
        applyTask(task, "Рассылка", app);
        app.getExecutorService().execute(task);
    }

    private void save() {
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream("data/mailing"))) {
            Account account = selectAccountButton.getCurrentAccount().get();
            if (account == null)
                os.writeInt(-1);
            else
                os.writeInt(account.getId());
            os.writeInt(currentMessageKit);
            os.writeUTF(messageDelayField.getText());
            os.writeUTF(dialogDelayField.getText());
            os.writeUTF(exclusionArea.getText());
        } catch (Exception e) {
            log.warn("save mailing settings error", e);
            app.showAlert(new SimpleAlert("Не удалось сохранить настройки", AlertVariant.DANGER), Duration.seconds(5));
        }
    }

    private void load() {
        File loadFile = new File("data/mailing");
        if (!loadFile.exists())
            return;
        try (DataInputStream is = new DataInputStream(new FileInputStream(loadFile))) {
            int accountId = is.readInt();
            if (accountId > -1)
                selectAccountButton.selectAccountById(accountId);
            currentMessageKit = is.readInt();
            if (currentMessageKit > -1) {
                selectMessageButton.setText("Сообщение: " + messageService.findById(currentMessageKit).getName());
            }
            messageDelayField.setText(is.readUTF());
            dialogDelayField.setText(is.readUTF());
            exclusionArea.setText(is.readUTF());
        } catch (Exception e) {
            log.warn("load mailing settings error", e);
            app.showAlert(new SimpleAlert("Не удалось загрузить настройки", AlertVariant.DANGER), Duration.seconds(5));
        }
    }

    public boolean setKitItems(List<? extends ImageItemWrapper<Page>> items) {
        if (task != null && task.isRunning()) {
            app.showAlert(new SimpleAlert("Нельзя установить набор пока запущена Рассылка", AlertVariant.DANGER), Duration.seconds(5));
            return false;
        }
        List<ProgressItemWrapper<Page>> mapItems = items.stream().map(wrapper -> {
                    ProgressItemWrapper<Page> progressWrapper = new ProgressItemWrapper<>(wrapper.getItem());
                    progressWrapper.setImage(wrapper.getImage());
                    return progressWrapper;
                })
                .collect(Collectors.toList());
        try {
            kitListView.setItems(FXCollections.observableList(mapItems));
            kitListView.refresh();
            updateListViewItemsCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void updateListViewItemsCount() {
        countText.setVisible(!kitListView.getItems().isEmpty());
        countText.setText(String.format("(%s)", kitListView.getItems().size()));
    }

    @Override
    public void onStop(ActionEvent event) {
        clear();
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    private void start() {
        startButton.setDisable(true);
        stopButton.setDisable(false);
    }

    @FXML
    public void onExportClick(ActionEvent event) {
        if (kitListView.getItems().isEmpty()) {
            app.showAlert(new SimpleAlert("Текущий набор пуст", AlertVariant.WARN), Duration.seconds(5));
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv", "*.csv"));
        File file = chooser.showSaveDialog(app.getStage());
        if (file == null)
            return;
        Button button = (Button) event.getTarget();
        button.setDisable(true);
        app.showAlert(new SimpleAlert("Начинаю экспорт набора", AlertVariant.SUCCESS), Duration.seconds(5));
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.whenComplete((r, e) -> Platform.runLater(() -> {
            button.setDisable(false);
            if (e == null) {
                app.showAlert(new SimpleAlert("Экспорт набора завершен", AlertVariant.SUCCESS), Duration.seconds(5));
            } else {
                log.warn("export error", e);
                app.showAlert(new SimpleAlert("Не удалось экспортировать набор: " + e.getMessage(), AlertVariant.DANGER), Duration.seconds(5));
            }
        }));
        app.getExecutorService().execute(() -> {
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(239);
                os.write(187);
                os.write(191);
                os.write("ссылка;id;тип;имя;подписчики;фото\n".getBytes(StandardCharsets.UTF_8));
                for (ImageItemWrapper<Page> item : kitListView.getItems()) {
                    Page page = item.getItem();
                    os.write(String.format(
                            "%s;%s;%s;%s;%s;%s%n",
                            page.getLink(),
                            page.getId(),
                            page.getType().name(),
                            page.getName().replace(";", ""),
                            page.getSubscribers(),
                            page.getIcon()
                    ).getBytes(StandardCharsets.UTF_8));
                }
                completableFuture.complete(null);
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        });
    }

    @FXML
    public void onDeleteExclusionClick(ActionEvent event) {
        if (task != null && !task.isDone() && !task.isCancelled()) {
            app.showAlert(new SimpleAlert("Запуск возможен только после завершения задачи", AlertVariant.WARN), Duration.seconds(2));
            return;
        }
        if (exclusionArea.getText().trim().isEmpty()) {
            app.showAlert(new SimpleAlert("Список слов пуст", AlertVariant.WARN), Duration.seconds(2));
            return;
        }
        if (kitListView.getItems().isEmpty()) {
            app.showAlert(new SimpleAlert("Текущий набор пуст", AlertVariant.WARN), Duration.seconds(2));
            return;
        }
        final String[] exclusionWords = exclusionArea.getText().split("\n");
        for (int i = 0; i < exclusionWords.length; i++) {
            exclusionWords[i] = exclusionWords[i].toLowerCase(Locale.ROOT);
        }
        Task<?> deleteTask = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                LinkedList<ImageItemWrapper<Page>> removeItems = new LinkedList<>();
                ObservableList<ProgressItemWrapper<Page>> items = kitListView.getItems();
                for (ProgressItemWrapper<Page> item : items) {
                    for (String w : exclusionWords) {
                        if (w.trim().isEmpty()) {
                            continue;
                        }
                        if (item.getItem().getName().toLowerCase(Locale.ROOT).contains(w)) {
                            removeItems.add(item);
                            break;
                        }
                    }
                }
                Platform.runLater(() -> {
                    items.removeAll(removeItems);
                    updateListViewItemsCount();
                });
                return null;
            }
        };
        save();
        applyTask(deleteTask, "Удаление страниц", app);
        task = deleteTask;
        start();
        app.getExecutorService().execute(deleteTask);
    }

    @FXML
    public void onSelectMessageClick(ActionEvent event) {
        SelectMessagePopup popup = app.getBean(SelectMessagePopup.class);
        popup.setOnClose(() -> app.hideModal(popup));
        if (currentMessageKit > -1)
            popup.setCurrentValue(currentMessageKit);
        popup.setOnConfirm(messageKit -> {
            currentMessageKit = messageKit == null ? -1 : messageKit.getId();
            selectMessageButton.setText("Сообщение: " + (messageKit == null ? "Не выбрано" : messageKit.getName()));
        });
        app.showModal(popup);
    }
}
