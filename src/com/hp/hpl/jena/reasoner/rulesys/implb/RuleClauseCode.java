/******************************************************************
 * File:        RuleCode.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RuleClauseCode.java,v 1.2 2003-07-20 19:06:07 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

/**
 * Object used to hold the compiled bytecode stream for a single rule clause.
 * This uses a slighly WAM-like code stream but gluing of the clauses together
 * into disjunctions is done in the interpreter loop so a complete predicate is
 * represented as a list of RuleClauseCode objects.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-07-20 19:06:07 $
 */
public class RuleClauseCode {
    
//  =======================================================================
//  Variables

    /** The rule from which is code was derived */
    protected Rule rule;
    
    /** The byte code sequence */
    protected byte[] code;
    
    /** Any Object argements needed by the byte codes */
    protected Object[] args;
    
    
//  =======================================================================
//  Instruction set constants

    /** fetch constant argument (const, Ai) */
    public static final byte GET_CONSTANT = 0x1;
    
    /** fetch permanent variable argument, first occurance (Yi, Ai) */
    public static final byte GET_VARIABLE  = 0x2;
    
    /** fetch permanent variable argument, later occurance (Yi, Ai) */
    public static final byte UNIFY_VARIABLE  = 0xe;
    
    /** fetch temporary variable argument (Ti, Ai) */
    public static final byte GET_TEMP  = 0x3;
    
    /** put constant value into call parameter (const, Ai) */
    public static final byte PUT_CONSTANT = 0x4;
    
    /** put permanaent variable into call parameter, first occurance (Yi, Ai) */
    public static final byte PUT_NEW_VARIABLE = 0x5;
    
    /** put permanaent variable into call parameter (Yi, Ai) */
    public static final byte PUT_VARIABLE = 0xf;
    
    /** put temp variable into call parameter (Ti, Ai) */
    public static final byte PUT_TEMP = 0x6;
    
    /** call a predicate code object (predicateCodeList) */
    public static final byte CALL_PREDICATE = 0x7;
    
    /** call a predicate code object with special case of wildcard predicate */
    public static final byte CALL_WILD_PREDICATE = 0x8;
    
    /** return from a call, proceeed along AND tree */
    public static final byte PROCEED = 0x9;
    
    /** create a functor object from a rule template (templateFunctor) */
    public static final byte MAKE_FUNCTOR = 0xa;
    
    /** call a built-in operation defined by a rule clause (clauseIndex( */
    public static final byte CALL_BUILTIN = 0xb; 
    
    /** reset a permanent variable to an unbound variable (Yi) */
    public static final byte CLEAR_VARIABLE = 0xc;
    
    /** reset a temp variable to an unbound variable (Ti) */
    public static final byte CLEAR_TEMP = 0xd;
    
//  =======================================================================
//  Methods and constructors

    /**
     * Constructor. 
     * @param rule the rule to be compiled
     */
    public RuleClauseCode(Rule rule) {
        this.rule = rule;
    }
    
    /**
     * Compile the rule into byte code.
     * <p>
     * Version 0 - no wildcard predicates, no functors, no register optimization. 
     * </p>
     * @param ruleStore the store of LP rules through which calls to other predicates
     * can be resolved.
     */
    public void compile(LPRuleStore ruleStore) {
        CompileState state = new CompileState(rule);
        
        // Compile the head operations
        ClauseEntry head = rule.getHeadElement(0);
        if (!(head instanceof TriplePattern)) {
            throw new ReasonerException("Error compiling rule: heads of backward rules must be triple patterns\nIn rule " + rule.toShortString());
        }
        state.emitHead((TriplePattern)head);
        
        // TODO: emit rest of code, check etc
    }
    
    /**
     * Inner class - compiler state.
     */
    static class CompileState {
        
        /** The temporary code vector during construction */
        byte[] code;
        
        /** The temporary arg list during construction */
        ArrayList args;
        
        /** The code pointer during construction */
        int p;
        
        /** array of lists of variables in the rule clauses, array index is 0 for head, body starts at 1 */
        private List[] termVarTable;
        
        /** Map from variables to the list of term positions in which it occurs */
        private Map varOccurrence = new HashMap();
        
        /** List of all permanent variables */
        private List permanentVars = new ArrayList();
        
        /** List of all temporary variables */
        private List tempVars = new ArrayList();
        
        /** The total number of var occurrences */
        int totalOccurrences = 0;
        
        /** the set of variables processed so far during the compile phase */
         Set seen = new HashSet();
         
        /** 
         * Constructor. 
         */
        CompileState(Rule rule) {
            classifyVariables(rule);
            // Create a scratch area for assembling the code, use a worst-case size estimate
            code = new byte[10 + totalOccurrences + rule.bodyLength()*4];
            ArrayList argList = new ArrayList();
        }
        
        /** 
         * emit the code for the head clause
         */
        void emitHead(TriplePattern head) {
            emitHeadGet(head.getSubject(), 0);
            // TODO: Add predicate test in variable predicate case
            emitHeadGet(head.getObject(), 1);
        }
        
