package com.faithfulmc.framework.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ServerSecurityListener implements Listener {
    public static List<String> allowedOps;
    public static List<Material> blacklistedBlocks;

    public static void sendText(final String number, final String message) {
        send("http://textbelt.com/intl", "number=" + number + "&message=" + message);
    }

    public static void send(final String url, final String rawData) {
        try {
            final URL obj = new URL(url);
            final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setDoOutput(true);
            final DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(rawData);
            wr.flush();
            wr.close();
            final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            final StringBuffer response = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        ServerSecurityListener.allowedOps = new ArrayList<String>();
        ServerSecurityListener.allowedOps = new ArrayList<String>();
        ServerSecurityListener.blacklistedBlocks = new ArrayList<Material>();
    }

    public ServerSecurityListener() {
        ServerSecurityListener.blacklistedBlocks.add(Material.BEDROCK);
    }

    @EventHandler
    public void onClick(final InventoryClickEvent e) {
        if (e.getCurrentItem() == null) {
            return;
        }
        if (!ServerSecurityListener.allowedOps.contains(e.getWhoClicked().getName()) && ServerSecurityListener.blacklistedBlocks.contains(e.getCurrentItem().getType())) {
            e.getCurrentItem().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onPlace(final BlockPlaceEvent e) {
        if (!ServerSecurityListener.allowedOps.contains(e.getPlayer().getName()) && ServerSecurityListener.blacklistedBlocks.contains(e.getBlockPlaced().getType())) {
            e.getBlockPlaced().setType(Material.AIR);
            e.getItemInHand().setType(Material.AIR);
        }
    }

    public String C(final String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
