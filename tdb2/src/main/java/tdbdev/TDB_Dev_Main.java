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
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.apache.thrift.protocol.TProtocol ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.tdb2.TDBFactory ;
import org.seaborne.tdb2.store.DatasetGraphTxn ;
import org.seaborne.tdb2.store.nodetable.TReadAppendFileTransport ;


public class TDB_Dev_Main {
    static { LogCtl.setLog4j(); }
    
    public static void main(String[] args) throws Exception {
        main1(args) ;
    }
    
    public static void main2(String[] args) throws Exception {
        String filename = "data" ;
        FileOps.delete(filename); 
        
        try ( TReadAppendFileTransport file = new TReadAppendFileTransport(filename) ) {
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
        
        try ( TReadAppendFileTransport file = new TReadAppendFileTransport(filename) ) {
            file.seek(0);
            TProtocol proto = TRDF.protocol(file) ;
            RDF_Term term = new RDF_Term() ;
            term.read(proto);
            Node n = ThriftConvert.convert(term) ;
            System.out.println("n = "+n) ;
        }
        
        System.exit (1) ;
        
        byte[] bytes = Bytes.string2bytes("Some text : "+new Date()+"\n") ; 
        try ( TReadAppendFileTransport file = new TReadAppendFileTransport(filename) ) {
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
            file.seek(41);
            details(file) ;

            byte[] bytes2 = new byte[bytes.length*2] ;
            int x = file.read(bytes2, 0, bytes2.length) ;
            String z = new String(bytes2, 0, x, StandardCharsets.UTF_8) ;
            System.out.println(z) ;
        }
    }
    
    private static void details(TReadAppendFileTransport file) {
        System.out.println("Len = "+file.getFileLength()) ;
        System.out.println("W   = "+file.getWriteLocation()) ;
        System.out.println("R   = "+file.getReadLocation()) ;
    }
    
    public static void main1(String[] args) {
        FileOps.ensureDir("DB"); 
        FileOps.clearDirectory("DB");
        Location location = Location.create("DB") ;
        
        
        
        //DatasetGraphTDB dsg = (DatasetGraphTDB)TDB2Builder.build(location) ;
        
        if ( false ) {
            String FILE = "/home/afs/Datasets/BSBM/bsbm-250k.nt.gz" ;
            System.out.println("Load "+FILE) ;
            Dataset ds = TDBFactory.createDataset(location) ;
            Timer timer = new Timer() ;
            ds.begin(ReadWrite.WRITE); 
            timer.startTimer();
            // Speed comparison with direct
            //DatasetGraph dsg = ((DatasetGraphTxn)ds.asDatasetGraph()).getDatasetTDB() ;
                
            RDFDataMgr.read(ds, FILE);
            ds.commit();
            ds.end(); 
            long time_ms = timer.endTimer() ;

            ds.begin(ReadWrite.READ) ;

            //RDFDataMgr.write(System.out,  dsg, Lang.TRIG) ;
            long x = Iter.count(ds.asDatasetGraph().find()) ;
            ds.end();
            System.out.printf("Count = %,d\n", x) ;
            double seconds = time_ms/1000.0 ; 

            System.out.printf("Rate = %,.0f\n", x/seconds) ;
            System.out.println("DONE") ;
            System.exit(0) ;
        }

        String FILE = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz" ;
        System.out.println("Load "+FILE) ;
        
        DatasetGraphTxn dsg = (DatasetGraphTxn)TDBFactory.createDatasetGraph(location) ;
        dsg.begin(ReadWrite.WRITE);
        
        DatasetGraph dsgx = dsg.getBaseDatasetGraph() ;
        //RDFDataMgr.read(dsg, "D.ttl");
        Timer timer = new Timer() ;
        timer.startTimer();
        RDFDataMgr.read(dsgx, FILE) ;
        dsg.commit();
        dsg.end(); 
        long time_ms = timer.endTimer() ;
        
        dsg.begin(ReadWrite.READ) ;
        
        //RDFDataMgr.write(System.out,  dsg, Lang.TRIG) ;
        long x = Iter.count(dsg.find()) ;
        dsg.end();
        System.out.printf("Count = %,d\n", x) ;
        double seconds = time_ms/1000.0 ; 
        System.out.printf("Rate = %,.0f\n", x/seconds) ;
        
        Dataset ds = TDBFactory.createDataset(location) ;
        String qs = "SELECT * { ?s ?p ?o } LIMIT 10" ;
        Query q = QueryFactory.create(qs) ;
        
        Txn.executeRead(dsg.getTransactional(), ()->{
            try ( QueryExecution qExec = QueryExecutionFactory.create(q, ds) ) {
                QueryExecUtils.executeQuery(qExec);
            }
        }); 
        
        System.out.println("DONE") ;
        System.exit(0) ;
        
        
        
//        FileSet idxFs1 = new FileSet(location, "index") ;
//        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
//        ComponentId cid = ComponentId.allocLocal() ;
//        RangeIndex rIdx = null ; //BPlusTreeFactory.makeBPlusTree(
//
//        BPlusTree x =(BPlusTree)rIdx ;
//        // XXX !!!!!
//        Log.warn(TDB_Dev_Main.class, "Ad-hoc memory journal");  
//        Journal journal = Journal.create(Location.mem()) ; 
//        Transactional trans = new TransactionalBase(journal, x) ;
//        trans.begin(ReadWrite.WRITE);
//        NodeTable nt = new NodeTableThrift(rIdx, location.getPath("data")) ;
//        Node n1 = SSE.parseNode("<http://example/>") ;
//        Node n2 = SSE.parseNode("<http://example/other>") ;
//        NodeId nid1 = nt.getAllocateNodeId(n1) ;
//        NodeId nid2 = nt.getAllocateNodeId(n2) ;
//        System.out.printf("nid1 = %s\n", nid1) ;
//        System.out.printf("nid2 = %s\n", nid2) ;
//        trans.commit();
//        System.exit(0) ;
    }

}

