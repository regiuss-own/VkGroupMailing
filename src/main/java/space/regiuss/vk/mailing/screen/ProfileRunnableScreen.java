package space.regiuss.vk.mailing.screen;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.messenger.VkMessenger;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.model.ProfileTaskData;
import space.regiuss.vk.mailing.node.CurrentKitView;
import space.regiuss.vk.mailing.node.SelectAccountButton;
import space.regiuss.vk.mailing.task.ProfileTask;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.RunnablePane;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.rgfx.spring.RGFXAPP;

import javax.annotation.PostConstruct;
import java.io.*;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ProfileRunnableScreen extends RunnablePane {

    private final VkMailingApp app;
    private ProfileTask task;

    @FXML
    private CheckBox onlyCanMessageCheckBox;

    @FXML
    private TextField minSubCountField;

    @FXML
    private TextField maxSubCountField;

    @FXML
    @Getter
    private CurrentKitView currentKitView;

    @FXML
    private TextField groupField;

    @FXML
    private SelectAccountButton selectAccountButton;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/profiles.fxml"));
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

        startButton.setDisable(true);
        stopButton.setDisable(false);
        save();
        ProfileTaskData taskData = new ProfileTaskData();
        taskData.setGroup(groupField.getText());
        taskData.setMinSubscribersCount(minSubCount);
        taskData.setMaxSubscribersCount(maxSubCount);
        taskData.setOnlyCanMessage(onlyCanMessageCheckBox.isSelected());
        Messenger messenger = new VkMessenger(account.getToken());
        task = new ProfileTask(messenger, taskData);
        currentKitView.applyPageListListener(task.getPageListProperty());
        applyTask(
                task,
                "По профилям",
                app
        );
        app.getExecutorService().execute(task);
    }

    private void save() {
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream("data/profile"))) {
            Account account = selectAccountButton.getCurrentAccount().get();
            if (account == null)
                os.writeInt(-1);
            else
                os.writeInt(account.getId());
            os.writeUTF(minSubCountField.getText());
            os.writeUTF(maxSubCountField.getText());
            os.writeUTF(groupField.getText());
            os.writeBoolean(onlyCanMessageCheckBox.isSelected());
        } catch (Exception e) {
            log.warn("save profile settings error", e);
            app.showAlert(new SimpleAlert("Не удалось сохранить настройки", AlertVariant.DANGER), Duration.seconds(5));
        }
    }

    private void load() {
        File loadFile = new File("data/profile");
        if (!loadFile.exists())
            return;
        try (DataInputStream is = new DataInputStream(new FileInputStream(loadFile))) {
            int accountId = is.readInt();
            if (accountId > -1)
                selectAccountButton.selectAccountById(accountId);
            minSubCountField.setText(is.readUTF());
            maxSubCountField.setText(is.readUTF());
            groupField.setText(is.readUTF());
            onlyCanMessageCheckBox.setSelected(is.readBoolean());
        } catch (Exception e) {
            log.warn("load profile settings error", e);
            app.showAlert(new SimpleAlert("Не удалось загрузить настройки", AlertVariant.WARN), Duration.seconds(5));
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
