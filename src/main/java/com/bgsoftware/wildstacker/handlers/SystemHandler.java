package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import com.bgsoftware.wildstacker.api.loot.LootTable;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.data.AbstractDataHandler;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.objects.WStackedObject;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.tasks.KillTask;
import com.bgsoftware.wildstacker.tasks.SaveTask;
import com.bgsoftware.wildstacker.tasks.StackTask;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.ReflectionUtil;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.google.common.collect.Iterators;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.List;

public final class SystemHandler implements SystemManager {

    private WildStackerPlugin plugin;
    private AbstractDataHandler dataHandler;

    public SystemHandler(WildStackerPlugin plugin){
        this.plugin = plugin;
        this.dataHandler = plugin.getDataHandler();

        //Start all required tasks
        Executor.sync(() -> {
            SaveTask.start();
            KillTask.start();
            StackTask.start();
        }, 1L);

        //Start the auto-clear
        //Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::performCacheClear, 300L, 300L);
    }

    /*
     * StackedObject's methods
     */

    @Override
    public void removeStackObject(StackedObject stackedObject) {
        if(stackedObject instanceof StackedEntity)
            dataHandler.CACHED_ENTITIES.remove(dataHandler.DEFAULT_CHUNK, ((StackedEntity) stackedObject).getUniqueId());
        else if(stackedObject instanceof StackedItem)
            dataHandler.CACHED_ITEMS.remove(((WStackedObject) stackedObject).getChunk(), ((StackedItem) stackedObject).getUniqueId());
        else if(stackedObject instanceof StackedSpawner)
            dataHandler.CACHED_SPAWNERS.remove(((WStackedObject) stackedObject).getChunk(), ((StackedSpawner) stackedObject).getLocation());
        else if(stackedObject instanceof StackedBarrel)
            dataHandler.CACHED_BARRELS.remove(((WStackedObject) stackedObject).getChunk(), ((StackedBarrel) stackedObject).getLocation());
    }

    @Override
    public StackedEntity getStackedEntity(LivingEntity livingEntity) {
//        if(dataHandler.CACHED_OBJECTS.containsKey(livingEntity.getUniqueId())) {
//            if(!(livingEntity instanceof Player) && !(livingEntity instanceof ArmorStand))
//                return (StackedEntity) dataHandler.CACHED_OBJECTS.get(livingEntity.getUniqueId());
//        }
//
//        StackedEntity stackedEntity = new WStackedEntity(livingEntity);
//
//        if(dataHandler.CACHED_AMOUNT_ENTITIES.containsKey(livingEntity.getUniqueId())) {
//            stackedEntity.setStackAmount(dataHandler.CACHED_AMOUNT_ENTITIES.get(livingEntity.getUniqueId()), false);
//            dataHandler.CACHED_AMOUNT_ENTITIES.remove(livingEntity.getUniqueId());
//        }
//
//        if(dataHandler.CACHED_SPAWN_REASON_ENTITIES.containsKey(livingEntity.getUniqueId())){
//            stackedEntity.setSpawnReason(dataHandler.CACHED_SPAWN_REASON_ENTITIES.get(livingEntity.getUniqueId()));
//            dataHandler.CACHED_SPAWN_REASON_ENTITIES.remove(livingEntity.getUniqueId());
//        }
//
//        if(!(livingEntity instanceof Player) && !(livingEntity instanceof ArmorStand) &&
//                plugin.getSettings().entitiesStackingEnabled && !plugin.getSettings().blacklistedEntities.contains(livingEntity.getType().name()) &&
//                !plugin.getSettings().entitiesDisabledWorlds.contains(livingEntity.getWorld().getName()))
//            dataHandler.CACHED_OBJECTS.put(livingEntity.getUniqueId(), stackedEntity);
//
//        return stackedEntity;
        StackedEntity stackedEntity = dataHandler.CACHED_ENTITIES.get(dataHandler.DEFAULT_CHUNK, livingEntity.getUniqueId());

        if(stackedEntity != null && stackedEntity.getLivingEntity() != null)
            return stackedEntity;

        //Entity wasn't found, creating a new object
        stackedEntity = new WStackedEntity(livingEntity);

        //A new entity was created. Let's see if we need to add him
        if(!(livingEntity instanceof Player) && !(livingEntity instanceof ArmorStand) &&
                plugin.getSettings().entitiesStackingEnabled && stackedEntity.isWhitelisted() && !stackedEntity.isBlacklisted() && !stackedEntity.isWorldDisabled())
            dataHandler.CACHED_ENTITIES.put(dataHandler.DEFAULT_CHUNK, stackedEntity.getUniqueId(), stackedEntity);

        return stackedEntity;
    }

