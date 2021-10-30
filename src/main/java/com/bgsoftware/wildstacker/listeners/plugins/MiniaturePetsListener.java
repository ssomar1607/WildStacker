package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.kirelcodes.miniaturepets.api.events.pets.PetFinishedSpawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class MiniaturePetsListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMiniaturePetSpawn(PetFinishedSpawnEvent e) {
        LivingEntity livingEntity = e.getPet().getNavigator();
        if (EntityUtils.isStackable(livingEntity))
            WStackedEntity.of(livingEntity).setSpawnCause(SpawnCause.MINIATURE_PETS);
    }

}