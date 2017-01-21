/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import java.io.File;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author lukeg
 */
public class ArenaBuilder implements Listener{

    
    private Location l1, l2;
    private final List<Arena> arenas = new LinkedList();
    
    public ArenaBuilder(){
        File folder = VsehoArena.SINGLETON.getDataFolder();
        for(File f : folder.listFiles()){
            if(f.getName().endsWith(".arena")){
                String name = f.getName().substring(0,f.getName().indexOf('.'));
                arenas.add(Arena.loadArena(name));
            }
        }
    }
    
    
    public boolean setL1(Location l) {
        if(l.equals(this.l1))
            return false;
        this.l1 = l;
        return true;
    }
    
    /**
     * 
     * @param l
     * @param p player who report will be send
     */
    public void setL1(Location l, Player p){
        setL1(l);
        p.sendMessage("[Arena]: Loc1 set to " + locToSimpleString(l));
    }

    public boolean setL2(Location l) {
        if(l.equals(this.l2))
            return false;
        this.l2 = l;
        return true;
    }
    
    /**
     * 
     * @param l
     * @param p Player who report will be send
     */
    public void setL2(Location l, Player p) {
        if(setL2(l))
            p.sendMessage("[Arena]: Loc2 set to " + locToSimpleString(l));
    }
    
    public Arena getArena(String name){
        Iterator<Arena> aIter = arenas.iterator();
        while(aIter.hasNext()){
            Arena a = aIter.next();
            if(a.getName().equals(name)){
                return a;
            }
        }
        return null;
    }
    
    public Arena getArena(String name, Player p){
        Arena a = getArena(name);
        if(a == null){
            p.sendMessage("[Arena]: This arena doesn't exist");
        }
        return a;
    }

    public List<Arena> getArenas() {
        return arenas;
    }
    
    /**
     * Create new arena and store it.
     * @param name name of Arena
     * @param p player who report will be send
     */
    public void createArena(String name, Player p){
        if(l1 != null & l2 != null){
            Arena arena = new Arena(name, l1, l2);
            arenas.add(arena);
            p.sendMessage("[Arena]: Arena " + name + " has been created");
        }else{
            p.sendMessage("[Arena]: Both Loc1 and Loc 2 must be set");
        }
    }
    
    public void saveAll(){
        arenas.forEach((a) -> {
            a.saveArena();
        });
    }
    
    public void registerChest(Player p, String arenaName, boolean start){
        Arena a = getArena(arenaName);
        if(a == null){
            p.sendMessage("[Arena]: This arena dosn't exist");
            return;
        }
        String status = a.registerChest(start, l1, l2)        ;
        p.sendMessage("[Arena]: " + status);
    }
    
    
    /**
     * When player interact with wood_axe, store location of interacted block to loc1 or loc2 depends on Left of Right click
     * @param event 
     */
    @EventHandler
    public void onAxeHit(PlayerInteractEvent event){
        Material inHand = event.getPlayer().getInventory().getItemInMainHand().getType();
        Player p = event.getPlayer();
        
        if(inHand == Material.STICK){
            event.setCancelled(true);
            Location l = event.getClickedBlock().getLocation();
            if(event.getAction() == Action.LEFT_CLICK_BLOCK){
                setL1(l, p);
            }else if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
                setL2(l, p);
            }
        }
    }
    
    /**
     * Get simlple reprezentation of location.
     * @param l
     * @return Location in format (x, y, z)
     */
    private String locToSimpleString(Location l){
        return String.format("(%d,%d,%d)", l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }


    
}
