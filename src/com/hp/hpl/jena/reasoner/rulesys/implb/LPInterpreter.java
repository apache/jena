/******************************************************************
 * File:        LPBRuleEngine.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPInterpreter.java,v 1.4 2003-07-23 16:24:17 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.*;
import org.apache.log4j.Logger;

/**
 * Bytecode interpeter engine for the LP version of the backward
 * chaining rule system. An instance of this is forked off for each
 * parallel query.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-07-23 16:24:17 $
 */
public class LPInterpreter {

    //  =======================================================================
    //  Variables

    /** The engine which is using this interpreter */
    protected LPBRuleEngine engine;

    /** Set to true to flag that derivations should be logged */
    protected boolean recordDerivations;

    /** True if the engine has terminated */
    protected boolean isComplete = false;

    /** The set of temporary variables (Ti) in use by this interpreter */
    protected Node[] tVars = new Node[RuleClauseCode.MAX_TEMPORARY_VARS];

    /** The set of argument variables (Ai) in use by this interpreter */
    protected Node[] argVars = new Node[RuleClauseCode.MAX_ARGUMENT_VARS];
        
    /** The set of "permanent" variables (Yi) in use by this interpreter */
    protected Node[] pVars;

    /** The current environment frame */
    protected EnvironmentFrame envFrame;

    /** The current choice point frame */
    protected FrameObject cpFrame;
    
    /** The trail of variable bindings that have to be unwound on backtrack */
    protected ArrayList trail = new ArrayList();
    
    /** TEMP: The singleton result triple */
    protected Object answer;

    /** log4j logger*/
    static Logger logger = Logger.getLogger(LPInterpreter.class);

    //  =======================================================================
    //  Constructors

    /**
     * Constructor.
     * @param engine the engine which is calling this interpreter
     * @param goal the query to be satisfied
     */
    public LPInterpreter(LPBRuleEngine engine, TriplePattern goal) {
        this.engine = engine;
        List clauses = engine.getRuleStore().codeFor(goal);
        
        // Construct dummy top environemnt which is a call into the clauses for this goal
        envFrame = LPEnvironmentFactory.createEnvironment();
        envFrame.init(RuleClauseCode.returnCodeBlock);
        envFrame.pVars[0] = argVars[0] = standardize(goal.getSubject());
        envFrame.pVars[1] = argVars[1] = standardize(goal.getPredicate());
        envFrame.pVars[2] = argVars[2] = standardize(goal.getObject());
        
        if (clauses != null && clauses.size() > 0) {
            ChoicePointFrame newChoiceFrame = ChoicePointFactory.create();
            newChoiceFrame.init(this, clauses);
            newChoiceFrame.linkTo(null);
            cpFrame = newChoiceFrame;
        }
        
        TripleMatchFrame tmFrame = TripleMatchFactory.create();
        tmFrame.init(this);
        tmFrame.linkTo(cpFrame);
        cpFrame = tmFrame;
    }

    //  =======================================================================
    //  Control methods

