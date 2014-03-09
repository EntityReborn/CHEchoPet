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

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import io.github.dsh105.echopet.api.event.PetDamageEvent;
import io.github.dsh105.echopet.api.event.PetInteractEvent;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class Events implements Listener {
    public void fireEvent(final String name, final BindableEvent evt) {
        EventUtils.TriggerListener(Driver.EXTENSION, name, evt);
    }
    
    @EventHandler
    public void onPetInteract(PetInteractEvent pie) {
        Interact event = new Interact(pie);
        fireEvent("pet_interact", event);
    }
    
    @EventHandler
    public void onPetDamaged(PetDamageEvent pde) {
        Damage event = new Damage(pde);
        fireEvent("pet_damaged", event);
    }
    
    public static class Interact implements BindableEvent {
        PetInteractEvent event;
        
        public Interact(PetInteractEvent pie) {
            event = pie;
        }
        
        public PetInteractEvent event() {
            return event;
        }
        
        public Object _GetObject() {
            return event;
        }
    }
    
    public static class Damage implements BindableEvent {
        PetDamageEvent event;
        
        public Damage(PetDamageEvent pie) {
            event = pie;
        }
        
        public PetDamageEvent event() {
            return event;
        }
        
        public Object _GetObject() {
            return event;
        }
    }
    
    @api
    public static class pet_damaged extends AbstractEvent {

        public String getName() {
            return "pet_damaged";
        }

        public String docs() {
            return ""; //TBA
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            return true;
        }

        public BindableEvent convert(CArray manualObject) {
            return null;
        }

        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            Map<String, Construct> map = new HashMap<String, Construct>();
            
            if (e instanceof Damage) {
                Damage event = (Damage)e;
                PetDamageEvent damage = event.event();
                
                map.put("owner", new CString(damage.getPet().getNameOfOwner(), Target.UNKNOWN));
                map.put("cause", new CString(damage.getDamageCause().name(), Target.UNKNOWN));
                map.put("amount", new CDouble(damage.getDamage(), Target.UNKNOWN));
                map.put("wassecondpet", new CBoolean(damage.getPet().isRider(), Target.UNKNOWN));
            }
            
            return map;
        }

        public Driver driver() {
            return Driver.EXTENSION;
        }

        public boolean modifyEvent(String key, Construct value, BindableEvent e) throws ConfigRuntimeException {
            if (e instanceof Damage) {
                Damage event = (Damage)e; 
                
                if (key.equalsIgnoreCase("amount")) {
                    double damage = Static.getDouble(value, Target.UNKNOWN);
                    event.event().setDamage(damage);
                }
            }
            
            return false;
        }

        public Version since() {
            return CHVersion.V3_3_1;
        }
    }
    
    @api
    public static class pet_interact extends AbstractEvent {

        public String getName() {
            return "pet_interact";
        }

        public String docs() {
            return ""; //TBA
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            return true;
        }

        public BindableEvent convert(CArray manualObject) {
            return null;
        }

        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            Map<String, Construct> retn = new HashMap<String, Construct>();
            
            if (e instanceof Interact) {
                Interact event = (Interact)e;
                PetInteractEvent interact = event.event();
                
                retn.put("whointeracted", new CString(interact.getPlayer().getName(), Target.UNKNOWN));
                retn.put("action", new CString(interact.getAction().name(), Target.UNKNOWN));
                retn.put("owner", new CString(interact.getPet().getNameOfOwner(), Target.UNKNOWN));
            }
            
            return retn;
        }

        public Driver driver() {
            return Driver.EXTENSION;
        }

        public boolean modifyEvent(String key, Construct value, BindableEvent e) throws ConfigRuntimeException {
            if (e instanceof Interact) {
                Interact event = (Interact)e; 
            }
            
            return false;
        }

        public Version since() {
            return CHVersion.V3_3_1;
        }
    }
}
