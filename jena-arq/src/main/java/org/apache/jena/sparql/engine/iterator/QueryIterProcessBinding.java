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

package org.apache.jena.sparql.engine.iterator ;

import java.util.NoSuchElementException ;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;

/**
 * An iterator that applies a condition. The condition may return a different
 * binding.
 */

public abstract class QueryIterProcessBinding extends QueryIter1 {
    /** Process the binding - return null for "not accept".
     * Subclasses may return a different Binding to the argument and
     * the result is the returned Binding.
     */
    abstract public Binding accept(Binding binding) ;

    private Binding nextBinding ;
    private final AtomicBoolean signalCancel ;

    public QueryIterProcessBinding(QueryIterator qIter, ExecutionContext context) {
        super(qIter, context) ;
        nextBinding = null ;
        AtomicBoolean signal;
        try {
            signal = context.getContext().get(ARQConstants.symCancelQuery);
        } catch(Exception ex) {
            signal = null;
        }
        signalCancel = signal;
    }

    /**
     * Are there any more acceptable objects.
     *
     * @return true if there is another acceptable object.
     */
    @Override
    protected boolean hasNextBinding() {
        // Needs to be idempotent.?
        if ( isFinished() )
            return false ;

        if ( nextBinding != null )
            return true ;

        // Null iterator.
        if ( getInput() == null )
            throw new ARQInternalErrorException(Lib.className(this) + ": Null iterator") ;

        while (getInput().hasNext()) {
            checkCancelled();
            // Skip forward until a binding to return is found.
            Binding input = getInput().nextBinding() ;
            Binding output = accept(input) ;
            if ( output != null ) {
                nextBinding = output ;
                return true ;
            }
        }
        nextBinding = null ;
        return false ;
    }

    private final void checkCancelled() {
        if ( signalCancel != null && signalCancel.get() ) {
            this.cancel();
            throw new QueryCancelledException();
        }
    }

    /**
     * The next acceptable object in the iterator.
     *
     * @return The next acceptable object.
     */
    @Override
    public Binding moveToNextBinding() {
        if ( hasNext() ) {
            Binding r = nextBinding ;
            nextBinding = null ;
            return r ;
        }
        throw new NoSuchElementException() ;
    }

    @Override
    protected void closeSubIterator() {}

    @Override
    protected void requestSubCancel() {}
}
