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
package org.hit.server;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import java.util.logging.Logger;

import org.hit.actors.Actor;
import org.hit.actors.ActorID;
import org.hit.actors.EventBus;
import org.hit.communicator.CommunicatingActor;
import org.hit.consensus.ConsensusManager;
import org.hit.consensus.UnitID;
import org.hit.db.engine.DBEngine;
import org.hit.di.HitServerModule;
import org.hit.event.ChangeAcceptorToLeaderEvent;
import org.hit.event.CreateRaftAcceptorEvent;
import org.hit.event.CreateRaftLeaderEvent;
import org.hit.event.Event;
import org.hit.event.GroupReadyEvent;
import org.hit.event.JoinGroupEvent;
import org.hit.event.LeaderChangeEvent;
import org.hit.gms.GroupManager;
import org.hit.gms.GroupID;
import org.hit.gossip.Disseminator;
import org.hit.util.LogFactory;

/**
 * Extends {@link Actor} to support initializing the server.
 * 
 * @author Balraja Subbiah
 */
public class ServerComponentManager extends Actor
{
    private static final Logger LOG = LogFactory.getInstance()
                                                .getLogger(ServerComponentManager.class);
    
    private final CommunicatingActor myCommunicatingActor;

    private final ConsensusManager   myConsensusManager;

    private final DBEngine           myDBEngine;

    private final Disseminator       myDisseminator;
    
    private final GroupManager       myGroupManager;
    
    private final GroupID            myServerGroupID;
    
    private final GroupID            myReplicationGroupID;
    
    private final GroupID            myReplicationSlaveID;
    
    private final ServerConfig       myServerConfig;
    
    private final UnitID             myReplicationUnitID;
    
    private final UnitID             myReplicationSlaveUnitID;
    
    private GroupReadyEvent myReplicatedGroupReadyEvent;
    
    private GroupReadyEvent myReplicatedSlaveGroupReadyEvent;
    
    /**
     * CTOR
     */
    @Inject
    public ServerComponentManager(
        EventBus eventBus,
        @Named("ServerGroupID") 
        GroupID serverGroupID,
        @Named("ReplicationGroupID") 
        GroupID replicationGroupID,
        @Named("ReplicationSlaveGroupID")
        GroupID replicationSlave,
        ServerConfig config,
        @Named("ReplicationUnitID")
        UnitID replicationUnitID,
        @Named("ReplicationSlaveUnitID")
        UnitID replicationSlaveUnitID)
    {
        super(eventBus, new ActorID(ServerComponentManager.class.getSimpleName()));
        Injector injector = Guice.createInjector(new HitServerModule());
        myCommunicatingActor = injector.getInstance(CommunicatingActor.class);
        myConsensusManager = injector.getInstance(ConsensusManager.class);
        myDisseminator = injector.getInstance(Disseminator.class);
        myDBEngine      = injector.getInstance(DBEngine.class);
        myGroupManager = injector.getInstance(GroupManager.class);
        myServerGroupID = serverGroupID;
        myReplicationGroupID = replicationGroupID;
        myReplicationSlaveID = replicationSlave;
        myServerConfig = config;
        myReplicationUnitID = replicationUnitID;
        myReplicationSlaveUnitID = replicationSlaveUnitID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processEvent(Event event)
    {
        if (event instanceof GroupReadyEvent) {
            GroupReadyEvent grEvent = (GroupReadyEvent) event;
            if (grEvent.getGroupID().equals(myReplicationGroupID)) {
                myReplicatedGroupReadyEvent = grEvent;
                
                LOG.info("Sending request to join group " 
                         + myReplicationSlaveID);
                
                getEventBus().publish(
                    new JoinGroupEvent(myReplicationSlaveID, 
                                       false,
                                       myServerConfig.getReplicationFactor()));
            }
            else if (grEvent.getGroupID().equals(myReplicationSlaveID)) {
                myReplicatedSlaveGroupReadyEvent = grEvent;
                LOG.info("Sending request to join group " 
                    + myServerGroupID);
                
                getEventBus().publish(
                    new JoinGroupEvent(myServerGroupID, 
                                       myServerConfig.isMaster(),
                                       myServerConfig.getInitialServerCount()));
            }
            else if (grEvent.getGroupID().equals(myServerGroupID)) {
                myDBEngine.init(grEvent.getLeader());
                myCommunicatingActor.start();
                LOG.info("Communicator started");
                myConsensusManager.start();
                LOG.info("Consensus manager started");
                myDisseminator.start();
                LOG.info("Gossiper started");
                myDBEngine.start();
                LOG.info("Database engine started");
                
                getEventBus().publish(
                    new CreateRaftLeaderEvent(
                        myReplicationUnitID,
                        myReplicatedGroupReadyEvent.getFollowers(),
                        myReplicatedGroupReadyEvent.getTerm()));
                
                getEventBus().publish(
                    new CreateRaftAcceptorEvent(
                        myReplicationSlaveUnitID,
                        myReplicatedSlaveGroupReadyEvent.getLeader(),
                        myReplicatedSlaveGroupReadyEvent.getTerm()));
            }
            else if (event instanceof LeaderChangeEvent) {
                LeaderChangeEvent lce = (LeaderChangeEvent) event;
                if (   lce.getGroupID().equals(myReplicationSlaveID)
                    && myReplicatedGroupReadyEvent.getLeader()
                                                  .equals(lce.getOldLeader())) 
                {
                    getEventBus().publish(
                        new ChangeAcceptorToLeaderEvent(
                            new CreateRaftLeaderEvent(
                                myReplicationSlaveUnitID,
                                lce.getFollowers(),
                                lce.getTerm())));
                }
            }
            
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerEvents()
    {
        getEventBus().registerForEvent(GroupReadyEvent.class, 
                                       getActorID());
        
        getEventBus().registerForEvent(LeaderChangeEvent.class, 
                                       getActorID());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void start()
    {
        super.start();
        LOG.info("Starting Group Manager");
        myGroupManager.start();
        
        LOG.info("Sending request to join group " + myReplicationGroupID);
        getEventBus().publish(
            new JoinGroupEvent(myServerGroupID, 
                               true,
                               myServerConfig.getReplicationFactor()));
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void stop()
    {
        super.stop();
        myCommunicatingActor.stop();
        myConsensusManager.stop();
        myDisseminator.stop();
        myDBEngine.stop();
        myDisseminator.stop();
        myGroupManager.stop();
    }

}
