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

import static org.seaborne.dboe.engine.EngLib.object ;
import static org.seaborne.dboe.engine.EngLib.predicate ;
import static org.seaborne.dboe.engine.EngLib.subject ;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.RepeatApplyIterator ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.apache.jena.sparql.core.Var ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.access.AccessData ;
import org.seaborne.dboe.engine.access.AccessRows ;
import org.seaborne.dboe.engine.explain.Explain2 ;
import org.seaborne.dboe.engine.row.RowListBuilderBase ;
import org.seaborne.dboe.engine.row.RowVarBinding ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Support for AccessorTDB */
public class AccessOps
{
    public static final boolean DEBUG = false ;
    private static Logger log = LoggerFactory.getLogger(AccessOps.class) ; 

    /** Helper for implementing AccessTuples.get(PredicateObjectList).
     *  This executes for fixed subject (the subject slot of accessTuple).
     * @param accessTuple
     * @param access        AccessData    
     * @param predObjList   Predicate-Object list.
     */
    
    public final /*package*/ static <X> Iterator<Row<X>> executeTermSubject$(Tuple<X> accessTuple, 
                                                                             AccessData<X> access,
                                                                             PredicateObjectList<X> predObjList) {
        // XXX Special case predObjList = 1
        if ( DEBUG ) FmtLog.info(log, "accExecuteTermSubject : %s", predObjList)  ;
        
        // Cache
        Iterator<Tuple<X>> iter = access.accessTuples(accessTuple) ;
        List<Tuple<X>> data = Iter.toList(iter) ;

        Row<X> initial = RowLib.identityRow() ;
        
        //int N = accessTuple.size() ;  
        X subject = subject(accessTuple) ;
        if ( DEBUG ) FmtLog.info(log, "  Subject = %s", subject) ;

        List<Row<X>> tails = new ArrayList<>() ;
        tails.add(initial) ;

        // This is the (reused) area to return results from querying the data.  Avoids churn.
        List<X> space = new ArrayList<>() ;
        
        for (int idxPredicate = 0; idxPredicate < predObjList.size(); idxPredicate++) {
            X pred = predObjList.getPredicate(idxPredicate) ;
            Slot<X> obj =  predObjList.getObject(idxPredicate) ;
            if ( DEBUG ) FmtLog.info(log, "  Predicate-object: %s %s", pred, obj) ;
            
            //for each solution ;
            List<Row<X>> newTails = new ArrayList<>() ;
            for ( Row<X> parent : tails ) {
                if ( DEBUG ) FmtLog.info(log, "  parent: %s", parent) ;
                Slot<X> obj1 = predObjList.getObject(idxPredicate) ;
                if ( parent != null && obj.isVar() ) {
                    X val1 = parent.get(obj.var) ;
                    if ( val1 != null )
                        obj1 = Slot.createTermSlot(val1) ;
                }
                    
                if ( obj1.isVar() ) {
                    Var var = obj1.var ;
                    space.clear() ;
                    find(data, pred, space) ;
                    if ( DEBUG ) FmtLog.info(log, "  find: %s %s --> %s", pred, var, space) ;
    
                    for ( X val : space ) {
                        Row<X> vb = new RowVarBinding<X>(parent, var, val) ;
                        newTails.add(vb);
                        if ( DEBUG ) FmtLog.info(log, "  add %s %s = %s", var, val, vb) ;
                    }
                    if ( DEBUG ) FmtLog.info(log, "  --> %s", newTails) ;
                    
                } else {
                    // Obj is a term.
                    if ( contains(data, pred, obj1.term) ) {
                        if ( DEBUG ) FmtLog.info(log, "  Accept: %s %s", pred, obj1.term) ;
                        newTails.add(parent);
                    }
                    else {
                        if ( DEBUG ) FmtLog.info(log, "  Reject: %s %s", pred, obj1.term) ;
                        //rowsBuilder.reject() ;
                    }
                }
            }
            tails = newTails ;
        }
        return tails.iterator() ;
    }

    private static <X> void find(List<Tuple<X>> data, X predicate, List<X> space) { 
        for ( Tuple<X> tuple : data )
            if ( predicate(tuple).equals(predicate) )
                space.add(object(tuple)) ;
    }
    
    private static <X> boolean contains(List<Tuple<X>> data, X predicate, X value) { 
        for ( Tuple<X> tuple : data ) {
            if ( predicate(tuple).equals(predicate) && object(tuple).equals(value) )
                return true ;
        }
        return false ;
    }
    

    public final /*package*/ static <X> Iterator<Row<X>> executeVarSubject$(final Var varSubject,
                                                                            final AccessData<X> dataAccessor,
                                                                            AccessRows<X> accessor, 
                                                                            PredicateObjectList<X> predObjList) {
        // Put in a StepSubstitutionJoin in case there is an incoming initial input  
        // tuple = 
        
        final Tuple<Slot<X>> tuple = predObjList.createTupleSlot(0) ;
        final PredicateObjectList<X> predObjList2 = predObjList.slice(1) ;
        
        Explain2.explain(Quack.quackExec, "  AccessOps.executeVarSubject$ : subject=%s %s",varSubject, tuple) ;
        // Match the subjects.
        Iterator<Row<X>> rows = accessor.accessRows(tuple) ; 
        
        //X g = (predObjList.getGraph() == null) ? null : predObjList.getGraph().term ;
        
        Iterator<Row<X>> iter = new RepeatApplyIterator<Row<X>>(rows) {
            @Override
            protected Iterator<Row<X>> makeNextStage(Row<X> row) {
                X s = row.get(varSubject) ;
                Explain2.explain(Quack.quackExec, "  AccessOps.executeVarSubject$ : makeNextStage: %s", row) ;
                //Ground, for this row.
                final PredicateObjectList<X> predObjList3 = EngLib.substitute(predObjList2, row) ;
                RowListBuilder<X> builder = new RowListBuilderBase<>() ;
                // XXX Repeat cache possibilities here!
                // XXX Graph?
                Tuple<X> accessTupleSub = tuple.len() == 3 ? TupleFactory.tuple(s, null, null) : TupleFactory.tuple(null, s, null, null) ;
                // XXX DataAccess?
                Iterator<Row<X>> results = executeTermSubject$(accessTupleSub, dataAccessor, predObjList3) ;
                
                if ( Explain2.isActive(Quack.quackExec) ) {
                    List<Row<X>> x = Iter.toList(results) ;
                    Explain2.explain(Quack.quackExec, "  AccessOps.executeVarSubject$ : #results: %d", x.size()) ;
                    results = RowLib.mergeRows(x.iterator(), row) ;
                }
                return RowLib.mergeRows(results, row) ;
            }
            
            @Override public void close() { super.close() ; }
        } ;
        
        // collapse iter
        //iter = Iter.iterator(iter) ;
        return iter ;
    }
}