    @Override
    public StackedItem getStackedItem(Item item) {
//        if(dataHandler.CACHED_OBJECTS.containsKey(item.getUniqueId()))
//            return (StackedItem) dataHandler.CACHED_OBJECTS.get(item.getUniqueId());
//
//        StackedItem stackedItem = new WStackedItem(item);
//
//        if(dataHandler.CACHED_AMOUNT_ITEMS.containsKey(item.getUniqueId())) {
//            stackedItem.setStackAmount(dataHandler.CACHED_AMOUNT_ITEMS.get(item.getUniqueId()), false);
//            dataHandler.CACHED_AMOUNT_ITEMS.remove(item.getUniqueId());
//        }
//
//        if(plugin.getSettings().itemsStackingEnabled)
//            dataHandler.CACHED_OBJECTS.put(item.getUniqueId(), stackedItem);
//
//        return stackedItem;
        StackedItem stackedItem = dataHandler.CACHED_ITEMS.get(item.getLocation().getChunk(), item.getUniqueId());

        if(stackedItem != null && stackedItem.getItem() != null)
            return stackedItem;

        //Item wasn't found, creating a new object.
        stackedItem = new WStackedItem(item);

        //A new item was created. Let's see if we need to add him
        if(plugin.getSettings().itemsStackingEnabled && stackedItem.isWhitelisted() && !stackedItem.isBlacklisted() && !stackedItem.isWorldDisabled())
            dataHandler.CACHED_ITEMS.put(item.getLocation().getChunk(), stackedItem.getUniqueId(), stackedItem);

        return stackedItem;
    }

    @Override
    public StackedSpawner getStackedSpawner(CreatureSpawner spawner) {
        return getStackedSpawner(spawner.getLocation());
    }

    @Override
    public StackedSpawner getStackedSpawner(Location location) {
//        if(dataHandler.CACHED_OBJECTS.containsKey(location))
//            return (StackedSpawner) dataHandler.CACHED_OBJECTS.get(location);
//
//        StackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) location.getBlock().getState());
//
//        if(plugin.getSettings().spawnersStackingEnabled)
//            dataHandler.CACHED_OBJECTS.put(location, stackedSpawner);
//
//        return stackedSpawner;
        StackedSpawner stackedSpawner = dataHandler.CACHED_SPAWNERS.get(location.getChunk(), location);

        if(stackedSpawner != null)
            return stackedSpawner;

        //Spawner wasn't found, creating a new object
        stackedSpawner = new WStackedSpawner((CreatureSpawner) location.getBlock().getState());

        //A new spawner was created. Let's see if we need to add him
        if(plugin.getSettings().spawnersStackingEnabled && stackedSpawner.isWhitelisted() && !stackedSpawner.isBlacklisted() && !stackedSpawner.isWorldDisabled())
            dataHandler.CACHED_SPAWNERS.put(location.getChunk(), stackedSpawner.getLocation(), stackedSpawner);

        return stackedSpawner;
    }

    @Override
    public StackedBarrel getStackedBarrel(Block block) {
        return getStackedBarrel(block == null ? null : block.getLocation());
    }

    @Override
    public StackedBarrel getStackedBarrel(Location location) {
//        if(dataHandler.CACHED_OBJECTS.containsKey(location))
//            return (StackedBarrel) dataHandler.CACHED_OBJECTS.get(location);
//
//        StackedBarrel stackedBarrel = new WStackedBarrel(location, location.getBlock().getState().getData().toItemStack(1));
//        stackedBarrel.createDisplayBlock();
//
//        if(plugin.getSettings().barrelsStackingEnabled)
//            dataHandler.CACHED_OBJECTS.put(location, stackedBarrel);
//
//        return stackedBarrel;
        StackedBarrel stackedBarrel = dataHandler.CACHED_BARRELS.get(location.getChunk(), location);

        if(stackedBarrel != null)
            return stackedBarrel;

        //Barrel wasn't found, creating a new object
        stackedBarrel = new WStackedBarrel(location.getBlock(), location.getBlock().getState().getData().toItemStack(1));
        Executor.sync(() -> {
            if(isStackedBarrel(location.getBlock()))
                WStackedBarrel.of(location.getBlock()).createDisplayBlock();
        }, 2L);

        //A new barrel was created. Let's see if we need to add him
        if(plugin.getSettings().barrelsStackingEnabled && stackedBarrel.isWhitelisted() && !stackedBarrel.isBlacklisted() && !stackedBarrel.isWorldDisabled())
            dataHandler.CACHED_BARRELS.put(location.getChunk(), stackedBarrel.getLocation(), stackedBarrel);

        return stackedBarrel;
    }

    @Override
    public List<StackedEntity> getStackedEntities() {
        return Arrays.asList(Iterators.toArray(dataHandler.CACHED_ENTITIES.iterator(), StackedEntity.class));
    }

    @Override
    public List<StackedItem> getStackedItems() {
        return Arrays.asList(Iterators.toArray(dataHandler.CACHED_ITEMS.iterator(), StackedItem.class));
    }

