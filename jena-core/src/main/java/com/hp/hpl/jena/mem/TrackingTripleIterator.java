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
import com.hp.hpl.jena.util.iterator.WrappedIterator;

/**
    A WrappedIterator which remembers the last object next'ed in a
    protected instance variable, so that subclasses have access to it 
    during .remove.
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
    }
