package space.regiuss.vk.mailing.screen;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
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
import space.regiuss.vk.mailing.enums.PageMode;
import space.regiuss.vk.mailing.exporter.EmailKitExporter;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.messenger.VkMessenger;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.model.ByEmailData;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.node.CurrentKitView;
import space.regiuss.vk.mailing.node.EmailPageListItem;
import space.regiuss.vk.mailing.node.SelectAccountButton;
import space.regiuss.vk.mailing.task.ByEmailTask;
import space.regiuss.vk.mailing.wrapper.EmailItemWrapper;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Arrays;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ByMailRunnableScreen extends RunnablePane {

    private final VkMailingApp app;
    private ByEmailTask task;

    @FXML
    private CheckBox checkDescriptionCheckBox;

    @FXML
    private ComboBox<PageMode> pageModeComboBox;

    @FXML
    private SelectAccountButton selectAccountButton;

    @FXML
    private TextArea searchArea;

    @FXML
    private CurrentKitView<EmailItemWrapper<Page>> currentKitView;

    @FXML
    private Label statusText;

    @FXML
    private ProgressBar progressBar;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/byMail.fxml"));
        pageModeComboBox.setItems(FXCollections.observableArrayList(PageMode.values()));
        pageModeComboBox.getSelectionModel().select(0);
    }

    @PostConstruct
    public void init() {
        currentKitView.setCellFactory(pageListView -> new EmailPageListItem(app.getHostServices()));
        currentKitView.setKitExporter(new EmailKitExporter<>());
        load();
    }

    @Override
    public void onStart(ActionEvent actionEvent) {
        Account account = selectAccountButton.getCurrentAccount().get();
        if (account == null) {
            app.showAlert(new SimpleAlert("Выберите аккаунт", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        if (searchArea.getText() == null || searchArea.getText().trim().isEmpty()) {
            app.showAlert(new SimpleAlert("Поле Поиск не может быть пустым", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }

        save();
        setState(RunnableState.RUNNING);

        Messenger messenger = new VkMessenger(account.getToken());
        ByEmailData data = new ByEmailData(
                messenger,
                Arrays.asList(searchArea.getText().split("\n")),
                pageModeComboBox.getSelectionModel().getSelectedItem(),
                checkDescriptionCheckBox.isSelected()
        );
        ByEmailTask task = new ByEmailTask(data);
        currentKitView.applyWrapperListListener(task.getPageListProperty());
        applyTask(
                task,
                "По почте",
                app
        );
        progressBar.progressProperty().bind(task.progressProperty());
        statusText.textProperty().bind(task.messageProperty());
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

    @Override
    protected void clear() {
        super.clear();
        statusText.textProperty().unbind();
        statusText.setText("Готово");
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
    }

    @FXML
    public void onUploadListMail(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv", "*.csv"));
        File file = chooser.showOpenDialog(app.getStage());
        if (file == null) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            for (String line; (line = reader.readLine()) != null; ) {
                int separatorIndex = line.indexOf(';');
                if (separatorIndex > 0) {
                    line = line.substring(0, separatorIndex);
                }
                sb.append(line.replaceAll("\\s", "")).append('\n');
            }
            searchArea.setText(sb.toString());
        } catch (Exception e) {
            log.warn("import error", e);
            app.showAlert(new SimpleAlert("Не удалось импортировать файл", AlertVariant.DANGER), Duration.seconds(5));
        }
    }

    private void save() {
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream("data/byMail"))) {
            Account account = selectAccountButton.getCurrentAccount().get();
            if (account == null)
                os.writeInt(-1);
            else
                os.writeInt(account.getId());
            os.writeUTF(searchArea.getText());
            os.writeInt(pageModeComboBox.getSelectionModel().getSelectedIndex());
            os.writeBoolean(checkDescriptionCheckBox.isSelected());
        } catch (Exception e) {
            log.warn("save byMail settings error", e);
            app.showAlert(new SimpleAlert("Не удалось сохранить настройки", AlertVariant.DANGER), Duration.seconds(5));
        }
    }

    private void load() {
        File loadFile = new File("data/byMail");
        if (!loadFile.exists())
            return;
        try (DataInputStream is = new DataInputStream(new FileInputStream(loadFile))) {
            int accountId = is.readInt();
            if (accountId > -1)
                selectAccountButton.selectAccountById(accountId);
            searchArea.setText(is.readUTF());
            pageModeComboBox.getSelectionModel().select(is.readInt());
            checkDescriptionCheckBox.setSelected(is.readBoolean());
        } catch (Exception e) {
            log.warn("load byMail settings error", e);
            app.showAlert(new SimpleAlert("Не удалось загрузить настройки", AlertVariant.WARN), Duration.seconds(5));
        }
    }
}
