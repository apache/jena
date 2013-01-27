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

/**
 * A ClosableIterator that contains only one element
 */
public class SingletonIterator<T> extends NiceIterator<T> implements ExtendedIterator<T> {
    
    /** The single value to be returned */
    private T item;

    /** element delivered */
    private boolean delivered = false;
        
    /**
     * Constructor
     * @param element the single value to be returned
     */
    public SingletonIterator(T element) {
        this.item = element;
    }
    
    /**
     * Can return a single value
     */
    @Override
    public boolean hasNext() {
        return !delivered;
    }

    /**
     * Return the value
     */
    @Override
    public T next() {
        if (delivered) 
            return noElements( "no objects in this iterator" );
        else {
            delivered = true;
            return item;
        }
    }

}
