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

package com.hp.hpl.jena.sparql.core ;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Node_Variable ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;

/** A SPARQL variable */

public class Var extends Node_Variable
{
    /* Variable used to indicate "don't bind"
     * Each use is unique.
     */
    
    // Legal SPARQL variable name but note it must be exactly this
    // object, not just the same name, to be anonymous.
    public static Var ANON = new Var("?_") ; 
    
    public static Var alloc(String varName)
    {
//        if ( varName.equals("_") )
//            return ANON ;
        return new Var(varName) ;
    }
    
    public static Var alloc(Node_Variable v)    // asVar?
    { 
        if ( v instanceof Var )
            return (Var)v ;
        return new Var(v) ;
    }
    
    public static Var alloc(Node v)
    { 
        if ( v instanceof Var )
            return (Var)v ;
        if ( v instanceof Node_Variable )
            return new Var((Node_Variable)v) ;
        throw new NotAVariableException("Node: "+v) ;
    }
    
    public static Var alloc(Var v)
    {
        return v ;
    }
    
    public static Var alloc(ExprVar nv)         { return new Var(nv) ; }
    
    public static Node lookup(Binding binding, Node node)
    {
        if ( ! Var.isVar(node) )
            return node ;
        Var var = Var.alloc(node) ;
        return lookup(binding, var) ;
    }
    
    public static Node lookup(Binding binding, Var var)
    {
        Node n = binding.get(var) ;
        if ( n != null )
            return n ;
        return var ;
    }
    
    // Precalulated the hash code because hashCode() is used so heavily with Var's
    private final int hashCodeValue ;  
    
    private Var(String varName)      { super(varName) ; hashCodeValue = super.hashCode() ; }
    
    private Var(Node_Variable v)     { this( v.getName() ) ; }
    
    private Var(ExprVar v)           { this(v.getVarName()) ; }
    
    // Not needed
    public Node asNode() { return this ; }
    
    public String getVarName() { return getName() ; }
    
    static class NotAVariableException extends ARQInternalErrorException
    {
        NotAVariableException(String msg) { super(msg) ; }
    }
    
    @Override
    public final int hashCode() { return hashCodeValue ; }

    @Override
    public final boolean equals(Object other)
    { 
        if ( this == other ) return true ;
        if ( ! ( other instanceof Var ) ) return false ;
        return super.equals(other) ;
    }
    
//    @Override
//    public String toString() { return node.toString() ; }

    public boolean isNamedVar() { return isNamedVarName(getName()) ; }
    
    public boolean isBlankNodeVar() { return isBlankNodeVarName(getName()) ; }
    
    public boolean isAllocVar() { return isAllocVarName(getName()) ; }
    
    public boolean isAnonVar() { return isAnonVar(this) ; }
    
    // -------
    
    public static String canonical(String x)
    {
        if ( x.startsWith("?") )
            return x.substring(1) ;
        if ( x.startsWith("$") )
            return x.substring(1) ;
        return x ;
    }

    public static boolean isVar(Node node)
    {
        if ( node instanceof Var ) return true ;
        if ( node != null && node.isVariable() )
            throw new NotAVariableException("Node_variable (not a Var) found") ;
        return false ;
    }
    
    public static boolean isRenamedVar(Node node)
    { return node.isVariable() && isRenamedVar(node.getName()) ; }
    
    public static boolean isRenamedVar(String x)
    { return x.startsWith(ARQConstants.allocVarScopeHiding) ; }
    
    public static boolean isNamedVar(Node node)
    { return node.isVariable() && isNamedVarName(node.getName()) ; }

    public static boolean isNamedVarName(String x)
    { return ! isBlankNodeVarName(x) && ! isAllocVarName(x) ; }

    public static boolean isBlankNodeVar(Node node)
    { return node.isVariable() && isBlankNodeVarName(node.getName()) ; }

    public static boolean isBlankNodeVarName(String x)
    { return x.startsWith(ARQConstants.allocVarAnonMarker) ; }

    public static boolean isAllocVar(Node node)
    { return node.isVariable() && isAllocVarName(node.getName()) ; }
    
    public static boolean isAllocVarName(String x)
    { return x.startsWith(ARQConstants.allocVarMarker) ; }
    
    /** Convert a collection of variable names to variables */ 
    public static List<Var> varList(Collection<String> varNames)
    {
        List<Var> x = new ArrayList<>() ;
        for (String obj : varNames)
            x.add(Var.alloc(obj)) ;
        return x ;
    }
    
    public static boolean isAnonVar(Var var)
    {
        return var == ANON ;
    }
    
    /** Return a list of String names from a collection of variables */ 
    public static List<String> varNames(Collection<Var> vars)
    {
        List<String> x = new ArrayList<>() ;
        for (Var var : vars)
            x.add(var.getVarName()) ;
        return x ;
    }
}
