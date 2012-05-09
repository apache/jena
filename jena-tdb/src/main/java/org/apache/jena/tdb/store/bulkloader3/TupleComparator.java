/**
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

package org.apache.jena.tdb.store.bulkloader3;

import java.util.Comparator;

import org.openjena.atlas.AtlasException;
import org.openjena.atlas.lib.Tuple;

public class TupleComparator implements Comparator<Tuple<Long>> {
    @Override
    public int compare(Tuple<Long> t1, Tuple<Long> t2) {
        int size = t1.size();
        if ( size != t2.size() ) throw new AtlasException("Cannot compare tuple of different sizes.") ;
        for ( int i = 0; i < size; i++ ) {
            int result = t1.get(i).compareTo(t2.get(i)) ;
            if ( result != 0 ) {
                return result ;
            }
        }
        return 0;
    }
}