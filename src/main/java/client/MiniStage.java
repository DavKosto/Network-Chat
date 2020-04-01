package client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

class MiniStage extends Stage {
    String nickTo;
    DataOutputStream out;

    MiniStage(String nickTo, DataOutputStream out, List<TextArea> parentList) {
        this.nickTo = nickTo;
        this.out = out;
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("../../resources/personal.fxml"));
            setTitle("personal window");
            Scene scene = new Scene(root, 300, 400);
            setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
