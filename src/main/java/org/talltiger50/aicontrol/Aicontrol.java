package org.talltiger50.aicontrol;


import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.*;

import com.google.gson.*;
import org.bukkit.util.Vector;

import static org.bukkit.Bukkit.*;

public class Aicontrol extends JavaPlugin {
    HttpServer server;
    public ArrayList<AI> ais = new ArrayList<>();

    @Override
    public void onEnable() {
        //new AI(new Location(getWorld("world"),17,76,9),this);
        getLogger().info("JsonControlPlugin has been enabled!");
        startHttpServer(this);

    }

    @Override
    public void onDisable() {
        getLogger().info("JsonControlPlugin has been disabled!");
        server.stop(0);
        for(AI ai:ais){
            ai.delete();
        }
        ais.clear();
    }

    private void startHttpServer(Aicontrol ins) {
        try {
             server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/jsoncommand", new JsonCommandHandler(this));
            server.start();
        } catch (IOException e) {
            getLogger().warning("Failed to start HTTP server: " + e.getMessage());
        }
    }


    class JsonCommandHandler implements HttpHandler {
        Aicontrol outerInstance;

        public JsonCommandHandler(Aicontrol outerInstance) {
            this.outerInstance = outerInstance;
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle incoming JSON command here
            // For demonstration, just log the request body
            String response = "";
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                //System.out.println("Received JSON command: " + requestBody);
                System.out.println(requestBody);
                // Parse the JSON request body using Gson
                Gson gson = new Gson();
                JsonObject jsonRequest = gson.fromJson(requestBody, JsonObject.class);

                // Extract the command from the JSON request
                String command = jsonRequest.get("command").getAsString();


                // Perform actions based on the command
                // Example: You can switch on the command and perform different actions accordingly

                switch (command) {
                    case "message" -> {
                        response = "sent message";
                        String message = jsonRequest.get("message").getAsString();
                        outerInstance.getLogger().info(message);
                    }
                    // Perform action 1
                    case "makeai" -> {

                        // Perform action 2
                        float x, y, z;
                        try {
                            x = jsonRequest.get("x").getAsFloat();
                            y = jsonRequest.get("y").getAsFloat();
                            z = jsonRequest.get("z").getAsFloat();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing AI spawn location from JSON request. make");
                            response = "error";
                            return;
                        }

                        try {
                            Location aiLocation = new Location(getWorlds().get(0), x, y, z);
                            AI newAI = new AI(aiLocation, Bukkit.getPluginManager().getPlugin("Aicontrol"), ais.size());
                            ais.add(newAI);
                            outerInstance.getLogger().info(String.valueOf(ais.size()));
                            response = "making ai";
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.out.println("Error creating AI object. make");
                            response = "error";

                        }

                    }
                    case "moveai" -> {
                        response = "making ai";
                        // Perform action 2
                        float x, y, z;
                        int index;
                        try {
                            x = jsonRequest.get("x").getAsFloat();
                            y = jsonRequest.get("y").getAsFloat();
                            z = jsonRequest.get("z").getAsFloat();
                            index = jsonRequest.get("index").getAsInt();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing AI spawn location from JSON request. move");
                            return;
                        }
                        System.out.println(index);
                        ais.get(index).move(new Vector(x, y, z));

                    }
                    case "getblocks" -> {

                        int index;
                        try {
                            index = jsonRequest.get("index").getAsInt();
                            outerInstance.getLogger().info(String.valueOf(ais.size()));
                            response = ais.get(index).getBlocksAroundPoint(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing AI spawn location from JSON request. getblocks");
                            response = "";
                        }


                    }
                    case "deleteall" -> {
                        try {
                            for (AI ai : ais) {
                                ai.delete();
                            }
                            ais.clear();
                            outerInstance.getLogger().info("deleting ai");
                            response = "done";
                        }catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("deleting ai");
                            response = "done";
                        }
                    }


                    case "count" -> {
                        response = "" + ais.size();
                    }
                    case "getpos" -> {

                        int index;
                        try {

                            index = jsonRequest.get("index").getAsInt();
                            response = ais.get(index).getPos();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing AI data from JSON request. getpos");
                            response = "";
                        }

                    }
                    case "reset" -> {

                        int index;
                        try {

                            index = jsonRequest.get("index").getAsInt();
                            ais.get(index).reset();
                            response = "reseting";
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing AI data from JSON request. reset");
                            response = "";
                        }
                    }
                    case "block" -> {
                        int index, x, y, z;
                        ItemStack item;
                        boolean type;
                        try {

                            index = jsonRequest.get("index").getAsInt();
                            type = jsonRequest.get("type").getAsBoolean();

                            if (type) {
                                x = jsonRequest.get("x").getAsInt();
                                y = jsonRequest.get("y").getAsInt();
                                z = jsonRequest.get("z").getAsInt();
                                ais.get(index).breakBlock(new Vector(x, y, z));
                            }

                            response = "breaking block";
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing AI data from JSON request. block");
                            response = "";
                        }
                    }
                    case "getitems" -> {
                        int index;

                        try {
                            index = jsonRequest.get("index").getAsInt();
                            JsonArray JsonID = new JsonArray();
                            for (ItemStack id : ais.get(index).data.aiInventory.getContents()) {
                                JsonID.add(id.getType().ordinal());
                            }
                            JsonArray JsonName = new JsonArray();
                            for (ItemStack id : ais.get(index).data.aiInventory.getContents()) {
                                JsonName.add(id.getType().name());
                            }

                            // Create a JSON object with key "blocks" and value as the JSON array
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.add("name", JsonID);
                            jsonObject.add("ids", JsonName);

                            // Convert JSON object to string
                            response = new Gson().toJson(jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing AI data from JSON request. getitems");
                            response = "";
                        }
                    }

                    default -> response = "Unknown command: " + command;

                    // Handle unknown command
                }
            }catch (IOException e){
                e.printStackTrace();
                response = "Error processing request";
            }finally {
                // Send response back to the client
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

        }
    }

}

