package space.regiuss.vk.mailing.screen;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.enums.RunnableState;
import space.regiuss.rgfx.node.RunnablePane;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.messenger.VkMessenger;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.SearchGroupData;
import space.regiuss.vk.mailing.node.CurrentKitView;
import space.regiuss.vk.mailing.node.SelectAccountButton;
import space.regiuss.vk.mailing.task.GroupTask;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Arrays;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class GroupRunnableScreen extends RunnablePane {

    private final VkMailingApp app;
    private Task<?> task;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusText;

    @FXML
    private CheckBox onlyCanMessageCheckBox;

    @FXML
    @Getter
    private CurrentKitView<ImageItemWrapper<Page>> currentKitView;

    @FXML
    private SelectAccountButton selectAccountButton;

    @FXML
    private TextField minSubCountField;

    @FXML
    private TextField maxSubCountField;

    @FXML
    private TextArea searchArea;

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
        if (searchArea.getText() == null || searchArea.getText().trim().isEmpty()) {
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

        setState(RunnableState.RUNNING);
        save();
        SearchGroupData data = new SearchGroupData();
        data.setSearch(Arrays.asList(searchArea.getText().split("\n")));
        data.setSort(sortCheckBox.isSelected());
        data.setMaxSubscribers(maxSubCount);
        data.setMinSubscribers(minSubCount);
        data.setOnlyCanMessage(onlyCanMessageCheckBox.isSelected());
        Messenger messenger = new VkMessenger(account.getToken());
        GroupTask task = new GroupTask(messenger, data);
        currentKitView.applyPageListListener(task.getPageListProperty(), ImageItemWrapper::new);
        applyTask(
                task,
                "По группам",
                app
        );
        progressBar.progressProperty().bind(task.progressProperty());
        statusText.textProperty().bind(task.messageProperty());
        this.task = task;
        app.getExecutorService().execute(task);
    }

    @Override
    protected void clear() {
        super.clear();
        statusText.textProperty().unbind();
        statusText.setText("Готово");
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
    }

    @Override
    public void onStop(ActionEvent event) {
        clear();
        if (task != null) {
            task.cancel(true);
            task = null;
        }
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
            os.writeUTF(searchArea.getText());
            os.writeBoolean(sortCheckBox.isSelected());
            os.writeBoolean(onlyCanMessageCheckBox.isSelected());
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
            searchArea.setText(is.readUTF());
            sortCheckBox.setSelected(is.readBoolean());
            onlyCanMessageCheckBox.setSelected(is.readBoolean());
        } catch (Exception e) {
            log.warn("load group settings error", e);
            app.showAlert(new SimpleAlert("Не удалось загрузить настройки", AlertVariant.WARN), Duration.seconds(5));
        }
    }
}
