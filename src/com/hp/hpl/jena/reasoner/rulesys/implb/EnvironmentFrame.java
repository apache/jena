/******************************************************************
 * File:        LPEnvironment.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: EnvironmentFrame.java,v 1.1 2003-07-22 21:44:19 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.graph.Node;

/**
 * Represents a single frame in the LP interpreter's environment stack. The
 * environment stack represents the AND part of the search tree - it is a sequence
 * of nested predicate calls.
 * <p>
 * This is used in the inner loop of the interpreter and so is a pure data structure
 * not an abstract data type.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-07-22 21:44:19 $
 */
public class EnvironmentFrame extends FrameObject {

    /** The set of permanent variables Yi) in use by this frame.  */
    Node[] pVars;
    
    /** The code the the clause currently being processed */
    RuleClauseCode clause;
    
    /** The program counter offet in the clause's byte code */
    int pc;
    
    /** The argument counter offset in the clause's arg stream */
    int ac;
    
    /** 
     * Constructor 
     * @param factory The parent factory to which free frames can be returned
     */
    public EnvironmentFrame(LPEnvironmentFactory factory) {
        super(factory);
    }
    
    /**
     * Initialize a starting frame.
     * @param clause the compiled code being interpreted by this env frame 
     */
    public void init(RuleClauseCode clause) { 
        this.clause = clause;
        pc = 0;
        // Note that the current fixed-frame implementation is just a short cut 
        // the first implementation and will get relaced by a
        // dynamic (and possibly trimmable) implementation in the future
        pVars = new Node[RuleClauseCode.MAX_PERMANENT_VARS];
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