/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.BackwardRuleInfGraphI;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import java.util.*;

/**
 * Wraps up the results an LP rule engine instance into a conventional
 * iterator. Ensures that the engine is closed and detached from the
 * inference graph if the iterator hits the end of the result set.
 */
public class LPTopGoalIterator implements ClosableIterator<Triple>, LPInterpreterContext {
    /** The next result to be returned, or null if we have finished */
    Triple lookAhead;

    /** The parent backward chaining engine - nulled on close */
    LPInterpreter interpreter;

    /** The parent InfGraph -- retained on close to allow CME detection */
    BackwardRuleInfGraphI infgraph;
    
    /** The set of choice points that the top level interpter is waiting for */
    protected Set<ConsumerChoicePointFrame> choicePoints = new HashSet<>();

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

    /* A Note on lock ordering:
     * 
     * Elsewhere code takes an LPBRuleEngine then an LPTopGoalIterator
     * Ensure we do that lock order here as well as just synchronized 
     * on the method reverses the lock ordering, leading to deadlock.
     */
    
    
    /**
     * Find the next result in the goal state and put it in the
     * lookahead buffer.
     */
    private void moveForward() {
        LPBRuleEngine lpEngine ;
        synchronized(this)
        {
            checkClosed();
            lpEngine = interpreter.getEngine();
        }
        synchronized (lpEngine) {
            synchronized(this)
            {
                checkClosed();

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
            for ( ConsumerChoicePointFrame ccp : choicePoints )
            {
                if ( ccp.isReady() )
                {
                    if ( nextToRun == null )
                    {
                        nextToRun = ccp;
                    }
                    isReady = true;
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
    public void close() {
        LPBRuleEngine lpEngine ;
        synchronized(this)
        {
            if ( interpreter == null ) return ;
            lpEngine = interpreter.getEngine();
        }

        synchronized (lpEngine) {
            // Elsewhere code takes an LPBRuleEngine then an LPTopGoalIterator
            // Ensure we do that lock order here as well as just synchronized 
            // on the method reverses the locks takne, leading to deadlock.
            synchronized(this)
            {
                if (interpreter != null) {
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
