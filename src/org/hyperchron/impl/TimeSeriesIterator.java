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
	
	public long getID() {
		if (currentIndex < currentLeaf.length) {
			if (currentLeaf.IDs == null)
				BlockStore.instance.LoadDataIntoLeaf(currentLeaf.entityDescriptor.uuid, currentLeaf, true);
				
			return currentLeaf.IDs[currentIndex];
		}
			
		return -1;
	}
}
