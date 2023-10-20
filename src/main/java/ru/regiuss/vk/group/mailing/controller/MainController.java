package ru.regiuss.vk.group.mailing.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.log4j.Log4j2;
import ru.regiuss.vk.group.mailing.MessageListCell;
import ru.regiuss.vk.group.mailing.VkGroupApp;
import ru.regiuss.vk.group.mailing.enums.BookmarkType;
import ru.regiuss.vk.group.mailing.messenger.Messenger;
import ru.regiuss.vk.group.mailing.messenger.VkMessenger;
import ru.regiuss.vk.group.mailing.model.*;
import ru.regiuss.vk.group.mailing.popup.AuthPopup;
import ru.regiuss.vk.group.mailing.popup.MessagePopup;
import ru.regiuss.vk.group.mailing.popup.WarnPopup;
import ru.regiuss.vk.group.mailing.task.BookmarkMailingTask;
import ru.regiuss.vk.group.mailing.task.SearchMailingTask;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@Log4j2
@SuppressWarnings("unused")
public class MainController implements Initializable {

    private final VkGroupApp app;

    @FXML
    private VBox bookmarkTaskBox;

    @FXML
    private VBox groupTaskBox;

    @FXML
    private ToggleGroup taskTypeGroup;

    @FXML
    private CheckBox sortCheckBox;

    @FXML
    private ComboBox<BookmarkType> bookmarkType;

    @FXML
    private TextField dialogDelayField;

    @FXML
    private TextField messageDelayField;

    @FXML
    private ListView<Message> messagesList;

    @FXML
    private TextField minSubCountField;

    @FXML
    private TextField maxSubCountField;

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField searchField;

    @FXML
    private HBox settingsPane;

    @FXML
    private Label runStatus;

    @FXML
    private TextField tokenField;

    @FXML
    private Label tokenStatus;

    private Task<?> task;

    public MainController(VkGroupApp app) {
        this.app = app;
    }

    @FXML
    void onAddMessage(ActionEvent event) {
        MessagePopup popup = new MessagePopup(app);
        popup.setOnClose(() -> rootPane.getChildren().remove(popup));
        popup.setOnMessage(message -> {
            if (message != null)
                messagesList.getItems().add(message);
        });
        rootPane.getChildren().add(popup);
    }

    @FXML
    void onAuth(ActionEvent event) {
        AuthPopup popup = new AuthPopup();
        popup.setOnClose(() -> rootPane.getChildren().remove(popup));
        popup.setOnToken(token -> tokenField.setText(token));
        rootPane.getChildren().add(popup);
    }

    @FXML
    void onCheckToken(ActionEvent event) {
        final String token = tokenField.getText();
        app.getExecutorService().execute(() -> {
            final User user = VkMessenger.getUser(token);
            Platform.runLater(() -> {
                if (user == null)
                    tokenStatus.setText("Ошибка");
                else
                    tokenStatus.setText("Рабочий");
            });
        });
    }

