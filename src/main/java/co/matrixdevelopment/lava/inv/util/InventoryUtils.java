package co.matrixdevelopment.lava.inv.util;

import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.tileentity.TileEntity;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

public class InventoryUtils {

    @Nullable
    public static InventoryHolder getInventoryOwner(IInventory inventory) {
        if (inventory instanceof TileEntity) {
            TileEntity te = (TileEntity) inventory;
            BlockState state = te.getWorld().getWorld().getBlockAt(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()).getState();
            if (state instanceof InventoryHolder) return (InventoryHolder) state;
        } else if (inventory instanceof InventoryBasic) {
            InventoryHolder ib = (InventoryHolder) inventory;
            if (ib instanceof ContainerHorseChest) return (InventoryHolder) ((ContainerHorseChest) ib).getAnimal().getBukkitEntity();
            if (ib instanceof InventoryEnderChest) return ((InventoryEnderChest) ib).getBukkitOwner();
        } else if (inventory instanceof EntityMinecartContainer) {
            Entity cart = ((EntityMinecartContainer) inventory).getBukkitEntity();
            if (cart instanceof InventoryHolder) return (InventoryHolder) cart;
        } else if (inventory instanceof InventoryPlayer) {
            InventoryPlayer ip = (InventoryPlayer) inventory;
            return ip.player.getBukkitEntity();
        } else if (inventory instanceof InventoryCrafting) {
            return inventory.getOwner();
        } else if (inventory instanceof InventoryMerchant) {
            return ((InventoryMerchant) inventory).getPlayer().getBukkitEntity();
        }

        return null;
    }
}
