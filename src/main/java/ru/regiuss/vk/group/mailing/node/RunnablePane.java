package ru.regiuss.vk.group.mailing.node;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import ru.regiuss.vk.group.mailing.RGFXAPP;

import java.util.Objects;

@Getter
@Setter
public abstract class RunnablePane extends VBox {
    private ObjectProperty<Node> header = new SimpleObjectProperty<>();
    private ObjectProperty<Node> content = new SimpleObjectProperty<>();
    private ObjectProperty<Node> footer = new SimpleObjectProperty<>();

    @FXML
    private HBox headerNode;

    @FXML
    private StackPane contentNode;

    @FXML
    private HBox footerNode;

    public RunnablePane() {
        header.addListener((observableValue, node, t1) -> updateNode(headerNode, node, t1));
        content.addListener((observableValue, node, t1) -> updateNode(contentNode, node, t1));
        footer.addListener((observableValue, node, t1) -> updateNode(footerNode, node, t1));

        RGFXAPP.load(this, getClass().getResource("/view/runnable.fxml"));
    }

    @FXML
    public abstract void onStart(ActionEvent event);

    @FXML
    public abstract void onStop(ActionEvent event);

    private void updateNode(Pane own, Node node, Node t1) {
        if (Objects.nonNull(node))
            own.getChildren().remove(node);
        if (Objects.nonNull(t1))
            own.getChildren().add(t1);
    }

    public Node getHeader() {
        return header.get();
    }

    public ObjectProperty<Node> headerProperty() {
        return header;
    }

    public void setHeader(Node header) {
        this.header.set(header);
    }

    public Node getContent() {
        return content.get();
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    public Node getFooter() {
        return footer.get();
    }

    public ObjectProperty<Node> footerProperty() {
        return footer;
    }

    public void setFooter(Node footer) {
        this.footer.set(footer);
    }
}
