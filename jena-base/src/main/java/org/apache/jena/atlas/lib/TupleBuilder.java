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

package org.apache.jena.atlas.lib;


import java.util.ArrayList ;
import java.util.List ;

/** Tuple builder class - tuples are immutable, this is how to create them in the builder style */
public class TupleBuilder<T> 
{
    private List<T> x = new ArrayList<>() ;
    
    public TupleBuilder() { } 
    
    public TupleBuilder<T> add(T element) {
        x.add(element) ;
        return this ;
    }
    
    public TupleBuilder<T> reset() {
        x.clear() ;
        return this ;
    }
    
    public Tuple<T> build() { 
        @SuppressWarnings("unchecked")
        T[] elts = (T[])new Object[x.size()] ; 
        // Copy contents, should not create a new array because elts
        // is created with the right size so elts == elts2 
        T[] elts2 = x.toArray(elts) ;
        return new Tuple<>(elts2) ;
    }
}
