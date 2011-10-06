package org.hyperchron.osgi;

import java.io.FileNotFoundException;

import org.hyperchron.TimeSeries;
import org.hyperchron.blocks.BlockStoreFactory;
import org.hyperchron.blocks.BlockStoreMetric;
import org.hyperchron.impl.HyperchronMetrics;
import org.hyperchron.impl.TimeSeriesImplementation;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

import com.db4o.ext.Db4oException;
import com.db4o.osgi.Db4oService;

public class OSGiComponent {
	TimeSeries timeSeries = null;
	ServiceRegistration timeSeriesRegistration;
	
	protected ComponentContext context = null;
	
	public String tsFileDB = null;
	public String blockDB = null;
		
	public void activate(ComponentContext context) {
		this.context = context;
		
		tsFileDB = System.getProperty("timeseries.entityfile");
		if (tsFileDB == null)
			tsFileDB = context.getBundleContext().getDataFile("entities.db").getAbsolutePath();		

		blockDB = System.getProperty("timeseries.blockfile");
		if (blockDB == null)
			blockDB = context.getBundleContext().getDataFile("block.db").getAbsolutePath();
		
		try {
			timeSeries = new TimeSeriesImplementation(db4o.openFile(tsFileDB), BlockStoreFactory.openBlockStore(blockDB, new BlockStoreMetric(HyperchronMetrics.BLOCK_SIZE, HyperchronMetrics.SUPERBLOCK_ENTRIES)));

			timeSeriesRegistration = context.getBundleContext().registerService(TimeSeries.class.getName(), timeSeries, null);
		} catch (Db4oException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void deactivate() {
		timeSeriesRegistration.unregister();
		
		timeSeries.Shutdown();
	}
	
	protected Db4oService db4o;
	
	public void bindDb4oService (Db4oService db4o) { this.db4o = db4o; };
}
