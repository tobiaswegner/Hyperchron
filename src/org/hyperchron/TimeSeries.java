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

package org.hyperchron;

public interface TimeSeries {
	public long getIterator(String key);
	public void releaseIterator(long Iterator);

	public void setIteratorAtBegin(long Iterator);
	public void setIteratorAtEnd(long Iterator);
	public long setIteratorAtRevision(long Iterator, long Revision);
	public void setIteratorAfterTimestamp(long Iterator, long Timestamp);
	
	public long getIteratorRevision(long Iterator);

	public void IteratorGoToNextRevision(long Iterator);
	public void IteratorGoToPreviousRevision(long Iterator);
	
	public long getID(long Iterator);
	public long getCurrentTime(long Iterator);
	
	public long saveTimestamp (String key, long Timestamp);
	
	public void Shutdown ();
}
