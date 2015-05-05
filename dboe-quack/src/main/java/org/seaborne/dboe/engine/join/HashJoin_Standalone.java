/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine.join;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.lib.SetUtils ;
import org.apache.jena.ext.com.google.common.collect.HashMultimap ;
import org.apache.jena.ext.com.google.common.collect.Multimap ;
import org.apache.jena.sparql.core.Var ;
import org.seaborne.dboe.engine.* ;

public class HashJoin_Standalone
{
    // No hash key marker.
    public static final Object noKeyHash = new Object() ;

    /** Evaluate a hash join. */
    public static <X> RowList<X> hashJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        Hasher<X> hasher = hash() ;
        return hashJoin(joinKey, left, right, hasher, builder) ;
    }
    
    /** Evaluate a hash join. */
    public static <X> RowList<X> hashJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, Hasher<X> hasher, RowBuilder<X> builder) {
        Set<Var> vars = SetUtils.union(left.vars(), right.vars()) ;
        Iterator<Row<X>> r = new RowsHashJoin<X>(joinKey, left, right, hasher, builder) ;
        return RowLib.createRowList(vars, r) ;
    }
    
    private static class RowsHashJoin<X> extends IteratorSlotted<Row<X>> implements Iterator<Row<X>> {
        private long s_countLHS             = 0 ;       // Left input side size
        private long s_countRHS             = 0 ;       // Right input side size
        private long s_countResults         = 0 ;       // Result size.
        private long s_bucketCount          = 0 ;
        private long s_maxBucketSize        = 0 ;
        private long s_noKeyBucketSize      = 0 ;
        private long s_maxMatchGroup        = 0 ;
        private long s_countRightMiss       = 0 ;
        
        private final Set<Var>                 vars ;
        private final JoinKey                  joinKey ;
        private final Hasher<X>                hasher ;
        private final RowBuilder<X>            builder ;
        private final Multimap<Object, Row<X>> buckets ;
        private final Collection<Row<X>>       leftNoKey ;

        private Iterator<Row<X>>               iterRight ;
        private Row<X>                         rowRight          = null ;
        private Iterator<Row<X>>               iterCurrent ;

        public RowsHashJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, Hasher<X> hasher, RowBuilder<X> builder) {
            this.vars = SetUtils.union(left.vars(), right.vars()) ;
            this.hasher = hasher ;
            this.builder = builder ;
            this.joinKey = joinKey ;
            
            this.iterRight = right.iterator() ;

            // Phase 1 : Build hash table. 
            buckets = HashMultimap.create() ;
            Iterator<Row<X>> iter1 = left.iterator() ; 
            for (; iter1.hasNext();) {
                Row<X> row1 = iter1.next() ;
                s_countLHS ++ ;
                Object longHash = hash(hasher, joinKey, row1) ;
                buckets.put(longHash, row1) ;
            }
            
            this.iterCurrent = null ;
            this.leftNoKey = buckets.get(noKeyHash) ;
            
            if ( true )
            {
                long max = 0 ;
                for ( Object key : buckets.keys() ) {
                    long s = buckets.get(key).size() ;
                    max = Math.max(max, s) ;
                }
                s_maxBucketSize = max ;
            }
            
            s_bucketCount = buckets.keys().size() ;
            s_noKeyBucketSize = (leftNoKey == null ) ? 0 : leftNoKey.size() ;
        }

        @Override
        protected Row<X> moveToNext() {
            // Gather stats
            // Internal IteratorSlotted.ended call?
            // iterCurrent is the iterator of entries in the left hashed table
            // for the right row.    
            // iterRight is the stream of incoming rows.
            for(;;) {
                // Ensure we are processing a row. 
                while ( iterCurrent == null ) {
                    // Move on to the next row from the right.
                    if ( ! iterRight.hasNext() ) {
                        joinFinished() ;
                        return null ;
                    }
                    
                    rowRight = iterRight.next() ;    
                    s_countRHS ++ ;
                    // Something common here:
                    // (key,table) -> iterator.
                    
                    Object longHash = hash(hasher, joinKey, rowRight) ;
                    
                    if ( longHash == noKeyHash ) {
                        // No shared vars with the JoinKey; iterator over the whole of the left.
                        iterCurrent = buckets.values().iterator() ;
                    } else {
                        // Get all rows matching with the hash from the right row. 
                        Collection<Row<X>> sameKey = buckets.get(longHash) ;    // Maybe null.
                        if ( sameKey != null )
                            s_maxMatchGroup = Math.max(s_maxMatchGroup, sameKey.size()) ;
                        else
                            s_countRightMiss ++ ;
                        iterCurrent = ( sameKey == null ) ? null : sameKey.iterator() ;
                        if ( leftNoKey != null )
                            iterCurrent = Iter.concat(iterCurrent, leftNoKey.iterator()) ;
                    }
                }
                
                // Iterator valid.
                if ( iterCurrent != null ) {
                    // Emit one row using the rightRow and the current matched left rows. 
                    if ( ! iterCurrent.hasNext() ) {
                        iterCurrent = null ;
                        continue ;
                    }
    
                    builder.reset() ;
                    Row<X> rowLeft = iterCurrent.next() ;
                    Row<X> r = Join.merge(rowLeft, rowRight, builder) ;
                    if (r != null) {
                        s_countResults ++ ;
                        return r ;
                    }
                }
            }
        }        

        private void joinFinished() {}
            
        @Override
        protected void closeIterator() {
            if ( Quack.JOIN_EXPLAIN ) {
                String x = String.format(
                             "HashJoin: LHS=%d RHS=%d Results=%d RightMisses=%d MaxBucket=%d NoKeyBucket=%d",
                             s_countLHS, s_countRHS, s_countResults, 
                             s_countRightMiss, s_maxBucketSize, s_noKeyBucketSize) ;
                Quack.joinStatsLog.debug(x);
            }
        }

        @Override
        protected boolean hasMore() {
            return ! isFinished() ;
        }
    }
    
    public interface Hasher<X>
    {
        /** Must cope with null in either slot */ 
        public long hash(Var v, X x) ;
        
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

    public static final long nullHashCode = 5 ; 
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
}

