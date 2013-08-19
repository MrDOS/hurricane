package ca.seenet.seecraft.hurricane;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Hurricane: watch out or it'll blow you away.
 *
 * @author scoleman
 */
public class HurricanePlugin extends JavaPlugin
{
    private MovementRestrictor movementRestrictor;

    @Override
    public void onEnable()
    {
        this.movementRestrictor = new MovementRestrictor(super.getServer().getWorlds().get(0));
        super.getServer().getPluginManager().registerEvents(this.movementRestrictor, this);
    }

    @Override
    public void onDisable()
    {
        this.movementRestrictor = null;
        HandlerList.unregisterAll(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!sender.isOp())
        {
            sender.sendMessage("Go away.");
            return true;
        }

        /* We don't care about any commands other than `jailradius` and aren't configured to receive them, so we don't
         * need to bother checking the command name. */

        if (args.length > 0)
        {
            try
            {
                double radius = Double.valueOf(args[0]);
                this.movementRestrictor.setRadius(radius);
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage("Invalid jail radius.");
            }
        }

        sender.sendMessage("The jail radius is " + this.movementRestrictor.getRadius() + ".");

        return true;
    }
}
