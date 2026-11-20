package dev.testdon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.sound.SoundInstance;

/**
 * Stores sound events that the local client actually receives and starts playing.
 * This cannot reveal sounds that the server never sent to this client.
 */
public final class SoundMarkers {
    private static final long LIFETIME_MS = 2200L;
    private static final List<Marker> MARKERS = new ArrayList<>();

    private SoundMarkers() {}

    public static synchronized void add(SoundInstance sound) {
        if (sound == null || sound.isRelative()) return;

        MARKERS.add(new Marker(
            sound.getX(),
            sound.getY(),
            sound.getZ(),
            sound.getId().toString(),
            System.currentTimeMillis()
        ));

        // Avoid unbounded growth during busy scenes.
        while (MARKERS.size() > 160) {
            MARKERS.remove(0);
        }
    }

    public static synchronized List<Marker> snapshot() {
        return List.copyOf(MARKERS);
    }

    public static synchronized void expire() {
        long now = System.currentTimeMillis();
        Iterator<Marker> it = MARKERS.iterator();
        while (it.hasNext()) {
            if (now - it.next().createdAt() > LIFETIME_MS) {
                it.remove();
            }
        }
    }

    public record Marker(double x, double y, double z, String soundId, long createdAt) {}
          }
