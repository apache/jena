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

package org.apache.jena.query.text.assembler;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;

import static org.apache.jena.query.text.assembler.TextVocab.pDirectory;
import static org.apache.jena.query.text.assembler.TextVocab.pEntityMap;

public class TextIndexLuceneMultilingualAssembler extends AssemblerBase {
    /*
    <#index> a :TextIndexLuceneMultilingual ;
        #text:directory "mem" ;
        #text:directory "DIR" ;
        text:directory <file:DIR> ;
        text:entityMap <#endMap> ;
        .
    */
    
    @SuppressWarnings("resource")
    @Override
    public TextIndex open(Assembler a, Resource root, Mode mode) {
        try {
            if ( !GraphUtils.exactlyOneProperty(root, pDirectory) )
                throw new TextIndexException("No 'text:directory' property on " + root) ;

            Directory directory ;

            RDFNode n = root.getProperty(pDirectory).getObject() ;
            if ( n.isLiteral() ) {
                String literalValue = n.asLiteral().getLexicalForm() ;
                if (literalValue.equals("mem")) {
                    directory = new RAMDirectory() ;
                } else {
                    File dir = new File(literalValue) ;
                    directory = FSDirectory.open(dir) ;
                }
            } else {
                Resource x = n.asResource() ;
                String path = IRILib.IRIToFilename(x.getURI()) ;
                File dir = new File(path) ;
                directory = FSDirectory.open(dir) ;
            }

            Resource r = GraphUtils.getResourceValue(root, pEntityMap) ;
            EntityDefinition docDef = (EntityDefinition)a.open(r) ;

            return TextDatasetFactory.createLuceneIndexMultilingual(directory, docDef) ;
        } catch (IOException e) {
            IO.exception(e) ;
            return null ;
        }
    }
}
