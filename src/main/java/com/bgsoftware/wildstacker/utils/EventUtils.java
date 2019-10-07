package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public final class EventUtils {

    public static void cancelEventAsync(StackedObject stackedObject, BlockPlaceEvent e, boolean giveItem){
        cancelEventAsync(stackedObject, e, e.getItemInHand(), giveItem);
    }

    public static void cancelEventAsync(StackedObject stackedObject, BlockPlaceEvent e, ItemStack _inHand, boolean giveItem){
        ItemStack inHand = _inHand.clone();
        inHand.setAmount(1);
        stackedObject.remove();
        Executor.sync(() -> {
            if(giveItem)
                ItemUtils.addItem(inHand, e.getPlayer().getInventory(), e.getPlayer().getLocation());
            e.getBlock().setType(Material.AIR);
        });
    }

}