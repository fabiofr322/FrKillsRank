package fabiofr32.frKillsRank.managers;

import java.util.UUID;

public class PlayerRank {
    public UUID uuid;
    public int points;

    public PlayerRank(UUID uuid, int points) {
        this.uuid = uuid;
        this.points = points;
    }
}