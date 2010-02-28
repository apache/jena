/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.vocabulary.ResultSetGraphVocab ;
import com.hp.hpl.jena.vocabulary.RDF ;


public class RDFOutput
{
    boolean reportAllVars = false ;
    
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
        results.addProperty(RDF.type, ResultSetGraphVocab.ResultSet) ;
        
        for (String vName : resultSet.getResultVars() )
            results.addProperty(ResultSetGraphVocab.resultVariable, vName) ;
        
        int count = 0 ;
        for ( ; resultSet.hasNext() ; )
        {
            count++ ;
            QuerySolution rBind = resultSet.nextSolution() ;
            Resource thisSolution = model.createResource() ;
            results.addProperty(ResultSetGraphVocab.solution, thisSolution) ;

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
                    
                thisBinding.addProperty(ResultSetGraphVocab.variable, rVar) ;
                thisBinding.addProperty(ResultSetGraphVocab.value, n) ;
                thisSolution.addProperty(ResultSetGraphVocab.binding, thisBinding) ;
            }
        }
        //results.addProperty(ResultSetVocab.size, count) ;
        return results ;
    }
    
    // Boolean results
    
    public Model toModel(boolean result)
    {
        Model m = GraphFactory.makeJenaDefaultModel() ;
        asRDF(m, result) ;
        if ( m.getNsPrefixURI("rs") == null )
            m.setNsPrefix("rs", ResultSetGraphVocab.getURI() ) ;
        if ( m.getNsPrefixURI("rdf") == null )
            m.setNsPrefix("rdf", RDF.getURI() ) ;
        if ( m.getNsPrefixURI("xsd") == null )
            m.setNsPrefix("xsd", XSDDatatype.XSD+"#") ;

        return m ;

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

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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