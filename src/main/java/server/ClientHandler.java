package server;

import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ClientHandler {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class);

    private Server server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String nick;

    private List<String> blackList;
    String getNick() {
        return nick;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.blackList = new ArrayList<>();

            new Thread(() -> {
                try {

                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("regs")) {
                            String[] tokens = str.split(" ");
                            boolean isLogin = AuthService.loginIsThere(tokens[1]);
                            boolean isNick = AuthService.nicknameIsThere(tokens[3]);
                            if (isNick) {
                                sendMsg("Пользователь с Вашим ником существует." +
                                        " Пожалуйста, выберите для себя другой ник, и повторите регистрацию.");
                            }
                            if (isLogin) {
                                sendMsg("Пользователь с Вашим логином существует." +
                                        " Пожалуйста, выберите для себя другой логин, и повторите регистрацию.");
                            }
                            if (!isLogin && !isNick) {
                                AuthService.addNewUser(tokens[1], tokens[2], tokens[3]);
                                sendMsg("regsOk");
                                break;
                            }
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("auth")) { // /auth login72 pass72
                            LOGGER.info("Идет авторизация");
                            String[] tokens = str.split(" ");
                            LOGGER.info("Идет получения ника из БД");
                            String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                            LOGGER.info("Пользователя " + newNick + " авторизавался!");
                            if (newNick != null) {
                                if (!server.isNickBusy(newNick)) {
                                    LOGGER.info("Загрузка истории сообщения " + newNick + " из БД");
                                    LinkedList<String> linkedList = AuthService.getMessageHistory();
                                    nick = newNick;
                                    sendMsg("authOk");
                                    for (String s : linkedList) {
                                        sendMsg(s);
                                    }
                                    LOGGER.info(nick + " авторизацию прошол успешно");
                                    LOGGER.info("Загрузка черного списка для " + nick + " из БД");
                                    blackList = AuthService.getBlacklist(this.nick);
                                    LOGGER.info("Добавление пользователья в список подключенных");
                                    server.subscribe(this);
                                    break;
                                } else {
                                    LOGGER.info("Учетная запись уже используется");
                                    sendMsg("Учетная запись уже используется");
                                }
                            } else {
                                LOGGER.info("Неверный логин/пароль");
                                sendMsg("Неверный логин/пароль");
                            }
                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/exit")) {
                                LOGGER.info("Клиент хочет отключиться");
                                out.writeUTF("/serverClosed");
                                break;
                            }
                            if (str.startsWith("/w ")) {
                                String[] tokens = str.split(" ", 3);
                                LOGGER.info("Отправка персонального сообщения");
                                server.sendPersonalMsg(this, tokens[1], tokens[2]);
                            }
                            if (str.startsWith("/blacklist ")) { // /blacklist nick3
                                LOGGER.info("Добавление пользователя в черный список");
                                String[] tokens = str.split(" ");
                                LOGGER.info("Добавление пользователь " + tokens[1] + " в черный список");
                                AuthService.addInBlackList(this.nick, tokens[1]);
                                blackList.add(tokens[1]);
                                sendMsg("Вы добавили пользователя " + tokens[1] + " в черный список");
                            }
                        } else {
                            LOGGER.info("Пользователь " + nick + " отправил широковещательное сообщение");
                            server.broadcastMsg(this, nick + ": " + str);
                        }
                        System.out.println("Client: " + str);
                    }
                } catch (IOException e) {
                    LOGGER.info(e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                        LOGGER.info("DataInputStream закрылся");
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage());
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                        LOGGER.info("DataOutputStream закрылся");
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage());
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                        LOGGER.info("socket закрылся");
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage());
                        e.printStackTrace();
                    }
                    LOGGER.info("Удаление пользователья из подключенных " + nick);
                    server.unsubscribe(this);

                }
            }).start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean checkBlackList(String nick) {
        return blackList.contains(nick);
    }

    public void sendHistoricalMsg(String msg) {
        LOGGER.info("Добавления в историю пользователя сообщения " + this.nick);
        AuthService.insertMessageHistory(this.nick, msg);
        sendMsg(msg);
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
