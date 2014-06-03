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
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
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
import io.github.dsh105.echopet.api.entity.pet.Pet;
import io.github.dsh105.echopet.api.event.PetInteractEvent;
import io.github.dsh105.echopet.nms.v1_7_R2.entity.CraftPet;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

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

    private boolean wasCancelled;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPetDamaged(EntityDamageEvent e) {
        wasCancelled = e.isCancelled();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPetDamaged2(EntityDamageEvent e) {
        e.setCancelled(wasCancelled);
        
        if (e.getEntity() instanceof CraftPet) {
            CraftPet cpet = (CraftPet)e.getEntity();
            Pet pet = cpet.getPet();
            
            Damage event = new Damage(pet, e);
            fireEvent("pet_damaged", event);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleEnter(VehicleEnterEvent e) {
        if (e.getVehicle() instanceof CraftPet) {
            CraftPet cpet = (CraftPet)e.getVehicle();
            Pet pet = cpet.getPet();
            
            Mount event = new Mount(pet, e);
            fireEvent("pet_mounted", event);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleExit(VehicleExitEvent e) {
        if (e.getVehicle() instanceof CraftPet) {
            CraftPet cpet = (CraftPet)e.getVehicle();
            Pet pet = cpet.getPet();
            
            Unmount event = new Unmount(pet, e);
            fireEvent("pet_unmounted", event);
        }
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

    public static class Mount implements BindableEvent {

        VehicleEnterEvent event;
        Pet pet;

        public Mount(Pet p, VehicleEnterEvent pie) {
            event = pie;
            pet = p;
        }

        public VehicleEnterEvent event() {
            return event;
        }

        public Object _GetObject() {
            return event;
        }
    }
    
    public static class Damage implements BindableEvent {

        EntityDamageEvent event;
        Pet pet;

        public Damage(Pet p, EntityDamageEvent pie) {
            event = pie;
            pet = p;
        }

        public EntityDamageEvent event() {
            return event;
        }

        public Object _GetObject() {
            return event;
        }
    }
    
    public static class Unmount implements BindableEvent {

        VehicleExitEvent event;
        Pet pet;

        public Unmount(Pet p, VehicleExitEvent pie) {
            event = pie;
            pet = p;
        }

        public VehicleExitEvent event() {
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
            Map<String, Construct> map = evaluate_helper(e);

            if (e instanceof Damage) {
                Damage event = (Damage) e;
                EntityDamageEvent damage = event.event();
                Pet pet = event.pet;

                map.put("owner", new CString(pet.getNameOfOwner(), Target.UNKNOWN));
                map.put("cause", new CString(damage.getCause().name(), Target.UNKNOWN));
                map.put("amount", new CDouble(damage.getDamage(), Target.UNKNOWN));
                map.put("wassecondpet", CBoolean.get(pet.isRider()));

                if (damage instanceof EntityDamageByEntityEvent) {
                    Entity damager = ((EntityDamageByEntityEvent) damage).getDamager();

                    if (damager instanceof Player) {
                        map.put("damager", new CString(((Player) damager).getName(), Target.UNKNOWN));
                    } else {
                        map.put("damager", new CInt(damager.getEntityId(), Target.UNKNOWN));
                    }
                    
                    if (damager instanceof Projectile) {
                        ProjectileSource shooter = ((Projectile) damager).getShooter();

                        if (shooter instanceof Player) {
                            map.put("shooter", new CString(((Player) shooter).getName(), Target.UNKNOWN));
                        } else if (shooter instanceof Entity) {
                            map.put("shooter", new CInt(((Entity) shooter).getEntityId(), Target.UNKNOWN));
                        } else if (shooter instanceof BlockProjectileSource) {
                            BlockProjectileSource source = (BlockProjectileSource) shooter;
                            MCLocation location = new BukkitMCLocation(source.getBlock().getLocation());
                            map.put("shooter", ObjectGenerator.GetGenerator().location(location));
                        }
                    }
                }
            }

            return map;
        }

        public Driver driver() {
            return Driver.EXTENSION;
        }

        public boolean modifyEvent(String key, Construct value, BindableEvent e) throws ConfigRuntimeException {
            if (e instanceof Damage) {
                Damage event = (Damage) e;

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

        public BindableEvent convert(CArray carray, Target target) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            Map<String, Construct> retn = evaluate_helper(e);

            if (e instanceof Interact) {
                Interact event = (Interact) e;
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
                Interact event = (Interact) e;
            }

            return false;
        }

        public Version since() {
            return CHVersion.V3_3_1;
        }

        public BindableEvent convert(CArray carray, Target target) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    @api
    public static class pet_mounted extends AbstractEvent {

        public String getName() {
            return "pet_mounted";
        }

        public String docs() {
            return ""; //TBA
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            return true;
        }

        public BindableEvent convert(CArray manualObject, Target t ) {
            return null;
        }

        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            Map<String, Construct> retn = evaluate_helper(e);

            if (e instanceof Mount) {
                Mount event = (Mount) e;

                retn.put("owner", new CString(event.pet.getNameOfOwner(), Target.UNKNOWN));
            }

            return retn;
        }

        public Driver driver() {
            return Driver.EXTENSION;
        }

        public boolean modifyEvent(String key, Construct value, BindableEvent e) throws ConfigRuntimeException {
            if (e instanceof Mount) {
                Mount event = (Mount) e;
            }

            return false;
        }

        public Version since() {
            return CHVersion.V3_3_1;
        }
        /*
        public BindableEvent convert(CArray carray, Target target) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        */
    }
    
    @api
    public static class pet_unmounted extends AbstractEvent {

        public String getName() {
            return "pet_unmounted";
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
            Map<String, Construct> retn = evaluate_helper(e);

            if (e instanceof Unmount) {
                Unmount event = (Unmount) e;

                retn.put("owner", new CString(event.pet.getNameOfOwner(), Target.UNKNOWN));
            }

            return retn;
        }

        public Driver driver() {
            return Driver.EXTENSION;
        }

        public boolean modifyEvent(String key, Construct value, BindableEvent e) throws ConfigRuntimeException {
            if (e instanceof Unmount) {
                Unmount event = (Unmount) e;
            }

            return false;
        }

        public Version since() {
            return CHVersion.V3_3_1;
        }

        public BindableEvent convert(CArray carray, Target target) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
