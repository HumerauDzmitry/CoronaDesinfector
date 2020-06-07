package com.humerau;

public class ConsoleAnnouncer implements Announcer {

    @InjectByType
    private Recommendator recomendator;

    @Override
    public void announce(String message) {
        System.out.println(message);
        recomendator.recommend();
    }
}
