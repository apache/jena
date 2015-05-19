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

package org.seaborne.dboe.engine.tdb;

import static org.seaborne.dboe.engine.EngLib.accVars ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.PhysicalPlan ;
import org.seaborne.dboe.engine.Slot ;
import org.seaborne.dboe.engine.Step ;

import org.apache.jena.sparql.core.Var ;
import org.seaborne.tdb2.store.NodeId ;
import org.seaborne.tdb2.store.tupletable.TupleIndex ;

public class PlannerMerge {
    // --- Merge
    
    /*package*/ static PhysicalPlan<NodeId> merge(PhysicalPlan<NodeId> plan, List<Tuple<Slot<NodeId>>> tuples, Set<Var> vars, AccessorTDB accessor) {
        tuples = new ArrayList<>(tuples) ;
        int N = tuples.size() ;
        // **** Only do is VAR p VAR . VAR p VAR .
        // Assuming reordering, that means test first tuple for VAR-p-VAR 
        // Assume any fixed S or O means that Substitute is better.
        
        // ---- Step 1. First merge pair.
        // -- Seed first triple.
        Tuple<Slot<NodeId>> firstTuple = tuples.get(0) ;
        TupleIndex[] indexes = accessor.getDB().getTupleTable(firstTuple).getIndexes() ;
        tuples.set(0, null) ;
        accVars(vars, firstTuple) ;

        // -- Find second triple 
        // Maybe several possibilities here.
        // For now - take first longest prefix match.
        Pair<MergeActionIdxIdx, Integer> p = findTupleToMergeWith(tuples, firstTuple, indexes) ;
        
        if ( p == null ) {
            // @@ No merge
            // @@ FIXME
            return null ;
        }

        Tuple<Slot<NodeId>> tuple1 = firstTuple ;
        MergeActionIdxIdx mergeStep = p.getLeft() ;
        Var mergeVar = mergeStep.getVar() ;

        int idx = p.getRight() ;
        
        Tuple<Slot<NodeId>> secondTriple = tuples.get(idx) ;
        Tuple<Slot<NodeId>> tuple2 = secondTriple ;
        accVars(vars, secondTriple);

        tuples.set(idx, null) ;

        Step<NodeId> step = new StepMergeJoin(mergeStep) ;
        plan.add(step) ;
        
        boolean MULTIMERGE = false ;
        if ( MULTIMERGE ) {
            // ---- Step 2. Subsequent merges   
            // Extend for as many tuples as possible.
            for ( int i = 0 ; i < N ; i++ ) {
                if ( tuples.get(i) == null )
                    continue ;
                // With access plans
                Pair<MergeActionVarIdx, Integer> pp = findSameMergeVar(tuples, /*plan.*/mergeVar, indexes) ;
                if ( pp == null )
                    break ;
                int idx2 = pp.getRight() ;
                Tuple<Slot<NodeId>> t = tuples.get(idx2) ;
                tuples.set(idx2, null) ;
                //accVars(plan.vars, t) ;
                accVars(vars, t) ;
                System.out.println("Loop: "+t) ; 
                //plan.merge.add(t) ;
                //plan.mergeAccess.add(pp.getLeft()) ;
                System.out.println("Loop: "+pp.getLeft()) ; 
                //ADD TO PLAN
            }
        }
        return plan ;
    }

    /** Find first (non-null) triple with the mergeVar */
    private static Pair<MergeActionVarIdx,Integer> findSameMergeVar(List<Tuple<Slot<NodeId>>> triples, Var mergeVar, TupleIndex[] indexes) {
        for ( int i = 0 ; i < triples.size() ; i++ ) {
            Tuple<Slot<NodeId>> t = triples.get(i) ;
            if ( t == null )
                continue ;
            IndexAccess[] accesses = access(t, indexes) ;
            for ( IndexAccess ia : accesses) {
                if ( ia.getVar().equals(mergeVar) )
                    return Pair.create(new MergeActionVarIdx(mergeVar, ia), i) ;
            }
//            if ( subject(t).isVar() && subject(t).var.equals(mergeVar))
//                return i ; 
//            if ( object(t).isVar() && object(t).var.equals(mergeVar))
//                return i ; 
        }
        return null ;
    }
    
