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

public class TestLiteralsInModel extends ModelTestBase
    {
    public TestLiteralsInModel( String name )
        { super( name ); }

    protected final Model m = getModel();
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    static final Resource X = resource( "X" );
    
    static final Property P = property( "P" );
    
    public void testAddWithFloatObject()
        {
        m.addLiteral( X, P, 14.0f );
        assertTrue( m.contains( X, P, m.createTypedLiteral( 14.0f ) ) );
        assertTrue( m.containsLiteral( X, P, 14.0f ) );
        }
    
    public void testAddWithDoubleObject()
        {
        m.addLiteral( X, P, 14.0d );
        assertTrue( m.contains( X, P, m.createTypedLiteral( 14.0d ) ) );
        assertTrue( m.containsLiteral( X, P, 14.0d ) );
        }
    
    public void testAddWithBooleanObject()
        {
        m.addLiteral( X, P, true );
        assertTrue( m.contains( X, P, m.createTypedLiteral( true ) ) );
        assertTrue( m.containsLiteral( X, P, true ) );
        }
    
    public void testAddWithCharObject()
        {
        m.addLiteral( X, P, 'x' );
        assertTrue( m.contains( X, P, m.createTypedLiteral( 'x' ) ) );
        assertTrue( m.containsLiteral( X, P, 'x' ) );
        }
    
    public void testAddWithLongObject()
        {
        m.addLiteral( X, P, 99L );
        assertTrue( m.contains( X, P, m.createTypedLiteral( 99L ) ) );
        assertTrue( m.containsLiteral( X, P, 99L ) );
        }
    
    public void testAddWithLiteralObject()
        {
        Literal lit = m.createLiteral( "spoo" );
        m.addLiteral( X, P, lit );
        assertTrue( m.contains( X, P, lit ) );
        assertTrue( m.containsLiteral( X, P, lit ) );
        }
    
    public void testAddWithIntObject()
        {
        m.addLiteral( X, P, 99 );
        assertTrue( m.contains( X, P, m.createTypedLiteral( 99 ) ) );
        assertTrue( m.containsLiteral( X, P, 99 ) );
        }
    
// that version of addLiteral is deprecated; test removed.
//    public void testAddWithAnObject()
//        {
//        Object z = new Date();
//        m.addLiteral( X, P, z );
//        assertTrue( m.contains( X, P, m.createTypedLiteral( z ) ) );
//        assertTrue( m.containsLiteral( X, P, z ) );
//        }
    }
