package com.geekbrains.sep22.geekcloudclient;

import com.geekbrains.DaemonThreadFactory;
import com.geekbrains.model.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class CloudMainController implements Initializable {
    public ListView<String> clientView;
    public ListView<String> serverView;
    private String currentDirectory;
    private String selectedItem;
    private boolean isClientView = true;

    private Network<ObjectDecoderInputStream, ObjectEncoderOutputStream> network;

    private Socket socket;

    private boolean needReadMessages = true;

    private DaemonThreadFactory factory;

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileRequest(fileName));
    }

    public void sendToServer(ActionEvent actionEvent) throws IOException {
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
                }
            }
        } catch (Exception e) {
            System.err.println("Server off");
            e.printStackTrace();
        }
    }

    private void initNetwork() {
        try {
            socket = new Socket("localhost", 8189);
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
        if (showConfirm().getModalResult()) {
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
        RenameFormController renameFormController = showRenameForm();
        if (renameFormController.getModalResult()) {
            if (isClientView) {
                renameLocalForm(renameFormController.getNewName());
            } else {
                log.debug("server file selected");
                renameOnServerForm(renameFormController.getNewName());
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

    private RenameFormController showRenameForm() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("rename-form.fxml"));
        Parent parent = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(parent));

        stage.initModality(Modality.WINDOW_MODAL);

        stage.showAndWait();

        RenameFormController renameFormController = loader.getController();
        return renameFormController;
    }

    private ConfirmChoiceFormController showConfirm() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("choice-trust-form.fxml"));
        Parent parent = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(parent));

        stage.initModality(Modality.WINDOW_MODAL);

        stage.showAndWait();

        ConfirmChoiceFormController confirmForm = loader.getController();
        return confirmForm;

    }


    private void showError(String error) {
        log.debug(error);
//                TODO 03-10-2022 показать ошибку клиенту
    }


}
