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
    An iterator that consumes an underlying iterator and maps its results before
    delivering them; supports remove if the underlying iterator does.
*/

public class Map1Iterator<From, To> extends NiceIterator<To> implements ClosableIterator<To>
    {
	private Map1<From, To> map;
	private Iterator<From> base;
	
        /**
         * Construct a list of the converted.
         * @param map The conversion to apply.
         * @param base the iterator of elements to convert
         */
	public Map1Iterator( Map1<From, To> map, Iterator<From> base ) 
        {
        this.map = map;
        this.base = base;
        }
    
	public @Override To next() 
        { return map.map1( base.next() ); }
	
	public @Override boolean hasNext()
	    { return base.hasNext(); }
	
	public @Override void remove()
	    { base.remove(); }
	
	@Override public void close()
	    { NiceIterator.close( base ); }
    }
