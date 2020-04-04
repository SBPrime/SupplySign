package redsgreens.SupplySign;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SupplySignItemStack {

    private final Material material;
    private final int amount;

    public SupplySignItemStack(Material m, int a)
    {
            material = m;
            amount = a;
    }

    public ItemStack getItemStack()
    {
            return new ItemStack(material, amount);
    }

    public Material getMaterial()
    {
            return material;
    }

    public int getAmount()
    {
            return amount;
    }
	
    @Override
    public boolean equals(Object other) 
    {
        if (this == other)
          return true;
        if (!(other instanceof SupplySignItemStack))
          return false;
        SupplySignItemStack otherIS = (SupplySignItemStack) other;
        return (this.material == otherIS.getMaterial() && this.amount == otherIS.getAmount()); 
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() { return this.material.hashCode(); }
	
    @Override
    public String toString()
    {
        return "Material=" + material.name().toLowerCase() + ", Amount=" + amount;
    }
}
