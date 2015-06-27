/**
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

package org.apache.jena.fuseki.servlets;

import static org.apache.jena.fuseki.HttpNames.paramDefaultGraphURI ;
import static org.apache.jena.fuseki.HttpNames.paramNamedGraphURI ;

import java.util.Arrays ;
import java.util.Collections ;
import java.util.List ;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryParseException ;
import org.apache.jena.sparql.core.DatasetDescription ;

/** Support for the SPARQL protocol (SPARQL Query, SPARQL Update)
 */
public  abstract class SPARQL_Protocol extends SPARQL_ServletBase
{
    protected SPARQL_Protocol() { super() ; }

    protected static String messageForQPE(QueryParseException ex)
    {
        if ( ex.getMessage() != null )
            return ex.getMessage() ;
        if ( ex.getCause() != null )
            return Lib.classShortName(ex.getCause().getClass()) ;
        return null ;
    }
    
    protected static DatasetDescription getDatasetDescription(HttpAction action)
    {
        List<String> graphURLs = toStrList(action.request.getParameterValues(paramDefaultGraphURI)) ;
        List<String> namedGraphs = toStrList(action.request.getParameterValues(paramNamedGraphURI)) ;
        
        graphURLs = removeEmptyValues(graphURLs) ;
        namedGraphs = removeEmptyValues(namedGraphs) ;
        
        if ( graphURLs.size() == 0 && namedGraphs.size() == 0 )
            return null ;
        return DatasetDescription.create(graphURLs, namedGraphs) ;
    }
    
    protected static DatasetDescription getDatasetDescription(Query query)
    {
        return DatasetDescription.create(query) ;
    }
   
    private static List<String> toStrList(String[] array)
    {
        if ( array == null )
            return Collections.emptyList() ;
        return Arrays.asList(array) ;
    }

    private static List<String> removeEmptyValues(List<String> list)
    {
        return Iter.iter(list).filter(Objects::nonNull).filter(item -> !item.isEmpty()).toList() ;
    }
    
    protected static int countParamOccurences(HttpServletRequest request, String param)
    {
        String[] x = request.getParameterValues(param) ;
        if ( x == null )
            return 0 ;
        return x.length ;
    }
    

}

