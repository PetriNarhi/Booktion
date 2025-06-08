package com.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController 
public class VoteController {

    private final Map<String, AtomicInteger> bookVotes = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> movieVotes = new ConcurrentHashMap<>();


    @PostMapping("/vote")

    public Map<String, Integer> vote( @RequestParam String query, @RequestParam String type){
        try{
            System.out.println("Title " + query);
            sendVote(query, type);

        } catch (Exception e){
            e.printStackTrace();
        }

        if ("book".equals(type)) {
            bookVotes.computeIfAbsent(query.toLowerCase(), k -> new AtomicInteger(0)).incrementAndGet();
        }else if ("movie".equals(type)) {
            movieVotes.computeIfAbsent(query.toLowerCase(), k -> new AtomicInteger(0)).incrementAndGet();
        }

        Map<String, Integer> res = new HashMap<>();
        res.put("bookVotes", bookVotes.getOrDefault(query.toLowerCase(), new AtomicInteger(0)).get());
        res.put("movieVotes", movieVotes.getOrDefault(query.toLowerCase(), new AtomicInteger(0)).get());

        return res;
    }


    @GetMapping("/votes")
public Map<String, Integer> getVotes(@RequestParam String query) {
    Map<String, Integer> res = new HashMap<>();
    int bookVotes = 0;
    int movieVotes = 0;

    Connection con = connectDatabase();
    if (con != null) {
        try{
            PreparedStatement stmt = con.prepareStatement(
                "SELECT bookvotes, movievotes FROM votes WHERE LOWER(title) = LOWER(?)"
            );
            stmt.setString(1, query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()){
                bookVotes = rs.getInt("bookvotes");
                movieVotes = rs.getInt("movievotes");
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    res.put("bookVotes", bookVotes);
    res.put("movieVotes", movieVotes);
    return res;
}


@GetMapping("/votes/total")
public Map<String, Integer> getTotalVotes() {
    int totalBookVotes = 0;
    int totalMovieVotes = 0;

    Connection con = connectDatabase();
    if (con != null) {
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SUM(bookvotes) AS totalBookVotes, SUM(movievotes) AS totalMovieVotes FROM votes");
            if (rs.next()) {
                totalBookVotes = rs.getInt("totalBookVotes");
                totalMovieVotes = rs.getInt("totalMovieVotes");
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    Map<String, Integer> res = new HashMap<>();
    res.put("bookVotes", totalBookVotes);
    res.put("movieVotes", totalMovieVotes);
    return res;
}


    public Connection connectDatabase() {

        String url = "jdbc:postgresql://pgserver.mau.se:5432/ao7221";
        String user = "ao7221";
        String password = "sqshnv5y";


        try{
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection Established");
            return conn;

        } catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
            return null;
        }

    }

    private void sendVote(String title, String type) throws Exception {
        Connection con = connectDatabase();
        if (con == null) {
            throw new SQLException("Failed to connect to the database");
        }

        try{
            PreparedStatement checkStmt = con.prepareStatement("SELECT * FROM votes WHERE LOWER(title) = LOWER(?)");
            checkStmt.setString(1, title);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()){
                int currentVotes = rs.getInt(type.equals("book") ? "bookvotes" : "movievotes");
                int newVotes = currentVotes + 1;

                String updateSql = type.equals("book") ?
                        "UPDATE votes SET bookvotes = ? WHERE LOWER(title) = LOWER(?)" : "UPDATE votes SET movievotes = ? WHERE LOWER(title) = LOWER(?)";

                PreparedStatement updateStmt = con.prepareStatement(updateSql);
                updateStmt.setInt(1, newVotes);
                updateStmt.setString(2, title);
                updateStmt.executeUpdate();
                updateStmt.close();

            }else{
                int book = type.equals("book") ? 1 : 0;
                int movie = type.equals("movie") ? 1 : 0;

                PreparedStatement insertStmt = con.prepareStatement("INSERT INTO votes (title, bookvotes, movievotes) VALUES (?, ?, ?)");
                insertStmt.setString(1, title);
                insertStmt.setInt(2, book);
                insertStmt.setInt(3, movie);

                insertStmt.executeUpdate();
                insertStmt.close();
            }
            rs.close();
            checkStmt.close();
            con.close();

        } catch (SQLException e){
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/leaderboard/movies")

    public List <Map <String, Object>> topMovies(){

        List <Map<String, Object>> result = new ArrayList<>();
        Connection con = connectDatabase();

        if (con != null){
            try{
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT title, movievotes FROM votes WHERE movievotes > 0 ORDER BY movievotes DESC LIMIT 5");

                while (rs.next()){
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", rs.getString("title"));
                    map.put("votes", rs.getInt("movievotes"));
                    result.add(map);
                }
                rs.close();
                stmt.close();
                con.close();

            } catch (SQLException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    // Endpoint leaderboard. Percentage??
    @GetMapping("/leaderboard/books")

    public List<Map<String, Object>> topBooks(){

        List<Map<String, Object>> result = new ArrayList<>();
        Connection con = connectDatabase();
        if(con != null){
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT title, bookvotes FROM votes WHERE bookvotes > 0 ORDER BY bookvotes DESC LIMIT 5");

                while(rs.next()){
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", rs.getString("title"));
                    map.put("votes", rs.getInt("bookvotes"));
                    result.add(map);
                }

                rs.close();
                stmt.close();
                con.close();
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }


}
