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

package com.hp.hpl.jena.sparql.resultset;

import org.apache.jena.atlas.logging.Log ;

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
            throw new ResultSetException("Not a ResultSet result") ;
        return resultSet ;
    }

    public boolean getBooleanResult()
    {
        if ( ! hasBeenSet )
            throw new ResultSetException("Not set") ;
        if ( ! isBoolean() )
            throw new ResultSetException("Not a boolean result") ;
        return booleanResult;
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
