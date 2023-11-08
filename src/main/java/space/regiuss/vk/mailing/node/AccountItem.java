package space.regiuss.vk.mailing.node;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import lombok.Getter;
import space.regiuss.vk.mailing.model.Account;

@Getter
public class AccountItem extends HBox {
    protected final Account account;
    protected final ImageView icon;
    protected final Label nameLabel;

    public AccountItem(Account account) {
        this.account = account;
        setMaxWidth(300);
        setPrefWidth(300);
        setSpacing(10);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("account-item");

        icon = new javafx.scene.image.ImageView(new Image(account.getIcon(), true));
        icon.setFitWidth(80);
        icon.setFitHeight(80);
        icon.setClip(new Circle(40, 40, 40));
        getChildren().add(icon);

        nameLabel = new Label(account.getName());
        nameLabel.setFont(Font.font(16));
        VBox infoBox = new VBox(nameLabel);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(infoBox);
    }
}
