/******************************************************************
 * File:        RETEClauseFilter.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: RETEClauseFilter.java,v 1.6 2003-08-27 13:11:15 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;

import java.util.*;

/**
 * Checks a triple against the grounded matches and intra-triple matches
 * for a single rule clause. If the match passes it creates a binding
 * environment token and passes it on the the RETE network itself. The checks
 * and bindings are implemented using a simple byte-coded interpreter.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2003-08-27 13:11:15 $
 */
public class RETEClauseFilter implements RETESourceNode {
    
    /** Contains the set of byte-coded instructions and argument pointers */
    protected byte[] instructions;
    
    /** Contains the object arguments referenced from the instructions array */
    protected Object[] args;
    
    /** The network node to receive any created tokens */
    protected RETESinkNode continuation;
    
    /** Instruction code: Check triple entry (arg1) against literal value (arg2). */
    public static final byte TESTValue = 0x01;
    
    /** Instruction code: Check literal value is a functor of name arg1 */
    public static final byte TESTFunctorName = 0x02;
    
    /** Instruction code: Cross match two triple entries (arg1, arg2) */
    public static final byte TESTIntraMatch = 0x03;
    
    /** Instruction code: Create a result environment of length arg1. */
    public static final byte CREATEToken = 0x04;
    
    /** Instruction code: Bind a node (arg1) to a place in the rules token (arg2). */
    public static final byte BIND = 0x05;
    
    /** Instruction code: Final entry - dispatch to the network. */
    public static final byte END = 0x06;
    
    /** Argument addressing code: triple subject */
    public static final byte ADDRSubject = 0x10;
    
    /** Argument addressing code: triple predicate */
    public static final byte ADDRPredicate = 0x20;
    
    /** Argument addressing code: triple object as a whole */
    public static final byte ADDRObject = 0x30;
    
    /** Argument addressing code: triple object functor node, offset in 
     *  low nibble, only usable after a successful TestFunctorName. */
    public static final byte ADDRFunctorNode = 0x40;
        
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
     * Create a filter node from a rule clause.
     * Clause complexity is limited to less than 50 args in a Functor.
     * @param clause the rule clause
     * @param envLength the size of binding environment that should be created on successful matches
     * @param varList a list to which all clause variables will be appended
     */
    public static RETEClauseFilter compile(TriplePattern clause, int envLength, List varList) { 
        byte[] instructions = new byte[300];
        byte[] bindInstructions = new byte[100];
        ArrayList args = new ArrayList();
        int pc = 0;   
        int bpc = 0;
        
        // Pass 0 - prepare env creation statement
        bindInstructions[bpc++] = CREATEToken;
        bindInstructions[bpc++] = (byte)envLength;
        
        // Pass 1 - check literal values
        Node n = clause.getSubject();
        if ( !n.isVariable() ) {
            instructions[pc++] = TESTValue;
            instructions[pc++] = ADDRSubject;
            instructions[pc++] = (byte)args.size();
            args.add( n );
        } else {
            bindInstructions[bpc++] = BIND;
            bindInstructions[bpc++] = ADDRSubject;
            bindInstructions[bpc++] = (byte)((Node_RuleVariable)n).getIndex();
            varList.add(n);
        }
        n = clause.getPredicate();
        if ( !n.isVariable() ) {
            instructions[pc++] = TESTValue;
            instructions[pc++] = ADDRPredicate;
            instructions[pc++] = (byte)args.size();
            args.add( clause.getPredicate() );
        } else {
            bindInstructions[bpc++] = BIND;
            bindInstructions[bpc++] = ADDRPredicate;
            bindInstructions[bpc++] = (byte)((Node_RuleVariable)n).getIndex();
            varList.add(n);
        }
        n = clause.getObject();
        if ( !n.isVariable() ) {
            if (Functor.isFunctor(n)) {
                // Pass 2 - check functor
                Functor f = (Functor)n.getLiteral().getValue();
                instructions[pc++] = TESTFunctorName;
                instructions[pc++] = (byte)args.size();
                args.add(f.getName());
                Node[] fargs = f.getArgs();
                for (int i = 0; i < fargs.length; i++) {
                    Node fn = fargs[i];
                    byte addr = (byte) (ADDRFunctorNode | (0x0f & i));
                    if ( !fn.isVariable() ) {
                        instructions[pc++] = TESTValue;
                        instructions[pc++] = addr;
                        instructions[pc++] = (byte)args.size();
                        args.add( fn );
                    } else {
                        bindInstructions[bpc++] = BIND;
                        bindInstructions[bpc++] = addr;
                        bindInstructions[bpc++] = (byte)((Node_RuleVariable)fn).getIndex();
                        varList.add(fn);
                    }
                }
            } else {
                instructions[pc++] = TESTValue;
                instructions[pc++] = ADDRObject;
                instructions[pc++] = (byte)args.size();
                args.add( n );
            }
        } else {
            bindInstructions[bpc++] = BIND;
            bindInstructions[bpc++] = ADDRObject;
            bindInstructions[bpc++] = (byte)((Node_RuleVariable)n).getIndex();
            varList.add(n);
        }
        bindInstructions[bpc++] = END;
        
        // Pass 4 - Pack instructions
        byte[] packed = new byte[pc + bpc];
        System.arraycopy(instructions, 0, packed, 0, pc);
        System.arraycopy(bindInstructions, 0, packed, pc, bpc);
        Object[] packedArgs = args.toArray();
        
        return new RETEClauseFilter(packed, packedArgs);
    }
    
