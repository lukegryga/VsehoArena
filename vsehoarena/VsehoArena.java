/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author lukeg
 */
public class VsehoArena extends JavaPlugin {
    
    public static VsehoArena SINGLETON;
    
    private ArenaBuilder aBuilder = null;
    
    ControlableChestInventory invinv;

    public static void main(String[] args) {
        
    }

    @Override
    public void onEnable() {
        SINGLETON = this;
        if(!this.getDataFolder().exists())
            this.getDataFolder().mkdir();
        
        aBuilder = new ArenaBuilder();
        getServer().getPluginManager().registerEvents(aBuilder, this);
            
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player pSender = (Player)sender;
        
        switch(args.length){
            case 1:
                if(args[0].equals("test1")){
                    invinv = ControlableChestInventory.newInstance("Ahoj");
                    getServer().getPluginManager().registerEvents(invinv, this);
                    pSender.openInventory(invinv.getInventory());
                }
                if(args[0].equals("stest1")){
                    try {
                        FileOutputStream fOutStream = new FileOutputStream("test.test");
                        try (ObjectOutputStream objectOS = new ObjectOutputStream(fOutStream)) {
                            objectOS.writeObject(invinv.serialize());
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(VsehoArena.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(args[0].equals("ltest1")){
                    try {
                        FileInputStream fInStream = new FileInputStream("test.test");
                        ObjectInputStream objectIS = new ObjectInputStream(fInStream);
                        invinv = ControlableChestInventory.deserialize((HashMap)objectIS.readObject());
                        getServer().getPluginManager().registerEvents(invinv, this);
                        pSender.openInventory(invinv.getInventory());
                    } catch (IOException | ClassNotFoundException ex) {
                        Logger.getLogger(VsehoArena.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                Arena arena = null;
                for(Arena a : aBuilder.getArenas()){
                    if(UtilLib.isIn3D(a.getL1(), a.getL2(), pSender.getLocation())){
                        arena = a;
                        break;
                    }
                }
                if(arena == null){
                    pSender.sendMessage("You are not in the arena, please wirte an arena name at the end of command");
                    return false;
                }
                if(args[0].equalsIgnoreCase("addstart")){
                    aBuilder.registerChest(pSender, arena.getName(), true);
                }else if(args[0].equalsIgnoreCase("addwild")){
                    aBuilder.registerChest(pSender, arena.getName(), false);
                }else if(args[0].equalsIgnoreCase("start")){
                    arena.startGame();
                }else if(args[0].equalsIgnoreCase("regen")){
                    arena.regenerateArena();
                }else if(args[0].equalsIgnoreCase("startitems")){
                    arena.showStartInv(pSender);
                }else if(args[0].equalsIgnoreCase("wilditems")){
                    arena.showWildInv(pSender);
                }else if(args[0].equalsIgnoreCase("update")){
                    arena.generateSchematic();
                }else if(args[0].equalsIgnoreCase("clear")){
                    arena.tidyUp();
                }
                break;
            
            case 2:
                if(args[0].equalsIgnoreCase("create")){
                    aBuilder.createArena(args[1], pSender);
                }else if(args[0].equalsIgnoreCase("addstart")){
                    aBuilder.registerChest(pSender, args[1], true);
                }
                else if(args[0].equalsIgnoreCase("addwild")){
                    aBuilder.registerChest(pSender, args[1], false);
                }else if(args[0].equalsIgnoreCase("start")){
                    Arena a = aBuilder.getArena(args[1], pSender);
                    if(a != null)
                        a.startGame();
                }else if(args[0].equalsIgnoreCase("regen")){
                    Arena a = aBuilder.getArena(args[1], pSender);
                    if(a != null)
                        a.regenerateArena();
                }else if(args[0].equalsIgnoreCase("startitems")){
                    Arena a = aBuilder.getArena(args[1], pSender);
                    if(a != null)
                        a.showStartInv(pSender);
                }else if(args[0].equalsIgnoreCase("wilditems")){
                    Arena a = aBuilder.getArena(args[1], pSender);
                    if(a != null)
                        a.showWildInv(pSender);
                }else if(args[0].equalsIgnoreCase("update")){
                    Arena a = aBuilder.getArena(args[1], pSender);
                    if(a != null)
                        a.generateSchematic();
                }
                break;
        }
        
        return true;
    }

    @Override
    public void onDisable() {
        aBuilder.saveAll();
    } 
}
