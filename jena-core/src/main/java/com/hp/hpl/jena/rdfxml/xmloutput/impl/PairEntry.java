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

package com.hp.hpl.jena.rdfxml.xmloutput.impl;

class PairEntry<K,V>  implements java.util.Map.Entry<K,V>  {
    K a;
    V b;
    @Override
    public boolean equals(Object o) {
        if (o != null && (o instanceof PairEntry<?,?>)) {
            PairEntry<?,?> e = (PairEntry<?,?>) o;
            return e.a.equals(a) && e.b.equals(b);
        } 
        return false;
        
    }
    @Override
    public K getKey() {
        return a;
    }
    @Override
    public V getValue() {
        return b;
    }
    @Override
    public int hashCode() {
        return a.hashCode() ^ b.hashCode();
    }
    @Override
    public V setValue(Object value) {
        throw new UnsupportedOperationException();
    }
    PairEntry(K a, V b) {
        this.a = a;
        this.b = b;
    }
}
