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

package org.apache.jena.sparql.engine.join;

import java.util.Iterator;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;

/** Internal operations in support of join algorithms. */
class JoinLib {

    /** Control stats output / development use */
    static final boolean JOIN_EXPLAIN = false;

    // No hash key marker.
    public static final Object noKeyHash = new Object() ;
    public static final long nullHashCode = 5 ;

    public static long hash(Var v, Node x) {
        long h = 17;
        if ( v != null )
            h = h ^ v.hashCode();
        if ( x != null )
            h = h ^ x.hashCode();
        return h;
    }

    public static Object hash(Iterable<Var> joinKey, Binding row) {
        return hash(joinKey.iterator(), row);
    }

    public static Object hash(Iterator<Var> vars, Binding row) {
          long x = 31 ;
          boolean seenJoinKeyVar = false ;
          // Neutral to order in the set.
          while (vars.hasNext()) {
              Var v = vars.next();
              Node value = row.get(v) ;
              long h = nullHashCode ;
              if ( value != null ) {
                  seenJoinKeyVar = true ;
                  h = hash(v, value) ;
              } else {
                  // In join key, not in row.
              }

              x = x ^ h ;
          }
          if ( ! seenJoinKeyVar )
              return noKeyHash ;
          return x ;
      }
}

