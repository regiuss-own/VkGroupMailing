package ru.regiuss.vk.group.mailing.screen;

import javafx.event.ActionEvent;
import lombok.extern.log4j.Log4j2;
import ru.regiuss.vk.group.mailing.RGFXAPP;
import ru.regiuss.vk.group.mailing.node.RunnablePane;

@Log4j2
public class GroupRunnableScreen extends RunnablePane {
    public GroupRunnableScreen() {
        RGFXAPP.load(this, getClass().getResource("/view/group.fxml"));
    }

    @Override
    public void onStart(ActionEvent event) {
        log.info("start");
    }

    @Override
    public void onStop(ActionEvent event) {
        log.info("stop");
    }
}
