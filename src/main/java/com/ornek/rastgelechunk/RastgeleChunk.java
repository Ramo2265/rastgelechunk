package com.ornek.rastgelechunk;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class RastgeleChunk extends JavaPlugin implements Listener {

    // Her tikte kac katman (y seviyesi) degisecek. Lag olursa dusur.
    private static final int KATMAN_PER_TIK = 8;

    private final Random random = new Random();
    private final List<Material> bloklar = new ArrayList<>();
    private final Set<UUID> islemdekiler = new HashSet<>();

    @Override
    public void onEnable() {
        for (Material m : Material.values()) {
            if (m.isLegacy() || !m.isBlock() || !m.isSolid() || !m.isOccluding()) continue;
            String n = m.name();
            if (n.contains("CHEST") || n.contains("SHULKER") || n.contains("SPAWNER")
                    || n.contains("COMMAND") || n.contains("STRUCTURE") || n.contains("JIGSAW")
                    || n.contains("BARRIER") || n.contains("BEDROCK") || n.contains("BARREL")
                    || n.contains("FURNACE") || n.contains("HOPPER") || n.contains("DISPENSER")
                    || n.contains("DROPPER") || n.contains("TEST_")) continue;
            bloklar.add(m);
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("RastgeleChunk aktif. " + bloklar.size() + " blok turu kullanilacak.");
    }

    @EventHandler
    public void onJump(PlayerStatisticIncrementEvent e) {
        if (e.getStatistic() != Statistic.JUMP) return;

        Player p = e.getPlayer();
        if (!islemdekiler.add(p.getUniqueId())) return; // onceki islem bitmeden yenisini baslatma

        Chunk chunk = p.getLocation().getChunk();
        World world = chunk.getWorld();
        int px = p.getLocation().getBlockX();
        int py = p.getLocation().getBlockY();
        int pz = p.getLocation().getBlockZ();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        new BukkitRunnable() {
            int y = minY;

            @Override
            public void run() {
                if (!chunk.isLoaded()) {
                    islemdekiler.remove(p.getUniqueId());
                    cancel();
                    return;
                }
                int bitis = Math.min(y + KATMAN_PER_TIK, maxY);
                for (; y < bitis; y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            var b = chunk.getBlock(x, y, z);
                            // Oyuncunun ayaginin altini ve bulundugu yeri atla
                            if (b.getX() == px && b.getZ() == pz && b.getY() >= py - 1 && b.getY() <= py + 1) continue;
                            b.setType(bloklar.get(random.nextInt(bloklar.size())), false);
                        }
                    }
                }
                if (y >= maxY) {
                    islemdekiler.remove(p.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }
}
