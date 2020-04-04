package redsgreens.SupplySign;

import java.util.ArrayList;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

/**
 * Handle events for all Player related events
 * @author redsgreens
 */
public class SupplySignPlayerListener implements Listener {

    private final SupplySign Plugin;
	
    public SupplySignPlayerListener(SupplySign plugin) 
    {
    	Plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    // catch player right-click events
    {

    	Block block;
    	Player player = event.getPlayer();
    	
        // return if the event is not a right-click-block action
        Action action = event.getAction();
        switch (action) {
            case RIGHT_CLICK_BLOCK:
                block = event.getClickedBlock();
                break;
            case RIGHT_CLICK_AIR:
                try
                {
                    block = player.getTargetBlock(null, 5);
                    if(block == null)
                        return;
                    else if(block.getType() == Material.AIR)
                        return;
                    else if(block.getLocation().distance(player.getLocation()) > 4)
                        return;
                }
                catch(Exception e)
                {
                    return;
                }
                break;
            default:
                return;
        }

        if (!SignUtils.isSign(block.getType()) && !SupplySignUtil.isContainer(block)) {
            return;
        }
		
        Sign sign;
        if(SupplySignUtil.isContainer(block)){
                sign = SupplySignUtil.getAttachedSign(block);
                if(sign == null) return;
                
                if (!block.getType().equals(Material.CHEST)) {
                    // prevent opening inventory of a dispenser with a supplysign attached
                    event.setCancelled(true);
                    if(Plugin.Config.ShowErrorsInClient)
                            player.sendMessage("§cErr: SupplySign attached to dispenser, inventory unavailable.");
                    return;
                }
        }
        else {
            sign = (Sign)block.getState();
        }
		
        try
        {
            if (!SupplySignUtil.isSupplySign(sign)) {
                return;
            }
            event.setCancelled(true);

            if (SignUtils.isSignWall(sign.getBlock().getType())) // special checks for wall signs on chests or dispensers
            {
                Block blockBehindSign = SupplySignUtil.getBlockBehindWallSign(sign);
                if(blockBehindSign.getType() == Material.DISPENSER) // if it's a dispenser cancel right click on sign

                {
                    if(Plugin.Config.ShowErrorsInClient)
                        player.sendMessage("§cErr: SupplySign attached to dispenser, inventory unavailable.");
                    return;
                }
                else if(blockBehindSign.getType() == Material.CHEST && 
                        SignUtils.isSignWall(block.getType())) // if it's a chest simulate a click on the chest and return
                {
                    Event e = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getItemInHand(), blockBehindSign, event.getBlockFace());
                    Plugin.getServer().getPluginManager().callEvent(e);
                    event.setCancelled(true);
                    return;
                }
            }

            String[] itemList = null;
            // if it's a kit, test for generic access permission or access to this specific kit
            if(sign.getLine(1).trim().contains("kit:")){
                String[] split = sign.getLine(1).trim().split(":");

                if(Plugin.isAuthorized(player, "access") || Plugin.isAuthorized(player, "access." + split[1]))
                    itemList = Plugin.Kits.getKit(split[1]).stream().map(Object::toString).toArray(String[]::new);
                else if(Plugin.Config.ShowErrorsInClient)
                    player.sendMessage("§cErr: you don't have permission to access this SupplySign.");
            }
            else
            {
                if(Plugin.isAuthorized(player, "access"))
                {
                    // it's not a kit, so load the items from the lines on the sign
                    
                    itemList = Stream.of(sign.getLines()).skip(1).map(s -> SupplySignUtil.stripColorCodes(s).trim())
                        .filter(i -> !"".equalsIgnoreCase(i))
                        .toArray(String[]::new);
                }
                else if(Plugin.Config.ShowErrorsInClient)
                    player.sendMessage("§cErr: you don't have permission to access this SupplySign.");
            }

            if(itemList != null && itemList.length > 0)
                Plugin.Items.showInventory(player, itemList);
        }
        catch (Throwable ex)
        {
            if(Plugin.Config.ShowErrorsInClient)
                player.sendMessage("§cErr: " + ex.getMessage());
        }
    }
}

