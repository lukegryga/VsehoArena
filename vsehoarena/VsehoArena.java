/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.world.DataException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.xml.sax.SAXException;

/**
 *
 * @author lukeg
 */
public class VsehoArena extends JavaPlugin {
    
    static ArenaBuilder aBuilder = new ArenaBuilder();
    
    public static VsehoArena SINGLETON;

    public static void main(String[] args) {
//        try {
//            aBuilder.loadArena();
//        } catch (ParserConfigurationException ex) {
//            Logger.getLogger(VsehoArena.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SAXException ex) {
//            Logger.getLogger(VsehoArena.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    public void onEnable() {
        SINGLETON = this;
        getServer().getPluginManager().registerEvents(aBuilder, this);
        if(!this.getDataFolder().exists())
            this.getDataFolder().mkdir();
        
        try {
            aBuilder.loadArena();
        } catch (SAXException | ParserConfigurationException ex) {
            Logger.getLogger(VsehoArena.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player pSender = (Player)sender;
        
        switch(args.length){
            case 1:
                if(args[0].equals("addWild")){
                    aBuilder.registerChest(pSender, false);
                }else if(args[0].equals("addStart")){
                    aBuilder.registerChest(pSender, true);
                }else if(args[0].equals("start")){
                    aBuilder.startGame(pSender);
                }else if(args[0].equals("reload")){
                    try {
                        aBuilder.loadArena();
                    } catch (SAXException | ParserConfigurationException ex) {
                        Logger.getLogger(VsehoArena.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else if(args[0].equals("regen")){
                    try {
                        aBuilder.getArena().regenerateArena();
                    } catch (IOException | DataException | MaxChangedBlocksException ex) {
                        Logger.getLogger(VsehoArena.class.getName()).log(Level.SEVERE, null, ex);
                        pSender.sendMessage("[Arena] Regen failed");
                    }
                }
                break;
            
            case 2:
                if(args[0].equals("create")){
                    aBuilder.createArena(args[1], pSender);
                }
                break;
        }
        
        return true;
    }

    @Override
    public void onDisable() {
        aBuilder.saveArena();
    } 
}
