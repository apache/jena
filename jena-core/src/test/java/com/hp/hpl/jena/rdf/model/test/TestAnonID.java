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

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.test.JenaTestBase;

import junit.framework.TestSuite;

/**
 * Test for anonID generation. (Originally test for the debugging hack
 * that switches off anonID generation.)
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:33 $
 */
public class TestAnonID extends JenaTestBase {
    
    /**
     * Boilerplate for junit
     */ 
    public TestAnonID( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestAnonID.class ); 
    }  

    /**
     * Check that anonIDs are distinct whichever state the flag is in.
     */
    public void testAnonID() {
        boolean prior = JenaParameters.disableBNodeUIDGeneration;
        try
            {
            JenaParameters.disableBNodeUIDGeneration = false;
            doTestAnonID();
            JenaParameters.disableBNodeUIDGeneration = true;
            doTestAnonID();
            }
        finally
            { JenaParameters.disableBNodeUIDGeneration = prior; }
    }

    /**
         Check that anonIDs are distinct whichever state the flag is in.
    */
    public void doTestAnonID() {
        AnonId id1 = AnonId.create();
        AnonId id2 = AnonId.create();
        AnonId id3 = AnonId.create();
        AnonId id4 = AnonId.create();
        
        assertDiffer( id1, id2 );
        assertDiffer( id1, id3 );
        assertDiffer( id1, id4 );
        assertDiffer( id2, id3 );
        assertDiffer( id2, id4 );
    }
    
    /**
        Test that creation of an AnonId from an AnonId string preserves that
        string and is equal to the original AnonId.
    */
    public void testAnonIdPreserved()
        {
        AnonId anon = AnonId.create();
        String id = anon.toString();
        assertEquals( anon, AnonId.create( id ) );
        assertEquals( id, AnonId.create( id ).toString() );
        }

}
