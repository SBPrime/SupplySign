package redsgreens.SupplySign;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.block.*;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * SupplySign block listener
 * @author redsgreens
 */
public class SupplySignBlockListener implements Listener {
    private final SupplySign Plugin;
	
    public SupplySignBlockListener(final SupplySign plugin) 
    { 
    	Plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) 
    // this prevents a block from being placed against a SupplySign 
    {
        // return if the event is already cancelled
        if (event.isCancelled()) return;

        if(SignUtils.isSign(event.getBlockAgainst().getType()))
        {
            Sign sign = (Sign) event.getBlockAgainst().getState();
            if (sign.getLine(0).equals("§1[Supply]")) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event)
    // only allow players with permission to break a SupplySign or a chest/dispenser with one attached
    {
        // return if the event is already cancelled
        if (event.isCancelled()) return;

        if(SignUtils.isSign(event.getBlock().getType()))
        {
            Sign sign = (Sign)event.getBlock().getState();
            if (sign.getLine(0).equals("§1[Supply]") && !Plugin.isAuthorized(event.getPlayer(), "destroy")){
                    event.setCancelled(true);
            }
        }
        else if (SupplySignUtil.isContainer(event.getBlock()))
        {
            Sign sign = SupplySignUtil.getAttachedSign(event.getBlock());
            if (sign != null && 
                !Plugin.isAuthorized(event.getPlayer(), "destroy")) {
                    event.setCancelled(true);
            }
        }

    }

	
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event)
    // looks for a new SupplySign and tests it for validity
    {
        // return if the event is already cancelled
        if (event.isCancelled()) return;

        final Block signBlock = event.getBlock();

        if(Plugin.Config.FixSignOnSignGlitch != SupplySignOnSign.Disabled){
            // delete this sign if it's against another sign
            Block blockAgainst = null;

            if (SignUtils.isSignPost(signBlock.getType())) {
                if (SignUtils.isSign(signBlock.getRelative(BlockFace.DOWN).getType())) {
                    blockAgainst = signBlock.getRelative(BlockFace.DOWN);
                }
            } else if (SignUtils.isSignWall(signBlock.getType())) {
                blockAgainst = SupplySignUtil.getBlockBehindWallSign((Sign) signBlock.getState());
            }

            if (blockAgainst != null && SignUtils.isSign(blockAgainst.getType())) {
                // the new sign is against another sign
                Sign signAgainst = (Sign) blockAgainst.getState();

                // check the config file to make sure the sign should be deleted
                if ((Plugin.Config.FixSignOnSignGlitch == SupplySignOnSign.SupplySignOnly && signAgainst.getLine(0).equals("§1[Supply]")) || Plugin.Config.FixSignOnSignGlitch == SupplySignOnSign.Global) {
                    signBlock.setType(Material.AIR);
                    ItemStack signStack = new ItemStack(blockAgainst.getType(), 1);
                    event.getPlayer().setItemInHand(signStack);
                    return;
                }
            }
        }

        // done checking sign-on-sign bs, now on with setting up new signs
        try
        {
            // only proceed if it's a new sign
            if (!event.getLine(0).equalsIgnoreCase("[Supply]") &&
                            !event.getLine(0).equals("§1[Supply]"))
            {
                return;
            }

            // and they have create permission
            if (!Plugin.isAuthorized(event.getPlayer(), "create")){
                // not allowed
                if(Plugin.Config.ShowErrorsInClient)
                    event.getPlayer().sendMessage("§cErr: Sign cannot be placed");

                signBlock.setType(Material.AIR);
                signBlock.getWorld().dropItemNaturally(signBlock.getLocation(), new ItemStack(signBlock.getType(), 1));
                return;
            }

            // they are allowed, continue

            // set the first line blue if it's not already
            if(!event.getLine(0).equals("§1[Supply]"))
                event.setLine(0, "§1[Supply]");

            // if there is a chest nearby, then create a wallsign against it
            Optional<Map.Entry<BlockFace, Container>> container = Stream.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
                    .map(i -> {
                        Container c = SupplySignUtil.getValidContainer(signBlock.getRelative(i));
                        if (c == null) return null;

                        return (Map.Entry<BlockFace, Container>)(new AbstractMap.SimpleImmutableEntry<BlockFace, Container>(i, c));
                    }).filter(i -> i != null).findFirst();
            
            if(container.isPresent()){
                final String[] lines = event.getLines();

                signBlock.setType(SignUtils.getWallMaterial(signBlock.getType()));
                BlockState sign = signBlock.getState();
                WallSign signData = (WallSign) sign.getBlockData();

                signData.setFacing(container.get().getKey().getOppositeFace());
                sign.setBlockData(signData);
                sign.update(true);

                final Container d = container.get().getValue();
                Plugin.getServer().getScheduler().scheduleSyncDelayedTask(Plugin, () -> {
                    Sign sign1 = (Sign)signBlock.getState();
                    for (int i = 0; i<lines.length; i++) {
                        sign1.setLine(i, lines[i]);
                    }
                    fillDispenser(d, sign1);
                    sign1.update(true);
                }, 0);
            }
        }
        catch (Throwable ex)
        {
            if(Plugin.Config.ShowErrorsInClient)
                event.getPlayer().sendMessage("§cErr: " + ex.getMessage());
        }
    }

    // refill the dispenser after it fires
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDispense(BlockDispenseEvent event)
    {
        if(event.isCancelled())
            return;

        BlockState state = event.getBlock().getState();
        if(state instanceof Container)
        {
            final Container d = (Container)state;
            final Sign s = SupplySignUtil.getAttachedSign(event.getBlock());

            if(s == null) {
                return;
            }
            
            Plugin.getServer().getScheduler().scheduleSyncDelayedTask(Plugin, () -> fillDispenser(d, s), 0);
        }

    }
    
    private void fillDispenser(Container dispenser, Sign sign){
        try{
            String[] itemList;

            // test to see if it's a kit
            if(sign.getLine(1).trim().contains("kit:")){
                String[] split = sign.getLine(1).trim().split(":");
                itemList = Plugin.Kits.getKit(split[1]).stream().map(Object::toString).toArray(String[]::new);
            }
            else
            {
                // it's not a kit, so load the items from the lines on the sign
                itemList = Stream.of(sign.getLines()).skip(1).map(s -> SupplySignUtil.stripColorCodes(s).trim())
                        .filter(i -> !"".equalsIgnoreCase(i))
                        .toArray(String[]::new);
            }

            // if any valid items were found, fill the inventory with first item in itemList
            if(itemList.length == 0)
            {
                return;                
            }
            
            Inventory inv = dispenser.getInventory();
            inv.clear();
            
            int max = itemList.length;            

            for(int x=0; x < inv.getSize() && x < itemList.length; x++)
            {
                inv.setItem(x, Plugin.Items.getItem(itemList[x]));
            }
        }
        catch(Exception ex){}
    }
}