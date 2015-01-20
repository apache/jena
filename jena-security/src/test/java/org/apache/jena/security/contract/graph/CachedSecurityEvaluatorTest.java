package org.apache.jena.security.contract.graph;

import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.StaticSecurityEvaluator;
import org.apache.jena.security.impl.CachedSecurityEvaluator;
import org.junit.Test;
import static org.junit.Assert.*;

public class CachedSecurityEvaluatorTest {

	private StaticSecurityEvaluator securityEvaluator;
	private SecurityEvaluator cachedEvaluator;
	
	public CachedSecurityEvaluatorTest() {
		securityEvaluator = new StaticSecurityEvaluator( "bob" );
		cachedEvaluator = new CachedSecurityEvaluator( securityEvaluator, "ted" );
		
	}
	
	@Test
	public void testGetPrincipal()
	{
		assertEquals( "bob", securityEvaluator.getPrincipal());
		assertEquals( "ted", cachedEvaluator.getPrincipal());
	}

}