    @Override
    public List<StackedSpawner> getStackedSpawners(){
        return Arrays.asList(Iterators.toArray(dataHandler.CACHED_SPAWNERS.iterator(), StackedSpawner.class));
    }

    @Override
    public List<StackedBarrel> getStackedBarrels(){
        return Arrays.asList(Iterators.toArray(dataHandler.CACHED_BARRELS.iterator(), StackedBarrel.class));
    }

    @Override
    public boolean isStackedSpawner(Block block) {
        return block != null && block.getType() == Materials.SPAWNER.toBukkitType() && dataHandler.CACHED_SPAWNERS.contains(block.getChunk(), block.getLocation());
    }

    @Override
    public boolean isStackedBarrel(Block block) {
        return block != null && block.getType() == Material.CAULDRON && dataHandler.CACHED_BARRELS.contains(block.getChunk(), block.getLocation());
    }

    @Override
    public void performCacheClear() {
        getStackedEntities().forEach(stackedEntity -> {
            if(isChunkLoaded(stackedEntity.getLivingEntity().getLocation()) && stackedEntity.getLivingEntity().isDead())
                stackedEntity.remove();
        });

        getStackedItems().forEach(stackedItem -> {
            if(stackedItem.getStackAmount() <= 1)
                removeStackObject(stackedItem);
            if(isChunkLoaded(stackedItem.getItem().getLocation()) && stackedItem.getItem().isDead())
                stackedItem.remove();
        });

        getStackedSpawners().forEach(stackedSpawner -> {
            if(stackedSpawner.getStackAmount() <= 1)
                removeStackObject(stackedSpawner);
            if(isChunkLoaded(stackedSpawner.getLocation()) && !isStackedSpawner(stackedSpawner.getSpawner().getBlock()))
                stackedSpawner.remove();
        });

        getStackedBarrels().forEach(stackedBarrel -> {
            if(isChunkLoaded(stackedBarrel.getLocation()) && !isStackedBarrel(stackedBarrel.getBlock()))
                stackedBarrel.remove();
        });
    }

    private boolean isChunkLoaded(Location location){
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    @Override
    public void updateLinkedEntity(LivingEntity livingEntity, LivingEntity newLivingEntity) {
        for(StackedSpawner stackedSpawner : getStackedSpawners()){
            LivingEntity linkedEntity = ((WStackedSpawner) stackedSpawner).getRawLinkedEntity();
            if(linkedEntity != null && linkedEntity.equals(livingEntity))
                stackedSpawner.setLinkedEntity(newLivingEntity);
        }
    }

    /*
     * General methods
     */

    @Override
    public <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type){
        return spawnEntityWithoutStacking(location, type, CreatureSpawnEvent.SpawnReason.SPAWNER);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type, CreatureSpawnEvent.SpawnReason spawnReason) {
        try{
            World world = location.getWorld();

            Class craftWorldClass = ReflectionUtil.getBukkitClass("CraftWorld");
            Object craftWorld = craftWorldClass.cast(world);

            Class entityClass = ReflectionUtil.getNMSClass("Entity");

            Object entity = craftWorldClass.getMethod("createEntity", Location.class, Class.class).invoke(craftWorld, location, type);

            Entity bukkitEntity = (Entity) entity.getClass().getMethod("getBukkitEntity").invoke(entity);

            EntitiesListener.noStackEntities.add(bukkitEntity.getUniqueId());

            craftWorldClass.getMethod("addEntity", entityClass, CreatureSpawnEvent.SpawnReason.class).invoke(craftWorld, entity, spawnReason);

            return type.cast(bukkitEntity);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void spawnCorpse(StackedEntity stackedEntity) {
        LivingEntity livingEntity = (LivingEntity) spawnEntityWithoutStacking(stackedEntity.getLivingEntity().getLocation(),
                stackedEntity.getType().getEntityClass(), CreatureSpawnEvent.SpawnReason.CUSTOM);
        if(livingEntity != null) {
            livingEntity.setMetadata("corpse", new FixedMetadataValue(plugin, ""));
            livingEntity.setHealth(0);
        }
    }

    @Override
    public void performKillAll(){
        Executor.async(() -> {
            for(StackedEntity stackedEntity : getStackedEntities()) {
                if (stackedEntity.getStackAmount() > 1)
                    stackedEntity.remove();
            }

            if(plugin.getSettings().itemsKillAll) {
                for (StackedItem stackedItem : getStackedItems()) {
                    if (stackedItem.getStackAmount() > 1) {
                        stackedItem.remove();
                    }
                }
            }

            for(Player pl : Bukkit.getOnlinePlayers()) {
                if (pl.isOp())
                    Locale.KILL_ALL_OPS.send(pl);
            }
        });
    }

    /*
     * Loot loot methods
     */

    @Override
    public LootTable getLootTable(LivingEntity livingEntity) {
        return plugin.getLootHandler().getLootTable(livingEntity);
    }
}
