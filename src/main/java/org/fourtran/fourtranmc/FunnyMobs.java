package org.fourtran.fourtranmc;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class FunnyMobs implements Listener {

    private final FourtranMC plugin;

    public FunnyMobs(FourtranMC instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMobSpawn(EntitySpawnEvent event) {
        Entity e = event.getEntity();
        EntityType etype = event.getEntityType();

        e.setCustomNameVisible(true);
        switch (etype) {
            case SKELETON:
                e.customName(Component.text("TERF"));
                break;
            case ZOMBIE:
                e.customName(Component.text("CHASER"));
                break;
            case CREEPER:
                e.customName(Component.text("JOHN 50"));
                break;
            case ENDERMAN:
                e.customName(Component.text("HEIGHTHON"));
                break;
            case ENDER_DRAGON:
                e.customName(Component.text("BLANCHARD"));
            default:
                e.setCustomNameVisible(false);
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMobDeath(EntityDeathEvent event) {
        Entity e = event.getEntity();
        EntityType etype = event.getEntityType();

        ItemStack eOrT;
        if (new Random().nextBoolean()) {
            ItemStack estrogen = new ItemStack(Material.PINK_DYE);
            estrogen.editMeta((itemMeta -> {
                itemMeta.displayName(HRT.componentE);

                //List<String> lore = new ArrayList<>();
                //lore.add("§n1mg estrogen");
                //itemMeta.setLore(lore);
            }));

            eOrT = estrogen;
        } else {
            ItemStack testosterone = new ItemStack(Material.LIGHT_BLUE_DYE);
            testosterone.editMeta((itemMeta -> {
                itemMeta.displayName(HRT.componentT);

                //List<String> lore = new ArrayList<>();
                //lore.add("§n1mg testosterone");
                //itemMeta.setLore(lore);
            }));

            eOrT = testosterone;
        }

        event.getDrops().add(eOrT);
    }

}
