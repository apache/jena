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
import static org.apache.jena.query.text.assembler.TextVocab.pCloseIndexOnClose ;
import static org.apache.jena.query.text.assembler.TextVocab.textDataset ;

import java.lang.reflect.Constructor;

import org.apache.jena.query.text.TextDatasetFactory ;
import org.apache.jena.query.text.TextDocProducer;

import java.lang.reflect.Constructor ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.text.TextDatasetFactory ;
import org.apache.jena.query.text.TextDocProducer ;
import org.apache.jena.query.text.TextDocProducerTriples ;

import org.apache.jena.query.text.TextIndex ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Resource ;

import com.hp.hpl.jena.sparql.core.DatasetGraph;

import com.hp.hpl.jena.sparql.ARQConstants ;

import com.hp.hpl.jena.sparql.core.assembler.DatasetAssembler ;
import com.hp.hpl.jena.sparql.util.Loader ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;

import static org.apache.jena.query.text.assembler.TextVocab.* ;

public class TextDatasetAssembler extends AssemblerBase implements Assembler
{
    private DatasetAssembler datasetAssembler = new DatasetAssembler() ;
    
    public static Resource getType() { return textDataset ; }
        
    /*
<#text_dataset> rdf:type     text:Dataset ;
    text:dataset <#dataset> ;
    text:index   <#index> ;
    .

    */
    
    /**
    	open creates a text dataset. The underlying dataset is
    	specfied by the value of the root's dataset property, the
    	text index by the value of the index property, any document
    	producer by the value of the docProducer property if present,
    	the closeIndexOnClose defaults to true unless the
    	closeIndexOnClose property specifies otherwise. 
     */
//    @Override
//	public Dataset open(Assembler a, Resource root, Mode mode)
//	{
//		Resource dataset = GraphUtils.getResourceValue(root, pDataset) ;
//		Resource index   = GraphUtils.getResourceValue(root, pIndex) ;
//        String producer = GraphUtils.getStringValue(root, pDocProducer ) ;
//        String close = GraphUtils.getStringValue(root, pCloseIndexOnClose ) ;
//
//        boolean closeIndexOnClose = (close == null ? true : close.equals("true"));
//        
//        Dataset ds = (Dataset)a.open(dataset) ;
//        TextIndex textIndex = (TextIndex)a.open(index) ;
//        TextDocProducer docProducer = null;
//        if (producer != null) {
//            docProducer = getDocProducer(root, producer, ds.asDatasetGraph(), textIndex);
//        }
//        Dataset dst = TextDatasetFactory.create(ds, textIndex, docProducer, closeIndexOnClose) ;
//        return dst ;        
//    }

	public Dataset open(Assembler a, Resource root, Mode mode) {	
	
		Resource dataset = GraphUtils.getResourceValue(root, pDataset) ;
		Resource index   = GraphUtils.getResourceValue(root, pIndex) ;

        Resource textDocProducerNode = GraphUtils.getResourceValue(root, pTextDocProducer) ;
        
        Dataset ds = (Dataset)a.open(dataset) ;
        TextIndex textIndex = (TextIndex)a.open(index) ;
        
        // Null will use the default producer
        TextDocProducer textDocProducer = null ;
        
        if (null != textDocProducerNode) {
            Class<?> c = Loader.loadClass(textDocProducerNode.getURI(), TextDocProducer.class) ;
            
            String className = textDocProducerNode.getURI().substring(ARQConstants.javaClassURIScheme.length()) ;
            Constructor<?> dyadic = getConstructor(c, DatasetGraph.class, TextIndex.class);
            Constructor<?> monadic = getConstructor(c, TextIndex.class);
            
            try {
            	if (dyadic != null) {
            		textDocProducer = (TextDocProducer) dyadic.newInstance(ds.asDatasetGraph(), textIndex) ;
            	} else if (monadic != null) {
            		textDocProducer = (TextDocProducer) monadic.newInstance(textIndex) ;
            	} else {
            		Log.warn(Loader.class, "Exception during instantiation '"+className+"' no TextIndex or DatasetGraph,Index constructor" );
            	}
            } catch (Exception ex) {
            	Log.warn(Loader.class, "Exception during instantiation '"+className+"': "+ex.getMessage()) ;
            	return null ;            	
            }
        }
        
        Dataset dst = TextDatasetFactory.create(ds, textIndex, true, textDocProducer) ;
        return dst ;
    }
	
	private Constructor<?> getConstructor(Class<?> c, Class<?> ...types) {
		try {
			return c.getConstructor(types);
		} catch (NoSuchMethodException e) {			
			return null;	
		}
	}
	
	@SuppressWarnings("unchecked")
	private TextDocProducer getDocProducer(Resource root, String className, DatasetGraph dsg, TextIndex textIndex) {
	    Class<TextDocProducer> pc;
	    try {
	        pc = (Class<TextDocProducer>) Class.forName(className);
	    } catch (ClassNotFoundException e) {
	        throw new AssemblerException(root, "failed to load class '" + className + "'", e);
	    }
	    Constructor<TextDocProducer> constructor;
	    try {
	        constructor = pc.getDeclaredConstructor(DatasetGraph.class, TextIndex.class);
	    } catch (NoSuchMethodException e) {
	        throw new AssemblerException(root, "DocProducer class has no constructor" + className +"(DatasetGraph,TextIndex)", e);
	    } catch (SecurityException e) {
	        throw new AssemblerException(root, "Security exception accessing " + className + "(DatasetGraph,TextIndex)", e);
	    }
	    try {
	        return constructor.newInstance(dsg, textIndex);
	    } catch (Exception e) {
	        throw new AssemblerException(root, "Can't create instance of " + className, e);
	    }   
	}
}

