package com.theironyard;

public class Game {
    String name;
    String genre;
    String platform;
    int releaseYear;

    public Game(){}

    public Game(String name, String genre, String platform, int releaseYear) {
        this.name = name;
        this.genre = genre;
        this.platform = platform;
        this.releaseYear = releaseYear;
    }
}
