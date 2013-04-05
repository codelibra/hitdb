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

import org.hit.consensus.ConsensusAcceptor;
import org.hit.consensus.Proposal;

/**
 * Defines the contract for an {@link Event} that's published by the 
 * {@link ConsensusAcceptor} to notify other components in the system 
 * about the proposal it has received.
 * 
 * @author Balraja Subbiah
 */
public class ProposalNotificationEvent implements Event
{
    private final Proposal myProposal;
    
    private final boolean myCommitNotification;

    /**
     * CTOR
     */
    public ProposalNotificationEvent(Proposal proposal)
    {
        this(proposal, false);
    }
    
    /**
     * CTOR
     */
    public ProposalNotificationEvent(Proposal proposal,
                                     boolean commitNotification)
    {
        super();
        myProposal = proposal;
        myCommitNotification = commitNotification;
    }

    /**
     * Returns the value of commitNotification
     */
    public boolean isCommitNotification()
    {
        return myCommitNotification;
    }

    /**
     * Returns the value of proposal
     */
    public Proposal getProposal()
    {
        return myProposal;
    }
}