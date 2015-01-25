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

package org.seaborne.jena.engine.join;

import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.iterator.PeekIterator ;
import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.SetUtils ;
import org.seaborne.jena.engine.* ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.core.Var ;

/** The merge join algorithm - it assumes the inputs are in clustered order */ 
public class MergeJoin {
    private static boolean CHECK = true ;
    private static Logger log = LoggerFactory.getLogger(MergeJoin.class) ;  
    
    public static <X> RowList<X> mergeJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, 
                                            RowOrder<X> comparator, RowBuilder<X> builder) {
        boolean covered = true ;
        boolean joinKeyPresent = true ;
        if ( joinKey == null || joinKey.getVarKey() == null ) {
            joinKeyPresent = false ;
            covered = false ;
        }        

        // Join identity?

        if ( joinKeyPresent && ! left.vars().contains(joinKey.getVarKey()) ) {
            covered = false ;
            //log.warn("LHS ("+left.vars()+") does not cover the JoinKey ("+joinKey+")") ;
        }        
        
        if ( joinKeyPresent && ! right.vars().contains(joinKey.getVarKey()) ) {
            covered = false ;
            //log.warn("RHS ("+right.vars()+") does not cover the JoinKey ("+joinKey+")") ;
        }        
        
        if ( ! covered )
            return HashJoin.hashJoin(joinKey, left, right, builder) ;
        
        Set<Var> vars = SetUtils.union(left.vars(), right.vars()) ;
        Iterator<Row<X>> r = new RowsMergeJoin<X>(joinKey, left, right, comparator, builder) ;
        return RowLib.createRowList(vars, r) ;
    }
    
    private static class RowsMergeJoin<X> extends IteratorSlotted<Row<X>> implements Iterator<Row<X>> {
        private long s_countLHS = 0 ;  
        private long s_countRHS = 0 ;  
        private long s_countResults = 0 ;
        
        private final JoinKey joinKey ;
        private final RowOrder<X> comparator ;
        private final List<Row<X>> accumulator = DS.list() ;
        private final RowBuilder<X> builder ; 

        private final PeekIterator<Row<X>> iter1 ;
        private final PeekIterator<Row<X>> iter2 ;

        // Current row pair 
        private Row<X> row1 = null ;
        private Row<X> row2 = null ;
        
        // Current match.
        private Iterator<Row<X>> activeIter = null ;
        
        
        public RowsMergeJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, RowOrder<X> comparator, RowBuilder<X> builder) { 
            this.iter1 = PeekIterator.create(left.iterator()) ;
            this.iter2 = PeekIterator.create(right.iterator()) ;
            this.joinKey = joinKey ;
            this.comparator = comparator ;
            this.builder = builder ;
        }
        
        @Override
        protected Row<X> moveToNext() {
            if ( isFinished() )
                return null ;
            
            if ( activeIter != null ) {
                if ( activeIter.hasNext() ) {
                    s_countResults ++ ;
                    return activeIter.next() ;
                }
                activeIter = null ;
            }

            // activeIter == null
            activeIter = moreResults() ;
            
            if ( activeIter == null || ! activeIter.hasNext() )
                return null ;

            s_countResults ++ ;
            return activeIter.next() ;
        }
            
        private  Iterator<Row<X>> moreResults() {   
            for(;;)
            {
                if ( row1 == null ) {
                    if ( !iter1.hasNext() )
                        break ;
                    row1 = iter1.next() ;
                    s_countLHS ++ ;
                }
                if ( row2 == null ) {
                    if ( !iter2.hasNext() )
                        break ;
                    row2 = iter2.next() ;
                    s_countRHS ++ ;
                }
                
                if ( CHECK ) {
                    Row<X> row1a = iter1.peek() ;
                    if ( row1a != null && comparator.compare(joinKey, row1, row1a) > 0 )
                        log.warn("LHS not sorted : "+ row1 + " --" +row1a ) ; 
                    
                    Row<X> row2a = iter2.peek() ;
                    if ( row2a != null && comparator.compare(joinKey, row2, row2a) > 0 ) 
                        log.warn("RHS not sorted : "+ row2 + " --" +row2a ) ;  
                }
                
                int x = comparator.compare(joinKey, row1, row2) ;
                if ( x > 0 )
                {   // Discard smaller 
                    row2 = null ;
                    continue ;
                }
                if ( x < 0 )
                {   // Discard smaller
                    row1 = null ;
                    continue ;
                }

                // ** Find all for this pair.
                // Same : find all the rows that are in the same cluster, both sides.
                // Collect the left-side rows, then iterate over for each row on the right. 
                // It's like a mini innerloop join.
                
                accumulator.clear() ;
                List<Row<X>> x1 = DS.list() ;
                
                for(;;) {
                    x1.add(row1) ;
                    // Now look to see if we move on with same match.
                    // COMMON
                    Row<X> row1x = iter1.peek() ;
                    if ( row1x == null )
                        break ;
                    s_countLHS ++ ;
                    if ( comparator.compare(joinKey, row1, row1x) != 0 )
                        break ;
                    row1 = row1x ;
                    iter1.next() ;
                }
                
                row1 = null ;
                
                // BUT if row2 shares zero variables then it is compatible with all rows.
                // Assume no key -> lowest?
                
                for(;;) {
                    // General pattern here?
                    for ( Row<X> r : x1 ) {
                        Row<X> rx = Join.merge(r, row2, builder) ;
                        // Maybe incompatible on other keys.
                        if ( rx != null )
                            accumulator.add(rx) ;
                    }
                    // Now look to see if we move on with same match. 
                    Row<X> row2x = iter2.peek() ;
                    if ( row2x == null ) 
                        break ;
                    s_countRHS ++ ;
                    if ( comparator.compare(joinKey, row2, row2x) != 0)
                        break ;
                    row2 = row2x ;
                    iter2.next() ;
                }
                row2 = null ;
                
                if ( accumulator.size() == 0 )
                    break ;
                
                return accumulator.iterator() ;
            }
            // No more.
            return null ;
        }

        @Override
        protected boolean hasMore() {
            return ! isFinished() ;
        }
        
        @Override
        protected void closeIterator() {
            if ( Quack.JOIN_EXPLAIN ) {
                String x = String.format(
                             "MergeJoin: LHS=%d RHS=%d Results=%d",
                             s_countLHS, s_countRHS, s_countResults) ;
                Quack.joinStatsLog.debug(x);
            }
        }

    }
}
