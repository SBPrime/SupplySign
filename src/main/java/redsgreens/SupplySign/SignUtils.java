package redsgreens.SupplySign;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Material;

/**
 *
 * @author SBPrime
 */
public final class SignUtils {
    
    private final static Map<Material, SignEntry> SIGNS;
    private final static SignEntry SIGN_DEFAULT = new SignEntry(null, Material.AIR, Material.AIR);

    static {
        Material[] signWall = new Material[] { 
            Material.ACACIA_WALL_SIGN, Material.BIRCH_WALL_SIGN, Material.DARK_OAK_WALL_SIGN,
            Material.JUNGLE_WALL_SIGN, Material.OAK_WALL_SIGN, Material.SPRUCE_WALL_SIGN
        };
        
        Material[] signPost = new Material[] { 
            Material.ACACIA_SIGN, Material.BIRCH_SIGN, Material.DARK_OAK_SIGN,
                    Material.JUNGLE_SIGN, Material.OAK_SIGN, Material.SPRUCE_SIGN
        };
        
        SIGNS = new HashMap<>();
        for (int i = 0; i < signPost.length; i++) {
            Material wall = signWall[i];
            Material post = signPost[i];
            
            SIGNS.put(wall, new SignEntry(SignType.WALL, wall, post));
            SIGNS.put(post, new SignEntry(SignType.POST, wall, post));
        }
    }
    
    private SignUtils() {}
    
    public static boolean isSign(Material m) { return SIGNS.containsKey(m); }
    public static boolean isSignWall(Material m) { return SignType.WALL.equals(SIGNS.getOrDefault(m, SIGN_DEFAULT).type); }
    public static boolean isSignPost(Material m) { return SignType.POST.equals(SIGNS.getOrDefault(m, SIGN_DEFAULT).type); }
    public static Material getWallMaterial(Material m) { return SIGNS.getOrDefault(m, SIGN_DEFAULT).materialWall; }
    public static Material getPostMaterial(Material m) { return SIGNS.getOrDefault(m, SIGN_DEFAULT).materialPost; }
            
    private static enum SignType { WALL, POST }
    
    private static class SignEntry {
        public final SignType type;
        public final Material materialWall;
        public final Material materialPost;
        
        public SignEntry(SignType type, Material materialWall, Material materialPost) {
            this.type = type;
            this.materialWall = materialWall;
            this.materialPost = materialPost;
        }
    }
}
