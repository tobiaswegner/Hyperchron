package org.hyperchron.test;

import java.util.ArrayList;

import org.hyperchron.impl.TimeSeriesImplementation;

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
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ts = new TimeSeriesImplementation();
		
		ts.activate();

		long startTime = System.nanoTime();
		long endTime = startTime;
		
		/*
		 * 0 linear insert
		 * 1 data retrieval
		 */
		
		int bench = 1;
		
		switch (bench) {
		case 0:
			int offset = 0;
			int length = 64 * 1024 * 1024;
			
			for (int i = offset; i < offset + length; i++) {
				ts.saveTimestamp("debug", i);
			}
			
			endTime = System.nanoTime();
			
			//give flush thread time to work
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			break;
		case 1:
			checkValues (readTS("debug", 512 * 1024, 512 * 1024 + 1024), 512 * 1024, 1024);
			checkValues (readTS("debug", 2 * 1024 * 1024, 2 * 1024 * 1024 + 1024), 2 * 1024 * 1024, 1024);
			checkValues (readTS("debug", 16 * 1024 * 1024, 16 * 1024 * 1024 + 1024), 16 * 1024 * 1024, 1024);
/*			
			for (int i = 0; i < vals.size(); i++) {
				TimedValue value = vals.get(i);
				
				if (!Long.toString(value.getTimeStamp()).equals(value.getValue())) {
					System.out.println("Fatal error, db inconsistent");
				}
			}*/
			
			endTime = System.nanoTime();

			break;
		default:
		}
		
		System.out.println("Benchmark took " + (endTime-startTime) + "ns (" + ((endTime-startTime) / 1000000000.0) + "s )");
		
		ts.deactivate();
	}

}