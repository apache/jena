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

package org.apache.jena.tdb.assembler;

import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue ;
import static org.apache.jena.sparql.util.graph.GraphUtils.getResourceValue ;
import static org.apache.jena.sparql.util.graph.GraphUtils.getStringValue ;
import static org.apache.jena.tdb.assembler.VocabTDB.pDataset ;
import static org.apache.jena.tdb.assembler.VocabTDB.pGraphName1 ;
import static org.apache.jena.tdb.assembler.VocabTDB.pGraphName2 ;
import static org.apache.jena.tdb.assembler.VocabTDB.pIndex ;
import static org.apache.jena.tdb.assembler.VocabTDB.pLocation ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.assembler.assemblers.AssemblerBase ;
import org.apache.jena.assembler.exceptions.AssemblerException ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.tdb.TDBException ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.base.file.Location ;

public class TDBGraphAssembler extends AssemblerBase implements Assembler
{
    static IndexAssembler indexAssembler = null ; 
    
    @Override
    public Model open(Assembler a, Resource root, Mode mode)
    {
        // Make a model - the default model of the TDB dataset
        // [] rdf:type tdb:GraphTDB ;
        //    tdb:location "dir" ;
        
        // Make a named model.
        // [] rdf:type tdb:GraphTDB ;
        //    tdb:location "dir" ;
        //    tdb:graphName <http://example/name> ;

        // Location or dataset reference.
        String locationDir = getStringValue(root, pLocation) ;
        Resource dataset = getResourceValue(root, pDataset) ;
        
        if ( locationDir != null && dataset != null )
            throw new AssemblerException(root, "Both location and dataset given: exactly one required") ; 
        
        if ( locationDir == null && dataset == null )
            throw new AssemblerException(root, "Must give location or refer to a dataset description") ;
        
        String graphName = null ;
        if ( root.hasProperty(pGraphName1) )
            graphName = getAsStringValue(root, pGraphName1) ;
        if ( root.hasProperty(pGraphName2) )
            graphName = getAsStringValue(root, pGraphName2) ;

        if ( root.hasProperty(pIndex) )
            Log.warn(this, "Custom indexes not implemented yet - ignored") ;

        final Dataset ds ;
        
        if ( locationDir != null )
        {
            Location location = Location.create(locationDir) ;
            ds = TDBFactory.createDataset(location) ;
        }
        else
            ds = DatasetAssemblerTDB.make(dataset) ;

        try {
            if ( graphName != null )
                return ds.getNamedModel(graphName) ;
            else
                return ds.getDefaultModel() ;
        } catch (RuntimeException ex)
        {
            ex.printStackTrace(System.err) ;
            throw ex ;
        }
    }
    
    //@Unused
    private void indexes(Resource root)
    {
        // ---- API ways

        StmtIterator sIter = root.listProperties(pIndex) ;
        while(sIter.hasNext())
        {
            RDFNode obj = sIter.nextStatement().getObject() ;
            if ( obj.isLiteral() )
            {
                String desc = ((Literal)obj).getString() ;
                System.out.printf("Index: %s\n", desc) ; System.out.flush();
                continue ;
            }
            throw new TDBException("Wrong format for tdb:index: should be a string: found: "+NodeFmtLib.displayStr(obj)) ; 
            //          Resource x = (Resource)obj ;
            //          String desc = x.getProperty(pDescription).getString() ;
            //          String file = x.getProperty(pFile).getString() ;
            //          System.out.printf("Index: %s in file %s\n", desc, file) ; System.out.flush();
        }

        System.out.flush();
        throw new TDBException("Custom indexes turned off") ; 
    }

}
