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

package dev;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.transaction.txn.Transaction ;
import org.seaborne.dboe.transaction.txn.TransactionalSystem ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;
import org.seaborne.tdb2.TDB2Factory ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;

public class MainCohort {
    static { LogCtl.setLog4j() ; }
    
    static RecordFactory recordFactory = new RecordFactory(4, 0) ;
    
    static Journal journal = Journal.create(Location.mem()) ;

    public static void main(String... args) {
        DatasetGraph dsg = TDB2Factory.createDatasetGraph() ;
        TransactionalSystem txnSystem = ((DatasetGraphTDB)dsg).getTxnSystem() ;
        
        dsg.begin(ReadWrite.READ);
        
        Transaction txn = txnSystem.getThreadTransaction() ;
        boolean b = txn.promote() ;
        if ( ! b )
            System.out.println("Did not promote");
        else {
            txn.promote() ;
            Quad q = SSE.parseQuad("(_ <s> <p> <o> )") ; 
            dsg.add(q) ;
            dsg.commit() ;
        }
        dsg.end() ;
        
        System.out.println("DONE") ;
        
    }
}


