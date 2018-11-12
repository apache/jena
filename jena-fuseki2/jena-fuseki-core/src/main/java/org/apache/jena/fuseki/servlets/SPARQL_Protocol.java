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

package org.apache.jena.fuseki.servlets ;

import static org.apache.jena.riot.web.HttpNames.paramDefaultGraphURI ;
import static org.apache.jena.riot.web.HttpNames.paramNamedGraphURI ;

import java.util.Arrays ;
import java.util.Collections ;
import java.util.List ;
import java.util.function.Predicate ;

import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryException ;
import org.apache.jena.sparql.core.DatasetDescription ;

/**
 * Support for the SPARQL protocol (SPARQL Query, SPARQL Update)
 * <p>
 * When data-level access control is in use, the ActionService's are
 * {@code AccessCtl_SPARQL_QueryDataset} etc.
 */
public abstract class SPARQL_Protocol extends ActionService {

    protected SPARQL_Protocol() {
        super() ;
    }

    protected static String messageForQueryException(QueryException ex) {
        if ( ex.getMessage() != null )
            return ex.getMessage() ;
        if ( ex.getCause() != null )
            return Lib.classShortName(ex.getCause().getClass()) ;
        return null ;
    }

    protected static DatasetDescription getProtocolDatasetDescription(HttpAction action) {
        List<String> graphURLs = toStrList(action.request.getParameterValues(paramDefaultGraphURI)) ;
        List<String> namedGraphs = toStrList(action.request.getParameterValues(paramNamedGraphURI)) ;

        graphURLs = removeEmptyValues(graphURLs) ;
        namedGraphs = removeEmptyValues(namedGraphs) ;

        if ( graphURLs.size() == 0 && namedGraphs.size() == 0 )
            return null ;
        return DatasetDescription.create(graphURLs, namedGraphs) ;
    }

    protected static DatasetDescription getQueryDatasetDescription(Query query) {
        return DatasetDescription.create(query) ;
    }

    /** Given an action (protocol request) and a query, decide the DatasetDescription, if any.
     *   
     * @param action Action details - may be null.
     * @param query  The query - may be null.
     * @return DatasetDescription or null
     */
    protected static DatasetDescription getDatasetDescription(HttpAction action, Query query) {
        // Protocol overrides query,
        DatasetDescription dsDesc = null ;
        if ( action != null ) {
            dsDesc = getProtocolDatasetDescription(action) ;
            if (dsDesc != null ) 
                return dsDesc ;
        }
        if ( query != null )
            dsDesc = getQueryDatasetDescription(query) ;
        return dsDesc ;
    }
    
    private static List<String> toStrList(String[] array) {
        if ( array == null )
            return Collections.emptyList() ;
        return Arrays.asList(array) ;
    }

    private static List<String> removeEmptyValues(List<String> list) {
        return Iter.iter(list).filter(acceptNonEmpty).toList() ;
    }

    private static Predicate<String> acceptNonEmpty = item -> item != null && !item.isEmpty() ;

    protected static int countParamOccurences(HttpServletRequest request, String param) {
        String[] x = request.getParameterValues(param) ;
        if ( x == null )
            return 0 ;
        return x.length ;
    }

}
