package redsgreens.SupplySign;

import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;

public class SupplySignUtil {

    // check to see if this is a chest without a supply sign already on it
    public static boolean isValidChest(Block b) {
        if (b.getType() != Material.CHEST) {
            return false;
        }

        Block[] adjBlocks = new Block[]{b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST)};

        for (int i = 0; i < adjBlocks.length; i++) {
            if (SignUtils.isSignWall(adjBlocks[i].getType())) {
                Sign sign = (Sign) adjBlocks[i].getState();
                if (sign.getLine(0).equals("§1[Supply]")) {
                    return false;
                }
            }
        }

        return true;
    }

    // check to see if this is a dispenser without a supply sign already on it
    public static boolean isValidDispenser(Block b) {
        if (b.getType() != Material.DISPENSER) {
            return false;
        }

        Block[] adjBlocks = new Block[]{b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST)};

        for (int i = 0; i < adjBlocks.length; i++) {
            if (SignUtils.isSignWall(adjBlocks[i].getType())) {
                Sign sign = (Sign) adjBlocks[i].getState();
                if (sign.getLine(0).equals("§1[Supply]")) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isContainer(final Block b) {
        return b.getState() instanceof Container;
    }
    
    public static Container getValidContainer(final Block b) {
        if (!isContainer(b)) {
            return null;
        }

        if (Stream.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
                .map(b::getRelative)
                .filter(i -> SignUtils.isSignWall(i.getType()))
                .map(i -> ((Sign) i.getState()).getLine(0))
                .anyMatch(i -> "§1[Supply]".equals(i))) {
            return null;
        }

        return (Container) b.getState();
    }

    // check to see if this is a single wide chest
    public static boolean isSingleChest(Block b) {

        if (b.getType() != Material.CHEST) {
            return false;
        }

        Block[] adjBlocks = new Block[]{b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST)};

        for (int i = 0; i < adjBlocks.length; i++) {
            if (adjBlocks[i].getType() == Material.CHEST) {
                return false;
            }
        }

        return true;
    }

    // check to see if this is a single wide chest
    public static boolean isDoubleChest(Block b) {

        if (b.getType() != Material.CHEST) {
            return false;
        }

        Block[] adjBlocks = new Block[]{b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST)};

        for (int i = 0; i < adjBlocks.length; i++) {
            if (adjBlocks[i].getType() == Material.CHEST) {
                return true;
            }
        }

        return false;
    }

    // find a sign attached to a chest
    public static Sign getAttachedSign(Block b) {
        if (!isContainer(b)) {
            return null;
        }

        Block[] adjBlocks;
        
        if (isDoubleChest(b)) {
            // it's a double, so find the other half and check faces of both blocks
            Block b2 = findOtherHalfofChest(b);
            adjBlocks = new Block[]{b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST), b2.getRelative(BlockFace.NORTH), b2.getRelative(BlockFace.EAST), b2.getRelative(BlockFace.SOUTH), b2.getRelative(BlockFace.WEST)};
        } else {
            // it's a single chest or dispenser, so check the four adjacent blocks
            adjBlocks = new Block[]{b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST)};
        }

        for (int i = 0; i < adjBlocks.length; i++) {
            if (isSupplySign(adjBlocks[i])) {
                return (Sign) adjBlocks[i].getState();
            }
        }

        return null;
    }

    public static Block findOtherHalfofChest(Block b) {
        // didn't find one, so find the other half of the chest and check it's faces
        Block[] adjBlocks = new Block[]{b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST)};
        for (int i = 0; i < adjBlocks.length; i++) {
            if (adjBlocks[i].getType() == Material.CHEST) {
                return adjBlocks[i];
            }
        }

        return null;
    }

    // get the block that has a wall sign on it
    public static Block getBlockBehindWallSign(Sign sign) {
        Block blockAgainst = null;
        Block signBlock = sign.getBlock();

        if (!SignUtils.isSignWall(sign.getType())) {
            return null;
        }
        
        return signBlock.getRelative(((WallSign) sign.getBlockData()).getFacing().getOppositeFace());
    }

    public static String stripColorCodes(String str) {
        return str.replaceAll("\u00A7[0-9a-fA-F]", "");
    }

    public static Boolean isSupplySign(Sign sign) {
        if (sign.getLine(0).equals("§1[Supply]")) {
            return true;
        } else {
            return false;
        }
    }

    public static Boolean isSupplySign(Block b) {
        if (!SignUtils.isSign(b.getType())) {
            return false;
        }
        
        return isSupplySign((Sign) b.getState());
    }
}
