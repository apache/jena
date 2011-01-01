/*
 * (c) Copyright 2010 Epimrophics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tdb.tools;

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.Tuple ;
import org.openjena.atlas.logging.Log ;
import tdb.cmdline.ModLocation ;
import arq.cmdline.CmdGeneral ;

import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.bulkloader2.IndexFactory ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;


public class dumpbpt extends CmdGeneral
{
    ModLocation modLocation = new ModLocation() ;
    
    static public void main(String... argv)
    { 
        Log.setLog4j() ;
        new dumpbpt(argv).mainRun() ;
    }

    protected dumpbpt(String[] argv)
    {
        super(argv) ;
        super.addModule(modLocation) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        if ( modVersion.getVersionFlag() )
            modVersion.printVersionAndExit() ;
        if ( modLocation.getLocation() == null )
            cmdError("Location required") ;
        if ( super.getPositional().size() == 0 )
            cmdError("No index specified") ;
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" --loc=DIR IndexName" ;
    }

    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
    }
    
    @Override
    protected void exec()
    {
        List<String> tripleIndexes = Arrays.asList(Names.tripleIndexes) ;
        List<String> quadIndexes = Arrays.asList(Names.quadIndexes) ;
        Location loc = modLocation.getLocation() ;
        
        // The name is the order.
        for ( String indexName : super.getPositional() )
        {
            String primary ;
            if ( tripleIndexes.contains(indexName) )
            {
                primary = Names.primaryIndexTriples ;
            }
            else if ( quadIndexes.contains(indexName) )
            {
                primary = Names.primaryIndexQuads ;
            }
            else
            {
                cmdError("No such index: "+indexName) ;
                primary = null ; 
            }
            
            int keyLength = SystemTDB.SizeOfNodeId * indexName.length() ;
            int valueLength = 0 ;
            
            
            RangeIndex rIndex = IndexFactory.openBPT(loc, indexName, 
                                                     SystemTDB.BlockReadCacheSize,
                                                     SystemTDB.BlockWriteCacheSize,
                                                     keyLength, valueLength) ;
            BPlusTree bpt = (BPlusTree)rIndex ;
            
            if ( false )
            {
                System.out.println("---- Index structure") ;
                bpt.dump() ;
            }
            if ( true )
            {
                System.out.println("---- Index contents") ;
                Iterator<Record> iter = bpt.iterator() ;
                for ( ; iter.hasNext() ; )
                {
                    Record r = iter.next();
                    System.out.println(r) ;
                }
            }
            
            // Check.
            Iterator<Record> iterCheck = bpt.iterator() ;
            Record r1 = null ;
            int i = 0 ;
            for ( ; iterCheck.hasNext() ; )
            {
                Record r2 = iterCheck.next();
                i++ ;
                
                if ( r1 != null )
                {
                    if ( ! Record.keyLT(r1, r2) )
                    {
                        System.err.println("key error@ "+i) ;
                        System.err.println("  "+r1) ;
                        System.err.println("  "+r2) ;
                    }
                }
                r1 = r2 ;
            }
            
            if ( false )
            {
                // Dump in tuple order.
                TupleIndex tupleIndex = new TupleIndexRecord(primary.length(), new ColumnMap(primary, indexName), rIndex.getRecordFactory(), rIndex) ;
                if ( true )
                {
                    System.out.println("---- Tuple contents") ;
                    Iterator<Tuple<NodeId>> iter2 = tupleIndex.all() ;
                    for ( ; iter2.hasNext() ; )
                    {
                        Tuple<NodeId> row = iter2.next();
                        System.out.println(row) ;
                    }
                }
            }
        }
        
    }

}

/*
 * (c) Copyright 2010 Epimrophics Ltd.
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