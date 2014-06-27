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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.RuleClauseCode.CompileState.RuleClauseCodeList;
import com.hp.hpl.jena.util.PrintUtil;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bytecode interpeter engine for the LP version of the backward
 * chaining rule system. An instance of this is forked off for each
 * parallel query.
 */
public class LPInterpreter {

    //  =======================================================================
    //  Variables

    /** The engine which is using this interpreter */
    protected LPBRuleEngine engine;

    /** The execution context that should be notified of suspended branches */
    protected LPInterpreterContext iContext;
    
    /** True if the engine has terminated */
    protected boolean isComplete = false;

    /** The set of temporary variables (Ti) in use by this interpreter */
    protected Node[] tVars = new Node[RuleClauseCode.MAX_TEMPORARY_VARS];

    /** The set of argument variables (Ai) in use by this interpreter */
    protected Node[] argVars = new Node[RuleClauseCode.MAX_ARGUMENT_VARS];
        
    /** The set of "permanent" variables (Yi) in use by this interpreter */
    protected Node[] pVars = null;

    /** The current environment frame */
    protected EnvironmentFrame envFrame;

    /** The current choice point frame */
    protected FrameObject cpFrame;
    
    /** The trail of variable bindings that have to be unwound on backtrack */
    protected ArrayList<Node> trail = new ArrayList<>();

    /** The execution context description to be passed to builtins */
    protected RuleContext context;
        
    /** Trick to allow the very top level triple lookup to return results with reduced store turnover */
    protected TopLevelTripleMatchFrame topTMFrame;
    
    /** Original set up goal, only used for debugging */
    protected TriplePattern goal;
        
    static Logger logger = LoggerFactory.getLogger(LPInterpreter.class);

    //  =======================================================================
    //  Constructors

    /**
     * Constructor used to construct top level calls.
     * @param engine the engine which is calling this interpreter
     * @param goal the query to be satisfied
     */
    public LPInterpreter(LPBRuleEngine engine, TriplePattern goal) {
        this(engine, goal, engine.getRuleStore().codeFor(goal), true);
    }


    /**
     * Constructor.
     * @param engine the engine which is calling this interpreter
     * @param goal the query to be satisfied
     * @param isTop true if this is a top level call from the outside iterator, false means it is an
     * internal generator call which means we don't need to insert an tabled call
     */
    public LPInterpreter(LPBRuleEngine engine, TriplePattern goal, boolean isTop) {
        this(engine, goal, engine.getRuleStore().codeFor(goal), isTop);
    }
    
    /**
     * Constructor.
     * @param engine the engine which is calling this interpreter
     * @param goal the query to be satisfied
     * @param clauses the set of code blocks needed to implement this goal
     * @param isTop true if this is a top level call from the outside iterator, false means it is an
     * internal generator call which means we don't need to insert an tabled call
     */
    public LPInterpreter(LPBRuleEngine engine, TriplePattern goal, List<RuleClauseCode> clauses, boolean isTop) {
        this.engine = engine;
        this.goal = goal;       // Used for debug only
        
        // Construct dummy top environemnt which is a call into the clauses for this goal
        if (engine.getDerivationLogging()) {
            envFrame = new EnvironmentFrameWithDerivation(RuleClauseCode.returnCodeBlock);
        } else {
            envFrame = new EnvironmentFrame(RuleClauseCode.returnCodeBlock);
        }
        envFrame.allocate(RuleClauseCode.MAX_PERMANENT_VARS);
        HashMap<Node, Node> mappedVars = new HashMap<>();
        envFrame.pVars[0] = argVars[0] = standardize(goal.getSubject(), mappedVars);
        envFrame.pVars[1] = argVars[1] = standardize(goal.getPredicate(), mappedVars);
        envFrame.pVars[2] = argVars[2] = standardize(goal.getObject(), mappedVars);
        if (engine.getDerivationLogging()) {
            ((EnvironmentFrameWithDerivation)envFrame).initDerivationRecord(argVars);
        }
        
        if (clauses != null && clauses.size() > 0) {
            if (isTop && engine.getRuleStore().isTabled(goal)) {
                setupTabledCall(0, 0);
//                setupClauseCall(0, 0, clauses);
            } else {
                setupClauseCall(0, 0, clauses, goal.isGround());
            }
        }
        
//        TripleMatchFrame tmFrame = new TripleMatchFrame(this);
        topTMFrame = new TopLevelTripleMatchFrame(this, goal);
        topTMFrame.linkTo(cpFrame);
        topTMFrame.setContinuation(0, 0);
        cpFrame = topTMFrame;
    }

