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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.hit.communicator.Message;
import org.hit.communicator.NodeID;
import org.hit.db.model.DBOperation;

/**
 * The reply message for a successful <code>DBOperation</code>
 *
 * @author Balraja Subbiah
 */
public class DBOperationSuccessMessage extends Message
{
    private DBOperation myAppliedDBOperation;

    private Object myResult;

    /**
     * CTOR
     */
    public DBOperationSuccessMessage()
    {
        this(null, null, null);
    }

    /**
     * CTOR
     */
    public DBOperationSuccessMessage(NodeID      serverID,
                                     DBOperation appliedDBOperation,
                                     Object      result)
    {
        super(serverID);
        myAppliedDBOperation = appliedDBOperation;
        myResult = result;
    }

    /**
     * Returns the value of appliedDBOperation
     */
    public DBOperation getAppliedDBOperation()
    {
        return myAppliedDBOperation;
    }

    /**
     * Returns the value of result
     */
    public Object getResult()
    {
        return myResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        myAppliedDBOperation = (DBOperation) in.readObject();
        boolean hasResult = in.readBoolean();
        if (hasResult) {
            myResult = in.readObject();
        }
        else {
            myResult = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        super.writeExternal(out);
        out.writeObject(myAppliedDBOperation);
        if (myResult != null) {
            out.writeBoolean(true);
            out.writeObject(myResult);
        }
        else {
            out.writeBoolean(false);
        }
    }
}
