package com.geekbrains.sep22.geekcloudclient.subFormsControllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwoFieldFormController extends ChoiceFormController {
    @FXML
    private TextField field1;
    @FXML
    private TextField field2;

    @Override
    public void init(String... a) {
        log.debug(" two fields form init " + a[0] + " " + a[1]);
        field1.setText(a[0]);
        field2.setText(a[1]);
    }

    @Override
    public String[] getData() {
        log.debug("server address: " + field1.getText() + " port: " +  field2.getText());
        return new String[]{
                field1.getText(),
                field2.getText()
        };
    }
}
