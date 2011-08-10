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

import org.hyperchron.impl.blocks.BlockStore;


public class TimeSeriesIterator {
	public Tree tree = null;
	
	public TreeLeaf currentLeaf = null;
	public int currentIndex = -1;
	
	public TimeSeriesIterator(Tree tree) {
		this.tree = tree;
	}
	
	public void next () {
		if (currentIndex < currentLeaf.length - 1) {
			currentIndex++;
		} else {
			if (currentLeaf.nextSibling != null) {
				currentLeaf = (TreeLeaf)currentLeaf.nextSibling;
				currentIndex = 0;
			}
		}
	}
	
	public long next (long steps) {
		long currentRevision = 0;
		
		while (steps > 0)
		{
			int revisionsLeft = revisionsLeftInCurrentLeaf();
			
			if (steps > revisionsLeft) {
				steps -= revisionsLeft;
				currentRevision += revisionsLeft;
				
				currentIndex += revisionsLeft;
				
				if (currentLeaf.nextSibling == null)
					return currentRevision;
				
				currentLeaf = currentLeaf.nextSibling;
				currentIndex = 0;
				
				currentRevision++;
				steps--;
			}
			else
			{
				currentIndex += (int)steps;
				currentRevision += steps;
				
				steps = 0;
			}
		}
		
		return currentRevision;
	}
	
	public void previous () {
		if (currentIndex > 0) {
			currentIndex--;
		} else {
			if (currentLeaf.previousSibling != null) {
				currentLeaf = (TreeLeaf)currentLeaf.previousSibling;
				currentIndex = currentLeaf.length - 1;
			}
		}
	}
	
	public long previous (long steps) {
		long currentRevision = 0;
		
		while (steps > 0)
		{
			int revisionsLeft = currentIndex;
			
			if (steps > revisionsLeft) {
				steps -= revisionsLeft;
				currentRevision += revisionsLeft;
				
				currentIndex = 0;
				
				if (currentLeaf.previousSibling == null)
					return currentRevision;
				
				currentLeaf = currentLeaf.previousSibling;
				currentIndex = currentLeaf.length - 1;
				
				currentRevision++;
				steps--;
			}
			else
			{
				currentIndex -= (int)steps;
				currentRevision += steps;
				
				steps = 0;
			}
		}
		
		return currentRevision;
	}
	
	public long getTimeStamp() {
		if (currentLeaf.timeStamps == null)
			BlockStore.instance.LoadDataIntoLeaf(currentLeaf.entityDescriptor.uuid, currentLeaf, true);

		synchronized (currentLeaf) {
			if (currentIndex < currentLeaf.length) {				
				return currentLeaf.timeStamps[currentIndex];
			}			
		}
		
			
		return -1;
	}
	
	public int revisionsLeftInCurrentLeaf () {
		return currentLeaf.length - currentIndex - 1;
	}
}
