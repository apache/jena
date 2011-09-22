/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;

/**
 * The class "ResultSet" is reserved for the SELECT result format.
 * This class can hold a ResultSet, a boolean or a Model.
 */

public class SPARQLResult
{
    private boolean hasBeenSet = false ;
    
    private ResultSet resultSet = null ;
    private Boolean booleanResult = null ;
    private Model model = null ;
    
    // Delayed choice of result type.
    protected SPARQLResult() {}
    
    public SPARQLResult(Model model)            { set(model) ; }
    public SPARQLResult(ResultSet resultSet)    { set(resultSet) ;}
    public SPARQLResult(boolean booleanResult)  { set(booleanResult) ; }
    
    public boolean isResultSet()
    {
        if ( ! hasBeenSet )
            throw new ResultSetException("Not set") ;
        return resultSet != null ;
    }
    
    /** Synonym for isGraph */
    public boolean isModel() { return isGraph() ; }

    public boolean isGraph()
    {
        if ( ! hasBeenSet )
            throw new ResultSetException("Not set") ;
        return model != null ;
    }

    public boolean isBoolean()
    {
        if ( ! hasBeenSet )
            throw new ResultSetException("Not set") ;
        return booleanResult != null ;
    }

    
    public ResultSet getResultSet()
    {
        if ( ! hasBeenSet )
            throw new ResultSetException("Not set") ;
        if ( ! isResultSet() )
            throw new ResultSetException("Not an ResultSet result") ;
        return resultSet ;
    }

    public boolean getBooleanResult()
    {
        if ( ! hasBeenSet )
            throw new ResultSetException("Not set") ;
        if ( ! isBoolean() )
            throw new ResultSetException("Not a boolean result") ;
        return booleanResult.booleanValue() ;
    }

    public Model getModel() { 
        if ( ! hasBeenSet )
            throw new ResultSetException("Not set") ;
        if ( ! isModel() )
            throw new ResultSetException("Not a graph result") ;
        return model ;
    }
    
    public boolean isHasBeenSet() { return hasBeenSet; }
    
    protected void set(ResultSet rs)
    { 
        resultSet = rs ;
        hasBeenSet = true ;
    }

    protected void set(Model m)
    { model = m ; hasBeenSet = true ; }
    
    protected void set(boolean r)
    { set (new Boolean(r)) ; } 
    
    protected void set(Boolean r)
    { booleanResult  = r ;  hasBeenSet = true ; }
    
    static protected void addBinding(BindingMap binding, Var var, Node value)
    {
        Node n = binding.get(var) ;
        if ( n != null )
        {
            // Same - silently skip.
            if ( n.equals(value) )
                return ;
            Log.warn(SPARQLResult.class, 
                     String.format("Multiple occurences of a binding for variable '%s' with different values - ignored", var.getName())) ;
            return ;
        }
        binding.add(var, value) ;
    }
    
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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