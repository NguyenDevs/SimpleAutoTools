package org.nguyendevs.simpleautotools.data;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private boolean enabled;

    public PlayerData(UUID uuid, boolean enabled) {
        this.uuid = uuid;
        this.enabled = enabled;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}