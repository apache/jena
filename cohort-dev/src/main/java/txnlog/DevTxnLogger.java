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

package txnlog;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.OutputStream ;

import org.apache.jena.sparql.core.DatasetChanges ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.thrift.TException ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.tdb2.TDBFactory ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;

public class DevTxnLogger {
    
    interface DatasetChangesFactory {
        DatasetChanges make(OutputStream logStream) ;
    }
    
    public static void main(String ... argv)  throws TException  {
        // Need "log" concept.
        
        DatasetGraphTDB dsgBase = (DatasetGraphTDB)TDBFactory.createDatasetGraph() ;
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;

        // Build into TDB
        // Better - on/off switch in DatasetGraphTDB
        // ThreadLocal (transaction local) monitor.
        // Or not needed if we assume single-writer if done after the add/delete/deleteAny
        // notifyAdd and nofityDeleet are pre-op.
     
        logWriteTxn(dsgBase, out, () -> {
            Quad q1 = SSE.parseQuad("(<g> <s> :p :o)") ;
            dsgBase.add(q1);
        }) ;
        
        DatasetGraph dsg2 = DatasetGraphFactory.createMem() ;
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        TxnLogger.replay(in, new ChangeLogApply(dsg2)) ;
        System.out.println(dsg2) ;
    }
    
    public static void logWriteTxn(DatasetGraphTDB dsgBase, OutputStream logStream, Runnable r) {
        DatasetChangesFactory dsgChangeFactory = new DatasetChangesFactory() {
            @Override
            public DatasetChanges make(OutputStream logStream) {
                DatasetChanges changes = new ThriftChangeLog(logStream) ;
                DatasetGraphMonitorTxn dsg = new DatasetGraphMonitorTxn(dsgBase, changes, false) ;
                return changes ;
            }
        } ;
        DatasetChanges changes = dsgChangeFactory.make(logStream) ;
        
        Txn.executeWrite(dsgBase, ()-> {
            // XXX dsgBase.setMonitor(changes) ;
            changes.start();
            r.run();
            changes.finish() ;
            // XXX dsgBase.removeMonitor(changes) ;
        });
                         
    }
}