    /**
     * Stop the current work. This is called if the top level results iterator has
     * either finished or the calling application has had enough. 
     */
    public synchronized void close() {
        isComplete = true;
        engine.detach(this);
        if (cpFrame != null) cpFrame.close();
    }

    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
    }

    /**
     * Return the next result from this engine.
     * @return either a StateFlag or  a result Triple
     */
    public Object next() {
        StateFlag answer = run();
        if (answer == StateFlag.FAIL) {
            return answer;
        } else {
            // TODO: avoid store turn over here in case where answer has been returned by a TripleMatch
            return new Triple(deref(pVars[0]), deref(pVars[1]), deref(pVars[2]));
        }
    }

    //  =======================================================================
    //  Engine implementation  

    /**
     * Restore the current choice point and restart execution of the LP code
     * until either find a successful branch (in which case exit with StateFlag.ACTIVE
     * and variables bound to the correct results) or exhaust all choice points (in
     * which case exit with StateFlag.FAIL and no bound results). In future tabled
     * version could also exit with StateFlag.SUSPEND in cases whether the intepreter
     * needs to suspend to await tabled results from a parallel proof tree.
     */
    protected StateFlag run() {

        main: while (cpFrame != null) {
            // restore choice point
            if (cpFrame instanceof ChoicePointFrame) {
                ChoicePointFrame choice = (ChoicePointFrame)cpFrame;
                if (!choice.clauseIterator.hasNext()) {
                    // No more choices left in this choice point
                    cpFrame = choice.getLink();
                    continue main;
                }
                
                // Create an execution environment for the new choice of clause
                RuleClauseCode clause = (RuleClauseCode)choice.clauseIterator.next();
                envFrame = LPEnvironmentFactory.createEnvironment();
                envFrame.init(clause);
                envFrame.linkTo(choice.envFrame);

                // Restore the choice point state
                argVars = choice.argVars;
                int trailMark = choice.trailIndex;
                if (trailMark > trail.size()) {
                    unwindTrail(trailMark);
                }
                
                // then fall through into the recreated execution context for the new call
                
            } else if (cpFrame instanceof TripleMatchFrame) {
                TripleMatchFrame tmFrame = (TripleMatchFrame)cpFrame;
                
                // Restore the calling context
                envFrame = tmFrame.envFrame;
                int trailMark = tmFrame.trailIndex;
                if (trailMark > trail.size()) {
                    unwindTrail(trailMark);
                }
                
                // Find the next choice result directly
                if (!tmFrame.nextMatch(this)) {
                    // No more matches
                    cpFrame = cpFrame.getLink();
                    continue main;
                }

                // then fall through to the execution context in which the the match was called
                
            } else {
                throw new ReasonerException("Internal error in backward rule system, unrecognized choice point");
            }
            
            interpreter: while (envFrame != null) {

                // Start of bytecode intepreter loop
                // Init the state variables
                int pc = envFrame.pc; // Program counter
                int ac = envFrame.ac; // Argument counter
                byte[] code = envFrame.clause.getCode();
                Object[] args = envFrame.clause.getArgs();
                pVars = envFrame.pVars;
                int yi, ai, ti;
                Node arg, constant;
                List predicateCode;
                RuleClauseCode clause;
                TripleMatchFrame tmFrame;
    
                // Debug ...
//                System.out.println("Interpeting code (at p = " + pc + "):");
//                envFrame.clause.print(System.out);
        
                codeloop: while (true) {
                    switch (code[pc++]) {
                        case RuleClauseCode.GET_VARIABLE :
                            yi = code[pc++];
                            ai = code[pc++];
                            pVars[yi] = argVars[ai];
                            break;
    
                        case RuleClauseCode.GET_TEMP :
                            ti = code[pc++];
                            ai = code[pc++];
                            tVars[ti] = argVars[ai];
                            break;
    
                        case RuleClauseCode.GET_CONSTANT :
                            ai = code[pc++];
                            arg = argVars[ai];
                            if (arg instanceof Node_RuleVariable) arg = ((Node_RuleVariable)arg).deref();
                            constant = (Node) args[ac++]; 
                            if (arg instanceof Node_RuleVariable) {
                                bind(arg, constant);
                            } else {
                                if (!arg.sameValueAs(constant)) {
                                    continue main;  
                                }
                            }
                            break;
                        
                        case RuleClauseCode.UNIFY_VARIABLE :
                            yi = code[pc++];
                            ai = code[pc++];
                            if (!unify(argVars[ai], pVars[yi])) {
                                continue main;  
                            }
                            break;
    
                        case RuleClauseCode.UNIFY_TEMP :
                            ti = code[pc++];
                            ai = code[pc++];
                            if (!unify(argVars[ai], tVars[ti])) {
                                continue main;  
                            }
                            break;
    
                        case RuleClauseCode.PUT_NEW_VARIABLE:
                            yi = code[pc++];
                            ai = code[pc++];
                            argVars[ai] = pVars[yi] = new Node_RuleVariable(null, yi); 
                            break;
                        
                        case RuleClauseCode.PUT_VARIABLE:
                            yi = code[pc++];
                            ai = code[pc++];
                            argVars[ai] = pVars[yi];
                            break;
                        
                        case RuleClauseCode.PUT_TEMP:
                            ti = code[pc++];
                            ai = code[pc++];
                            argVars[ai] = tVars[ti];
                            break;
                        
                        case RuleClauseCode.PUT_CONSTANT:
                            ai = code[pc++];
                            argVars[ai] = (Node)args[ac++];
                            break;
                    
                        case RuleClauseCode.CLEAR_ARG:
                            ai = code[pc++];
                            argVars[ai] = new Node_RuleVariable(null, ai); 
                            break;
                        
                        case RuleClauseCode.LAST_CALL_PREDICATE:
                            // TODO: improved implementation of last call case!
                        case RuleClauseCode.CALL_PREDICATE:
                            List clauses = (List)args[ac++];
                            // Stash the current state
                            envFrame.ac = ac;
                            envFrame.pc = pc;
                            // Create the new choice points
                            ChoicePointFrame newChoiceFrame = ChoicePointFactory.create();
                            newChoiceFrame.init(this, clauses);
                            newChoiceFrame.linkTo(cpFrame);
                            tmFrame = TripleMatchFactory.create();
                            tmFrame.init(this);
                            tmFrame.linkTo(newChoiceFrame);
                            cpFrame = tmFrame;
                            continue main;
                                            
                        case RuleClauseCode.CALL_TRIPLE_MATCH:
                            // Stash the current state
                            envFrame.ac = ac;
                            envFrame.pc = pc;
                            tmFrame = TripleMatchFactory.create();
                            tmFrame.init(this);
                            tmFrame.linkTo(cpFrame);
                            cpFrame = tmFrame;
                            continue main;
                                            
                        case RuleClauseCode.PROCEED:
                            envFrame = (EnvironmentFrame) envFrame.link;
                            continue interpreter;
                        
                        default :
                            throw new ReasonerException("Internal error in backward rule system\nIllegal op code");
                    }
                }
                // End of innter code loop
            }
            // End of bytecode interpreter loop, gets to here if we complete an AND chain
            return StateFlag.ACTIVE;
        }
        // Gets to here if we have run out of choice point frames
        return StateFlag.FAIL;
    }
    
    /**
     * TEMP.
     * (Re)start interpreter execution at the current environment frame, continue
     * until the first result has been computed. Return either a triple or Fail.
     */
    protected Object interpret() {

        // Init the state variables
        int pc = envFrame.pc; // Program counter
        int ac = envFrame.ac; // Argument counter
        byte[] code = envFrame.clause.getCode();
        Object[] args = envFrame.clause.getArgs();
        Node[] pVars = envFrame.pVars;
        int yi, ai, ti;
        Node arg, constant;
        List predicateCode;
        RuleClauseCode clause;
        MutableTriplePattern finderPattern = new MutableTriplePattern();

        // Debug ...
        System.out.println("Interpeting code (at p = " + pc + ":");
        envFrame.clause.print(System.out);
        
        fail: for (;;) {
            switch (code[pc++]) {
                case RuleClauseCode.GET_VARIABLE :
                    yi = code[pc++];
                    ai = code[pc++];
                    pVars[yi] = argVars[ai];
                    break;

                case RuleClauseCode.GET_TEMP :
                    ti = code[pc++];
                    ai = code[pc++];
                    tVars[ti] = argVars[ai];
                    break;

                case RuleClauseCode.GET_CONSTANT :
                    ai = code[pc++];
                    arg = argVars[ai];
                    if (arg instanceof Node_RuleVariable) arg = ((Node_RuleVariable)arg).deref();
                    constant = (Node) args[ac++]; 
                    if (arg instanceof Node_RuleVariable) {
                        bind(arg, constant);
                    } else {
                        if (!arg.sameValueAs(constant)) {
                            break fail;  
                        }
                    }
                    break;
                    
                case RuleClauseCode.UNIFY_VARIABLE :
                    yi = code[pc++];
                    ai = code[pc++];
                    if (!unify(argVars[ai], pVars[yi])) {
                        break fail;
                    }
                    break;

                case RuleClauseCode.UNIFY_TEMP :
                    ti = code[pc++];
                    ai = code[pc++];
                    if (!unify(argVars[ai], tVars[ti])) {
                        break fail;
                    }
                    break;

                case RuleClauseCode.PUT_NEW_VARIABLE:
                    yi = code[pc++];
                    ai = code[pc++];
                    argVars[ai] = pVars[yi] = new Node_RuleVariable(null, yi); 
                    break;
                    
                case RuleClauseCode.PUT_VARIABLE:
                    yi = code[pc++];
                    ai = code[pc++];
                    argVars[ai] = pVars[yi];
                    break;
                    
                case RuleClauseCode.PUT_TEMP:
                    ti = code[pc++];
                    ai = code[pc++];
                    argVars[ai] = tVars[ti];
                    break;
                    
                case RuleClauseCode.PUT_CONSTANT:
                    ai = code[pc++];
                    argVars[ai] = (Node)args[ac++];
                    break;
                
                case RuleClauseCode.CLEAR_ARG:
                    ai = code[pc++];
                    argVars[ai] = new Node_RuleVariable(null, ai); 
                    break;
                    
                case RuleClauseCode.CALL_PREDICATE:
                    predicateCode = (List) args[ac++];
                    // TODO choice point stuff, this bit TEMP
                    clause = (RuleClauseCode)predicateCode.get(0);
                    // Stash the current state
                    envFrame.ac = ac;
                    envFrame.pc = pc;
                    // Create the new state
                    EnvironmentFrame newframe = LPEnvironmentFactory.createEnvironment();
                    newframe.init(clause);
                    newframe.linkTo(envFrame);
                    envFrame = newframe;
                    pc = ac = 0;
                    code = clause.getCode();
                    args = clause.getArgs();
                    pVars = envFrame.pVars;
                    
                    // Debug ...
                    System.out.println("Nested call to:");
                    envFrame.clause.print(System.out);
                
                    break;
                    
                case RuleClauseCode.LAST_CALL_PREDICATE:
                    predicateCode = (List) args[ac++];
                    // TODO choice point stuff, this bit TEMP
                    clause = (RuleClauseCode)predicateCode.get(0);
                    pc = ac = 0;
                    code = clause.getCode();
                    args = clause.getArgs();
                    
                    // Debug ...
                    System.out.println("Nested last call to:");
                    clause.print(System.out);
                
                    break;
                    
                case RuleClauseCode.CALL_TRIPLE_MATCH:
                    // TODO choice point stuff, this bit TEMP
                    Node s = deref(argVars[0]);
                    Node p = deref(argVars[1]);
                    Node o = deref(argVars[2]);
                    finderPattern.setPattern(s, p, o);
                    ExtendedIterator it = engine.getInfGraph().findDataMatches(finderPattern);
                    if (it.hasNext()) {
                        Triple match = (Triple)it.next();
                        it.close();
                        if (s instanceof Node_RuleVariable) {
                            bind(s, match.getSubject());
                        }
                        if (p instanceof Node_RuleVariable) {
                            bind(p, match.getPredicate());
                        }
                        if (o instanceof Node_RuleVariable) {
                            bind(o, match.getObject());
                        }
                    } else {
                        break fail;
                    }
                    break;
                    
                case RuleClauseCode.PROCEED:
                    envFrame = (EnvironmentFrame) envFrame.link;
                    if (envFrame != null) {
                        pc = envFrame.pc;
                        ac = envFrame.ac;
                        code = envFrame.clause.getCode();
                        args = envFrame.clause.getArgs();
                        pVars = envFrame.pVars;
                    } else {
                        // Found top level result, report it
                        // TODO avoid store turn over here, especially when the result actually is from the graph
                        return new Triple(deref(pVars[0]), deref(pVars[1]), deref(pVars[2]));
                    }
                    break;
                    
                default :
                    throw new ReasonerException("Internal error in backward rule system\nIllegal op code");
            }
        }

        return StateFlag.FAIL;
    }

    /**
     * Unify two nodes. Current implementation does not support functors.
     * @return true if the unifcation succeeds
     */
    public boolean unify(Node n1, Node n2) {
        Node nv1 = n1;
        if (nv1 instanceof Node_RuleVariable) {
            nv1 = ((Node_RuleVariable)n1).deref();
            
        } 
        Node nv2 = n2;
        if (nv2 instanceof Node_RuleVariable) {
            nv2 = ((Node_RuleVariable)n2).deref();
        }
        if (nv1 instanceof Node_RuleVariable) {
            bind(nv1, nv2);
            return true;
        } else if (nv2 instanceof Node_RuleVariable) {
            bind(nv2, nv1);
            return true;
        } else {
            return nv1.sameValueAs(nv2);
        }
        
    }
    
    /**
     * Bind a value to a variable, recording the binding in the trail.
     * @param var the dereferenced variable to be bound
     * @param val the value to bind to it
     */
    public void bind(Node var, Node val) {
        ((Node_RuleVariable)var).simpleBind(val);
        trail.add(var);
    }
    
    /**
     * Unwind the trail to given low water mark
     */
    public void unwindTrail(int mark) {
        for (int i = trail.size()-1; i >= mark; i--) {
            Node_RuleVariable var = (Node_RuleVariable)trail.get(i);
            var.unbind();
            trail.remove(i);
        }
    }
    
    /**
     * Derefernce a node, following any binding trail.
     */
    public static Node deref(Node node) {
        if (node instanceof Node_RuleVariable) {
            return ((Node_RuleVariable)node).deref();
        } else {
            return node;
        }
    }
    
    /**
     * Standardize a node by replacing any variables by new variables.
     * This is used in constructing the arguments to a top level call from a goal pattern.
     */
    public Node standardize(Node node) {
        if (node.isVariable()) {
            return new Node_RuleVariable(null, 0);
        } else {
            return node;
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