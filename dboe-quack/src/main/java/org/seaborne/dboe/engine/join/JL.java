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

package org.seaborne.dboe.engine.join;

import org.apache.jena.sparql.core.Var ;
import org.seaborne.dboe.engine.JoinKey ;
import org.seaborne.dboe.engine.Row ;

public class JL {
    
    public static final long nullHashCode = 5 ;
    public static final Object noKeyHash = new Object() ;

    public static <X> Object hash(Hasher<X> hasher, JoinKey joinKey, Row<X> row) {
        long x = 31 ;
        boolean seenJoinKeyVar = false ; 
        // Neutral to order in the set.
        for ( Var v : joinKey ) {
            X value = row.get(v) ;
            long h = nullHashCode ;
            if ( value != null ) {
                seenJoinKeyVar = true ;
                h = hasher.hash(v, value) ;
            } else {
                // In join key, not in row.
            }
    
            x = x ^ h ;
        }
        if ( ! seenJoinKeyVar )
            return noKeyHash ;
        return x ;
    }
    
    public static <X> Hasher<X> hash() { 
        return new Hasher<X>(){ 
            @Override 
            public long hash(Var v, X x) 
            { 
                long h = 17 ;
                if ( v != null )
                    h = h ^ v.hashCode() ;
                if ( x != null )  
                    h = h ^ x.hashCode() ;
                return h ;
            }
        } ;
    }



}

