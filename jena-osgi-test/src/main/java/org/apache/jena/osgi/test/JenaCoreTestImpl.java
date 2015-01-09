package org.apache.jena.osgi.test;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.junit.Test;

@Component
@Service(JenaCoreTest.class)
public class JenaCoreTestImpl implements JenaCoreTest {
	@Test
	public void coreTests() {
		System.out.println("Testing stuff");
		// Ideally we would run everything in com.hp.hpl.jena.test.TestPackage
		// directly, but currently it relies heavily on the folder testing/ being
		// on the current path. We'll cheat and set that at the user.dir..
//		Path jenaCoreFolder = Paths.get("..", "jena-core");
//		if (! Files.isDirectory(jenaCoreFolder.resolve("testing"))) { 
//			throw new RuntimeException("Can't find ../jena-core/testing");
//		}
//		System.setProperty("user.dir", jenaCoreFolder.toAbsolutePath().toString());		
		
//		TestSuite suite = TestPackage.suite();
//		junit.textui.TestRunner.run(suite) ;
//        //SimpleTestRunner.runAndReport(suite) ;
	}
	

}
