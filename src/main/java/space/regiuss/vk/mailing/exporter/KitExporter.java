package space.regiuss.vk.mailing.exporter;

import javafx.scene.control.ListView;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface KitExporter<T extends ImageItemWrapper<Page>> {

    void export(ListView<T> listView, File file, CompletableFuture<?> completableFuture);

}
