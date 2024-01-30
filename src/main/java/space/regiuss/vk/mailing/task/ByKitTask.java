package space.regiuss.vk.mailing.task;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.PageType;
import space.regiuss.vk.mailing.wrapper.DescriptionItemWrapper;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@RequiredArgsConstructor
public class ByKitTask extends Task<Void> {

    @Getter
    private final ListProperty<DescriptionItemWrapper<Page>> pageListProperty = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
    private final Pattern pattern;
    private final List<ImageItemWrapper<Page>> items;
    private final Messenger messenger;


    @Override
    protected Void call() {
        for (ImageItemWrapper<Page> wrapper : items) {
            if (isCancelled()) {
                return null;
            }
            Page page = wrapper.getItem();
            Callable<String> callable;
            if (page.getId().getPageType() == PageType.USER) {
                callable = () -> messenger.getUserInfoByIds(Collections.singletonList(page.getId().getPageId()))
                        .get(0).getJson();
            } else {
                callable = () -> messenger.getGroupInfoByIds(Collections.singletonList(page.getId().getPageId()))
                        .get(0).toString();
            }
            String result = null;
            for (int i = 0; i < 3; i++) {
                try {
                    result = callable.call();
                    break;
                } catch (Exception e) {
                    log.warn("call error {}/3", i);
                }
            }
            if (result == null) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            Matcher m = pattern.matcher(result);
            while (m.find()) {
                String f = m.group();
                sb.append(f).append(',');
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
                Platform.runLater(() -> pageListProperty.add(new DescriptionItemWrapper<>(page, sb.toString())));
            }
        }
        return null;
    }

}
