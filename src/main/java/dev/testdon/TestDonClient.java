
package dev.testdon;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class TestDonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(
            client -> SoundMarkers.expire()
        );

        ThroughWallRenderer.register();
    }
}
