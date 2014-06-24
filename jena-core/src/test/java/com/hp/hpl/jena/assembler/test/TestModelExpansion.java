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

package com.hp.hpl.jena.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.ModelExpansion;
import com.hp.hpl.jena.rdf.model.*;

public class TestModelExpansion extends AssemblerTestBase
    {
    public TestModelExpansion( String name )
        { super( name ); }

    public void testAddsSubclasses() 
        {
        Model base = model( "a R b" );
        Model schema = model( "x rdfs:subClassOf y; y P z" );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertIsoModels( model( "a R b; x rdfs:subClassOf y" ), answer );
        }
    
    public void testOmitsAnonynousSubclasses()
        {
        Model base = model( "a R b" );
        Model schema = model( "x rdfs:subClassOf _y; z rdfs:subClassOf _a" );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertIsoModels( model( "a R b" ), answer );
        }
    
    public void testAddsDomainTypes()
        {
        Model base = model( "a R b" );
        Model schema = model( "R rdfs:domain T" );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertIsoModels( model( "a R b; a rdf:type T" ), answer );
        }
    
    public void testAddsRangeTypes()
        {
        Model base = model( "a R b" );
        Model schema = model( "R rdfs:range T" );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertIsoModels( model( "a R b; b rdf:type T" ), answer );
        }
    
    public void testLabelsDontCrashExpansion()
        {
        Model base = ModelFactory.createRDFSModel( model( "a R b; a rdfs:label 'hello'" ) );
        Model schema = ModelFactory.createRDFSModel( model( "R rdfs:range T" ) );
        Model answer = ModelExpansion.withSchema( base, schema );
        }
    
    public void testIntersection()
        {
        testIntersection( "x rdf:type T; x rdf:type U", true, "T U" );
        testIntersection( "x rdf:type T; x rdf:type U", true, "T" );
        testIntersection( "x rdf:type T; x rdf:type U", false, "T U V" );
        testIntersection( "x rdf:type T; x rdf:type U; x rdf:type V", true, "T U V" );
        }

    private void testIntersection( String xTyped, boolean infers, String intersectionTypes )
        {
        Model base = model( xTyped );
        Model schema = intersectionModel( "I", intersectionTypes );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertEquals( "should [not] infer (x rdf:type I)", infers, answer.contains( statement( "x rdf:type I" ) ) );
        }

    private Model intersectionModel( String inter, String types )
        {
        return model( "I owl:equivalentClass _L; _L owl:intersectionOf _L1" + rdfList( "_L", types ) );
        }
    
    private String rdfList( String base, String types )
        {
        StringBuilder result = new StringBuilder();
        List<String> L = listOfStrings( types );
        String rest = "rdf:nil";
        for (int i = L.size(); i > 0; i -= 1)
            {
            String current = base + i;
            result.append( "; " ).append( current ).append( " rdf:rest " ).append( rest );
            result.append( "; " ).append( current ).append( " rdf:first " ).append( L.get(i-1) );
            rest = current;
            }
        return result.toString();
        }

    public void testAddsSupertypes()
        {
        Model base = model( "a rdf:type T; T rdfs:subClassOf U" );
        Model schema = model( "T rdfs:subClassOf V" );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertIsoModels( model( "a rdf:type T; a rdf:type U; a rdf:type V; T rdfs:subClassOf U; T rdfs:subClassOf V" ), answer );
        }    
    public void testSubclassClosureA()
        {
        Model m = model( "A rdfs:subClassOf B; B rdfs:subClassOf C" );
        subClassClosure( m );
        assertIsoModels( model( "A rdfs:subClassOf B; B rdfs:subClassOf C; A rdfs:subClassOf C" ), m );
        }
    
    public void testSubclassClosureB()
        {
        Model m = model( "A rdfs:subClassOf B; B rdfs:subClassOf C; X rdfs:subClassOf C" );
        subClassClosure( m );
        assertIsoModels( model( "A rdfs:subClassOf B; B rdfs:subClassOf C; A rdfs:subClassOf C; X rdfs:subClassOf C" ), m );
        }
    
    public void testSubclassClosureC()
        {
        Model m = model( "A rdfs:subClassOf B; B rdfs:subClassOf C; X rdfs:subClassOf C; Y rdfs:subClassOf X" );
        subClassClosure( m );
        assertIsoModels( model( "A rdfs:subClassOf B; B rdfs:subClassOf C; A rdfs:subClassOf C; X rdfs:subClassOf C; Y rdfs:subClassOf X; Y rdfs:subClassOf C" ), m );
        }
    
    public void testSubclassClosureD()
        {
        Model m = model( "A rdfs:subClassOf B; B rdfs:subClassOf C; X rdfs:subClassOf C; Y rdfs:subClassOf X; U rdfs:subClassOf A; U rdfs:subClassOf Y" );
        subClassClosure( m );
        assertIsoModels( model( "A rdfs:subClassOf B; B rdfs:subClassOf C; A rdfs:subClassOf C; X rdfs:subClassOf C; Y rdfs:subClassOf X; Y rdfs:subClassOf C; U rdfs:subClassOf A; U rdfs:subClassOf B; U rdfs:subClassOf C; U rdfs:subClassOf X; U rdfs:subClassOf Y" ), m );
        }
    
    public void testSubclassClosureE()
        {
        Model m = model( "A rdfs:subClassOf B; B rdfs:subClassOf C" );
        subClassClosure( m );
        assertIsoModels( model( "A rdfs:subClassOf B; B rdfs:subClassOf C; A rdfs:subClassOf C" ), m );
        }

    protected void subClassClosure( Model m )
        { ModelExpansion.addSubClassClosure( m ); }
    }
