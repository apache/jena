package org.apache.jena.osgi.test;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class JenaOSGITestActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Starting ");
		Dictionary<String, String> properties = new Hashtable<>();
		properties.put("eosgi.testId", "JenaOSGITest");
		properties.put("eosgi.testEngine", "junit4");		
		context.registerService(JenaOSGITest.class, new JenaOSGITestImpl(), properties);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
