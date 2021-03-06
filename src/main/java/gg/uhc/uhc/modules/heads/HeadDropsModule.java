/*
 * Project: UHC
 * Class: gg.uhc.uhc.modules.heads.HeadDropsModule
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Graham Howden <graham_howden1 at yahoo.co.uk>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package gg.uhc.uhc.modules.heads;

import gg.uhc.uhc.modules.DisableableModule;
import gg.uhc.uhc.modules.ModuleRegistry;
import gg.uhc.uhc.modules.death.StandItemsMetadata;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public class HeadDropsModule extends DisableableModule implements Listener {

    protected static final Random RANDOM = new Random();
    protected static final String DROP_CHANCE_KEY = "drop chance";
    protected static final String ICON_NAME = "Head Drops";
    protected static final double DEFAULT_DROP_CHANCE = 100D;
    protected static final double PERCENT_MULTIPLIER = 100D;

    protected static final NumberFormat FORMATTER = NumberFormat.getNumberInstance();

    static {
        FORMATTER.setMinimumFractionDigits(0);
        FORMATTER.setMaximumFractionDigits(1);
    }

    protected final PlayerHeadProvider playerHeadProvider;
    protected double dropRate;

    public HeadDropsModule(PlayerHeadProvider playerHeadProvider) {
        setId("HeadDrops");

        this.playerHeadProvider = playerHeadProvider;
        this.iconName = ICON_NAME;
        this.icon.setType(Material.SKULL_ITEM);
        this.icon.setDurability(PlayerHeadProvider.PLAYER_HEAD_DATA);
        this.icon.setWeight(ModuleRegistry.CATEGORY_APPLES);
    }

    @Override
    protected boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public void initialize() throws InvalidConfigurationException {
        if (!config.contains(DROP_CHANCE_KEY)) {
            config.set(DROP_CHANCE_KEY, DEFAULT_DROP_CHANCE);
        }

        if (!config.isDouble(DROP_CHANCE_KEY) && !config.isInt(DROP_CHANCE_KEY)) {
            throw new InvalidConfigurationException(
                    "Invalid value at " + config.getCurrentPath() + ".drop chance (" + config.get(DROP_CHANCE_KEY)
            );
        }

        dropRate = config.getDouble(DROP_CHANCE_KEY) / PERCENT_MULTIPLIER;

        super.initialize();
    }

    public double getDropRate() {
        return dropRate;
    }

    public void setDropRate(double rate) {
        Preconditions.checkArgument(rate >= 0D && rate <= 1D);
        this.dropRate = rate;
        config.set(DROP_CHANCE_KEY, this.dropRate);
        saveConfig();
        rerender();
    }

    protected List<String> getEnabledLore() {
        return messages.evalTemplates(
                ENABLED_LORE_PATH,
                ImmutableMap.of("rate", FORMATTER.format(dropRate * PERCENT_MULTIPLIER))
        );
    }

    @EventHandler(priority = EventPriority.LOW)
    public void on(PlayerDeathEvent event) {
        if (!isEnabled() || RANDOM.nextDouble() < (1D - dropRate)) {
            // set to an empty map to avoid stale metadata problems
            event.getEntity().setMetadata(StandItemsMetadata.KEY, new StandItemsMetadata(plugin));
            return;
        }

        final Player player = event.getEntity();

        // create a head
        final ItemStack head = playerHeadProvider.getPlayerHeadItem(player);

        // add it to the drops
        event.getDrops().add(head);

        // add metadata for the armour stand module to put the helmet on the player and remove from drops
        final EnumMap<EquipmentSlot, ItemStack> standItems = Maps.newEnumMap(EquipmentSlot.class);
        standItems.put(EquipmentSlot.HEAD, head);

        player.setMetadata(StandItemsMetadata.KEY, new StandItemsMetadata(plugin, standItems));
    }
}
