package space.regiuss.vk.mailing.node;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.popup.SelectAccountPopup;
import space.regiuss.vk.mailing.service.AccountService;
import space.regiuss.rgfx.node.Icon;
import space.regiuss.rgfx.spring.RGFXAPP;

@Getter
@Setter
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class SelectAccountButton extends Button {

    private final ObjectProperty<Account> currentAccount = new SimpleObjectProperty<>();
    private final AccountService accountService;
    private final VkMailingApp app;

    {
        currentAccount.addListener((observableValue, account, t1) -> {
            setText("Аккаунт: " + (t1 == null ? "Не выбрано" : t1.getName()));
        });
        getStyleClass().add("white");
        setGraphic(new Icon(Icon.IconValue.USER, Icon.IconType.REGULAR, 15, Paint.valueOf("#000")));
        setFont(Font.font(16));
        setText("Аккаунт: Не выбрано");
        setOnAction(this::onAction);
    }

    public static SelectAccountButton getInstance() {
        return RGFXAPP.getContext().getBean(SelectAccountButton.class);
    }

    public void onAction(ActionEvent event) {
        SelectAccountPopup popup = app.getBean(SelectAccountPopup.class);
        popup.getCurrentAccount().set(currentAccount.get());
        popup.setOnClose(() -> app.hideModal(popup));
        popup.setOnConfirm(currentAccount::set);
        app.showModal(popup);
    }

    public void selectAccountById(int accountId) {
        Account account = accountService.getById(accountId);
        if (account != null)
            currentAccount.set(account);
    }
}
