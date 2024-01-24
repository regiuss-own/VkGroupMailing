package space.regiuss.vk.mailing.node;

import javafx.application.HostServices;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.wrapper.EmailItemWrapper;

public class EmailPageListItem extends PageListItem<EmailItemWrapper<Page>> {

    protected final Text emailText;

    public EmailPageListItem(HostServices hostServices) {
        super(hostServices);
        emailText = new Text();
        emailText.setFont(Font.font(16));
        infoBox.getChildren().add(emailText);
    }

    @Override
    protected void updateItem(EmailItemWrapper<Page> wrapper, boolean empty) {
        super.updateItem(wrapper, empty);
        if (wrapper == null || empty)
            return;
        emailText.setText(wrapper.getEmail());
    }

}
