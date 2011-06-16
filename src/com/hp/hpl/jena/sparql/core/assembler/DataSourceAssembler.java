/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core.assembler;

import java.lang.reflect.Method;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.query.DataSource ;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;

public class DataSourceAssembler extends AssemblerBase implements Assembler
{
    public static Resource getType() { return DatasetAssemblerVocab.tDataset ; }
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        DataSource ds = createDataSource(a, root, mode) ;
        createTextIndex(ds, root) ;

        return ds ;
    }
    
    public DataSource createDataSource(Assembler a, Resource root, Mode mode) 
    {
        // Non-expanding version.
        //DataSource ds = DatasetFactory.create() ;
        
        // Expanding version.
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        DataSource ds = DatasetFactory.create(dsg) ;

        // -------- Default graph
        // Can use ja:graph or ja:defaultGraph
        Resource dftGraph = GraphUtils.getResourceValue(root, DatasetAssemblerVocab.pDefaultGraph) ;
        if ( dftGraph == null )
            dftGraph = GraphUtils.getResourceValue(root, DatasetAssemblerVocab.pGraph) ;
        
        Model dftModel = null ;
        if ( dftGraph != null )
            dftModel = a.openModel(dftGraph) ;
        else
            // Assembler description did not define one - make a dummy.
            dftModel = GraphFactory.makePlainModel() ;

        ds.setDefaultModel(dftModel) ;

        // -------- Named graphs
        List<RDFNode> nodes = GraphUtils.multiValue(root, DatasetAssemblerVocab.pNamedGraph) ; 
        
        for ( Iterator<RDFNode> iter= nodes.iterator() ; iter.hasNext() ; )
        {
            RDFNode n = iter.next();
            if ( ! ( n instanceof Resource ) )
                throw new DatasetAssemblerException(root, "Not a resource: "+FmtUtils.stringForRDFNode(n)) ;
            Resource r = (Resource)n ;

            String gName = GraphUtils.getAsStringValue(r, DatasetAssemblerVocab.pGraphName) ;
            Resource g = GraphUtils.getResourceValue(r, DatasetAssemblerVocab.pGraph) ;
            if ( g == null )
            {
                g = GraphUtils.getResourceValue(r, DatasetAssemblerVocab.pGraphAlt) ;
                if ( g != null )
                    Log.warn(this, "Use of old vocabulary: use :graph not :graphData") ;
                else
                    throw new DatasetAssemblerException(root, "no graph for: "+gName) ;
            }
            
            Model m = a.openModel(g) ;
            ds.addNamedModel(gName, m) ;
        }
        
        return ds ;
    }

    public Object createTextIndex (Dataset ds, Resource root) 
    {
    	Object result = createTextIndex (ds, root, "org.apache.jena.larq.assembler.AssemblerLARQ") ;
    	if ( result == null ) result = createTextIndex (ds, root, "com.hp.hpl.jena.query.larq.AssemblerLARQ") ;
    	return result ;
    }
    
    protected Object createTextIndex (Dataset ds, Resource root, String className) 
    {
        try 
        {
            Class<?> clazz = Class.forName(className) ;
            if ( root.hasProperty(DatasetAssemblerVocab.pIndex) ) 
            {
                try {
                    Log.info(DataSourceAssembler.class, "Initializing LARQ") ;
                    String index = GraphUtils.getAsStringValue(root, DatasetAssemblerVocab.pIndex) ;
                    Class<?> paramTypes[] = new Class[] { Dataset.class, String.class } ;
                    Method method = clazz.getDeclaredMethod("make", paramTypes) ;
                    Object args[] = new Object[] { ds, index } ;
                    return method.invoke(clazz, args) ;
                } catch (Exception e) {
                	String msg;
                	if ( e.getMessage() != null ) {
                		msg = e.getMessage();
                	} else {
                		msg = e.getCause().getMessage();
                	}
                    Log.warn(DataSourceAssembler.class, String.format("Unable to initialize LARQ using %s: %s", className, msg)) ;
                }                
            }
        } catch(ClassNotFoundException e) {
            Log.info(DataSourceAssembler.class, "LARQ initialization: class " + className + " not in the classpath.") ;
        }
        
        return null ;
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