/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.world.DataException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.xml.sax.SAXException;

/**
 *
 * @author lukeg
 */
public class VsehoArena extends JavaPlugin {
    
    public static VsehoArena SINGLETON;
    
    private ArenaBuilder aBuilder = null;

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
                if(args[0].equals("create")){
                    aBuilder.createArena(args[1], pSender);
                }else if(args[0].equals("addstart")){
                    aBuilder.registerChest(pSender, args[1], true);
                }
                else if(args[0].equals("addwild")){
                    aBuilder.registerChest(pSender, args[1], false);
                }else if(args[0].equals("start")){
                    Arena a = aBuilder.getArena(args[1], pSender);
                    if(a != null)
                        a.startGame();
                }else if(args[0].equals("regen")){
                    Arena a = aBuilder.getArena(args[1], pSender);
                    if(a != null)
                        a.regenerateArena();
                }else if(args[0].equals("startitems")){
                    Arena a = aBuilder.getArena(args[1], pSender);
                    if(a != null)
                        a.showStartInv(pSender);
                }else if(args[0].equals("addwildinv")){
                    Arena a = aBuilder.getArena(args[1], pSender);
                    if(a != null)
                        a.createNewWildInv();
                }else if(args[0].equals("wilditems")){
                    Arena a = aBuilder.getArena(args[1], pSender);
                    if(a != null)
                        a.showWildInv(1, pSender);
                }
                break;
                
            case 3:
                if(args[0].equals("wilditems")){
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
