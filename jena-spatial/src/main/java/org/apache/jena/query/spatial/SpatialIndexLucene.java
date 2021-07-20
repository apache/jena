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

package org.apache.jena.query.spatial;

import java.io.IOException ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.analysis.standard.StandardAnalyzer ;
import org.apache.lucene.document.Document ;
import org.apache.lucene.document.Field ;
import org.apache.lucene.document.FieldType ;
import org.apache.lucene.index.* ;
import org.apache.lucene.search.* ;
import org.apache.lucene.spatial.SpatialStrategy ;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy ;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree ;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree ;
import org.apache.lucene.spatial.query.SpatialArgs ;
import org.apache.lucene.spatial.query.SpatialOperation ;
import org.apache.lucene.store.Directory ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import org.locationtech.spatial4j.shape.Shape ;

public class SpatialIndexLucene implements SpatialIndex {
	private static Logger log = LoggerFactory
			.getLogger(SpatialIndexLucene.class);

	private static int MAX_N = 10000;

	public static final FieldType ftIRI;
	static {
		ftIRI = new FieldType();
		ftIRI.setTokenized(false);
		ftIRI.setStored(true);
		ftIRI.setIndexOptions(IndexOptions.DOCS);
		ftIRI.freeze();
	}
	// public static final FieldType ftText = TextField.TYPE_NOT_STORED ;
	// Bigger index, easier to debug!
	// public static final FieldType ftText = TextField.TYPE_STORED ;

	private final EntityDefinition docDef;
	private final Directory directory;
	private IndexWriter indexWriter;
	private Analyzer analyzer = new StandardAnalyzer();

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

	public SpatialIndexLucene(Directory directory, EntityDefinition def) {
		this.directory = directory;
		this.docDef = def;

		int maxLevels = 11;// results in sub-meter precision for geohash
		// This can also be constructed from SpatialPrefixTreeFactory
		SpatialPrefixTree grid = new GeohashPrefixTree(SpatialQuery.ctx, maxLevels);

		this.strategy = new RecursivePrefixTreeStrategy(grid, def.getGeoField());

		//this.strategy = new PointVectorStrategy(ctx, def.getGeoField());
		
		// force creation of the index if it don't exist
		// otherwise if we get a search before data is written we get an
		// exception
		startIndexing();
		finishIndexing();
	}

	public Directory getDirectory() {
		return directory;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	@Override
	public void startIndexing() {
		try {
			IndexWriterConfig wConfig = new IndexWriterConfig(analyzer);
			indexWriter = new IndexWriter(directory, wConfig);
		} catch (IndexFormatTooOldException e) {
			throw new SpatialIndexException("jena-spatial/Lucene cannot use indexes created before Jena 3.3.0. "
	        		+ "Please rebuild your spatial index using jena.spatialindexer from Jena 3.3.0 or above.", e);
		} catch (IOException e) {
			exception(e);
		}
	}

	@Override
	public void finishIndexing() {
		try {
			indexWriter.commit();
			indexWriter.close();
			indexWriter = null;
		} catch (IOException e) {
			exception(e);
		}
	}

	@Override
	public void abortIndexing() {
		try {
			indexWriter.rollback();
		} catch (IOException ex) {
			exception(ex);
		}
	}

	@Override
	public void close() {
		if (indexWriter != null)
			try {
				indexWriter.close();
			} catch (IOException ex) {
				exception(ex);
			}
	}

	@Override
	public void add(String entityURI, Shape... shapes) {
		try {
			boolean autoBatch = (indexWriter == null);

			Document doc = doc(entityURI, shapes);
			if (autoBatch)
				startIndexing();
			indexWriter.addDocument(doc);
			if (autoBatch)
				finishIndexing();
		} catch (IOException e) {
			exception(e);
		}
	}

	private Document doc(String entityURI, Shape... shapes) {
		Document doc = new Document();
		Field entField = new Field(docDef.getEntityField(), entityURI, ftIRI);
		doc.add(entField);
		for (Shape shape : shapes) {
			for (IndexableField f : strategy.createIndexableFields(shape)) {
				doc.add(f);
			}
		}

		return doc;
	}
	
    @Override
    public List<Node> query(Shape shape, int limit, SpatialOperation operation) {
        // Upgrade at Java7 ...
        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            return query$(indexReader, shape, limit, operation) ;
        }
        catch (Exception ex) {
            exception(ex) ;
            return null ;
        }
    }
	
	private List<Node> query$(IndexReader indexReader, Shape shape, int limit, SpatialOperation operation) throws IOException {
		if (limit <= 0)
			limit = MAX_N;

		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		SpatialArgs args = new SpatialArgs(operation, shape);
		args.setDistErr(0.0);
		Query query = strategy.makeQuery(args);
		TopDocs docs = indexSearcher.search(query, limit);

		List<Node> results = new ArrayList<>();

		for (ScoreDoc sd : docs.scoreDocs) {
			Document doc = indexSearcher.doc(sd.doc);
			String[] values = doc.getValues(docDef.getEntityField());
			for (String v : values) {
			    Node n = SpatialQueryFuncs.stringToNode(v) ;
				results.add(n);
			}
		}
		return results;
	}

	@Override
	public EntityDefinition getDocDef() {
		return docDef;
	}


	private static void exception(Exception ex) {
		throw new SpatialIndexException(ex);
	}





}
