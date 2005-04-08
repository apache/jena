/******************************************************************
 * File:        ConsumerChoicePoint.java
 * Created by:  Dave Reynolds
 * Created on:  07-Aug-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: ConsumerChoicePointFrame.java,v 1.8 2005-04-08 13:02:10 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

import java.util.*;

/**
 * Frame in the LPInterpreter's control stack used to represent matching
 * to the results of a tabled predicate. Conventionally the system state which
 * finds and tables the goal results is called the generator and states which
 * require those results are called consumers.
 * <p>
 * This is used in the inner loop of the interpreter and so is a pure data structure
 * not an abstract data type and assumes privileged access to the interpreter state.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.8 $ on $Date: 2005-04-08 13:02:10 $
 */
public class ConsumerChoicePointFrame extends GenericTripleMatchFrame 
                implements LPAgendaEntry, LPInterpreterState {
        
    /** The generator whose tabled results we are selecting over */
    protected Generator generator;
    
    /** The index in the generator's result set that we have reached so far. */
    protected int resultIndex;
    
    /** The preserved permanent registers for the pickled interpreter */
    protected Node[] pVars;

    /** The preserved trail variables for the picked interpreter */
    protected Node_RuleVariable[] trailVars;

    /** The preserved trail bound values for the picked interpreter */
    protected Node[] trailValues;
    
    /** The length of the preserved trail */
    protected int trailLength;
    
    /** The generator or top iterator we are producting results for */
    protected LPInterpreterContext context;
        
    /**
     * Constructor.
     * @param interpreter the parent interpreter whose state is to be preserved here, its arg stack
     * defines the parameters for the target goal
     */
    public ConsumerChoicePointFrame(LPInterpreter interpreter) {
        init(interpreter);
    }
    
    /**
     * Initialize the choice point state.
     * @param interpreter the parent interpreter whose state is to be preserved here, its arg stack
     * defines the parameters for the target goal
     */
    public void init(LPInterpreter interpreter) {
        super.init(interpreter);
        context = interpreter.getContext();
        generator = interpreter.getEngine().generatorFor(goal);
        generator.addConsumer(this);
        resultIndex = 0;
    }
    
    /**
     * Preserve the state of an interpreter into this frame.
     */
    public void preserveState(List trail) {
        // Save the trail state
        int trailLen = trail.size();
        if (trailLen > trailLength) {
            trailValues = new Node[trailLen];
            trailVars = new Node_RuleVariable[trailLen];
        }
        trailLength = trailLen;
        for (int i = 0; i < trailLen; i++) {
            Node_RuleVariable var = (Node_RuleVariable) trail.get(i);
            trailVars[i] = var;
            trailValues[i] = var.getRawBoundValue();
        }
        // Save the permanent variables
        Node[] currentPVars = envFrame.pVars;
        if (currentPVars != null) {
            if (pVars == null || pVars.length < currentPVars.length) {
                pVars = new Node[currentPVars.length];
            }
            System.arraycopy(currentPVars, 0, pVars, 0, currentPVars.length);
        }
    }
    
    /**
     * Restore the state of an interpreter from this frame
     */
    public void restoreState(LPInterpreter interp) {
        interp.unwindTrail(0);
        for (int i = 0; i < trailLength; i++) {
            interp.bind(trailVars[i], trailValues[i]);
        }
        if (pVars != null) {
            System.arraycopy(pVars, 0, envFrame.pVars, 0, pVars.length);
        }
    }
    
    /**
     * Find the next result triple and bind the result vars appropriately.
     * @param interpreter the calling interpreter whose trail should be used
     * @return FAIL if there are no more matches and the generator is closed, SUSPEND if
     * there are no more matches but the generator could generate more, SATISFIED if
     * a match has been found.
     */
    public synchronized StateFlag nextMatch(LPInterpreter interpreter) {
        while (resultIndex < generator.results.size()) {
            Triple result = (Triple) generator.results.get(resultIndex++);
            // Check if we have finished with this generator
            if (resultIndex >= generator.results.size() && generator.isComplete()) {
                generator.removeConsumer(this);
            }
            if (bindResult(result, interpreter)) {
                return StateFlag.SATISFIED;
            }            
        }
        if (generator.isComplete()) {
            setFinished();
            generator.removeConsumer(this);
            return StateFlag.FAIL;
        } else {
            return StateFlag.SUSPEND;
        }
    }
    
    /**
     * Return true if this choice point could usefully be restarted.
     */
    public boolean isReady() {
        return generator.numResults() > resultIndex;
    }
    
    /**
     * Called by generator when there are more results available.
     */
    public void setReady() {
        context.setReady(this);
    }
    
    /**
     * Notify that this consumer choice point has finished consuming all
     * the results of a closed generator.
     */
    public void setFinished() {
        context.notifyFinished(this);
    }
    
    /**
     * Reactivate this choice point to return new results.
     */
    public void pump() {
        if (context instanceof Generator) {
            ((Generator)context).pump(this);
        } else {
            // The top level iterator is in charge and will restore and run this choice point itself
        }
    }
    
    /**
     * Return the generator associated with this entry (might be the entry itself)
     */
    public Generator getGenerator() {
        return generator;
    }
    
    /**
     * Return the interpeter context which is reading the results of this consumer.
     */
    public LPInterpreterContext getConsumingContext() {
        return context;
    }

}


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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