/******************************************************************
 * File:        LPTopGoalIterator.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPTopGoalIterator.java,v 1.3 2003-08-06 17:00:22 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import java.util.NoSuchElementException;

import com.hp.hpl.jena.reasoner.rulesys.impl.StateFlag;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

/**
 * Wraps up the results an LP rule engine instance into a conventional
 * iterator. Ensures that the engine is closed and detached from the 
 * inference graph if the iterator hits the end of the result set.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-08-06 17:00:22 $
 */
public class LPTopGoalIterator implements ClosableIterator {
    /** The next result to be returned, or null if we have finished */
    Object lookAhead;
    
    /** The parent backward chaining engine */
    LPInterpreter interpreter;
    
    /** True if the iteration has started */
    boolean started = false;
    
    /**
     * Constructor. Wraps a top level goal state as an iterator
     */
    public LPTopGoalIterator(LPInterpreter engine) {
        this.interpreter = engine;
    }
    
    /**
     * Find the next result in the goal state and put it in the
     * lookahead buffer.
     */
    private void moveForward() {
        lookAhead = interpreter.next();
        if (lookAhead == StateFlag.FAIL) {
            close();
        } else if (lookAhead == StateFlag.SUSPEND) {
            interpreter.getEngine().pump(interpreter.getBlockingGenerator());
            moveForward();
        }
        started = true;
    }
    
    /**
     * @see com.hp.hpl.jena.util.iterator.ClosableIterator#close()
     */
    public void close() {
        lookAhead = null;
        interpreter.close();
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