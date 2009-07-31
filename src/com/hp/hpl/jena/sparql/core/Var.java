/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.ExprVar;

/** A SPARQL variable
 * 
 * @author Andy Seaborne
 */

public class Var extends Node_Variable
{
    public static Var alloc(String varName)     { return new Var(varName) ; }
    public static Var alloc(Node_Variable v)    // asVar?
    { 
        if ( v instanceof Var )
            return (Var)v ;
        return new Var(v) ;
    }
    
    public static Var alloc(Node v) // asVar?
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
    
    private Var(String varName)      { super(varName) ; }
    
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
    public int hashCode() { return super.hashCode() ; }

    @Override
    public boolean equals(Object other)
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
        List<Var> x = new ArrayList<Var>() ;
        for (String obj : varNames)
            x.add(Var.alloc(obj)) ;
        return x ;
    }
    
    /** Return a list of String names from a collection of variables */ 
    public static List<String> varNames(Collection<Var> vars)
    {
        List<String> x = new ArrayList<String>() ;
        for (Var var : vars)
            x.add(var.getVarName()) ;
        return x ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */