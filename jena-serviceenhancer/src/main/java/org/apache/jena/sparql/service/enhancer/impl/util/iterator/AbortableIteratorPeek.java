/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.service.enhancer.impl.util.iterator;

import org.apache.jena.sparql.ARQInternalErrorException;

public class AbortableIteratorPeek<T>
    extends AbortableIterator1<T, T>
{
    private T binding = null ;
    private boolean closed = false ;

    public AbortableIteratorPeek(AbortableIterator<T> iterator) {
        super(iterator);
    }

    /** Returns the next binding without moving on.  Returns "null" for no such element. */
    public T peek()
    {
        if ( closed ) return null ;
        if ( ! hasNextBinding() )
            return null ;
        return binding ;
    }

    @Override
    protected boolean hasNextBinding()
    {
        if ( binding != null )
            return true ;
        if ( ! getInput().hasNext() )
            return false ;
        binding = getInput().nextBinding() ;
        return true ;
    }

    @Override
    protected T moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            throw new ARQInternalErrorException("No next binding") ;
        T b = binding ;
        binding = null ;
        return b ;
    }

    @Override
    protected void closeSubIterator() {
        this.closed = true;
    }

    @Override
    protected void requestSubCancel() {
    }

//    @Override
//    public void output(IndentedWriter out, SerializationContext sCxt) {
//        // TODO Auto-generated method stub
//
//    }
}
