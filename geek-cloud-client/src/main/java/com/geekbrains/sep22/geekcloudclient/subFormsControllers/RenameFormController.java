package com.geekbrains.sep22.geekcloudclient.subFormsControllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
public class RenameFormController extends ChoiceFormController {
    @FXML
    private TextField newFileNameField;

    @Override
    void init(String... a) {
        newFileNameField.setText(a[0]);
    }

    @Override
    public String[] getData() {
        return new String[]{newFileNameField.getText()};
    }
}
