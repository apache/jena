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

package org.apache.jena.mem;

import java.util.Iterator;
import java.util.function.Consumer;

import org.apache.jena.graph.* ;
import org.apache.jena.util.iterator.WrappedIterator ;

/**
    A WrappedIterator which remembers the last object next'ed in a
    protected instance variable, so that subclasses have access to it 
    during .remove.
    After a call to {@link TrackingTripleIterator#forEachRemaining} current is null. So calling #remove after
    #forEachRemaining is not supported.
*/
public class TrackingTripleIterator extends WrappedIterator<Triple>
    {
    public TrackingTripleIterator( Iterator<Triple> it ) 
        { super( it ); }    
    
    /**
        The remembered current triple. Subclass should *not* assign to this variable. 
    */
    protected Triple current;
        
    /**
        Answer the next object, remembering it in <code>current</code>. 
     	@see java.util.Iterator#next()
    */
    @Override
    public Triple next()
        { return current = super.next(); }

    @Override
        public void forEachRemaining(Consumer<? super Triple> action)
        {
            /** The behavior of {@link java.util.Iterator#remove}is undefined after a call to forEachRemaining.
               So it should be okay, to not waste performance here. */
            this.current = null;
            super.forEachRemaining(action);
        }
    }
