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

package com.hp.hpl.jena.sparql.core;

import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.lib.Sync ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.SystemARQ ;

/** A DatasetGraph that uses the dataset lock to give weak transactional behaviour.
 *  Only supports multiple-reader OR single-writer, and no transction abort.
 *  Transactions are not durable. 
 */
public class DatasetGraphWithLock extends DatasetGraphTrackActive implements Sync
{
    static class JenaLockException extends JenaException
    {
        public JenaLockException()                                  { super(); }
        public JenaLockException(String message)                    { super(message); }
        public JenaLockException(Throwable cause)                   { super(cause) ; }
        public JenaLockException(String message, Throwable cause)   { super(message, cause) ; }
    }
    
    private DatasetGraph dsg ;
    private boolean locked ;
    private ReadWrite readWrite ;
    

    public DatasetGraphWithLock(DatasetGraph dsg)
    {
        this.dsg = dsg ;
        this.locked = false ;
        this.readWrite = null ;
    }

    @Override
    protected DatasetGraph get()
    {
        return dsg ;
    }

    @Override
    protected void checkActive()
    {
        if ( ! isInTransaction() )
            throw new JenaLockException("Not in a locked region") ;
    }

    @Override
    protected void checkNotActive()
    {
        if ( isInTransaction() )
            throw new JenaLockException("Currently in a locked region") ;
    }

    @Override
    protected void _begin(ReadWrite readWrite)
    {
        this.readWrite = readWrite ;
        boolean b = ( readWrite == ReadWrite.READ ) ;
        dsg.getLock().enterCriticalSection(b) ;
        locked = true ;
    }

    @Override
    protected void _commit()
    {
        if ( readWrite ==  ReadWrite.WRITE )
            sync() ;
        locked = false ;
        dsg.getLock().leaveCriticalSection() ;
    }

    @Override
    protected void _abort()
    {
        locked = false ;
        throw new JenaLockException("Can't abort a locked update") ;   
        //dsg.getLock().leaveCriticalSection() ;
    }

    @Override
    protected void _end()
    {
        if ( locked )
            dsg.getLock().leaveCriticalSection() ;
        locked = false ;
    }

    @Override
    protected void _close()
    {
        if ( dsg != null )
            dsg.close() ;
        dsg = null ;
    }

    @Override
    public void sync()
    {
        SystemARQ.sync(dsg) ;
        // ARQ 2.7.0 bug - fails to sync the default graph.
        // When moving to 2.7.1 or later, remove the code line below. 
        // And these comments. 
        SystemARQ.sync(dsg.getDefaultGraph()) ;
    }
    
    @Override 
    public String toString() { 
        try { return dsg.toString() ; } catch (Exception ex) { return Lib.className(this) ; }  
    }
}
