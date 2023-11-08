package space.regiuss.vk.mailing.node;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import space.regiuss.rgfx.RGFXAPP;

public class AccountAndMessageHeader extends HBox {
    public AccountAndMessageHeader() {
        RGFXAPP.load(this, getClass().getResource("/view/accountAndMessage.fxml"));
    }

    @FXML
    public void onSelectAccountClick(ActionEvent event) {

    }

    @FXML
    public void onSelectMessageClick(ActionEvent event) {

    }
}
