/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.IndexFactoryBPlusTree;
import com.hp.hpl.jena.tdb.index.IndexFactoryBPlusTreeMem;
import com.hp.hpl.jena.tdb.index.IndexFactoryBTree;
import com.hp.hpl.jena.tdb.index.IndexFactoryBTreeMem;
import com.hp.hpl.jena.tdb.sys.Const;

/** Place to put various "making" explicitly for testing */

public class GraphTDBFactoryTest
{
    /** Create a graph backed with storage at a location using and BTree indexes (testing) */
    public static GraphTDB createBTree(Location location)
    { 
        IndexFactoryBTree idxFactory = new IndexFactoryBTree(Const.BlockSizeTest) ;
        IndexBuilder builder = new IndexBuilder(idxFactory,idxFactory) ; 
        return GraphTDBFactory.create(builder, location) ;
    }

    /** Create a graph backed with storage at a location using and BTree indexes (testing) */
    public static GraphTDB createBPlusTree(Location location)
    { 
        IndexFactoryBPlusTree idxFactory = new IndexFactoryBPlusTree(Const.BlockSizeTest) ;
        IndexBuilder builder = new IndexBuilder(idxFactory,idxFactory) ; 
        return GraphTDBFactory.create(builder, location) ;
    }

    /** Create a graph backed with storage and BTree indexes in-memory (testing) */
    public static GraphTDB createBTreeMem()
    { 
        IndexFactoryBPlusTreeMem idxFactory = new IndexFactoryBPlusTreeMem(Const.OrderMem) ;
        IndexBuilder builder = new IndexBuilder(idxFactory,idxFactory) ; 
        return GraphTDBFactory.createMem(builder) ;
    }
    
    /** Create a graph backed with storage and B+Tree indexes in-memory (testing) */
    public static GraphTDB createBPlusTreeMem()
    { 
        IndexFactoryBTreeMem idxFactory = new IndexFactoryBTreeMem(Const.OrderMem) ;
        IndexBuilder builder = new IndexBuilder(idxFactory,idxFactory) ; 
        return GraphTDBFactory.createMem(builder) ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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