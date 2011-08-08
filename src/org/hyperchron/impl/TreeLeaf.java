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

public class TreeLeaf extends TreeElement {
	public long[] timeStamps = null;
	public long[] IDs = null;
	
	public int length = 0;
	
	public long startingRevision = -1;
	
	public EntityDescriptor entityDescriptor = null;

	public long blockID = -1;
	public long entityID = -1;
		
	public TreeLeaf previousSibling = null;
	public TreeLeaf nextSibling = null;
	
	public TreeLeaf LRUprev = null;
	public TreeLeaf LRUnext = null;
	
	public TreeLeaf(long blockID, long entityID) {
		this.blockID = blockID;
		this.entityID = entityID;
	}
}
