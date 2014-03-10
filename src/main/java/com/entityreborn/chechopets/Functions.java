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
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.functions.Exceptions;
import io.github.dsh105.echopet.api.EchoPetAPI;
import io.github.dsh105.echopet.entity.Pet;
import io.github.dsh105.echopet.entity.PetData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class Functions {

    public static abstract class PetFunc extends AbstractFunction {

        public Exceptions.ExceptionType[] thrown() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Version since() {
            return CHVersion.V3_3_1;
        }

        public String getName() {
            return getClass().getSimpleName();
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            Static.checkPlugin("EchoPet", t);

            return exec2(t, environment, args);
        }

        public abstract Construct exec2(Target t, Environment environment, Construct... args) throws ConfigRuntimeException;
    }

    @api
    public static class has_pet extends PetFunc {

        public Construct exec2(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();

            if (args.length != 0) {
                player = Static.GetPlayer(args[0], t);
            }

            return new CBoolean(EchoPetAPI.getAPI().hasPet((Player) player.getHandle()), t);
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "boolean {[player]} Check to see if a player has a pet.";
        }
    }
    
    @api
    public static class pet_info extends PetFunc {
        public CArray getPetArray(Pet pet, Target t) {
            CArray arr = CArray.GetAssociativeArray(t);
            
            arr.set("id", new CInt(pet.getCraftPet().getEntityId(), t), t);
            arr.set("owner", pet.getNameOfOwner(), t);
            arr.set("name", pet.getPetName(), t);
            arr.set("type", pet.getPetType().name(), t);
            arr.set("isownerriding", new CBoolean(pet.isOwnerRiding(), t), t);
            arr.set("isownerwearing", new CBoolean(pet.isHat(), t), t);
            arr.set("isrider", new CBoolean(pet.isRider(), t), t);
            
            CArray datarr = new CArray(t);
            for (PetData data : pet.getPetData()) {
                datarr.push(new CString(data.name(), t));
            }
            arr.set("data", datarr, t);
            
            CArray rider = new CArray(t);
            if (pet.getRider() != null) {
                rider = getPetArray(pet.getRider(), t);
            }
            arr.set("rider", rider, t);
            
            return arr;
        }
        
        public Construct exec2(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();

            if (args.length != 0) {
                if (args[0] instanceof CInt) {
                    int id = Static.getInt32(args[0], t);

                    for (Pet pet : EchoPetAPI.getAPI().getAllPets()) {
                        if (pet.getCraftPet().getEntityId() == id) {
                            return getPetArray(pet, t);
                        }
                    }
                    
                    throw new ConfigRuntimeException("No pet with that id found,"
                            + " or entity id is not from a pet!", 
                            Exceptions.ExceptionType.BadEntityException, t);
                } else {
                    player = Static.GetPlayer(args[0], t);
                }
            }
            
            Pet pet = Util.getPet(player, t);
            
            return getPetArray(pet, t);
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "array {[player]} Return various info about a player's pet.";
        }
    }
    
    @api
    public static class is_pet extends PetFunc {

        public Construct exec2(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            int id = Static.getInt32(args[0], t);

            for (Pet pet : EchoPetAPI.getAPI().getAllPets()) {
                if (pet.getCraftPet().getEntityId() == id) {
                    return new CBoolean(true, t);
                }
            }
            
            return new CBoolean(false, t);
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {id} Return whether a given ID is a pet or not.";
        }
    }

    @api
    public static class remove_pet extends PetFunc {

        public Construct exec2(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            boolean sendmessage = false;

            if (args.length == 1) {
                if (args[0] instanceof CBoolean) {
                    sendmessage = Static.getBoolean(args[0]);
                } else {
                    player = Static.GetPlayer(args[0], t);
                }
            } else if (args.length == 2) {
                player = Static.GetPlayer(args[0], t);
                sendmessage = Static.getBoolean(args[1]);
            }

            EchoPetAPI.getAPI().removePet((Player) player.getHandle(), sendmessage, true);

            return new CVoid(t);
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "void {[player], [sendmessage]} Remove a player's pet. "
                    + "Optionally, send the user a message. sendmessage defaults "
                    + "to false.";
        }
    }

    @api
    public static class teleport_pet extends PetFunc {

        public Construct exec2(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            MCLocation location = player.getLocation();

            if (args.length == 1) {
                if (args[0] instanceof CArray) {
                    // We gave it a location. Teleport our own pet to there.
                    location = ObjectGenerator.GetGenerator().location(
                            args[0], player.getWorld(), t);
                } else {
                    throw new ConfigRuntimeException("Expected a location for"
                            + " teleport_pet, got '" + args[0].val()
                            + "' instead.", Exceptions.ExceptionType.FormatException, t);
                }
            } else if (args.length == 2) {
                player = Static.GetPlayer(args[0], t);
                location = ObjectGenerator.GetGenerator().location(
                        args[1], player.getWorld(), t);
            }

            Pet pet = Util.getPet(player, t);
            EchoPetAPI.getAPI().teleportPet(pet, (Location) location.getHandle());

            return new CVoid(t);
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "void {[player], [location]} Teleport a pet to a given location."
                    + " If you omit the player, the player running the command will"
                    + " have his pet moved.";
        }
    }

    @api
    public static class set_ride_pet extends PetFunc {

        public Construct exec2(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            boolean riding = true;

            if (args.length == 1) {
                if (args[0] instanceof CBoolean) {
                    riding = Static.getBoolean(args[0]);
                } else {
                    throw new ConfigRuntimeException("Expected a boolean for"
                            + " set_ride_pet, got '" + args[0].val()
                            + "' instead.", Exceptions.ExceptionType.FormatException, t);
                }
            } else {
                player = Static.GetPlayer(args[0], t);
                riding = Static.getBoolean(args[1]);
            }

            Pet pet = Util.getPet(player, t);
            pet.ownerRidePet(riding);

            return new CVoid(t);
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], ride} Set whether a player is riding it's pet or not.";
        }
    }
    
    @api
    public static class show_pet_menu extends PetFunc {

        public Construct exec2(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            boolean message = false;

            if (args.length == 1) {
                if (args[0] instanceof CBoolean) {
                    message = Static.getBoolean(args[0]);
                } else {
                    player = Static.GetPlayer(args[0], t);
                }
            } else if (args.length == 2) {
                player = Static.GetPlayer(args[0], t);
                message = Static.getBoolean(args[1]);
            }
            
            EchoPetAPI.getAPI().openPetDataMenu((Player)player.getHandle(), message);

            return new CVoid(t);
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "void {[player], [showmessage]} Show the pet data menu.";
        }
    }
    
    @api
    public static class show_selector_menu extends PetFunc {

        public Construct exec2(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            boolean message = false;

            if (args.length == 1) {
                if (args[0] instanceof CBoolean) {
                    message = Static.getBoolean(args[0]);
                } else {
                    player = Static.GetPlayer(args[0], t);
                }
            } else if (args.length == 2) {
                player = Static.GetPlayer(args[0], t);
                message = Static.getBoolean(args[1]);
            }
            
            EchoPetAPI.getAPI().openPetSelector((Player)player.getHandle(), message);

            return new CVoid(t);
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "void {[player], [showmessage]} Show the pet selector menu.";
        }
    }
}
