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

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Enumeration ;
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
import com.hp.hpl.jena.util.FileManager ;

// UNFINISHED

public class SPARQL_QueryGeneral extends SPARQL_Query
{
    final FileManager fileManager ;
    final int MaxTriples = 100*1000 ; 
    
    public SPARQL_QueryGeneral(boolean verbose)
    { 
        super(verbose) ;
        fileManager = new FileManager() ;
        // Only know how to handle http URLs 
        fileManager.addLocatorURL() ;
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
    protected boolean requestNoQueryString(HttpServletRequest request, HttpServletResponse response)
    {
        errorBadRequest("No query string given") ;
        return false ;
    }
    
    @Override
    protected Dataset decideDataset(HttpActionQuery action, Query query, String queryStringLog) 
    {
        // Dataset comes from:
        // default-graph-uri/named-graph-uri
        // FROM/FROM NAMED.
        
        errorNotImplemented("General SPARQL query with dataset description") ;
        return null ;
    }
    
    private boolean datasetInProtocol(HttpServletRequest request)
    {
        String d = request.getHeader(paramDefaultGraphURI) ;
        if ( d != null && !d.equals("") )
            return true ;
        
        List<String> n = toStrList(request.getHeaders(paramNamedGraphURI)) ;
        if ( n != null && n.size() > 0 )
            return true ;
        return false ;
    }
    
    protected Dataset datasetFromProtocol(HttpServletRequest request)
    {
        try {
            
            List<String> graphURLs = toStrList(request.getHeaders(paramDefaultGraphURI)) ;
            List<String> namedGraphs = toStrList(request.getHeaders(paramNamedGraphURI)) ;
            
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
                        Model model2 = fileManager.loadModel(uri) ;
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

    private List<String> toStrList(Enumeration<?> enumeration)
    {
        List<String> x = new ArrayList<String>() ;
        for ( ; enumeration.hasMoreElements() ; )
        {
            String str = (String)enumeration.nextElement() ;
            x.add(str) ;
        }
        return x ;
    }

    private  <T>  List<T> removeEmptyValues(List<T> list)
    {
        return Iter.iter(list).removeNulls().toList() ;
    }
    
}
