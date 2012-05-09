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

import java.lang.reflect.Method;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import org.openjena.atlas.logging.Log ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;

public class DatasetAssembler extends AssemblerBase implements Assembler
{
    public static Resource getType() { return DatasetAssemblerVocab.tDataset ; }
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        Dataset ds = createDataset(a, root, mode) ;
        createTextIndex(ds, root) ;

        return ds ;
    }
    
    public Dataset createDataset(Assembler a, Resource root, Mode mode) 
    {
        // Expanding version.
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        AssemblerUtils.setContext(root, dsg.getContext()) ;
        
        Dataset ds = DatasetFactory.create(dsg) ;

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
                    Log.info(DatasetAssembler.class, "Initializing LARQ") ;
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
                    Log.warn(DatasetAssembler.class, String.format("Unable to initialize LARQ using %s: %s", className, msg)) ;
                }                
            }
        } catch(ClassNotFoundException e) {
            LoggerFactory.getLogger(DatasetAssembler.class).debug("LARQ initialization: class " + className + " not in the classpath.") ;
        }
        
        return null ;
    }
    
}
