/******************************************************************
 * File:        LPBRuleEngine.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPInterpreter.java,v 1.8 2003-07-25 16:34:34 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;

import java.util.*;
import org.apache.log4j.Logger;

/**
 * Bytecode interpeter engine for the LP version of the backward
 * chaining rule system. An instance of this is forked off for each
 * parallel query.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.8 $ on $Date: 2003-07-25 16:34:34 $
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
    protected Node[] pVars = new Node[RuleClauseCode.MAX_PERMANENT_VARS];

    /** The current environment frame */
    protected EnvironmentFrame envFrame;

    /** The current choice point frame */
    protected FrameObject cpFrame;
    
    /** The trail of variable bindings that have to be unwound on backtrack */
    protected ArrayList trail = new ArrayList();

    /** The execution context description to be passed to builtins */
    protected RuleContext context;
        
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
        
        BBRuleContext bbcontext = new BBRuleContext(engine.getInfGraph());
        bbcontext.setEnv(new LPBindingEnvironment(this));
        context = bbcontext;
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
    public synchronized Object next() {
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
        int pc = 0;     // Program code counter
        int ac = 0;     // Program arg code counter
        RuleClauseCode clause = null;       // The clause being executed
        
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
                clause = (RuleClauseCode)choice.clauseIterator.next();
                envFrame = LPEnvironmentFactory.createEnvironment();
                envFrame.init(clause);
                envFrame.linkTo(choice.envFrame);
                envFrame.cpc = choice.cpc;
                envFrame.cac = choice.cac;

                // Restore the choice point state
                System.arraycopy(choice.argVars, 0, argVars, 0, RuleClauseCode.MAX_ARGUMENT_VARS);
                int trailMark = choice.trailIndex;
                if (trailMark < trail.size()) {
                    unwindTrail(trailMark);
                }
                pc = ac = 0;
                context.setRule(clause.getRule());
                // then fall through into the recreated execution context for the new call
                
            } else if (cpFrame instanceof TripleMatchFrame) {
                TripleMatchFrame tmFrame = (TripleMatchFrame)cpFrame;
                
                // Restore the calling context
                envFrame = tmFrame.envFrame;
                int trailMark = tmFrame.trailIndex;
                if (trailMark < trail.size()) {
                    unwindTrail(trailMark);
                }
                
                // Find the next choice result directly
                if (!tmFrame.nextMatch(this)) {
                    // No more matches
                    cpFrame = cpFrame.getLink();
                    continue main;
                }
                pc = tmFrame.cpc;
                ac = tmFrame.cac;

                // then fall through to the execution context in which the the match was called
                
            } else {
                throw new ReasonerException("Internal error in backward rule system, unrecognized choice point");
            }
            
            interpreter: while (envFrame != null) {

                // Start of bytecode intepreter loop
                // Init the state variables
                byte[] code = envFrame.clause.getCode();
                Object[] args = envFrame.clause.getArgs();
                pVars = envFrame.pVars;
                int yi, ai, ti;
                Node arg, constant;
                List predicateCode;
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
                        
                        case RuleClauseCode.PUT_DEREF_VARIABLE:
                            yi = code[pc++];
                            ai = code[pc++];
                            argVars[ai] = deref(pVars[yi]);
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
                            // Create the new choice points
                            ChoicePointFrame newChoiceFrame = ChoicePointFactory.create();
                            newChoiceFrame.init(this, clauses);
                            newChoiceFrame.linkTo(cpFrame);
                            tmFrame = TripleMatchFactory.create();
                            tmFrame.init(this);
                            tmFrame.linkTo(newChoiceFrame);
                            tmFrame.setContinuation(pc, ac);
                            newChoiceFrame.setContinuation(pc, ac);
                            cpFrame = tmFrame;
                            continue main;
                                            
                        case RuleClauseCode.CALL_TRIPLE_MATCH:
                            // Stash the current state
                            tmFrame = TripleMatchFactory.create();
                            tmFrame.init(this);
                            tmFrame.setContinuation(pc, ac);
                            tmFrame.linkTo(cpFrame);
                            cpFrame = tmFrame;
                            continue main;
                                            
                        case RuleClauseCode.PROCEED:
                            pc = envFrame.cpc;
                            ac = envFrame.cac;
                            envFrame = (EnvironmentFrame) envFrame.link;
                            continue interpreter;
                        
                        case RuleClauseCode.CALL_BUILTIN:
                            Builtin builtin = (Builtin)args[ac++];
                            builtin.bodyCall(argVars, code[pc++], context);
                            break;
                            
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