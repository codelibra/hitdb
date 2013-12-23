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

package org.hit.example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.hit.db.model.Persistable;
import org.hit.db.model.mutations.MutationFactory;

/**
 * A type for defining the route serviced by an airline
 * 
 * @author Balraja Subbiah
 */
public class Route implements Persistable<Long>, Externalizable
{
    public static final String TABLE_NAME = "routes";
    
    private static final String ROUTE_ID = "route_id";
    
    private static final String SRC_ARPORT = "src_airport_id";
    
    private static final String DESTN_SIRPORT = "destn_airport_id";
    
    private static final String AIRLINE_ID = "airline_id";
    
    private static final String SHARED = "shared";
    
    private static final String NUM_STOPS = "num_stops";
    
    private long myRouteId;
    
    private long mySrcAirportId;
    
    private long myDestnAirportId;
    
    private long myAirlineId;
    
    private boolean myShared;
    
    private int myNumStops;
    
    /**
     * CTOR
     */
    public Route(long routeId, 
                 long airlineId, 
                 long srcAirportId, 
                 long destnAirportId,
                 boolean shared, 
                 int numStops)
    {
        super();
        myRouteId = routeId;
        mySrcAirportId = srcAirportId;
        myDestnAirportId = destnAirportId;
        myAirlineId = airlineId;
        myShared = shared;
        myNumStops = numStops;
    }

    /**
     * Returns the value of routeId
     */
    public long getRouteId()
    {
        return myRouteId;
    }

    /**
     * Setter for the routeId
     */
    public void setRouteId(long routeId)
    {
        myRouteId = routeId;
    }

    /**
     * Returns the value of srcAirportId
     */
    public long getSrcAirportId()
    {
        return mySrcAirportId;
    }

    /**
     * Setter for the srcAirportId
     */
    public void setSrcAirportId(long srcAirportId)
    {
        mySrcAirportId = srcAirportId;
    }

    /**
     * Returns the value of destnAirportId
     */
    public long getDestnAirportId()
    {
        return myDestnAirportId;
    }

    /**
     * Setter for the destnAirportId
     */
    public void setDestnAirportId(long destnAirportId)
    {
        myDestnAirportId = destnAirportId;
    }

    /**
     * Returns the value of airlineId
     */
    public long getAirlineId()
    {
        return myAirlineId;
    }

    /**
     * Setter for the airlineId
     */
    public void setAirlineId(long airlineId)
    {
        myAirlineId = airlineId;
    }

    /**
     * Returns the value of codeShare
     */
    public boolean isShared()
    {
        return myShared;
    }

    /**
     * Setter for the codeShare
     */
    public void setShared(boolean shared)
    {
        myShared = shared;
    }

    /**
     * Returns the value of numStops
     */
    public int getNumStops()
    {
        return myNumStops;
    }

    /**
     * Setter for the numStops
     */
    public void setNumStops(int numStops)
    {
        myNumStops = numStops;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFieldValue(String fieldName)
    {
        switch(fieldName) {
        case ROUTE_ID:
            return Long.valueOf(getRouteId());
        case SRC_ARPORT:
            return Long.valueOf(getSrcAirportId());
        case DESTN_SIRPORT:
            return Long.valueOf(getDestnAirportId());
        case AIRLINE_ID:
            return Long.valueOf(getAirlineId());
        case SHARED:
            return Boolean.valueOf(isShared());
        case NUM_STOPS:
            return Integer.valueOf(getNumStops());
        default:
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long primaryKey()
    {
        return Long.valueOf(myRouteId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
    }
    
    public static MutationFactory<Long, Route> getMutationFactory()
    {
        return new MutationFactory<>(TABLE_NAME);
    }
}
