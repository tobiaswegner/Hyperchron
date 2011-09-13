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

package org.hyperchron.test;

import java.util.ArrayList;
import java.util.Random;

import org.hyperchron.impl.TimeSeriesImplementation;

import com.db4o.Db4oEmbedded;

public class HyperchronTest {

	protected static TimeSeriesImplementation ts = null;
	
	public static ArrayList<Long> readTS(String uuid, long startTS, long endTS) {
		ArrayList<Long> result = new ArrayList<Long>();
		
		long iterator = ts.getIterator(uuid);
		
		ts.setIteratorAfterTimestamp(iterator, startTS);
		
		while (ts.getTimestamp(iterator) < endTS) {
			result.add(new Long(ts.getTimestamp(iterator)));
			
			ts.IteratorGoToNextRevision(iterator);
		}
		
		ts.releaseIterator(iterator);
		
		return result;
	}
	
	public static void checkValues (ArrayList<Long> values, int start, int length) {
		if (values.size() != length) {
			System.out.println("size missmatch");
			
			return;
		}
		
		for (int i = 0; i < length; i++)
			if (values.get(i).longValue() != start + i) {
				System.out.println("Fatal error, db inconsistent");
				
				return;
			}
	}
	
	public static void checkValue (String uuid, int position) {
		long iterator = ts.getIterator(uuid);
		
		ts.setIteratorAfterTimestamp(iterator, position);
		
		if (ts.getTimestamp(iterator) != position) {
			System.out.println("Fatal error, db inconsistent");
		}

		ts.releaseIterator(iterator);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String databaseFileName = System.getProperty("timeseries.entityfile");
		if (databaseFileName == null)
			databaseFileName = "D:\\Temp\\ts\\entities.db";		
		
		ts = new TimeSeriesImplementation(Db4oEmbedded.openFile(databaseFileName));
		
		long startTime = System.nanoTime();
		long endTime = startTime;
		
		/*
		 * 0 linear insert
		 * 1 reverse linear insert
		 * 2 data retrieval
		 * 3 random data retrieval
		 */
		
		int bench = 2;

		int offset = 0;
		int length = 64 * 1024 * 1024;
		
		switch (bench) {
		case 0:
			for (int i = offset; i < offset + length; i++) {
				ts.saveTimestamp("debug", i);
			}
			
			endTime = System.nanoTime();
			
			break;
		case 1:
			for (int i = offset + length - 1; i >= offset; i--) {
				ts.saveTimestamp("debug", i);
			}
			
			endTime = System.nanoTime();
			
			break;
		case 2:
			checkValues (readTS("debug", 512 * 1024, 512 * 1024 + 1024), 512 * 1024, 1024);
			checkValues (readTS("debug", 2 * 1024 * 1024, 2 * 1024 * 1024 + 1024), 2 * 1024 * 1024, 1024);
			checkValues (readTS("debug", 16 * 1024 * 1024, 16 * 1024 * 1024 + 1024), 16 * 1024 * 1024, 1024);
			
			endTime = System.nanoTime();

			break;
		case 3:
			Random rnd = new Random();
			
			for (int i = 0; i < 1024; i++) {
				checkValue("debug", offset + rnd.nextInt(length));
			}
			
			endTime = System.nanoTime();
			
			break;
		default:
		}
		
		System.out.println("Benchmark took " + (endTime-startTime) + "ns (" + ((endTime-startTime) / 1000000000.0) + "s )");
		
		ts.Shutdown();
	}

}
