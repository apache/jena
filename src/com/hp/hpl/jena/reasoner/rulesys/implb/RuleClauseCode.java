/******************************************************************
 * File:        RuleCode.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RuleClauseCode.java,v 1.20 2003-08-14 17:49:06 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;

import java.io.PrintStream;
import java.util.*;

/**
 * Object used to hold the compiled bytecode stream for a single rule clause.
 * This uses a slighly WAM-like code stream but gluing of the clauses together
 * into disjunctions is done in the interpreter loop so a complete predicate is
 * represented as a list of RuleClauseCode objects.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.20 $ on $Date: 2003-08-14 17:49:06 $
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
    public static final byte UNIFY_VARIABLE  = 0x3;
    
    /** fetch temporary variable argument (Ti, Ai) */
    public static final byte GET_TEMP  = 0x4;
    
    /** fetch temporary variable argument, later occurance (Ti, Ai) */
    public static final byte UNIFY_TEMP  = 0x12;
    
    /** put constant value into call parameter (const, Ai) */
    public static final byte PUT_CONSTANT = 0x5;
    
    /** put permanaent variable into call parameter, first occurance (Yi, Ai) */
    public static final byte PUT_NEW_VARIABLE = 0x6;
    
    /** put permanaent variable into call parameter (Yi, Ai) */
    public static final byte PUT_VARIABLE = 0x7;
    
    /** put a dereferenced permanent variable into call parameter ready for BUILTIN call (Yi, Ai) */
    public static final byte PUT_DEREF_VARIABLE = 0x14;
    
    /** put temp variable into call parameter (Ti, Ai) */
    public static final byte PUT_TEMP = 0x8;
    
    /** call a predicate code object (predicateCodeList) */
    public static final byte CALL_PREDICATE = 0x9;
    
    /** deconstruct a functor argument (functor) */
    public static final byte GET_FUNCTOR = 0xa;
    
    /** call a predicate code object with run time indexing (predicateCodeList) */
    public static final byte CALL_PREDICATE_INDEX = 0x17;
    
    /** call a pure triple match (predicate) */
    public static final byte CALL_TRIPLE_MATCH = 0x11;
    
    /** variant on CALL_PREDICATE using the last call optimization, only current used in chain rules */
    public static final byte LAST_CALL_PREDICATE = 0x13;
    
    /** call a table code object () */
    public static final byte CALL_TABLED = 0x18;
    
    /** call a table code object from a wildcard () */
    public static final byte CALL_WILD_TABLED = 0x19;
    
    /** return from a call, proceeed along AND tree */
    public static final byte PROCEED = 0xb;
    
    /** create a functor object from a rule template (templateFunctor) */
    public static final byte MAKE_FUNCTOR = 0xc;
    
    /** call a built-in operation defined by a rule clause (clauseIndex( */
    public static final byte CALL_BUILTIN = 0xd; 
    
    /** reset a permanent variable to an unbound variable (Yi) */
    public static final byte CLEAR_VARIABLE = 0xe;
    
    /** reset a temp variable to an unbound variable (Ti) */
    public static final byte CLEAR_TEMP = 0xf;
    
    /** reset an argument to an unbound variable (Ai) */
    public static final byte CLEAR_ARG = 0x10;
    
    /** Allocate a new environment frame */
    public static final byte ALLOCATE = 0x16;
    
    // current next = 0x20
    
    /** The maximum number of permanent variables allowed in a single rule clause. 
     *   Future refactorings will remove this restriction. */
    public static final int MAX_PERMANENT_VARS = 6;
    
    /** The maximum number of argument variables allowed in a single goal 
     *   Future refactorings will remove this restriction. */
    public static final int MAX_ARGUMENT_VARS = 8;
    
    /** The maximum number of temporary variables allowed in a single rule clause. */
    public static final int MAX_TEMPORARY_VARS = 10;
    
    /** Dummy code block which just returns */
    public static RuleClauseCode returnCodeBlock;
    
    static {
        returnCodeBlock = new RuleClauseCode(null);
        returnCodeBlock.code = new byte[] {PROCEED};
    }
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
     * Return the byte code vector for this clause.
     */
    public byte[] getCode() {
        return code;
    }
    
    /**
     * Return the argument vector associated with this clauses' byte codes.
     */
    public Object[] getArgs() {
        return args;
    }
    
    /**
     * Return the rule from which this code block was compiled.
     */
    public Rule getRule() {
        return rule;
    }
    
    /**
     * Compile the rule into byte code.
     * @param ruleStore the store of LP rules through which calls to other predicates
     * can be resolved.
     */
    public void compile(LPRuleStore ruleStore) {
        CompileState state = new CompileState(rule);

        // Compile the head operations
        ClauseEntry head = rule.getHeadElement(0);
        if (!(head instanceof TriplePattern)) {
            throw new LPRuleSyntaxException("Heads of backward rules must be triple patterns", rule);
        }
        state.emitHead((TriplePattern)head);
        
        // Compile the body operations
        for (int i = 0; i < rule.bodyLength(); i++) {
            ClauseEntry entry = rule.getBodyElement(i);
            if (entry instanceof TriplePattern) {
                state.emitBody((TriplePattern)entry, ruleStore);
            } else if (entry instanceof Functor) {
                state.emitBody((Functor)entry);
            } else {
                throw new LPRuleSyntaxException("can't create new bRules in an bRule", rule);
            }
        }
        
        // Extract the final code
        code = state.getFinalCode();
        args = state.getFinalArgs();
    }
            
    /**
     * Debug helper - list the code to a stream
     */
    public void print(PrintStream out) {
        if (code == null) {
            out.println("Code not available");
        } else {
            int argi = 0;
            for (int p = 0; p < code.length; ) {
                byte instruction = code[p++];
                switch (instruction) {
                case GET_CONSTANT:
                    out.println("GET_CONSTANT " + args[argi++] + ", A" + code[p++]);
                    break;
                case GET_VARIABLE:
                    out.println("GET_VARIABLE " + "Y" + code[p++] + ", A" + code[p++]);
                    break;
                case UNIFY_VARIABLE:
                    out.println("UNIFY_VARIABLE " + "Y" + code[p++] + ", A" + code[p++]);
                    break;
                case GET_TEMP:
                    out.println("GET_TEMP " + "T" + code[p++] + ", A" + code[p++]);
                    break;
                case UNIFY_TEMP:
                    out.println("UNIFY_TEMP " + "T" + code[p++] + ", A" + code[p++]);
                    break;
                case PUT_CONSTANT:
                    out.println("PUT_CONSTANT " + args[argi++] + ", A" + code[p++]);
                    break;
                case PUT_NEW_VARIABLE:
                    out.println("PUT_NEW_VARIABLE " + "Y" + code[p++] + ", A" + code[p++]);
                    break;
                case PUT_TEMP:
                    out.println("PUT_TEMP " + "T" + code[p++] + ", A" + code[p++]);
                    break;
                case PUT_VARIABLE:
                    out.println("PUT_VARIABLE " + "Y" + code[p++] + ", A" + code[p++]);
                    break;
                case PUT_DEREF_VARIABLE:
                    out.println("PUT_DEREF_VARIABLE " + "Y" + code[p++] + ", A" + code[p++]);
                    break;
                case CALL_PREDICATE:
                    out.println("CALL_PREDICATE " + args[argi++]);
                    break;
                case CALL_TABLED:
                    out.println("CALL_TABLED ");
                    break;
                case CALL_WILD_TABLED:
                    out.println("CALL_WILD_TABLED ");
                    break;
                case CALL_PREDICATE_INDEX:
                    out.println("CALL_PREDICATE_INDEX " + args[argi++]);
                    break;
                case LAST_CALL_PREDICATE:
                    out.println("LAST_CALL_PREDICATE " + args[argi++]);
                    break;
                case CALL_TRIPLE_MATCH:
                        out.println("CALL_TRIPLE_MATCH");
                        break;
                case PROCEED:
                    out.println("PROCEED");
                    break;
                case MAKE_FUNCTOR:
                    out.println("MAKE_FUNCTOR " + args[argi++]); 
                    break;
                case GET_FUNCTOR:
                    out.println("GET_FUNCTOR " + args[argi++]); 
                    break;
                case CALL_BUILTIN:
                    out.println("CALL_BUILTIN " + ((Builtin)args[argi++]).getName() + "/" + code[p++]);
                    break;
                case CLEAR_ARG:
                    out.println("CLEAR_ARG " + "A" + code[p++]);
                    break;
                case ALLOCATE:
                    out.println("ALLOCATE");
                    break;
                default:
                    out.println("Unused code: " + instruction);
                    break;
                }
            }
        }
    }
    
    /**
     * Print clause as rule for tracing.
     */
    public String toString() {
        if (rule == null) {
            return "[anon]";
        } else {
            return "[" + rule.toShortString() + "]";
        }
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
         
        /** The rule being parsed */
        Rule rule;
        
        /** 
         * Constructor. 
         */
        CompileState(Rule rule) {
            classifyVariables(rule);
            this.rule = rule;
            // Create a scratch area for assembling the code, use a worst-case size estimate
            code = new byte[10 + totalOccurrences + rule.bodyLength()*10];
            args = new ArrayList();
        }
        
        /** 
         * emit the code for the head clause
         */
        void emitHead(TriplePattern head) {
            if (permanentVars.size() > 0) code[p++] = ALLOCATE;
            emitHeadGet(head.getSubject(), 0);
            emitHeadGet(head.getPredicate(), 1);
            emitHeadGet(head.getObject(), 2);
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
                        ((TermIndex)occurrences.get(0)).index <= 2 && 
                        ((TermIndex)occurrences.get(0)).index ==((TermIndex)occurrences.get(1)).index) {
                            // No movement code required, var in right place  
                    } else {
                        code[p++] = seen.add(var) ? GET_TEMP : UNIFY_TEMP;
                        code[p++] = (byte)var.getIndex();
                        code[p++] = (byte)argi;
                    }
                } else {
                    code[p++] = seen.add(var) ? GET_VARIABLE : UNIFY_VARIABLE;
                    code[p++] = (byte)var.getIndex();
                    code[p++] = (byte)argi;
                }
            } else if (Functor.isFunctor(node)) {
                Functor f = (Functor)node.getLiteral().getValue();
                code[p++] = GET_FUNCTOR;
                args.add(f);
                Node[] fargs = f.getArgs();
                for (int i = 0; i < fargs.length; i++) {
                    emitHeadGet(fargs[i], i+3);
                }
            } else {
                code[p++] = GET_CONSTANT;
                code[p++] = (byte)argi;
                args.add(node);
            }
        }
        
        /**
         * Emit code for a body clause.
         * @param goal the triple pattern to be called
         */
        void emitBody(TriplePattern goal, LPRuleStore store) {
            int argi = 0;
            emitBodyPut(goal.getSubject(), 0);
            emitBodyPut(goal.getPredicate(), 1);
            emitBodyPut(goal.getObject(), 2);
            List predicateCode = store.codeFor(goal);
            if (predicateCode == null || predicateCode.size() == 0) {
                code[p++] = CALL_TRIPLE_MATCH;
            } else {
//                if (goal.getPredicate().isVariable()) {
//                    code[p++] = CALL_WILD_TABLED; // experimental at this stage ...
                if (store.isTabled(goal)) {
                    code[p++] = goal.getPredicate().isVariable() ? CALL_WILD_TABLED : CALL_TABLED;
                } else {
                    if (permanentVars.size() == 0) {
                        code[p++] = LAST_CALL_PREDICATE;
                    } else {
                        // Normal call, but can it be indexed further?
                        if (store.isIndexedPredicate(goal.getPredicate()) && goal.getObject().isVariable()) {
                            code[p++] = CALL_PREDICATE_INDEX;
                        } else {
                            code[p++] = CALL_PREDICATE;
                        }
                    }
                    args.add(predicateCode);
                }
            }
        }
        
        /**
         * Emit code a single body put operation.
         * @param node the node to emit
         * @param argi the argument register to use
         */
        void emitBodyPut(Node node, int argi) {
            if (node instanceof Node_RuleVariable) {
                Node_RuleVariable var = (Node_RuleVariable)node;
                if (isDummy(var)) {
                    code[p++] = CLEAR_ARG;
                    code[p++] = (byte)argi;
                    return;
                }
                if (isTemp(var)) {
                    List occurrences = (List)varOccurrence.get(var);
                    if (occurrences.size() == 2 && 
                        ((TermIndex)occurrences.get(0)).index ==((TermIndex)occurrences.get(1)).index) {
                            // No movement code required, var in right place  
                    } else {
                        code[p++] = PUT_TEMP;
                        code[p++] = (byte)var.getIndex();
                        code[p++] = (byte)argi;
                    }
                } else {
                    if (! seen.add(var)) {
                        code[p++] = PUT_VARIABLE;
                    } else {
                        code[p++] = PUT_NEW_VARIABLE;
                    }
                    code[p++] = (byte)var.getIndex();
                    code[p++] = (byte)argi;
                }
            } else if (Functor.isFunctor(node)) {
                Functor f = (Functor)node.getLiteral().getValue();
                Node[] fargs = f.getArgs();
                for (int i = 0; i < fargs.length; i++) {
                    emitBodyPut(fargs[i], i+3);
                }
                code[p++] = MAKE_FUNCTOR;
                args.add(f);
            } else {
                code[p++] = PUT_CONSTANT;
                code[p++] = (byte)argi;
                args.add(node);
            }
        }
        
        /**
         * Emit code for a call to a built-in predicate (functor).
         * @param functor the built-in to be invoked.
         */
        void emitBody(Functor functor) {
            Node[] fargs = functor.getArgs();
            Builtin builtin = functor.getImplementor();
            if (builtin == null) {
                throw new LPRuleSyntaxException("Unknown builtin operation " + functor.getName(), rule);
            }
            if (builtin.getArgLength() != 0 && builtin.getArgLength() != fargs.length) {
                throw new LPRuleSyntaxException("Wrong number of arguments to functor " + functor.getName() 
                                                  + " expected " + functor.getArgLength(), rule);
            }
            for (int i = 0; i < fargs.length; i++) {
                Node node = fargs[i];
                if (node instanceof Node_RuleVariable) {
                    if (!seen.add(node)) {
                        code[p++] = PUT_DEREF_VARIABLE;
                    } else {
                        code[p++] = PUT_NEW_VARIABLE;
                    }
                    code[p++] = (byte)((Node_RuleVariable)node).getIndex();
                    code[p++] = (byte)i;                    
                } else {
                    code[p++] = PUT_CONSTANT;
                    code[p++] = (byte)i;
                    args.add(node);
                }
                
            }
            code[p++] = CALL_BUILTIN;
            code[p++] = (byte)fargs.length;
            args.add(builtin);
        }
         
        /**
         * Complete the byte stream with a PROCEED and return the packed version of the code.
         * @return the byte code array
         */
        byte[] getFinalCode() {
            code[p++] = PROCEED;
            byte[] finalCode = new byte[p];
            System.arraycopy(code, 0, finalCode, 0, p);
            return finalCode;
        }
        
        /**
         * Fetch the packed array of argument objects for the byte code.
         * @return arg array
         */
        Object[] getFinalArgs() {
            return args.toArray();
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
                    Node n = (Node)varEnts.get(j);
                    if (n.isVariable()) {
                        Node_RuleVariable var = (Node_RuleVariable)n; 
                        List occurrences = (List)varOccurrence.get(var);
                        if (occurrences == null) {
                            occurrences = new ArrayList();
                            varOccurrence.put(var, occurrences);
                        }
                        occurrences.add(new TermIndex(var, i, j));
                    }
                }
            }
            
            // Classify vars as permanent unless they are just dummies (only used once at all)
            // or only used head+first body goal (but not if used in a builtin)
            for (Iterator enti = varOccurrence.values().iterator(); enti.hasNext(); ) {
                List occurrences = (List)enti.next();
                Node_RuleVariable var = null;
                boolean inFirst = false;
                boolean inLaterBody = false;
                boolean inBuiltin = false;
                for (Iterator oi = occurrences.iterator(); oi.hasNext(); ) {
                    TermIndex occurence = (TermIndex)oi.next();
                    var = occurence.var;
                    int termNumber = occurence.termNumber;
                    if (termNumber == 0) {
                        inFirst = true;
                    } else if (termNumber > 1) {
                        inLaterBody = true;
                    }
                    if (termNumber > 0 && rule.getBodyElement(termNumber-1) instanceof Functor) {
                        inBuiltin = true;
                    }
                }
                if (inBuiltin) {
                    // inBuiltin case is overkill but safe
                    permanentVars.add(var);
                } else {
                    if (!isDummy(var)) {
                        if (inLaterBody || !inFirst) {
                            permanentVars.add(var);
                        } else {
                            tempVars.add(var);
                        }
                    }
                }
                 
            }
            
            if (permanentVars.size() > MAX_PERMANENT_VARS) {
                throw new LPRuleSyntaxException("Rule too complex for current implementation\n" 
                            + "Rule clauses are limited to " + MAX_PERMANENT_VARS + "permanent variables\n", rule); 
            }
            
            if (tempVars.size() > MAX_TEMPORARY_VARS) {
                throw new LPRuleSyntaxException("Rule too complex for current implementation\n" 
                            + "Rule clauses are limited to " + MAX_TEMPORARY_VARS + "temporary variables\n", rule); 
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
                
        /** Return an list of variables or nodes in a ClauseEntry, in flattened order */
        private List termVars(ClauseEntry term) {
            List result = new ArrayList();
            if (term instanceof TriplePattern) {
                TriplePattern goal = (TriplePattern)term;
                result.add(goal.getSubject());
                result.add(goal.getPredicate());
                Node obj = goal.getObject();
                if (Functor.isFunctor(obj)) {
                    result.add(obj);
                    result.addAll(termVars((Functor)obj.getLiteral().getValue()));
                } else {
                    result.add(obj);
                }
            } else if (term instanceof Functor) {
                Node[] args = (Node[]) ((Functor)term).getArgs();
                for (int i = 0; i < args.length; i++) {
                    result.add(args[i]);
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
        
        /** The variable being indexed */
        Node_RuleVariable var;
        
        /** Constructor */
        TermIndex(Node_RuleVariable var, int termNumber, int index) {
            this.var = var;
            this.termNumber = termNumber;
            this.index = index;
        }
    }
      
    /**
     * Debug support - not unit testing.
     */
    public static void main(String[] args) {
        try {
            LPRuleStore store = new LPRuleStore();
            String test1 = "(?a p ?y) <- (?a p2 ?z).";
            String test2 = "(?a p ?y) <- (?a q2 ?z) (?z q2 ?w).";
            String test3 =  "(?a p ?a) <- (?z r2 ?w) (?z r2 ?w).";
            String test4 =  "(?a p ?a) <- (?z r2 ?w) (?a r2 ?w).";
            String test5 = "(?a p ?y) <- (?a p ?z), (?z p ?y).";
            String test6 = "(?x p C3) <- (C1 r ?x).";
            String test7 = "(?x p ?y) <- (?x r ?y) (?x q ?y).";
            String test8 = "(?x p ?y) <- (?x p ?z) addOne(?z, ?y).";
            String test9 = "(?x p ?y) <- (?x p ?z) sum(?z, 2, ?y).";
            String test10 = "(?x p ?y) <- (?x p ?v), sum(?v 2 ?y).";
            String test11 = "(b p ?y) <- (a ?y ?v).";
            String test12 = "(?x p ?y) <- (?x p foo(?z, ?y)).";
            String test13 = "(?x p foo(?y,?z)) <- (?x q ?y), (?x q ?z).";
            String test14 = "(?x p ?z) <- (?x e ?z), (?z q ?z).";
            String test15 = "(?x p ?y ) <- bound(?x), (?x p ?y).";
            String test16 = "(a p b ) <- unbound(?x).";
            String test17 = "(?a p ?a) <- (?a q class).";
            String test18 = "(?a p ?a) <- (?a s ?a).";
            store.addRule(Rule.parseRule(test18));
            System.out.println("Code for p:");
            List codeList = store.codeFor(Node.createURI("p"));
            RuleClauseCode code = (RuleClauseCode)codeList.get(0);
            code.print(System.out);
        } catch (Exception e) {
            System.out.println("Problem: " + e);
            e.printStackTrace();
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