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

import java.nio.LongBuffer;

public class TreeLeaf extends TreeElement {
	public LongBuffer timeStamps = null;
	public long lastWrite = -1;
	public long lastFlush = -1;
	
	private int length = 0;
	public void initLength(int length) { this.length = length; };
	public void setLength(int length) { this.length = length; tree.blockStore.WriteToSuperblock(getChunkID(), getOffsetInChunk(), 2, length); };
	public int getLength () { return length; };
	
	public void initStartingTimestamp(long startingTimestamp) { this.startingTimestamp = startingTimestamp; };
	public void setStartingTimestamp(long startingTimestamp) { this.startingTimestamp = startingTimestamp; tree.blockStore.WriteToSuperblock(getChunkID(), getOffsetInChunk(), 1, startingTimestamp); };
	
	public long startingRevision = -1;
	
	public EntityDescriptor entityDescriptor = null;

	public long blockID = -1;
	public long entityID = -1;
		
	public TreeLeaf previousSibling = null;
	public TreeLeaf nextSibling = null;
	
	public TreeLeaf LRUprev = null;
	public TreeLeaf LRUnext = null;
	
	public Tree tree;
	
	public TreeLeaf(long chunkID, long blockOffset, long entityID, Tree tree) {
		this(chunkID * HyperchronMetrics.blocksPerSuperblock + blockOffset, entityID, tree);
	}

	public TreeLeaf(long blockID, long entityID, Tree tree) {
		this.blockID = blockID;
		this.entityID = entityID;
		this.tree = tree;
		
		tree.blockStore.WriteToSuperblock(getChunkID(), getOffsetInChunk(), 0, entityID);
	}
	
	public int getOffsetInChunk() {
		return (int)(blockID % HyperchronMetrics.blocksPerSuperblock);
	}
	
	public long getChunkID() {
		return blockID / HyperchronMetrics.blocksPerSuperblock;
	}
	
	public int getOffsetInBuffer() {
		return getOffsetInChunk() * HyperchronMetrics.BLOCK_SIZE;
	}
}
