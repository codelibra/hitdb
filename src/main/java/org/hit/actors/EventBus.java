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

package org.hit.actors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hit.concurrent.epq.EventPassingQueue;
import org.hit.concurrent.epq.WaitStrategy;
import org.hit.event.Event;

/**
 * An event bus used for communication between various actors of the
 * system. Primarily an actor can register for an event. When an actor
 * owns an event, instances of that event type when published on the
 * event bus will be delivered to that actor. Alternatively we
 * can directly publish events to the actors. Each actor owns an
 * {@linkplain EventPassingQueue} and all the events delivered to that
 * actor will be published to the actor's queue.
 * 
 * @author Balraja Subbiah
 */
public class EventBus
{
    private final Map<ActorID, EventPassingQueue> myActorToEPQ;
    
    private final Map<Class<? extends Event>, List<ActorID>> myEvent2Actors;

    /**
     * CTOR
     */
    public EventBus()
    {
        myActorToEPQ = new ConcurrentHashMap<>();
        myEvent2Actors = new ConcurrentHashMap<>();
    }
    
    /**
     * Consumes <code>Event</code>s delivered to this component.
     */
    public Event consume(ActorID actorID) throws EventBusException
    {
        EventPassingQueue epq = myActorToEPQ.get(actorID);
        if (epq == null) {
            throw new EventBusException(actorID + " not registered");
        }
        return epq.consume();
    }
    
    /**
     * Publishes the given <code>Event</code> to the actor.
     */
    private void publish(ActorID actorID, Event event)
        throws EventBusException
    {
        EventPassingQueue epq = myActorToEPQ.get(actorID);
        if (epq == null) {
            throw new EventBusException(actorID + " not registered");
        }
        epq.publish(event);
    }
    
    /**
     * Publishes the <code>Event</code> to interested actors.
     */
    public void publish(Event event) throws EventBusException
    {
        List<ActorID> actors = myEvent2Actors.get(event.getClass());
        if (actors != null) {
            for (ActorID actor : actors) {
                publish(actor, event);
            }
        }
    }
    
    /**
     * Registers a component for receiving <code>Event</code>s sent to it.
     */
    public void register(ActorID actorID, int size)
    {
        if (!myActorToEPQ.containsKey(actorID)) {
            myActorToEPQ.put(actorID,
                             new EventPassingQueue(size,
                                                   WaitStrategy.SLEEP));
        }
    }
    
    /**
     * Registers a given component with the event bus to receive all events
     * of that type.
     */
    public void registerForEvent(Class<? extends Event> eventType,
                                 ActorID actorID)
    {
        List<ActorID> actors = myEvent2Actors.get(eventType);
        if (actors == null) {
            actors = new CopyOnWriteArrayList<>();
            myEvent2Actors.put(eventType, actors);
        }
        actors.add(actorID);
    }
}
