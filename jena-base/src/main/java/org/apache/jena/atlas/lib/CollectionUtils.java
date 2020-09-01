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

import java.util.Collection ;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;

public class CollectionUtils
{
    /** Test for same elements, regardless of cardinality */
    public static <T> boolean sameElts(Collection<T> left, Collection<T> right) {
        return right.containsAll(left) && left.containsAll(right) ;
    }

    /** Return an element from a collection. */
    public static <T> T oneElt(Collection<T> collection) {
        if ( collection == null || collection.isEmpty() )
            return null;
        if ( collection instanceof List<?> )
            return ((List<T>)collection).get(0);
        return Iter.first(collection.iterator());
    }
}
