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

package com.hp.hpl.jena.sparql.core;

import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;

public abstract class AbstractTestDataset extends BaseTest
{
    // Assumes a dadatset which need explicit add graph 
    protected abstract Dataset createFixed() ;
    
    static Model model1 = ModelFactory.createDefaultModel() ;
    static Model model2 = ModelFactory.createDefaultModel() ;
    
    static Resource s1 = model1.createResource("s1") ;
    static Resource s2 = model1.createResource("s2") ;

    static Property p1 = model1.createProperty("p1") ;
    static Property p2 = model1.createProperty("p2") ;
    
    static Resource o1 = model1.createResource("o1") ;
    static Resource o2 = model1.createResource("o2") ;
    
    static {
        model1.add(s1, p1, o1) ;
        model2.add(s2, p2, o2) ;
    }
    
    @Test public void dataset_01()
    {
        Dataset ds = createFixed() ;
        assertNotNull(ds.getDefaultModel()) ;
        assertNotNull(ds.asDatasetGraph()) ;
    }
    
    @Test public void dataset_02()
    {
        Dataset ds = createFixed() ;
        ds.getDefaultModel().add(s1,p1,o1) ;
        assertTrue(model1.isIsomorphicWith(ds.getDefaultModel())) ;
    }

    @Test public void dataset_03()
    {
        Dataset ds = createFixed() ;
        ds.setDefaultModel(model2) ;
        assertTrue(model2.isIsomorphicWith(ds.getDefaultModel())) ;
    }

    @Test public void dataset_04()
    {
        String graphName = "http://example/" ;
        Dataset ds = createFixed() ;
        ds.addNamedModel(graphName, model1) ;
        assertTrue(ds.containsNamedModel(graphName)) ;
        
        List<String> x = Iter.toList(ds.listNames()) ;
        assertEquals(1, x.size()) ;
        assertEquals(graphName, x.get(0)) ;
        
        assertFalse(model1.isIsomorphicWith(ds.getDefaultModel())) ;
        Model m = ds.getNamedModel(graphName) ;

        assertNotNull(m) ;
        assertTrue(model1.isIsomorphicWith(m)) ;
        
        ds.removeNamedModel(graphName) ;
        // Not getNamedModel and test for null as some dadasets are "auto graph creating"
        assertFalse(ds.containsNamedModel(graphName)) ;
    }

    @Test public void dataset_05()
    {
        String graphName = "http://example/" ;
        Dataset ds = createFixed() ;
        ds.addNamedModel(graphName, model1) ;
        ds.replaceNamedModel(graphName, model2) ;
        assertTrue(ds.containsNamedModel(graphName)) ;
        
        List<String> x = Iter.toList(ds.listNames()) ;
        assertEquals(1, x.size()) ;
        assertEquals(graphName, x.get(0)) ;
        
        assertFalse(model1.isIsomorphicWith(ds.getNamedModel(graphName))) ;
        assertTrue(model2.isIsomorphicWith(ds.getNamedModel(graphName))) ;
    }
}
