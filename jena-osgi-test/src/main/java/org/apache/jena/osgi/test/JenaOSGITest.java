package org.apache.jena.osgi.test;

import org.junit.Test;

/** 
 * OSGi service interface for test using osgi-testrunner-junit4 
 * <p>
 * Junit annotations like @Test must be made here.
 * The implementation is in JenaOSGITestImpl, which is registered
 * with OSGi by JenaOSGIActiviator.
 * 
 * @author stain
 *
 */
public interface JenaOSGITest {

	@Test
	public void testJenaIRI() throws Exception;

	@Test
	public void testJenaCore() throws Exception;
	
	@Test
	public void testJenaArq() throws Exception;
	
	@Test
	public void testJenaTdb() throws Exception;
	
}
