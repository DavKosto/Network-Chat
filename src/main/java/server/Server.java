package server;


import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class);

    private Vector<ClientHandler> clients;

    public Server() {
        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;
        try {
            LOGGER.info("Идет подключение к БД и к Серверу");
            AuthService.connect();
            server = new ServerSocket(8189);
            LOGGER.info("Сервер запущен. Ожидаем клиентов...");
            System.out.println("Сервер запущен. Ожидаем клиентов...");
            while (true) {
                socket = server.accept();
                LOGGER.info("Клиент подключился");
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                LOGGER.info("Сокет отключился");
                assert socket != null;
                socket.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
            try {
                LOGGER.info("Сервер отключился");
                server.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nickTo)) {
                LOGGER.info(o.getNick()+" отправил персональное сообщение "+nickTo);
                o.sendMsg("from " + from.getNick() + ": " + msg);
                from.sendMsg("to " + nickTo + ": " + msg);
                return;
            }
        }
        LOGGER.info("Клиент с ником " + nickTo + " не найден в чате");
        from.sendMsg("Клиент с ником " + nickTo + " не найден в чате");
    }

    public void broadcastMsg(ClientHandler from, String msg) {
        LOGGER.info("Пользователь " + from.getNick() + " отправил сообщения");
        for (ClientHandler o : clients) {
            if (!o.checkBlackList(from.getNick())) {
                o.sendHistoricalMsg(msg);
            }
        }
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientslist ");
        for (ClientHandler o : clients) {
            sb.append(o.getNick()).append(" ");
        }
        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientsList();
    }
}
