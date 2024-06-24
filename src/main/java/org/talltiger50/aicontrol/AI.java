package org.talltiger50.aicontrol;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class AI {
    public World world;
    public Location startpos;
    private LivingEntity aiMob;
    public AIData data =new AIData();


    public AI(Location pos, Plugin plugin, int name) {

        Chunk chunk = pos.getChunk();
        world = pos.getWorld();
        if (world == null) {
            System.out.println( "Invalid world provided for AI spawn location.");
            return;
        }
        System.out.println("Attempting to spawn AI entity at: " + pos);

        Bukkit.getScheduler().runTask(plugin, () -> {
            // Spawn entity here
            try{
            System.out.println("Attempting to spawn AI entity at: " + pos);
            chunk.load();
            aiMob = (LivingEntity) world.spawnEntity(pos, EntityType.ARMOR_STAND);
            if (aiMob == null) {
                System.out.println("Failed to spawn AI entity.");
                return;
            }
            aiMob.setCustomName(String.valueOf(name));
            aiMob.setCustomNameVisible(true);
            startpos=pos;
            System.out.println("AI created successfully.");
            }catch(Exception e){
                e.printStackTrace();

            }

        });


    }
    public boolean isEmpty(Location location) {
        return location.getBlock().isEmpty();
    }

    public void move(Vector vel) {
        Location aiMobLocation = aiMob.getLocation().clone();
        Location targetLocation = aiMobLocation.add(vel);

        if (isEmpty(targetLocation) &&
                isEmpty(targetLocation.clone().add(0.5, 0, 0)) &&
                isEmpty(targetLocation.clone().add(-0.5, 0, 0)) &&
                isEmpty(targetLocation.clone().add(0, 0, 0.5)) &&
                isEmpty(targetLocation.clone().add(0, 0, -0.5)) &&
                isEmpty(targetLocation.clone().add(0.5, 0, 0.5))) {

            if (aiMobLocation.clone().add(0,-1,0).getBlock().isEmpty()) {
                aiMob.teleport(new Location(aiMobLocation.getWorld(), targetLocation.getX(), aiMobLocation.getY(), targetLocation.getZ()));
            }else{
                aiMob.teleport(targetLocation);
            }
        }
    }
    public String getBlocksAroundPoint(int radius) {
        Location center = aiMob.getLocation();
        List<Integer> blockIds = new ArrayList<>();
        List<String> blockNames = new ArrayList<>();
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        Set<Location> visitedLocations = new HashSet<>(); // Track visited locations

        // Loop through blocks around the mob
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            for (int y = centerY - 2; y <= centerY + 2; y++) {
                for (int z = centerZ - 2; z <= centerZ + 2; z++) {
                    Location loc = new Location(world, x, y, z);

                    Block block = loc.getBlock();
                    int blockId = block.getType().ordinal();
                    // Store block type ordinal
                    blockIds.add(blockId);
                    // Store block type name
                    blockNames.add(block.getType().name());
                    // Mark the location as visited
                    visitedLocations.add(loc);



                }
            }
        }

        JsonArray JsonID = new JsonArray();
        for (Integer id : blockIds) {
            JsonID.add(id);
        }
        JsonArray JsonName = new JsonArray();
        for (String name : blockNames) {
            JsonName.add(name);
        }

        // Create a JSON object with key "blocks" and value as the JSON array
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("IDs", JsonID);
        jsonObject.add("Names", JsonName);

        // Convert JSON object to string
        return new Gson().toJson(jsonObject);
    }
    public void breakBlock(Vector relPos) {
        // Get the current location of aiMob
        Location mobLocation = aiMob.getLocation();
        Location targetLocation = mobLocation.clone().add(relPos);

        Block targetBlock = targetLocation.getBlock();
        // Break the block (set its type to AIR)
        for (ItemStack drop : targetBlock.getDrops()) {
            // Add each drop to the AI's inventory
            data.aiInventory.addItem(drop);
        }
        targetBlock.setType(Material.AIR);
        data.blocks.put(targetBlock.getLocation(),targetBlock.getBlockData());

        targetLocation.getWorld().playEffect(targetLocation, Effect.STEP_SOUND, targetBlock.getType());
    }
    public void placeBlock(ItemStack item,Vector relPos) {
        if (!data.aiInventory.contains(item) || item.getType().isBlock()){
            return;
        }
        // Get the current location of aiMob
        Location mobLocation = aiMob.getLocation();
        Location targetLocation = mobLocation.clone().add(relPos);

        Block targetBlock = targetLocation.getBlock();
        // Break the block (set its type to AIR)
        for (ItemStack drop : targetBlock.getDrops()) {
            // Add each drop to the AI's inventory
            data.aiInventory.addItem(drop);
        }
        data.blocks.put(targetBlock.getLocation(),Material.AIR.createBlockData());
        targetBlock.setType(item.getType());



    }
    public void reset(){
        if (!data.blocks.entrySet().isEmpty()) {
            for (Map.Entry<Location, BlockData> entry : data.blocks.entrySet()) {
                Location location = entry.getKey();
                BlockData block = entry.getValue();

                aiMob.getWorld().setBlockData(location, block);
            }
        }
        data.aiInventory.clear();
        data.blocks.clear();
        aiMob.teleport(startpos);

    }
    public void delete(){
        aiMob.remove();
    }
    public String getPos(){
        Vector pos=aiMob.getLocation().toVector();

        JsonArray posn = new JsonArray();
        posn.add(pos.getX());
        posn.add(pos.getY());
        posn.add(pos.getZ());
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("pos", posn);


        // Convert JSON object to string
        return new Gson().toJson(jsonObject);

    }

}


class AIData {

    public Inventory aiInventory = Bukkit.createInventory(null, 9, "ai Inventory");
    public Map<Location, BlockData> blocks = new HashMap<>();

    // Getters and setters (if needed)

    @Override
    public String toString() {
        return "";
    }
    public void giveItem(ItemStack item){
        if (aiInventory.contains(item)){

        }
        aiInventory.addItem(item);

    }
}