    private static IndexAccess[] access(Tuple<Slot<NodeId>> triple, TupleIndex[] indexes)
    {
        IndexAccess[] accesses = new IndexAccess[indexes.length] ;
        int i = 0 ;
        for ( TupleIndex idx : indexes )
        {
            IndexAccess a = access(triple, idx) ;
            accesses[i++] = a ;
        }
        
        return accesses ;
    }

    private static IndexAccess access(Tuple<Slot<NodeId>> tuple, TupleIndex idx) {
        // @@ Walk down using fetchSlotIdx rather than map triple? Minor? 
        Tuple<Slot<NodeId>> t = idx.getColumnMap().map(tuple) ;
        for ( int i = 0 ; i < tuple.size() ; i++ ) {
            Slot<NodeId>n = t.get(i) ;
            if ( n.isVar() )
                return new IndexAccess(tuple, idx, i, n.var) ;
        }
        return null ;
    }

    /** Find a tuple to merge with - used when we are initialising the merge part */  
    private static Pair<MergeActionIdxIdx, Integer> findTupleToMergeWith(List<Tuple<Slot<NodeId>>> tuples, Tuple<Slot<NodeId>> t, TupleIndex[] indexes) {
        int weight = Integer.MAX_VALUE ;
        MergeActionIdxIdx selectedMerge = null ; 
        Var joinVar = null ;
        int idx = -1 ;
        for ( int i = 0 ; i < tuples.size() ; i++ ) {
            Tuple<Slot<NodeId>> tuple = tuples.get(i) ;
            if ( tuple == null ) 
                continue ;

            MergeActionIdxIdx maii = calcMergeAction(t, tuple, indexes) ;
            if ( maii == null )
                continue ;
            int len = maii.getPrefixCount() ;
            if ( weight > len ) {
                selectedMerge = maii ;
                weight = len ;
                joinVar = maii.getVar() ;
                idx = i ;
            }
            //pairWith(t, tuple, indexes) ;
        }
        if ( selectedMerge == null ) return null ;
        return Pair.create(selectedMerge, idx) ;
    }

    
    private static MergeActionIdxIdx calcMergeAction(Tuple<Slot<NodeId>> tuple1, Tuple<Slot<NodeId>> tuple2, TupleIndex[] indexes)
    {
        IndexAccess[] access1 = access(tuple1, indexes) ;
        IndexAccess[] access2 = access(tuple2, indexes) ;
            
//        System.out.println(Arrays.asList(access1)) ;
//        System.out.println(Arrays.asList(access2)) ;
        // Special case? access1.length=1, access2.length=1

        MergeActionIdxIdx action = null ;
        for ( IndexAccess a1 : access1 )
        {
            if ( a1 == null ) continue ;
            for ( IndexAccess a2 : access2 )
            {
                if ( a2 == null ) continue ;
                //System.out.println("Consider: "+a1+" // "+a2) ;
                if ( a1.getVar().equals(a2.getVar()))
                {
                    MergeActionIdxIdx action2 = new MergeActionIdxIdx(a1,a2) ;
                    // Longest prefixes.
                    if ( action == null )
                        action = action2 ;
                    else
                    {
                        //System.out.println("Choose: "+action+" // "+action2) ;
                        // Choose one with most prefixing.
                        int len1 = action.getPrefixCount() ;
                        int len2 = action2.getPrefixCount() ;
                        if ( len2 == len1 )
                        {
                            // Example: same var uses more than once in a tuple.
                            if ( action2.getIndex1() == action2.getIndex2() )
                                // Prefer same index.
                                // Better - special action.
                                action = action2 ;
                        }
                        else if ( len2 > len1 )
                            action = action2 ;
                        // else do nothing.
                    }
                }
            }
        }
        return action ;
    }

}

