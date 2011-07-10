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

package tx.api;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxnTDB ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;

public class StoreConnection
{
    private DatasetGraphTDB baseDSG ;

    private StoreConnection(Location location)
    {
        baseDSG = DatasetBuilderStd.build(location.getDirectoryPath()) ;
    }
    
    public DatasetGraphTxnTDB begin(ReadWrite mode)
    {
        switch (mode)
        {
            case READ :
                // Make a new read dataset.
                //new DatasetGraphTxnRead(baseDSG) ;
                break ;
            case WRITE :
                // Check only active transaction.
                // Make from the last commited transation or base.
                DatasetGraphTxnTDB dsg2 = new TransactionManager().begin(baseDSG) ;
        }
        System.err.println("StoreConnection.begin: Not implemented fully") ;
        return null ;
    }
    
    
    // ---- statics
    
    public static StoreConnection make(String location)
    {
        return make(new Location(location)) ; 
    }

    
    public static StoreConnection make(Location location)
    {
        //TDBFactory.expel(location) ;
        // Cache?
        return new StoreConnection(location) ; 
    }
    
    
    
}

