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
import java.util.Iterator ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.base.record.Record ;
import org.apache.jena.tdb.base.record.RecordFactory ;
import org.apache.jena.tdb.index.IndexFactory ;
import org.apache.jena.tdb.index.RangeIndex ;
import org.apache.jena.tdb.index.bplustree.BPlusTree ;
import org.apache.jena.tdb.sys.SystemTDB ;
import tdb.cmdline.ModLocation ;
import arq.cmdline.CmdGeneral ;

public class dumpbpt extends CmdGeneral {
    ModLocation modLocation = new ModLocation() ;

    static public void main(String... argv) {
        LogCtl.setLog4j() ;
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
        Location loc = modLocation.getLocation() ;

        // The name is the order.
        for ( String indexName : super.getPositional() ) {

            int keySubLen = SystemTDB.SizeOfNodeId ;
            int keyUnitLen = indexName.length() ;
            int keyLength = keySubLen * keyUnitLen ;
            int valueLength = 0 ;

            RecordFactory rf = new RecordFactory(keyLength, valueLength) ;
            RangeIndex rIndex = IndexFactory.buildRangeIndex(loc, indexName, rf) ;
            BPlusTree bpt = (BPlusTree)rIndex ;

            
            System.out.println("---- Index contents") ;
            Iterator<Record> iter = bpt.iterator() ;
            if ( !iter.hasNext() )
                System.out.println("<<Empty>>") ;

            for ( ; iter.hasNext() ; ) {
                Record r = iter.next() ;
                printRecord("", System.out, r, keyUnitLen) ;
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
                out.printf("%02x", r.getKey()[j]) ;

            // long x = Bytes.getLong(r.getKey(), i*SystemTDB.SizeOfNodeId) ;
            // System.out.printf("%016x", x) ;
        }
        out.println() ;
    }
}
