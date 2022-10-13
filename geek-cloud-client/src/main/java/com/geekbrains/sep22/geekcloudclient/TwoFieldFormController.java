package com.geekbrains.sep22.geekcloudclient;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class TwoFieldFormController implements Initializable {
    private TextField field1;
    private TextField field2;
    private boolean result;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        result = false;
    }

    public void init(String f1, String f2){
        field1.appendText(f1);
        field2.appendText(f2);
    }

    public void accept(ActionEvent actionEvent) {
        result = true;
    }

    public void cancel(ActionEvent actionEvent) {
        ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
    }

    public boolean getResult() {
        return result;
    }

    public String[] getFieldsText() {
        return new String[]{
                field1.getText(),
                field2.getText()
        };
    }


}
