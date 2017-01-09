/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.DataException;
import org.bukkit.potion.Potion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *
 * @author lukeg
 */
public class ArenaBuilder implements Listener{
    
    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder;
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private Transformer transformer;
    
    private Location l1, l2;
    
    private Arena arena = null;
    
    public ArenaBuilder(){
        try {
            builder = factory.newDocumentBuilder();
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Arena.class.getName()).log(Level.SEVERE, "XML builder fails", ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(ArenaBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Arena getArena() {
        return arena;
    }
    
    public void loadArena() throws ParserConfigurationException, SAXException{
        File f = new File("./plugins/arena.xml");
        Document doc;
        try {
            doc = builder.parse(f);
        } catch (IOException ex) {
            Bukkit.getServer().getConsoleSender().sendMessage("There is no arena yet.");
            return;
        }
        Location loc1, loc2;
        String arenaName;
        Set<Chest> wildChests, startChests;
        Map<ItemStack, Integer> wildItems, startItems;
        Element root = doc.getDocumentElement();
        arenaName = root.getElementsByTagName("name").item(0).getTextContent();
        loc1 = stringToLocation(root.getElementsByTagName("loc1").item(0).getTextContent());
        loc2 = stringToLocation(root.getElementsByTagName("loc2").item(0).getTextContent());
        Element eWItems = (Element)root.getElementsByTagName("wildItems").item(0);
        Element eSItems = (Element)root.getElementsByTagName("startItems").item(0);
        wildItems = getItems(eWItems);
        startItems = getItems(eSItems);
        Element eWChests = (Element)root.getElementsByTagName("wildChests").item(0);
        Element eSChests = (Element)root.getElementsByTagName("startChests").item(0);
        wildChests = getChests(eWChests);
        startChests = getChests(eSChests);
        arena = new Arena(arenaName, loc1, loc2, wildChests, startChests, wildItems, startItems);
    }
   
    public void saveArena(){
        Document doc = builder.newDocument();
        Element root = doc.createElement("arena");
        doc.appendChild(root);
        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(this.arena.getName()));
        Element loc1 = doc.createElement("loc1");
        loc1.appendChild(doc.createTextNode(locationToString(this.arena.getL1())));
        Element loc2 = doc.createElement("loc2");
        loc2.appendChild(doc.createTextNode(locationToString(this.arena.getL2())));
        root.appendChild(name);
        root.appendChild(loc1);
        root.appendChild(loc2);
        root.appendChild(chestsToXml(doc, "wildChests", this.arena.getWildChests()));
        root.appendChild(chestsToXml(doc, "startChests", this.arena.getStartChests()));
        root.appendChild(itemsToXml(doc, "wildItems", this.arena.getWildItems()));
        root.appendChild(itemsToXml(doc, "startItems", this.arena.getStartItems()));
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("./plugins/arena.xml"));
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            Bukkit.getServer().getConsoleSender().sendMessage("[Arena]: Save xml arena failed");
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
    
    public void startGame(Player p){
        if(arena != null){
            p.sendMessage(arena.startGame());
        }else{
            p.sendMessage("[Arena]: There is no Arena");
        }
    }
    
    /**
     * Create new arena and store it.
     * @param name name of Arena
     * @param p player who report will be send
     */
    public void createArena(String name, Player p){
        if(l1 != null & l2 != null){
            arena = new Arena(name, l1, l2);
            try{
                generateSchematic(name);
            }catch (IOException | DataException e){
                Logger.getLogger(ArenaBuilder.class.getName()).log(Level.SEVERE, null, e);
                p.sendMessage("[Arena]: Error while saving shematic file. Save it manually.");
            }
            p.sendMessage("[Arena]: Arena " + name + " has been created");
        }else{
            p.sendMessage("[Arena]: Both Loc1 and Loc 2 must be set");
        }
    }
    
    /**
     * Generates schematic from cuboid defined by l1 and l2 and saves to plugin's data folder
     * @param name Name of generated schematic
     * @throws IOException
     * @throws DataException 
     */
    @SuppressWarnings("deprecated")
    public void generateSchematic(String name) throws IOException, DataException{
        Location min = UtilLib.getMin(l1, l2);
        Location max = UtilLib.getMax(l1, l2);
        Vector start = new Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ());
        Vector offset = new Vector(max.getBlockX() - min.getBlockX(), 
                max.getBlockY() - min.getBlockY(), max.getBlockZ() - min.getBlockZ());
        CuboidClipboard cb = new CuboidClipboard(offset, start);
        EditSession es = new EditSession(new BukkitWorld(l1.getWorld()), 999999999);
        File file = new File(VsehoArena.SINGLETON.getDataFolder(), name + ".schematic");
        cb.copy(es);
        cb.saveSchematic(file);
    }
    
    /**
     * Register chest in arena. If arena already has registered this chest, cancel old registration and create new one.
     * @param p - player who report will be send
     * @param startChest if true, then register the start chest, the wild chest otherwise
     */
    public void registerChest(Player p, boolean startChest){
        if(arena == null){
            p.sendMessage("There is no arena yet");
            return;
        }
        if(l1.getBlock().getType() != Material.CHEST){
            p.sendMessage("[Arena]: There is no chest on loc1");
        }else{
            String status;
            Chest chest = (Chest)l1.getBlock().getState();
            if(startChest){
                status = arena.addStartChests(chest);
            }else{
                status = arena.addWildChests(chest);
            }
            p.sendMessage("[arena]: " + status);
        }
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
    
    private String locationToString(Location loc){
        return new StringBuilder()
                .append(loc.getWorld().getName()).append(";")
                .append(loc.getBlockX()).append(";")
                .append(loc.getBlockY()).append(";")
                .append(loc.getBlockZ()).toString();
    }
    
    private Location stringToLocation(String sLocation){
        String[] sLoc = sLocation.split(";");
        int[] iXyz = new int[3];
        for(int i = 0; i<3; i++){
            iXyz[i] = Integer.parseInt(sLoc[i+1]);
        }
        return new Location(Bukkit.getWorld(sLoc[0]), iXyz[0], iXyz[1], iXyz[2]);
    }
    
    /**
     * Parse chest from XML.
     * Get Set of chest from locations specified in XML
     * @param chestRoot root of chest nodes
     * @return 
     */
    private Set<Chest> getChests(Element chestRoot){
        Set<Chest> chests = new HashSet();
        NodeList nChest = chestRoot.getElementsByTagName("chest");
        for(int i = 0; i<nChest.getLength(); i++){
            Location l = stringToLocation(nChest.item(i).getTextContent());
            if(l.getBlock().getType() == Material.CHEST){
                Chest ch = (Chest)l.getBlock().getState();
                chests.add(ch);
            }else{
                Bukkit.getServer().getConsoleSender().sendMessage("[Arena]: Chest on " + locToSimpleString(l) + " has been removed!");
            }
        }
        return chests;
    }
    
    private Map<ItemStack, Integer> getItems(Element itemRoot){
        Map<ItemStack, Integer> items = new HashMap();
        NodeList nItems = itemRoot.getElementsByTagName("itemStack");
        for(int i=0; i< nItems.getLength(); i++){
            Element eItem = (Element)nItems.item(i);
            Material itemMaterial = Material.matchMaterial(eItem.getAttribute("material"));
            int itemAmount = Integer.parseInt(eItem.getAttribute("amount"));
            int itemNumber = Integer.parseInt(eItem.getAttribute("number"));
            ItemStack item = new ItemStack(itemMaterial, itemAmount);
            items.put(item, itemNumber);
        }
        return items;
    }
    
    private Element chestsToXml(Document doc, String name, Set<Chest> chests){
        Element chRoot = doc.createElement(name);
        for(Chest chest : chests){
            Element eChest = doc.createElement("chest");
            String loc = locationToString(chest.getLocation());
            eChest.appendChild(doc.createTextNode(loc));
            chRoot.appendChild(eChest);
        }
        return chRoot;
    }
    
    private Element itemsToXml(Document doc, String name, Map<ItemStack, Integer> items){
        Element chRoot = doc.createElement(name);
        for(Entry<ItemStack, Integer> entry : items.entrySet()){
            ItemStack item = entry.getKey();
            Element eItemStack = doc.createElement("itemStack");
            eItemStack.setAttribute("material", item.getType().toString());
            eItemStack.setAttribute("amount", String.valueOf(item.getAmount()));
            eItemStack.setAttribute("number", String.valueOf(entry.getValue()));
            eItemStack.setAttribute("serialize", String.valueOf(item.serialize()));
            chRoot.appendChild(eItemStack);
        }
        return chRoot;
    }

    
}
