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

import static java.lang.String.format ;
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

import org.openjena.atlas.iterator.Filter ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.InternalErrorException ;
import org.openjena.fuseki.migrate.GraphLoadUtils ;
import org.openjena.riot.RiotException ;

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
    { }
    
    @Override
    protected String mapRequestToDataset(String uri)
    { return null ; }
    
    @Override
    protected Dataset decideDataset(HttpActionQuery action, Query query, String queryStringLog) 
    {
        Dataset ds = datasetFromProtocol(action) ;
        if ( ds == null )
            ds = datasetFromQuery(action, query) ;
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
    
    protected Dataset datasetFromProtocol(HttpActionQuery action)
    {
        List<String> graphURLs = toStrList(action.request.getParameterValues(paramDefaultGraphURI)) ;
        List<String> namedGraphs = toStrList(action.request.getParameterValues(paramNamedGraphURI)) ;
        return datasetFromDescription(action, graphURLs, namedGraphs) ;
    }
    
    protected Dataset datasetFromQuery(HttpActionQuery action, Query query)
    {
        List<String> graphURLs = query.getGraphURIs() ;
        List<String> namedGraphs = query.getNamedGraphURIs() ;
        return datasetFromDescription(action, graphURLs, namedGraphs) ;
    }
    
    protected Dataset datasetFromDescription(HttpActionQuery action, List<String> graphURLs, List<String> namedGraphs)
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
                    if ( uri == null || uri.equals("") )
                        throw new InternalErrorException("Default graph URI is null or the empty string")  ;

                    try {
                        //TODO Clearup - RIOT integration.
                        GraphLoadUtils.loadModel(model, uri, MaxTriples) ;
                        log.info(format("[%d] Load (default graph) %s", action.id, uri)) ;
                    } catch (RiotException ex) {
                        log.info(format("[%d] Parsing error loading %s: %s", action.id, uri, ex.getMessage())) ;
                        errorBadRequest("Failed to load URL (parse error) "+uri+" : "+ex.getMessage()) ;
                    } catch (Exception ex)
                    {
                        log.info(format("[%d] Failed to load (default) %s: %s", action.id, uri, ex.getMessage())) ;
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
                    if ( uri == null || uri.equals("") )
                        throw new InternalErrorException("Named graph URI is null or the empty string")  ;

                    try {
                        Model model = ModelFactory.createDefaultModel() ;
                        GraphLoadUtils.loadModel(model, uri, MaxTriples) ;
                        log.info(format("[%d] Load (named graph) %s", action.id, uri)) ;
                        dataset.addNamedModel(uri, model) ;
                    } catch (RiotException ex) {
                        log.info(format("[%d] Parsing error loading %s: %s", action.id, uri, ex.getMessage())) ;
                        errorBadRequest("Failed to load URL (parse error) "+uri+" : "+ex.getMessage()) ;
                    } catch (Exception ex)
                    {
                        log.info(format("[%d] Failed to load (named graph) %s: %s", action.id, uri, ex.getMessage())) ;
                        errorBadRequest("Failed to load URL "+uri) ;
                    }
                }
            }
            
            return dataset ;
            
        } 
        catch (ActionErrorException ex) { throw ex ; }
        catch (Exception ex)
        {
            log.info(format("[%d] SPARQL parameter error: "+ex.getMessage(),action.id, ex)) ;
            errorBadRequest("Parameter error: "+ex.getMessage());
            return null ;
        }
        
    }

    private List<String> toStrList(String[] array)
    {
        if ( array == null )
            return Collections.emptyList() ;
        return Arrays.asList(array) ;
    }

    private  List<String> removeEmptyValues(List<String> list)
    {
        return Iter.iter(list).filter(acceptNonEmpty).toList() ;
    }
    
    private static Filter<String> acceptNonEmpty = new Filter<String>(){ 
        @Override
        public boolean accept(String item)
        {
            return item != null && item.length() != 0 ;
        }
    } ;
}
