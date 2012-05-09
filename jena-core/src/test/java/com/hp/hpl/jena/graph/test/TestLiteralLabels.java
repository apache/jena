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

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;

import junit.framework.*;

/**
    Tests submitted By: Wolfgang Groiss (littlelui) via SF bugtracker, incorporated
    into new test class by kers.
    
    @author kers
*/
public class TestLiteralLabels extends GraphTestBase
    {
    public TestLiteralLabels( String name )
        { super( name );  }

    public static Test suite()
        { return new TestSuite( TestLiteralLabels.class ); }
    
    public void testHashCode()  
        {
        LiteralLabel ll = LiteralLabelFactory.create( "test", "", null );
        ll.hashCode();
        }

    public void testHashCode2() 
        {
        LiteralLabel ll = LiteralLabelFactory.create( "test",  "", null );
        ll.hashCode();
        }    
    
    public void testHashCodesForBase64Binary()
        {
        LiteralLabel A = node( "'0123'http://www.w3.org/2001/XMLSchema#base64Binary" ).getLiteral();
        LiteralLabel B = node( "'0123'http://www.w3.org/2001/XMLSchema#base64Binary" ).getLiteral();
        assertEquals( A.hashCode(), B.hashCode() );
        }
    
    public void testHashCodesForHexBinary()
        {
        LiteralLabel A = node( "'0123'http://www.w3.org/2001/XMLSchema#hexBinary" ).getLiteral();
        LiteralLabel B = node( "'0123'http://www.w3.org/2001/XMLSchema#hexBinary" ).getLiteral();
        assertEquals( A.hashCode(), B.hashCode() );
        }
    
    public void testDatatypeIsEqualsNotCalledIfSecondOperandIsNotTyped()
        {
        RDFDatatype d = new BaseDatatype( "eh:/FakeDataType" ) 
            {
            @Override
            public boolean isEqual( LiteralLabel A, LiteralLabel B ) 
                { 
                fail( "RDFDatatype::isEquals should not be called if B has no datatype" ); 
                return false; 
                }
            };
        LiteralLabel A = LiteralLabelFactory.create( "17", "", d );
        LiteralLabel B = LiteralLabelFactory.create( "17", "", null );
        assertFalse( A.sameValueAs( B ) );
        }

    // AFS
    public void testEquality1()
    {
        LiteralLabel A = LiteralLabelFactory.create("xyz") ;
        LiteralLabel B = LiteralLabelFactory.create("xyz") ;
        assertTrue(A.equals(B)) ;
        assertTrue(A.sameValueAs(B)) ;
        assertEquals(A.hashCode(), B.hashCode()) ;
    }
    
    public void testEquality2()
    {
        LiteralLabel A = LiteralLabelFactory.create("xyz") ;
        LiteralLabel B = LiteralLabelFactory.create("XYZ") ;
        assertFalse(A.equals(B)) ;
        assertFalse(A.sameValueAs(B)) ;
    }

    public void testEquality3()
    {
        LiteralLabel A = LiteralLabelFactory.create("xyz", "en-us") ;
        LiteralLabel B = LiteralLabelFactory.create("xyz", "en-uk") ;
        assertFalse(A.equals(B)) ;
        assertFalse(A.sameValueAs(B)) ;
    }

    public void testEquality4()
    {
        LiteralLabel A = LiteralLabelFactory.create("xyz", "en-UK") ;
        LiteralLabel B = LiteralLabelFactory.create("xyz", "en-uk") ;
        assertFalse(A.equals(B)) ;
        assertTrue(A.sameValueAs(B)) ;
    }
    
}
