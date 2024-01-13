package space.regiuss.vk.mailing.screen;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.model.ImageItemWrapper;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.SearchGroupData;
import space.regiuss.vk.mailing.node.CurrentKitView;
import space.regiuss.vk.mailing.node.SelectAccountButton;
import space.regiuss.vk.mailing.task.GroupTask;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.LinkedList;
import java.util.Locale;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class GroupRunnableScreen extends RunnablePane {

    private final VkMailingApp app;
    private Task<?> task;

    @FXML
    private TextArea exclusionArea;

    @FXML
    private CheckBox onlyCanMessageCheckBox;

    @FXML
    @Getter
    private CurrentKitView currentKitView;

    @FXML
    private SelectAccountButton selectAccountButton;

    @FXML
    private TextField minSubCountField;

    @FXML
    private TextField maxSubCountField;

    @FXML
    private TextField searchField;

    @FXML
    private CheckBox sortCheckBox;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/group.fxml"));
    }

    @PostConstruct
    public void init() {
        load();
    }

    @Override
    public void onStart(ActionEvent event) {
        Account account = selectAccountButton.getCurrentAccount().get();
        if (account == null) {
            app.showAlert(new SimpleAlert("Выберите аккаунт", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        if (searchField.getText() == null || searchField.getText().trim().isEmpty()) {
            app.showAlert(new SimpleAlert("Поле Поиск не может быть пустым", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }

        int maxSubCount = 0;
        try {
            maxSubCount = Integer.parseInt(maxSubCountField.getText());
        } catch (Exception e) {
            log.warn("maxSubCount number convert error", e);
            app.showAlert(
                    new SimpleAlert(
                            "Неверный формат поля Максимальное количество подписчиков\nиспользовано значение по умолчанию - 0",
                            AlertVariant.WARN
                    ),
                    Duration.seconds(5)
            );
        }

        int minSubCount = 0;
        try {
            minSubCount = Integer.parseInt(minSubCountField.getText());
        } catch (Exception e) {
            log.warn("minSubCount number convert error", e);
            app.showAlert(
                    new SimpleAlert(
                            "Неверный формат поля Минимальное количество подписчиков\nиспользовано значение по умолчанию - 0",
                            AlertVariant.WARN
                    ),
                    Duration.seconds(5)
            );
        }

        start();
        save();
        SearchGroupData data = new SearchGroupData();
        data.setSearch(searchField.getText());
        data.setSort(sortCheckBox.isSelected());
        data.setMaxSubscribers(maxSubCount);
        data.setMinSubscribers(minSubCount);
        data.setOnlyCanMessage(onlyCanMessageCheckBox.isSelected());
        Messenger messenger = new VkMessenger(account.getToken());
        GroupTask task = new GroupTask(messenger, data);
        currentKitView.applyPageListListener(task.getPageListProperty());
        applyTask(
                task,
                "По группам",
                app
        );
        this.task = task;
        app.getExecutorService().execute(task);
    }

    private void start() {
        startButton.setDisable(true);
        stopButton.setDisable(false);
    }

    @Override
    public void onStop(ActionEvent event) {
        clear();
        if (task != null) {
            task.cancel(true);
            task = null;
        }
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
        if (currentKitView.getListView().getItems().isEmpty()) {
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
                ObservableList<ImageItemWrapper<Page>> items = currentKitView.getListView().getItems();
                for (ImageItemWrapper<Page> item : items) {
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
                Platform.runLater(() -> items.removeAll(removeItems));
                return null;
            }
        };
        applyTask(deleteTask, "Удаление страниц", app);
        task = deleteTask;
        start();
        app.getExecutorService().execute(deleteTask);
    }

    private void save() {
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream("data/group"))) {
            Account account = selectAccountButton.getCurrentAccount().get();
            if (account == null)
                os.writeInt(-1);
            else
                os.writeInt(account.getId());
            os.writeUTF(minSubCountField.getText());
            os.writeUTF(maxSubCountField.getText());
            os.writeUTF(searchField.getText());
            os.writeBoolean(sortCheckBox.isSelected());
            os.writeBoolean(onlyCanMessageCheckBox.isSelected());
            os.writeUTF(exclusionArea.getText());
        } catch (Exception e) {
            log.warn("save group settings error", e);
            app.showAlert(new SimpleAlert("Не удалось сохранить настройки", AlertVariant.DANGER), Duration.seconds(5));
        }
    }

    private void load() {
        File loadFile = new File("data/group");
        if (!loadFile.exists())
            return;
        try (DataInputStream is = new DataInputStream(new FileInputStream(loadFile))) {
            int accountId = is.readInt();
            if (accountId > -1)
                selectAccountButton.selectAccountById(accountId);
            minSubCountField.setText(is.readUTF());
            maxSubCountField.setText(is.readUTF());
            searchField.setText(is.readUTF());
            sortCheckBox.setSelected(is.readBoolean());
            onlyCanMessageCheckBox.setSelected(is.readBoolean());
            exclusionArea.setText(is.readUTF());
        } catch (Exception e) {
            log.warn("load group settings error", e);
            app.showAlert(new SimpleAlert("Не удалось загрузить настройки", AlertVariant.WARN), Duration.seconds(5));
        }
    }
}
