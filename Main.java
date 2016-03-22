package com.darkania.cool;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import net.minecraft.server.v1_9_R1.PacketPlayInClientCommand;
import net.minecraft.server.v1_9_R1.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_9_R1.PacketPlayOutCamera;
import net.minecraft.server.v1_9_R1.PacketPlayOutChat;
import net.minecraft.server.v1_9_R1.IChatBaseComponent.ChatSerializer;

public class Main extends JavaPlugin implements Listener,CommandExecutor{
	static Team ghost;
	//dispatchCommand... title e.getPlayer().getName() title ["",{"text":"1"}] conteo timeado 5 4 3 2...
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		ghost = board.getTeam("Ghost");
		
		if(ghost == null){
			ghost = board.registerNewTeam("Ghost");
		}
	
		ghost.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
		ghost.setCanSeeFriendlyInvisibles(true);
	}
	
	@EventHandler
	public void entrando(PlayerJoinEvent e){
e.getPlayer().addAttachment(this, "bukkit.command.tps", true);
addPlayer(e.getPlayer());
ItemStack item = new ItemStack(Material.COMPASS);
ItemMeta meta = item.getItemMeta();
meta.setDisplayName("§aTracker");
item.setItemMeta(meta);

e.getPlayer().getInventory().setItem(4, item);


spawn(e.getPlayer().getLocation(),new ItemStack(Material.WOOD),"§4Holz §b25$");
sendActionBar(e.getPlayer(), "&eWellcome &4" + e.getPlayer().getName());
	}
	
	
	public void spawn(Location loc,ItemStack mat,String Text){
		loc.setY(loc.getY()+0.85- 1.3);
		ArmorStand armorstand = (ArmorStand)loc.getWorld().spawn(loc, ArmorStand.class);
		armorstand.setGravity(false);
		armorstand.setVisible(false);
		Item i = loc.getWorld().dropItem(loc, mat);
	    i.setPickupDelay(2147483647);
	    i.setCustomName(Text);
	    i.setCustomNameVisible(true);
	    armorstand.setPassenger(i);
	}
	
	
String prefix = "§7[§cSystem§7] §7";
public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
Player p = (Player)sender;
if(cmd.getName().equalsIgnoreCase("ping")) {
if(args.length == 0) {
p.sendMessage(prefix + "Tu ping es de §c" + getPing(p) + "§7ms");
}
}


if(label.equalsIgnoreCase("spectate")){
if(args.length == 1){
if(args[0].equalsIgnoreCase("leave")){
Player player = (Player)sender;
PacketPlayOutCamera camera = new PacketPlayOutCamera();
camera.a = player.getEntityId();
player.setGameMode(GameMode.SURVIVAL);
((CraftPlayer)player).getHandle().playerConnection.sendPacket(camera);
}else{
if(Bukkit.getPlayer(args[0]) != null){
Player target = (Player)Bukkit.getPlayer(args[0]);
PacketPlayOutCamera camera = new PacketPlayOutCamera();
camera.a = target.getEntityId();
Player player = (Player)sender;
player.setGameMode(GameMode.SPECTATOR);
((CraftPlayer)player).getHandle().playerConnection.sendPacket(camera);
}else{
sender.sendMessage(ChatColor.RED+"NO ESTA EN LINEA!");
}}
}
}


return false;
}
	
public int getPing(Player p) {
CraftPlayer pingc = (CraftPlayer) p;
EntityPlayer pinge = pingc.getHandle();
return pinge.ping;
}

@EventHandler
public void onInteract(PlayerInteractEvent e){
Player p = e.getPlayer();
if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
if(e.getClickedBlock().getType() == Material.DIRT){
removePlayer(p);
}}

if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
    try {
if (p.getInventory().getItemInMainHand().getType() == Material.COMPASS) {
for (Entity ent : p.getNearbyEntities(100D, 25D, 100D)) {
if (ent instanceof Player) {
Player near = (Player) ent;
p.setCompassTarget(near.getLocation());
p.sendMessage("§b" + near.getName() + " §8» §a" + ((int) p.getLocation().distance(near.getLocation())) + " Blöcke");
}
}
}
} catch (Exception ex) {
}
}
}


public static void addPlayer(Player player){
	ghost.addEntry(player.getName());
player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,15));
}

public static void removePlayer(Player player){
ghost.removeEntry(player.getName());
player.removePotionEffect(PotionEffectType.INVISIBILITY);
}

public void Respawn(final Player player,int Time){
Bukkit.getScheduler().runTaskLater(this, new Runnable() {
@Override
public void run() {
((CraftPlayer)player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
}
},Time);
}
   

@EventHandler
public void Death(EntityDeathEvent event){
if(event.getEntity().getKiller() instanceof Player){
Player player = (Player)event.getEntity().getKiller();
player.giveExp(10);
event.getEntity().getLocation().setY((event.getEntity().getLocation().getY() + -2D)-1.25);
ArmorStand Hologram = (ArmorStand)event.getEntity().getLocation().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.ARMOR_STAND);
Hologram.setCustomName("§a+10 Exp");
Hologram.setCustomNameVisible(true);
Hologram.setGravity(false);
Hologram.setVisible(false);
Bukkit.getScheduler().runTaskLater(this, new Runnable() {
@Override
public void run() {
Hologram.remove();
}
}, 30);
}
}

@EventHandler
public void onDeath(PlayerDeathEvent event){
    Player player = (Player)event.getEntity();
    Respawn(player,1);
}



ArrayList<String> Zoom = new ArrayList<String>();
@EventHandler
public void onZoom(PlayerToggleSneakEvent event){
if(event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.BOW)){
if(!Zoom.contains(event.getPlayer().getName())){
event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW,20000,5));
Zoom.add(event.getPlayer().getName());
}else{
event.getPlayer().removePotionEffect(PotionEffectType.SLOW);
Zoom.remove(event.getPlayer().getName());
}}
}



public void spawnGiantItem(Location location,ItemStack itemstack){
	Giant giant = location.getWorld().spawn(location,Giant.class);
	giant.getEquipment().setItemInMainHand(itemstack);
	giant.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,999));
	
	ArmorStand holder = location.getWorld().spawn(location,ArmorStand.class);
	holder.setGravity(false);
	holder.setVisible(false);
	
	holder.setPassenger(giant);
}

@EventHandler
public void onDamage(EntityDamageEvent event){
	if(event.getEntity() instanceof Giant){
		event.setCancelled(true);
	}
}


@EventHandler
public void onSneak(PlayerToggleSneakEvent event){
	if(event.isSneaking()&&event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_AXE)){
		spawnGiantItem(event.getPlayer().getLocation().clone().subtract(0,7,0), new ItemStack(Material.DIAMOND_SPADE));
	}
}

public static void sendActionBar(Player player, String Welcome)
{
String s = ChatColor.translateAlternateColorCodes('&', Welcome.replace("_", " "));
IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + s + "\"}");
PacketPlayOutChat bar = new PacketPlayOutChat(icbc, (byte)2);
((CraftPlayer)player).getHandle().playerConnection.sendPacket(bar);
}


}
