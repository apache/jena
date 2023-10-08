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

package tdb.tools ;

import java.io.PrintStream ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Objects ;

import arq.cmdline.CmdARQ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.base.record.Record;
import org.apache.jena.tdb1.base.record.RecordFactory;
import org.apache.jena.tdb1.index.IndexFactory;
import org.apache.jena.tdb1.index.RangeIndex;
import org.apache.jena.tdb1.index.bplustree.BPlusTree;
import org.apache.jena.tdb1.lib.ColumnMap;
import org.apache.jena.tdb1.store.NodeId;
import org.apache.jena.tdb1.store.tupletable.TupleIndex;
import org.apache.jena.tdb1.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb1.sys.Names;
import org.apache.jena.tdb1.sys.SystemTDB;
import tdb.cmdline.ModLocation ;

public class dumpbpt extends CmdARQ {
    ModLocation modLocation = new ModLocation() ;

    static public void main(String... argv) {
        LogCtl.setLogging();
        new dumpbpt(argv).mainRun() ;
    }

    protected dumpbpt(String[] argv) {
        super(argv) ;
        super.addModule(modLocation) ;
    }

    @Override
    protected void processModulesAndArgs() {
        if ( modVersion.getVersionFlag() )
            modVersion.printVersionAndExit() ;
        if ( modLocation.getLocation() == null )
            cmdError("Location required") ;
        if ( super.getPositional().size() == 0 )
            cmdError("No index specified") ;
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " --loc=DIR IndexName" ;
    }

    @Override
    protected String getCommandName() {
        return Lib.className(this) ;
    }

    @Override
    protected void exec() {
        List<String> tripleIndexes = Arrays.asList(Names.tripleIndexes) ;
        List<String> quadIndexes = Arrays.asList(Names.quadIndexes) ;
        Location loc = modLocation.getLocation() ;

        // The name is the order.
        for ( String indexName : super.getPositional() ) {
            String primary ;

            if ( indexName.length() == 3 ) {
                primary = Names.primaryIndexTriples ;
            } else if ( indexName.length() == 4 ) {
                primary = Names.primaryIndexQuads ;
            } else if ( Objects.equals(indexName, Names.indexNode2Id) ) {
                primary = Names.indexNode2Id;
            } else {
                cmdError("Wrong length: " + indexName) ;
                primary = null ;
            }

            //prefix2id
            //prefixIdx : GPU

            int keySubLen = SystemTDB.SizeOfNodeId ;
            int keyUnitLen = indexName.length() ;
            int keyLength = keySubLen * keyUnitLen ;
            int valueLength = 0 ;

            // Node table indexes.
            if ( Objects.equals(indexName, Names.indexNode2Id) || Objects.equals(indexName, Names.prefixNode2Id) ) {
                keySubLen = SystemTDB.LenNodeHash;
                keyUnitLen = 1 ;
                keyLength = SystemTDB.LenNodeHash;
                valueLength = SystemTDB.SizeOfNodeId;
            }
            // Prefixes
            if ( Objects.equals(indexName, Names.indexPrefix) ) {
                primary = Names.primaryIndexPrefix;
            }

            RecordFactory rf = new RecordFactory(keyLength, valueLength) ;
            RangeIndex rIndex = IndexFactory.buildRangeIndex(loc, indexName, rf) ;
            BPlusTree bpt = (BPlusTree)rIndex ;

            if ( false ) {
                System.out.println("---- Index structure") ;
                bpt.dump() ;

            }
            if ( true ) {
                System.out.println("---- Index contents") ;
                Iterator<Record> iter = bpt.iterator() ;
                if ( !iter.hasNext() )
                    System.out.println("<<Empty>>") ;

                for ( ; iter.hasNext() ; ) {
                    Record r = iter.next() ;
                    printRecord("", System.out, r, keyUnitLen) ;
                }
            }

            // Check.
            Iterator<Record> iterCheck = bpt.iterator() ;
            Record r1 = null ;
            int i = 0 ;
            for ( ; iterCheck.hasNext() ; ) {
                Record r2 = iterCheck.next() ;
                i++ ;

                if ( r1 != null ) {
                    if ( !Record.keyLT(r1, r2) ) {
                        System.err.println("key error@ " + i) ;
                        printRecord("  ", System.err, r1, keyUnitLen) ;
                        printRecord("  ", System.err, r2, keyUnitLen) ;
                    }
                }
                r1 = r2 ;
            }

            if ( false ) {
                // Dump in tuple order.
                TupleIndex tupleIndex = new TupleIndexRecord(primary.length(), new ColumnMap(primary, indexName), indexName,
                                                             rIndex.getRecordFactory(), rIndex) ;
                if ( true ) {
                    System.out.println("---- Tuple contents") ;
                    Iterator<Tuple<NodeId>> iter2 = tupleIndex.all() ;
                    if ( !iter2.hasNext() )
                        System.out.println("<<Empty>>") ;

                    for ( ; iter2.hasNext() ; ) {
                        Tuple<NodeId> row = iter2.next() ;
                        System.out.println(row) ;
                    }
                }
            }
        }
    }

    private static void printRecord(String label, PrintStream out, Record r, int keyUnitLen) {
        // out.println(r) ;

        int keySubLen = r.getKey().length / keyUnitLen ;
        if ( label != null )
            out.print(label) ;
        for ( int i = 0 ; i < keyUnitLen ; i++ ) {
            if ( i != 0 )
                out.print(" ") ;

            // Print in chunks
            int k = i * keySubLen ;
            for ( int j = k ; j < k + keySubLen ; j++ )
                out.printf("%02X", r.getKey()[j]) ;

            // long x = Bytes.getLong(r.getKey(), i*SystemTDB.SizeOfNodeId) ;
            // System.out.printf("%016X", x) ;
        }

        if ( r.getValue() != null &&  r.getValue().length != 0 ) {
            out.print(" -> ");
            String s = Bytes.asHexUC(r.getValue());
            out.print(s);
        }



        out.println() ;
    }
}
