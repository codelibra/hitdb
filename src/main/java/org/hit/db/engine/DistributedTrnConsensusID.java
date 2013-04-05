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

package org.hit.db.engine;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import org.hit.communicator.NodeID;
import org.hit.consensus.UnitID;

/**
 * Defines the class that can be used for uniquely identifying the 
 * consensus.
 */
class DistributedTrnConsensusID implements UnitID
{
    private String myConsensusID;
    
    /**
     * CTOR
     */
    public DistributedTrnConsensusID()
    {
        myConsensusID = null;
    }

    /**
     * CTOR
     */
    public DistributedTrnConsensusID(NodeID clientId)
    {
        myConsensusID = 
            clientId.toString() + new Date().toString();
    }

    /**
     * Returns the value of consensusID
     */
    public String getConsensusID()
    {
        return myConsensusID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((myConsensusID == null) ? 0 : myConsensusID.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DistributedTrnConsensusID other = (DistributedTrnConsensusID) obj;
        if (myConsensusID == null) {
            if (other.myConsensusID != null)
                return false;
        }
        else if (!myConsensusID.equals(other.myConsensusID))
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "TransactionConsensusID [myConsensusID=" + myConsensusID
               + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(myConsensusID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        myConsensusID = (String) in.readObject();
    }
}