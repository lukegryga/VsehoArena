/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

/**
 *
 * @author lukeg
 */
public class Game implements Listener{

    private final Arena arena;
    private final Set<Player> players = new HashSet();
    private Set<Player> alivePlayers = new HashSet();
    private final Scoreboard sBoard;
    private Team team;
    
    private boolean gameInProgress = false;
    
    public Game(Arena arena){
        this.arena = arena;
        sBoard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }
    
    public String startGame(){
        if(initPlayers() <= 1){
            return "[Arena]: There are not enought players in arena";
        }
        if(gameInProgress){
            return "[Arena] Game is just running";
        }
        arena.regenerateArena();
        arena.tidyUp();
        alivePlayers = players;
        if(!playersOnStart()){
            return "[Arena]: There are more player then number of Start chests";
        }
        initScoreboard();
        arena.divideWildItems();
        Bukkit.getServer().getPluginManager().registerEvents(this, VsehoArena.SINGLETON);
        players.forEach(p -> {
            p.getInventory().clear();
            p.sendMessage("[Arena]: Game started");
            p.setGameMode(GameMode.SURVIVAL);});
        gameInProgress = true;
        return "[Arena]: You succesfuly started the GAME";
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(alivePlayers.remove(e.getEntity())){
            players.forEach(p -> p.sendMessage("[Arena]: " + e.getEntity().getName() + " died in Arena"));
            team.removePlayer(e.getEntity());
            e.getEntity().getInventory().clear();
            if(alivePlayers.size() <=1){
                gameFinished();
            }
        }
    }
    
    private int initPlayers(){
        //Find all player in arena and add it to players
        players.addAll(Bukkit.getServer().getOnlinePlayers().stream().filter((p) -> {
                return UtilLib.isIn3D(arena.getL1(), arena.getL2(), p.getLocation());})
                    .collect(Collectors.toList()));
        return players.size();
    }
    
    private boolean playersOnStart(){
        Set<Integer> rolled = new HashSet();
        Random random = new Random();
        if(arena.getStartChests().size() < players.size()){
            return false;
        }
        List<Chest> lChests = new LinkedList();
        lChests.addAll(arena.getStartChests());
        players.forEach((p) -> {
            Integer roll;
            do{
                roll = random.nextInt(arena.getStartChests().size());
            }while(rolled.contains(roll));
            p.teleport(lChests.get(roll).getLocation().add(new Vector(0,2,0)));
            arena.fillStartChest(lChests.get(roll));
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(5);
            rolled.add(roll);
        });
        return true;
    }
    
    private void gameFinished(){
        Player winPlayer = alivePlayers.iterator().next();
        PlayerDeathEvent.getHandlerList().unregister(this);
        players.forEach(p -> {p.sendMessage("[Arena]: " + winPlayer.getName() + " won Arena game");});
        sBoard.getObjective("Arena Info").unregister();
        sBoard.getTeam("Fighters").unregister();
        players.clear();
        alivePlayers.clear();
        gameInProgress = false;
    }
    
    private void initScoreboard(){
        team = sBoard.registerNewTeam("Fighters");
        players.forEach(p -> {team.addEntry(p.getName()); p.setScoreboard(sBoard);});
        Objective objective = sBoard.registerNewObjective("Arena Info", "health");
        objective.setDisplayName("Arena Game");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    
    
    
    
}
