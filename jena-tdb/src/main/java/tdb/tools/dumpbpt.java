/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tdb.tools;

import java.io.PrintStream ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.atlas.logging.LogCtl ;
import tdb.cmdline.ModLocation ;
import arq.cmdline.CmdGeneral ;

import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.IndexFactory ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;


public class dumpbpt extends CmdGeneral
{
    ModLocation modLocation = new ModLocation() ;
    
    static public void main(String... argv)
    { 
        LogCtl.setLog4j() ;
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
            if ( indexName.length() == 3 )
            {
                primary = Names.primaryIndexTriples ;
            }
            else if ( indexName.length() == 4 )
            {
                primary = Names.primaryIndexQuads ;
            }
            else
            {
                cmdError("Wrong length: "+indexName) ;
                primary = null ; 
            }
            
            int keySubLen =  SystemTDB.SizeOfNodeId ;
            int keyUnitLen = indexName.length() ;
            int keyLength = keySubLen*keyUnitLen ;
            int valueLength = 0 ;
            
            
            RecordFactory rf = new RecordFactory(keyLength, valueLength) ;
            RangeIndex rIndex = IndexFactory.buildRangeIndex(loc, indexName, rf) ;
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
                if ( ! iter.hasNext() )
                    System.out.println("<<Empty>>") ;
                
                for ( ; iter.hasNext() ; )
                {
                    Record r = iter.next();
                    printRecord("", System.out, r, keyUnitLen) ;
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
                        printRecord("  ", System.err, r1, keyUnitLen) ;
                        printRecord("  ", System.err, r2, keyUnitLen) ;
                    }
                }
                r1 = r2 ;
            }
            
            if ( false )
            {
                // Dump in tuple order.
                TupleIndex tupleIndex = new TupleIndexRecord(primary.length(), new ColumnMap(primary, indexName), indexName, rIndex.getRecordFactory(), rIndex) ;
                if ( true )
                {
                    System.out.println("---- Tuple contents") ;
                    Iterator<Tuple<NodeId>> iter2 = tupleIndex.all() ;
                    if ( ! iter2.hasNext() )
                        System.out.println("<<Empty>>") ;

                    for ( ; iter2.hasNext() ; )
                    {
                        Tuple<NodeId> row = iter2.next();
                        System.out.println(row) ;
                    }
                }
            }
        }
    }
    
    private static void printRecord(String label, PrintStream out, Record r, int keyUnitLen)
    {
        //out.println(r) ;

        int keySubLen = r.getKey().length/keyUnitLen ;
        if ( label != null )
            out.print(label) ;
        for ( int i = 0 ; i < keyUnitLen ; i++ )
        {   
            if ( i != 0 )
                out.print(" ") ;
            
            // Print in chunks
            int k = i*keySubLen ;
            for ( int j = k ; j < k+keySubLen ; j++ )
                out.printf("%02x", r.getKey()[j]) ;
            
//            long x = Bytes.getLong(r.getKey(), i*SystemTDB.SizeOfNodeId) ;
//            System.out.printf("%016x", x) ;
        }
        out.println() ;
    }
}
