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

package tx.api;

import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;

public class DatasetGraphTX extends DatasetGraphTrackActive 
{
    // DatasetGraphWrapper with mutable DatasetGraph
    private DatasetGraphTxn dsg = null ;
    private ReadWrite ReadWrite = null ;
    private final Location location ;
    private final StoreConnection sConn ;
    
    static class JenaTransactionException extends JenaException
    {
        public JenaTransactionException()                                  { super(); }
        public JenaTransactionException(String message)                    { super(message); }
        public JenaTransactionException(Throwable cause)                   { super(cause) ; }
        public JenaTransactionException(String message, Throwable cause)   { super(message, cause) ; }
    }
    
    public DatasetGraphTX(Location location)
    {
        this.location = location ;
        sConn = StoreConnection.make(location) ;
    }

    @Override
    protected DatasetGraph get()
    {
        return dsg ;
    }

    @Override
    protected void checkActive()
    {
        if ( ! inTransaction )
            throw new JenaTransactionException("Not in a transaction ("+location+")") ;
    }

    @Override
    protected void checkNotActive()
    {
        if ( inTransaction )
            throw new JenaTransactionException("Currently in a transaction ("+location+")") ;
    }

    @Override
    protected void _begin(com.hp.hpl.jena.tdb.ReadWrite readWrite)
    {
        dsg = sConn.begin(ReadWrite) ;
        inTransaction = true ;
    }

    @Override
    protected void _commit()
    {
        dsg.commit() ;
        inTransaction = false ;
    }

    @Override
    protected void _abort()
    {
        dsg.abort() ;
        inTransaction = false ;
    }

    @Override
    protected void _close()
    {
        dsg.close() ;
    }
}
