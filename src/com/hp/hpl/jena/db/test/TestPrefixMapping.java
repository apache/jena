/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestPrefixMapping.java,v 1.5 2004-07-16 15:52:16 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.test;

import java.util.*;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.shared.test.AbstractTestPrefixMapping;
import junit.framework.*;

/**
 * 
 * This shares the same test as the in-memory prefix mapping.
 * (Tests for the persistence of prefix maps are in TestNSPrefix).
 *
 *	@author csayers based on testGraphRDB by kers
 *	@version $Revision: 1.5 $
 */
public class TestPrefixMapping extends AbstractTestPrefixMapping {

	private List models = null;
	private IDBConnection theConnection = null;
	private static int count = 0;
	
	public TestPrefixMapping(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(TestPrefixMapping.class);
	}

	public void setUp() {
		theConnection = TestConnection.makeAndCleanTestConnection();
		models = new ArrayList();
	}

	public void tearDown() {
		
		// close all the models we opened
		Iterator it = models.iterator();
		while(it.hasNext()) {
			Model m = (Model)it.next();
			m.close();
		}
			
		try {
			theConnection.close();
		} catch (Exception e) {
			throw new JenaException(e);
		}
	}

    private String getModelName()
        { return "test" + count++; }
        
    private Model getModel()
        {
        Model model = ModelRDB.createModel( theConnection, getModelName() );
        models.add( model );
        return model;
        }
        
	public PrefixMapping getMapping() {
		Model model = getModel();
		return model.getGraph().getPrefixMapping();
	}
    
    public void testPrefixesPersist()
        {
        String name = "prefix-testing-model-persist"; 
        Model m = ModelRDB.createModel( theConnection, name );
        m.setNsPrefix( "hello", "eh:/someURI#" );
        m.setNsPrefix( "bingo", "eh:/otherURI#" );
        m.setNsPrefix( "yendi", "eh:/otherURI#" );
        m.close();
        Model m1 = ModelRDB.open( theConnection, name );
        assertEquals( "eh:/someURI#", m1.getNsPrefixURI( "hello" ) );
        assertEquals( "eh:/otherURI#", m1.getNsPrefixURI( "yendi" ) );
        assertEquals( null, m1.getNsPrefixURI( "bingo" ) );
        m1.close();
        }
    
    public void testPrefixesRemoved()
        {
        String name = "prefix-testing-model-remove"; 
        Model m = ModelRDB.createModel( theConnection, name );
        m.setNsPrefix( "hello", "eh:/someURI#" );
        m.setNsPrefix( "there", "eg:/otherURI#" );
        m.removeNsPrefix( "hello" );
        assertEquals( null, m.getNsPrefixURI( "hello" ) );
        m.close();
        Model m1 = ModelRDB.open( theConnection, name );
        assertEquals( null, m1.getNsPrefixURI( "hello" ) );
        assertEquals( "eg:/otherURI#", m1.getNsPrefixURI( "there" ) );
        m1.close();
        }

}

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/