    /**
     * Set the continuation node for this node.
     */
    public void setContinuation(RETESinkNode continuation) {
        this.continuation = continuation;
    }

    /**
     * Insert or remove a triple into the network.
     * @param triple the triple to process.
     * @param isAdd true if the triple is being added to the working set.
     */
    public void fire(Triple triple, boolean isAdd) {
        
        Functor lastFunctor = null;     // bound by TESTFunctorName
        BindingVector env = null;       // bound by CREATEToken
        Node n = null;                  // Temp workspace
        
        for (int pc = 0; pc < instructions.length; ) {
            switch(instructions[pc++]) {
                
            case TESTValue: 
                // Check triple entry (arg1) against literal value (arg2)
                if (! getTripleValue(triple, instructions[pc++], lastFunctor)
                                .sameValueAs(args[instructions[pc++]])) return;
                break;
                
            case TESTFunctorName:
                // Check literal value is a functor of name arg1.
                // Side effect: leaves a loop variable pointing to functor 
                // for possible later functor argument accesses
                n = triple.getObject();
                if ( !n.isLiteral() ) return;
                LiteralLabel ll = n.getLiteral();
                if ( ll.getDatatype() != Functor.FunctorDatatype.theFunctorDatatype) return;
                lastFunctor = (Functor)ll.getValue();
                if ( !lastFunctor.getName().equals(args[instructions[pc++]]) ) return;
                break;
                
            case CREATEToken:
                // Create a result environment of length arg1
                env = new BindingVector(new Node[instructions[pc++]]);
                break;
                
            case BIND:
                // Bind a node (arg1) to a place in the rules token (arg2)
                n = getTripleValue(triple, instructions[pc++], lastFunctor);
                if ( !env.bind(instructions[pc++], n) ) return;
                break;
                
            case END:
                // Success, fire the continuation
                continuation.fire(env, isAdd);
            }
        }

    }
    
    /**
     * Helperful function. Return the node from the argument triple
     * corresponding to the byte code address.
     */
    private Node getTripleValue(Triple triple, byte address, Functor lastFunctor) {
        switch (address & 0xf0) {
        case ADDRSubject:
            return triple.getSubject();
        case ADDRPredicate:
            return triple.getPredicate();
        case ADDRObject:
            return triple.getObject();
        case ADDRFunctorNode:
            return lastFunctor.getArgs()[address & 0x0f];
        }
        return null;
    }
    
    /**
     * Clone this node in the network.
     * @param netCopy a map from RETENode to cloned instance
     * @param context the new context to which the network is being ported
     */
    public RETENode clone(Map netCopy, RETERuleContext context) {
        RETEClauseFilter clone = (RETEClauseFilter)netCopy.get(this);
        if (clone == null) {
            clone = new RETEClauseFilter(instructions, args);
            clone.setContinuation((RETESinkNode)continuation.clone(netCopy, context));
            netCopy.put(this, clone);
        }
        return clone;
    }
    
}



/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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