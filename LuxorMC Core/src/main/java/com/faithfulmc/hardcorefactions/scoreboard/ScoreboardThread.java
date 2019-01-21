package com.faithfulmc.hardcorefactions.scoreboard;

import com.faithfulmc.hardcorefactions.HCF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScoreboardThread implements Runnable{
    private int THREAD_ID = 0;

    private final HCF plugin;
    private final List<PlayerBoard> playerBoards = Collections.synchronizedList(new ArrayList<>());
    private boolean running = true;
    private final Thread thread;

    public ScoreboardThread(HCF plugin) {
        this.plugin = plugin;
        thread = new Thread(this);
        thread.setName("HCF Scoreboard Thread #" + (THREAD_ID++));
        thread.setPriority(4);
        thread.start();
    }

    private final int TICK_IN_MS = 100;
    private final long TICK_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(TICK_IN_MS);
    private final long NANO = TimeUnit.MILLISECONDS.toNanos(1);

    public void removeBoard(PlayerBoard playerBoard){
        if(playerBoards.remove(playerBoard)){
            playerBoard.setScoreboardThread(null);
        }
    }

    public void addBoard(PlayerBoard playerBoard){
        playerBoards.add(playerBoard);
    }

    public void tick(){
        long now = System.currentTimeMillis();
        synchronized (this.playerBoards) {
            for (PlayerBoard board : this.playerBoards) {
                if (board.getPlayer().isOnline() && !board.isRemoved() && board.getScoreboardThread() == this) {
                    try {
                        board.updateObjective(now);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }
    }

    public void run() {
        long start = System.nanoTime(), finish, diff;
        while (plugin.isEnabled() && running){
            tick();
            finish = System.nanoTime();
            diff = finish - start;
            if(diff < TICK_IN_NANOS){
                long sleep = (TICK_IN_NANOS - diff) / NANO;
                if(sleep > 0){
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                        break;
                    }
                }
            }
            start = System.nanoTime();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
