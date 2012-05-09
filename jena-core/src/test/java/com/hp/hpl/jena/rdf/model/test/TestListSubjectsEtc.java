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

import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestSuite;

/**
     TestListSubjectsEtc - tests for listSubjects, listObjects [and listPredicates, if
     it were to exist]
     TODO make preperly generic, add missing test cases [we're relying, at root,
     on SimpleQueryHandler]
     
     @author kers
 */
public class TestListSubjectsEtc extends ModelTestBase
    {
    public TestListSubjectsEtc( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestListSubjectsEtc.class ); }
    
    public void testListSubjectsNoRemove()
        {
        Model m = modelWithStatements( "a P b; b Q c; c R a" );
        ResIterator it = m.listSubjects();
        it.next();
        try { it.remove(); fail( "listSubjects should not support .remove()" ); }
        catch (UnsupportedOperationException e) { pass(); }
        }
    
    public void testListObjectsNoRemove()
        {
        Model m = modelWithStatements( "a P b; b Q c; c R a" );
        NodeIterator it = m.listObjects();
        it.next();
        try { it.remove(); fail( "listObjects should not support .remove()" ); }
        catch (UnsupportedOperationException e) { pass(); }
        }
    
    public void testListSubjectsWorksAfterRemoveProperties()
        {
        Model m = modelWithStatements( "p1 before terminal; p2 before terminal" );
        m.createResource( "eh:/p1" ).removeProperties();
        assertIsoModels( modelWithStatements( "p2 before terminal" ), m );
        assertEquals( resourceSet( "p2" ), m.listSubjects().toSet() );
        }
    
    public void testListSubjectsWorksAfterRemovePropertiesWIthLots()
        {
        Model m = modelWithStatements( "p2 before terminal" );
        for (int i = 0; i < 100; i += 1) modelAdd( m, "p1 hasValue " + i );
        m.createResource( "eh:/p1" ).removeProperties();
        assertIsoModels( modelWithStatements( "p2 before terminal" ), m );
        assertEquals( resourceSet( "p2" ), m.listSubjects().toSet() );
        }
    }
