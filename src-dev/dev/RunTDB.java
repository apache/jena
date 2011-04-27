/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Date ;
import java.util.concurrent.TimeUnit ;

import org.openjena.atlas.logging.Log ;
import setup.DatasetBuilderStd ;
import setup.ObjectFileBuilder ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryCancelledException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrTracker ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

public class RunTDB
{
    static { Log.setLog4j() ; }
    static String divider = "----------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }
    
    static void exit(int rc)
    {
        System.out.println("EXIT") ;
        System.exit(rc) ;
    }
    

    public static class DSB2 extends DatasetBuilderStd
    {
        public DSB2()
        {
            super() ;
            ObjectFileBuilder objectFileBuilder         = new ObjectFileBuilderStd() ;
            BlockMgrBuilderStd blockMgrBuilder          = new BlockMgrBuilderStd()
            {
                @Override
                public BlockMgr buildBlockMgr(FileSet fileset, String name, int blockSize)
                {
                    BlockMgr bMgr = super.buildBlockMgr(fileset, name, blockSize) ;
                    return new BlockMgrTracker("DSB2", bMgr) ;
                }
            } ;
            
            IndexBuilderStd indexBuilder                = new IndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
            RangeIndexBuilderStd rangeIndexBuilder      = new RangeIndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
            
            NodeTableBuilderStd nodeTableBuilder        = new NodeTableBuilderStd(indexBuilder, objectFileBuilder) ;
            TupleIndexBuilderStd tupleIndexBuilder      = new TupleIndexBuilderStd(rangeIndexBuilder) ;
            set(nodeTableBuilder, tupleIndexBuilder, indexBuilder, rangeIndexBuilder, blockMgrBuilder, objectFileBuilder) ;
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        DatasetGraphTDB dsg = setup.TDBBuilder.build(new Location("DB")) ;
        
        //Dataset ds = TDBFactory.createDataset("DB") ;
        Dataset ds = TDBFactory.createDataset(dsg) ;
        Query query = QueryFactory.read("Q.rq") ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        System.out.println(new Date()) ;
        qExec.setTimeout(2000, TimeUnit.MILLISECONDS) ;
        try { ResultSetFormatter.out(qExec.execSelect()) ; } catch (QueryCancelledException ex) { System.out.println("Cancelled") ;}
        System.out.println(new Date()) ;
        exit(0) ;
    }
}
 
/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
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