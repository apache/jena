/******************************************************************
 * File:        LPEnvironment.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: EnvironmentFrame.java,v 1.6 2003-08-14 17:49:06 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.graph.Node;

/**
 * Represents a single frame in the LP interpreter's environment stack. The
 * environment stack represents the AND part of the search tree - it is a sequence
 * of nested predicate calls.
 * <p>
 * This is used in the inner loop of the interpreter and so is a pure data structure
 * not an abstract data type and assumes privileged access to the interpreter state.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2003-08-14 17:49:06 $
 */
public class EnvironmentFrame extends FrameObject {

    /** The set of permanent variables Y(i) in use by this frame.  */
    Node[] pVars;
    
    /** The code the the clause currently being processed */
    RuleClauseCode clause;
    
    /** The continuation program counter offet in the parent clause's byte code */
    int cpc;
    
    /** The continuation argument counter offset in the parent clause's arg stream */
    int cac;
    
    /** 
     * Constructor 
     * @param clause the compiled code being interpreted by this env frame 
     */
    public EnvironmentFrame(RuleClauseCode clause) {
        this.clause = clause;
    }
    
    /**
     * Initialize a starting frame.
     * @param clause the compiled code being interpreted by this env frame 
     */
    public void init(RuleClauseCode clause) { 
        this.clause = clause;
    }
    
    /**
     * Allocate a vector of permanent variables for use in the rule execution.
     */
    public void allocate(int n) {
            pVars = new Node[n];
    }
    
    /**
     * Printable string for debugging.
     */
    public String toString() {
        if (clause == null || clause.rule == null) {
            return "[anon]";
        } else {
            return "[" + clause.rule.toShortString() + "]";
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