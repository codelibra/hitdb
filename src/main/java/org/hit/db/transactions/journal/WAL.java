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

package org.hit.db.transactions.journal;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hit.db.model.Mutation;
import org.hit.db.transactions.TransactionID;
import org.hit.db.transactions.WriteTransaction;
import org.hit.util.LogFactory;

import com.google.inject.Inject;

/**
 * Defines the contract for the write ahead logs to which mutations are
 * pesisted before they are committed. It stores
 * {@link WALConfig#getTransactionsPerFile() } number of mutations in a file.
 * Each record is stored as Double.NaN,record_size_as_int,serialized_format.
 *
 * @author Balraja Subbiah
 */
public class WAL
{
    /**
     * Type for capturing the necessary information to be written into a
     * write ahead log.
     */
    public static class WALRecord implements Externalizable
    {
        private Mutation myMutation;

        private TransactionID myTransactionID;

        /**
         * CTOR
         */
        public WALRecord()
        {
            this(null, null);
        }

        /**
         * CTOR
         */
        public WALRecord(TransactionID transactionID, Mutation mutation)
        {
            super();
            myTransactionID = transactionID;
            myMutation = mutation;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException
        {
            myTransactionID = (TransactionID) in.readObject();
            myMutation = (Mutation) in.readObject();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeExternal(ObjectOutput out) throws IOException
        {
            out.writeObject(myTransactionID);
            out.writeObject(myMutation);
        }
    }

    private static final Logger LOG = LogFactory.getInstance()
                                                .getLogger(WAL.class);

    private static final double RECORD_DELIMITER = Double.NaN;

    private static final String TRANSACTION_LOG_SUFFIX = ".transactionLog";

    private final WALConfig myConfig;

    private final FileSystemFacacde myFacacde;

    private final Lock myLock;

    private DataOutputStream myLogStream;

    private final AtomicInteger myPersistedTransactionCount;

    /**
     * CTOR
     */
    @Inject
    public WAL(FileSystemFacacde facade, WALConfig config)
    {
        myFacacde = facade;
        myLock = new ReentrantLock();
        myConfig = config;
        myPersistedTransactionCount = new AtomicInteger(0);
        myFacacde.makeDirectory(myConfig.getBaseDirectoryPath());
        myLogStream =
            myFacacde.createFile(
                makeFileName(myPersistedTransactionCount.get()),
                false);
    }

    /**
     * Perists the given mutation to a file system. So that it can be
     * replayed in the future.
     *
     * @param transaction The transaction to be written to the journal.
     */
    public void addTransaction(WriteTransaction transaction)
    {
        try {
            myLock.lock();
            int newTransactionID =
                myPersistedTransactionCount.incrementAndGet();
            if (newTransactionID % myConfig.getTransactionsPerFile() == 0) {
                myLogStream.flush();
                myLogStream.close();
                myLogStream =
                    myFacacde.createFile(
                         makeFileName(newTransactionID), false);
            }

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream ooStream = new ObjectOutputStream(byteStream);
            ooStream.writeObject(
                new WALRecord(transaction.getTransactionID(),
                              transaction.getMutation()));

            myLogStream.writeDouble(RECORD_DELIMITER);
            myLogStream.writeInt(byteStream.size());
            myLogStream.write(byteStream.toByteArray());
        }
        catch (IOException e) {
            LOG.log(Level.SEVERE,
                    "Exception when persisting a transaction to WAL",
                    e);

        }
        finally {
            myLock.unlock();
        }
    }

    private String makeFileName(int transactionCount)
    {
        return myConfig.getBaseDirectoryPath()
               + File.separator
               + Integer.toString(transactionCount)
               + TRANSACTION_LOG_SUFFIX;
    }
}
