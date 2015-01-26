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

package org.seaborne.dboe.engine;

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.access.AccessRows ;
import org.seaborne.dboe.engine.join.* ;

import com.hp.hpl.jena.sparql.core.Var ;

/** Join library */ 
public class Join
{
    // Debugging - sometimes it's easier to force execution to execute immediately. 
    // This forces (semi-)streaming joins to do all the work immediately.
    // Future - discard the concrete versions?
    public static boolean MATERIALIZE = false ;
    private static <X> RowList<X> materialize(RowList<X> rowList) {
        if ( ! MATERIALIZE ) 
            return rowList ;
        return rowList.materialize() ;
    }
    
    /** Perform a hash join */
    public static <X> RowList<X> hashJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, RowBuilder<X> builder) {
        return materialize(HashJoin.hashJoin(joinKey, left, right, builder)) ;
    }

    /** Perform a merge join - the left and right row lists must be in comparator order */
    public static <X> RowList<X> mergeJoin(JoinKey joinKey, RowList<X> left, RowList<X> right, 
                                                RowOrder<X> comparator, RowBuilder<X> builder) {
        return materialize(MergeJoin.mergeJoin(joinKey, left, right, comparator, builder)) ;
    }

    /** Perform a substitution join: this an index join achieved by replacing 
     *  variables in the pattern (right-side access) by values from rows of the left.    
     */
    public static <X> RowList<X> substitutionJoin(RowList<X> left, Tuple<Slot<X>> pattern, AccessRows<X> accessor, RowBuilder<X> builder) {
        return materialize(SubstitutionJoin.substitutionJoin(left, pattern, accessor, builder)) ;
    }
    
    // May leave JoinKey in for checking and symmetry
    
    /** Inner loop join */ 
    public static <X> RowList<X> innerLoopJoin(/*JoinKey joinKey,*/ RowList<X> leftTable, RowList<X> rightTable, RowBuilder<X> builder) {
        return materialize(InnerLoopJoin.innerLoopJoin(leftTable, rightTable, builder));
    }

    /** Test whether two rows are (SPARQL) compatible */ 
    public static <X> boolean compatible(Row<X> left, Row<X> right) {
        // Test to see if compatible: Iterate over variables in left
        for ( Var v : left.vars() ) {
            X xLeft  = left.get(v) ; 
            X xRight = right.get(v) ;
            // xLeft can't be null.
            if ( xRight != null && ! xRight.equals(xLeft) )
                return false ;
        }
        return true ;
    }
    
    /** Compute the merge of two rows, or return null if the rows are foiund not to be compatible. */ 
    public static <X> Row<X> merge(Row<X> left, Row<X> right, RowBuilder<X> builder) {
        builder.reset() ;
        for ( Var v : left.vars() ) {
            X xLeft  = left.get(v) ;
            builder.add(v, xLeft) ;
        }
            
        for ( Var v : right.vars() ) {
            X xLeft  = left.get(v) ;
            X xRight = right.get(v) ;
            if ( xLeft != null && xRight != null && ! xRight.equals(xLeft) )
                return null ;
            if ( xLeft == null )
                builder.add(v, xRight) ;
        }
        return builder.build() ;
    }
}
