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

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.shared.AbstractTestPrefixMapping;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
    Test that a model is a prefix mapping.
 	@author kers
*/
public class TestModelPrefixMapping extends AbstractTestPrefixMapping
    {
    public TestModelPrefixMapping( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestModelPrefixMapping.class ); }   

    @Override
    protected PrefixMapping getMapping()
        { return ModelFactory.createDefaultModel(); }       
    
    protected static final String alphaPrefix = "alpha";
    protected static final String betaPrefix = "beta";
    protected static final String alphaURI = "http://testing.jena.hpl.hp.com/alpha#";
    protected static final String betaURI = "http://testing.jena.hpl.hp.com/beta#";
    
    protected PrefixMapping baseMap = PrefixMapping.Factory.create()
        .setNsPrefix( alphaPrefix, alphaURI )
        .setNsPrefix( betaPrefix, betaURI );
    
    private PrefixMapping prevMap;
    
    public void setPrefixes()
        {
        prevMap = ModelCom.setDefaultModelPrefixes( baseMap );
        }
    
    public void restorePrefixes()
        {
        ModelCom.setDefaultModelPrefixes( prevMap );
        }
    
    /**
        Test that a freshly-created Model has the prefixes established by the
        default in ModelCom.
    */
    public void testDefaultPrefixes()
        {
        setPrefixes();
        Model m = ModelFactory.createDefaultModel();
        assertEquals( baseMap.getNsPrefixMap(), m.getNsPrefixMap() );
        restorePrefixes();
        }
    
    public void testOnlyFreshPrefixes()
        {
        setPrefixes();
        try { doOnlyFreshPrefixes(); } finally { restorePrefixes(); }
        }
    
    /**
       Test that existing prefixes are not over-ridden by the default ones.
    */
    private void doOnlyFreshPrefixes()
        { 
        String newURI = "abc:def/";
        Graph g = Factory.createDefaultGraph();
        PrefixMapping pm = g.getPrefixMapping();
        pm.setNsPrefix( alphaPrefix, newURI );
        Model m = ModelFactory.createModelForGraph( g );
        assertEquals( newURI, m.getNsPrefixURI( alphaPrefix ) );
        assertEquals( betaURI, m.getNsPrefixURI( betaPrefix ) ); }
    
    public void testGetDefault()
        { setPrefixes();
        try { assertSame( baseMap, ModelCom.getDefaultModelPrefixes() ); } 
        finally { restorePrefixes(); } }
    }
