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

package com.hp.hpl.jena.rdf.arp;

import java.util.Arrays;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.arp.impl.ARPSaxErrorHandler;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

final class JenaHandler extends ARPSaxErrorHandler implements StatementHandler,
        NamespaceHandler
    {
    static private final int BULK_UPDATE_SIZE = 1000;

    private final BulkUpdateHandler bulk;

    private final PrefixMapping prefixMapping;

    protected final Triple triples[];

    protected int here = 0;

    public JenaHandler( Model m, RDFErrorHandler e )
        { this( m.getGraph(), e ); }

    public JenaHandler( Graph g, Model m, RDFErrorHandler e )
        { this( g, modelToPrefixMapping( m ), e ); }
    
    private JenaHandler( Graph graph, RDFErrorHandler e )
        { this( graph, graph.getPrefixMapping(), e ); }

    private JenaHandler( Graph graph, PrefixMapping prefixMapping, RDFErrorHandler errorHandler )
        {
        super( errorHandler );
        this.bulk = graph.getBulkUpdateHandler();
        this.triples = new Triple[BULK_UPDATE_SIZE];
        this.prefixMapping = prefixMapping; 
        }
    
    private static PrefixMapping modelToPrefixMapping( Model model )
        {
        return model == null 
            ? PrefixMapping.Factory.create() 
            : model.getGraph().getPrefixMapping()
            ;
        }

    public void useWith( ARPHandlers h )
        {
        h.setStatementHandler( this );
        h.setErrorHandler( this );
        h.setNamespaceHandler( this );
        }

    @Override
    public void statement( AResource subj, AResource pred, AResource obj )
        {
        try
            { triples[here++] = JenaReader.convert( subj, pred, obj ); }
        catch (JenaException e)
            { errorHandler.error( e ); }
        if (here == BULK_UPDATE_SIZE) bulkUpdate();
        }

    @Override
    public void statement( AResource subj, AResource pred, ALiteral lit )
        {
        try
            { triples[here++] = JenaReader.convert( subj, pred, lit ); }
        catch (JenaException e)
            { errorHandler.error( e ); }
        if (here == BULK_UPDATE_SIZE) bulkUpdate();
        }

    public void bulkUpdate()
        {
        try
            {
            if (here == BULK_UPDATE_SIZE) bulk.add( triples );
            else bulk.add( Arrays.asList( triples ).subList( 0, here ) );
            }
        catch (JenaException e)
            { errorHandler.error( e ); }
        finally
            { here = 0; }
        }

    @Override
    public void startPrefixMapping( String prefix, String uri )
        {
        if (PrefixMappingImpl.isNiceURI( uri )) prefixMapping.setNsPrefix( prefix, uri );
        }

    @Override
    public void endPrefixMapping( String prefix )
        {}
    }
