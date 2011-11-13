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

import java.util.Iterator ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.vocabulary.ResultSetGraphVocab ;
import com.hp.hpl.jena.vocabulary.RDF ;


public class RDFOutput
{
    private boolean reportAllVars = false ;
    private boolean includeTypeProperties = false ;
    
    public RDFOutput() { }
    
    public boolean getAllVars() { return reportAllVars ; }
    /** Set whether all variables, not just selected ones, are recorded */ 
    public void setAllVars(boolean all) { reportAllVars = all ; } 
    
    /** Encode the result set as RDF.
     * @return Model       Model contains the results
     */

    public Model toModel(ResultSet resultSet)
    {
        Model m = GraphFactory.makeJenaDefaultModel() ;
        asRDF(m, resultSet) ;
        if ( m.getNsPrefixURI("rs") == null )
            m.setNsPrefix("rs", ResultSetGraphVocab.getURI() ) ;
        if ( m.getNsPrefixURI("rdf") == null )
            m.setNsPrefix("rdf", RDF.getURI() ) ;
        return m ;
    }
    
    /** Encode the result set as RDF in the model provided.
     *  
     * @param model     The place where to put the RDF.
     * @return Resource The resource for the result set.
     */ 

    public Resource asRDF(Model model, ResultSet resultSet)
    {
        Resource results = model.createResource() ;
        // This always goes in.
        results.addProperty(RDF.type, ResultSetGraphVocab.ResultSet) ;
        
        for (String vName : resultSet.getResultVars() )
            results.addProperty(ResultSetGraphVocab.resultVariable, vName) ;
        
        int count = 0 ;
        for ( ; resultSet.hasNext() ; )
        {
            count++ ;
            QuerySolution rBind = resultSet.nextSolution() ;
            Resource thisSolution = model.createResource() ;
            if ( includeTypeProperties )
                thisSolution.addProperty(RDF.type, ResultSetGraphVocab.ResultSolution) ;
            results.addProperty(ResultSetGraphVocab.solution, thisSolution) ;
            if ( false )
                results.addLiteral(ResultSetGraphVocab.index, count) ;

            Iterator<String> iter = getAllVars() ?
                                    rBind.varNames() :
                                    resultSet.getResultVars().iterator() ;
            
            for ( ; iter.hasNext() ; )
            {
                Resource thisBinding = model.createResource() ;
                String rVar = iter.next() ;
                RDFNode n = rBind.get(rVar) ;
                
                if ( n == null )
                    continue ;
                    
//                if ( ! explicitUndefinedTerm && n == null )
//                    continue ;
//                
//                if ( n == null )
//                {
//                    if ( !explicitUndefinedTerm )
//                        continue ;
//                    // This variable was not found in the results.
//                    // Encode the result set with an explicit "not defined" 
//                    n = ResultSetVocab.undefined ;
//                }
                if ( includeTypeProperties )
                    thisBinding.addProperty(RDF.type, ResultSetGraphVocab.ResultBinding) ;
                thisBinding.addProperty(ResultSetGraphVocab.variable, rVar) ;
                thisBinding.addProperty(ResultSetGraphVocab.value, n) ;
                thisSolution.addProperty(ResultSetGraphVocab.binding, thisBinding) ;
            }
        }
        results.addProperty(ResultSetGraphVocab.size, model.createTypedLiteral(count)) ;
        addPrefixes(model) ;
        return results ;
    }
    
    // Boolean results
    
    public Model toModel(boolean result)
    {
        Model m = GraphFactory.makeJenaDefaultModel() ;
        asRDF(m, result) ;
        addPrefixes(m) ;
        return m ;
    }

    private void addPrefixes(Model model)
    {
        if ( model.getNsPrefixURI("rs") == null )
            model.setNsPrefix("rs", ResultSetGraphVocab.getURI() ) ;
        if ( model.getNsPrefixURI("rdf") == null )
            model.setNsPrefix("rdf", RDF.getURI() ) ;
        if ( model.getNsPrefixURI("xsd") == null )
            model.setNsPrefix("xsd", XSDDatatype.XSD+"#") ;
    }
    
    public Resource asRDF(Model model, boolean result)
    {
        Resource results = model.createResource() ;
        results.addProperty(RDF.type, ResultSetGraphVocab.ResultSet) ;
        Literal lit = model.createTypedLiteral(result) ;
        results.addProperty(ResultSetGraphVocab.p_boolean, lit) ;
        return results ;
    }

}
