/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestReificationPredicates.java,v 1.2 2006-03-22 13:52:22 andy_seaborne Exp $
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


/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
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