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

package org.hit.messages;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.hit.communicator.Message;
import org.hit.communicator.NodeID;
import org.hit.db.model.DatabaseException;
import org.hit.pool.Poolable;

/**
 * Message for denoting that <code>DBOperationFailed</code> on the database.
 *
 * @author Balraja Subbiah
 */
public class DBOperationFailureMessage extends Message implements Poolable
{
    private Throwable myException;

    private String myMessage;

    private long mySequenceNumber;

    /**
     * CTOR
     */
    public DBOperationFailureMessage()
    {
        myException = null;
        mySequenceNumber = -1L;
        myMessage = null;
    }

    /**
     * A helper method to initialize the object.
     */
    public DBOperationFailureMessage initialize(
        NodeID      nodeID,
        long        sequenceNumber,
        String      message)
    {
        return initialize(
            nodeID, sequenceNumber, message, new DatabaseException(message));
    }

    /**
     * A helper method to initialize the object.
     */
    public DBOperationFailureMessage initialize(
        NodeID      nodeID,
        long        sequenceNumber,
        String       message,
        Throwable    exception)
    {
        setSenderID(nodeID);
        myMessage = message;
        mySequenceNumber = sequenceNumber;
        myException = exception;
        return this;
    }

    /**
     * Returns the value of exception
     */
    public Throwable getException()
    {
        return myException;
    }

    /**
     * Returns the value of message
     */
    public String getMessage()
    {
        return myMessage;
    }

    /**
     * Returns the value of dBOperation
     */
    public long getSequenceNumber()
    {
        return mySequenceNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(ObjectInput in)
        throws IOException,ClassNotFoundException
    {
        super.readExternal(in);
        mySequenceNumber = in.readLong();
        myMessage     = in.readUTF();
        myException   = (Throwable) in.readObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        super.writeExternal(out);
        out.writeLong(mySequenceNumber);
        out.writeUTF(myMessage);
        out.writeObject(myException);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void free()
    {
        myException = null;
        mySequenceNumber = -1L;
        myMessage = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize()
    {
    }
}
