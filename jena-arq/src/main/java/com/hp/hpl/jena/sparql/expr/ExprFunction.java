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

package com.hp.hpl.jena.sparql.expr;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.serializer.SerializationContext ;

/** A function in the expression hierarchy.
 *  Everything that is evaluable (i.e. not NodeValue, NodeVar) is a function).
 *  It is useful to distinguish between values, vars and functions.
 *  The exists operations (which take a op, not arguments) are functions of zero arguments.
 */
 
public abstract class ExprFunction extends ExprNode
{
    protected FunctionLabel funcSymbol ;
    protected String opSign ;
    private List<Expr> argList = null ;
    
    protected ExprFunction(String fName)
    {
        funcSymbol = new FunctionLabel(fName) ;
        opSign = null ;
    }

    protected ExprFunction(String fName, String opSign)
    {
        this(fName) ;
        this.opSign = opSign ;
    }

    public abstract Expr getArg(int i) ;
    public abstract int numArgs() ;

    // ExprFunctionN overrides this.
    public List<Expr> getArgs()
    {
        if ( argList != null )
            return argList ; 
        argList = new ArrayList<>() ;
        for ( int i = 1 ; i <= numArgs() ; i++ )
            argList.add(this.getArg(i)) ;
        return argList ;        
    }

    @Override
    public boolean isFunction()        { return true ; }
    @Override
    public ExprFunction getFunction()  { return this ; }
    
    @Override
    public int hashCode()
    {
        return funcSymbol.hashCode() ^ numArgs() ;
    }
    
    // A function is equal if:
    // + The name is the same
    // + The arguments are the same (including arity).
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        if ( ! other.getClass().equals(this.getClass()) )
            return false ;
        
        ExprFunction ex = (ExprFunction)other ;
        
        if ( ! funcSymbol.equals(ex.funcSymbol) )
            return false ;
        
        if ( numArgs() != ex.numArgs() )
            return false ;
        
        // Arguments are 1, 2, 3, ...
        for ( int i = 1 ; i <= numArgs() ; i++ )
        {
            Expr a1 = this.getArg(i) ;
            Expr a2 = ex.getArg(i) ;
            if ( ! Lib.equal(a1, a2) )
                return false ;
        }
        return true ;
    }
    
    /** Name used for output in SPARQL format needing functional form (no specific keyword).
     *  e.g. regexp(), custom functions, ...
     */
    
    public String getFunctionPrintName(SerializationContext cxt)
    { return funcSymbol.getSymbol() ; }

    /** Name used in a functional form (i.e. SPARQL algebra).
     *  getOpName() is used in preference as a short, symbol name.
     */
    public String getFunctionName(SerializationContext cxt)
    { return funcSymbol.getSymbol() ; }
    
    /** Used to get a unique name for the function, which is intern'ed.  Used in hashCode() */ 
    public FunctionLabel getFunctionSymbol()
    { return funcSymbol ; }

    /** URI for this function, whether custom or specification defined URI (these are keywords in the language) */  
    public String getFunctionIRI() { return null ; }

    /** Get the symbol name (+, ! etc) for this function -- maybe null for none */
    public String getOpName()
    { return opSign ; }
}
