/*
    Hit is a high speed transactional database for handling millions
    of updates with comfort and ease. 

    Copyright (C) 2013  Balraja Subbiah

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.hit.event;

import java.util.Collection;

import org.hit.gossip.Gossip;

/**
 * Defines an event for updating the information to be spread via gossip
 * 
 * @author Balraja Subbiah
 */
public class GossipUpdateEvent implements Event
{
    private final Collection<Gossip> myGossip;

    /**
     * CTOR
     */
    public GossipUpdateEvent(Collection<Gossip> gossip)
    {
        super();
        myGossip = gossip;
    }

    /**
     * Returns the value of gossip
     */
    public Collection<Gossip> getGossip()
    {
        return myGossip;
    } 
}
