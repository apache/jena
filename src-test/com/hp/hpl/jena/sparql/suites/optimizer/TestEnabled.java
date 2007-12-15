/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites.optimizer;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.optimizer.Optimizer;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Constants;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.sparql.suites.optimizer.Util;

import junit.extensions.TestSetup;
import junit.framework.*;

/**
 * The if the BGP optimizer is enabled and enabling/disabling.
 * The flag if the optimizer is enabled or disabled is stored
 * as a boolean value to the ARQ context. Please note that
 * only the optimized basic pattern stage generator is allowed
 * to turn this flag to true, not the Optimizer class, since
 * this should test whether ARQ executes the optimized stage
 * generator during query evaluation.
 * 
 * Please note that if per default the optimizer is not
 * turned on, the first test will fail.
 * 
 * @author Markus Stocker
 */

public class TestEnabled extends TestCase
{	
	private static Triple triple1 = null ;
	private static Triple triple2 = null ;
	private static Model model = null ;
	private static final String testDataFileName = "testing/Optimizer/Test-data.n3" ;
	
	public TestEnabled(String title)
	{		
		super(title) ;
	}
	
	protected static void oneTimeSetUp()
	{
		triple1 = new Triple(Var.alloc("x"), RDFS.label.asNode(), Var.alloc("y")) ;
		triple2 = new Triple(Var.alloc("x"), RDF.type.asNode(), RDFS.Class.asNode()) ;
		
		model = Util.readModel(testDataFileName) ;
	}
	
	protected static void oneTimeTearDown()
	{
		model.close() ;
	}
	
	public void testDefaultEnabled()
	{
		isEnabled() ;
	}
	
	public void testDisable()
	{
		Optimizer.enable() ;
		Optimizer.disable() ;
		isDisabled() ;
	}
	
	public void testEnable()
	{
		Optimizer.disable() ;
		Optimizer.enable() ;
		isEnabled() ;
	}
	
	public static Test suite()
    {
        TestSuite ts = new TestSuite("TestEnabled") ;
			
		ts.addTest(new TestEnabled("testDefaultEnabled")) ;
		ts.addTest(new TestEnabled("testDisable")) ;
		ts.addTest(new TestEnabled("testEnable")) ;
		
        // Wrapper for the test suite including the test cases which executes the setup only once
		TestSetup wrapper = new TestSetup(ts) 
		{			
			protected void setUp() 
			{
				oneTimeSetUp();
			}

			protected void tearDown() 
			{
				oneTimeTearDown();
			}
		};
		
		return wrapper ;
    }
	
	private void isEnabled()
	{	
		QueryExecution qe = runQuery(getQuery()) ;
		
		Boolean isEnabled = (Boolean)qe.getContext().get(Constants.isEnabled) ;
		
		if (isEnabled != null)	
			assertTrue(isEnabled.booleanValue() == true) ;
		else
			assertTrue(false == true) ;
		
        qe.close() ;
	}
	
	private void isDisabled()
	{	
		QueryExecution qe = runQuery(getQuery()) ;
		
		Boolean isEnabled = (Boolean)qe.getContext().get(Constants.isEnabled) ;
		
		if (isEnabled != null)	
			assertTrue(isEnabled.booleanValue() == false) ;
		else
			assertTrue(false == true) ;
		
        qe.close() ;
	}
	
	private Query getQuery()
	{
		ElementTriplesBlock el = new ElementTriplesBlock() ;
		el.addTriple(triple1) ;
		el.addTriple(triple2) ;
		
		Query q = QueryFactory.make() ;
		q.setQuerySelectType() ;
		q.setQueryResultStar(true) ;
		q.setQueryPattern(el) ;
		
		return q ;
	}
	
	private QueryExecution runQuery(Query q)
	{
		QueryExecution qe = QueryExecutionFactory.create(q, model) ;
		ResultSet rs = qe.execSelect() ;
		ResultSetFormatter.consume(rs) ;
		
		return qe ;
	}
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */