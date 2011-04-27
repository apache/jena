/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.graph.AbstractTestGraph2 ;
import com.hp.hpl.jena.tdb.ConfigTest;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.junit.GraphLocation;

/** Programmatic tests on persistent graph */
public class TestGraphTDB extends AbstractTestGraph2
{
    static GraphLocation graphLocation = null ;
    
    @BeforeClass public static void beforeClass()
    {
        graphLocation = new GraphLocation(new Location(ConfigTest.getTestingDirDB())) ;
        graphLocation.release() ;
        graphLocation.clearDirectory() ;
        graphLocation.createGraph() ;
        graph = graphLocation.getGraph() ;
    }
    // ----------
    
    @AfterClass public static void afterClass()
    { 
        graphLocation.release() ;
        graphLocation.clearDirectory() ;
    }
    

    static Graph graph = null ;
    @Before public void before()
    { 
        graph.getBulkUpdateHandler().removeAll() ;
    }
            
            
    @After public void after()   
    { 
        
    }
    
    @Override
    protected Graph emptyGraph()
    {
        return graph ;
    }
    
    @Override
    protected void returnGraph(Graph g)
    {}
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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