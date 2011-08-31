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

package org.openjena.fuseki.servlets;

import static org.openjena.fuseki.Fuseki.webFileManager ;
import static org.openjena.fuseki.HttpNames.paramAccept ;
import static org.openjena.fuseki.HttpNames.paramCallback ;
import static org.openjena.fuseki.HttpNames.paramDefaultGraphURI ;
import static org.openjena.fuseki.HttpNames.paramForceAccept ;
import static org.openjena.fuseki.HttpNames.paramNamedGraphURI ;
import static org.openjena.fuseki.HttpNames.paramOutput1 ;
import static org.openjena.fuseki.HttpNames.paramOutput2 ;
import static org.openjena.fuseki.HttpNames.paramQuery ;
import static org.openjena.fuseki.HttpNames.paramQueryRef ;
import static org.openjena.fuseki.HttpNames.paramStyleSheet ;

import java.util.Arrays ;
import java.util.Collections ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.fuseki.migrate.GraphLoadUtils ;

import com.hp.hpl.jena.query.DataSource ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class SPARQL_QueryGeneral extends SPARQL_Query
{
    final int MaxTriples = 100*1000 ; 
    
    public SPARQL_QueryGeneral(boolean verbose)
    { 
        super(verbose) ;
    }

    public SPARQL_QueryGeneral()
    { this(false) ; }

    // All the params we support
    private static String[] params_ = { paramQuery, 
                                        paramDefaultGraphURI, paramNamedGraphURI,
                                        paramQueryRef,
                                        paramStyleSheet,
                                        paramAccept,
                                        paramOutput1, paramOutput2, 
                                        paramCallback, 
                                        paramForceAccept } ;
    private static Set<String> params = new HashSet<String>(Arrays.asList(params_)) ;
    
    @Override
    protected void validate(HttpServletRequest request)
    {
        validate(request, params) ;
    }

    @Override
    protected void validateQuery(HttpActionQuery action, Query query)
    {
        if ( query.hasDatasetDescription() )
            errorBadRequest("Query may not include a dataset description (FROM/FROM NAMED)") ;
    }
    
    @Override
    protected String mapRequestToDataset(String uri)
    { return null ; }
    
    @Override
    protected Dataset decideDataset(HttpActionQuery action, Query query, String queryStringLog) 
    {
        Dataset ds = datasetFromProtocol(action.request) ;
        if ( ds == null )
            ds = datasetFromQuery(query) ;
        if ( ds == null )
            errorBadRequest("No dataset description in protocol request or in the query string") ;
        return ds ;
    }
    
    @Override
    protected boolean requestNoQueryString(HttpServletRequest request, HttpServletResponse response)
    {
        errorBadRequest("No query string given") ;
        return false ;
    }

    private boolean datasetInProtocol(HttpServletRequest request)
    {
        String d = request.getParameter(paramDefaultGraphURI) ;
        if ( d != null && !d.equals("") )
            return true ;
        
        List<String> n = toStrList(request.getParameterValues(paramNamedGraphURI)) ;
        if ( n != null && n.size() > 0 )
            return true ;
        return false ;
    }
    
    protected Dataset datasetFromProtocol(HttpServletRequest request)
    {
        List<String> graphURLs = toStrList(request.getParameterValues(paramDefaultGraphURI)) ;
        List<String> namedGraphs = toStrList(request.getParameterValues(paramNamedGraphURI)) ;
        return datasetFromDescription(graphURLs, namedGraphs) ;
    }
    
    protected Dataset datasetFromQuery(Query query)
    {
        List<String> graphURLs = query.getGraphURIs() ;
        List<String> namedGraphs = query.getNamedGraphURIs() ;
        return datasetFromDescription(graphURLs, namedGraphs) ;
    }
    
    protected Dataset datasetFromDescription(List<String> graphURLs, List<String> namedGraphs)
    {
        try {
            graphURLs = removeEmptyValues(graphURLs) ;
            namedGraphs = removeEmptyValues(namedGraphs) ;
            
            if ( graphURLs.size() == 0 && namedGraphs.size() == 0 )
                return null ;
            
            DataSource dataset = DatasetFactory.create() ;
            // Look in cache for loaded graphs!!

            // ---- Default graph
            {
                Model model = ModelFactory.createDefaultModel() ;
                for ( String uri : graphURLs )
                {
                    if ( uri == null )
                    {
                        // TODO LOG
                        log.warn("Null "+paramDefaultGraphURI+ " (ignored)") ;
                        continue ;
                    }
                    if ( uri.equals("") )
                    {
                        // TODO LOG
                        log.warn("Empty "+paramDefaultGraphURI+ " (ignored)") ;
                        continue ;
                    }

                    try {
                        //TODO Clearup - RIOT integration.
                        GraphLoadUtils.loadModel(model, uri, MaxTriples) ;
                        log.info("Load (default) "+uri) ;
                    } catch (Exception ex)
                    {
                        // TODO LOG
                        log.info("Failed to load (default) "+uri+" : "+ex.getMessage()) ;
                        errorBadRequest("Failed to load URL "+uri) ;
                    }
                }
                dataset.setDefaultModel(model) ;
            }
            // ---- Named graphs
            if ( namedGraphs != null )
            {
                for ( String uri : namedGraphs )
                {
                    if ( uri == null )
                    {
                        log.warn("Null "+paramNamedGraphURI+ " (ignored)") ;
                        continue ;
                    }
                    if ( uri.equals("") )
                    {
                        log.warn("Empty "+paramNamedGraphURI+ " (ignored)") ;
                        continue ;
                    }
                    try {
                        Model model2 = webFileManager.loadModel(uri) ;
                        log.info("Load (named) "+uri) ;
                        dataset.addNamedModel(uri, model2) ;
                    } catch (Exception ex)
                    {
                        // TODO LOG
                        log.info("Failed to load (named) "+uri+" : "+ex.getMessage()) ;
                        errorBadRequest("Failed to load (named) "+uri+" : "+ex.getMessage()) ;
                    }
                }
            }
            
            return dataset ;
            
        } 
        catch (Exception ex)
        {
            // TODO LOG
            log.info("SPARQL parameter error",ex) ;
            errorBadRequest("Parameter error");
            return null ;
        }
        
    }

    private List<String> toStrList(String[] array)
    {
        if ( array == null )
            return Collections.emptyList() ;
        return Arrays.asList(array) ;
    }

    private  <T>  List<T> removeEmptyValues(List<T> list)
    {
        return Iter.iter(list).removeNulls().toList() ;
    }
    
}
