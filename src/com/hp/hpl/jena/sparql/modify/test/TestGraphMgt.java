/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.test;

import com.hp.hpl.jena.sparql.AlreadyExists;
import com.hp.hpl.jena.sparql.DoesNotExist;
import com.hp.hpl.jena.sparql.modify.op.*;
import com.hp.hpl.jena.sparql.util.GraphUtils;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;


public class TestGraphMgt extends TestUpdateBase
{
    static final String graphIRI = "http://example/graph" ;
    
    public void testCreateDrop1()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        Update u = new UpdateCreate(graphIRI) ;
        u.exec(gStore) ;
        assertTrue(gStore.containsNamedGraph(graphIRI)) ;
        assertTrue(graphEmpty(gStore.getNamedGraph(graphIRI))) ;

        try {
            u.exec(gStore) ;
            fail() ;
        } catch (AlreadyExists ex) {}
        
        u = new UpdateDrop(graphIRI) ;
        u.exec(gStore) ;
        assertFalse(gStore.containsNamedGraph(graphIRI)) ;
        
        try {
            u.exec(gStore) ;
            fail() ;
        } catch (DoesNotExist ex) {}
        
    }

    public void testCreateDrop2()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        Update u = new UpdateCreate(graphIRI) ;
        u.exec(gStore) ;
        
        u = new UpdateCreate(graphIRI, true) ;
        u.exec(gStore) ;
        
        assertTrue(gStore.containsNamedGraph(graphIRI)) ;
        assertTrue(graphEmpty(gStore.getNamedGraph(graphIRI))) ;
        
        u = new UpdateDrop(graphIRI) ;
        u.exec(gStore) ;
        assertFalse(gStore.containsNamedGraph(graphIRI)) ;
        u = new UpdateDrop(graphIRI, true) ;
        u.exec(gStore) ;
    }
    
    public void testCreateDrop3()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        script(gStore, "create-1.ru") ;
        assertTrue(gStore.containsNamedGraph(graphIRI)) ;
        assertTrue(graphEmpty(gStore.getNamedGraph(graphIRI))) ;
    }

    public void testCreateDrop4()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.addNamedGraph(graphIRI, GraphUtils.makeDefaultGraph()) ;
        script(gStore, "drop-1.ru") ;
        assertFalse(gStore.containsNamedGraph(graphIRI)) ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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