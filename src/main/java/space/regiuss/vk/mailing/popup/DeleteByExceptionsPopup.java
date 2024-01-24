package space.regiuss.vk.mailing.popup;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.popup.BackgroundPopup;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;
import space.regiuss.vk.mailing.model.Page;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;

@RequiredArgsConstructor
public class DeleteByExceptionsPopup extends BackgroundPopup {

    private final ListView<? extends ImageItemWrapper<Page>> listView;
    @Setter
    private Runnable onSuccess;

    @FXML
    private TextArea exclusionArea;

    {
        RGFXAPP.load(this, getClass().getResource("/view/popup/deleteByExceptionsPopup.fxml"));
    }

    @FXML
    public void onConfirm(ActionEvent event) {
        if (exclusionArea.getText().trim().isEmpty()) {
            return;
        }
        final String[] exclusionWords = exclusionArea.getText().split("\n");
        for (int i = 0; i < exclusionWords.length; i++) {
            exclusionWords[i] = exclusionWords[i].toLowerCase(Locale.ROOT);
        }
        LinkedList<ImageItemWrapper<Page>> removeItems = new LinkedList<>();
        ObservableList<? extends ImageItemWrapper<Page>> items = listView.getItems();
        for (ImageItemWrapper<Page> item : items) {
            for (String w : exclusionWords) {
                if (w.trim().isEmpty()) {
                    continue;
                }
                if (item.getItem().getName().toLowerCase(Locale.ROOT).contains(w)) {
                    removeItems.add(item);
                    break;
                }
            }
        }
        items.removeAll(removeItems);
        listView.refresh();
        getOnClose().run();
        if (Objects.nonNull(onSuccess)) {
            onSuccess.run();
        }
    }

    @FXML
    public void onCancel(ActionEvent event) {
        getOnClose().run();
    }
}
