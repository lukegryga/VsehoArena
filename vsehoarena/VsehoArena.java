/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.world.DataException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.xml.sax.SAXException;

/**
 *
 * @author lukeg
 */
public class VsehoArena extends JavaPlugin {
    
    public static VsehoArena SINGLETON;
    
    private ArenaBuilder aBuilder = null;

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
                        a.showWildInv(1, pSender);
                }
                break;
                
            case 3:
                if(args[0].equalsIgnoreCase("wilditems")){
                    try{
                    Integer number = Integer.valueOf(args[1]);
                    Arena a = aBuilder.getArena(args[2], pSender);
                    if(a != null)
                        a.showWildInv(number, pSender);
                    }catch(NumberFormatException ex){
                        pSender.sendMessage("[Arena]: Wrong format of number");
                    }
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
