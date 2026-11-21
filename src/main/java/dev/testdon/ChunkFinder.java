
package dev.testdon;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public final class ChunkFinder {

    private ChunkFinder() {}

    private static final Map<Long, Integer> RESULTS = new HashMap<>();
    private static final Map<Long, Long> LAST_SCAN = new HashMap<>();

    public static void scan(WorldChunk chunk) {
        long now = System.currentTimeMillis();
        ChunkPos pos = chunk.getPos();
        long key = pos.toLong();

        Long previous = LAST_SCAN.get(key);

        if (previous != null && now - previous < 5000L) {
            return;
        }

        LAST_SCAN.put(key, now);

        int count = 0;

        for (var entry : chunk.getBlockEntities().entrySet()) {
            Block block = chunk.getBlockState(entry.getKey()).getBlock();

            if (block == Blocks.CHEST
                    || block == Blocks.TRAPPED_CHEST
                    || block == Blocks.ENDER_CHEST
                    || block == Blocks.HOPPER
                    || block == Blocks.FURNACE
                    || block == Blocks.BLAST_FURNACE
                    || block == Blocks.SMOKER) {
                count++;
            }
        }

        if (count >= 3) {
            RESULTS.put(key, count);
        } else {
            RESULTS.remove(key);
        }
    }

    public static Map<Long, Integer> snapshot() {
        return Map.copyOf(RESULTS);
    }

    public static void clear() {
        RESULTS.clear();
        LAST_SCAN.clear();
    }
}
