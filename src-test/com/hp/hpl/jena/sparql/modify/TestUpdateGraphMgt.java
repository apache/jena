/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.modify.request.UpdateCreate ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.GraphStoreFactory ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateException ;

public abstract class TestUpdateGraphMgt extends TestUpdateBase
{
    static final Node graphIRI = Node.createURI("http://example/graph") ;
    
    @Test public void testCreateDrop1()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        Update u = new UpdateCreate(graphIRI) ;
        UpdateAction.execute(u, gStore) ;
        assertTrue(gStore.containsGraph(graphIRI)) ;
        assertTrue(graphEmpty(gStore.getGraph(graphIRI))) ;

        // try again - should fail (already exists)
        try {
            UpdateAction.execute(u, gStore) ;
            fail() ;
        } catch (UpdateException ex) {}

        // Drop it.
        u = new UpdateDrop(graphIRI) ;
        UpdateAction.execute(u, gStore) ;
        assertFalse(gStore.containsGraph(graphIRI)) ;
        
        // Drop it again. - should fail
        try {
            UpdateAction.execute(u, gStore) ;
            fail() ;
        } catch (UpdateException ex) {}
        
    }

    @Test public void testCreateDrop2()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
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
        GraphStore gStore = GraphStoreFactory.create() ;
        script(gStore, "create-1.ru") ;
        assertTrue(gStore.containsGraph(graphIRI)) ;
        assertTrue(graphEmpty(gStore.getGraph(graphIRI))) ;
    }

    @Test public void testCreateDrop4()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.addGraph(graphIRI, GraphFactory.createDefaultGraph()) ;
        script(gStore, "drop-1.ru") ;
        assertFalse(gStore.containsGraph(graphIRI)) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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