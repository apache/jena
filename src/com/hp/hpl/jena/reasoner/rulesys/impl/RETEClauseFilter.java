/******************************************************************
 * File:        RETEClauseFilter.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RETEClauseFilter.java,v 1.1 2003-06-09 08:28:19 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;

/**
 * Checks a triple against the grounded matches and intra-triple matches
 * for a single rule clause. If the match passes it creates a binding
 * environment token and passes it on the the RETE network itself. The checks
 * and bindings are implemented using a simple byte-coded interpreter.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-06-09 08:28:19 $
 */
public class RETEClauseFilter {
    
    /** Contains the set of byte-coded instructions and argument pointers */
    protected byte[] instructions;
    
    /** Contains the object arguments referenced from the instructions array */
    protected Object[] args;
    
    /** The network node to receive any created tokens */
    protected RETENode continuation;
    
    /** Instruction code: Check triple entry (arg1) against literal value (arg2). */
    public static final byte TESTValue = 0x01;
    
    /** Instruction code: Cross match two triple entries (arg1, arg2) */
    public static final byte TESTIntraMatch = 0x02;
    
    /** Instruction code: Create a result environment of length arg1. */
    public static final byte CREATEToken = 0x03;
    
    /** Instruction code: Bind a node (arg1) to a place in the rules token (arg2). */
    public static final byte BIND = 0x04;
    
    /** Instruction code: Final entry - dispatch to the network. */
    public static final byte END = 0x05;
    
    /** Argument addressing code: triple subject */
    public static final byte ADDRSubject = 0x01;
    
    /** Argument addressing code: triple predicate */
    public static final byte ADDRPredicate = 0x02;
    
    /** Argument addressing code: triple object as a whole */
    public static final byte ADDRObject = 0x03;
    
    /** Argument addressing code: triple object functor name (null if not functor) */
    public static final byte ADDRFunctorName = 0x04;
    
    /** Argument addressing code: triple object functor node, offset in low nibble */
    public static final byte ADDRFunctorNode = 0x10;
        
    /**
     * Contructor.
     * @param instructions the set of byte-coded instructions and argument pointers.
     * @param args the object arguments referenced from the instructions array.
     */
    public RETEClauseFilter(byte[] instructions, Object[] args) {
        this.instructions = instructions;
        this.args = args;
    }
    
    /**
     * Set the continuation node for this node.
     */
    public void setContinuation(RETENode continuation) {
        this.continuation = continuation;
    }

    /**
     * Insert or remove a triple into the network.
     * @param triple the triple to process.
     * @param isAdd true if the triple is being added to the working set.
     */
    public void fire(Triple triple, boolean isAdd) {
        // TODO: implement filter
        // rest are just dummies
        BindingEnvironment env = new BindingVector(new Node[10]);
        continuation.fire(env, isAdd);
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