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

package org.hit.node;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

import org.hit.communicator.NodeID;
import org.hit.key.Keyspace;

/**
 * Extends <code>PartitionTable</code> to support mapping as a DHT.
 * 
 * @author Balraja Subbiah
 */
public class HashTable<S extends Comparable<S>> 
    extends PartitionTable<S, BigInteger>
{
    /**
     * CTOR
     */
    public HashTable()
    {
        super();
    }

    /**
     * CTOR
     */
    public HashTable(String tableName, Keyspace<S, BigInteger> keyspace)
    {
        super(tableName, keyspace);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeID doLookup(BigInteger                  key,
                              TreeMap<BigInteger, NodeID> nodeMap)
    {
        NodeID holdingNode = nodeMap.get(key);
        if (holdingNode == null) {
            Map.Entry<BigInteger, NodeID> nextEntry = 
                nodeMap.ceilingEntry(key);
            
            if (nextEntry != null) {
                holdingNode = nextEntry.getValue();
            }
            else {
                holdingNode = nodeMap.firstEntry().getValue();
            }
        }
        return holdingNode;
    }
}
