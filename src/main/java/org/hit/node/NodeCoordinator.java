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

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hit.actors.Actor;
import org.hit.actors.ActorID;
import org.hit.actors.EventBus;
import org.hit.actors.EventBusException;
import org.hit.communicator.NodeID;
import org.hit.event.DBStatEvent;
import org.hit.event.Event;
import org.hit.event.GossipNotificationEvent;
import org.hit.event.SendMessageEvent;
import org.hit.gossip.Gossip;
import org.hit.messages.Heartbeat;
import org.hit.util.LogFactory;
import org.hit.util.NamedThreadFactory;

import com.google.inject.Inject;

/**
 * Defines the <code>NodeCoordinator</code> that acts as client to the 
 * <code>NodeMonitor</code> running on the master.
 * 
 * @author Balraja Subbiah
 */
public class NodeCoordinator extends Actor
{
    private static final Logger LOG = 
        LogFactory.getInstance().getLogger(NodeCoordinator.class);
                               
    private final Map<String, PartitionTable<?, ?>> myPartitions;
    
    private final TObjectLongMap<String> myTableRowCountMap;
    
    private final ScheduledExecutorService myScheduler;
    
    private NodeID myMaster;
    
    private NodeConfig myConfig;
    
    private class PublishHeartbeatTask implements Runnable
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            try {
                if (myMaster != null) {
                    getEventBus().publish(
                        new SendMessageEvent(
                            Collections.singletonList(myMaster),
                            new Heartbeat(myTableRowCountMap)));
                }
            }
            catch (EventBusException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
    
    private class ApplyDBStatsTask implements Runnable
    {
        private final DBStatEvent myDBStat;
        
        /**
         * CTOR
         */
        public ApplyDBStatsTask(DBStatEvent stat)
        {
            myDBStat = stat;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            myTableRowCountMap.putAll(myDBStat.getTableToRowCountMap());
        }
    }
    
    /**
     * CTOR
     */
    @Inject
    public NodeCoordinator(EventBus eventBus, NodeConfig config)
    {
        super(eventBus, new ActorID(NodeCoordinator.class.getSimpleName()));
        myPartitions = new HashMap<>();
        myTableRowCountMap = new TObjectLongHashMap<>();
        myConfig = config;
        myScheduler = 
            Executors.newScheduledThreadPool(
                1,
                new NamedThreadFactory("NodeCoordinatorSchedule"));
    }
    
    /**
     * Setter for the master
     */
    public void setMaster(NodeID master)
    {
        myMaster = master;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processEvent(Event event)
    {
        if (event instanceof GossipNotificationEvent) {
            GossipNotificationEvent gne = (GossipNotificationEvent) event;
            for (Gossip gossip : gne.getGossip()) {
                if (gossip instanceof PartitionTable) {
                    myPartitions.put((String)              gossip.getKey(), 
                                     (PartitionTable<?,?>) gossip);
                }
            }
        }
        else if (event instanceof DBStatEvent) {
            DBStatEvent stat = (DBStatEvent) event;
            myScheduler.submit(new ApplyDBStatsTask(stat));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerEvents()
    {
        getEventBus().registerForEvent(
            GossipNotificationEvent.class, getActorID());
        
        getEventBus().registerForEvent(
            DBStatEvent.class, getActorID());
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void start()
    {
        super.start();
        myScheduler.scheduleWithFixedDelay(
            new PublishHeartbeatTask(),
            myConfig.getHeartBeatInterval(),
            myConfig.getHeartBeatInterval(),
            TimeUnit.SECONDS);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void stop()
    {
        super.stop();
        myScheduler.shutdownNow();
    }
}
