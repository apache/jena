/******************************************************************
 * File:        Generator.java
 * Created by:  Dave Reynolds
 * Created on:  06-Aug-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Generator.java,v 1.3 2003-08-07 21:06:20 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import java.util.*;

import com.hp.hpl.jena.reasoner.rulesys.impl.StateFlag;

/**
 * A generator represents a set of memoized results for a single 
 * tabled subgoal. The generator may be complete (in which case it just
 * contains the complete cached set of results for a goal), ready (not complete
 * but likely to product more results if called) or blocked (not complete and
 * awaiing results from a dependent generator).
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-08-07 21:06:20 $
 */
public class Generator {

    /** The intepreter instance which generates the results for this goal, 
     *  null if the generator is complete */
    protected LPInterpreter interpreter;
    
//    /** The choice point frame at which the interpreter should restart */
//    protected FrameObject choicePoint;
    
    /** The ordered set of results available for the goal */
    protected ArrayList results = new ArrayList();
    
    /** A indexed version of the result set, used while the generator is live 
     *  to detect duplicate results */
    protected Set resultSet = new HashSet();
    
    /** set to true if the dependent generator has new results ready for us */
    protected boolean isReady = true;
    
    /** The generator, if any, which this generator is currently awaiting results from */
    protected Generator dependsOn = null;
    
    /** The list of generators which are awaint results from us */
    protected List dependents = new ArrayList();
    
    /**
     * Constructor.
     * 
     * @param interpreter an initialized interpreter instance that will answer 
     * results for this generator.
     */
    public Generator(LPInterpreter interpreter) {
        this.interpreter = interpreter;
    }
    
    /**
     * Signal that this generator is complete, no more results can be created.
     */
    public void setComplete() {
        if (!isComplete()) {
            interpreter.close();
            interpreter = null;
            resultSet = null;
            isReady = false;
            if (dependsOn != null) {
                dependsOn.removeDependent(this);
            }
            dependsOn = null;
            // Propagate completion now
            for (Iterator i = dependents.iterator(); i.hasNext(); ) {
                Generator dep = (Generator)i.next();
                dep.setComplete();
            }
            dependents = null;
        }
    }
    
//    /**
//     * Return the interpeter choice point state at which this generator should resume.
//     */
//    public void setChoicePoint(FrameObject choice) {
//        choicePoint = choice;
//    }
    
    /**
     * Signal that the generator that we are blocked on has new results.
     */
    public void setReady() {
        if (!isReady) {
            if (dependsOn != null) {
                dependsOn.removeDependent(this);
                dependsOn = null;
            }
            isReady = true;
            interpreter.getEngine().schedule(this);
        }
    }
    
    /**
     * Return true if the generator is ready to be scheduled (i.e. it is not
     * known to be complete and not known to be waiting for a dependent generator).
     */
    public boolean isReady() {
        return isReady;
    }
    
    /**
     * Return true if the generator is complete.
     */
    public boolean isComplete() {
        return interpreter == null;
    }
    
    /**
     * Remove the given generator from the list of dependents of this one.
     */
    protected void removeDependent(Generator dep) {
        dependents.remove(dep);
    }
    
    /**
     * Add the given generator to the list of dependents of this one.
     */
    protected void addDependent(Generator dep) {
        dependents.add(dep);
    }
    
    /**
     * Cycle this generator until it either completes or blocks.
     */
    public synchronized void pump() {
        if (!isReady()) return;
        int priorNresults = results.size();
        boolean finished = false;
        List notifyList = dependents;
        while (!finished) {
            Object result = interpreter.next();
            if (result == StateFlag.FAIL) {
                setComplete();
                finished = true;
            } else if (result == StateFlag.SUSPEND) {
                blockon(interpreter.getBlockingGenerator());
                if (isIndirectlyComplete()) {
                    setComplete();
                }
                finished = true;
            } else {
                // Simple triple result
                if (resultSet.add(result)) {
                    results.add(result);
                }
            }
        }
        if (results.size() > priorNresults) {
            propagateResultState(notifyList);
        }
    }
    
    /**
     * Record that this generator is blocked on the given dependent.
     */
    protected void blockon(Generator dep) {
        dependsOn = dep;
        dep.addDependent(this);
        isReady = false;
    }
    
    /**
     * Called when this generator has more results available which might unblock
     * some dependents.
     */
    protected void propagateResultState(List notifyList) {
        for (Iterator i = notifyList.iterator(); i.hasNext(); ) {
            ((Generator)i.next()).setReady();
        }
    }
    
    /**
     * Check for deadlocked states where none of the generators we are (indirectly)
     * dependent on can run.
     */
    protected boolean isIndirectlyComplete() {
        HashSet visited = new HashSet();
        visited.add(this);
        return doIsIndirectlyComplete(visited);
    }
    
    /**
     * Check for deadlocked states where none of the generators we are (indirectly)
     * dependent on can run.
     */
    protected boolean doIsIndirectlyComplete(Set visited) {
        if (isReady()) {
            return false;
        } else if (visited.add(this)) {
            if (dependsOn == null) {
                return true;
            } else {
                return dependsOn.doIsIndirectlyComplete(visited);
            }
        } else {
            return true;
        }
    }
    
}


/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/