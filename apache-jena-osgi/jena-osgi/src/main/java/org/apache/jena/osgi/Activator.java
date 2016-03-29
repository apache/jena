package org.apache.jena.osgi;

import org.apache.jena.system.JenaSubsystemRegistry;
import org.apache.jena.system.JenaSubsystemRegistryBasic;
import org.apache.jena.system.JenaSystem;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	/* 
	 * Based on http://svn.apache.org/repos/asf/aries/trunk/spi-fly/spi-fly-examples/spi-fly-example-provider-consumer-bundle/src/main/java/org/apache/aries/spifly/pc/bundle/Activator.java
	 * the Activator#start() waits for bundle extension by Aries SPI Fly, configures JenaSystem logging and requests for initialization.     
	 */
	public void start(BundleContext context) throws Exception {

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}				
				setUpJena();
			}
		});
		t.start();
	}

	private void setUpJena() {
		JenaSubsystemRegistry r = new JenaSubsystemRegistryBasic() {
			@Override
			public void load() {
				super.load();
			}
		};
		JenaSystem.setSubsystemRegistry(r);
		JenaSystem.DEBUG_INIT = true;
		JenaSystem.init();
		System.out.println("Jena-OSGi bundle configuration done!");
		
	}

	public void stop(BundleContext context) throws Exception {

	}
}
