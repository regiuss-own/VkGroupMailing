package space.regiuss.vk.mailing.screen;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
import space.regiuss.vk.mailing.enums.PageMode;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.messenger.VkMessenger;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.node.CurrentKitView;
import space.regiuss.vk.mailing.node.SelectAccountButton;
import space.regiuss.vk.mailing.repository.PageBlacklistRepository;
import space.regiuss.vk.mailing.task.BookmarkTask;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

import javax.annotation.PostConstruct;
import java.io.File;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class BookmarkRunnableScreen extends RunnablePane implements SavableAndLoadable {

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    private SaveLoadManager saveLoadManager;
    private final VkMailingApp app;
    private final PageBlacklistRepository pageBlacklistRepository;
    private BookmarkTask task;

    @FXML
    private CheckBox onlyCanMessageCheckBox;

    @FXML
    private SelectAccountButton selectAccountButton;

    @FXML
    private ComboBox<PageMode> bookmarkType;

    @FXML
    @Getter
    private CurrentKitView<ImageItemWrapper<Page>> currentKitView;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/bookmark.fxml"));
        saveLoadManager = createSaveLoadManager();
        bookmarkType.setItems(FXCollections.observableArrayList(PageMode.values()));
        bookmarkType.getSelectionModel().select(0);
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
        setState(RunnableState.RUNNING);
        save();
        Messenger messenger = new VkMessenger(account.getToken());
        task = new BookmarkTask(
                messenger,
                bookmarkType.getValue(),
                pageBlacklistRepository,
                onlyCanMessageCheckBox.isSelected()
        );
        currentKitView.applyPageListListener(task.getPageListProperty(), ImageItemWrapper::new);
        applyTask(
                task,
                "По избранному",
                app
        );
        app.getExecutorService().execute(task);
    }

    @Override
    public SaveLoadManager createSaveLoadManager() {
        SaveLoadManager saveLoadManager = new SaveLoadManager(new File("data/bookmark"));
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
        saveLoadManager.add(
                os -> os.writeUTF(bookmarkType.getSelectionModel().getSelectedItem().name()),
                is -> bookmarkType.getSelectionModel().select(PageMode.valueOf(is.readUTF()))
        );
        saveLoadManager.add(onlyCanMessageCheckBox);
        saveLoadManager.setOnLoadError(e -> {
            log.warn("load bookmark settings error", e);
            app.showAlert(new SimpleAlert("Не удалось загрузить настройки", AlertVariant.WARN), Duration.seconds(5));
        });
        saveLoadManager.setOnSaveError(e -> {
            log.warn("save bookmark settings error", e);
            app.showAlert(new SimpleAlert("Не удалось сохранить настройки", AlertVariant.DANGER), Duration.seconds(5));
        });
        return saveLoadManager;
    }

    @Override
    public void onStop(ActionEvent event) {
        clear();
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }
}
