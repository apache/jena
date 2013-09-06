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

package org.apache.jena.query.spatial;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;
import com.spatial4j.core.shape.Shape;

public class SpatialIndexSolr implements SpatialIndex {
	private static Logger log = LoggerFactory.getLogger(SpatialIndexSolr.class);
	private final SolrServer solrServer;
	private EntityDefinition docDef;
	private SpatialPrefixTree grid;

	/**
	 * The Lucene spatial {@link SpatialStrategy} encapsulates an approach to
	 * indexing and searching shapes, and providing distance values for them.
	 * It's a simple API to unify different approaches. You might use more than
	 * one strategy for a shape as each strategy has its strengths and
	 * weaknesses.
	 * <p />
	 * Note that these are initialized with a field name.
	 */
	private SpatialStrategy strategy;

	public SpatialIndexSolr(SolrServer server, EntityDefinition def) {
		this.solrServer = server;
		this.docDef = def;

		int maxLevels = 11;// results in sub-meter precision for geohash
		// This can also be constructed from SpatialPrefixTreeFactory
		grid = new GeohashPrefixTree(SpatialQuery.ctx, maxLevels);

		this.strategy = new RecursivePrefixTreeStrategy(grid, def.getGeoField());
	}

	@Override
	public void startIndexing() {
	}

	@Override
	public void finishIndexing() {
		try {
			solrServer.commit();
		} catch (Exception ex) {
			exception(ex);
		}
	}

	@Override
	public void abortIndexing() {
		try {
			solrServer.rollback();
		} catch (Exception ex) {
			exception(ex);
		}
	}

	@Override
	public void close() {
		if (solrServer != null)
			solrServer.shutdown();
	}

	@Override
	public void add(String entityURI, Shape... shapes) {

		// log.info("Add entity: "+entityURI) ;
		try {
			SolrInputDocument doc = solrDoc(entityURI, shapes);
			solrServer.add(doc);
		} catch (Exception e) {
			exception(e);
		}
	}

    @SuppressWarnings("deprecation")
	private SolrInputDocument solrDoc(String entityURI, Shape... shapes) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField(docDef.getEntityField(), entityURI);
		if (shapes.length != 1) {
			throw new SpatialIndexException(
					"Solr spatial only supports indexing one shape a time, but provided: "
							+ shapes.length + " shapes.");
		}
		doc.addField(docDef.getGeoField(), SpatialQuery.ctx.toString(shapes[0]));
		return doc;
	}

	@Override
	public List<Node> query(Shape shape, int limit, SpatialOperation operation) {

		SolrDocumentList solrResults = solrQuery(shape, limit, operation);
		List<Node> results = new ArrayList<Node>();

		for (SolrDocument sd : solrResults) {
			String uriStr = (String) sd.getFieldValue(docDef.getEntityField());
			// log.info("Entity: "+uriStr) ;
			results.add(NodeFactory.createURI(uriStr));
		}

		if (limit > 0 && results.size() > limit)
			results = results.subList(0, limit);

		return results;
	}

    @SuppressWarnings("deprecation")
	private SolrDocumentList solrQuery(Shape shape, int limit,
			SpatialOperation operation) {
		SolrQuery sq = new SolrQuery();
		sq.setQuery("*:*");
		sq.setFilterQueries(docDef.getGeoField() + ":\"" + operation.toString()
				+ "(" + SpatialQuery.ctx.toString(shape) + ") distErrPct=0\"");
		//System.out.println("SolrQuery: " +sq.toString());
		try {
			QueryResponse rsp = solrServer.query(sq);
			SolrDocumentList docs = rsp.getResults();
			return docs;
		} catch (SolrServerException e) {
			exception(e);
			return null;
		}
	}

	@Override
	public EntityDefinition getDocDef() {
		return docDef;
	}

	private Node entryToNode(String v) {
		// TEMP
		return NodeFactoryExtra.createLiteralNode(v, null, null);
	}

	public SolrServer getServer() {
		return solrServer;
	}

	private static Void exception(Exception ex) {
		throw new SpatialIndexException(ex);
	}
}
