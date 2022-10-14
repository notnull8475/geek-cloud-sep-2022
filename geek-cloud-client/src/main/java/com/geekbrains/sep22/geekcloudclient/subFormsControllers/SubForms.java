package com.geekbrains.sep22.geekcloudclient.subFormsControllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SubForms {
    public RenameFormController showOneItemForm(FormActions action) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        if (action.equals(FormActions.RENAME)) {
            loader.setLocation(getClass().getResource("rename-form.fxml"));
        } else if (action.equals(FormActions.CREATE)) {
            loader.setLocation(getClass().getResource("create-file-path-form.fxml"));
        } else {
            throw new RuntimeException("неверные данные формы");
        }
        Parent parent = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(parent));

        stage.initModality(Modality.WINDOW_MODAL);

        stage.showAndWait();

        return loader.getController();
    }

    public ChoiceFormController showConfirm() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("choice-trust-form.fxml"));
        Parent parent = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(parent));

        stage.initModality(Modality.WINDOW_MODAL);

        stage.showAndWait();

        return loader.getController();

    }

    public TwoFieldFormController showTwoFieldForm(FormActions action, String f1, String f2) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        if (action == FormActions.AUTH) {
            loader.setLocation(getClass().getResource("auth-form.fxml"));
        } else if (action == FormActions.CONF) {
            loader.setLocation(getClass().getResource("settings-form.fxml"));

        }
        Parent parent = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(parent));

        stage.initModality(Modality.WINDOW_MODAL);

        stage.showAndWait();

        TwoFieldFormController form = loader.getController();
        form.init(f1, f2);
        return form;
    }
}
