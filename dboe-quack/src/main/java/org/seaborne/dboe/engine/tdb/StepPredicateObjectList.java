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

import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.RepeatApplyIterator ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.access.AccessorBase ;
import org.seaborne.dboe.engine.row.RowBuilderBase ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Get a number of predicate-objects, all with the same subject (the same variable or the same term) . */
public class StepPredicateObjectList implements Step<NodeId> {
    
    private final PredicateObjectList<NodeId> predObjs ;
    private final AccessorTDB accessor ;
    
    private boolean multipleSamePredicate = false ; 
    
    public StepPredicateObjectList(Slot<NodeId> graph, Slot<NodeId> subject, AccessorTDB accessor) {
        predObjs = new PredicateObjectList<>(graph, subject) ;
        this.accessor = accessor ;
    }
    
    public void add(Slot<NodeId> p, Slot<NodeId> o) {
        if ( p.isVar() )
            throw new InternalErrorException() ;
        predObjs.add(p, o);
    }
    
    /** Get subject for this predicate-object list */
    public Slot<NodeId> getSubject() {
        return predObjs.getSubject() ;
    }

    /** Get graph for this predicate-object list : maybe null (default graph); maybe union */
    public Slot<NodeId> getGraph() {
        return predObjs.getGraph() ;
    }

    public static boolean UseNaiveExecution = false ; 
    
    @Override
    public RowList<NodeId> execute(RowList<NodeId> input) {
        // Very simple execution that should get the right answers reliably.
        if ( UseNaiveExecution )
            return executeUnwind(input) ;
        
        if ( predObjs.isEmpty() )
            return input ; 

        if ( predObjs.size() == 1 ) {
            Tuple<Slot<NodeId>> tuple = predObjs.createTupleSlot(0) ;
            Step<NodeId> step = new StepSubstitutionJoin<NodeId>(tuple, accessor, new RowBuilderBase<NodeId>()) ;
            return step.execute(input) ;
        }
        Set<Var> vars = new HashSet<>() ;
        vars.addAll(input.vars()) ;
        vars.addAll(predObjs.getVars()) ;

        // If input is the identity rowlist ...
        if ( input.isIdentity() )
            return RowLib.createRowList(vars, executePredObjList(predObjs)) ;
        else {
            Iterator<Row<NodeId>> iter = new RepeatApplyIterator<Row<NodeId>>(input.iterator()) {
                @Override
                protected Iterator<Row<NodeId>> makeNextStage(Row<NodeId> row) {
                    PredicateObjectList<NodeId> predObjs2 = predObjs ;
                    if ( ! row.isIdentity() )
                        predObjs2 = EngLib.substitute(predObjs, row) ;
                    Iterator<Row<NodeId>> results = executePredObjList(predObjs2) ;
                    if ( ! row.isIdentity() )
                        results = RowLib.mergeRows(results, row) ;
                    return results ;
                }
            } ;
            return RowLib.createRowList(vars, iter) ;
        }
    }
    
    private Iterator<Row<NodeId>> executePredObjList(PredicateObjectList<NodeId> predObjs2) {
        return accessor.fetch(predObjs2) ;
    }

    private String str(Object obj) {
        if ( obj == null )
            return "null" ;
        if ( obj instanceof NodeId ) {
            NodeId nid = (NodeId)obj ;
            return nid.toString() + "/" + FmtUtils.stringForNode(accessor.getNodeTable().getNodeForNodeId(nid)) ;
        }
        return obj.toString() ;
    }

    private static RowList<NodeId> createRowList(Set<Var> vars, List<Row<NodeId>> rowList) {
        return RowLib.createRowList(vars, rowList.iterator()) ;
    }
        
    /** Utility to execute by converting to a list of one-steps and executing.
     * Useful for testing.
     */
    private RowList<NodeId> executeUnwind(RowList<NodeId> input) {
        return AccessorBase.fetch(accessor, predObjs) ;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder() ;
        buff.append("StepPredicateObjectList: ") ;
        if ( getGraph() != null )
            buff.append(str(getGraph())).append("/") ;
        buff.append(getSubject()) ;
        
        for ( int i = 0 ; i < predObjs.size() ; i ++ ) {
            buff.append(" ") ;
            NodeId p = predObjs.getPredicate(i) ;
            Slot<NodeId> o = predObjs.getObject(i) ;
            buff.append("(").append(p).append(",").append(o).append(")") ;
        }
        return buff.toString() ;
    }
}