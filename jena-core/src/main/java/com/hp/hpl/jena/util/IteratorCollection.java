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

package com.hp.hpl.jena.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.util.iterator.NiceIterator;


/**
 */
public class IteratorCollection
    {
    /**
        Only static methods here - the class cannot be instantiated.
    */
    private IteratorCollection()
        {}
    
    /**
        Answer the elements of the given iterator as a set. The iterator is consumed
        by the operation. Even if an exception is thrown, the iterator will be closed.
        @param i the iterator to convert
        @return A set of the members of i
    */
    public static <T> Set<T> iteratorToSet( Iterator<? extends T> i )
        {
        Set<T> result = CollectionFactory.createHashedSet();
        try { while (i.hasNext()) result.add( i.next() ); }
        finally { NiceIterator.close( i ); }
        return result;
        }

    /**
        Answer the elements of the given iterator as a list, in the order that they
        arrived from the iterator. The iterator is consumed by this operation:
        even if an exception is thrown, the iterator will be closed.
    	@param it the iterator to convert
    	@return a list of the elements of <code>it</code>, in order
     */
    public static <T> List<T> iteratorToList( Iterator<? extends T> it )
        {
        List<T> result = new ArrayList<>();
        try { while (it.hasNext()) result.add( it.next() ); }
        finally { NiceIterator.close( it ); }
        return result;
        }

    }
