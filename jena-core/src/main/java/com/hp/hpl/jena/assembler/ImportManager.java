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

package com.hp.hpl.jena.assembler;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.*;

public class ImportManager
    {
    public ImportManager()
        {}

    /**
        A shared instance of ImportManage, used as a default by several other
        assembler methods.
    */
    public final static ImportManager instance = new ImportManager();
    
    /**
        The cache of models already read by this manager.
    */
    protected Map<String, Graph> cache = new HashMap<>();
    
    /**
        Clear this ImportManager's cache.
    */
    public void clear()
        { cache.clear(); }
    
    /**
        Answer <code>model</code> if it has no imports, or a union model with
        <code>model</code> as its base and its imported models as the other
        components. The default file manager is used to load the models.
    */
    public Model withImports( Model model )
        { return withImports( FileManager.get(), model ); }

    /**
        Answer <code>model</code> if it has no imports, or a union model with
        <code>model</code> as its base and its imported models as the other
        components. The file manager <code>fm</code> is used to load the
        imported models.
    */
    public Model withImports( FileManager fm, Model model )
        { return withImports( fm, model, new HashSet<String>() ); }

    private Model withImports( FileManager fm, Model model, Set<String> loading )
        {
        StmtIterator oit = model.listStatements( null, OWL.imports, (RDFNode) null );
        StmtIterator jit = model.listStatements( null, JA.imports, (RDFNode) null );
        if (oit.hasNext() || jit.hasNext())
            {
            MultiUnion g = new MultiUnion( new Graph[] { model.getGraph() } );
            addImportedGraphs( fm, loading, oit, g );
            addImportedGraphs( fm, loading, jit, g );
            return ModelFactory.createModelForGraph( g );
            }
        else
            return model;
        }

    private void addImportedGraphs( FileManager fm, Set<String> loading, StmtIterator oit, MultiUnion g )
        {
        while (oit.hasNext()) 
            {
            String path = getObjectURI( oit.nextStatement() );
            if (loading.add( path )) g.addGraph( graphFor( fm, loading, path ) );
            }
        }
    
    private String getObjectURI( Statement s )
        {
        RDFNode ob = s.getObject();
        if (ob.isLiteral()) return AssemblerHelp.getString( s );
        if (ob.isAnon()) throw new BadObjectException( s );
        return ((Resource) ob).getURI();
        }

    protected Graph graphFor( FileManager fm, Set<String> loading, String path )
        {
        Graph already = cache.get( path );
        if (already == null)
            {
            Graph result = withImports( fm, fm.loadModel( path ), loading ).getGraph();
            cache.put( path, result );
            return result;
            }
        else
            return already;
        }
    }
