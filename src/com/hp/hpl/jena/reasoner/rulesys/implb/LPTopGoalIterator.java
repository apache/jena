/******************************************************************
 * File:        LPTopGoalIterator.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPTopGoalIterator.java,v 1.8 2003-08-14 07:51:10 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import java.util.NoSuchElementException;

import com.hp.hpl.jena.reasoner.rulesys.impl.StateFlag;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import java.util.*;

/**
 * Wraps up the results an LP rule engine instance into a conventional
 * iterator. Ensures that the engine is closed and detached from the 
 * inference graph if the iterator hits the end of the result set.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.8 $ on $Date: 2003-08-14 07:51:10 $
 */
public class LPTopGoalIterator implements ClosableIterator, LPInterpreterContext {
    /** The next result to be returned, or null if we have finished */
    Object lookAhead;
    
    /** The parent backward chaining engine */
    LPInterpreter interpreter;
    
    /** The set of choice points that the top level interpter is waiting for */
    protected Set choicePoints = new HashSet();
    
    /** The choice point most recently notified as ready to run. */
    protected ConsumerChoicePointFrame nextToRun;
    
    /** set to true if we should be able to usefully run */
    protected boolean isReady = true;
    
    /** set to true if at least one branch has block so an active readiness check is required */
    protected boolean checkReadyNeeded = false;

    /** True if the iteration has started */
    boolean started = false;
    
    /**
     * Constructor. Wraps a top level goal state as an iterator
     */
    public LPTopGoalIterator(LPInterpreter engine) {
        this.interpreter = engine;
//        engine.setState(this);
        engine.setTopInterpreter(this);
    }
    
    /**
     * Find the next result in the goal state and put it in the
     * lookahead buffer.
     */
    private void moveForward() {
        synchronized (interpreter.getEngine().getInfGraph()) {
            started = true;
            lookAhead = interpreter.next();
            if (lookAhead == StateFlag.FAIL) {
                if (choicePoints.isEmpty()) {
                    // Nothing left to try
                    close();
                } else {
                    // Some options open, continue pumping
                    nextToRun = null;
                    interpreter.getEngine().pump(this);
                    if (nextToRun == null) {
                        // Reached final closure
                        close();
                    } else {
                        interpreter.setState(nextToRun);
                        moveForward();
                    }
                }
            }
        }
    }

    /** Notify this context that a brach was suspended awaiting futher
     *  results from the given generator. */
    public void notifyBlockedOn(ConsumerChoicePointFrame ccp) {
        choicePoints.add(ccp);
        checkReadyNeeded = true;
    }
    
    /** 
     * Notify this context that the given choice point has terminated
     * and can be remove from the wait list. 
     */
    public void notifyFinished(ConsumerChoicePointFrame ccp) {
        choicePoints.remove(ccp);
        checkReadyNeeded = true;
    }
    
    /**
     * Directly set that this generator is ready (because the generating
     * for one of its generatingCPs has produced new results).
     */
    public void setReady(ConsumerChoicePointFrame ccp) {
        nextToRun = ccp;
        isReady = true;
        checkReadyNeeded = false;
    }
    
    /**
     * Return true if the iterator is ready to be scheduled (i.e. it is not
     * known to be complete and not known to be waiting for a dependent generator).
     */
    public boolean isReady() {
        if (checkReadyNeeded) {
            isReady = false;
            for (Iterator i = choicePoints.iterator(); i.hasNext(); ) {
                if ( ((ConsumerChoicePointFrame)i.next()).isReady() ) {
                    isReady =  true;
                    break; 
                }
            }
            checkReadyNeeded = false;
            return isReady;
        } else {
            return isReady;
        }
    }
        
    /**
     * @see com.hp.hpl.jena.util.iterator.ClosableIterator#close()
     */
    public void close() {
        if (interpreter != null) {
            lookAhead = null;
            interpreter.close();
            interpreter = null;
            isReady = false;
            checkReadyNeeded = false;
            nextToRun = null;
            choicePoints = null;
        }
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        if (!started) moveForward();
        return (lookAhead != null);
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Object next() {
        if (!started) moveForward();
        if (lookAhead == null) {
            throw new NoSuchElementException("Overran end of LP result set");
        }
        Object result = lookAhead;
        moveForward();
        return result;
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
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