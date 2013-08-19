package ca.seenet.seecraft.hurricane;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Limit player movement within a world to a circular area around the global spawn.
 * Only restricts X/Z movement, not vertical (Y) movement.
 *
 * @author scoleman
 */
public class MovementRestrictor implements Listener
{
    private static final int DAMAGE_DURATION = 100;

    /**
     * The affected world.
     */
    private World world;
    /**
     * The current radius of the restricted area.
     */
    private double radius;

    /**
     * Set up movement restriction on a world.
     *
     * @param world the affected world
     */
    public MovementRestrictor(World world)
    {
        this.world = world;
        this.radius = 0.0;
    }

    /**
     * Get the radius.
     *
     * @return the limitation radius
     */
    public double getRadius()
    {
        return this.radius;
    }

    /**
     * Restrict players to a given radius. If the radius is set to 0, no limit is enforced.
     *
     * @param radius the radius of the restricted area
     */
    public void setRadius(double radius)
    {
        this.radius = radius;

        for (Player player : this.world.getPlayers())
        {
            /* We don't want to let players spawn in an invalid bed -- that would be unkind. */
            if (!this.validLocation(player.getBedSpawnLocation()))
                player.setBedSpawnLocation(null);

            if (!this.validLocation(player.getLocation()))
                this.punishPlayer(player);
        }
    }

    /**
     * Determine if a given location is valid.
     *
     * @param location the location to test
     * @return whether or not the location is valid
     */
    public boolean validLocation(Location location)
    {
        /* If we have no radius to enforce, all locations are valid. */
        if (this.radius == 0.0)
            return true;

        /* If the location doesn't exist, it might as well be valid. */
        if (location == null)
            return true;

        Location worldSpawn = this.world.getSpawnLocation();
        double offsetX = Math.abs(worldSpawn.getX() - location.getX());
        double offsetY = Math.abs(worldSpawn.getY() - location.getY());
        double offset = Math.sqrt(offsetX * offsetX + offsetY * offsetY);

        return (this.radius > offset);
    }

    @EventHandler
    public void onPlayerMovement(PlayerMoveEvent event)
    {
        Location origin = event.getFrom();
        Location destination = event.getTo();

        /* If the player is only reorienting themselves or moving within the current block, we assume we don't need to
         * do anything -- they should already have been handled when they moved into their current situation, and even
         * if that's somehow become invalid, that will be fixed when they next move. */
        if (origin.getBlockX() == destination.getBlockX()
                && origin.getBlockZ() == destination.getBlockZ())
            return;

        /* If the destination location is uncool, we better do something about it. */
        if (!this.validLocation(event.getTo()))
            punishPlayer(event.getPlayer());
    }

    private void punishPlayer(Player player)
    {
        switch (this.world.getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ()))
        {
            case OCEAN:
            case RIVER:
            case TAIGA:
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.POISON, MovementRestrictor.DAMAGE_DURATION, 1));
                break;
            default:
                player.setFireTicks(MovementRestrictor.DAMAGE_DURATION);
        }
    }
}
