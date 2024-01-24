package space.regiuss.vk.mailing;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.Getter;
import org.springframework.context.support.GenericApplicationContext;
import space.regiuss.rgfx.AppInitData;
import space.regiuss.rgfx.node.Icon;
import space.regiuss.rgfx.node.Loader;
import space.regiuss.rgfx.node.MenuButton;
import space.regiuss.rgfx.node.RootSideBarPane;
import space.regiuss.rgfx.spring.RGFXAPP;
import space.regiuss.vk.mailing.screen.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class VkMailingApp extends RGFXAPP {
    private final Map<Class<?>, Toggle> buttons = new HashMap<>(16);

    @Override
    public void init(AppInitData appInitData) {
        appInitData
                .weight(1280)
                .height(720)
                .title("VkMailing")
                .icon(getClass().getResource("/img/icon.png"));
    }

    public void start() {
        currentScreen.addListener((observableValue, aClass, t1) -> {
            if (t1 == null)
                return;
            Toggle button = buttons.get(t1);
            if (button != null)
                button.setSelected(true);
        });
        showModal(new Loader());
    }

    @Override
    protected RootSideBarPane createRoot() {
        RootSideBarPane root = new RootSideBarPane();
        root.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());

        ToggleGroup menuToggleGroup = new ToggleGroup();
        Paint fill = Paint.valueOf("#333");
        int size = 20;
        root.setMenuItems(Arrays.asList(
                createMenuButton(
                        menuToggleGroup,
                        "По группам",
                        new Icon(Icon.IconValue.COMPASS, Icon.IconType.REGULAR, size, fill),
                        GroupRunnableScreen.class,
                        () -> openScreen(GroupRunnableScreen.class)
                ),
                createMenuButton(
                        menuToggleGroup,
                        "По избранному",
                        new Icon(Icon.IconValue.BOOKMARK, Icon.IconType.REGULAR, size, fill),
                        BookmarkRunnableScreen.class,
                        () -> openScreen(BookmarkRunnableScreen.class)
                ),
                createMenuButton(
                        menuToggleGroup,
                        "По профилям",
                        new Icon(Icon.IconValue.EYE, Icon.IconType.REGULAR, size, fill),
                        ProfileRunnableScreen.class,
                        () -> openScreen(ProfileRunnableScreen.class)
                ),
                createMenuButton(
                        menuToggleGroup,
                        "По почте",
                        new Icon(Icon.IconValue.ENVELOPE, Icon.IconType.REGULAR, size, fill),
                        ByMailRunnableScreen.class,
                        () -> openScreen(ByMailRunnableScreen.class)
                ),
                new Separator(),
                createMenuButton(
                        menuToggleGroup,
                        "Рассылка",
                        new Icon(Icon.IconValue.PAPER_PLANE, Icon.IconType.REGULAR, size, fill),
                        MailingRunnableScreen.class,
                        () -> openScreen(MailingRunnableScreen.class)
                ),
                new Separator(),
                createMenuButton(
                        menuToggleGroup,
                        "Аккаунты",
                        new Icon(Icon.IconValue.USER, Icon.IconType.REGULAR, size, fill),
                        AccountListScreen.class,
                        () -> openScreen(AccountListScreen.class)
                ),
                createMenuButton(
                        menuToggleGroup,
                        "Сообщения",
                        new Icon(Icon.IconValue.COMMENT, Icon.IconType.REGULAR, size, fill),
                        MessagesScreen.class,
                        () -> openScreen(MessagesScreen.class)
                )
                /*createMenuButton(
                        menuToggleGroup,
                        "Наборы",
                        new Icon(Icon.IconValue.FOLDER, Icon.IconType.REGULAR, size, fill),
                        null,
                        () -> openScreen(GroupRunnableScreen.class)
                )*/
        ));
        ImageView headerImage = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/icon.png"))));
        headerImage.setFitHeight(32);
        headerImage.setFitWidth(32);
        Text headerText = new Text("VkMailing");
        headerText.setFont(Font.font(20));
        HBox header = new HBox(
                headerImage,
                headerText
        );
        header.setPadding(new Insets(0, 10, 0, 10));
        header.setSpacing(15);
        header.setAlignment(Pos.CENTER_LEFT);
        root.setHeader(header);

        return root;
    }

    private MenuButton createMenuButton(ToggleGroup toggleGroup, String text, Node graphic, Class<?> screen, Runnable onSelect) {
        StackPane sp = new StackPane(graphic);
        sp.setMinWidth(32);
        MenuButton button = new MenuButton(sp);
        button.setToggleGroup(toggleGroup);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnSelect(onSelect);
        button.setGraphicTextGap(20);
        button.setFont(Font.font(16));
        button.setText(text);
        button.setAlignment(Pos.CENTER_LEFT);
        if (screen != null) {
            buttons.put(screen, button);
        }
        return button;
    }

    @Override
    public ScheduledExecutorService createExecutorService() {
        return Executors.newScheduledThreadPool(5, r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setName("App-Thread-Pool-");
            return t;
        });
    }

    @Override
    public void applyInitializer(GenericApplicationContext applicationContext) {
        applicationContext.registerBean(VkMailingApp.class, () -> this);
    }

    @Override
    public Class<?> getSpringBootSource() {
        return VkMailing.class;
    }
}
