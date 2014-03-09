/*
 * The MIT License
 *
 * Copyright 2014 Jason Unger <entityreborn@gmail.com>.
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

package com.entityreborn.chechopets;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

@MSExtension("CHEchoPets")
public class LifeCycle extends AbstractExtension {
    Events eventListener;
    
    @Override
    public void onStartup() {
        System.out.println(getName() + " v" + getVersion() + " loaded.");

        if (!EPLoaded()) {
            System.out.println("Oops, EchoPet isn't installed!");
        } else {
            eventListener = new Events();
            CommandHelperPlugin plugin = CommandHelperPlugin.self;
            
            Bukkit.getPluginManager().registerEvents(eventListener, plugin);
        }
    }

    @Override
    public void onShutdown() {
        System.out.println(getName() + " v" + getVersion() + " unloaded.");
        
        if (eventListener != null) {
            HandlerList.unregisterAll(eventListener);
            eventListener = null;
        }
    }

    public Version getVersion() {
        return new SimpleVersion(0, 0, 1);
    }

    public static boolean EPLoaded() {
        return Static.getServer().getPluginManager().getPlugin("EchoPet") != null;
    }
}
