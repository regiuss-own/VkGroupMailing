package space.regiuss.vk.mailing.node;

import javafx.application.HostServices;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.wrapper.DescriptionItemWrapper;

public class DescriptionPageListItem extends PageListItem<DescriptionItemWrapper<Page>> {

    protected final Text descriptionText;

    public DescriptionPageListItem(HostServices hostServices) {
        super(hostServices);
        descriptionText = new Text();
        descriptionText.setFont(Font.font(16));
        infoBox.getChildren().add(descriptionText);
    }

    @Override
    protected void updateItem(DescriptionItemWrapper<Page> wrapper, boolean empty) {
        super.updateItem(wrapper, empty);
        if (wrapper == null || empty)
            return;
        descriptionText.setText(wrapper.getDescription());
    }

}
