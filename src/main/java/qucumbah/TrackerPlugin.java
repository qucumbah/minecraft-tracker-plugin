package qucumbah;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class TrackerPlugin extends JavaPlugin implements Listener {
  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  private HashMap<Player, Player> hunterVictimPairs = new HashMap<>();

  @EventHandler
  public void onPlayerUse(PlayerInteractEvent interactEvent) {
    Action action = interactEvent.getAction();
    if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
      return;
    }

    Player hunter = interactEvent.getPlayer();
    if (!hunterVictimPairs.containsKey(hunter)) {
      return;
    }

    EquipmentSlot hand = interactEvent.getHand();
    if (hunter.getInventory().getItem(hand).getType() != Material.COMPASS) {
      return;
    }

    Player victim = hunterVictimPairs.get(hunter);
    if (!victim.isOnline() || (victim.getWorld() != hunter.getWorld())) {
      return;
    }

    hunter.setCompassTarget(victim.getLocation());
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent respawnEvent) {
    Player respawnee = respawnEvent.getPlayer();
    if (!hunterVictimPairs.containsKey(respawnee)) {
      return;
    }

    giveTrackerTo(respawnee);
  }

  private boolean handleTrackStart(CommandSender sender, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("This command can only be run by a player");
      return false;
    }

    if (args.length != 1) {
      sender.sendMessage("You have to specify a victim");
      return false;
    }

    Player hunter = (Player)sender;
    Player victim = Bukkit.getServer().getPlayer(args[0]);

    if (victim == null) {
      sender.sendMessage(args[0] + " is not online");
      return false;
    }

    if (hunterVictimPairs.containsKey(hunter)) {
      sender.sendMessage("You can only hunt one player at a time");
      return false;
    }

    hunterVictimPairs.put(hunter, victim);
    giveTrackerTo(hunter);
    sender.sendMessage("You are now hunting " + victim.getName());
    return true;
  }

  private boolean handleTrackStop(CommandSender sender) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("This command can only be run by a player");
      return false;
    }

    Player hunter = (Player)sender;

    if (!hunterVictimPairs.containsKey(hunter)) {
      sender.sendMessage("You are not currently hunting anybody");
      return false;
    }

    sender.sendMessage("You have stopped hunting " + hunterVictimPairs.get(hunter));
    hunterVictimPairs.remove(hunter);
    return true;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    switch (command.getName()) {
      case "trackstart":
        return handleTrackStart(sender, args);
      case "trackstop":
        return handleTrackStop(sender);
      default:
        return false;
    }
  }

  private void giveTrackerTo(Player player) {
    player.getInventory().addItem(new ItemStack(Material.COMPASS));
  }
}
