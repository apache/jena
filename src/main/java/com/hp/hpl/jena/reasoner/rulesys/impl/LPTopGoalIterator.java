/******************************************************************
 * File:        LPTopGoalIterator.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jul-2003
 *
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: LPTopGoalIterator.java,v 1.2 2010-05-08 19:38:22 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.NoSuchElementException;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.BackwardRuleInfGraphI;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import java.util.*;

/**
 * Wraps up the results an LP rule engine instance into a conventional
 * iterator. Ensures that the engine is closed and detached from the
 * inference graph if the iterator hits the end of the result set.
 *
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2010-05-08 19:38:22 $
 */
public class LPTopGoalIterator implements ClosableIterator<Triple>, LPInterpreterContext {
    /** The next result to be returned, or null if we have finished */
    Triple lookAhead;

    /** The parent backward chaining engine - nulled on close */
    LPInterpreter interpreter;

    /** The parent InfGraph -- retained on close to allow CME detection */
    BackwardRuleInfGraphI infgraph;
    
    /** The set of choice points that the top level interpter is waiting for */
    protected Set<ConsumerChoicePointFrame> choicePoints = new HashSet<ConsumerChoicePointFrame>();

    /** The choice point most recently notified as ready to run. */
    protected ConsumerChoicePointFrame nextToRun;

    /** set to true if we should be able to usefully run */
    protected boolean isReady = true;

    /** set to true if at least one branch has block so an active readiness check is required */
    protected boolean checkReadyNeeded = false;

    /** True if the iteration has started */
    boolean lookaheadValid = false;

    /** Version stamp of the graph when we start */
    protected int initialVersion;
    
    /**
     * Constructor. Wraps a top level goal state as an iterator
     */
    public LPTopGoalIterator(LPInterpreter engine) {
        this.interpreter = engine;
        infgraph = engine.getEngine().getInfGraph();
        initialVersion = infgraph.getVersion();
//        engine.setState(this);
        engine.setTopInterpreter(this);
    }

    /**
     * Find the next result in the goal state and put it in the
     * lookahead buffer.
     */
    private synchronized void moveForward() {
        checkClosed();
        LPBRuleEngine lpEngine = interpreter.getEngine();
        synchronized (lpEngine) {

            lookaheadValid = true;

            // TODO nasty dynamic typing here.
            Object next = interpreter.next();
            lookAhead = next instanceof Triple ? (Triple) next : null;
            if (next == StateFlag.FAIL) {
                if (choicePoints.isEmpty()) {
                    // Nothing left to try
                    close();
                } else {
                    // Some options open, continue pumping
                    nextToRun = null;
                    lpEngine.pump(this);
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
    @Override
    public void notifyBlockedOn(ConsumerChoicePointFrame ccp) {
        choicePoints.add(ccp);
        checkReadyNeeded = true;
    }

    /**
     * Notify this context that the given choice point has terminated
     * and can be remove from the wait list.
     */
    @Override
    public void notifyFinished(ConsumerChoicePointFrame ccp) {
        choicePoints.remove(ccp);
        checkReadyNeeded = true;
    }

    /**
     * Directly set that this generator is ready (because the generating
     * for one of its generatingCPs has produced new results).
     */
    @Override
    public void setReady(ConsumerChoicePointFrame ccp) {
        nextToRun = ccp;
        isReady = true;
        checkReadyNeeded = false;
    }

    /**
     * Return true if the iterator is ready to be scheduled (i.e. it is not
     * known to be complete and not known to be waiting for a dependent generator).
     */
    @Override
    public boolean isReady() {
        if (checkReadyNeeded) {
            isReady = false;
            for (Iterator<ConsumerChoicePointFrame> i = choicePoints.iterator(); i.hasNext(); ) {
                ConsumerChoicePointFrame ccp = i.next();
                if ( ccp.isReady() ) {
                    if (nextToRun == null) {
                        nextToRun = ccp;
                    }
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
    @Override
    public synchronized void close() {
        if (interpreter != null) {
            synchronized (interpreter.getEngine()) {
                // LogFactory.getLog( getClass() ).debug( "Entering close sync block on " + interpreter.getEngine() );

                // Check for any dangling generators which are complete
                interpreter.getEngine().checkForCompletions();
                // Close this top goal
                lookAhead = null;
                //LogFactory.getLog( getClass() ).debug( "Nulling and closing LPTopGoalIterator " + interpreter );
                interpreter.close();
                // was TEMP experiment: interpreter.getEngine().detach(interpreter);
                interpreter = null;
                isReady = false;
                checkReadyNeeded = false;
                nextToRun = null;
//                choicePoints = null;  // disabled to prevent async close causing problems
                //LogFactory.getLog( getClass() ).debug( "Leaving close sync block " );
            }
        }
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        checkCME();
        if (!lookaheadValid) moveForward();
        return (lookAhead != null);
    }

    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public Triple next() {
        checkCME();
        if (!lookaheadValid) moveForward();
        if (lookAhead == null) {
            throw new NoSuchElementException("Overran end of LP result set");
        }
        Triple result = lookAhead;
        lookaheadValid = false;
        return result;
    }
    
    /**
     * Check that the iterator has either cleanly closed or
     * the version stamp is still valid
     */
    private void checkCME() {
        if (initialVersion != infgraph.getVersion()) {
            throw new ConcurrentModificationException();
        }
    }
    
    /**
     * Check if the iterator has been closed and so we can't move forward safely
     */
    private void checkClosed() {
        if (interpreter == null || interpreter.getEngine() == null) {
            throw new ConcurrentModificationException("Due to closed iterator");
        }
    }

    /**
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}



/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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