package com.geekbrains.sep22.geekcloudclient.subFormsControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ChoiceFormController implements Initializable {
    boolean result = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    void init(String... a) {
    }

    @FXML
    void accept(ActionEvent event) {
        result = true;
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    void cancel(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    public String[] getData() {
        return new String[0];
    }

    public boolean getResult() {
        return result;
    }
}
