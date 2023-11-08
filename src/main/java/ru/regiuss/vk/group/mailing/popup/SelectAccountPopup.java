package ru.regiuss.vk.group.mailing.popup;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.regiuss.vk.group.mailing.model.Account;
import ru.regiuss.vk.group.mailing.node.AccountItem;
import ru.regiuss.vk.group.mailing.service.AccountService;
import space.regiuss.rgfx.node.TileView;
import space.regiuss.rgfx.popup.BackgroundPopup;
import space.regiuss.rgfx.spring.RGFXAPP;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Getter
public class SelectAccountPopup extends BackgroundPopup {

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private final ObjectProperty<Account> currentAccount = new SimpleObjectProperty<>();
    @Setter
    private Consumer<Account> onConfirm;
    public TileView<AccountItem> accountsPane;
    private final AccountService accountService;

    {
        RGFXAPP.load(this, getClass().getResource("/view/popup/selectAccountPopup.fxml"));
        accountsPane.getScrollPane().setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        currentAccount.addListener((o, oldValue, newValue) -> {
            if (oldValue != null)
                accountsPane.getItems().stream().filter(item -> item.getAccount().equals(oldValue)).findFirst().get()
                        .pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
            if (newValue != null)
                accountsPane.getItems().stream().filter(item -> item.getAccount().equals(newValue)).findFirst().get()
                        .pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
        });
    }

    @PostConstruct
    public void init() {
        for (Account account : accountService.getAll()) {
            AccountItem item = new AccountItem(account);
            item.setOnMouseClicked(event -> currentAccount.set(account));
            accountsPane.add(item);
        }
    }

    @FXML
    public void onConfirm(ActionEvent event) {
        onConfirm.accept(currentAccount.get());
        onClose.run();
    }
}
