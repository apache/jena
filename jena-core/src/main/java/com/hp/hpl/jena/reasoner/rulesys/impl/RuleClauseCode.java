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

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;

import java.io.PrintStream;
import java.util.*;

/**
 * Object used to hold the compiled bytecode stream for a single rule clause.
 * This uses a slightly WAM-like code stream but gluing of the clauses together
 * into disjunctions is done in the interpreter loop so a complete predicate is
 * represented as a list of RuleClauseCode objects.
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
    
    /** starting byte code offset for body terms */
    protected int[] termStart;
     
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
    
    /** Test if an argument is bound (Ai) */
    public static final byte TEST_BOUND = 0x20;
    
    /** Test if an argument is unbound (Ai) */
    public static final byte TEST_UNBOUND = 0x21;
    
    // current next = 0x22
    
    /** The maximum number of permanent variables allowed in a single rule clause. 
     *  Now only relevent for initial holding clause. */
    public static final int MAX_PERMANENT_VARS = 15;
    
    /** The maximum number of argument variables allowed in a single goal 
     *   Future refactorings will remove this restriction. */
    public static final int MAX_ARGUMENT_VARS = 8;
    
    /** The maximum number of temporary variables allowed in a single rule clause. */
    public static final int MAX_TEMPORARY_VARS = 8;
    
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
        
        // Compile any early binding tests
        int skip = 0; // state.emitBindingTests();
        
        // Compile the head operations
        ClauseEntry head = rule.getHeadElement(0);
        if (!(head instanceof TriplePattern)) {
            throw new LPRuleSyntaxException("Heads of backward rules must be triple patterns", rule);
        }
        state.emitHead((TriplePattern)head);
        
        // Compile the body operations
        termStart = new int[rule.bodyLength()];
        for (int i = skip; i < rule.bodyLength(); i++) {
            termStart[i] = state.p;
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
     * Translate a program counter offset to the index of the corresponding
     * body term (or -1 if a head term or a dummy rule).
     */
    public int termIndex(int pc) {
        if (rule == null) return -1;
        int term = 0; 
        // Trivial linear search because this is only used for logging which is
        // horribly expensive anyway.
        while (term < rule.bodyLength()) {
            if (pc <= termStart[term]) {
                return term - 1;            
            }
            term++;
        }
        return term - 1;
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
                case TEST_BOUND:
                    out.println("TEST_BOUND A" + code[p++]);
                    break;
                case TEST_UNBOUND:
                    out.println("TEST_UNBOUND A" + code[p++]);
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
                    out.println("ALLOCATE + " + code[p++]);
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
    @Override
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
        ArrayList<Object> args;
        
        /** The code pointer during construction */
        int p;
        
        /** array of lists of variables in the rule clauses, array index is 0 for head, body starts at 1 */
        private List<Node>[] termVarTable;
        
        /** Map from variables to the list of term positions in which it occurs */
        private Map<Node_RuleVariable, List<TermIndex>> varOccurrence = new HashMap<>();
        
        /** List of all permanent variables */
        private List<Node_RuleVariable> permanentVars = new ArrayList<>();
        
        /** List of all temporary variables */
        private List<Node_RuleVariable> tempVars = new ArrayList<>();
        
        /** The total number of var occurrences */
        int totalOccurrences = 0;
        
        /** the set of variables processed so far during the compile phase */
        Set<Node_RuleVariable> seen = new HashSet<>();
         
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
            args = new ArrayList<>();
        }
        
        /**
         * Emit the code for any bound/unbound tests add start of body
         * and return the number of body clauses dealt with.
         */
        int emitBindingTests() {
            int i = 0;
            while  (i < rule.bodyLength()) {
                ClauseEntry term = rule.getBodyElement(i);
                if (term instanceof Functor) {
                    Functor f = (Functor)term;
                    if (f.getArgLength() != 1) break;
                    int ai = aIndex(f.getArgs()[0]);
                    if (ai >= 0) {
                        if (f.getName().equals("bound")) {
                            code[p++] = TEST_BOUND;
                            code[p++] = (byte)ai;
                        } else if (f.getName().equals("unbound")) {
                            code[p++] = TEST_UNBOUND;
                            code[p++] = (byte)ai;
                        } else {
                            break;
                        }
                    }
                } else {
                    break;
                }
                i++;
            }
            return i;
        }
        
        /**
         * Return the argument index of the given variable.
         */
        int aIndex(Node n) {
            TriplePattern tp = (TriplePattern)rule.getHeadElement(0);
            if (tp.getSubject() == n) {
                return 0;
            } else if (tp.getPredicate() == n) {
                return 1;
            } else if (tp.getObject() == n) {
                return 2;
            } else {
                return -1;
            }
        }
        
        /** 
         * emit the code for the head clause
         */
        void emitHead(TriplePattern head) {
            if (permanentVars.size() > 0) {
                code[p++] = ALLOCATE;
                code[p++] = (byte)permanentVars.size();
            }
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
                    List<TermIndex>occurrences = varOccurrence.get(var);
                    if (occurrences.size() == 2 &&
                        occurrences.get(0).index <= 2 && 
                        occurrences.get(0).index ==occurrences.get(1).index) {
                            // No movement code required, var in right place  
                    } else {
                        code[p++] = seen.add(var) ? GET_TEMP : UNIFY_TEMP;
                        code[p++] = (byte)tempVars.indexOf(var);
                        code[p++] = (byte)argi;
                    }
                } else {
                    code[p++] = seen.add(var) ? GET_VARIABLE : UNIFY_VARIABLE;
                    code[p++] = (byte)permanentVars.indexOf(var);
                    code[p++] = (byte)argi;
                }
            } else if (Functor.isFunctor(node)) {
                Functor f = (Functor)node.getLiteralValue();
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
            emitBodyPut(goal.getSubject(), 0, false);
            emitBodyPut(goal.getPredicate(), 1, false);
            emitBodyPut(goal.getObject(), 2, false);
            List<RuleClauseCode> predicateCode = store.codeFor(goal);
            if (predicateCode == null || predicateCode.size() == 0) {
                code[p++] = CALL_TRIPLE_MATCH;
            } else {
//                code[p++] = CALL_TABLED;
                if (goal.getPredicate().isVariable()) {
                    // Experimental. Force tabling of any wildcard predicate calls
                    code[p++] = CALL_TABLED;
                } else if (store.isTabled(goal)) {
//                if (store.isTabled(goal)) {
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
                    args.add( new RuleClauseCodeList( predicateCode ) );
                }
            }
        }
        
        /**
            Wrapper class with reified
         */
        public static class RuleClauseCodeList 
            {
            private final List<RuleClauseCode> list;
            public RuleClauseCodeList( List<RuleClauseCode> list ) { this.list = list; }
            List<RuleClauseCode> getList()  { return list; }
            }
        
        /**
         * Emit code a single body put operation.
         * @param node the node to emit
         * @param argi the argument register to use
         * @param deref if true force a dereference of the variable binding at this point for
         * use in calling builtins
         */
        void emitBodyPut(Node node, int argi, boolean deref) {
            if (argi >= MAX_ARGUMENT_VARS) {
                throw new LPRuleSyntaxException("Rule too complex for current implementation\n" 
                            + "Rule clauses are limited to " + MAX_ARGUMENT_VARS + " argument variables\n", rule); 

            }
            if (node instanceof Node_RuleVariable) {
                Node_RuleVariable var = (Node_RuleVariable)node;
                if (isDummy(var)) {
                    code[p++] = CLEAR_ARG;
                    code[p++] = (byte)argi;
                    return;
                }
                if (isTemp(var)) {
                    List<TermIndex> occurrences = varOccurrence.get(var);
                    if (occurrences.size() == 2 && 
                        occurrences.get(0).index ==occurrences.get(1).index) {
                            // No movement code required, var in right place  
                    } else {
                        code[p++] = PUT_TEMP;
                        code[p++] = (byte)tempVars.indexOf(var);
                        code[p++] = (byte)argi;
                    }
                } else {
                    if (! seen.add(var)) {
                        code[p++] = (deref ? PUT_DEREF_VARIABLE : PUT_VARIABLE);
                    } else {
                        code[p++] = PUT_NEW_VARIABLE;
                    }
                    code[p++] = (byte)permanentVars.indexOf(var);
                    code[p++] = (byte)argi;
                }
            } else if (Functor.isFunctor(node)) {
                Functor f = (Functor)node.getLiteralValue();
                Node[] fargs = f.getArgs();
                for (int i = 0; i < fargs.length; i++) {
                    emitBodyPut(fargs[i], i+3, deref);
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
                                                  + " : got " + functor.getArgLength()
                                                  + " : expected " + builtin.getArgLength(), rule);
            }
            for (int i = 0; i < fargs.length; i++) {
                Node node = fargs[i];
                // We optionally force an eager dereference of variables here.
                // We used to force this but the current builtin implementations
                // now robust against it (the do a deref themselves anyway).
                 emitBodyPut(node, i, true);
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
        void classifyVariables( Rule rule ) {
            // Build index data structure into var use in each term in the rule
            @SuppressWarnings("unchecked") List<Node>[] termListArray = new List[rule.bodyLength() + 1];
            termVarTable = termListArray;
            termVarTable[0] = termVars( rule.getHeadElement(0) );
            totalOccurrences += termVarTable[0].size();
            for (int i = 0; i < rule.bodyLength(); i++) {
                termVarTable[i+1] = termVars( rule.getBodyElement(i) );
                totalOccurrences += termVarTable[i+1].size();
            }
            
            // Build the inverted data structure
            for (int i = 0; i < rule.bodyLength() + 1; i++ ) {
                List<Node> varEnts = termVarTable[i];
                for (int j = 0; j < varEnts.size(); j++) {
                    Node n = varEnts.get(j);
                    if (n.isVariable()) {
                        Node_RuleVariable var = (Node_RuleVariable)n; 
                        List<TermIndex> occurrences = varOccurrence.get(var);
                        if (occurrences == null) {
                            occurrences = new ArrayList<>();
                            varOccurrence.put(var, occurrences);
                        }
                        occurrences.add(new TermIndex(var, i, j));
                    }
                }
            }
            
            // Classify vars as permanent unless they are just dummies (only used once at all)
            // or only used head+first body goal (but not if used in a builtin)
            for ( List<TermIndex> occurrences : varOccurrence.values() )
            {
                Node_RuleVariable var = null;
                boolean inFirst = false;
                boolean inLaterBody = false;
                boolean inBuiltin = false;
                for ( Iterator<TermIndex> oi = occurrences.iterator(); oi.hasNext(); )
                {
                    TermIndex occurence = oi.next();
                    var = occurence.var;
                    int termNumber = occurence.termNumber;
                    if ( termNumber == 0 )
                    {
                        inFirst = true;
                    }
                    else if ( termNumber > 1 )
                    {
                        inLaterBody = true;
                    }
                    if ( termNumber > 0 && rule.getBodyElement( termNumber - 1 ) instanceof Functor )
                    {
                        inBuiltin = true;
                    }
                }
                // Don't think we need to protected builtin's any more, so ignore that test
//                if (inBuiltin) {
//                    permanentVars.add(var);
//                } else {
                if ( !isDummy( var ) )
                {
                    if ( inLaterBody || !inFirst )
                    {
                        permanentVars.add( var );
                    }
                    else
                    {
                        tempVars.add( var );
                    }
                }
//                }

            }
            
            if (permanentVars.size() > MAX_PERMANENT_VARS) {
                throw new LPRuleSyntaxException("Rule too complex for current implementation\n" 
                            + "Rule clauses are limited to " + MAX_PERMANENT_VARS + " permanent variables\n", rule); 
            }
            
            if (tempVars.size() > MAX_TEMPORARY_VARS) {
                throw new LPRuleSyntaxException("Rule too complex for current implementation\n" 
                            + "Rule clauses are limited to " + MAX_TEMPORARY_VARS + " temporary variables\n", rule); 
            }
            
            // Builtins in the forward system use the var index to modify variable bindings.
            // At one time I though the LP system might need to emulate this but (a) it doesn't and
            // (b) renumber vars to make that possible wreaks rule equality which wreaks add/remove in
            // hybrid rule sets. This code left in but commented out as a reminder to not go down
            // that path again.
            
            // Renumber the vars
//            for (int i = 0; i < permanentVars.size(); i++ ) {
//                Node_RuleVariable var = (Node_RuleVariable)permanentVars.get(i);
//                var.setIndex(i);
//            }
//            for (int i = 0; i < tempVars.size(); i++ ) {
//                Node_RuleVariable var = (Node_RuleVariable)tempVars.get(i);
//                var.setIndex(i);
//            }
            
        }
       
        /** Return true if the given rule variable is a temp */
        boolean isTemp(Node_RuleVariable var) {
            return !isDummy(var) && !permanentVars.contains(var);
        }
        
        /** Return true if the given rule variable is a dummy */
        boolean isDummy(Node_RuleVariable var) {
            List<TermIndex> occurances = varOccurrence.get(var);
            if (occurances == null || occurances.size() <= 1) return true;
            return false;
        }
                
        /** Return an list of variables or nodes in a ClauseEntry, in flattened order */
        private List<Node> termVars(ClauseEntry term) {
            List<Node> result = new ArrayList<>();
            if (term instanceof TriplePattern) {
                TriplePattern goal = (TriplePattern)term;
                result.add(goal.getSubject());
                result.add(goal.getPredicate());
                Node obj = goal.getObject();
                if (Functor.isFunctor(obj)) {
                    result.add(obj);
                    result.addAll(termVars((Functor)obj.getLiteralValue()));
                } else {
                    result.add(obj);
                }
            } else if (term instanceof Functor) {
                Node[] args = ((Functor)term).getArgs();
                for ( Node arg : args )
                {
                    result.add( arg );
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
            String test19 = "(?X p ?T) <- (?X rdf:type c), noValue(?X, p), makeInstance(?X, p, ?T).";
            String test20 = "(a p b ) <- unbound(?x).";
            String testLong = "(?P p ?C) <- (?P q ?D), (?D r xsd(?B, ?S1, ?L1)),(?P p ?E), notEqual(?D, ?E) " +
                    "(?E e xsd(?B, ?S2, ?L2)),min(?S1, ?S2, ?S3),min(?L1, ?L2, ?L3), (?C r xsd(?B, ?S3, ?L3)).";
            String test21 = "(?a p ?y) <- (?x s ?y) (?a p ?x).";
            String test22 = "(?C p ?D) <- (?C rb:xsdBase ?BC), (?D rb:xsdBase ?BD), notEqual(?BC, ?BD).";
            store.addRule(Rule.parseRule(test22));
            System.out.println("Code for p:");
            List<RuleClauseCode> codeList = store.codeFor(NodeFactory.createURI("p"));
            RuleClauseCode code = codeList.get(0);
            code.print(System.out);
        } catch (Exception e) {
            System.out.println("Problem: " + e);
            e.printStackTrace();
        }
    }
}
