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
import java.util.Iterator ;
/** An Iterator that is created lazily.
 * The sequence to be defined is defined by
 * the subclass's definition of create().
 * This is only called on the first call to
 * <CODE>hasNext()</CODE> or <CODE>next()</CODE>.
 * This allows an Iterator to be passed to some other
 * code, while delaying the evaluation of what actually
 * is going to be iterated over.
 */
abstract public class LateBindingIterator<T> implements Iterator<T> {

    private Iterator<? extends T> it;
    
    /** An Iterator that is created lazily. 
     * The sequence to be defined is defined by 
     * a subclass's instantiation of create().
     * This is only called on the first call to
     * <CODE>hasNext()</CODE> or <CODE>next()</CODE>.
 */
    public LateBindingIterator() {
    }

    @Override
    public boolean hasNext() {
        lazy();
        return it.hasNext();
    }
    
    @Override
    public T next() {
        lazy();
        return it.next();
    }
    
    @Override
    public void remove() {
        lazy();
        it.remove();
    }
    
    private void lazy() {
        if ( it == null )
            it = create();
    }
/** The subclass must define this to return
 * the Iterator to invoke. This method will be
 * called at most once, on the first call to
 * <CODE>next()</CODE> or <CODE>hasNext()</CODE>.
 * From then on, all calls to this will be passed
 * through to the returned Iterator.
 * @return The parent iterator defining the sequence.
 */    
    public abstract Iterator<? extends T> create();
    
}
