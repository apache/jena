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

package org.apache.jena.sparql.core;

import org.apache.jena.query.ReadWrite ;

/** Check the transactional state of a DatasetGraph */ 
public abstract class DatasetGraphTrackActive extends DatasetGraphWrapper
{
    @Override
    protected abstract DatasetGraph get() ;

    protected DatasetGraphTrackActive() { super(null) ; }

    /** Check the transaction state from the point of view of the caller
     *  (usuually, for the current thread).
     */
    protected abstract void checkActive() ;
    protected abstract void checkNotActive() ;
    
    @Override
    public final void begin(ReadWrite readWrite) {
        checkNotActive();
        _begin(readWrite);
    }

    @Override
    public final void commit() {
        checkActive();
        _commit();
    }

    @Override
    public final void abort() {
        checkActive();
        _abort();
    }

    @Override
    public final void end() {
        // Don't check if active. We may have committed or aborted already.
        _end();
    }
    
    @Override
    public abstract boolean isInTransaction() ;
    protected abstract void _begin(ReadWrite readWrite) ;
    protected abstract void _commit() ;
    protected abstract void _abort() ;
    protected abstract void _end() ;
    
    @Override
    public void close() {
        if ( isInTransaction() )
            abort();
        // Don't close really - let the implementation decide.
        _close();
    }

    /** close() has been called. Subcklasses can make this a no-op. */ 
    protected abstract void  _close() ;
}
