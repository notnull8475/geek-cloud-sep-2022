package com.geekbrains.sep22.geekcloudclient;

import com.geekbrains.DaemonThreadFactory;
import com.geekbrains.model.*;
import com.geekbrains.sep22.geekcloudclient.subFormsControllers.ChoiceFormController;
import com.geekbrains.sep22.geekcloudclient.subFormsControllers.FormActions;
import com.geekbrains.sep22.geekcloudclient.subFormsControllers.RenameFormController;
import com.geekbrains.sep22.geekcloudclient.subFormsControllers.SubForms;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class CloudMainController extends SubForms implements Initializable {
    public ListView<String> clientView;
    public ListView<String> serverView;
    private String currentDirectory;
    private String selectedItem;
    private boolean isClientView = true;
    private String server = "127.0.0.1";
    private int port = 8189;


    private Network<ObjectDecoderInputStream, ObjectEncoderOutputStream> network;

    private Socket socket;

    private boolean needReadMessages = true;

    private DaemonThreadFactory factory;

    public void downloadFile() throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileRequest(fileName));
    }

    public void copyFile(ActionEvent actionEvent) throws IOException {
        if (isClientView)
            sendToServer();
        else
            downloadFile();
    }

    private void sendToServer() throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileMessage(Path.of(currentDirectory).resolve(fileName)));
    }

    private void readMessages() {
        try {
            while (needReadMessages) {
                CloudMessage message = (CloudMessage) network.getInputStream().readObject();
                if (message instanceof FileMessage fileMessage) {
                    Files.write(Path.of(currentDirectory).resolve(fileMessage.getFileName()), fileMessage.getBytes());
                    Platform.runLater(() -> fillView(clientView, getFiles(currentDirectory)));
                } else if (message instanceof ListMessage listMessage) {
                    Platform.runLater(() -> fillView(serverView, listMessage.getFiles()));
                } else if (message instanceof ResultMessage result) {
                    if (result.type().equals(ResultType.ERROR)){showError(result.message());}
                    if (result.type().equals(ResultType.SUCCESS)){showMessage(result.message());}
                    if (result.type().equals(ResultType.MESSAGE)){showMessage(result.message());}
                    if (result.type().equals(ResultType.AUTH_ERROR)){showError(result.message());}
                    if (result.type().equals(ResultType.AUTH_SUCCESS)){showMessage(result.message());}
                }
            }
        } catch (Exception e) {
            System.err.println("Server off");
            e.printStackTrace();
        }
    }



    private void initNetwork() {
        try {
            socket = new Socket(server, port);
            network = new Network<>(
                    new ObjectDecoderInputStream(socket.getInputStream()),
                    new ObjectEncoderOutputStream(socket.getOutputStream())
            );
            factory.getThread(this::readMessages, "cloud-client-read-thread")
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        needReadMessages = true;
        factory = new DaemonThreadFactory();
        initNetwork();
        setCurrentDirectory(System.getProperty("user.home"));
        fillView(clientView, getFiles(currentDirectory));

        clientView.setOnMouseClicked(event -> {
            isClientView = true;
            selectedItem = clientView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                File selectedFile = new File(currentDirectory + "/" + selectedItem);
                if (selectedFile.isDirectory()) {
                    setCurrentDirectory(currentDirectory + "/" + selectedItem);
                }
            }
        });
        serverView.setOnMouseClicked(mouseEvent -> {
            isClientView = false;
            selectedItem = serverView.getSelectionModel().getSelectedItem();
            if (mouseEvent.getClickCount() == 2) {
                try {
                    network.getOutputStream().writeObject(new DirFileListRequest(selectedItem));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void setCurrentDirectory(String directory) {
        currentDirectory = directory;
        fillView(clientView, getFiles(currentDirectory));
    }

    private void fillView(ListView<String> view, List<String> data) {
        view.getItems().clear();
        view.getItems().addAll(data);
    }

    private List<String> getFiles(String directory) {
        // file.txt 125 b
        // dir [DIR]
        File dir = new File(directory);
        if (dir.isDirectory()) {
            String[] list = dir.list();
            if (list != null) {
                List<String> files = new ArrayList<>(Arrays.asList(list));
                files.add(0, "..");
                return files;
            }
        }
        return List.of();
    }

    public void deleteFile(ActionEvent actionEvent) throws IOException {
        if (showConfirm().getResult()) {
            if (isClientView) {
                if (new File(currentDirectory + File.separator + selectedItem).delete()) {
                    log.debug("file is deleted");
                    fillView(clientView, getFiles(currentDirectory));
                }
                showError("FILE IS NOT DELETED");
            } else {
                try {
                    network.getOutputStream().writeObject(new DeleteFile(selectedItem));
                    network.getOutputStream().writeObject(new DirFileListRequest());
                } catch (IOException e) {
                    log.debug("ERROR Delete Failed " + e.getMessage());
                    showError("Delete failed " + e.getMessage());
                }
            }
        }
    }

    public void renameFile(ActionEvent actionEvent) throws IOException {
        RenameFormController renameFormController = showOneItemForm(FormActions.RENAME);
        if (renameFormController.getResult()) {
            if (isClientView) {
                renameLocalForm(renameFormController.getData()[0]);
            } else {
                log.debug("server file selected");
                renameOnServerForm(renameFormController.getData()[0]);
                network.getOutputStream().writeObject(new DirFileListRequest());
            }
        }
    }

    private void renameLocalForm(String newName) {
//        String newFileName = fileName;
        File file = new File(currentDirectory + File.separator + selectedItem);
        File newNameFile = new File(currentDirectory + File.separator + newName);
        if (newNameFile.exists()) {
            showError("File with name " + newName + " is exist ");
        } else {
            log.debug("file to rename " + file.getAbsolutePath());
            log.debug("new file name " + newNameFile.getAbsolutePath());
            if (file.renameTo(newNameFile)) {
                fillView(clientView, getFiles(currentDirectory));
            } else {
                showError("file not renamed");
            }

        }
    }


    private void renameOnServerForm(String newName) {
        try {
            network.getOutputStream().writeObject(new RenameFile(selectedItem, newName));
            network.getOutputStream().writeObject(new DirFileListRequest(selectedItem));
        } catch (IOException e) {
            log.debug("ERROR Rename Failed " + e.getMessage());
            showError("rename failed " + e.getMessage());
        }
    }

    private void showError(String error) {
        log.error("ERROR " + error);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Test Connection");

        // Header Text: null
        alert.setHeaderText(null);
        alert.setContentText(error);

        alert.showAndWait();
    }
    private void showMessage(String message) {
        log.debug("MESSAGE " + message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test Connection");

        // Header Text: null
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();

    }

    public void createNewFile(ActionEvent event) throws IOException {
        ChoiceFormController form = showOneItemForm(FormActions.CREATE);
        if (form.getResult()) {
            if (isClientView) {
                File file = new File(currentDirectory + File.separator + form.getData()[0]);
                if (file.exists()) {
                    showError("File is exist!");
                    return;
                }
                if (!file.createNewFile()) {
                    showError("file is not created");
                }
            } else {
                network.getOutputStream().writeObject(new CreateFileRequest(form.getData()[0]));
                network.getOutputStream().writeObject(new DirFileListRequest(selectedItem));
            }
        }
    }

    public void createNewPath(ActionEvent event) throws IOException {
        ChoiceFormController form = showOneItemForm(FormActions.CREATE);
        if (form.getResult()) {
            if (isClientView) {
                File file = new File(currentDirectory + File.separator + form.getData()[0]);
                if (file.exists()) {
                    showError("Path is exist!");
                    return;
                }
                if (!file.mkdirs()) {
                    showError("Path is not created");
                }
            } else {
                network.getOutputStream().writeObject(new CreatePathRequest(form.getData()[0]));
                network.getOutputStream().writeObject(new DirFileListRequest(selectedItem));
            }
        }
    }

    public void authorization(ActionEvent event) {
        try {
            ChoiceFormController form = showTwoFieldForm(FormActions.AUTH, null, null);
            network.getOutputStream().writeObject(new AuthorizationRequest(form.getData()[0], form.getData()[1]));
        } catch (IOException e) {
            showError("Проблема авторизации " + e.getMessage());
//            throw new RuntimeException(e);
        }

    }

    public void confEdit(ActionEvent actionEvent) {
        try {
            ChoiceFormController form = showTwoFieldForm(FormActions.CONF, server, String.valueOf(port));
            if (form.getResult()) {
                server = form.getData()[0];
                port = Integer.parseInt(form.getData()[1]);
//                TODO сделать в форме ввод только чисел для окна конфигурации.
            }
        } catch (IOException e) {
            showError("Ошибка изменения настроек " + e.getMessage());
//            throw new RuntimeException(e);
        }
    }


    public void registration(ActionEvent event) {
        try {
            ChoiceFormController form = showTwoFieldForm(FormActions.REGISTER, null, null);
            network.getOutputStream().writeObject(new RegistrationRequest(form.getData()[0], form.getData()[1]));
        } catch (IOException e) {
            showError("Проблема регистрации " + e.getMessage());
//            throw new RuntimeException(e);
        }
    }
}