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

import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.Tuple ;
import static org.seaborne.dboe.engine.EngLib.* ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.row.RowBuilderBase ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import org.apache.jena.sparql.core.Var ;
import org.apache.jena.tdb.store.NodeId ;

/** Given a {@code List<Tuple<Slot<NodeId>>>} (a basic graph pattern or quad pattern),
 *  generate a physical plan.  Assumes input is in the "best" order.
 *  <p>
 *  This does not do any reordering except that merges (if used)
 *  will pick tuples out of order.
 *  <p>
 *  Merge is usually not enabled (requires specific, non-standard-TDB1 index structure). 
 *  <p>
 *  Does do unionDefaultGraph.
 */
public class PlannerSubstitution implements Planner {
    // Log for explain and system mesages
    
    // Turn merge join processing on or off.
    public static boolean DOMERGE = true ;

    private static Logger syslog = Quack.log ;
    // Log for internal messages
    private static Logger log = LoggerFactory.getLogger(PlannerSubstitution.class) ;

    private final AccessorTDB accessor ; 
    
    public PlannerSubstitution(AccessorTDB accessor) {
        this.accessor = accessor ;
    }
    
    // XXX This is odd - used by PlannerPredObjList
    public static Step<NodeId> plan1(Tuple<Slot<NodeId>> tuple, AccessorTDB accessor) {
        return new PlannerSubstitution(accessor).plan1(tuple) ;
    }
    
    public Step<NodeId> plan1(Tuple<Slot<NodeId>> tuple) {
        // Put in a StepSubstitutionJoin in case there is an incoming initial input  
        Step<NodeId> step = new StepSubstitutionJoin<NodeId>(tuple, accessor, new RowBuilderBase<NodeId>()) ;
        return step ;
    }
    
    /** Decide an execution plan for a pattern of tuples of slots of NodeId,
     *  given a tuple table (which must be "appropiate" for the pattern,
     *  including right width).   
     */
    @Override
    public PhysicalPlan<NodeId> generatePlan(List<Tuple<Slot<NodeId>>> tuples) {
        PhysicalPlan<NodeId> plan = new PhysicalPlan<NodeId>() ;
        
        if ( tuples == null ) {
            // caused by a term being "not found"
            syslog.warn("Null tuples");
            plan.add(new StepNothing<NodeId>()) ;
            return plan ;
        }
        
        if ( tuples.size() == 0 ) {
            plan.add(new StepPassThrough<NodeId>()) ;
            return plan ;
        }

//        if ( Quack.EXPLAIN )
//            Quack.explainlog.debug("Pattern size = "+tuples.size()) ;
        
        if ( tuples.size() == 1 ) {
            Step<NodeId> step = plan1(tuples.get(0)) ;
            plan.add(step);
            return plan ;
        }

        int N = tuples.size() ;
        Set<Var> vars = DS.set();

        if ( false ) {
            // **** MergeJoin
            Tuple<Slot<NodeId>> t0 = tuples.get(0) ;
            boolean isVarVar = 
                subject(t0).isVar() && object(t0).isVar() ;
            
            if ( isVarVar && syslog.isDebugEnabled() )
                syslog.debug("VarVar: "+tuples) ;
            
            boolean doMerge = DOMERGE && isVarVar ;

            if ( doMerge ) {
                // Mutated by merge planner.
                tuples = DS.list(tuples) ;
                PlannerMerge.merge(plan, tuples, vars, accessor) ;
            }
        } 
        
        // Substitutions for the rest.
        for ( int i = 0 ; i < N ; i++ ) {
            Tuple<Slot<NodeId>> t = tuples.get(i) ;
            if ( t == null )
                continue ;
            accVars(vars, t) ;
            RowBuilder<NodeId> builder = new RowBuilderBase<>() ;
            Step<NodeId> step =  new StepSubstitutionJoin<NodeId>(t, accessor, builder) ;
            plan.add(step) ;
        }

        return plan ;
    }
}
