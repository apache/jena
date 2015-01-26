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

import java.util.* ;

import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.lib.SetUtils ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.join.HashJoin.Hasher ;

import com.hp.hpl.jena.sparql.core.Var ;

/** Symmetric hash join that can read from either inputs */  
public class PipelineHashJoin {
    // No hash key marker.
    public static final Object noKeyHash = new Object() ;

    public static <X> Iterator<Row<X>> hashJoinDev(JoinKey joinKey, RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        Set<Var> vars = SetUtils.union(left.vars(), right.vars()) ;
        Hasher<X> hasher = hash() ;
        Iterator<Row<X>> r = new RowsPipelineHashJoin<X>(joinKey, left, right, hasher, builder) ;
        return r ;
    }

    
    /** Evaluate. */
    public static <X> RowList<X> hashJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        Hasher<X> hasher = hash() ;
        return hashJoin(joinKey, left, right, hasher, builder) ;
    }
    
    /** Evaluate. */
    public static <X> RowList<X> hashJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, Hasher<X> hasher, RowBuilder<X> builder) {
        Set<Var> vars = SetUtils.union(left.vars(), right.vars()) ;
        Iterator<Row<X>> r = new RowsPipelineHashJoin<X>(joinKey, left, right, hasher, builder) ;
        return RowLib.createRowList(vars, r) ;
    }
    
    private static class RowsPipelineHashJoin<X> extends IteratorSlotted<Row<X>> implements Iterator<Row<X>> {
        private long s_countLHS             = 0 ;       // Left input side size
        private long s_countRHS             = 0 ;       // Right input side size
        private long s_countResults         = 0 ;       // Result size.
        
        private final Set<Var>                 vars ;
        private final JoinKey                  joinKey ;
        private final Hasher<X>                hasher ;
        private final RowBuilder<X>            builder ;

        private HashProbeTable<X> leftTable ; 
        private HashProbeTable<X> rightTable ;

        // Input.
        private final Iterator<Row<X>>         iterLeft ;
        private final Iterator<Row<X>>         iterRight ;
        // Output.
        private Iterator<Row<X>>               iterCurrent ;
        private Row<X>                         rowCurrent ;

        public RowsPipelineHashJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, Hasher<X> hasher, RowBuilder<X> builder) {
            
            this.vars = SetUtils.union(left.vars(), right.vars()) ;
            this.hasher = hasher ;
            this.joinKey = joinKey ;
            this.builder = builder ;
            this.leftTable = new HashProbeTable<>(hasher, joinKey) ; 
            this.rightTable = new HashProbeTable<>(hasher, joinKey) ;
            this.iterLeft = left.iterator() ;
            this.iterRight = right.iterator() ;
        }
        @Override
        protected Row<X> moveToNext() {
            for(;;) {
             // Ensure we are processing a row.
                while ( iterCurrent == null ) {
                    // How can we poll left and right?  BlockingQueues!
                    
                    boolean leftReady = iterLeft.hasNext() ;
                    boolean rightReady = iterRight.hasNext() ;
                    if ( ! leftReady && ! rightReady ) {
                        joinFinished() ;
                        return null ;
                    }
                    
                    HashProbeTable<X> storeTable ;
                    HashProbeTable<X> probeTable ;
                    
                    // At least one of left and right is ready
                    // If both, get from the short side, hoping to make it run out
                    // which turns us into a regular hashjoin for the remainder.
                    
                    boolean getFromLeft = ( leftReady && s_countLHS <= s_countRHS ) || ! rightReady ;
                    
                    // Move one row.
                    if ( getFromLeft ) {
                        rowCurrent = iterLeft.next() ;
                        s_countLHS ++ ;
                        storeTable = leftTable ;
                        probeTable = rightTable ;
                        if ( ! rightReady && leftTable != null ) { 
                            // Drop other probe table.
                            leftTable.clear() ;
                            leftTable = null ;
                        }
                    } else { // right ready.
                        rowCurrent = iterRight.next() ;
                        s_countRHS ++ ;
                        storeTable = rightTable ;
                        probeTable = leftTable ;
                        if ( ! leftReady && rightTable != null ) { 
                            // Drop other probe table.
                            rightTable.clear() ;
                            rightTable = null ;
                        }
                    }

                    if ( leftReady && rightReady )
                        // Only store if both active otherwise it's really just a 
                        // regular hash join on the smaller side.
                        storeTable.put(rowCurrent) ;
                    
                    // get iterator of candidates.
                    iterCurrent = probeTable.getCandidates(rowCurrent) ;
                }

                // iterCurrent != null.
                // Loop on the hash-match entries.
                for ( ; iterCurrent.hasNext() ; ) {
                    Row<X> rowOther = iterCurrent.next() ;
                    Row<X> r = Join.merge(rowCurrent, rowOther, builder) ;
                    //System.out.println("R: "+r) ;
                    if (r != null) {
                        s_countResults ++ ;
                        return r ;
                    }
                }
                iterCurrent = null ;
                rowCurrent = null ;
                //System.out.println("End current") ;
            }
        }

        private void joinFinished() {}
        
        @Override
        protected boolean hasMore() {
            return ! isFinished() ;
        }
    
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
    
    // Share with HashJoin
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

