package org.hyperchron.osgi;

import org.hyperchron.TimeSeries;
import org.hyperchron.impl.TimeSeriesImplementation;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

import com.db4o.osgi.Db4oService;

public class OSGiComponent {
	TimeSeries timeSeries = null;
	ServiceRegistration timeSeriesRegistration;
	
	protected ComponentContext context = null;
	
	public String tsFileDB = null;
		
	public void activate(ComponentContext context) {
		this.context = context;
		
		tsFileDB = System.getProperty("timeseries.entityfile");
		if (tsFileDB == null)
			tsFileDB = "D:\\Temp\\ts\\entities.db";		

		timeSeries = new TimeSeriesImplementation(db4o.openFile(tsFileDB));
		
		timeSeriesRegistration = context.getBundleContext().registerService(TimeSeries.class.getName(), timeSeries, null);
	}	
	
	public void deactivate() {
		timeSeriesRegistration.unregister();
		
		timeSeries.Shutdown();
	}
	
	protected Db4oService db4o;
	
	public void bindDb4oService (Db4oService db4o) { this.db4o = db4o; };
}
