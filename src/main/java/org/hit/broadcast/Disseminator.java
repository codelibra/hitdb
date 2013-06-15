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

package org.hit.broadcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hit.actors.Actor;
import org.hit.actors.ActorID;
import org.hit.actors.EventBus;
import org.hit.actors.EventBusException;
import org.hit.communicator.NodeID;
import org.hit.event.Event;
import org.hit.event.SendMessageEvent;
import org.hit.messages.NodeAdvertisement;
import org.hit.messages.ReconcillationRequest;
import org.hit.messages.ReconcilliationResponse;
import org.hit.util.LogFactory;

import com.google.inject.Inject;

/**
 * Defines the contract for a manager that's responsible for disseminating
 * <code>Information</code> known to it to all other nodes.
 * 
 * @author Balraja Subbiah
 */
public class Disseminator extends Actor
{
    private static final Logger LOG = 
        LogFactory.getInstance().getLogger(Disseminator.class);

    private class SendDigestTask implements Runnable
    {
        private final NodeID myParticipatingNode;
        
        private final NodeID myOwner;
        
        private final Digest myDigest;
        
        /**
         * CTOR
         */
        public SendDigestTask(NodeID participatingNode, 
                              NodeID owner,
                              Digest digest)
        {
            myParticipatingNode = participatingNode;
            myOwner = owner;
            myDigest = digest;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            try {
                getEventBus().publish(new SendMessageEvent(
                    Collections.singletonList(myParticipatingNode),
                    new ReconcillationRequest(myOwner, myDigest)));
            }
            catch (EventBusException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }            
        }
    }
    
    private class ApplyDigestTask implements Runnable
    {
        private final NodeID myDigestOwner;
        
        private final Digest myDigest;
        
        /**
         * CTOR
         */
        public ApplyDigestTask(NodeID owner, Digest digest)
        {
            myDigestOwner = owner;
            myDigest = digest;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            try {
                if (!myParticipants.contains(myDigestOwner)) {
                    myParticipants.add(myDigestOwner);
                }
                getEventBus().publish(new SendMessageEvent(
                    Collections.singletonList(myDigestOwner),
                    new ReconcilliationResponse(
                        myOwner,
                        myRepository.processDigest(myDigest))));
            }
            catch (EventBusException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }            
        }
    }

    
    private class ReconcillationResponseTask implements Runnable
    {
        private final List<Information> myResponse;
        
        /**
         * CTOR
         */
        public ReconcillationResponseTask(List<Information> response)
        {
            myResponse = response;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            myRepository.update(myResponse);
        }
    }
    
    private class SendDigestPeriodicTask implements Runnable
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            Digest digest = myRepository.makeDigest();
            for (NodeID nodeID : myParticipants) {
                getActorExecutor().submit(
                    new SendDigestTask(nodeID, myOwner, digest));
            }
        }
    }
    
    private class AddParticipantTask implements Runnable
    {
        private final NodeID myNodeID;

        /**
         * CTOR
         */
        public AddParticipantTask(NodeID nodeID)
        {
            super();
            myNodeID = nodeID;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            myParticipants.add(myNodeID);
        }
    }
    
    private final List<NodeID> myParticipants;
    
    private final NodeID myOwner;
    
    private final Repository myRepository;
    
    /**
     * CTOR
     */
    @Inject
    public Disseminator(EventBus eventBus, NodeID node)
    {
        super(eventBus, new ActorID(Disseminator.class.getSimpleName()));
        myParticipants = new ArrayList<>();
        myOwner = node;
        myRepository = new Repository();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processEvent(Event event)
    {
        if (event instanceof ReconcilliationResponse) {
            ReconcilliationResponse response = 
                (ReconcilliationResponse) event;
            getActorExecutor().submit(
                new ReconcillationResponseTask(
                    response.getInformationList()));
        }
        else if (event instanceof ReconcillationRequest) {
            ReconcillationRequest rr = (ReconcillationRequest) event;
            getActorExecutor().submit(new ApplyDigestTask(
                myOwner, rr.getDigest()));
        }
        else if (event instanceof NodeAdvertisement) {
            NodeAdvertisement advertisement = 
                (NodeAdvertisement) event;
            getActorExecutor().submit(
                new AddParticipantTask(advertisement.getNodeId()));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void start()
    {
        super.start();
        getActorExecutor().scheduleAtFixedRate(
            new SendDigestPeriodicTask(), 1, 1, TimeUnit.SECONDS);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerEvents()
    {
        getEventBus().registerForEvent(ReconcilliationResponse.class, 
                                       getActorID());
        
        getEventBus().registerForEvent(NodeAdvertisement.class,
                                       getActorID());
        
        getEventBus().registerForEvent(ReconcillationRequest.class,
                                       getActorID());
    }
}
