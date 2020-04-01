package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/client.fxml"));
        primaryStage.setTitle("2k19SummerChat");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> System.out.println("On Close"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
















//
//    Добавим в чат свой чужой
//        Сначала изменим объект textArea на ListView
//<!--<TextArea fx:id="chatArea" editable="false" VBox.vgrow="ALWAYS"/>-->
//<ListView fx:id="messagesView" editable="false" VBox.vgrow="ALWAYS" />
//        Далее добавим связку и добавим поле ник
//
//@FXML
//ListView messagesView;
//        String nick = "";
//        Далее напишем метод
//
//public void setMsg(String str) {
//        Platform.runLater(new Runnable() {
//@Override
//public void run() {
//        Label message = new Label(str);
//        VBox messageBox = new VBox(message);
//        if(nick != "") {
//        String[] mass = str.split(":");
//        if(nick.equalsIgnoreCase(mass[0])) {
//        messageBox.setAlignment(Pos.CENTER_RIGHT);
//        }
//        }
//        messagesView.getItems().add(messageBox);
//        }
//        });
//        }
//        переписываем отправку сообщений
//        while (true) {
//        String str = in.readUTF();
//        if (str.startsWith("/authok")) {
//        String[] mass = str.split(" ");
//        nick = mass[1];
//        setAuthorized(true);
//        break;
//        } else {
//        setMsg(str);
//        }
//        }
//        while (true) {
//        String str = in.readUTF();
//        if (str.equals("/serverclosed")) break;
//        setMsg(str);
//        }
//        далее при отправке сообщения добавляем ник (класс Clienthandler)
//        if (newNick != null) {
//        if(!server.isNickBusy(newNick)) {
//        sendMsg("/authok " + newNick);
//        nick = newNick;
//        server.subscribe(this);
//        break;
//        } else {
