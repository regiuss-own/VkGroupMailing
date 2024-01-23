package space.regiuss.vk.mailing;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import space.regiuss.version.client.VersionClient;

@Component
@RequiredArgsConstructor
public class MailingVersionClient extends VersionClient {

    @Value("${app.version}")
    private final String appVersion;
    @Value("${app.name}")
    private final String appName;

    @Override
    public String getVersion() {
        return appVersion;
    }

    @Override
    public String getProjectName() {
        return appName;
    }
}
