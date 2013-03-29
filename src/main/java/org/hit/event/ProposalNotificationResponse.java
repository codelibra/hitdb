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

/**
 * Defines an event that's specifies whether the proposal received via
 * consensus protocol is acceptable
 * 
 * @author Balraja Subbiah
 */
public class ProposalNotificationResponse implements Event
{
    private final ProposalNotificationEvent myProposalNotification;

    private final boolean myCanAccept;

    /**
     * CTOR
     */
    public ProposalNotificationResponse(
                                        ProposalNotificationEvent proposalNotification,
                                        boolean canAccept)
    {
        super();
        myProposalNotification = proposalNotification;
        myCanAccept = canAccept;
    }

    /**
     * Returns the value of canAccept
     */
    public boolean canAccept()
    {
        return myCanAccept;
    }

    /**
     * Returns the value of proposalNotification
     */
    public ProposalNotificationEvent getProposalNotification()
    {
        return myProposalNotification;
    }
}
