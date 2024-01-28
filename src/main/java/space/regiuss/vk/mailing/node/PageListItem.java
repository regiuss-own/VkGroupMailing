package space.regiuss.vk.mailing.node;

import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import lombok.Getter;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.rgfx.node.Icon;

@Getter
public class PageListItem<T extends ImageItemWrapper<Page>> extends ListCell<T> {

    protected final HBox hBox;
    protected final ImageView imageView;
    protected final VBox infoBox;
    protected final Label nameLabel;
    protected final Label idLabel;
    protected final Label followersLabel;

    public PageListItem(HostServices hostServices) {
        setText(null);
        setPadding(new Insets(0, 0, 10, 0));
        hBox = new HBox();
        hBox.getStyleClass().add("rounded-box");
        hBox.setMaxWidth(Integer.MAX_VALUE);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10));

        imageView = new ImageView();
        imageView.setOnMouseClicked(event -> {
            hostServices.showDocument(getItem().getItem().getLink());
        });
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setClip(new Circle(40, 40, 40));
        hBox.getChildren().add(imageView);

        nameLabel = new Label();
        nameLabel.setOnMouseClicked(event -> {
            hostServices.showDocument(getItem().getItem().getLink());
        });
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(450);
        Font font = Font.font(16);
        nameLabel.setFont(font);
        idLabel = new Label();
        idLabel.setGraphic(new Icon(Icon.IconValue.CODE, Icon.IconType.SOLID, 16, Paint.valueOf("#000")));
        idLabel.setFont(font);
        followersLabel = new Label();
        followersLabel.setGraphic(new Icon(Icon.IconValue.USER, Icon.IconType.REGULAR, 16, Paint.valueOf("#000")));
        followersLabel.setFont(font);
        infoBox = new VBox(nameLabel, idLabel, followersLabel);
        hBox.getChildren().add(infoBox);
    }

    @Override
    protected void updateItem(T wrapper, boolean empty) {
        super.updateItem(wrapper, empty);
        if (empty || wrapper == null) {
            setGraphic(null);
            return;
        }
        Page page = wrapper.getItem();
        if (page.getIcon() != null) {
            if (wrapper.getImage() == null)
                wrapper.setImage(new Image(page.getIcon(), true));
            imageView.setImage(wrapper.getImage());
        }
        nameLabel.setText(page.getName());
        idLabel.setText(page.getId().getPageId().toString());
        if (page.getSubscribers() > -1) {
            followersLabel.setText(Integer.toString(page.getSubscribers()));
            followersLabel.setManaged(true);
            followersLabel.setVisible(true);
        } else {
            followersLabel.setManaged(false);
            followersLabel.setVisible(false);
        }
        setGraphic(hBox);
    }
}
