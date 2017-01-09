/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.DataException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author lukeg
 */
public class Arena {
    
    
    private Random random = new Random();
    
    private Game arenaGame;
    private String name;
    private Set<Chest> wildChests, startChests;
    private Map<ItemStack, Integer> wildItems, startItems;
    private String areaData;
    
    private Location l1;
    private Location l2;

    public Arena(String name, Location l1, Location l2){
        this(name, l1, l2, new HashSet(), new HashSet(), new HashMap(), new HashMap());
    }
    
    public Arena(String name, Location l1, Location l2, Set<Chest> wildChests, Set<Chest> startChests,
            Map<ItemStack, Integer> wildItems, Map<ItemStack, Integer> startItems){
        this.name = name;
        this.l1 = l1;
        this.l2 = l2;
        this.wildChests = wildChests;
        this.startChests = startChests;
        this.wildItems = wildItems;
        this.startItems = startItems;
        this.arenaGame = new Game(this);
    }

    public String getName() {
        return name;
    }

    public Set<Chest> getWildChests() {
        return wildChests;
    }

    public Set<Chest> getStartChests() {
        return startChests;
    }

    public Map<ItemStack, Integer> getWildItems() {
        return wildItems;
    }

    public Map<ItemStack, Integer> getStartItems() {
        return startItems;
    }

    public Location getL1() {
        return l1;
    }

    public Location getL2() {
        return l2;
    }

    public String getAreaData() {
        return areaData;
    }
    
    public void regenerateArena() throws MaxChangedBlocksException, DataException, IOException{
        EditSession es = new EditSession(new BukkitWorld(l1.getWorld()), 999999999);
        File schematicFile = new File(VsehoArena.SINGLETON.getDataFolder(), name + ".schematic");
        CuboidClipboard clipboard = CuboidClipboard.loadSchematic(schematicFile);
        Location min = UtilLib.getMin(l1, l2);
        Vector origin = new Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ());
        clipboard.paste(es, origin, false);
    }
    
    public String startGame(){
        return arenaGame.startGame();
    }

    public String addWildChests(Chest chest){
        boolean notInWild = wildChests.add(chest);
        boolean inStart = startChests.remove(chest);
        if(!notInWild){
            return "Chest is already wildChest";
        }else{
            if(inStart)
                return "Chest type has been changed from start to wild";
            else
                return "New wild chest has been added to arena";
        }
    }
    
    public String addStartChests(Chest chest){
        boolean notInStart = startChests.add(chest);
        boolean inWild = wildChests.remove(chest);
        if(!notInStart){
            return "Chest is already startChest";
        }else{
            if(inWild)
                return "Chest type has been changed from wild to start";
            else
                return "New start chest has been added to arena";
        }
    }
    
    public void tidyUp(){
        wildChests.forEach((ch) -> {ch.getInventory().clear();});
        startChests.forEach(ch -> {ch.getInventory().clear();});
    }
    
    public void fillStartChest(Chest ch){
        for(Map.Entry<ItemStack, Integer> e : startItems.entrySet()){
            for(int i = 0; i<e.getValue(); i++){
                ch.getInventory().addItem(e.getKey().clone());
            }
        }
    }
    
    public void divideWildItems(){
        List<Chest> lWildChests = new LinkedList();
        lWildChests.addAll(wildChests);
        int maxI = lWildChests.size();
        for(Map.Entry<ItemStack, Integer> e: wildItems.entrySet()){
            for(int i = 0; i<e.getValue(); i++){
                int r = random.nextInt(maxI);
                lWildChests.get(r).getInventory().addItem(e.getKey().clone());
            }
        }
    }





    
//    private String getAreaBlockData(){
//        StringBuilder result = new StringBuilder();
//        Location minLoc = VAS.getMin(getL1(), getL2());
//        Location maxLoc = VAS.getMax(getL1(), getL2());
//        Bukkit.getServer().getConsoleSender().sendMessage(minLoc.toString() + maxLoc.toString());
//        for(int x = minLoc.getBlockX(); x < maxLoc.getBlockX(); x++){
//            for(int y = minLoc.getBlockY(); y < maxLoc.getBlockY(); y++){
//                for(int z = minLoc.getBlockZ(); z < maxLoc.getBlockZ(); z++){
//                    result.append(new Location(minLoc.getWorld(), x, y, z).getBlock().getType().toString()).append(";");
//                }
//            }
//        }
//        return result.toString();
//    }
//    
//    public void repairArena(){
//        String[] blocks = areaData.split(";");
//        Location minLoc = VAS.getMin(getL1(), getL2());
//        Location maxLoc = VAS.getMax(getL1(), getL2());
//        int i = 0;
//        for(int x = minLoc.getBlockX(); x < maxLoc.getBlockX(); x++){
//            for(int y = minLoc.getBlockY(); y < maxLoc.getBlockY(); y++){
//                for(int z = minLoc.getBlockZ(); z < maxLoc.getBlockZ(); z++){
//                    new Location(minLoc.getWorld(), x, y, z).getBlock().setType(Material.matchMaterial(blocks[i]));
//                    i++;
//                }
//            }
//        }
//    }
    
        
    
}
