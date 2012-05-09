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
import com.hp.hpl.jena.shared.ReificationStyle;

import junit.framework.*;

/**
    test the properties required of ReifiedStatement objects.
    @author kers 
*/
public class TestReifiedStatements extends ModelTestBase
    {
    public TestReifiedStatements( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { TestSuite result = new TestSuite();
        result.addTest( new TestSuite( TestStandard.class ) );
        result.addTest( new TestSuite( TestConvenient.class ) );
        result.addTest( new TestSuite( TestMinimal.class ) );
        return result; }   
        
    public Model getModel()
        { return ModelFactory.createDefaultModel(); }
        
    public static class TestStandard extends AbstractTestReifiedStatements
        {
        public TestStandard( String name ) { super( name ); }
        public static final ReificationStyle style = ModelFactory.Standard;
        @Override
        public Model getModel() { return ModelFactory.createDefaultModel( style ); } 
        public void testStyle() { assertEquals( style, getModel().getReificationStyle() ); }
        }
        
    public static class TestConvenient extends AbstractTestReifiedStatements
        {
        public TestConvenient( String name ) { super( name ); }
        public static final ReificationStyle style = ModelFactory.Convenient;
        @Override
        public Model getModel() { return ModelFactory.createDefaultModel( style ); } 
        public void testStyle() { assertEquals( style, getModel().getReificationStyle() ); }
        }
        
    public static class TestMinimal extends AbstractTestReifiedStatements
        {
        public TestMinimal( String name ) { super( name ); }
        public static final ReificationStyle style = ModelFactory.Minimal;
        @Override
        public Model getModel() { return ModelFactory.createDefaultModel( style); } 
        public void testStyle() { assertEquals( style, getModel().getReificationStyle() ); }
        }
    }
