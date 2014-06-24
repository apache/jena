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

package com.hp.hpl.jena.util.iterator;

import java.util.Iterator;

/**
     An empty iterator. The specialised versions of andThen() eliminate left operands
     that are empty iterators from cascaded sequences.
*/
public class NullIterator<T> extends NiceIterator<T> 
    {
    public static <T> NullIterator<T>  instance() 
        { return new NullIterator<>(); }
    
    @SuppressWarnings("unchecked")
    @Override public <X extends T> ExtendedIterator<T> andThen( Iterator<X> it )
        { 
        return it instanceof ExtendedIterator 
            ? (ExtendedIterator<T>) it 
            : super.andThen( it )
            ;
        }
    }
