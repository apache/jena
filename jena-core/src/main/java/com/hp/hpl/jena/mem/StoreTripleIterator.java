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

package com.hp.hpl.jena.mem;

import java.util.Iterator;

import com.hp.hpl.jena.graph.*;

/**
     An iterator wrapper for NodeToTriplesMap iterators which ensures that
     a .remove on the base iterator is copied to the other two maps of this
     GraphMem. The current triple (the most recent result of .next) is
     tracked by the parent <code>TrackingTripleIterator</code> so that it
     can be removed from the other two maps, which are passed in when this 
     StoreTripleIterator is created.
 
*/
public class StoreTripleIterator extends TrackingTripleIterator
	{
    protected NodeToTriplesMapBase X;
    protected NodeToTriplesMapBase A;
    protected NodeToTriplesMapBase B;
    protected Graph toNotify;
    
    public StoreTripleIterator
        ( Graph toNotify, Iterator<Triple> it, 
          NodeToTriplesMapBase X, 
          NodeToTriplesMapBase A, 
          NodeToTriplesMapBase B )
    	{ 
        super( it ); 
        this.X = X;
        this.A = A; 
        this.B = B; 
        this.toNotify = toNotify;
        }

    @Override public void remove()
        {
        super.remove();
        X.removedOneViaIterator();
        A.remove( current );
        B.remove( current );
        toNotify.getEventManager().notifyDeleteTriple( toNotify, current );
        }
	}
