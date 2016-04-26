package com.theironyard;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) throws SQLException {

        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS games (id IDENTITY, gameName VARCHAR, genre VARCHAR, " +
                "platform VARCHAR, release_year INT)");
        // PreparedStatement stmt = conn.prepareStatement("COMMAND GOES HERE");

        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());

                    HashMap<Object, Object> m = new HashMap<>();
                    if (user == null) {
                        return new ModelAndView(m, "login.html");
                    }
                    else {
                        ArrayList<Game> gameArrayList = selectGames(conn);
                        m.put("games", gameArrayList);
                        return new ModelAndView(m, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/create-user",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name);
                        users.put(name, user);
                    }

                    Session session = request.session();
                    session.attribute("userName", name);

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-game",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    if (user == null) {
                        //throw new Exception("User is not logged in");
                        Spark.halt(403);
                    }

                    String gameName = request.queryParams("gameName");
                    String gameGenre = request.queryParams("gameGenre");
                    String gamePlatform = request.queryParams("gamePlatform");
                    Integer gameYear = Integer.valueOf(request.queryParams("gameYear"));
                    Game game = new Game(gameName, gameGenre, gamePlatform, gameYear);

                    insertGame(conn, gameName, gameGenre, gamePlatform, gameYear);

                    if (user != null) {
                        user.games.add(game);
                    }

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/delete-game",
                (request, response) -> {
                    Integer id = Integer.parseInt(request.queryParams("id"));
                    deleteGame(conn, id);

                    response.redirect("/");
                    return "";
                }
        );

        Spark.post(
                "/edit-game",
                (request, response) -> {
                    Integer id = Integer.parseInt(request.queryParams("ID"));
                    String name = request.queryParams("gameName");
                    String genre = request.queryParams("gameGenre");
                    String platform = request.queryParams("gamePlatform");
                    Integer gameYear = Integer.parseInt(request.queryParams("gameYear"));

                    updateGame(conn, id, name, genre, platform, gameYear);

                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );
    }

    static void updateGame(Connection conn, Integer id, String name, String genre,
                           String platform, Integer releaseYear) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("UPDATE GAMES SET GAMENAME = '" + name + "' WHERE ID = " + id);
        stmt.execute("UPDATE GAMES SET GENRE = '" + genre + "' WHERE ID = " + id);
        stmt.execute("UPDATE GAMES SET PLATFORM = '" + platform + "' WHERE ID = " + id);
        stmt.execute("UPDATE GAMES SET RELEASE_YEAR = " + releaseYear + " WHERE ID = " + id);
    }

    static User getUserFromSession(Session session) {
        String name = session.attribute("userName");
        return users.get(name);
    }

    static ArrayList<Game> selectGames(Connection conn) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM GAMES");
        while(results.next()){
            Integer id = results.getInt("ID");
            String name = results.getString("GAMENAME");
            String genre = results.getString("GENRE");
            String platform = results.getString("PLATFORM");
            Integer releaseYear = results.getInt("RELEASE_YEAR");

            games.add(new Game(id, name, genre, platform, releaseYear));
        }

        return games;
    }

    static void insertGame(Connection conn, String name, String genre, String platform, Integer releaseYear) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO games VALUES (NULL, ?, ?, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, genre);
        stmt.setString(3, platform);
        stmt.setInt(4, releaseYear);
        stmt.execute();
    }

    static void deleteGame(Connection conn, Integer id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM GAMES WHERE ID = " + id);
        stmt.execute();
    }
}
