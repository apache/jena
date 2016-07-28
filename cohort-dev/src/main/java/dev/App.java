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

package dev;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.seaborne.dboe.jenax.Txn ;
import org.seaborne.dboe.transaction.txn.Transaction ;
import org.seaborne.dboe.transaction.txn.TransactionalSystem ;
import org.seaborne.tdb2.TDB2Factory ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class App {
    
    static { LogCtl.setLog4j(); }
    static Logger log = LoggerFactory.getLogger(App.class) ;

    public static void main(String[] args) {
        Quad q1 = SSE.parseQuad("(_ <s> <p> 1 )") ; 
        
        DatasetGraph dsg = TDB2Factory.createDatasetGraph() ;
        Txn.execWrite(dsg, ()->{
            dsg.add(q1) ;        
        }) ;
            
        Txn.execRead(dsg, ()->{
            RDFDataMgr.write(System.out, dsg, Lang.TRIG) ;
        }) ;
        System.out.println("-----------") ;
        
        Quad q2 = SSE.parseQuad("(_ <s> <p> 2 )") ;
        TransactionalSystem txnSystem = ((DatasetGraphTDB)dsg).getTxnSystem() ;
        dsg.begin(ReadWrite.READ);
        Transaction txn = txnSystem.getThreadTransaction() ;
        boolean b = txn.promote() ;
        if ( ! b ) {
            System.out.println("Did not promote");
            throw new RuntimeException() ;
        }
        
        dsg.add(q2) ;
        dsg.commit() ;
        dsg.end() ;
        
        Txn.execRead(dsg, ()->{
            RDFDataMgr.write(System.out, dsg, Lang.TRIG) ;
        }) ;
        
        System.out.println("DONE") ;
    }
}
