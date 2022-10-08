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

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

    public void deleteFile(ActionEvent actionEvent) {
        if (clientView.isMouseTransparent()){
            try{
                Files.delete(Path.of(clientView.getSelectionModel().getSelectedItem()));
            } catch (IOException e) {
                showError("Error on delete file: " + e.getMessage());
//                throw new RuntimeException(e);
            }
        }
        if (serverView.isMouseTransparent()){

        }
    }

    public void renameFile(ActionEvent actionEvent) throws IOException {
        if (isClientView){
            renameLocalForm(new File(selectedItem));
        }
//            try{
//                renameLocalForm(new File(clientView.getSelectionModel().getSelectedItem()));
//            } catch (IOException e) {
//                showError("Error on rename file: " + e.getMessage());
////                throw new RuntimeException(e);
//            }

//        network.getOutputStream().writeObject(new FileRequest(fileName));
    }

    private void renameLocalForm(File file) throws IOException {
//        String newFileName = fileName;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("rename-form.fxml"));
        Parent parent = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(parent));

        stage.initModality(Modality.WINDOW_MODAL);

        stage.showAndWait();

        RenameFormController renameFormController = loader.getController();
        if(renameFormController.getModalResult()){
            String newFileName = renameFormController.getNewName();
            File newNameFile  = new File(newFileName);
            if (newNameFile.exists()){
                showError("File with name " + newFileName + " is exist ");
            } else {
                boolean success = file.renameTo(newNameFile);
            }
        }
    }
    private void showError(String error){

//                TODO 03-10-2022 показать ошибку клиенту
    }
}
