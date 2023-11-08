package ru.regiuss.vk.group.mailing.screen;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.regiuss.vk.group.mailing.VkGroupApp;
import ru.regiuss.vk.group.mailing.enums.BookmarkType;
import ru.regiuss.vk.group.mailing.messenger.Messenger;
import ru.regiuss.vk.group.mailing.messenger.VkMessenger;
import ru.regiuss.vk.group.mailing.model.Account;
import ru.regiuss.vk.group.mailing.node.CurrentKitView;
import ru.regiuss.vk.group.mailing.node.SelectAccountButton;
import ru.regiuss.vk.group.mailing.task.BookmarkTask;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.RunnablePane;
import space.regiuss.rgfx.node.SimpleAlert;

import java.io.*;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class BookmarkRunnableScreen extends RunnablePane {

    private final VkGroupApp app;
    private BookmarkTask task;

    @FXML
    private SelectAccountButton selectAccountButton;

    @FXML
    private ComboBox<BookmarkType> bookmarkType;

    @FXML
    @Getter
    private CurrentKitView currentKitView;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/bookmark.fxml"));
        bookmarkType.setItems(FXCollections.observableArrayList(BookmarkType.values()));
        bookmarkType.getSelectionModel().select(0);
        load();
    }

    @Override
    public void onStart(ActionEvent event) {
        Account account = selectAccountButton.getCurrentAccount().get();
        if (account == null) {
            app.showAlert(new SimpleAlert("Выберите аккаунт", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        startButton.setDisable(true);
        stopButton.setDisable(false);
        save();
        Messenger messenger = new VkMessenger(account.getToken());
        task = new BookmarkTask(messenger, bookmarkType.getValue());
        currentKitView.applyPageListListener(task.getPageListProperty());
        applyTask(
                task,
                "По избранному",
                app
        );
        app.getExecutorService().execute(task);
    }

    private void save() {
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream("data/bookmark"))) {
            Account account = selectAccountButton.getCurrentAccount().get();
            if (account == null)
                os.writeInt(-1);
            else
                os.writeInt(account.getId());
            os.writeUTF(bookmarkType.getSelectionModel().getSelectedItem().name());
        } catch (Exception e) {
            log.warn("save bookmark settings error", e);
            app.showAlert(new SimpleAlert("Не удалось сохранить настройки", AlertVariant.DANGER), Duration.seconds(5));
        }
    }

    private void load() {
        File loadFile = new File("data/bookmark");
        if (!loadFile.exists())
            return;
        try (DataInputStream is = new DataInputStream(new FileInputStream(loadFile))) {
            int accountId = is.readInt();
            if (accountId > -1)
                selectAccountButton.selectAccountById(accountId);
            bookmarkType.getSelectionModel().select(BookmarkType.valueOf(is.readUTF()));
        } catch (Exception e) {
            log.warn("load bookmark settings error", e);
            app.showAlert(new SimpleAlert("Не удалось загрузить настройки", AlertVariant.DANGER), Duration.seconds(5));
        }
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
