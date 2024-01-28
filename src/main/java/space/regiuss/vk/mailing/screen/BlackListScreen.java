package space.regiuss.vk.mailing.screen;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
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
import space.regiuss.rgfx.interfaces.SavableAndLoadable;
import space.regiuss.rgfx.manager.SaveLoadManager;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.messenger.VkMessenger;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.PageBlacklist;
import space.regiuss.vk.mailing.model.PageId;
import space.regiuss.vk.mailing.node.PageListItem;
import space.regiuss.vk.mailing.node.SelectAccountButton;
import space.regiuss.vk.mailing.repository.PageBlacklistRepository;
import space.regiuss.vk.mailing.task.ImportTask;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BlackListScreen extends VBox implements SavableAndLoadable {

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    private SaveLoadManager saveLoadManager;
    private final VkMailingApp app;
    private final PageBlacklistRepository pageBlacklistRepository;

    @FXML
    private Text countItemsText;

    @FXML
    private SelectAccountButton selectAccountButton;

    @FXML
    private ListView<ImageItemWrapper<Page>> blackListView;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/blackListScreen.fxml"));
        saveLoadManager = createSaveLoadManager();
        countItemsText.visibleProperty().bind(Bindings.isEmpty(blackListView.getItems()).not());
        countItemsText.textProperty().bind(Bindings.size(blackListView.getItems()).asString("(%s)"));
    }

    @PostConstruct
    public void init() {
        blackListView.setCellFactory(pageListView -> new PageListItem<>(app.getHostServices()));
        load();
        List<ImageItemWrapper<Page>> items = pageBlacklistRepository.findAll().stream().map(pageBlacklist -> {
            Page page = pageBlacklist.getPage();
            return new ImageItemWrapper<>(page);
        }).collect(Collectors.toList());
        blackListView.getItems().addAll(items);
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
        save();
        app.showAlert(new SimpleAlert("Начинаю импорт", AlertVariant.SUCCESS), Duration.seconds(5));
        Account account = selectAccountButton.getCurrentAccount().get();
        Messenger messenger = new VkMessenger(account.getToken());

        ImportTask task = new ImportTask(messenger, file);
        task.setOnSucceeded(event -> {
            app.showAlert(new SimpleAlert("Импорт завершен", AlertVariant.SUCCESS), Duration.seconds(5));
            List<ImageItemWrapper<Page>> items = task.getValue();
            List<PageId> ids = items.stream().map(wrapper -> wrapper.getItem().getId()).collect(Collectors.toList());
            Set<PageId> alreadyExistsIds = pageBlacklistRepository.findAllByIdIn(ids);
            List<PageBlacklist> blacklist = items.stream()
                    .filter(wrapper -> !alreadyExistsIds.contains(wrapper.getItem().getId()))
                    .map(wrapper -> {
                        Page page = wrapper.getItem();
                        PageBlacklist pageBlacklist = new PageBlacklist();
                        pageBlacklist.setId(page.getId());
                        pageBlacklist.setPage(page);
                        return pageBlacklist;
                    })
                    .collect(Collectors.toList());
            log.info("save count {}", blacklist.size());
            if (blacklist.isEmpty()) {
                return;
            }
            try {
                pageBlacklistRepository.saveAll(blacklist);
            } catch (Exception e) {
                app.showAlert(new SimpleAlert("Ошибка импорта: " + e.getMessage(), AlertVariant.DANGER), Duration.seconds(5));
                return;
            }
            blackListView.getItems().addAll(
                    blacklist.stream()
                            .map(pageBlacklist -> new ImageItemWrapper<>(pageBlacklist.getPage()))
                            .collect(Collectors.toList())
            );
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
        SaveLoadManager saveLoadManager = new SaveLoadManager(new File("data/blacklist"));
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
        return saveLoadManager;
    }
}