    @FXML
    void onDeleteMessage(ActionEvent event) {
        ObservableList<Message> items = messagesList.getItems();
        if (items.isEmpty())
            return;
        int selectedIndex = messagesList.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1)
            selectedIndex = items.size() - 1;
        items.remove(selectedIndex);
    }

    @FXML
    void onEditMessage(ActionEvent event) {
        int selectedIndex = messagesList.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1)
            return;
        MessagePopup popup = new MessagePopup(app);
        popup.setOnClose(() -> rootPane.getChildren().remove(popup));
        popup.setMessage(messagesList.getItems().get(selectedIndex));
        popup.setOnMessage(message -> messagesList.refresh());
        rootPane.getChildren().add(popup);
    }

    private void fillMailingData(MailingData data) {
        data.setMessages(new ArrayList<>(messagesList.getItems()));
        data.setGroupDelay(Integer.parseInt(dialogDelayField.getText()) * 1000);
        data.setMessageDelay(Integer.parseInt(messageDelayField.getText()) * 1000);
    }

    private void saveData() {
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("settings"))) {
            os.writeUTF(tokenField.getText());
            if (fieldIsInvalid(minSubCountField))
                minSubCountField.setText("0");
            os.writeUTF(minSubCountField.getText());
            if (fieldIsInvalid(dialogDelayField))
                dialogDelayField.setText("0");
            os.writeUTF(dialogDelayField.getText());
            if (fieldIsInvalid(messageDelayField))
                messageDelayField.setText("0");
            os.writeUTF(messageDelayField.getText());
            os.writeUTF(searchField.getText());
            ObservableList<Message> messages = messagesList.getItems();
            os.writeInt(messages.size());
            for (Message message : messages) {
                os.writeUTF(message.getText());
                List<Attachment> attachments = message.getAttachments();
                if (attachments == null)
                    os.writeInt(0);
                else {
                    os.writeInt(attachments.size());
                    for (Attachment attachment : attachments) {
                        os.writeUTF(attachment.getFile().getAbsolutePath());
                        os.writeBoolean(attachment.isDocument());
                    }
                }
            }
            os.writeUTF(maxSubCountField.getText());
            os.writeInt(taskTypeGroup.getToggles().get(0).isSelected() ? 0 : 1);
            os.writeInt(bookmarkType.getSelectionModel().getSelectedIndex());
            os.writeBoolean(sortCheckBox.isSelected());
        } catch (Exception e) {
            log.warn("save data error", e);
        }
    }

    private void loadData() {
        File settingsFile = new File("settings");
        if (!settingsFile.exists())
            return;
        try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(settingsFile))) {
            tokenField.setText(is.readUTF());
            minSubCountField.setText(is.readUTF());
            dialogDelayField.setText(is.readUTF());
            messageDelayField.setText(is.readUTF());
            searchField.setText(is.readUTF());
            int messagesCount = is.readInt();
            if (messagesCount < 1)
                return;
            List<Message> messages = new ArrayList<>(messagesCount);
            for (int i = 0; i < messagesCount; i++) {
                Message message = new Message();
                messages.add(message);
                message.setText(is.readUTF());
                int attachmentsCount = is.readInt();
                if (attachmentsCount < 1)
                    continue;
                List<Attachment> attachments = new ArrayList<>(attachmentsCount);
                message.setAttachments(attachments);
                for (int j = 0; j < attachmentsCount; j++) {
                    Attachment attachment = new Attachment();
                    attachments.add(attachment);
                    attachment.setFile(new File(is.readUTF()));
                    attachment.setDocument(is.readBoolean());
                }
            }
            messagesList.setItems(FXCollections.observableList(messages));
            maxSubCountField.setText(is.readUTF());
            taskTypeGroup.getToggles().get(is.readInt()).setSelected(true);
            bookmarkType.getSelectionModel().select(is.readInt());
            sortCheckBox.setSelected(is.readBoolean());
        } catch (Exception e) {
            log.warn("load data error", e);
        }
    }

    @FXML
    void onStart(ActionEvent event) {
        Button startButton = (Button) event.getTarget();

        if (task == null) {
            if (fieldIsInvalid(tokenField)) {
                WarnPopup popup = new WarnPopup("Ошибка", "Заполните поле Токен");
                popup.setOnClose(() -> rootPane.getChildren().remove(popup));
                rootPane.getChildren().add(popup);
                return;
            }
            if (messagesList.getItems().isEmpty()) {
                WarnPopup popup = new WarnPopup("Ошибка", "Укажите хотябы одно сообщение");
                popup.setOnClose(() -> rootPane.getChildren().remove(popup));
                rootPane.getChildren().add(popup);
                return;
            }
            Messenger messenger = new VkMessenger(tokenField.getText());
            try {
                if (taskTypeGroup.getToggles().get(0).isSelected()) {
                    SearchMailingData data = new SearchMailingData();
                    data.setSearch(searchField.getText());
                    data.setMinSubscribers(Integer.parseInt(minSubCountField.getText()));
                    data.setMaxSubscribers(Integer.parseInt(maxSubCountField.getText()));
                    data.setSort(sortCheckBox.isSelected());
                    fillMailingData(data);
                    task = new SearchMailingTask(messenger, data);
                } else {
                    BookmarkMailingData data = new BookmarkMailingData();
                    data.setType(bookmarkType.getSelectionModel().getSelectedItem());
                    fillMailingData(data);
                    task = new BookmarkMailingTask(messenger, data);
                }
            } catch (Exception e) {
                log.warn("get mailing data error", e);
                WarnPopup popup = new WarnPopup("Ошибка", "Проверьте правильность введенных данных");
                popup.setOnClose(() -> rootPane.getChildren().remove(popup));
                rootPane.getChildren().add(popup);
                return;
            }
            saveData();
            startButton.setText("Стоп");
            settingsPane.setDisable(true);
            EventHandler<WorkerStateEvent> handler = workerStateEvent -> {
                Throwable t = task.getException();
                clear(startButton);
                WarnPopup popup = t == null
                        ? new WarnPopup("Информация", "Рассылка завершена")
                        : new WarnPopup("Ошибка", t.getMessage());
                popup.setOnClose(() -> rootPane.getChildren().remove(popup));
                rootPane.getChildren().add(popup);
            };
            task.setOnSucceeded(handler);
            task.setOnFailed(handler);
            runStatus.textProperty().bind(task.messageProperty());
            app.getExecutorService().execute(task);
        } else {
            task.cancel(true);
            clear(startButton);
        }
    }

    private void clear(Button startButton) {
        startButton.setText("Старт");
        settingsPane.setDisable(false);
        runStatus.textProperty().unbind();
        runStatus.setText(runStatus.getText().replace("Запущено", "Не запущено"));
        task = null;
    }

    private boolean fieldIsInvalid(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private void setVisible(Parent parent, boolean visible) {
        parent.setManaged(visible);
        parent.setVisible(visible);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messagesList.setCellFactory(messageListView -> new MessageListCell());
        bookmarkType.setItems(FXCollections.observableList(Arrays.asList(BookmarkType.values())));
        taskTypeGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            if (t1 == null) {
                toggle.setSelected(true);
                return;
            }
            if (taskTypeGroup.getToggles().get(0).isSelected()) {
                setVisible(groupTaskBox, true);
                setVisible(bookmarkTaskBox, false);
            } else {
                setVisible(bookmarkTaskBox, true);
                setVisible(groupTaskBox, false);
            }
        });
        loadData();
        if (taskTypeGroup.getSelectedToggle() == null)
            taskTypeGroup.getToggles().get(0).setSelected(true);
        if (bookmarkType.getSelectionModel().isEmpty())
            bookmarkType.getSelectionModel().select(0);
        log.info(bookmarkType.getSelectionModel().isEmpty());
    }
}
