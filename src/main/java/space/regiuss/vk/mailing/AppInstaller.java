package space.regiuss.vk.mailing;

import lombok.extern.slf4j.Slf4j;
import space.regiuss.util.pathutils.PathUtils;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

@Slf4j
public class AppInstaller implements Consumer<File> {

    @Override
    public void accept(File file) {
        File currentFile = PathUtils.getRunningFile();
        try {
            Files.copy(currentFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            log.error("install error", e);
            JOptionPane.showMessageDialog(null, "install error");
        }

        String filePath = file.toPath().toAbsolutePath().normalize().toString();

        try {
            Runtime.getRuntime().exec(new String[]{filePath, "clearTemp"});
        } catch (Exception e) {
            log.error("run error", e);
            JOptionPane.showMessageDialog(null, "run error");
        }
        System.exit(0);
    }

}
