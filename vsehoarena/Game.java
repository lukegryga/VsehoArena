/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.world.DataException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
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

    private Arena arena;
    private Set<Player> players = new HashSet();
    private Set<Player> alivePlayers = new HashSet();
    private Scoreboard sBoard;
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
        try {
            if(initPlayers() <= 1){
                return "[Arena]: There are not enought players in arena";
            }
            if(gameInProgress){
                return "[Arena] Game is just running";
            }
            alivePlayers = players;
            if(!playersOnStart()){
                return "[Arena]: There are more player then number of Start chests";
            }
            arena.regenerateArena();
            arena.tidyUp();
            initScoreboard();
            arena.divideWildItems();
            Bukkit.getServer().getPluginManager().registerEvents(this, VsehoArena.SINGLETON);
            players.forEach(p -> {
                p.getInventory().clear();
                p.sendMessage("[Arena]: Game started");});
            gameInProgress = true;
            return "[Arena]: You succesfuly started the GAME";
        } catch (DataException | IOException | MaxChangedBlocksException ex) {
            return "[Arena]: Fails occured. Arena wasn't regenerated";
        }
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
        if(arena.getStartChests().size() < players.size()){
            return false;
        }
        List<Chest> lChests = new LinkedList();
        lChests.addAll(arena.getStartChests());
        int counter = 0;
        for(Player p : players){
            p.teleport(lChests.get(counter).getLocation().add(new Vector(0,2,0)));
            arena.fillStartChest(lChests.get(counter));
            p.setHealth(20);
            counter++;
        }
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
