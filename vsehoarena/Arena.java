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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 *
 * @author lukeg
 */
public class Arena {
    
    
    private final Random random = new Random();
    
    private Game arenaGame;
    private String name;
    private Set<Chest> wildChests, startChests;
    private List<Inventory> wildItems;
    private Inventory startItems;
    
    private Location l1;
    private Location l2;

    public Arena(String name, Location l1, Location l2){
        this(name, l1, l2, new HashSet(), new HashSet(), new LinkedList(), null);
    }
    
    public Arena(String name, Location l1, Location l2, Set<Chest> wildChests, Set<Chest> startChests,
            List<Inventory> wildItems, Inventory startItems){
        this.name = name;
        this.l1 = l1;
        this.l2 = l2;
        this.wildChests = wildChests;
        this.startChests = startChests;
        this.wildItems = wildItems;
        this.startItems = startItems;
        if(startItems == null){
            this.startItems = Bukkit.createInventory(null, 54);
        }
        this.arenaGame = new Game(this);
    }

    public String getName() {
        return name;
    }

    public Set<Chest> getStartChests() {
        return startChests;
    }

    public Location getL1() {
        return l1;
    }

    public Location getL2() {
        return l2;
    }
    
    
    
    public static Arena loadArena(String name){
        File arenaFile = new File(VsehoArena.SINGLETON.getDataFolder(), name + ".arena");
        FileInputStream fInStream;
        ObjectInputStream objectIS;
        HashMap<String, Object> deserialized = new HashMap();
        try {
            fInStream = new FileInputStream(arenaFile);
            objectIS = new ObjectInputStream(fInStream);
            deserialized = (HashMap)objectIS.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(Arena.class.getName()).log(Level.SEVERE, "Arena " + name + " was not found", ex);
            return null;
        }
        Location aLoc1 = Location.deserialize((Map<String, Object>)deserialized.get("loc1"));
        Location aLoc2 = Location.deserialize((Map<String, Object>)deserialized.get("loc2"));
        Set<Chest> aStartChst = deserializeChests((Map<String, Object>)deserialized.get("startChests"));
        Set<Chest> aWildChst = deserializeChests((Map<String, Object>)deserialized.get("wildChests"));
        Inventory aStartInv = deserializeInventory((Map<String, Object>)deserialized.get("startItems"));
        List<Inventory> aWildInvs = deserializeInventories((Map<String, Object>)deserialized.get("wildItems"));
        Arena a = new Arena(name, aLoc1, aLoc2, aWildChst, aStartChst, aWildInvs, aStartInv);
        return a;
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
    
    public void showStartInv(Player p){
        p.openInventory(startItems);
    }
    
    public void showWildInv(int number, Player p){
        number--;
        if(number >= 50){
            p.sendMessage("[Arena]: Limit of wild inventories (50) reached.");
            return;
        }
        while(number >= this.wildItems.size() ){
            p.sendMessage("[Arena]: " + createNewWildInv());
        }
        p.openInventory(wildItems.get(number));
    }
    
    public String createNewWildInv(){
        Inventory newInv = Bukkit.createInventory(null, 54);
        wildItems.add(newInv);
        return "Inventory " + wildItems.size() + " created.";
    }
    
    /**
     * Register chest in arena. If arena already has registered this chest, cancel old registration and create new one.
     * @param p - player who report will be send
     * @param startChest if true, then register the start chest, the wild chest otherwise
     * @param l1
     * @param l2
     * @return 
     */
    public String registerChest(boolean startChest, Location l1, Location l2){
        if(l1.getBlock().getType() != Material.CHEST){
            return "There is no chest on loc1";
        }else{
            Chest chest = (Chest)l1.getBlock().getState();
            if(startChest){
                return addStartChests(chest);
            }else{
                return addWildChests(chest);
            }
        }
    }

    
    public void regenerateArena(){
        try {
            EditSession es = new EditSession(new BukkitWorld(l1.getWorld()), 999999999);
            File schematicFile = new File(VsehoArena.SINGLETON.getDataFolder(), name + ".schematic");
            CuboidClipboard clipboard = CuboidClipboard.loadSchematic(schematicFile);
            Location min = UtilLib.getMin(l1, l2);
            Vector origin = new Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ());
            clipboard.paste(es, origin, false);
        } catch (IOException | DataException ex) {
            Logger.getLogger(Arena.class.getName()).log(Level.SEVERE, "Arena regenerating failed", ex);
        } catch (MaxChangedBlocksException ex) {
            Logger.getLogger(Arena.class.getName()).log(Level.SEVERE, "Arena regenerating failed", ex);
        }
    }
    
