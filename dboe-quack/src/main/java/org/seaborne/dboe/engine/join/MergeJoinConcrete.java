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

import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.PeekIterator ;
import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.SetUtils ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.row.RowListBuilderBase ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.core.Var ;

/** The merge join algorithm - it assumes the inputs are in clustered order.
 * This version materializes the results first, mainly for development
 * and for testing the algorithm.      
 */
public class MergeJoinConcrete {
    
    private static boolean CHECK = true ;
    private static Logger log = LoggerFactory.getLogger(MergeJoinConcrete.class) ;  
    
    public static  <X> RowList<X> mergeJoinBasic(JoinKey joinKey, RowList<X> left, RowList<X> right, 
                                                 RowOrder<X> comparator, RowBuilder<X> builder) {
        // ---- Setup check.
        // ** Join identity.
        boolean covered = true ;
        boolean joinKeyPresent = true ;
        if ( joinKey == null || joinKey.getVarKey() == null ) {
            joinKeyPresent = false ;
            covered = false ;
            //log.warn("No join key") ;
        }        
        
        // If the join key is not covered by the left or right, then switch to another join.
        // Issue - the join identity is not covered either!

        if ( joinKeyPresent && ! left.vars().contains(joinKey.getVarKey()) ) {
            covered = false ;
            //log.warn("LHS ("+left.vars()+") does not cover the JoinKey ("+joinKey+")") ;
        }        
        
        if ( joinKeyPresent && ! right.vars().contains(joinKey.getVarKey()) ) {
            covered = false ;
            //log.warn("RHS ("+right.vars()+") does not cover the JoinKey ("+joinKey+")") ;
        }        
        
        if ( ! covered )
            return HashJoinConcrete.hashJoinConcrete(joinKey, left, right, builder) ;
        // ---- Setup check.
        
        // Materializing version
        // Can start tuple2 further in using first index1 row.
        
        RowListBuilder<X> rowListBuilder = new RowListBuilderBase<X>() ;
        
        Set<Var> vars = SetUtils.union(left.vars(), right.vars()) ;
        PeekIterator<Row<X>> iter1 = PeekIterator.create(left.iterator()) ;
        PeekIterator<Row<X>> iter2 = PeekIterator.create(right.iterator()) ;

        Row<X> row1 = null ;
        Row<X> row2 = null ;
        
        
        for(;;)
        {
            if ( row1 == null ) {
                if ( !iter1.hasNext() )
                    break ;
                row1 = iter1.next() ;
            }
            if ( row2 == null ) {
                if ( !iter2.hasNext() )
                    break ;
                row2 = iter2.next() ;
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
            
            // Same : find all the rows that are in the same cluster, both sides.
            // Collect the left-side rows, then iterate over for each row on the right. 
            // It's like a mini innloop join.
            
            List<Row<X>> x1 = DS.list() ;
            
            for(;;) {
                x1.add(row1) ;
                // Now look to see if we move on with same match.
                // COMMON
                Row<X> row1x = iter1.peek() ;
                if ( row1x == null )
                    break ;
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
                        rowListBuilder.add(rx) ;
                }
                // Now look to see if we move on with same match. 
                Row<X> row2x = iter2.peek() ;
                if ( row2x == null ) 
                    break ;
                if ( comparator.compare(joinKey, row2, row2x) != 0)
                    break ;
                row2 = row2x ;
                iter2.next() ;
            }
            row2 = null ;
        }
        return rowListBuilder.build() ;
    }
}
