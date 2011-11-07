/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.db.test;

import java.util.*;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.*;

import junit.framework.*;

/**
 * 
 * This shares the same test as the in-memory prefix mapping.
 * (Tests for the persistence of prefix maps are in TestNSPrefix).
 *
 *	@author csayers based on testGraphRDB by kers
 *	@version $Revision: 1.2 $
 */
public class TestPrefixMapping extends AbstractTestPrefixMapping {

	private List<Model> models = null;
	private IDBConnection theConnection = null;
	private static int count = 0;
	
	public TestPrefixMapping(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(TestPrefixMapping.class);
	}

	@Override
    public void setUp() {
		theConnection = TestConnection.makeAndCleanTestConnection();
		models = new ArrayList<Model>();
	}

	@Override
    public void tearDown() {
		
		// close all the models we opened
		Iterator<Model> it = models.iterator();
		while(it.hasNext()) {
			Model m = it.next();
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
        
	@Override
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
        assertEquals( "eh:/otherURI#", m1.getNsPrefixURI( "bingo" ) );
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
