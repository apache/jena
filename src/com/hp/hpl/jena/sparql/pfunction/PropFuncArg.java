/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.pfunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintSerializableBase;
import com.hp.hpl.jena.sparql.util.PrintUtils;

/** Class representing an argument (subject or object position) of a property function.
 *  Such an argument can be a graph node (variable, IRI,  
 * 
 *  Blank nodes from the query will be seen as variables.  Most implementations will want to
 *  work with the property function arguments after substitution from the current binding.
 *  See method {@link #evalIfExists(Binding)} below.   
 *  
 * @author Andy Seaborne
 */

public class PropFuncArg extends PrintSerializableBase
{
    private List argList = null ;
    private Node arg = null ;
    
    public PropFuncArg(List argList, Node arg)
    {
        // arg is always the argument, which may be a list in argList.
        // If it's a list, remember that.
        if ( argList == null )
        {
            this.arg = arg ;
            return ;
        }
        this.argList = argList ;
    }
        
    public PropFuncArg(List argList)    { this.argList = argList ; }
    public PropFuncArg(Node arg)        { this.arg = arg ; }
    
    public Node getArg()                { return arg ; }
    public List getArgList()            { return argList ; }
    public int  getArgListSize()        { return argList==null ? -1 : argList.size() ; }
    public Node getArg(int index)
    {
        if ( argList == null ) return null ;
        return (Node)argList.get(index) ;
    }
    
    public boolean isList()             { return argList != null  ; }
    public boolean isNode()             { return arg != null  ; }
    
    public void output(IndentedWriter out, final SerializationContext sCxt)
    {
        if ( argList == null && arg == null )
            out.print("<<null>>") ;
        if ( argList != null )
        {
            out.print("(") ;
            PrintUtils.printList(out, argList, " ",
                                 new PrintUtils.Fmt(){
                                    public String fmt(Object thing)
                                    {
                                        return FmtUtils.stringForNode((Node)thing, sCxt) ;
                                    }}) ;
            out.print(")") ;
        }
        if ( arg != null )
            out.print(FmtUtils.stringForNode(arg)) ;
    }
    
    /** Create a new PropFuncArg by replacing any variables by their values given in the binding.
     *  If there is no binding, keep the variable.
     *  
     * @param binding
     * @return A PropFuncArg with any varibales substituted by values in the binding
     */  
    
    public PropFuncArg evalIfExists(Binding binding)
    {
        if ( ! isList() )
            return new PropFuncArg(evalIfExistsOneArg(binding, arg)) ;
        List newArgList = new ArrayList() ;
        for ( Iterator iter = argList.iterator() ; iter.hasNext() ; )
        {
            Node n = (Node)iter.next();
            newArgList.add(evalIfExistsOneArg(binding, n)) ;
        }
        return new PropFuncArg(newArgList) ;
    }
    
    private static Node evalIfExistsOneArg(Binding binding, Node n)
    {
        if ( ! n.isVariable() )
            return n ;
        Node r = binding.get(Var.alloc(n)) ; 
        return ( r != null ) ? r : n ; 
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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