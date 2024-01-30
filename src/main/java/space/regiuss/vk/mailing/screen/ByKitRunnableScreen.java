package space.regiuss.vk.mailing.screen;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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
import space.regiuss.rgfx.enums.RunnableState;
import space.regiuss.rgfx.interfaces.SavableAndLoadable;
import space.regiuss.rgfx.manager.SaveLoadManager;
import space.regiuss.rgfx.node.RunnablePane;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.exporter.DescriptionKitExporter;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.messenger.VkMessenger;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.node.CurrentKitView;
import space.regiuss.vk.mailing.node.DescriptionPageListItem;
import space.regiuss.vk.mailing.node.PageListItem;
import space.regiuss.vk.mailing.node.SelectAccountButton;
import space.regiuss.vk.mailing.task.ByKitTask;
import space.regiuss.vk.mailing.task.ImportTask;
import space.regiuss.vk.mailing.wrapper.DescriptionItemWrapper;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ByKitRunnableScreen extends RunnablePane implements SavableAndLoadable {

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    private SaveLoadManager saveLoadManager;
    private final VkMailingApp app;
    private Task<Void> task;

    @FXML
    private TextField regexField;

    @FXML
    private ListView<ImageItemWrapper<Page>> checkListView;

    @FXML
    private SelectAccountButton selectAccountButton;

    @FXML
    @Getter
    private CurrentKitView<DescriptionItemWrapper<Page>> currentKitView;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/bykit.fxml"));
        saveLoadManager = createSaveLoadManager();
    }

    @PostConstruct
    public void init() {
        checkListView.setCellFactory(pageListView -> new PageListItem<>(app.getHostServices()));
        currentKitView.setCellFactory(pageListView -> new DescriptionPageListItem(app.getHostServices()));
        currentKitView.setKitExporter(new DescriptionKitExporter<>());
        load();
    }

    @Override
    public void onStart(ActionEvent event) {
        Account account = selectAccountButton.getCurrentAccount().get();
        if (account == null) {
            app.showAlert(new SimpleAlert("Выберите аккаунт", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        if (regexField.getText() == null || regexField.getText().trim().isEmpty()) {
            app.showAlert(new SimpleAlert("Поле Регулярное выражение не может быть пустым", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }

        Pattern pattern;
        try {
            pattern = Pattern.compile(regexField.getText());
        } catch (Exception e) {
            log.warn("pattern compile error", e);
            app.showAlert(new SimpleAlert("Неверный формат регулярногов выражения", AlertVariant.WARN), Duration.seconds(5));
            return;
        }

        setState(RunnableState.RUNNING);
        save();

        Messenger messenger = new VkMessenger(account.getToken());
        ByKitTask task = new ByKitTask(pattern, checkListView.getItems(), messenger);
        currentKitView.applyWrapperListListener(task.getPageListProperty());
        applyTask(
                task,
                "По списку",
                app
        );
        this.task = task;
        app.getExecutorService().execute(task);
    }

    @Override
    public void onStop(ActionEvent actionEvent) {
        clear();
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    @FXML
    public void onImportClick(ActionEvent event) {
        if (selectAccountButton.getCurrentAccount().get() == null) {
            app.showAlert(new SimpleAlert("Для импорта необходимо указать аккаунт", AlertVariant.WARN), Duration.seconds(5));
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv", "*.csv"));
        File file = chooser.showOpenDialog(app.getStage());
        if (file != null)
            importFromFile(file);
    }

    private void importFromFile(File file) {
        app.showAlert(new SimpleAlert("Начинаю импорт", AlertVariant.SUCCESS), Duration.seconds(5));
        Account account = selectAccountButton.getCurrentAccount().get();
        Messenger messenger = new VkMessenger(account.getToken());

        ImportTask task = new ImportTask(messenger, file);
        task.setOnSucceeded(event -> {
            app.showAlert(new SimpleAlert("Импорт завершен", AlertVariant.SUCCESS), Duration.seconds(5));
            List<ImageItemWrapper<Page>> items = task.getValue();
            checkListView.getItems().addAll(items);
        });
        task.setOnFailed(event -> {
            Throwable e = task.getException();
            log.warn("import error", e);
            app.showAlert(new SimpleAlert("Ошибка импорта: " + e.getMessage(), AlertVariant.DANGER), Duration.seconds(5));
        });
        app.getExecutorService().execute(task);
    }

    @Override
    public SaveLoadManager createSaveLoadManager() {
        SaveLoadManager saveLoadManager = new SaveLoadManager(new File("data/byKit"));
        saveLoadManager.add(
                os -> {
                    Account account = selectAccountButton.getCurrentAccount().get();
                    if (account == null)
                        os.writeInt(-1);
                    else
                        os.writeInt(account.getId());
                },
                is -> {
                    int accountId = is.readInt();
                    if (accountId > -1)
                        selectAccountButton.selectAccountById(accountId);
                }
        );
        saveLoadManager.add(regexField);
        saveLoadManager.setOnSaveError(e -> {
            log.warn("save byKit settings error", e);
            app.showAlert(new SimpleAlert("Не удалось сохранить настройки", AlertVariant.DANGER), Duration.seconds(5));
        });
        saveLoadManager.setOnLoadError(e -> {
            log.warn("load byKit settings error", e);
            app.showAlert(new SimpleAlert("Не удалось загрузить настройки", AlertVariant.WARN), Duration.seconds(5));
        });
        return saveLoadManager;
    }
}
