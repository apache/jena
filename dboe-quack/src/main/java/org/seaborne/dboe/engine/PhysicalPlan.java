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

import java.util.List ;

import org.apache.jena.atlas.lib.DS ;

/** A physical plan is a sequence of steps, executes one after another
 *  with the output of the previous one feeding into the next.
 * 
 *  The naming favours linear plans but a step may itself execute other steps,
 *  and a physical plan is itself a step.
 */
public class PhysicalPlan<X> implements Step<X> {
    private List<Step<X>> steps = DS.list() ;
    
    public PhysicalPlan() {}
    
    /** If a plan contains StepNothing, it must execute to nothing */
    public boolean executesToNothing() {
        for (Step<X> s : steps )
             if (s instanceof StepNothing<?> )
                 return true ;
        return false ;
    }
    
    public void append(PhysicalPlan<X> plan) {
        for (Step<X> s : plan.steps )
            add(s) ; 
    }
    
    public void add(Step<X> step) { 
        steps.add(step) ;
    }
    
    @Override
    public RowList<X> execute(RowList<X> input) {
        RowList<X> chain = input ;
        for ( Step<X> step : steps) {
            chain = step.execute(chain) ;
        }
        return chain ;
    }  
    
    public RowList<X> executeDebug(RowList<X> input) {
        RowList<X> chain = input ;
        for ( Step<X> step : steps) {
            chain = step.execute(chain) ;
            if ( ! ( step instanceof StepDebug<?> ) )
                chain = new StepDebug<X>().execute(chain) ;
        }
        return chain ;
    }  

    @Override
    public String toString() {
        StringBuilder sbuff = new StringBuilder() ;
        sbuff.append("plan:") ;
        if ( steps.isEmpty() ) return "plan:empty" ;
        for ( Step<X> s : steps )
            sbuff.append("\n  ").append(s.toString()) ;
        return sbuff.toString() ;
    }
    
    public String steps() {
        if ( steps.isEmpty() ) return "plan:empty" ;
        StringBuilder sbuff = new StringBuilder() ;
        for ( Step<X> s : steps ) {
            if ( sbuff.length() == 0 )
                sbuff.append("  ") ;
            else
                sbuff.append("\n  ") ;
            sbuff.append(s.toString()) ;
        }
        return sbuff.toString() ;
    }

}
