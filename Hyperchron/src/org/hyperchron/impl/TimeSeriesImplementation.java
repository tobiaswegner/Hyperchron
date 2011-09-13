/*
 * Hyperchron, a timeseries data management solution.
 * Copyright (C) 2011 Tobias Wegner
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.hyperchron.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.hyperchron.TimeSeries;
import org.hyperchron.impl.blocks.BlockStore;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;

public class TimeSeriesImplementation implements TimeSeries {
	protected ObjectContainer entityDB = null;

	Hashtable<Long, TimeSeriesIterator> iterators = new Hashtable<Long, TimeSeriesIterator>();
	
	Hashtable<String, EntityDescriptor> entityDescriptions = new Hashtable<String, EntityDescriptor>();
	
	ArrayList<String> entities = new ArrayList<String>();
	
	java.util.concurrent.atomic.AtomicLong entityID = new AtomicLong();	

	volatile boolean ShuttingDown = false;

	public long[] loadNextIDFromDB() {
		final Query query = entityDB.query();
		query.constrain(long[].class);

		ObjectSet<long[]> qresult = query.execute();
		
		if (qresult.size() > 0) {
			return qresult.next();
		}
		
		return null;
	}
	
	public TimeSeriesImplementation(ObjectContainer entityDB) {
		this.entityDB = entityDB;
	
		long[] nextID = loadNextIDFromDB();
		
		if (nextID != null) {
			entityID.set(nextID[0]);
		}

		{
			final Query query = entityDB.query();
			query.constrain(EntityDescriptor.class);
	
			ObjectSet<EntityDescriptor> qresult = query.execute();
			
			while (qresult.hasNext()) {
				EntityDescriptor entityDescriptor = qresult.next();
				
				Tree tree = new Tree(entityDescriptor);
				entityDescriptor.tree = tree;
	
				entityDescriptions.put(entityDescriptor.uuid, entityDescriptor);
			}
		}
	}
	
	protected void finalize() throws Throwable {
		try {
			Shutdown();
		} finally {
			super.finalize();
		}
	}
	
	@Override
	public long getIterator(String key) {
		long newID = Math.abs( new Random().nextLong() );
		
		while (iterators.containsKey(new Long(newID))) {
			newID = Math.abs( new Random().nextLong() );
		}
		
		EntityDescriptor entityDescriptor = entityDescriptions.get(key);
		
		if (entityDescriptor == null)
			return -1; //not known to db
		
		iterators.put(new Long(newID), new TimeSeriesIterator(entityDescriptor.tree));
		
		setIteratorAtBegin(newID);
		
		return newID;
	}

	@Override
	public void releaseIterator(long Iterator) {
		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));

		iterators.remove(timeSeriesIterator);
	}

	@Override
	public void setIteratorAtBegin(long Iterator) {
		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));

		timeSeriesIterator.currentLeaf = timeSeriesIterator.tree.getFirstLeaf();
		timeSeriesIterator.currentIndex = 0;
	}

	@Override
	public void setIteratorAtEnd(long Iterator) {
		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));

		TreeLeaf lastLeaf = timeSeriesIterator.tree.getLastLeaf();

		while (lastLeaf.length == 0) {
			if (lastLeaf.previousSibling != null)
				lastLeaf = lastLeaf.previousSibling;
			else
				break;
		}			
		
		timeSeriesIterator.currentLeaf = lastLeaf;
		timeSeriesIterator.currentIndex = lastLeaf.length - 1;
	}

	@Override
	public long setIteratorAtRevision(long Iterator, long Revision) {
		setIteratorAtBegin(Iterator);

		if (Revision < 0)
			return 0;
		
		return MoveIterator(Iterator, Revision);
	}

	@Override
	public void setIteratorAfterTimestamp(long Iterator, long Timestamp) {
		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));

		Tree tree = timeSeriesIterator.tree;
		
//		TreeLeaf leaf = tree.GetLeafForTimestamp(Timestamp);
//		tree.GetIndexForTimestamp(leaf, Timestamp);
		
//		TreeElement rootElement = entityDescriptions.get(timeSeriesIterator.key).rootElement;
		
		if (Timestamp < tree.GetEndingTimeStamp(tree.rootNode)) {	
			timeSeriesIterator.currentLeaf = tree.GetLeafForTimestamp(Timestamp);
			timeSeriesIterator.currentIndex = tree.GetIndexForTimestamp(timeSeriesIterator.currentLeaf, Timestamp);
		} else {
			setIteratorAtEnd(Iterator);
		}
	}

	@Override
	public long getIteratorRevision(long Iterator) {
		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));

		return timeSeriesIterator.currentLeaf.startingRevision + timeSeriesIterator.currentIndex; 
	}
	
	@Override
	public void IteratorGoToPreviousRevision(long Iterator) {
		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));

		timeSeriesIterator.previous();
	}

	@Override
	public void IteratorGoToNextRevision(long Iterator) {
		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));

		timeSeriesIterator.next();
	}
	
	@Override
	public long MoveIterator(long Iterator, long deltaRevision) {
		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));

		if (deltaRevision > 0)
			return timeSeriesIterator.next(deltaRevision);
		else
			return timeSeriesIterator.previous(-deltaRevision);		
	}		

	@Override
	public long getTimestamp(long Iterator) {
		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));

		if ((timeSeriesIterator.currentIndex < 0) || (timeSeriesIterator.currentLeaf == null))
			return -1;
		
		long timestamp = timeSeriesIterator.tree.getTimeStampForIndex(timeSeriesIterator.currentLeaf, timeSeriesIterator.currentIndex);
		
		return timestamp;
	}
	
	@Override
	public boolean saveTimestamp(String key, long Timestamp) {
		if (ShuttingDown)
			return false;
		
		EntityDescriptor entityDescriptor = entityDescriptions.get(key);
		
		if (entityDescriptor == null) {
			long nextID = entityID.getAndIncrement();
			
			long[] nextIDObj = loadNextIDFromDB();
			
			if (nextIDObj == null)
				nextIDObj = new long[1];
			
			nextIDObj[0] = nextID;
			
			entityDB.store(nextIDObj);
			
			entityDescriptor = new EntityDescriptor(key, nextID);

			Tree tree = new Tree(entityDescriptor);
			entityDescriptor.tree = tree;

			entityDescriptions.put(key, entityDescriptor);
			
			entityDB.store(entityDescriptor);
		}

		entityDescriptor.tree.SaveTimestamp(Timestamp);
		
		return true;
	}

	@Override
	public void Shutdown () {
		if (ShuttingDown)
			return;

		ShuttingDown = true;
		
		BlockStore.instance.Shutdown();
	}
}
