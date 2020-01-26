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

package org.apache.jena.commonsrdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.RDF;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.commonsrdf.impl.JCR_BlankNode;
import org.apache.jena.commonsrdf.impl.JenaNode;
import org.junit.Test;

public class TestServiceLoaderCommonsRDF {
    
    @Test public void load_commons_rdf_1() {
        ServiceLoader<RDF> sl =  
          // Use this->classloader form : better for OSGi 
          ServiceLoader.load(RDF.class, this.getClass().getClassLoader()) ;
        List<RDF> providers = Iter.toList(sl.iterator());
        // The test have "simple" as well as the jena implementations.
        assertEquals(2, providers.size());
        RDF jenaRDF = providers.stream().filter(p->p.getClass().getName().contains("JenaRDF")).findFirst().get();
        BlankNode bnode = jenaRDF.createBlankNode();
        assertTrue(bnode instanceof JCR_BlankNode);
        org.apache.jena.graph.Node n = ((JenaNode)bnode).getNode();
        assertNotNull(n);
        assertNotNull(n.getBlankNodeLabel());
    }
}
