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

import static org.apache.jena.query.text.assembler.TextVocab.pDataset ;
import static org.apache.jena.query.text.assembler.TextVocab.pIndex ;
import static org.apache.jena.query.text.assembler.TextVocab.pTextDocProducer ;
import static org.apache.jena.query.text.assembler.TextVocab.textDataset ;

import java.lang.reflect.Constructor ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.assembler.assemblers.AssemblerBase ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.text.TextDatasetFactory ;
import org.apache.jena.query.text.TextDocProducer ;
import org.apache.jena.query.text.TextIndex ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.ClsLoader ;
import org.apache.jena.sparql.util.graph.GraphUtils ;

public class TextDatasetAssembler extends AssemblerBase implements Assembler
{
    public static Resource getType() { return textDataset ; }

    /*
<#text_dataset> rdf:type     text:Dataset ;
    text:dataset <#dataset> ;
    text:index   <#index> ;
    .

     */

    @Override
    public Dataset open(Assembler a, Resource root, Mode mode)
    {
        Resource dataset = GraphUtils.getResourceValue(root, pDataset) ;
        Resource index   = GraphUtils.getResourceValue(root, pIndex) ;
        Resource textDocProducerNode = GraphUtils.getResourceValue(root, pTextDocProducer) ;

        Dataset ds = (Dataset)a.open(dataset) ;
        TextIndex textIndex = (TextIndex)a.open(index) ;
        // Null will use the default producer
        TextDocProducer textDocProducer = null ;
        if (null != textDocProducerNode) {
            Class<?> c = ClsLoader.loadClass(textDocProducerNode.getURI(), TextDocProducer.class) ;

            String className = textDocProducerNode.getURI().substring(ARQConstants.javaClassURIScheme.length()) ;
            Constructor<?> dyadic = getConstructor(c, DatasetGraph.class, TextIndex.class);
            Constructor<?> monadic = getConstructor(c, TextIndex.class);

            try {
                if (dyadic != null) {
                    textDocProducer = (TextDocProducer) dyadic.newInstance(ds.asDatasetGraph(), textIndex) ;
                } else if (monadic != null) {
                    textDocProducer = (TextDocProducer) monadic.newInstance(textIndex) ;
                } else {
                    Log.warn(ClsLoader.class, "Exception during instantiation '"+className+"' no TextIndex or DatasetGraph,Index constructor" );
                }
            } catch (Exception ex) {
                Log.warn(ClsLoader.class, "Exception during instantiation '"+className+"': "+ex.getMessage()) ;
                return null ;
            }
        }

        // "true" -> closeIndexOnDSGClose
        Dataset dst = TextDatasetFactory.create(ds, textIndex, true, textDocProducer) ;
        return dst ;
    }

    private static Constructor<?> getConstructor(Class<?> c, Class<?> ...types) {
        try {
            return c.getConstructor(types);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}

