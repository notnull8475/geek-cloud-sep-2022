package com.geekbrains.sep22.geekcloudclient.subFormsControllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class TwoFieldFormController extends ChoiceFormController {
    @FXML
    private TextField field1;
    @FXML
    private TextField field2;

    @Override
    public void init(String... a) {
        field1.setText(a[0]);
        field2.setText(a[1]);
    }

    @Override
    public String[] getData() {
        return new String[]{
                field1.getText(),
                field2.getText()
        };
    }


}
