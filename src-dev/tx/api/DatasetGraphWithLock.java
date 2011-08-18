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

import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.tdb.ReadWrite ;

public class DatasetGraphWithLock extends DatasetGraphTrackActive 
{
    static class JenaLockException extends JenaException
    {
        public JenaLockException()                                  { super(); }
        public JenaLockException(String message)                    { super(message); }
        public JenaLockException(Throwable cause)                   { super(cause) ; }
        public JenaLockException(String message, Throwable cause)   { super(message, cause) ; }
    }
    
    private DatasetGraph dsg = null ;

    public DatasetGraphWithLock(DatasetGraph dsg)
    {
        this.dsg = dsg ;
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
            throw new JenaLockException("Not in a locked region") ;
    }

    @Override
    protected void checkNotActive()
    {
        if ( inTransaction )
            throw new JenaLockException("Currently in a locked region") ;
    }

    @Override
    protected void _begin(ReadWrite readWrite)
    {
        boolean b = ( readWrite == ReadWrite.READ ) ;
        dsg.getLock().enterCriticalSection(b) ;
    }

    @Override
    protected void _commit()
    {
        dsg.getLock().leaveCriticalSection() ;
    }

    @Override
    protected void _abort()
    {
        throw new JenaLockException("Can't abort a locked update") ;   
        //dsg.getLock().leaveCriticalSection() ;
    }

    @Override
    protected void _close()
    {
        dsg.close() ;
    }
}

