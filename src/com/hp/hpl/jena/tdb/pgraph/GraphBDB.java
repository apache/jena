/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.FileFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.file.ObjectFile;
import com.hp.hpl.jena.tdb.bdb.NodeTableBDB;
import com.hp.hpl.jena.tdb.bdb.SetupBDB;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;


/** Jena persistent graph implemented using BDB (Java Edition).  Node storage using an external file. */
public class GraphBDB extends PGraphBase
{
    private Transaction txn = null ;
    private SetupBDB config ;
 
    public GraphBDB(String dirname)
    {
        Location loc = Location.ensureDirectory(dirname) ;
        config = new SetupBDB(dirname) ;
        
        try {
            // BDB Btree for index, objects file for bulk node data. 
            ObjectFile objects = FileFactory.createObjectFileDisk(loc.getPath("nodes", "dat")) ;
            //ObjectFile objects = new ObjectFileSink() ;
            Database nodeToId = config.dbEnv.openDatabase(txn, "node2id", config.dbConfig);
            NodeTable nodeTable = new NodeTableBDB(config, nodeToId, objects) ;

            throw new TDBException("NOT IMPLEMENTED YET") ;
//            // ---- Triple indexes
//            Index3 spo = new TripleIndexBDB(config, mapSPO, "SPO") ;
//            Index3 pos = new TripleIndexBDB(config, mapPOS, "POS") ;
//            Index3 osp = new TripleIndexBDB(config, mapOSP, "OSP") ;
//            init(spo, pos, osp, nodeTable) ;
        } catch (DatabaseException ex)
        {
            throw new PGraphException(ex) ;
        }
    }

    @Override 
    public int graphBaseSize()
    {
        return -1 ;
//        try
//        {
//            return (int) indexSPO.count() ;
//        } catch (DatabaseException ex)
//        {
//            ex.printStackTrace();
//            throw new JenaException("GraphBDB.graphBaseSize", ex) ;
//        }
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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