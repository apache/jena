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

package org.apache.jena.query.spatial.assembler;

import static org.apache.jena.query.spatial.assembler.SpatialVocab.pDefinition ;
import static org.apache.jena.query.spatial.assembler.SpatialVocab.pServer ;
import org.apache.jena.query.spatial.EntityDefinition ;
import org.apache.jena.query.spatial.SpatialDatasetFactory ;
import org.apache.jena.query.spatial.SpatialIndex ;
import org.apache.jena.query.spatial.SpatialIndexException ;
import org.apache.solr.client.solrj.SolrServer ;
import org.apache.solr.client.solrj.impl.HttpSolrServer ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;

public class SpatialIndexSolrAssembler extends AssemblerBase {
	/*
	 * <#index> a :TextIndexSolr ; text:server
	 * <http://localhost:8983/solr/COLLECTION> ; #text:server <embedded:SolrARQ>
	 * ; text:entityMap <#endMap> ; .
	 */

	@Override
	public SpatialIndex open(Assembler a, Resource root, Mode mode) {
		String uri = GraphUtils.getResourceValue(root, pServer).getURI();
		SolrServer server;
		if (uri.startsWith("embedded:")) {
		    throw new SpatialIndexException("Embedded Solr server not supported (change code and dependencies to enable)") ;
//			try {
//	            if ( ! GraphUtils.exactlyOneProperty(root, pSolrHome) )
//	                throw new SpatialIndexException("No 'spatial:solrHome' property on EmbeddedSolrServer "+root) ;
//	            
//	            RDFNode n = root.getProperty(pSolrHome).getObject() ;
//	            
//	            if (n.isLiteral()){
//	            	throw new SpatialIndexException ("No 'spatial:solrHome' property on EmbeddedSolrServer "+root+ " is a literal and not a Resource/URI");
//	            }
//                Resource x = n.asResource() ;
//                String path = IRILib.IRIToFilename(x.getURI()) ;
//				
//				System.setProperty("solr.solr.home", path);
//				String coreName = uri.substring("embedded:".length());
//				CoreContainer.Initializer initializer = new CoreContainer.Initializer();
//				CoreContainer coreContainer = initializer.initialize();
//				server = new EmbeddedSolrServer(coreContainer, coreName);
//			} catch (FileNotFoundException e) {
//				throw new SpatialIndexException(e);
//				// throw new
//				// SpatialIndexException("Embedded Solr server not supported (change code and dependencies to enable)")
//				// ;
//			}
		} else if (uri.startsWith("http://")) {
			server = new HttpSolrServer(uri);
		} else
			throw new SpatialIndexException(
					"URI for the server must begin 'http://'");

		Resource r = GraphUtils.getResourceValue(root, pDefinition);
		EntityDefinition docDef = (EntityDefinition) a.open(r);
		return SpatialDatasetFactory.createSolrIndex(server, docDef);
	}
}
