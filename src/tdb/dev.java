/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import static com.hp.hpl.jena.tdb.sys.Names.tripleIndexes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.tdb.base.block.BlockMgrMem;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.store.BulkLoader;
import com.hp.hpl.jena.tdb.store.FactoryGraphTDB;
import com.hp.hpl.jena.tdb.store.GraphTDB;
import com.hp.hpl.jena.tdb.store.GraphTriplesTDB;
import com.hp.hpl.jena.tdb.store.NodeTable;
import com.hp.hpl.jena.tdb.store.NodeTableFactory;
import com.hp.hpl.jena.tdb.store.TripleTable;

/** Misc develop testing - not stable */
public class dev
{
    public static void main(String ... args) throws IOException
    {
        // FileOps.clearDirectory("DB") ;
        // tdbloader("--tdb=tdb.ttl", "/home/afs/Datasets/MusicBrainz/artists.nt") ;
        
        GraphTDB g = setup() ;
        BulkLoader b = new BulkLoader(g, true) ;
        List<String> files = new ArrayList<String>() ;
        files.add("/home/afs/Datasets/MusicBrainz/artists.nt") ;
        b.load(files) ;
        System.exit(0) ;
    }
    
    private static GraphTDB setup()
    {
        // Setup a graph - for experimental alternatives.
        BlockMgrMem.SafeMode = false ;
        IndexBuilder indexBuilder = IndexBuilder.mem() ;
        Location location = null ;
        
        NodeTable nodeTable = NodeTableFactory.create(indexBuilder, location) ;
        
        TripleTable table = FactoryGraphTDB.createTripleTable(indexBuilder, nodeTable, location, tripleIndexes) ; 
        ReorderTransformation transform = ReorderLib.identity() ;
        GraphTDB g = new GraphTriplesTDB(table, transform, location) ;
        return g ;
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