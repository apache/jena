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

package org.apache.jena.query.text;

import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.RAMDirectory ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.vocabulary.RDFS ;


/**
 * This abstract class defines a setup configuration for a dataset with a graph-enabled Lucene index.
 */
public class AbstractTestDatasetWithLuceneGraphTextIndex extends AbstractTestDatasetWithGraphTextIndex {

	public static void init() {
	        Dataset ds1 = TDBFactory.createDataset() ;
                Directory dir = new RAMDirectory() ;
                EntityDefinition eDef = new EntityDefinition("iri", "text", "graph", RDFS.label.asNode()) ;
                eDef.set("comment", RDFS.comment.asNode()) ; // some tests require indexing rdfs:comment
                TextIndex tidx = new TextIndexLucene(dir, eDef) ;
                dataset = TextDatasetFactory.create(ds1, tidx) ;
	}

}
