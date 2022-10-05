package com.geekbrains.sep22.geekcloudclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class RenameFormController implements Initializable {
    private String newFileName = null;
    private boolean isRenamed = false;
    @FXML
    private TextField newFileNameField;

    public void init(String fileName){
        newFileNameField.setText(fileName);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void onRenameClick(ActionEvent event){
        isRenamed = true;
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }

    public void onCancelClick(ActionEvent event){
        isRenamed = false;
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }

    public boolean getModalResult() {
        return isRenamed;
    }

    public String getNewName(){
        return newFileNameField.getText();
    }

}
