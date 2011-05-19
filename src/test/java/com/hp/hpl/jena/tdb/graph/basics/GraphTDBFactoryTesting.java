/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph.basics;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.IndexBuilder ;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryBPlusTree ;
import com.hp.hpl.jena.tdb.store.GraphTriplesTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** Place to put various "making" explicitly for testing */

class GraphTDBFactoryTesting
{
    // This class and FactoryGraphTDB are old ways of making graphs and datasets 
    
    /** Create a graph backed with storage at a location using B+Tree indexes (testing) */
    public static GraphTriplesTDB createBPlusTree(Location location)
    { 
        IndexFactoryBPlusTree idxFactory = new IndexFactoryBPlusTree(SystemTDB.BlockSizeTest) ;
        IndexBuilder builder = new IndexBuilder(idxFactory,idxFactory) ; 
        return FactoryGraphTDB.createGraph(builder, location) ;
    }

    /** Create a graph backed with storage and B+Tree indexes in-memory (testing) */
    public static GraphTriplesTDB createBPlusTreeMem()
    { 
        IndexFactoryBPlusTree idxFactory = new IndexFactoryBPlusTree(SystemTDB.BlockSizeTestMem) ;
        IndexBuilder builder = new IndexBuilder(idxFactory,idxFactory) ; 
        return FactoryGraphTDB.createGraphMem(builder) ;
    }
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