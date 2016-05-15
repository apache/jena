/*
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

import java.io.* ;
import java.util.ArrayList ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.function.Predicate ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.DateTimeUtils ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.* ;
import org.apache.jena.sparql.sse.SSE ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.transaction.TransactionalMonitor ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.tdb2.TDB2Factory ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import txnlog.ChangeLogApply ;
import txnlog.ThriftChangeLog ;
import txnlog.TxnLoggerThrift ;
import txnlog.notused.DatasetGraphMonitorTxn ;

public class DevTxnLogger {
    static { LogCtl.setLog4j(); } 
    
    // Add transaction lifecycle
    // ==> DatasetLifecycle (UseCycle)
    // No? Just start/finish enough.
    interface DatasetActions extends DatasetChanges {
        /** Indicator that a sequence of changes is about to start */ 
        @Override public void start() ;
        
        /** A change has occurred 
         * @see QuadAction 
         */
        @Override public void change(QuadAction qaction, Node g, Node s, Node p, Node o) ;
         
        /** Indicator that a sequence of changes has now finished */
        @Override public void finish() ;
        
        /** Release any resources */
        @Override public void reset() ;
        
//        public void txnBegin(Txn id, ReadWrite mode) ;
//        public void txnPromote() ;
//        public void txnCommit() ;
//        public void txnAbort() ;
//        public void txnEnd() ;
//            
//        default void startBegin(ReadWrite mode)     {}
//        default void finishBegin(ReadWrite mode)    {}
//
//        default void startPromote()     {}
//        default void finishPromote()    {}
//
//        default void startCommit()      {}
//        default void finishCommit()     {}
//
//        default void startAbort()       {}
//        default void finishAbort()      {}
//
//        default void startEnd()         {}
//        default void finishEnd()        {}
        
    }
    
    interface DatasetChangesFactory {
        DatasetChanges make(OutputStream logStream) ;
    }
    
    // Turn on and off transaction logging.
    
    static class TransactionalMonitorLogging implements TransactionalMonitor {
        private static Logger LOG = LoggerFactory.getLogger(TransactionalMonitorLogging.class) ;
        private final Location location;
        private final String basename = "txn" ;
        private final DatasetGraphTDB dsg ; 
        private OutputStream logStream = null ; 
        private Thread txnThread = null ;
        private DatasetChanges changes = null ; 
        private String uniqueFilename = null ;

        public TransactionalMonitorLogging(DatasetGraphTDB dsg, Location location) {
            this.dsg = dsg ;
            this.location = location ;
        }
        
        @Override public void startBegin(ReadWrite mode) {
            if ( mode == ReadWrite.WRITE ) {
                logStream = logFile() ;
                DatasetChangesFactory dsgChangeFactory = (x) ->  {
                    DatasetChanges changes = new ThriftChangeLog(x) ;
                    DatasetGraphMonitorTxn dsg = new DatasetGraphMonitorTxn(this.dsg, changes, false) ;
                    return changes ;
                } ;
                changes = dsgChangeFactory.make(logStream) ;
                dsg.setMonitor(changes);
                txnThread = Thread.currentThread() ;
                changes.start(); 
            }
        }
        
        
//        @Override public void finishBegin(ReadWrite mode)    {}

//        @Override public void startPromote()     {}
//        @Override public void finishPromote()    {}
//
//        @Override public void startCommit()      {}
//        @Override public void finishCommit()     {}
//
//        @Override public void startAbort()       {}
//        @Override public void finishAbort()      {}
//
//        @Override public void startEnd()         { }
        @Override public void finishEnd()        { 
            if ( txnThread == Thread.currentThread() ) {
                LOG.info("Finish: "+uniqueFilename); 
                changes.finish(); 
                IO.close(logStream) ;
                changes = null ;
                txnThread = null ;
            }
        }
        
        private Map<String, ByteArrayOutputStream> memDir = new LinkedHashMap<>() ;
        private List<String> files = new ArrayList<>() ;
        
        public List<String> getFiles() {
            if ( location.isMem() ) 
                return null ;
            return files ;
        }
        
        public Map<String, ByteArrayOutputStream> getMem() {
            if ( ! location.isMem() ) 
                return null ;
            return memDir ;
        }

        private OutputStream logFile() { 
            String timestamp = DateTimeUtils.nowAsString("yyyy-MM-dd_HH-mm-ss") ;
            String filename =  String.format("%s-%s", basename, timestamp) ;
            if ( location.isMem() ) {
                uniqueFilename = chooseUniqueFileName(filename, memDir::containsKey) ;
                LOG.info("Start: mem:"+uniqueFilename) ;
                ByteArrayOutputStream out = new ByteArrayOutputStream() ;
                memDir.put(uniqueFilename, out) ;
                return out ;
            } 
             
            uniqueFilename = chooseUniqueFileName(filename, FileOps::exists) ;
            LOG.info("Start: file:"+uniqueFilename) ;
            try {
                files.add(uniqueFilename) ;
                OutputStream out = new FileOutputStream(uniqueFilename);
                return new BufferedOutputStream(out, 1024*128) ;
            }
            catch (FileNotFoundException e) {
                IO.exception(e); 
                return null ;
            }
        }
        
        private static String chooseUniqueFileName(String filename, Predicate<String> existenceTest) {
            if ( ! existenceTest.test(filename) )
                return filename ;
            
            for(int i = 1 ; ; i++ ) {
                String fn = String.format("%s_%02d", filename, i) ;
                if ( ! existenceTest.test(fn) )
                    return fn ;
            }
        }
    }
    
    public static void main(String ... argv)  {
        DatasetGraphTDB dsgBase = (DatasetGraphTDB)TDB2Factory.createDatasetGraph() ;
        
        // ---- Collect
        TransactionalMonitorLogging tml = new TransactionalMonitorLogging(dsgBase, Location.mem()) ;
        dsgBase.setTransactionalMonitor(tml); 
        // Build into TDB
        Txn.executeWrite(dsgBase, () -> {
                Quad q1 = SSE.parseQuad("(<g1> <s> :p :o)") ;
                dsgBase.add(q1);
            }) ;
        Txn.executeWrite(dsgBase, () -> {
            Quad q1 = SSE.parseQuad("(<g2> <s> :p 123)") ;
            dsgBase.add(q1);
        }) ;
        
        DatasetGraph dsg2 = DatasetGraphFactory.createTxnMem() ;

        // ---- Replay

        if ( tml.getMem() != null ) {
            tml.getMem().forEach((k,v) -> {
                ByteArrayInputStream in =  new ByteArrayInputStream(v.toByteArray()) ;
                TxnLoggerThrift.replay(in, new ChangeLogApply(dsg2)) ;    
            }) ;
        }
        if ( tml.getFiles() != null ) {
            tml.getFiles().forEach((fn) -> {
                InputStream in = IO.openFile(fn) ;
                if ( ! ( in instanceof BufferedInputStream ) )
                    in = new BufferedInputStream(in, 1024*1024) ;
                TxnLoggerThrift.replay(in, new ChangeLogApply(dsg2)) ;    
            }) ;
        }
        
        System.out.println(dsg2) ;
        
        System.out.println("DONE") ;
    }
}

