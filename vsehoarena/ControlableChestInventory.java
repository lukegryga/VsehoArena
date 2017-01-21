/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Banner;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 *
 * @author lukeg
 */
public class ControlableChestInventory implements Listener, Serializable {
    
    private final Inventory delegate;
    private int innerIndex;
    private final List<ItemStack[]> itemLines;
    
    private ControlableChestInventory(String name){
        this(name, null);
    }
    
    private ControlableChestInventory(String name, List<ItemStack[]> itemLines){
        if(itemLines == null){
            this.itemLines = new LinkedList();
            this.itemLines.add(new ItemStack[9]);
        }else{
            this.itemLines = itemLines;
        }
        this.delegate = Bukkit.createInventory(null, 54, name);
        this.innerIndex = 0;
        addControl(delegate);
        fillTop();
        updateControl();
        VsehoArena.SINGLETON.getServer().getPluginManager().registerEvents(this, VsehoArena.SINGLETON);
    }
    
    public static ControlableChestInventory newInstance(String name){
        return new ControlableChestInventory(name);
    }
    
    public static ControlableChestInventory deserialize(Map<String, Object> serialized){
        String name = (String)serialized.get("name");
        List<ItemStack[]> itemLines = new LinkedList();
        serialized.entrySet().stream()
                .filter((Entry<String, Object> e) -> {return e.getKey().startsWith("itemLine");})
                .forEach(e -> {itemLines.add(deserializeItemLine((char[])e.getValue()));});
        return new ControlableChestInventory(name, itemLines);
    }
    
    public List<ItemStack> getItems(){
        saveTop();
        List<ItemStack> items = new LinkedList();
        itemLines.forEach((itemLine) -> {
            for(int i = 0; i<9; i++){
                if(itemLine[i] != null)
                    items.add(itemLine[i].clone());
            }
        });
        return items;
    }
    
    public List<ItemStack> getCompleteItems(){
        saveTop();
        List<ItemStack> items = new LinkedList();
        itemLines.forEach((itemLine) -> {
            for(int i = 0; i<9; i++){
                ItemStack it = itemLine[i];
                if(it == null)
                    continue;
                for(int j = 0; j < getItemNumber(it); j++){
                    ItemStack givenItem = it.clone();
                    items.add(givenItem);
                }
            }
        });
        return items;
        
    }

    public Inventory getInventory(){
        saveTop();
        innerIndex = 0;
        fillTop();
        return delegate;
    }
    
    public Map<String, Object> serialize(){
        saveTop();
        Map<String, Object> serialized = new HashMap();
        serialized.put("name", delegate.getName());
        for(int i = 0; i < itemLines.size(); i++){
            serialized.put("itemLine" + i, serializeItemLine(itemLines.get(i)));
         }
        return serialized;
    }
    
