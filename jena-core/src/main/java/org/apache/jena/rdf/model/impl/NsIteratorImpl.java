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

package org.apache.jena.rdf.model.impl;

import java.util.Iterator;

import org.apache.jena.rdf.model.* ;
import org.apache.jena.util.iterator.* ;

/** An NsIterator implementation
 */
public class NsIteratorImpl extends WrappedIterator<String> implements NsIterator
    {
    /** Creates new NsIteratorImpl, ignores _o_ */
    public NsIteratorImpl( Iterator<String> iter, Object o ) 
        { super( iter ); }

    @Override
    public String nextNs()
        { return next(); }
    }
