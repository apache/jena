/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package perf;


import atlas.lib.FileOps;
import atlas.lib.RandomLib;
import atlas.logging.Log;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.util.Timer;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.block.BlockMgrCache;
import com.hp.hpl.jena.tdb.base.block.BlockMgrDirect;
import com.hp.hpl.jena.tdb.base.block.FileMode;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class MultipleNamedGraphs
{
    public static void main(String ... args)
    {
        Log.setLog4j() ;
        if ( false )
        {
            Log.enable(BlockMgrDirect.class) ;
            Log.enable(BlockMgrCache.class) ;
        }
        FileOps.clearDirectory("DB") ;
        if ( false )
            SystemTDB.setFileMode(FileMode.mapped) ;
        
        int SYNC_INTERVAL = 1000 ; 
   
        Timer timer = new Timer() ;
        
        //Dataset ds = TDBFactory.createDataset() ;
        Dataset ds = TDBFactory.createDataset("DB") ;
        
        int N = 5*1000 ;
        timer.startTimer() ;
        for ( int i = 0 ; i < N ; i++ )
        {
            if ( i != 0 && i%1000 == 0)
                System.out.println("N= "+i) ;
            
            String uri = "http://example/graph/"+i ;
            Model m = ds.getNamedModel(uri) ;
            addSomeContent(m,i) ;

            if (i >0 && i % SYNC_INTERVAL == 0) 
            {
                long x = timer.readTimer() ;
                //System.out.printf("-- sync %.2fs\n", x/1000.0) ;
                TDB.sync(ds);
            }
        }
        TDB.sync(ds);
        long ms = timer.endTimer() ;
        System.out.printf("Time = %.2fs\n", ms/1000.0) ;
        System.out.printf("Sync interval: %d\n", SYNC_INTERVAL) ;
    }

    private static void addSomeContent(Model m, int i)
    {
        int N = RandomLib.qrandom.nextInt(10) + 10 ;
        for ( int j = 0 ; j < N ; j++ )
        {
            Resource r = m.createResource("http://example/r"+j) ;
            r.addProperty(m.createProperty("http://example/p"+j), "123") ;
        }
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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