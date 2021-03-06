package redsgreens.SupplySign;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SupplySignItems {

    private final SupplySign Plugin;

    private static Map<String, SupplySignItemStack> ItemsMap = new HashMap<String, SupplySignItemStack>();

    public SupplySignItems(final SupplySign plugin) {
        Plugin = plugin;
    }

    // read items.csv file
    public void loadItems() {
        try {
            loadCustomItems();
        } catch (IOException e) {
        }

        System.out.println("SupplySign loaded " + ItemsMap.size() + " items.");
    }

    private void loadCustomItems() throws IOException {
        // create the file from the one in the jar if it doesn't exist on disk
        File itemsFile = new File(Plugin.getDataFolder(), "items-custom.csv");
        if (!itemsFile.exists()) {
            itemsFile.createNewFile();
            InputStream res = SupplySign.class.getResourceAsStream("/items-custom.csv");

            FileWriter tx = new FileWriter(itemsFile);
            for (int i = 0; (i = res.read()) > 0;) {
                tx.write(i);
            }
            tx.flush();
            tx.close();
            res.close();

            return;
        }

        BufferedReader rx = new BufferedReader(new FileReader(itemsFile));
        try {

            for (int i = 0; rx.ready(); i++) {
                try {
                    String line = rx.readLine().trim().toLowerCase();
                    if (line.startsWith("#")) {
                        continue;
                    }

                    String[] parts = line.split(",");

                    String itemName = parts[0].toLowerCase();
                    String itemID = parts[1];
                    //Short itemDamage = Short.parseShort(parts[2]);
                    int itemStackSize = Integer.parseInt(parts[3]);

                    SupplySignItemStack stack = new SupplySignItemStack(Material.getMaterial(itemID.toUpperCase()), itemStackSize);

                    ItemsMap.put(itemName.toUpperCase(), stack);
                } catch (Exception ex) {
                    Plugin.logger.warning("Error parsing items-custom.csv on line " + i + ". " + ex.getMessage());
                }
            }
        } finally {
            rx.close();
        }
    }

    // return an ItemStack from by name
    public ItemStack getItem(String id) {
        // see if it's in the config files
        String id2 = SupplySignUtil.stripColorCodes(id.toUpperCase());
        if (ItemsMap.containsKey(id2)) {
            ItemStack is = ItemsMap.get(id2).getItemStack();
            return is;
        }

        try {
            // not in config files, see if bukkit can parse it
            Material m = Material.getMaterial(id2.toUpperCase());
            if (m != null) {
                return new ItemStack(m, m.getMaxStackSize());
            }

            // replace . with : (to allow either . or : to separate item and durability)
            if (id2.contains(".")) {
                id2.replace(".", ":");
            }

            // there's a : in the string, see if it looks like itemid:durability
            if (id2.contains(":")) {
                String split[] = id2.split(":");
                if (split.length == 2) {
                    m = Material.getMaterial(split[0].toUpperCase());
                    return new ItemStack(m, m.getMaxStackSize());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // arranges the items to be displayed and shows the inventory dialog
    public void showInventory(Player p, String[] itemList) {
        List<ItemStack> items = Stream.of(itemList).map(this::getItem).filter(i -> i != null).collect(Collectors.toList());
        Inventory inv = Plugin.getServer().createInventory(null, InventoryType.PLAYER);

        // clear the inventory
        inv.clear();

        try {
            switch (items.size()) {
                case 0:
                    return;
                case 1:
                    for (int i = 0; i < 36; i++) {
                        inv.setItem(i, items.get(0));
                    }
                    break;

                case 2:
                    for (int i = 0; i < 18; i++) {
                        inv.setItem(i, items.get(0));
                    }
                    for (int i = 0; i < 18; i++) {
                        inv.setItem(i + 18, items.get(1));
                    }
                    break;

                case 3:
                    for (int i = 0; i < 4; i++) {
                        inv.setItem((i * 9), items.get(0));
                        inv.setItem((i * 9) + 1, items.get(0));
                        inv.setItem((i * 9) + 2, items.get(0));
                        inv.setItem((i * 9) + 3, items.get(1));
                        inv.setItem((i * 9) + 4, items.get(1));
                        inv.setItem((i * 9) + 5, items.get(1));
                        inv.setItem((i * 9) + 6, items.get(2));
                        inv.setItem((i * 9) + 7, items.get(2));
                        inv.setItem((i * 9) + 8, items.get(2));
                    }
                    break;

                case 4:
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 9; j++) {
                            inv.setItem((i * 9) + j, items.get(i));
                        }
                    }
                    break;

                case 5:
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 7; j++) {
                            inv.setItem((i * 9) + j, items.get(i));
                        }
                    }
                    for (int k = 0; k < 4; k++) {
                        inv.setItem((k * 9) + 7, items.get(4));
                        inv.setItem((k * 9) + 8, items.get(4));
                    }
                    break;

                case 6:
                    for (int i = 0; i < 2; i++) {
                        inv.setItem((i * 9), items.get(0));
                        inv.setItem((i * 9) + 1, items.get(0));
                        inv.setItem((i * 9) + 2, items.get(0));
                        inv.setItem((i * 9) + 3, items.get(1));
                        inv.setItem((i * 9) + 4, items.get(1));
                        inv.setItem((i * 9) + 5, items.get(1));
                        inv.setItem((i * 9) + 6, items.get(2));
                        inv.setItem((i * 9) + 7, items.get(2));
                        inv.setItem((i * 9) + 8, items.get(2));
                    }
                    for (int i = 2; i < 4; i++) {
                        inv.setItem((i * 9), items.get(3));
                        inv.setItem((i * 9) + 1, items.get(3));
                        inv.setItem((i * 9) + 2, items.get(3));
                        inv.setItem((i * 9) + 3, items.get(4));
                        inv.setItem((i * 9) + 4, items.get(4));
                        inv.setItem((i * 9) + 5, items.get(4));
                        inv.setItem((i * 9) + 6, items.get(5));
                        inv.setItem((i * 9) + 7, items.get(5));
                        inv.setItem((i * 9) + 8, items.get(5));
                    }

                    break;

                case 7:
                    int pos1 = 0;
                    for (int i = 0; i < 7; i++) {
                        int jmax;
                        if (i % 2 == 0) {
                            jmax = 5;
                        } else {
                            jmax = 4;
                        }

                        for (int j = 0; j < jmax; j++) {
                            inv.setItem(pos1++, items.get(i));
                        }

                    }
                    break;

                case 8:
                    int pos2 = 0;
                    for (int i = 0; i < 4; i++) {
                        inv.setItem(pos2++, items.get((2 * i)));
                        inv.setItem(pos2++, items.get((2 * i)));
                        inv.setItem(pos2++, items.get((2 * i)));
                        inv.setItem(pos2++, items.get((2 * i)));
                        inv.setItem(pos2++, items.get((2 * i) + 1));
                        inv.setItem(pos2++, items.get((2 * i) + 1));
                        inv.setItem(pos2++, items.get((2 * i) + 1));
                        inv.setItem(pos2++, items.get((2 * i) + 1));
                        inv.setItem(pos2++, items.get((2 * i) + 1));
                    }
                    break;

                case 9:
                    int pos3 = 0;
                    for (int j = 0; j < 4; j++) {
                        for (int i = 0; i < 9; i++) {
                            inv.setItem(pos3++, items.get(i));
                        }
                    }
                    break;

                case 10:
                case 11:
                case 12:
                    int pos4 = 0;
                    for (int i = 0; i < itemList.length; i++) {
                        for (int j = 0; j < 3; j++) {
                            inv.setItem(pos4++, items.get(i));
                        }
                    }
                    break;

                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                    int pos;
                    for (int i = 0; i < itemList.length; i++) {
                        if (i < 9) {
                            pos = i;
                        } else {
                            pos = i + 9;
                        }
                        inv.setItem(pos, items.get(i));
                        inv.setItem(pos + 9, items.get(i));
                    }
                    break;

                default:
                    for (int i = 0; i < itemList.length; i++) {
                        inv.setItem(i, items.get(i));
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // show the inventory dialog 
        p.openInventory(inv);
    }

}