    private static ItemStack[] deserializeItemLine(char[] itemLine){
        ItemStack[] items = new ItemStack[9];
        try{
            ByteArrayInputStream is = new ByteArrayInputStream(Base64Coder.decode((char[])itemLine));
            BukkitObjectInputStream bois = new BukkitObjectInputStream(is);
            for(int i = 0; i < 8; i++){
                items[i] = (ItemStack)bois.readObject();
            }
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(ControlableChestInventory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return items;
    }
    
    private char[] serializeItemLine(ItemStack[] itemLine){
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (BukkitObjectOutputStream boos = new BukkitObjectOutputStream(os)) {
                for(int i = 0; i < 8; i++){
                    boos.writeObject(itemLine[i]);
                }
            }
            return Base64Coder.encode(os.toByteArray());
        } catch (IOException ex) {
            Logger.getLogger(ControlableChestInventory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private Inventory changePage(int value){
        saveTop();
        innerIndex += value;
        while(innerIndex >= itemLines.size())
            itemLines.add(new ItemStack[9]);
        if(this.innerIndex < 0)
            this.innerIndex = 0;
        fillTop();
        updateControl();
        return delegate;
    }

    
    private void addAmnout(int controlSlot, int value){
        ItemStack item = delegate.getItem(controlSlot%9);
        if(item != null){
            int amnout = item.getAmount() + value;
            item.setAmount(amnout);
            if(amnout > 0){
                delegate.getItem(controlSlot).setAmount(amnout);
            }
        }
    }
    
    private void addNumber(int controlSlot, int value){
        ItemStack item = delegate.getItem(controlSlot%9);
        if(item == null)
            return;
        
        int number = value + getItemNumber(item);
        if(number < 1)
            number = 1;
        setItemNumber(item, number);
        delegate.getItem(controlSlot).setAmount(number);
    }
    
    private void fillTop(){
        ItemStack[] top = itemLines.get(innerIndex);
        for(int i = 0; i<9; i++){
            delegate.setItem(i, top[i]);
        }
    }
    
    private void saveTop(){
        ItemStack[] top = new ItemStack[9];
        for(int i = 0; i<9; i++){
            top[i] = delegate.getItem(i);
        }
        itemLines.set(innerIndex, top);
    }
    
    private void updateControl(){
        for(int i = 0; i < 9; i++){
            ItemStack item = delegate.getItem(i);
            int amount = 1;
            int number = 1;
            if(item != null){
                number = getItemNumber(item);
                amount = item.getAmount();
            }
            delegate.getItem(i+9).setAmount(number);
            delegate.getItem(i+18).setAmount(amount);
            delegate.getItem(49).setAmount(innerIndex + 1);
        }
    }
    
    private void addControl(Inventory inv){
        Banner b = new Banner();
        ItemStack next = new ItemStack(Material.BANNER,1),
                  previous = new ItemStack(Material.BANNER,1),
                  number = new ItemStack(Material.BANNER,1),
                  amnout = new ItemStack(Material.BANNER,1),
                  identifier = new ItemStack(Material.BANNER,1);
        BannerMeta bMeta = (BannerMeta)next.getItemMeta();
        bMeta.setDisplayName("NEXT");
        next.setItemMeta(bMeta);
        bMeta.setDisplayName("PREVIOUS");
        previous.setItemMeta(bMeta);
        bMeta.setDisplayName("+number-");
        bMeta.setBaseColor(DyeColor.GREEN);
        number.setItemMeta(bMeta);
        bMeta.setDisplayName("+amount-");
        bMeta.setBaseColor(DyeColor.RED);
        amnout.setItemMeta(bMeta);
        bMeta.setDisplayName("PAGE");
        bMeta.setBaseColor(DyeColor.YELLOW);
        identifier.setItemMeta(bMeta);
        for(int i=0; i<9; i++)
            inv.setItem(i+9, number.clone());
        for(int i=0; i<9; i++)
            inv.setItem(i+18, amnout.clone());
        inv.setItem(45, previous);
        inv.setItem(53, next);
        inv.setItem(49, identifier);
    }

    private int getItemNumber(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        int number = 1;
        if(meta.hasLore()){
            for(String attr : item.getItemMeta().getLore()){
                if(attr.startsWith("nTimes=")){
                    number = Integer.parseInt(attr.substring(attr.indexOf("=")+1));
                    break;
                }
            }
        }
        return number;
    }
    
    private void setItemNumber(ItemStack item, int number){
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new LinkedList();
        Iterator<String> lIterator = lore.iterator();
        while(lIterator.hasNext()){
            String attr = lIterator.next();
            if(attr.startsWith("nTimes=")){
                lIterator.remove();
            }
        }
        lore.add("nTimes=" + number);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Inventory inv = event.getInventory();
        if(event.getClickedInventory().equals(delegate)){
            ItemStack clicked = event.getCurrentItem();
            if(clicked == null)
                return;
            if(clicked.getType().equals(Material.BANNER) && clicked.getItemMeta().hasDisplayName()){
                Player player = (Player)event.getWhoClicked();
                if(clicked.getItemMeta().getDisplayName().equalsIgnoreCase("next")){
                    player.openInventory(changePage(1));
                }else if(clicked.getItemMeta().getDisplayName().equalsIgnoreCase("previous")){
                    player.openInventory(changePage(-1));
                }else if(clicked.getItemMeta().getDisplayName().equalsIgnoreCase("+number-")){
                    if(event.getClick().equals(ClickType.LEFT))
                        addNumber(event.getSlot(), 1);
                    else if(event.getClick().equals(ClickType.RIGHT))
                        addNumber(event.getSlot(), -1);
                }else if(clicked.getItemMeta().getDisplayName().equalsIgnoreCase("+amount-")){
                    if(event.getClick().equals(ClickType.LEFT))
                        addAmnout(event.getSlot(), 1);
                    else if(event.getClick().equals(ClickType.RIGHT))
                        addAmnout(event.getSlot(), -1);
                }
                event.setCancelled(true);
            }
        }
    }

}
