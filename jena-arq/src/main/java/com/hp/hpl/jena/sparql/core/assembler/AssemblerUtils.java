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

package com.hp.hpl.jena.sparql.core.assembler;

import org.apache.jena.riot.RDFDataMgr ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.ResourceFactory ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.MappingRegistry ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.sparql.util.TypeNotUniqueException ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;

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
        registerWith(Assembler.general) ;
    }
    
    static public void registerWith(AssemblerGroup g)
    {
        // Wire in the extension assemblers (extensions relative to the Jena assembler framework)
        g.implementWith(DatasetAssembler.getType(), new DatasetAssembler()) ;
        g.implementWith(GraphStoreAssembler.getType(), new GraphStoreAssembler()) ;
        g.implementWith(DatasetNullAssembler.getType(), new DatasetNullAssembler()) ;
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
            spec = RDFDataMgr.loadModel(assemblerFile) ;
        } catch (Exception ex)
        { throw new ARQException("Failed reading assembler description: "+ex.getMessage()) ; }

        Resource root = null ;
        try {
            root = GraphUtils.findRootByType(spec, type) ;
            if ( root == null )
                return null ;
            
        } catch (TypeNotUniqueException ex)
        { throw new ARQException("Multiple types for: "+DatasetAssemblerVocab.tDataset) ; }
        return Assembler.general.open(root) ;
    }
    
    /** Look for and set context declarations. 
     * e.g.
     * <pre>
     * root ... ;
     *   ja:context [ ja:cxtName "arq:queryTimeout" ;  ja:cxtValue "10000" ] ;
     *   ...
     * </pre>
     * Short name forms of context parameters can be used.  
     * Setting as string "undef" will remove the context setting.
     */
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