    public String startGame(){
        return arenaGame.startGame();
    }
    
    public void tidyUp(){
        wildChests.forEach((ch) -> {ch.getInventory().clear();});
        startChests.forEach(ch -> {ch.getInventory().clear();});
    }
    
    public void fillStartChest(Chest ch){
        for(ItemStack item : startItems.getContents()){
            if(item != null)
                ch.getInventory().addItem(item);
        }
    }
    
    public void divideWildItems(){
        List<Chest> lWildChests = new LinkedList();
        lWildChests.addAll(wildChests);
        int maxI = lWildChests.size();
        for(Inventory inv : wildItems){
            for(int i = 0; i<inv.getSize(); i++){
                ItemStack item = inv.getItem(i);
                int rand = random.nextInt(maxI);
                if(item != null)
                    lWildChests.get(rand).getInventory().addItem(item);
            }
        }
    }
    
    
    public void saveArena(){
        File arenaFile = new File(VsehoArena.SINGLETON.getDataFolder(), this.name + ".arena");
        FileOutputStream fOutStream;
        ObjectOutputStream objectOS;
        Map<String,Object> serialized = new HashMap();
        serialized.put("name", this.name);
        serialized.put("loc1", this.l1.serialize());
        serialized.put("loc2", this.l2.serialize());
        serialized.put("startChests", serializeChestLocations(startChests));
        serialized.put("wildChests", serializeChestLocations(wildChests));
        serialized.put("startItems", serializeInventory(startItems));
        serialized.put("wildItems", serializeInventories(wildItems));
        try {
            fOutStream = new FileOutputStream(arenaFile);
            objectOS = new ObjectOutputStream(fOutStream);
            objectOS.writeObject(serialized);
            objectOS.close();
        } catch (IOException ex) {
            Logger.getLogger(Arena.class.getName()).log(Level.SEVERE, "Saving arena failed", ex);
            return;
        }
    }
                
    private static Map<String, Object> serializeChestLocations(Set<Chest> blocks){
        List<Location> locs = blocks.stream()
                .map((Chest ch)->{return ch.getLocation();})
                .collect(Collectors.toList());
        Map<String, Object> serialized = new HashMap();
        
        for(int i = 0; i < locs.size(); i++){
            serialized.put(String.valueOf(i), locs.get(i).serialize());
        }
        
        return serialized;
    }
    
    private static Set<Chest> deserializeChests(Map<String, Object> locations){
        Set<Chest> deserialized = new HashSet();
        for(Object obj : locations.values()){
            Location l = Location.deserialize((Map<String, Object>)obj);
            if(l.getBlock().getType().equals(Material.CHEST)){
                deserialized.add((Chest)l.getBlock().getState());
            }
        }
        return deserialized;
    }
    
    private static Map<String, Object> serializeInventories(List<Inventory> inventories){
        Map<String, Object> serialized = new HashMap();
        
        for(int i = 0; i<inventories.size(); i++){
            serialized.put(String.valueOf(i), serializeInventory(inventories.get(i)));
        }
        
        return serialized;
    }
    
    private static List<Inventory> deserializeInventories(Map<String, Object> inventories){
        List<Inventory> deserialized = new LinkedList();
        inventories.values().forEach((obj) -> {
            deserialized.add(deserializeInventory((Map<String, Object>)obj));
        });
        return deserialized;
    }
    
    private static Map<String, Object> serializeInventory(Inventory inv){
        Map<String, Object> serialized = new HashMap();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(os);
            boos.writeInt(inv.getSize());
            for(int i = 0; i < inv.getSize(); i++){
                boos.writeObject(inv.getItem(i));
            }
            boos.close();
            serialized.put("inventory", Base64Coder.encode(os.toByteArray()));
            boos.close();
        } catch (IOException ex) {
            Logger.getLogger(Arena.class.getName()).log(Level.SEVERE, "Deserializing inventory failed", ex);
        }
        return serialized;
    }
    
    private static Inventory deserializeInventory(Map<String, Object> inv){
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(Base64Coder.decode((char[])inv.get("inventory")));
            BukkitObjectInputStream bois = new BukkitObjectInputStream(is);
            Inventory deserialized = Bukkit.createInventory(null, bois.readInt());
            for(int i = 0; i < deserialized.getSize(); i++){
                deserialized.setItem(i, (ItemStack)bois.readObject());
            }
            return deserialized;
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(Arena.class.getName()).log(Level.SEVERE, "Deserializing inventory failed", ex);
        }
        return Bukkit.createInventory(null, 54);
    }
    
}
