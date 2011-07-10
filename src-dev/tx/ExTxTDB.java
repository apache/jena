/**
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

package tx;

import tx.api.ReadWrite ;
import tx.api.StoreConnection ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxnTDB ;
import com.hp.hpl.jena.tdb.transaction.Transaction ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.GraphStoreFactory ;
import com.hp.hpl.jena.update.UpdateExecutionFactory ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateProcessor ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class ExTxTDB
{
    // Internal state of a DSG
    enum DSG_STATE { DSG_READ, DSG_WRITE, DGS_BLOCKED } ;
    
    interface DatasetTx extends Dataset
    {
        public Transaction getTransaction() ; //{ return transaction ; }
        public void commit() ; // { transaction.commit() ; }
        public void abort() ; //{ transaction.abort() ; }
    }
    
    public static void example1()
    {
        // TODO
        // Add to UpdateExecutionFactory
        //UpdateProcessor ==> UpdateExecution
        // Silent abort
        
        StoreConnection sConn = StoreConnection.make("DB") ;
        
        DatasetGraphTxnTDB dsg = sConn.begin(ReadWrite.READ) ;
        try {
            // SPARQL
            QueryExecution qExec = QueryExecutionFactory.create("ASK{}", /*dsg*/(Dataset)null) ;
            qExec.close() ;
        } finally { dsg.close() ; } // Close all QExecs
        
        dsg = sConn.begin(ReadWrite.WRITE) ;
        try {
            // Update
            dsg.commit() ;
        } finally { dsg.close() ; } // WARNING if no commit or abort.
        
        // Alt: .beginQuery / .beginUpdate (isa Graphstore)
        
        dsg = sConn.begin(ReadWrite.WRITE) ;
        try {
            // Update.
            GraphStore gs = GraphStoreFactory.create(dsg) ;
            UpdateRequest request = UpdateFactory.create("") ;
            // Add this operation to bypass the need for GraphStore.
            UpdateProcessor proc = UpdateExecutionFactory.create(request, dsg) ;
            proc.execute() ;
            dsg.commit() ;
        } finally { dsg.close() ; } // WARNING if no commit or abort.
    }
}

