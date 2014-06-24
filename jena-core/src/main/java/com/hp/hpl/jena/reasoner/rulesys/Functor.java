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

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.datatypes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A functor comprises a functor name and a list of 
 * arguments. The arguments are Nodes of any type except functor nodes
 * (there is no functor nesting).  Functors play three roles in rules -
 * in heads they represent actions (procedural attachement); in bodies they
 * represent builtin predicates; in TriplePatterns they represent embedded
 * structured literals that are used to cache matched subgraphs such as
 * restriction specifications.
 */
public class Functor implements ClauseEntry {
    /** Functor's name */
    protected String name;
    
    /** Argument list - an array of nodes */
    protected Node[] args;
    
    /** A built in that implements the functor */
    protected Builtin implementor;
    
    /** A static Filter instance that detects triples with Functor objects */
    public static final Filter<Triple> acceptFilter = new Filter<Triple>() {
                @Override
                public boolean accept( Triple t) {
                    if (t.getSubject().isLiteral()) return true;
                    Node n = t.getObject();
                    return n.isLiteral() && n.getLiteralDatatype() == FunctorDatatype.theFunctorDatatype;
                }
            };
    
    protected static Logger logger = LoggerFactory.getLogger(Functor.class);
    
    /**
     * Constructor. 
     * @param name the name of the functor
     * @param args a list of nodes defining the arguments
     */
    public Functor(String name, List<Node> args) {
        this.name = name;
        this.args = args.toArray(new Node[]{});
    }
    
    /**
     * Constructor. 
     * @param name the name of the functor
     * @param args an array of nodes defining the arguments, this will not be copied so beware of
     * accidental structure sharing
     */
    public Functor(String name, Node[] args) {
        this.name = name;
        this.args = args;
    }
    
    /**
     * Constructor
     * @param name the name of the functor
     * @param args a list of nodes defining the arguments
     * @param registry a table of builtins to consult to check for 
     * implementations of this functor when used as a rule clause
     */
    public Functor(String name, List<Node> args, BuiltinRegistry registry) {
        this.name = name;
        this.args = args.toArray(new Node[]{});
        this.implementor = registry.getImplementation(name);
    }
    
    /**
     * Return the functor name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Return the functor aguments as an array of nodes
     */
    public Node[] getArgs() {
        return args;
    }
    
    /**
     * Return the length of the functor argument array.
     */
    public int getArgLength() {
        return args.length;
    }
    
    /**
     * Returns true if the functor is fully ground, no variables
     */
    public boolean isGround() {
        for ( Node n : args )
        {
            if ( n instanceof Node_RuleVariable || n instanceof Node_ANY )
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns true if the functor is fully ground in the given environment
     */
    public boolean isGround(BindingEnvironment env) {
        for ( Node n : args )
        {
            if ( env.getGroundVersion( n ).isVariable() )
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Execute the given built in as a body clause.
     * @param context an execution context giving access to other relevant data
     * @return true if the functor has an implementation and that implementation returns true when evaluated
     */
    public boolean evalAsBodyClause(RuleContext context) {
        if (getImplementor() == null) {
            logger.warn("Invoking undefined functor " + getName() + " in " + context.getRule().toShortString());
            return false;
        }
        return implementor.bodyCall(getBoundArgs(context.getEnv()), args.length, context);
    }
    
    /**
     * Execute the given built in as a body clause, only if it is side-effect-free.
     * @param context an execution context giving access to other relevant data
     * @return true if the functor has an implementation and that implementation returns true when evaluated
     */
    public boolean safeEvalAsBodyClause(RuleContext context) {
        if (getImplementor() == null) {
            logger.warn("Invoking undefined functor " + getName() + " in " + context.getRule().toShortString());
            return false;
        }
        if (implementor.isSafe()) {
            return implementor.bodyCall(getBoundArgs(context.getEnv()), args.length, context);
        } else {
            return false;
        }
    }
    
    /**
     * Return a new Node array containing the bound versions of this Functor's arguments
     */
    public Node[] getBoundArgs(BindingEnvironment env) {
        Node[] boundargs = new Node[args.length];
        for (int i = 0; i < args.length; i++) {
            boundargs[i] = env.getGroundVersion(args[i]);
        }
        return boundargs;
    }
    
    /**
     * Return the Builtin that implements this functor
     * @return the Builtin or null if there isn't one
     */
    public Builtin getImplementor() {
        if (implementor == null) {
            implementor = BuiltinRegistry.theRegistry.getImplementation(name);
        }
        return implementor;
    }
    
    /**
     * Set the Builtin that implements this functor.
     */
    public void setImplementor(Builtin implementor) {
        this.implementor = implementor;
    }
    
    /**
     * Printable string describing the functor
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder(name);
        buff.append("(");
        for (int i = 0; i < args.length; i++) {
            buff.append(PrintUtil.print(args[i]));
            if (i < args.length - 1) {
                buff.append(" ");
            }
        }
        buff.append(")");
        return buff.toString();
    }

    /**
     * tests that a given Node represents a functor
     */
    public static boolean isFunctor(Node n) {
        if (n == null) return false;
        return n.isLiteral() && n.getLiteralDatatype() == FunctorDatatype.theFunctorDatatype;
    }
    
    /**
     * Equality is based on structural comparison
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Functor) {
            Functor f2 = (Functor)obj;
            if (name.equals(f2.name) && args.length == f2.args.length) {
                for (int i = 0; i < args.length; i++) {
                    if (!args[i].sameValueAs(f2.args[i])) return false;
                }
                return true;
            }
        }
        return false;
    }
    
    /** hash function override */
    @Override
    public int hashCode() {
        return (name.hashCode()) ^ (args.length << 2);
    }
    
    /**
     * Compare Functors, taking into account variable indices.
     * The equality function ignores differences between variables.
     */
    @Override
    public boolean sameAs(Object o) {
        if (o instanceof Functor) {
            Functor f2 = (Functor)o;
            if (name.equals(f2.name) && args.length == f2.args.length) {
                for (int i = 0; i < args.length; i++) {
                    if (! Node_RuleVariable.sameNodeAs(args[i], f2.args[i])) return false;
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Create a functor and wrap it up as a Literal node
     * @param name the name of the functor
     * @param args an array of nodes defining the arguments, this will not be copied so beware of
     * accidental structure sharing
     */
    public static Node makeFunctorNode(String name, Node[] args) {
        return makeFunctorNode( new Functor( name, args ) );
    }
    
    /**
     * Wrap  a functor as a Literal node
     * @param f the functor data structure to be wrapped in a node.
     */
    public static Node makeFunctorNode(Functor f) {
        return NodeFactory.createUncachedLiteral(f, null, FunctorDatatype.theFunctorDatatype);
    }
    
   /**
    * Inner class. Dummy datatype definition for 
    * functor-valued literals.
    */
   public static class FunctorDatatype extends BaseDatatype {
    
        public FunctorDatatype() {
            super("urn:x-hp-jena:Functor");
        }
        
        public static final RDFDatatype theFunctorDatatype = new FunctorDatatype();
   }

}
