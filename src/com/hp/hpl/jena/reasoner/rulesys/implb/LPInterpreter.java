/******************************************************************
 * File:        LPBRuleEngine.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPInterpreter.java,v 1.22 2003-08-12 23:11:04 der Exp $
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
 * @version $Revision: 1.22 $ on $Date: 2003-08-12 23:11:04 $
 */
public class LPInterpreter {

    //  =======================================================================
    //  Variables

    /** The engine which is using this interpreter */
    protected LPBRuleEngine engine;

    /** The execution context that should be notified of suspended branches */
    protected LPInterpreterContext iContext;
    
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
        
    /** Trick to allow the very top level triple lookup to return results with reduced store turnover */
    protected TopLevelTripleMatchFrame topTMFrame;
    
    /** Original set up goal, only used for debugging */
    protected TriplePattern goal;
    
    /** log4j logger*/
    static Logger logger = Logger.getLogger(LPInterpreter.class);

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
    public LPInterpreter(LPBRuleEngine engine, TriplePattern goal, List clauses, boolean isTop) {
        this.engine = engine;
        this.goal = goal;       // Used for debug only
        // Temp ...
        // TODO: remove
        if (goal.getSubject().isVariable() 
            && goal.getPredicate().equals(com.hp.hpl.jena.vocabulary.RDFS.Nodes.subClassOf)
            && goal.getObject().isVariable()) {
                System.out.println("Creating interpreter: " + goal);
            }
        // ... end temp
        
        // Construct dummy top environemnt which is a call into the clauses for this goal
        envFrame = new EnvironmentFrame(RuleClauseCode.returnCodeBlock);
        envFrame.allocate(RuleClauseCode.MAX_PERMANENT_VARS);
        envFrame.pVars[0] = argVars[0] = standardize(goal.getSubject());
        envFrame.pVars[1] = argVars[1] = standardize(goal.getPredicate());
        envFrame.pVars[2] = argVars[2] = standardize(goal.getObject());
        
        if (clauses != null && clauses.size() > 0) {
            if (isTop && engine.getRuleStore().isTabled(goal)) {
                setupTabledCall(0, 0);
//                setupClauseCall(0, 0, clauses);
            } else {
                setupClauseCall(0, 0, clauses);
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
     * @param context the generator choice point or top level iterator which 
     * is requesting this result and might have preserved state to restore
     * @return either a StateFlag or  a result Triple
     */
    public synchronized Object next() {
        // Temp ...
        if (cpFrame == topTMFrame && argVars[0].isURI() && argVars[0].getURI().equals("C1") &&
                argVars[1].equals(com.hp.hpl.jena.vocabulary.RDF.Nodes.type) &&
                argVars[2].equals(com.hp.hpl.jena.vocabulary.RDFS.Nodes.Class)) {
                    System.out.println("Starting strange case ...");
                    // TODO: remove
                }
        // ... end temp
        StateFlag answer = run();
        if (answer == StateFlag.FAIL || answer == StateFlag.SUSPEND) {
            return answer;
        } else if (answer == StateFlag.SATISFIED) {
            return topTMFrame.lastMatch;
        } else {
            Triple t = new Triple(deref(pVars[0]), deref(pVars[1]), derefPossFunctor(pVars[2]));
            System.out.println("Ans: " + t);
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
        
        // Temp ...
        // TODO: remove
        if (goal.getSubject().isVariable() 
            && goal.getPredicate().equals(com.hp.hpl.jena.vocabulary.RDFS.Nodes.subClassOf)
            && goal.getObject().isVariable()) {
                System.out.println("Running interpreter: " + goal);
            }
        // ... end temp

        main: while (cpFrame != null) {
            // restore choice point
            if (cpFrame instanceof ChoicePointFrame) {
                choice = (ChoicePointFrame)cpFrame;
                if (!choice.hasNext()) {
                    // No more choices left in this choice point
                    cpFrame = choice.getLink();
                    continue main;
                }
                
                clause = (RuleClauseCode)choice.nextClause();
                // Temp ...
                // TODO: remove
//                if (clause.rule.getName().equals("rdfs7") && deref(argVars[2]).isURI() &&
//                        deref(argVars[2]).getURI().equals("C1")) {
                if (clause.rule.getName() != null && clause.rule.getName().equals("rdfs7") ) {
                    System.out.println("Starting choice on suspect rule. args = " + deref(argVars[0]) + " " + deref(argVars[1]) + " " + deref(argVars[2]));
                    System.out.println("Interpreter invocation goal was: " + goal);
                    if (iContext instanceof Generator) {
                        System.out.println("Invoked by generator with goal: " + ((Generator)iContext).goal);
                    }
                }
                // ... end temp
                // Create an execution environment for the new choice of clause
                envFrame = new EnvironmentFrame(clause);
                envFrame.linkTo(choice.envFrame);
                envFrame.cpc = choice.cpc;
                envFrame.cac = choice.cac;
//                logger.debug("Retry rule: " + envFrame);
                
                // Restore the choice point state
                System.arraycopy(choice.argVars, 0, argVars, 0, RuleClauseCode.MAX_ARGUMENT_VARS);
                int trailMark = choice.trailIndex;
                if (trailMark < trail.size()) {
                    unwindTrail(trailMark);
                }
                pc = ac = 0;
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
                    continue main;
                }
                pc = tmFrame.cpc;
                ac = tmFrame.cac;

                // then fall through to the execution context in which the the match was called
                
            } else if (cpFrame instanceof TopLevelTripleMatchFrame) {
                TopLevelTripleMatchFrame tmFrame = (TopLevelTripleMatchFrame)cpFrame;
                
                // Find the next choice result directly
                if (!tmFrame.nextMatch(this)) {
                    // No more matches
                    cpFrame = cpFrame.getLink();
                    continue main;
                } else {
                    // Match but this is the top level so return the triple directly
                    return StateFlag.SATISFIED;
                }
                
            } else if (cpFrame instanceof ConsumerChoicePointFrame) {
                ConsumerChoicePointFrame ccp = (ConsumerChoicePointFrame)cpFrame;
                
                // Restore the calling context
                envFrame = ccp.envFrame;
                clause = envFrame.clause;
                int trailMark = ccp.trailIndex;
                if (trailMark < trail.size()) {
                    unwindTrail(trailMark);
                }
                
                // Find the next choice result directly
                StateFlag state = ccp.nextMatch(this);
                if (state == StateFlag.FAIL) {
                    // No more matches
                    cpFrame = cpFrame.getLink();
                    continue main;
                } else if (state == StateFlag.SUSPEND) {
                    // Require other generators to cycle before resuming this one
                    preserveState(ccp);
                    iContext.notifyBlockedOn(ccp);
                    cpFrame = cpFrame.getLink();
                    continue main;
                }

                pc = ccp.cpc;
                ac = ccp.cac;

                // then fall through to the execution context in which the the match was called
                
            } else {
                throw new ReasonerException("Internal error in backward rule system, unrecognized choice point");
            }
            
            interpreter: while (envFrame != null) {

                // Start of bytecode intepreter loop
                // Init the state variables
                pVars = envFrame.pVars;
                int yi, ai, ti;
                Node arg, constant;
                List predicateCode;
                TripleMatchFrame tmFrame;
                code = clause.getCode();
                args = clause.getArgs();

                // Debug ...
//                logger.debug("Entering rule: " + envFrame);
//                System.out.println("Interpeting code (at p = " + pc + "):");
//                envFrame.clause.print(System.out);
        
                codeloop: while (true) {
                    switch (code[pc++]) {
                        case RuleClauseCode.ALLOCATE:
                            envFrame.allocate(RuleClauseCode.MAX_PERMANENT_VARS);
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
//                                    logger.debug("FAIL: " + envFrame);
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
                                Functor funcArg = (Functor)o.getLiteral().getValue();
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
                            if (!match) continue main;      // fail to unify functor shape
                            break;
                            
                        case RuleClauseCode.UNIFY_VARIABLE :
                            yi = code[pc++];
                            ai = code[pc++];
                            if (!unify(argVars[ai], pVars[yi])) {
//                                logger.debug("FAIL: " + envFrame);
                                continue main;  
                            }
                            break;
    
                        case RuleClauseCode.UNIFY_TEMP :
                            ti = code[pc++];
                            ai = code[pc++];
                            if (!unify(argVars[ai], tVars[ti])) {
//                                logger.debug("FAIL: " + envFrame);
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
                            List clauses = (List)args[ac++];
                            setupClauseCall(pc, ac, clauses);
                            setupTripleMatchCall(pc, ac);
                            continue main;
                                            
                        case RuleClauseCode.CALL_PREDICATE_INDEX:
                            // This code path is experimental, don't yet know if it has enough 
                            // performance benefit to justify the cost of maintaining it.
                            clauses = (List)args[ac++];
                            // Check if we can futher index the clauses
                            if (!argVars[2].isVariable()) {
                                clauses = engine.getRuleStore().codeFor(
                                    new TriplePattern(argVars[0], argVars[1], argVars[2]));
                            }
                            setupClauseCall(pc, ac, clauses);
                            setupTripleMatchCall(pc, ac);
                            continue main;
                                            
                         case RuleClauseCode.CALL_TRIPLE_MATCH:
                            setupTripleMatchCall(pc, ac);
                            continue main;
                         
                        case RuleClauseCode.CALL_TABLED:
                            setupTabledCall(pc, ac);
                            continue main;
                                            
                        case RuleClauseCode.CALL_WILD_TABLED:
                            if (engine.getRuleStore().isTabled(argVars[1])) {
                                setupTabledCall(pc, ac);
                            } else {
                                // normal call set up
                                clauses = engine.getRuleStore().codeFor(
                                    new TriplePattern(argVars[0], argVars[1], argVars[2]));
                                setupClauseCall(pc, ac, clauses);
                                setupTripleMatchCall(pc, ac);
                            }
                            continue main;
                                            
                        case RuleClauseCode.PROCEED:
                            pc = envFrame.cpc;
                            ac = envFrame.cac;
//                            logger.debug("EXIT " + envFrame);
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
//                                logger.debug("FAIL: " + envFrame.clause.rule.toShortString());
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
     * Set up a triple match choice point as part of a CALL.
     */
    private void setupTripleMatchCall(int pc, int ac) {
        TripleMatchFrame tmFrame = new TripleMatchFrame(this);
        tmFrame.setContinuation(pc, ac);
        tmFrame.linkTo(cpFrame);
        cpFrame = tmFrame;
//        logger.debug("CALL triple match: " + 
//                        deref(argVars[0]) + " " +
//                        deref(argVars[1]) + " " +
//                        deref(argVars[2]));
    }
    
    /**
     * Set up a clause choice point as part of a CALL.
     */
    private void setupClauseCall(int pc, int ac, List clauses) {
        ChoicePointFrame newChoiceFrame = new ChoicePointFrame(this, clauses);
        newChoiceFrame.linkTo(cpFrame);
        newChoiceFrame.setContinuation(pc, ac);
        cpFrame = newChoiceFrame;
    }
    
    /**
     * Set up a tabled choice point as part of a CALL.
     */
    private void setupTabledCall(int pc, int ac) {
        // Temp ...
        // TODO: remove
        if (deref(argVars[0]).isURI() && deref(argVars[0]).getURI().equals("C1") &&
                deref(argVars[1]).equals(com.hp.hpl.jena.vocabulary.RDF.Nodes.type) &&
                deref(argVars[2]).equals(com.hp.hpl.jena.vocabulary.RDFS.Nodes.Class)) {
                    System.out.println("Seting up call ...");
                }
        // ... end temp
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
        // Save the args
        System.arraycopy(argVars, 0, ccp.argVars, 0, argVars.length);
        // Save the trail state
        int trailLen = trail.size();
        if (trailLen > ccp.trailLength) {
            ccp.trailValues = new Node[trailLen];
            ccp.trailVars = new Node_RuleVariable[trailLen];
        }
        ccp.trailLength = trailLen;
        for (int i = 0; i < trailLen; i++) {
            Node_RuleVariable var = (Node_RuleVariable) trail.get(i);
            ccp.trailVars[i] = var;
            ccp.trailValues[i] = var.getRawBoundValue();
        }
    }
    
    /**
     * Restore the interpter state according to the given consumer choice point.
     */
    public void restoreState(ConsumerChoicePointFrame ccp) {
        // Temp
        Node s = ccp.goal.getSubject(); 
        if (s.isURI() && s.getURI().equals("C1")) {
            System.out.println("Restore of " + ccp.goal);
        }
        cpFrame = ccp;
        System.arraycopy(ccp.argVars, 0, argVars, 0, argVars.length);
        unwindTrail(0);
        for (int i = 0; i < ccp.trailLength; i++) {
            bind(ccp.trailVars[i], ccp.trailValues[i]);
        }
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
     * Derefernce a node which may be a functor node
     */
    public static Node derefPossFunctor(Node node) {
        if (node instanceof Node_RuleVariable) {
            Node dnode = ((Node_RuleVariable)node).deref();
            if (dnode.isVariable()) {
                // Problem with variable in return result
                // Temp debug ...
                System.out.println("Hit problem");
            }
            if (Functor.isFunctor(dnode)) {
                Functor f = (Functor) dnode.getLiteral().getValue();
                Node[] fargs = f.getArgs();
                boolean needCopy = false;
                for (int i = 0; i < fargs.length; i++) {
                    if (fargs[i].isVariable()) {
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
     */
    private Node standardize(Node node) {
        if (node == Node.ANY || node == Node_RuleVariable.WILD) {
            return new Node_RuleVariable(null, 0);
        } else {
            // TEmp...
            // TODO: remove
            Node_RuleVariable temp = new Node_RuleVariable(null, 0);
            unify(node, temp);
            return temp;
            // ... end temp
//            return node;
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