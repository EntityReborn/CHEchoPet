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

import com.dsh105.echopet.api.EchoPetAPI;
import com.dsh105.echopet.compat.api.entity.IPet;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import org.bukkit.entity.Player;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class Util {
    public static IPet getPet(Construct pname, Target t) {
        MCPlayer player = Static.GetPlayer(pname, t);
        
        return getPet(player, t);
    }
    
    public static IPet getPet(MCPlayer p, Target t) {
        IPet pet = EchoPetAPI.getAPI().getPet((Player) p.getHandle());
        
        if (pet == null) {
            throw new ConfigRuntimeException("That pet is not available. Either "
                    + "it is hidden or that player doesn't have a pet!", 
                    Exceptions.ExceptionType.BadEntityException, t);
        }
        
        return pet;
    }
    
    public static IPet getPet(int id, Target t) {
        for (IPet pet : EchoPetAPI.getAPI().getAllPets()) {
            if (pet.getCraftPet().getEntityId() == id) {
                return pet;
            }
        }
        
        return null;
    }
}
