package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextField msgField;

    @FXML
    HBox registrationPanel;

    @FXML
    TextField loginFieldRegistr;

    @FXML
    PasswordField passwordFieldRegistr;

    @FXML
    TextField nick;

    @FXML
    TextArea chatArea;

    @FXML
    HBox bottomPanel;

    @FXML
    HBox upperPanel;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;

    @FXML
    ListView<String> clientsList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private List<TextArea> textAreas;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
        textAreas = new ArrayList<>();
        textAreas.add(chatArea);
    }

    private void setAuthorized(boolean isAuthorized) {
        if (!isAuthorized) {
            registrationPanel.setVisible(true);
            registrationPanel.setManaged(true);
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
        }else {
            registrationPanel.setVisible(false);
            registrationPanel.setManaged(false);
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
        }
    }
    private void setCommunication(boolean isCommunication) {
        if (!isCommunication) {
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
        }
    }

    private void connect() {
        try {
            String IP_ADDRESS = "localhost";
            int PORT = 8189;
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            setAuthorized(false);
            Thread thread = new Thread(() -> {
                try {
                    while (true){
                        String str = in.readUTF();
                        if (str.startsWith("regsOk")) {
                            setAuthorized(true);
                            break;
                        } else {
                            chatArea.clear();
                            chatArea.appendText(str + "\n");
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("authOk")) {
                            setCommunication(true);
                            break;
                        } else {
                            for (TextArea o : textAreas) {
                                o.appendText(str + "\n");
                            }
                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/serverClosed")){
                                chatArea.clear();
                                setCommunication(false);
                                setAuthorized(false);
                                break;
                            }
                            if (str.startsWith("/clientsList ")) {
                                String[] tokens = str.split(" ");
                                Platform.runLater(() -> {
                                    clientsList.getItems().clear();
                                    for (int i = 1; i < tokens.length; i++) {
                                        clientsList.getItems().add(tokens[i]);
                                    }
                                });
                            }
                        } else {
                            chatArea.appendText(str + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthorized(false);
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToRegs(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF("regs " + loginFieldRegistr.getText() + " " +
                    passwordFieldRegistr.getText() + " " + nick.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth() {
        try {
            out.writeUTF("auth " + loginField.getText() + " " + passwordField.getText() + " ");
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAuthorizedPanel(ActionEvent actionEvent){
        if (socket == null || socket.isClosed()) {
            connect();
        }
        setAuthorized(true);
    }

    public void setRegistrationPanel(ActionEvent actionEvent){
        if (socket == null || socket.isClosed()) {
            connect();
        }
        setAuthorized(false);
    }


    public void selectClient(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2) {
            MiniStage ms = new MiniStage(clientsList.getSelectionModel().getSelectedItem(), out, textAreas);
            ms.show();
        }
    }
}
