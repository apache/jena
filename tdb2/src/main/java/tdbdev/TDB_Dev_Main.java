/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package tdbdev;

import java.nio.charset.StandardCharsets ;
import java.util.Date ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.* ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.thrift.TRDF ;
import org.apache.jena.riot.thrift.ThriftConvert ;
import org.apache.jena.riot.thrift.wire.RDF_Term ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.apache.thrift.protocol.TProtocol ;
import org.seaborne.dboe.base.file.BinaryDataFile ;
import org.seaborne.dboe.base.file.BinaryDataFileRandomAccess ;
import org.seaborne.dboe.base.file.BinaryDataFileWriteBuffered ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.TDBFactory ;
import org.seaborne.tdb2.lib.TDBTxn ;
import org.seaborne.tdb2.store.nodetable.TReadAppendFileTransport ;

public class TDB_Dev_Main {
    static { LogCtl.setLog4j(); }
    
    public static void main(String[] args) throws Exception {
        load() ;
    }
    
    public static void main2(String[] args) throws Exception {
        String filename = "data" ;
        FileOps.delete(filename); 
        
        BinaryDataFile binfile = new BinaryDataFileRandomAccess(filename) ;
        
        binfile = new BinaryDataFileWriteBuffered(binfile) ;
        binfile.open();
        
        try ( TReadAppendFileTransport file = new TReadAppendFileTransport(binfile) ) {
            TProtocol proto = TRDF.protocol(file) ;
            Node node = SSE.parseNode("<http://example/>") ;
            RDF_Term term = ThriftConvert.convert(node, true) ;
            term.write(proto);
            term.write(proto);
            term.write(proto);
            term.write(proto);
            term.write(proto);
            term.write(proto);
            term.write(proto);
            term.write(proto);
            term.write(proto);
            term.write(proto);
        }
        
        binfile = new BinaryDataFileRandomAccess(filename) ;
        binfile = new BinaryDataFileWriteBuffered(binfile) ;
        binfile.open();

        
        try ( TReadAppendFileTransport file = new TReadAppendFileTransport(binfile) ) {
            file.readPosition(0);
            TProtocol proto = TRDF.protocol(file) ;
            RDF_Term term = new RDF_Term() ;
            term.read(proto);
            Node n = ThriftConvert.convert(term) ;
            System.out.println("n = "+n) ;
            term.read(proto);
            n = ThriftConvert.convert(term) ;
            System.out.println("n = "+n) ;
        }
        
        System.exit (1) ;
        
        binfile = new BinaryDataFileRandomAccess(filename) ;
        binfile = new BinaryDataFileWriteBuffered(binfile) ;
        binfile.open();

        byte[] bytes = Bytes.string2bytes("Some text : "+new Date()+"\n") ; 
        try ( TReadAppendFileTransport file = new TReadAppendFileTransport(binfile) ) {
            details(file) ;
            System.out.println("truncate") ;
            file.truncate(41) ;
            details(file) ;
            
            System.out.println("write") ;
            file.write(bytes);
            details(file) ;

//            System.out.println("flush") ;
//            file.flush() ;
//            details(file) ;

            System.out.println("seek") ;
            file.readPosition(41);
            details(file) ;

            byte[] bytes2 = new byte[bytes.length*2] ;
            int x = file.read(bytes2, 0, bytes2.length) ;
            String z = new String(bytes2, 0, x, StandardCharsets.UTF_8) ;
            System.out.println(z) ;
        }
    }
    
    private static void details(TReadAppendFileTransport file) {
        System.out.println("Len = "+file.getBinaryDataFile().length()) ;
        System.out.println("W   = "+file.getBinaryDataFile().length()) ;
        System.out.println("R   = "+file.readPosition()) ;
    }
    
    public static void query() {
        Location location = Location.create("DB") ;
        String x = "<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType15>" ;
        String qs = "SELECT * { VALUES ?s {"+x+"} ?s ?p ?o }" ;
        Dataset ds = TDBFactory.createDataset(location) ;
        query(ds, qs) ;
        System.out.println("DONE") ;
        System.exit(0) ;
    }        
    
    public static void load() {
        Location location = Location.create("DB") ;
        FileOps.ensureDir("DB"); 
        FileOps.clearDirectory("DB");
        Dataset ds = TDBFactory.createDataset(location) ;
        String FILE = "/home/afs/Datasets/BSBM/bsbm-50k.nt.gz" ;
        
        long time_ms = -1 ;

        System.out.println("Load "+FILE) ;

        Timer timer = new Timer() ;
        timer.startTimer();
        TDBTxn.executeWrite(ds, ()->RDFDataMgr.read(ds, FILE)) ;
        time_ms = timer.endTimer() ;
        System.out.println("Load finish: "+Timer.timeStr(time_ms)+"s") ;    
        long x = TDBTxn.executeReadReturn(ds, () -> {
            System.out.println("Read start") ;
            return Iter.count(ds.asDatasetGraph().find()) ;
        }) ;
        System.out.printf("Count = %,d\n", x) ;
        if ( time_ms > 0 ) {
            double seconds = time_ms/1000.0 ; 
            System.out.printf("Rate = %,.0f TPS\n", x/seconds) ;
        }
        
        String qs = "SELECT * { ?s ?p ?o } LIMIT 10" ;
        query(ds, qs) ;
        System.out.println("DONE") ;
        System.exit(0) ;
    }
    
    public static void query(Dataset ds, String queryString) {
        Query q = QueryFactory.create(queryString) ;
        TDBTxn.executeRead(ds, ()->{
            try ( QueryExecution qExec = QueryExecutionFactory.create(q, ds) ) {
                QueryExecUtils.executeQuery(qExec);
            }
        }); 
    }

}

