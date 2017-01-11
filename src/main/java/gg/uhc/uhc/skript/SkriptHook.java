/*
 * Project: UHC
 * Class: gg.uhc.uhc.modules.SkriptHook
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

package gg.uhc.uhc.skript;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import gg.uhc.uhc.UHC;
import gg.uhc.uhc.modules.DisableableModule;
import gg.uhc.uhc.modules.Module;
import gg.uhc.uhc.modules.ModuleRegistry;
import gg.uhc.uhc.modules.events.ModuleChangeStatusEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import java.util.Iterator;
import java.util.logging.Logger;

public final class SkriptHook {
    private static ModuleRegistry registry;

    private SkriptHook() {}

    static Function<String, Module> findModuleByNameFunction() {
        return new Function<String, Module>() {
            @Override
            public Module apply(String input) {
                return SkriptHook.registry.get(input).orNull();
            }
        };
    }

    static Iterable<DisableableModule> getAllModules() {
        return Iterables.transform(
            Iterables.filter(
                registry.getModules(),
                Predicates.instanceOf(DisableableModule.class)
            ),
            new Function<Module, DisableableModule>() {
                @Override
                public DisableableModule apply(Module input) {
                    return (DisableableModule) input;
                }
            }
        );
    }

    public static void register(final UHC uhc) {
        registry = uhc.getRegistry();

        final SkriptAddon addon = Skript.registerAddon(uhc);
        final Logger logger = uhc.getLogger();
        logger.info("Registered skript addon " + addon);

        Classes.registerClass(
            new ClassInfo<>(DisableableModule.class, "module")
                    .user("modules?")
                    .name("UHC Module")
                    .usage("")
                    .examples("")
                    .defaultExpression(new EventValueExpression<>(DisableableModule.class))
        );
        logger.info("Regiseted module class");

        logger.info("Registered event: " + EvtModuleStatusChange.hook());

        ExprDisableableModule.hook();
        logger.info("Registered module name expression");

        ExprDisableableModuleStatus.hook();
        logger.info("Registered module status expression");

        ExprDisableableModuleName.hook();
        logger.info("Registered module name expression");

        CondDisableableModuleEnabled.hook();
        logger.info("Registered enabled condition");

        EffDisableableModuleStatus.hook();
        logger.info("Registered module status efffect");

        EventValues.registerEventValue(
            ModuleChangeStatusEvent.class,
            DisableableModule.class,
            new Getter<DisableableModule, ModuleChangeStatusEvent>() {
                @Override
                public DisableableModule get(ModuleChangeStatusEvent arg) {
                    return arg.getModule();
                }
            },
            0
        );
        logger.info("Regiseterd event value for module");
    }
}