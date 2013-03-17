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

package com.hp.hpl.jena.sparql.modify;

import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.modify.request.UpdateCreate ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateException ;

public abstract class AbstractTestUpdateGraphMgt extends AbstractTestUpdateBase
{
    static final Node graphIRI = NodeFactory.createURI("http://example/graph") ;
    
    @Test public void testCreateDrop1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        Update u = new UpdateCreate(graphIRI) ;
        UpdateAction.execute(u, gStore) ;
        assertTrue(gStore.containsGraph(graphIRI)) ;
        assertTrue(graphEmpty(gStore.getGraph(graphIRI))) ;

        // With "auto SILENT" then these aren't errors.
        boolean silentMode = true ;
        
        if ( ! silentMode )
        {
            // try again - should fail (already exists)
            try {
                UpdateAction.execute(u, gStore) ;
                fail() ;
            } catch (UpdateException ex) {}
        }
        
        // Drop it.
        u = new UpdateDrop(graphIRI) ;
        UpdateAction.execute(u, gStore) ;
        assertFalse(gStore.containsGraph(graphIRI)) ;
        
        if ( ! silentMode )
        {
            // Drop it again. - should fail
            try {
                UpdateAction.execute(u, gStore) ;
                fail() ;
            } catch (UpdateException ex) {}
        }
        
    }

    @Test public void testCreateDrop2()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        Update u = new UpdateCreate(graphIRI) ;
        UpdateAction.execute(u, gStore) ;
        
        u = new UpdateCreate(graphIRI, true) ;
        UpdateAction.execute(u, gStore) ;
        
        assertTrue(gStore.containsGraph(graphIRI)) ;
        assertTrue(graphEmpty(gStore.getGraph(graphIRI))) ;
        
        u = new UpdateDrop(graphIRI) ;
        UpdateAction.execute(u, gStore) ;
        assertFalse(gStore.containsGraph(graphIRI)) ;
        u = new UpdateDrop(graphIRI, true) ;
        UpdateAction.execute(u, gStore) ;

    }
    
    @Test public void testCreateDrop3()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "create-1.ru") ;
        assertTrue(gStore.containsGraph(graphIRI)) ;
        assertTrue(graphEmpty(gStore.getGraph(graphIRI))) ;
    }

    @Test public void testCreateDrop4()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        gStore.addGraph(graphIRI, GraphFactory.createDefaultGraph()) ;
        script(gStore, "drop-1.ru") ;
        assertFalse(gStore.containsGraph(graphIRI)) ;
    }
}