    /**
     * Called by top level interpeter to set to execution context for this interpeter to be
     * top level instead of an internal generator.
     */
    public void setTopInterpreter(LPInterpreterContext context) {
        iContext = context;
        FrameObject topChoice = topTMFrame.getLink();
        if (topChoice instanceof ConsumerChoicePointFrame) {
            ((ConsumerChoicePointFrame)topChoice).context = context;
        }
    }
    
    //  =======================================================================
    //  Control methods

    /**
     * Stop the current work. This is called if the top level results iterator has
     * either finished or the calling application has had enough. 
     */
    public void close() {
        isComplete = true;
        engine.detach(this);
        if (cpFrame != null) cpFrame.close();
    }
    
    /**
     * Start the interpreter running with the given context.
     */
    public void setState(LPInterpreterState state) {
        if (state instanceof ConsumerChoicePointFrame) {
            restoreState((ConsumerChoicePointFrame) state);
        } else {
            iContext = (LPInterpreterContext) state;
        }
    }
    /**
     * Return the next result from this engine, no further initialization.
     * Should be called from within an appropriately synchronized block.
     */
    public Object next() {
        boolean traceOn = engine.isTraceOn();
        
//        System.out.println("next() on interpeter for goal " + goal); 
        StateFlag answer = run();
//        System.out.println("end next() on interpeter for goal " + goal);
        
        if (answer == StateFlag.FAIL || answer == StateFlag.SUSPEND) {
            return answer;
        } else if (answer == StateFlag.SATISFIED) {
            if (traceOn) logger.info("RETURN: " + topTMFrame.lastMatch);
            return topTMFrame.lastMatch;
        } else {
            Triple t = new Triple(deref(pVars[0]), deref(pVars[1]), derefPossFunctor(pVars[2]));
            if (traceOn) logger.info("RETURN: " + t);
            return t;
        }
    }
    
    /**
     * Preserve the current interpreter state in the given 
     * @return
     */
    /**
     * Return the engine which owns this interpreter.
     */
    public LPBRuleEngine getEngine() {
        return engine;
    }
    
    /**
     * Return the current choice point frame that can be used to restart the
     * interpter at this point.
     */
    public FrameObject getChoiceFrame() {
        return cpFrame;
    }
    
