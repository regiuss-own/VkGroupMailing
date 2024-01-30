package space.regiuss.vk.mailing.screen;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.rgfx.node.TileView;
import space.regiuss.rgfx.popup.ConfirmPopup;
import space.regiuss.rgfx.spring.RGFXAPP;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.messenger.VkMessenger;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.node.AccountManageItem;
import space.regiuss.vk.mailing.popup.AuthPopup;
import space.regiuss.vk.mailing.service.AccountService;

import javax.annotation.PostConstruct;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class AccountListScreen extends VBox {

    private final VkMailingApp app;
    private final AccountService accountService;

    @FXML
    private TextField tokenField;

    @FXML
    private TileView<AccountManageItem> accountsPane;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/accountList.fxml"));
        accountsPane.getScrollPane().setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    @PostConstruct
    private void init() {
        for (Account account : accountService.getAll())
            addAccount(account);
    }

    private void addAccount(final Account account) {
        AccountManageItem item = new AccountManageItem(account, app);
        item.setOnRemove(event -> {
            ConfirmPopup popup = new ConfirmPopup("Подтверждение", "Вы действительно хотите удалить аккаунт " + account.getName() + "?");
            popup.setOnClose(() -> app.hideModal(popup));
            popup.setOnConfirm(() -> {
                accountsPane.remove(item);
                accountService.delete(account);
                app.showAlert(new SimpleAlert("Аккаунт успешно удален", AlertVariant.SUCCESS), Duration.seconds(5));
            });
            app.showModal(popup);
        });
        accountsPane.add(item);
    }

    private boolean addAccount(final String token) {
        Messenger messenger = new VkMessenger(token);
        Account account = messenger.getAccount();
        if (account == null) {
            app.showAlert(new SimpleAlert("Не удалось добавить аккаунт", AlertVariant.DANGER), Duration.seconds(5));
            return false;
        }
        if (accountsPane.getItems().stream().anyMatch(node -> node.getItem().getAccount().getId().equals(account.getId()))) {
            app.showAlert(new SimpleAlert("Аккаунт уже добавлен", AlertVariant.WARN), Duration.seconds(5));
            return true;
        }
        accountService.save(account);
        addAccount(account);
        app.showAlert(new SimpleAlert("Аккаунт успешно добавлен", AlertVariant.SUCCESS), Duration.seconds(5));
        return true;
    }

    @FXML
    public void onAddByTokenClick(ActionEvent event) {
        if (tokenField.getText().trim().isEmpty()) {
            app.showAlert(new SimpleAlert("Поле Токен не может быть пустым", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        if (addAccount(tokenField.getText()))
            tokenField.setText("");
    }

    @FXML
    public void onAuthByVkClick(ActionEvent event) {
        AuthPopup popup = new AuthPopup();
        popup.setOnClose(() -> app.hideModal(popup));
        popup.setOnToken(this::addAccount);
        app.showModal(popup);
    }
}
