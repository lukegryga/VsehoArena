/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vsehoarena;

import org.bukkit.Location;

/**
 *
 * @author lukeg
 */
public class UtilLib {
    
    private UtilLib(){}
    
    public static boolean isIn3D(Location loc1, Location loc2, Location target){
        
        if(loc1.getWorld() != target.getWorld())
            return false;
        
        Location min = getMin(loc1, loc2);
        Location max = getMax(loc1, loc2);
        
        return (target.getBlockX() >= min.getBlockX() && target.getBlockX() <= max.getBlockX() &&
                target.getBlockY() >= min.getBlockY() && target.getBlockY() <= max.getBlockY() &&
                target.getBlockZ() >= min.getBlockZ() && target.getBlockZ() <= max.getBlockZ());
    }
    
    public static Location getMin(Location l1, Location l2){
        int x, y, z;
        x = l1.getBlockX() < l2.getBlockX() ? l1.getBlockX() : l2.getBlockX();
        y = l1.getBlockY() < l2.getBlockY() ? l1.getBlockY() : l2.getBlockY();
        z = l1.getBlockZ() < l2.getBlockZ() ? l1.getBlockZ() : l2.getBlockZ();
        return new Location(l1.getWorld(), x, y, z);
    }
    
    public static Location getMax(Location l1, Location l2){
        int x, y, z;
        x = l1.getBlockX() > l2.getBlockX() ? l1.getBlockX() : l2.getBlockX();
        y = l1.getBlockY() > l2.getBlockY() ? l1.getBlockY() : l2.getBlockY();
        z = l1.getBlockZ() > l2.getBlockZ() ? l1.getBlockZ() : l2.getBlockZ();
        return new Location(l1.getWorld(), x, y, z);
    }
    
}
