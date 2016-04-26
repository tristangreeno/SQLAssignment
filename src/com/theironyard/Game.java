package com.theironyard;

public class Game {
    Integer id;
    String name;
    String genre;
    String platform;
    Integer releaseYear;

    public Game(){}

    public Game(String name, String genre, String platform, Integer releaseYear) {
        this.name = name;
        this.genre = genre;
        this.platform = platform;
        this.releaseYear = releaseYear;
    }

    public Game(Integer id, String name, String genre, String platform, int releaseYear) {
        this.id = id;

        this.name = name;
        this.genre = genre;
        this.platform = platform;
        this.releaseYear = releaseYear;
    }
}