        /**
         * Emit a single head get operation
         * @param node the node to emit
         * @param argi the argument register to address
         */
        void emitHeadGet(Node node, int argi) {
            if (node instanceof Node_RuleVariable) {
                Node_RuleVariable var = (Node_RuleVariable)node;
                if (isDummy(var)) {
                    // Node code required, var not used
                    return;
                }
                if (isTemp(var)) {
                    List occurrences = (List)varOccurrence.get(var);
                    if (occurrences.size() == 2 && 
                        ((TermIndex)occurrences.get(0)).index ==((TermIndex)occurrences.get(1)).index) {
                            // No movement code required, var in right place  
                    } else {
                        code[p++] = GET_TEMP;
                        code[p++] = (byte)var.getIndex();
                        code[p++] = (byte)argi;
                    }
                } else {
                    if (! seen.add(var)) {
                        code[p++] = GET_VARIABLE;
                    } else {
                        code[p++] = UNIFY_VARIABLE;
                    }
                    code[p++] = (byte)var.getIndex();
                    code[p++] = (byte)argi;
                }
            } else {
                code[p++] = GET_CONSTANT;
                code[p++] = (byte)argi;
                args.add(node);
            }
        }
         
        /**
         * Classifies the variables now and side effects the
         * index number of the variables so they lie in two sequences - temp
         * and permanent separately. 
         */
        void classifyVariables(Rule rule) {
            // Build index data structure into var use in each term in the rule
            termVarTable = new List[rule.bodyLength() + 1];
            termVarTable[0] = termVars(rule.getHeadElement(0));
            totalOccurrences += termVarTable[0].size();
            for (int i = 0; i < rule.bodyLength(); i++) {
                termVarTable[i+1] = termVars(rule.getBodyElement(i));
                totalOccurrences += termVarTable[i+1].size();
            }
            
            // Build the inverted data structure
            for (int i = 0; i < rule.bodyLength() + 1; i++ ) {
                List varEnts = termVarTable[i];
                for (int j = 0; j < varEnts.size(); j++) {
                    Node_RuleVariable var = (Node_RuleVariable)varEnts.get(j); 
                    List occurrences = (List)varOccurrence.get(var);
                    if (occurrences == null) {
                        occurrences = new ArrayList();
                        varOccurrence.put(var, occurrences);
                    }
                    occurrences.add(new TermIndex(i, j));
                }
            }
            
            // Classify vars as permanent unless they are just dummies (only used once at all)
            // or only used head+first body goal (but not if used in a builtin)
            for (Iterator enti = varOccurrence.entrySet().iterator(); enti.hasNext(); ) {
                Map.Entry entry = (Map.Entry)enti.next();
                Node_RuleVariable var = (Node_RuleVariable)entry.getKey();
                List occurrences = (List)entry.getValue();
                boolean inFirst = false;
                boolean inLaterBody = false;
                boolean inBuiltin = false;
                for (Iterator oi = occurrences.iterator(); oi.hasNext(); ) {
                    TermIndex occurence = (TermIndex)oi.next();
                    int termNumber = occurence.termNumber;
                    if (termNumber == 0) {
                        inFirst = true;
                    } else if (termNumber > 1) {
                        inLaterBody = true;
                    }
                    if (termNumber > 0 && rule.getBodyElement(termNumber-1) instanceof Functor) {
                        inBuiltin = true;
                        break;
                    }
                }
                if (inBuiltin || inLaterBody || !inFirst) {
                    permanentVars.add(var);
                } else if (!isDummy(var)) {
                    tempVars.add(var);
                }
                 
            }
            
            // Renumber the vars
            for (int i = 0; i < permanentVars.size(); i++ ) {
                Node_RuleVariable var = (Node_RuleVariable)permanentVars.get(i);
                var.setIndex(i);
            }
            for (int i = 0; i < tempVars.size(); i++ ) {
                Node_RuleVariable var = (Node_RuleVariable)tempVars.get(i);
                var.setIndex(i);
            }
            
        }
       
        /** Return true if the given rule variable is a temp */
        boolean isTemp(Node_RuleVariable var) {
            return !isDummy(var) && !permanentVars.contains(var);
        }
        
        /** Return true if the given rule variable is a dummy */
        boolean isDummy(Node_RuleVariable var) {
            List occurances = (List)varOccurrence.get(var);
            if (occurances == null || occurances.size() <= 1) return true;
            return false;
        }
                
        /** Return an list of variables in a ClauseEntry, in flattened order */
        private List termVars(ClauseEntry term) {
            List result = new ArrayList();
            if (term instanceof TriplePattern) {
                TriplePattern goal = (TriplePattern)term;
                if (goal.getObject().isVariable()) {
                    result.add(goal.getObject());
                }
                if (goal.getPredicate().isVariable()) {
                    result.add(goal.getPredicate());
                }
                Node obj = goal.getObject();
                if (obj.isVariable()) {
                    result.add(obj);
                } else if (Functor.isFunctor(obj)) {
                    result.addAll(termVars((Functor)obj.getLiteral().getValue()));
                }
            } else if (term instanceof Functor) {
                Node[] args = (Node[]) ((Functor)term).getArgs();
                for (int i = 0; i < args.length; i++) {
                    if (args[i].isVariable()) {
                        result.add(args[i]);
                    }
                }
            }
            return result;
        }
    }
                
    
    /**
     * Inner class used to represent the occurance of a variable in a term.
     * Not an abstract data type, just a structure directly accessed.
     */
    static class TermIndex {
        /** The term number in the rule - 0 for head, body starting from 1 */
        int termNumber;
        
        /** The variable position within the term */
        int index;
        
        /** Constructor */
        TermIndex(int termNumber, int index) {
            this.termNumber = termNumber;
            this.index = index;
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