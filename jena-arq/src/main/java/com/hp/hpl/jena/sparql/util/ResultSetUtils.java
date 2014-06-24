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

package com.hp.hpl.jena.sparql.util;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem ;

public class ResultSetUtils
{
    /**
     * Extracts a List filled with the binding of selectElement variable for each
     * query solution as RDFNodes (Resources or Literals).
     * Exhausts the result set.  Create a rewindable one to use multiple times. 
     *   
     * @see com.hp.hpl.jena.query.ResultSetFactory   
     */
    public static List<RDFNode> resultSetToList(ResultSet rs, String selectElement)
    {
        // feature suggested by James Howison
        List<RDFNode> items = new ArrayList<>() ;
        while (rs.hasNext())
        {
            QuerySolution qs = rs.nextSolution() ;
            RDFNode n = qs.get(selectElement) ;
            items.add(n) ;
        }
        return items ;
    }
    
    /**
     * Extracts a List filled with the binding of selectElement variable for each
     * query solution, turned into a string (URIs or lexical forms).  
     * Exhausts the result set.  Create a rewindable one to use multiple times. 
     * @see com.hp.hpl.jena.query.ResultSetFactory
     */
    public static List<String> resultSetToStringList(ResultSet rs,
                                             String selectElement,
                                             String literalOrResource)
    {
        // feature suggested by James Howison
        List<String> items = new ArrayList<>() ;
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

    /**
     * Create an in-memory result set from an array of 
     * ResulSets. It is assumed that all the ResultSets 
     * from the array have the same variables.
     * 
     * @param sets the ResultSets to concatenate.
     */
    public static ResultSet union(ResultSet... sets) {
        return new ResultSetMem(sets);
    }
    
}
