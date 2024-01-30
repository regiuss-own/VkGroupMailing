package space.regiuss.vk.mailing.screen;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
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
import space.regiuss.rgfx.interfaces.SavableAndLoadable;
import space.regiuss.rgfx.manager.SaveLoadManager;
import space.regiuss.rgfx.node.RunnablePane;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.enums.DescriptionMode;
import space.regiuss.vk.mailing.enums.PageMode;
import space.regiuss.vk.mailing.exporter.DescriptionKitExporter;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.messenger.VkMessenger;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.model.FastSearchData;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.node.CurrentKitView;
import space.regiuss.vk.mailing.node.DescriptionPageListItem;
import space.regiuss.vk.mailing.node.SelectAccountButton;
import space.regiuss.vk.mailing.repository.PageBlacklistRepository;
import space.regiuss.vk.mailing.task.FastSearchTask;
import space.regiuss.vk.mailing.util.Utils;
import space.regiuss.vk.mailing.wrapper.DescriptionItemWrapper;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class FastSearchRunnableScreen extends RunnablePane implements SavableAndLoadable {

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    private SaveLoadManager saveLoadManager;
    private final VkMailingApp app;
    private final PageBlacklistRepository pageBlacklistRepository;
    private FastSearchTask task;

    @FXML
    private Slider threadsCountSlider;

    @FXML
    private Text threadsCountLabel;

    @FXML
    private TextArea descriptionWordsArea;

    @FXML
    private TextField tryCountField;

    @FXML
    private ComboBox<DescriptionMode> descriptionModeComboBox;

    @FXML
    private ComboBox<PageMode> pageModeComboBox;

    @FXML
    private SelectAccountButton selectAccountButton;

    @FXML
    private TextArea searchArea;

    @FXML
    private CurrentKitView<DescriptionItemWrapper<Page>> currentKitView;

    @FXML
    private Label statusText;

    @FXML
    private ProgressBar progressBar;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/fastSearch.fxml"));
        saveLoadManager = createSaveLoadManager();
        pageModeComboBox.setItems(FXCollections.observableArrayList(PageMode.values()));
        pageModeComboBox.getSelectionModel().select(0);
        descriptionModeComboBox.setItems(FXCollections.observableArrayList(DescriptionMode.values()));
        descriptionModeComboBox.getSelectionModel().select(0);
        threadsCountLabel.textProperty().bind(
                Bindings.format(
                        "%.0f",
                        threadsCountSlider.valueProperty()
                )
        );
    }

    @PostConstruct
    public void init() {
        currentKitView.setCellFactory(pageListView -> new DescriptionPageListItem(app.getHostServices()));
        currentKitView.setKitExporter(new DescriptionKitExporter<>());
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

        int tryCount = Utils.parseNumber(tryCountField, "Количество попыток", app, 3);
        if (tryCount < 1) {
            tryCount = 1;
            app.showAlert(new SimpleAlert(
                    "Количество попыток не может быть < 1, установлено значение - 1",
                    AlertVariant.WARN
            ), Duration.seconds(5));
        }

        save();
        setState(RunnableState.RUNNING);

        Set<String> descriptionWords = new HashSet<>();
        for (String s : descriptionWordsArea.getText().toLowerCase(Locale.ROOT).split("\n")) {
            descriptionWords.add(s.trim());
        }

        Messenger messenger = new VkMessenger(account.getToken());
        FastSearchData data = new FastSearchData(
                messenger,
                Arrays.asList(searchArea.getText().split("\n")),
                pageModeComboBox.getSelectionModel().getSelectedItem(),
                descriptionModeComboBox.getSelectionModel().getSelectedItem(),
                pageBlacklistRepository,
                tryCount,
                descriptionWords,
                app.getExecutorService(),
                threadsCountSlider.valueProperty().intValue()
        );
        FastSearchTask task = new FastSearchTask(data);
        currentKitView.applyWrapperListListener(task.getPageListProperty());
        applyTask(
                task,
                "Быстрый поиск",
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
    public void onUploadSearch(ActionEvent event) {
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

    @Override
    public SaveLoadManager createSaveLoadManager() {
        SaveLoadManager saveLoadManager = new SaveLoadManager(new File("data/byMail"));
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
        saveLoadManager.add(searchArea);
        saveLoadManager.add(pageModeComboBox);
        saveLoadManager.add(descriptionModeComboBox);
        saveLoadManager.add(tryCountField);
        saveLoadManager.add(descriptionWordsArea);
        saveLoadManager.add(
                os -> {
                    os.write(threadsCountSlider.valueProperty().intValue());
                },
                is -> {
                    threadsCountSlider.setValue(is.readInt());
                }
        );
        saveLoadManager.setOnLoadError(e -> {
            log.warn("load byMail settings error", e);
            app.showAlert(new SimpleAlert("Не удалось загрузить настройки", AlertVariant.WARN), Duration.seconds(5));
        });
        saveLoadManager.setOnSaveError(e -> {
            log.warn("save byMail settings error", e);
            app.showAlert(new SimpleAlert("Не удалось сохранить настройки", AlertVariant.DANGER), Duration.seconds(5));
        });
        return saveLoadManager;
    }

}
