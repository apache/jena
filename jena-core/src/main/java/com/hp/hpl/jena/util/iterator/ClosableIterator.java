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
    An iterator which should be closed after use. Some iterators take up resources which 
    should be free'd as soon as possible, eg large structures which can be discarded
    early, or external resources such as database cursors.
<p>
    Users of ClosableIterators (and thus of ExtendedIterator) should close the 
    iterator if they are done with it before it is exhausted (ie hasNext() is still
     true).If they do not, resources may leak or be reclaimed unpredictably or 
     much later than convenient. It is unnecessary but harmless to close the
     iterator once it has become exhausted. [<b>note</b>: previous versions
     of this documention specified a close regardless of exhaustion, but this
     was never the contract applied internally.]
<p>
    Implementors are encouraged to dispose of resources as soon as is convenient.
 */

public interface ClosableIterator<T> extends Iterator<T> 
    {
    /** 
        Close the iterator. Other operations on this iterator may now throw an exception. 
        A ClosableIterator may be closed as many times as desired - the subsequent
        calls do nothing.
    */
    public void close();
    }
