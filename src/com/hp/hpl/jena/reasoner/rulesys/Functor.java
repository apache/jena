/******************************************************************
 * File:        Functor.java
 * Created by:  Dave Reynolds
 * Created on:  29-Mar-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Functor.java,v 1.4 2003-05-20 17:31:36 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.datatypes.*;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * A functor comprises a functor name and a list of 
 * arguments. The arguments are Nodes of any type except functor nodes
 * (there is no functor nesting).  Functors play three roles in rules -
 * in heads they represent actions (procedural attachement); in bodies they
 * represent builtin predicates; in TriplePatterns they represent embedded
 * structured literals that are used to cache matched subgraphs such as
 * restriction specifications.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-05-20 17:31:36 $
 */
public class Functor {
    /** Functor's name */
    protected String name;
    
    /** Argument list - an array of nodes */
    protected Node[] args;
    
    /** A built in that implements the functor */
    protected Builtin implementor;
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(Functor.class);
    
    /**
     * Constructor. 
     * @param name the name of the functor
     * @param args a list of nodes defining the arguments
     */
    public Functor(String name, List args) {
        this.name = name;
        this.args = (Node[]) args.toArray(new Node[]{});
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
    public Functor(String name, List args, BuiltinRegistry registry) {
        this.name = name;
        this.args = (Node[]) args.toArray(new Node[]{});
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
     * Returns true if the functor is fully ground, no variables
     */
    public boolean isGround() {
        for (int i = 0; i < args.length; i++) {
            Node n = args[i];
            if (n instanceof Node_RuleVariable || n instanceof Node_ANY) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns true if the functor is fully ground in the given environment
     */
    public boolean isGround(BindingEnvironment env) {
        for (int i = 0; i < args.length; i++) {
            Node n = args[i];
            if (env.getGroundVersion(args[i]).isVariable()) return false;
        }
        return true;
    }
    
    /**
     * Execute the given built in as a body clause.
     * @param context an execution context giving access to other relevant data
     * @return true if the functor has an implementation and that implementation returns true when evaluated
     */
    public boolean evalAsBodyClause(RuleContext context) {
        if (implementor == null) {
            logger.warn("Invoking undefined functor " + getName() + " in " + context.getRule().toShortString());
            return false;
        }
        return implementor.bodyCall(getBoundArgs(context.getEnv()), context);
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
    public String toString() {
        StringBuffer buff = new StringBuffer(name);
        buff.append("(");
        for (int i = 0; i < args.length; i++) {
            buff.append(PrintUtil.print(args[i]));
            if (i == args.length - 1) {
                buff.append(")");
            } else {
                buff.append(" ");
            }
        }
        return buff.toString();
    }

    /**
     * tests that a given Node represents a functor
     */
    public static boolean isFunctor(Node n) {
        if (n == null) return false;
        return n.isLiteral() && n.getLiteral().getDatatype() == FunctorDatatype.theFunctorDatatype;
    }
    
    /**
     * Equality is based on structural comparison
     */
    public boolean equals(Object obj) {
        if (obj instanceof Functor) {
            Functor f2 = (Functor)obj;
            if (name.equals(f2.name)) {
                for (int i = 0; i < args.length; i++) {
                    if (!args[i].sameValueAs(f2.args[i])) return false;
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
        Functor f = new Functor(name, args);
        LiteralLabel ll = new LiteralLabel(f, null, FunctorDatatype.theFunctorDatatype);
        return new Node_Literal(ll);
    }
    
    /**
     * Wrap  a functor as a Literal node
     * @param f the functor data structure to be wrapped in a node.
     */
    public static Node makeFunctorNode(Functor f) {
        LiteralLabel ll = new LiteralLabel(f, null, FunctorDatatype.theFunctorDatatype);
        return new Node_Literal(ll);
    }
    
   /**
    * Inner class. Dummy datatype definition for 
    * functor-valued literals.
    */
   public static class FunctorDatatype extends BaseDatatype {
    
        public FunctorDatatype() {
            super("Functor");
        }
        
        public static final RDFDatatype theFunctorDatatype = new FunctorDatatype();
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
