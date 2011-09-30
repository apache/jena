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

package com.hp.hpl.jena.sparql.core.assembler;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.QuerySolutionMap ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.ResourceFactory ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.mgt.Explain.InfoLevel ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.MappingRegistry ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.sparql.util.TypeNotUniqueException ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;
import com.hp.hpl.jena.util.FileManager ;

public class AssemblerUtils
{
    // Wrappers for reading things form a file - assumes one of the thing per file. 
    public static PrefixMapping readPrefixMapping(String file)
    {
        PrefixMapping pm = (PrefixMapping)AssemblerUtils.build(file, JA.PrefixMapping) ;
        return pm ;
    }
    
    private static boolean initialized = false ; 
    
    static { ARQ.init(); init() ; } 
    
    static public void init()
    {
        if ( initialized )
            return ;
        initialized = true ;
        // Wire in the extension assemblers (extensions relative to the Jena assembler framework)
        register(Assembler.general) ;
    }
    
    static public void register(AssemblerGroup g)
    {
        // Wire in the extension assemblers (extensions relative to the Jena assembler framework)
        g.implementWith(DataSourceAssembler.getType(), new DataSourceAssembler()) ;
        g.implementWith(GraphStoreAssembler.getType(), new GraphStoreAssembler()) ;
    }
    
    private static void assemblerClass(AssemblerGroup g, Resource r, Assembler a)
    {
        g.implementWith(r, a) ;
    }
    
    public static Object build(String assemblerFile, String typeURI)
    {
        Resource type = ResourceFactory.createResource(typeURI) ;
        return build(assemblerFile, type) ; 
    }
    
    public static Object build(String assemblerFile, Resource type)
    {
        if ( assemblerFile == null )
            throw new ARQException("No assembler file") ;
        Model spec = null ;
        try {
            spec = FileManager.get().loadModel(assemblerFile) ;
        } catch (Exception ex)
        { throw new ARQException("Failed reading assembler description: "+ex.getMessage()) ; }

        Resource root = null ;
        InfoLevel level = ARQ.getExecutionLogging() ;
        ARQ.setExecutionLogging(InfoLevel.NONE) ;
        try {
            root = GraphUtils.findRootByType(spec, type) ;
            if ( root == null )
                return null ;
            
        } catch (TypeNotUniqueException ex)
        { throw new ARQException("Multiple types for: "+DatasetAssemblerVocab.tDataset) ; }
        finally
        { ARQ.setExecutionLogging(level) ; }
        return Assembler.general.open(root) ;
    }
    
    public static void setContext(Resource r, Context context)
    {
        String qs = "PREFIX ja: <"+JA.getURI()+">\nSELECT * { ?x ja:context [ ja:cxtName ?name ; ja:cxtValue ?value ] }" ;
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        qsm.add("x", r) ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, r.getModel(), qsm) ;
        ResultSet rs = qExec.execSelect() ;
        while ( rs.hasNext() )
        {
            QuerySolution soln = rs.next() ;
            String name = soln.getLiteral("name").getLexicalForm() ;
            String value = soln.getLiteral("value").getLexicalForm() ;  // Works for numbers as well!
            name = MappingRegistry.mapPrefixName(name) ;
            Symbol symbol = Symbol.create(name) ;
            if ( "undef".equalsIgnoreCase(value) )
                context.remove(symbol) ;
            else
                context.set(symbol, value) ;
        }
    }
}
