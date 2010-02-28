/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.vocabulary.ResultSetGraphVocab ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class ResultSetUtils
{
    public static boolean equals(ResultSet rs1, ResultSet rs2)
    {
        if ( rs1 == rs2 ) return true ;
        Model model2 = resultSetToModel(rs2) ;
        return equals(rs1, model2) ;
    }

    static private boolean equals(ResultSet rs1, Model model2)
    {
        Model model1 = resultSetToModel(rs1) ;
        return model1.isIsomorphicWith(model2) ;
    }

    private static Model resultSetToModel(ResultSet rs)
    {
        Model m = GraphFactory.makeDefaultModel() ;
        ResultSetFormatter.asRDF(m, rs) ;
        if ( m.getNsPrefixURI("rs") == null )
            m.setNsPrefix("rs", ResultSetGraphVocab.getURI() ) ;
        if ( m.getNsPrefixURI("rdf") == null )
            m.setNsPrefix("rdf", RDF.getURI() ) ;
        if ( m.getNsPrefixURI("xsd") == null )
            m.setNsPrefix("xsd", XSDDatatype.XSD+"#") ;
        return m ;
    }
    
    /**
     * Extracts a List filled with the binding of selectElement variable for each
     * query solution as RDFNodes (Resources or Literals).
     * Exhausts the result set.  Create a rewindable one to use multiple times. 
     * @see{com.hp.hpl.jena.query.ResultSetFactory}   
     * Suggested by James Howison  
     */
    public static List<RDFNode> resultSetToList(ResultSet rs,
                                       String selectElement)
    {
        List<RDFNode> items = new ArrayList<RDFNode>() ;
        while (rs.hasNext())
        {
            QuerySolution qs = rs.nextSolution() ;
            RDFNode n = qs.get(selectElement) ;
            items.add(n) ;
        }
        return items ;
    }
    
    /*Suggested by James Howison. */
    /**
     * Extracts a List filled with the binding of selectElement variable for each
     * query solution, turned into a string (URIs or lexical forms).  
     * Exhausts the result set.  Create a rewindable one to use multiple times. 
     * @see{com.hp.hpl.jena.query.ResultSetFactory}   
     *   
     */
    public static List<String> resultSetToStringList(ResultSet rs,
                                             String selectElement,
                                             String literalOrResource)
    {
        List<String> items = new ArrayList<String>() ;
        while (rs.hasNext())
        {
            QuerySolution qs = rs.nextSolution() ;
            RDFNode rn = qs.get(selectElement) ;
            if ( rn.isLiteral() )
                items.add( ((Literal)rn).getLexicalForm() ) ;
            else if ( rn.isURIResource() )
                items.add( ((Resource)rn).getURI() ) ;
            else if ( rn.isAnon() )
            {
                items.add( ((Resource)rn).getId().getLabelString() ) ;
            }
            else 
                throw new ARQException("Unknow thing in results : "+rn) ;
        }
        return items ;
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