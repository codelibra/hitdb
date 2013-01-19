/*
    Hit is a high speed transactional database for handling millions
    of updates with comfort and ease.

    Copyright (C) 2012  Balraja Subbiah

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

package org.hit.consensus.paxos;

import java.util.Set;

import org.hit.actors.EventBus;
import org.hit.communicator.NodeID;
import org.hit.consensus.ConsensusAcceptor;
import org.hit.consensus.ConsensusLeader;
import org.hit.consensus.ConsensusProtocolProvider;
import org.hit.consensus.UnitID;

import com.google.inject.Inject;

/**
 * Implements <code>ConsensusProtocolProvider</code> to return <code>
 * ConsensusLeader</code> and <code>ConsensusAcceptor</code> that
 * adheres to the paxos protocol.
 * 
 * @author Balraja Subbiah
 */
public class PaxosProvider implements ConsensusProtocolProvider
{
    private final EventBus myEventBus;
    
    /**
     * CTOR
     */
    @Inject
    public PaxosProvider(EventBus eventBus)
    {
        myEventBus = eventBus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConsensusAcceptor makeAcceptor(
                                          UnitID unitID,
                                          NodeID leader,
                                          NodeID ourNodeID)
    {
        return new PaxosAcceptor(unitID, leader, myEventBus, ourNodeID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConsensusLeader makeLeader(
                                      UnitID unitId,
                                      Set<NodeID> acceptors,
                                      EventBus eventBus,
                                      NodeID ourNodeID)
    {
        return new PaxosLeader(unitId, acceptors, eventBus, ourNodeID);
    }
}
