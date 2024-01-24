package space.regiuss.vk.mailing.exporter;

import javafx.scene.control.ListView;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class DefaultKitExporter<T extends ImageItemWrapper<Page>> implements KitExporter<T> {

    @Override
    public void export(ListView<T> listView, File file, CompletableFuture<?> completableFuture) {
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(239);
            os.write(187);
            os.write(191);
            os.write("ссылка;id;тип;имя;подписчики;фото\n".getBytes(StandardCharsets.UTF_8));
            for (ImageItemWrapper<Page> item : listView.getItems()) {
                Page page = item.getItem();
                os.write(String.format(
                        "%s;%s;%s;%s;%s;%s%n",
                        page.getLink(),
                        page.getId(),
                        page.getType().name(),
                        page.getName().replace(";", ""),
                        page.getSubscribers(),
                        page.getIcon()
                ).getBytes(StandardCharsets.UTF_8));
            }
            completableFuture.complete(null);
        } catch (Exception e) {
            completableFuture.completeExceptionally(e);
        }
    }
}