    /**
     * Return the context in which this interpreter is running, that is
     * either the Generator for a tabled goal or a top level iterator.
     */
    public LPInterpreterContext getContext() {
        return iContext;
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
        ChoicePointFrame choice = null;
        byte[] code;
        Object[] args;
        boolean traceOn = engine.isTraceOn();
        boolean recordDerivations = engine.getDerivationLogging();
        
        main: while (cpFrame != null) {
            // restore choice point
            if (cpFrame instanceof ChoicePointFrame) {
                choice = (ChoicePointFrame)cpFrame;
                if (!choice.hasNext()) {
                    // No more choices left in this choice point
                    cpFrame = choice.getLink();
                    if (traceOn) logger.info("FAIL in clause " + choice.envFrame.clause + " choices exhausted");
                    continue main;
                }
                
                clause = choice.nextClause();
                // Create an execution environment for the new choice of clause
                if (recordDerivations) {
                    envFrame = new EnvironmentFrameWithDerivation(clause);
                } else {
                    envFrame = new EnvironmentFrame(clause);
                }
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
                if (recordDerivations) {
                    ((EnvironmentFrameWithDerivation)envFrame).initDerivationRecord(argVars);
                }
                
                if (traceOn) logger.info("ENTER " + clause + " : " + getArgTrace());
                
                // then fall through into the recreated execution context for the new call
                
            } else if (cpFrame instanceof TripleMatchFrame) {
                TripleMatchFrame tmFrame = (TripleMatchFrame)cpFrame;
                
                // Restore the calling context
                envFrame = tmFrame.envFrame;
                clause = envFrame.clause;
                int trailMark = tmFrame.trailIndex;
                if (trailMark < trail.size()) {
                    unwindTrail(trailMark);
                }
                
                // Find the next choice result directly
                if (!tmFrame.nextMatch(this)) {
                    // No more matches
                    cpFrame = cpFrame.getLink();
                    if (traceOn) logger.info("TRIPLE match (" + tmFrame.goal +") -> FAIL");
                    continue main;
                }
                if (traceOn) {
                    logger.info("TRIPLE match (" + tmFrame.goal +") -> " + getArgTrace());
                    logger.info("RENTER " + clause);
                }
                
                pc = tmFrame.cpc;
                ac = tmFrame.cac;
                     
                if (recordDerivations) {
                    if (envFrame instanceof EnvironmentFrameWithDerivation) {
                        ((EnvironmentFrameWithDerivation)envFrame).noteMatch(tmFrame.goal, pc);
                    }
                }

                // then fall through to the execution context in which the the match was called
                
            } else if (cpFrame instanceof TopLevelTripleMatchFrame) {
                TopLevelTripleMatchFrame tmFrame = (TopLevelTripleMatchFrame)cpFrame;
                
                // Find the next choice result directly
                if (!tmFrame.nextMatch(this)) {
                    // No more matches
                    cpFrame = cpFrame.getLink();
                    if (traceOn) logger.info("TRIPLE match (" + tmFrame.goal +") -> FAIL");
                    continue main;
                } else {
                    // Match but this is the top level so return the triple directly
                    if (traceOn) logger.info("TRIPLE match (" + tmFrame.goal +") ->");
                    return StateFlag.SATISFIED;
                }
                
            } else if (cpFrame instanceof ConsumerChoicePointFrame) {
                ConsumerChoicePointFrame ccp = (ConsumerChoicePointFrame)cpFrame;
                
                // Restore the calling context
                envFrame = ccp.envFrame;
                clause = envFrame.clause;
                if (traceOn) logger.info("RESTORE " + clause + ", due to tabled goal " + ccp.generator.goal);
                int trailMark = ccp.trailIndex;
                if (trailMark < trail.size()) {
                    unwindTrail(trailMark);
                }
                
                // Find the next choice result directly
                StateFlag state = ccp.nextMatch(this);
                if (state == StateFlag.FAIL) {
                    // No more matches
                    cpFrame = cpFrame.getLink();
                    if (traceOn) logger.info("FAIL " + clause);
                    continue main;
                } else if (state == StateFlag.SUSPEND) {
                    // Require other generators to cycle before resuming this one
                    preserveState(ccp);
                    iContext.notifyBlockedOn(ccp);
                    cpFrame = cpFrame.getLink();
                    if (traceOn)logger.info("SUSPEND " + clause);
                    continue main;
                }

                pc = ccp.cpc;
                ac = ccp.cac;
                
                if (recordDerivations) {
                    if (envFrame instanceof EnvironmentFrameWithDerivation) {
                        ((EnvironmentFrameWithDerivation)envFrame).noteMatch(ccp.goal, pc);
                    }
                }

                // then fall through to the execution context in which the the match was called
                
            } else {
                throw new ReasonerException("Internal error in backward rule system, unrecognized choice point");
            }
            
            engine.incrementProfile(clause);
            
            interpreter: while (envFrame != null) {

                // Start of bytecode intepreter loop
                // Init the state variables
                pVars = envFrame.pVars;
                int yi, ai, ti;
                Node arg, constant;
                code = clause.getCode();
                args = clause.getArgs();
        
                while (true) {
                    switch (code[pc++]) {
                        case RuleClauseCode.TEST_BOUND:
                            ai = code[pc++];
                            if (deref(argVars[ai]).isVariable()) {
                                if (traceOn) logger.info("FAIL " + clause);
                                continue main;  
                            }
                            break;
                            
                        case RuleClauseCode.TEST_UNBOUND:
                            ai = code[pc++];
                            if (! deref(argVars[ai]).isVariable()) {
                                if (traceOn) logger.info("FAIL " + clause);
                                continue main;  
                            }
                            break;
                            
                        case RuleClauseCode.ALLOCATE:
                            int envSize = code[pc++];
                            envFrame.allocate(envSize);
                            pVars = envFrame.pVars;
                            break;
                            
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
                                    if (traceOn) logger.info("FAIL " + clause);
                                    continue main;  
                                }
                            }
                            break;
                            
                        case RuleClauseCode.GET_FUNCTOR:
                            Functor func = (Functor)args[ac++];
                            boolean match = false;
                            Node o = argVars[2];
                            if (o instanceof Node_RuleVariable) o = ((Node_RuleVariable)o).deref();
                            if (Functor.isFunctor(o)) {
                                Functor funcArg = (Functor)o.getLiteralValue();
                                if (funcArg.getName().equals(func.getName())) {
                                    if (funcArg.getArgLength() == func.getArgLength()) {
                                        Node[] fargs = funcArg.getArgs();
                                        for (int i = 0; i < fargs.length; i++) {
                                            argVars[i+3] = fargs[i];
                                        }
                                        match = true;
                                    }
                                }
                            } else if (o.isVariable()) {
                                // Construct a new functor in place
                                Node[] fargs = new Node[func.getArgLength()];
                                Node[] templateArgs = func.getArgs();
                                for (int i = 0; i < fargs.length; i++) {
                                    Node template = templateArgs[i];
                                    if (template.isVariable()) template = new Node_RuleVariable(null, i+3);
                                    fargs[i] = template;
                                    argVars[i+3] = template;
                                }
                                Node newFunc = Functor.makeFunctorNode(func.getName(), fargs);
                                bind(((Node_RuleVariable)o).deref(), newFunc);
                                match = true;
                            }
                            if (!match) {
                                if (traceOn) logger.info("FAIL " + clause);
                                continue main;      // fail to unify functor shape
                            }
                            break;
                            
                        case RuleClauseCode.UNIFY_VARIABLE :
                            yi = code[pc++];
                            ai = code[pc++];
                            if (!unify(argVars[ai], pVars[yi])) {
                                if (traceOn) logger.info("FAIL " + clause);
                                continue main;  
                            }
                            break;
    
                        case RuleClauseCode.UNIFY_TEMP :
                            ti = code[pc++];
                            ai = code[pc++];
                            if (!unify(argVars[ai], tVars[ti])) {
                                if (traceOn) logger.info("FAIL " + clause);
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
                            
                        case RuleClauseCode.MAKE_FUNCTOR:
                            Functor f = (Functor)args[ac++];
                            Node[] fargs = new Node[f.getArgLength()];
                            System.arraycopy(argVars, 3, fargs, 0, fargs.length);
                            argVars[2] = Functor.makeFunctorNode(f.getName(), fargs);
                            break;
                        
                        case RuleClauseCode.LAST_CALL_PREDICATE:
                            // TODO: improved implementation of last call case
                        case RuleClauseCode.CALL_PREDICATE:
                            List<RuleClauseCode> clauses = ((RuleClauseCodeList) args[ac++]).getList();
                            // Check if this call is now grounded
                            boolean groundCall = isGrounded(argVars[0]) && isGrounded(argVars[1]) && isGrounded(argVars[2]);
                            setupClauseCall(pc, ac, clauses, groundCall);
                            setupTripleMatchCall(pc, ac);
                            continue main;
                                            
                        case RuleClauseCode.CALL_PREDICATE_INDEX:
                            // This code path is experimental, don't yet know if it has enough 
                            // performance benefit to justify the cost of maintaining it.
                            clauses = ((RuleClauseCodeList) args[ac++]).getList();
                            // Check if we can futher index the clauses
                            if (!argVars[2].isVariable()) {
                                clauses = engine.getRuleStore().codeFor(
                                    new TriplePattern(argVars[0], argVars[1], argVars[2]));
                            }
                            setupClauseCall(pc, ac, clauses, false);
                            setupTripleMatchCall(pc, ac);
                            continue main;
                                            
                         case RuleClauseCode.CALL_TRIPLE_MATCH:
                            setupTripleMatchCall(pc, ac);
                            continue main;
                         
                        case RuleClauseCode.CALL_TABLED:
                            setupTabledCall(pc, ac);
                            continue main;
                                            
                        case RuleClauseCode.CALL_WILD_TABLED:
                            Node predicate = deref(argVars[1]);
                            if (engine.getRuleStore().isTabled(predicate)) {
                                setupTabledCall(pc, ac);
                            } else {
                                // normal call set up
                                clauses = engine.getRuleStore().codeFor(
                                    new TriplePattern(argVars[0], predicate, argVars[2]));
                                if (clauses != null) setupClauseCall(pc, ac, clauses, false);
                                setupTripleMatchCall(pc, ac);
                            }
                            continue main;
                                            
                        case RuleClauseCode.PROCEED:
                            pc = envFrame.cpc;
                            ac = envFrame.cac;
                            if (traceOn) logger.info("EXIT " + clause);
                            if (choice != null) choice.noteSuccess();
                            if (recordDerivations && envFrame.getRule() != null) {
                                if (envFrame instanceof EnvironmentFrameWithDerivation) {
                                    EnvironmentFrameWithDerivation efd = (EnvironmentFrameWithDerivation) envFrame;
                                    Triple result = efd.getResult();
                                    List<Triple> matches = efd.getMatchList();
                                    BackwardRuleInfGraphI infGraph = engine.getInfGraph();
                                    RuleDerivation d = new RuleDerivation(envFrame.getRule(), result, matches, infGraph);
                                    infGraph.logDerivation(result, d);
                                    
                                    // Also want to record this result in the calling frame
                                    if (envFrame.link instanceof EnvironmentFrameWithDerivation) {
                                        EnvironmentFrameWithDerivation pefd = (EnvironmentFrameWithDerivation)envFrame.link;
                                        pefd.noteMatch(new TriplePattern(result), pc);
                                    }
                                }
                            }
                            envFrame = (EnvironmentFrame) envFrame.link;
                            if (envFrame != null) {
                                clause = envFrame.clause;
                            }
                            continue interpreter;
                        
                        case RuleClauseCode.CALL_BUILTIN:
                            Builtin builtin = (Builtin)args[ac++];
                            if (context == null) {
                                BBRuleContext bbcontext = new BBRuleContext(engine.getInfGraph());
                                bbcontext.setEnv(new LPBindingEnvironment(this));
                                context = bbcontext;
                            }
                            context.setRule(clause.getRule());
                            if (!builtin.bodyCall(argVars, code[pc++], context)) {
                                if (traceOn) logger.info("FAIL " + clause + ", due to " + builtin.getName());
                                continue main;  
                            }
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
     * Tracing support - return a format set of triple queries/results.
     */
    private String getArgTrace() {
        StringBuilder temp = new StringBuilder();
        temp.append(PrintUtil.print(deref(argVars[0])));
        temp.append(" ");
        temp.append(PrintUtil.print(deref(argVars[1])));
        temp.append(" ");
        temp.append(PrintUtil.print(deref(argVars[2])));
        return temp.toString();
    }
    
    /**
     * Set up a triple match choice point as part of a CALL.
     */
    private void setupTripleMatchCall(int pc, int ac) {
        TripleMatchFrame tmFrame = new TripleMatchFrame(this);
        tmFrame.setContinuation(pc, ac);
        tmFrame.linkTo(cpFrame);
        cpFrame = tmFrame;
    }
    
    /**
     * Set up a clause choice point as part of a CALL.
     */
    private void setupClauseCall(int pc, int ac, List<RuleClauseCode> clauses, boolean isSingleton) {
        ChoicePointFrame newChoiceFrame = new ChoicePointFrame(this, clauses, isSingleton);
        newChoiceFrame.linkTo(cpFrame);
        newChoiceFrame.setContinuation(pc, ac);
        cpFrame = newChoiceFrame;
    }
    
    /**
     * Set up a tabled choice point as part of a CALL.
     */
    private void setupTabledCall(int pc, int ac) {
        ConsumerChoicePointFrame ccp = new ConsumerChoicePointFrame(this);
        ccp.linkTo(cpFrame);
        ccp.setContinuation(pc, ac);
        cpFrame = ccp;
    }
    
    /**
     * Preserve the current interpter state in the consumer choice point at the top
     * of the choice point tree.
     */
    public void preserveState(ConsumerChoicePointFrame ccp) {
        ccp.preserveState(trail);
    }
    
    /**
     * Restore the interpter state according to the given consumer choice point.
     */
    public void restoreState(ConsumerChoicePointFrame ccp) {
        cpFrame = ccp;
        ccp.restoreState(this);
        iContext = ccp.context;
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
     * Check if a node values is now grounded
     */
    public static boolean isGrounded(Node node) {
        return !( deref(node) instanceof Node_RuleVariable );
    }
    
    /**
     * Return a dereferenced copy of a triple.
     */
    public static Triple deref(TriplePattern t) {
        if (t == null) return null;
        return new Triple(deref(t.getSubject()), deref(t.getPredicate()), deref(t.getObject()));
    }
    
    /**
     * Derefernce a node which may be a functor node
     */
    public static Node derefPossFunctor(Node node) {
        if (node instanceof Node_RuleVariable) {
            Node dnode = ((Node_RuleVariable)node).deref();
            if (dnode.isVariable()) {
                // Problem with variable in return result  "should never happen"
                throw new ReasonerException("Internal error in LP reasoner: variable in triple result");
            }
            if (Functor.isFunctor(dnode)) {
                Functor f = (Functor) dnode.getLiteralValue();
                Node[] fargs = f.getArgs();
                boolean needCopy = false;
                for ( Node farg : fargs )
                {
                    if ( farg.isVariable() )
                    {
                        needCopy = true;
                        break;
                    }
                }
                if (needCopy) {
                    Node[] newArgs = new Node[fargs.length];
                    for (int i = 0; i < fargs.length; i++) {
                        newArgs[i] = deref(fargs[i]);
                    }
                    dnode = Functor.makeFunctorNode(f.getName(), newArgs);
                }
                return dnode;
            } else {
                return dnode;
            }
        } else {
            return node;
        }
    }
    
    /**
     * Standardize a node by replacing instances of wildcard ANY by new distinct variables.
     * This is used in constructing the arguments to a top level call from a goal pattern.
     * @param node the node to be standardized
     * @param mappedVars known mappings from input variables to local variables
     */
    private Node standardize(Node node, Map<Node, Node> mappedVars) {
        Node dnode = deref(node);
        if (node == Node.ANY || node == Node_RuleVariable.WILD) {
            return new Node_RuleVariable(null, 0);
        } else if (dnode.isVariable()) {
            Node mnode = mappedVars.get(dnode);
            if (mnode == null) {
                mnode = new Node_RuleVariable(null, 0);
                mappedVars.put(dnode, mnode); 
            }
            return mnode;
        } else {
            return dnode;
        }
    }
        
}
