/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
            context.set(symbol, value) ;
        }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */