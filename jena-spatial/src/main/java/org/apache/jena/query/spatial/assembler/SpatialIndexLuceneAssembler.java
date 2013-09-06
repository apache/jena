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

package org.apache.jena.query.spatial.assembler;

import static org.apache.jena.query.spatial.assembler.SpatialVocab.pDefinition;
import static org.apache.jena.query.spatial.assembler.SpatialVocab.pDirectory;

import java.io.File;
import java.io.IOException;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.query.spatial.EntityDefinition;
import org.apache.jena.query.spatial.SpatialDatasetFactory;
import org.apache.jena.query.spatial.SpatialIndex;
import org.apache.jena.query.spatial.SpatialIndexException;
import org.apache.jena.riot.system.IRILib;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

public class SpatialIndexLuceneAssembler extends AssemblerBase
{
    /*
    <#index> a :SpatialIndexLucene ;
        #spatial:directory "mem" ;
        spatial:directory <file:DIR> ;
        spatial:definition <#definition> ;
        .
    */

    @Override
    public SpatialIndex open(Assembler a, Resource root, Mode mode)
    {
        try
        {
            if ( ! GraphUtils.exactlyOneProperty(root, pDirectory) )
                throw new SpatialIndexException("No 'spatial:directory' property on "+root) ;
            
            Directory directory ;
            RDFNode n = root.getProperty(pDirectory).getObject() ;
            if ( n.isLiteral() )
            {
                if ( ! "mem".equals(n.asLiteral().getLexicalForm()) )
                    throw new SpatialIndexException("No 'spatial:directory' property on "+root+ " is a literal and not \"mem\"") ;
                 directory = new RAMDirectory() ;
            }
            else
            { 
                Resource x = n.asResource() ;
                String path = IRILib.IRIToFilename(x.getURI()) ; 
                File dir = new File(path) ; 
                directory = FSDirectory.open(dir) ;
            }
        
            Resource r = GraphUtils.getResourceValue(root, pDefinition) ;
            EntityDefinition docDef = (EntityDefinition)a.open(r) ; 
            
            return SpatialDatasetFactory.createLuceneIndex(directory, docDef) ;
        } catch (IOException e) { IO.exception(e) ; return null ;}
    }

}

