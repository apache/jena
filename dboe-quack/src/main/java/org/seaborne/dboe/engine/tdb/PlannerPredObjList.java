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

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.tdb2.store.NodeId ;

public class PlannerPredObjList implements Planner {
    private AccessorTDB accessor ;

    public PlannerPredObjList(AccessorTDB accessor) { this.accessor = accessor ; }
    
    @Override
    public PhysicalPlan<NodeId> generatePlan(List<Tuple<Slot<NodeId>>> tuples) {
        // ---- Share with PlannerTDB
        PhysicalPlan<NodeId> plan = new PhysicalPlan<NodeId>() ;
        if ( tuples.size() == 0 ) {
            plan.add(new StepPassThrough<NodeId>()) ;
            return plan ;
        }

        if ( tuples.size() == 1 ) {
            Step<NodeId> step = PlannerSubstitution.plan1(tuples.get(0), accessor) ;
            plan.add(step);
            return plan ;
        }
        // ---- Share with PlannerTDB

        int N = tuples.get(0).len() ;
        if ( N !=3 && N != 4 )
            throw new NotImplemented("Tuple size not 3 or 4") ;
        
        // Copy.
        tuples = new ArrayList<>(tuples) ;
        for ( int i = 0 ; i < tuples.size() ; i++ ) {
            Tuple<Slot<NodeId>> t1 = null ;
            for ( ; i < tuples.size() ; i++ ) {
                t1 = tuples.get(i) ;
                if ( t1 != null )
                    break ;
            }

            if ( t1 == null )
                break ;
            
            if ( N == 4 ) {
                
            }

            if ( EngLib.predicate(t1).isVar() ) {
                // Variable in the predicate position : _ ?p _
                // Treat as an individual tuple.
                Step<NodeId> step = PlannerSubstitution.plan1(t1, accessor) ;
                plan.add(step);
                continue ;
            }

            if ( false ) {
                // if we want to treat variable subjects as individual tuples.
                if ( EngLib.subject(t1).isVar() ) {
                    Step<NodeId> step = PlannerSubstitution.plan1(t1, accessor) ;
                    plan.add(step);
                    continue ;
                }
            }

            // Same subject step 
            Slot<NodeId> g = EngLib.graph(t1) ;
            StepPredicateObjectList step = new StepPredicateObjectList(g , EngLib.subject(t1), accessor) ;
            plan.add(step) ;
            step.add(EngLib.predicate(t1), EngLib.object(t1)) ;

            // Find all common subjects (adjacent // all pattern)
            for ( int j = i+1 ; j < tuples.size() ; j++ ) {
                Tuple<Slot<NodeId>> t2 = tuples.get(j) ;
                if ( t2 == null ) continue ;
                boolean sameGroup = true ;
                
                if ( g != null )
                    sameGroup = EngLib.graph(t2).equals(step.getGraph()) ;
                sameGroup = sameGroup && EngLib.subject(t2).equals(step.getSubject()) ;
                if ( sameGroup ) {
                    step.add(EngLib.predicate(t2), EngLib.object(t2)) ;
                    tuples.set(j, null) ;
                }
                // Adjacent only -- add "else break"
            }
        }
        return plan ;
    }
}