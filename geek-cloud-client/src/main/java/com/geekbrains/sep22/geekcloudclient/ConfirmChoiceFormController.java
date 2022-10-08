package com.geekbrains.sep22.geekcloudclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ConfirmChoiceFormController implements Initializable {
    private boolean isConfirm;

    public boolean getModalResult() {
        return isConfirm;
    }

    public void noConfirm(ActionEvent event) {
        isConfirm = false;
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    public void yesConfirm(ActionEvent event) {
        isConfirm = true;
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isConfirm = false;
    }
}