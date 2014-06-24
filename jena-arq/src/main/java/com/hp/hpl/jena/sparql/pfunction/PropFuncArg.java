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

package com.hp.hpl.jena.sparql.pfunction;

import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.PrintSerializableBase ;

/** Class representing an argument (subject or object position) of a property function.
 *  Such an argument can be a graph node (variable, IRI, literal).
 * 
 *  Blank nodes from the query will be seen as variables.  Most implementations will want to
 *  work with the property function arguments after substitution from the current binding. */

public class PropFuncArg extends PrintSerializableBase
{
    private List<Node> argList = null ;
    private Node arg = null ;
    
    public PropFuncArg(List<Node> argList, Node arg)
    {
        // arg is always the argument, which may be a list in argList.
        // If it's a list, remember that.
        if ( argList == null )
        {
            this.arg = arg ;
            return ;
        }
        this.argList = argList ;
        // If the list is zero length, it's rdf:nil.  Be careful!
        if ( argList.isEmpty() )
            this.arg = NodeConst.nodeNil ;
    }
        
    public PropFuncArg(List<Node> argList)  { this.argList = argList ; }
    public PropFuncArg(Node arg)            { this.arg = arg ; }
    
    public Node getArg()                    { return arg ; }
    public List<Node> getArgList()          { return argList ; }
    public int  getArgListSize()            { return argList==null ? -1 : argList.size() ; }
    public Node getArg(int index)
    {
        if ( argList == null ) return null ;
        return argList.get(index) ;
    }
    
    @Override
    public int hashCode()
    {
        if ( isNode() ) return arg.hashCode() ;
        return argList.hashCode() ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof PropFuncArg ) ) return false ;
        PropFuncArg pfArg = (PropFuncArg)other ;
        if ( isNode() )
            return arg.equals(pfArg.arg) ;
        return argList.equals(pfArg.argList) ;
        
    }
    
    public boolean isList()             { return argList != null  ; }
    public boolean isNode()             { return arg != null  ; }
    
    public ExprList asExprList(PropFuncArg pfArg)
    {
        ExprList exprList = new ExprList() ;
        if ( pfArg.isNode() )
        {
            Node n = pfArg.getArg() ;
            Expr expr = ExprUtils.nodeToExpr(n) ;
            exprList.add(expr) ;
            return exprList ;
        }

        for ( Node n : pfArg.getArgList() )
        {
            Expr expr = ExprUtils.nodeToExpr( n );
            exprList.add( expr );
        }
        return exprList ;
    }

    
    @Override
    public void output(final IndentedWriter out, final SerializationContext sCxt)
    {
        if ( argList == null && arg == null )
            out.print("<<null>>") ;
        if ( argList != null )
        {
            out.print("(") ;
            boolean first = true ;
            for ( Node n : argList )
            {
                if ( ! first ) out.print(" ") ;
                String str = FmtUtils.stringForNode(n, sCxt) ;
                out.print(str) ;
                first = false ;
            }
            out.print(")") ;
        }
        if ( arg != null )
            out.print(FmtUtils.stringForNode(arg)) ;
    }
}
