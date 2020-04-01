package server;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class);

    private static Connection connection;
    private static Statement stmt;
    private static ArrayList<String> arrayBlacklist;

    static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            LOGGER.info("Идет подключение в БД");
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            LOGGER.info("Подклечен к БД");
            stmt = connection.createStatement();
            LOGGER.info("Подключен к Statement");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    static boolean nicknameIsThere(String nick) {
        String sql = String.format("SELECT id FROM main WHERE nickname = %s", nick);
        try {
            int id = 0;
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()){
                id = resultSet.getInt("id");
            }
            if (id != 0)
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    static boolean loginIsThere(String login) {
        String sql = String.format("SELECT id FROM main WHERE login = '%s'", login);
        try {
            int id = 0;
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()){
                id = resultSet.getInt("id");
            }
            if (id != 0)
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    static ArrayList getBlacklist(String nick){
        String sql = String.format("SELECT main.nickname as nickname FROM (SELECT main.id, main.nickname FROM main WHERE main.nickname = '%s') as m " +
                "INNER JOIN blacklist ON blacklist.id_user = m.id " +
                "INNER JOIN main ON blacklist.user_id_in_black_list = main.id", nick);
        try {
            arrayBlacklist = new ArrayList<>();
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()){
                arrayBlacklist.add(resultSet.getString("nickname"));
            }
        }catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        LOGGER.info("Черный список получен для пользователя " + nick);
        return arrayBlacklist;
    }

    static void addInBlackList(String nickUser, String nickBL){
        try {
            String query = "INSERT INTO blacklist (id_user, user_id_in_black_list) " +
            "VALUES (?, ?)";
            String idUser = String.format("SELECT main.id FROM main WHERE main.nickname in ('%s', '%s')", nickUser, nickBL);
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery(idUser);
            int i = 1;
            while (resultSet.next()){
                ps.setInt(i++, resultSet.getInt(1));
            }
            ps.execute();
            LOGGER.info("Пользователь " + nickUser + " успешно добавил пользователя " + nickBL + " в черный список");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    static void addNewUser(String login, String pass, String nick) {
        try {
            LOGGER.info("Добавление нового пользователья в БД");
            String query = "INSERT INTO main (login, password, nickname) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, login);
            ps.setInt(2, pass.hashCode());
            ps.setString(3, nick);
            ps.executeUpdate();
            LOGGER.info("Новый пользователь добавлен");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createTableMessageHistory(){
        LOGGER.info("Создание таблицы для истории сообшения");
        String sql = "CREATE TABLE MessageHistory (" +
                "id_user INTEGER NOT NULL" +
                "text TEXT NOT NULL" +
                "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP";
        try {
            stmt.executeUpdate(sql);
            LOGGER.info("Таблица для истории сообщения создана");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    static void insertMessageHistory(String nick, String text){
        try {
            ResultSet resultSet = stmt.executeQuery("SELECT main.id FROM main WHERE main.nickname = '" + nick+ "'");
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO MessageHistory (id_user, text) VALUES (?,?)");
            while (resultSet.next()){
                pstmt.setInt(1, resultSet.getInt("id"));
                pstmt.setString(2, text);
            }
            pstmt.executeUpdate();
            LOGGER.info("История сообщении для пользователя " + nick + " успешно добавлена");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    static LinkedList<String> getMessageHistory(){
        LinkedList<String> linkedList= new LinkedList();

        String sql = "SELECT MessageHistory.text, MessageHistory.time FROM main " +
                "INNER JOIN MessageHistory ON main.id = MessageHistory.id_user " +
                "ORDER BY MessageHistory.time";

        try {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()){
                linkedList.add(resultSet.getString("text"));
            }
            LOGGER.info("Загруга истории сообщении прошла успешно ");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return linkedList;
    }

    public static void updateMain(String newNick, String nick){
        try {
            PreparedStatement pstmt = connection.prepareStatement("UPDATE main SET nick = ? WHERE nick = ?");
            pstmt.setString(1, newNick);
            pstmt.setString(2, nick);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteFromMain(String nick){
        try {
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM main WHERE nick = ?");
            pstmt.setString(1, nick);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }

    }


    static String getNickByLoginAndPass(String login, String pass) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT nickname, password FROM main WHERE login = '" + login + "'");
            int myHash = pass.hashCode();
            if (rs.next()) {
                String nick = rs.getString(1);
                int dbHash = rs.getInt(2);
                if (myHash == dbHash) {
                    return nick;
                }
            }
        } catch (SQLException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    static void disconnect() {
        try {
            connection.close();
            LOGGER.info("Пользователь отсоеденился от БД");
        } catch (SQLException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
        }
    }
}
