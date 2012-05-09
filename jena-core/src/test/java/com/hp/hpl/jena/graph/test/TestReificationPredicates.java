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

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.*;

/**
    Tests that the predicates for recognising [parts of] reification quadlets,
    parked in Reifier.Util, work as required.
    
    @author kers
*/
public class TestReificationPredicates extends GraphTestBase
    {
    public TestReificationPredicates( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestReificationPredicates.class ); }

    public void testSubject()
        { 
        assertTrue( Reifier.Util.isReificationPredicate( RDF.Nodes.subject ) );
        }

    public void testPredicate()
        { 
        assertTrue( Reifier.Util.isReificationPredicate( RDF.Nodes.predicate ) );
        }
    
    public void testObject()
        { 
        assertTrue( Reifier.Util.isReificationPredicate( RDF.Nodes.object ) );
        }
    
    public void testRest()
        {
        assertFalse( Reifier.Util.isReificationPredicate( RDF.Nodes.rest ) );
        }    
    
    public void testFirst()
        {
        assertFalse( Reifier.Util.isReificationPredicate( RDF.Nodes.first ) );
        }
    
    public void testType()
        {
        assertFalse( Reifier.Util.isReificationPredicate( RDF.Nodes.type ) );
        }
    
    public void testValue()
        {
        assertFalse( Reifier.Util.isReificationPredicate( RDF.Nodes.value ) );
        }
    
    public void testSubjectInOtherNamespace()
        {
        assertFalse( Reifier.Util.isReificationPredicate( node( "subject" ) ) );
        }
    
    public void testStatementCouldBeStatement()
        {
        assertTrue( Reifier.Util.couldBeStatement( RDF.Nodes.Statement ) );
        }
    
    public void testVariableCouldBeStatement()
        {
        assertTrue( Reifier.Util.couldBeStatement( node( "?x" ) ) );
        }
    
    public void testANYCouldBeStatement()
        {
        assertTrue( Reifier.Util.couldBeStatement( Node.ANY ) );
        }
    
    public void testPropertyCouldNotBeStatement()
        {
        assertFalse( Reifier.Util.couldBeStatement( RDF.Nodes.Property ) );
        }
    
    public void testOtherStatementCouldBeStatement()
        {
        assertFalse( Reifier.Util.couldBeStatement( node( "Statement" ) ) );
        }
    
    public void testAltIsntIsReificationType()
        {
        assertFalse( Reifier.Util.isReificationType( RDF.Nodes.type, RDF.Nodes.Alt ) );
        }
    
    public void testBagIsntIsReificationType()
        {
        assertFalse( Reifier.Util.isReificationType( RDF.Nodes.type, RDF.Nodes.Bag ) );
        }    
    
    public void testOtherStatementIsntIsReificationType()
        {
        assertFalse( Reifier.Util.isReificationType( RDF.Nodes.type, node( "Statement" ) ) );
        }    
    
    public void testValueIsNtReificationType()
        {
        assertFalse( Reifier.Util.isReificationType( RDF.Nodes.value, RDF.Nodes.Statement ) );
        }    
    
    public void testValuePropertyIsntreificationType()
        {
        assertFalse( Reifier.Util.isReificationType( RDF.Nodes.value, RDF.Nodes.Property ) );
        }
    
    public void testStatementIsReificationType()
        {
        assertTrue( Reifier.Util.isReificationType( RDF.Nodes.type, RDF.Nodes.Statement ) );
        }    
    
    public void testVariableIsReificationType()
        {
        assertTrue( Reifier.Util.isReificationType( RDF.Nodes.type, node( "?x" ) ) );
        }   
    
    public void testANYIsReificationType()
        {
        assertTrue( Reifier.Util.isReificationType( RDF.Nodes.type, Node.ANY ) );
        }
    }
