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

public class TreeNode extends TreeElement {
	public static int MAX_CHILDREN = 16;
	
	public EntityDescriptor entityDescriptor = null;
	
	public TreeNode (TreeNode parent, EntityDescriptor entityDescriptor) {
		this.parent = parent;
		this.entityDescriptor = entityDescriptor;
	}
	
	public TreeNode previousSibling = null;
	public TreeNode nextSibling = null;

	public void SplitChild (TreeElement element) {
		if (element instanceof TreeNode) {
			
		}
		
		if (element instanceof TreeLeaf) {
			if (((TreeLeaf) element).nextSibling == null) {
				
			}
		}
	}
}
