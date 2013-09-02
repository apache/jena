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

import org.apache.jena.query.spatial.assembler.TestEntityDefinitionAssembler;
import org.apache.jena.query.spatial.assembler.TestSpatialDatasetAssembler;
import org.apache.jena.query.spatial.assembler.TestSpatialIndexLuceneAssembler;
import org.apache.jena.query.spatial.pfunction.lucene.TestEastPFWithLuceneSpatialIndex;
import org.apache.jena.query.spatial.pfunction.lucene.TestIntersectsBoxPFWithLuceneSpatialIndex;
import org.apache.jena.query.spatial.pfunction.lucene.TestIsNearByPFWithLuceneSpatialIndex;
import org.apache.jena.query.spatial.pfunction.lucene.TestIsWithinBoxPFWithLuceneSpatialIndex;
import org.apache.jena.query.spatial.pfunction.lucene.TestIsWithinCirclePFWithLuceneSpatialIndex;
import org.apache.jena.query.spatial.pfunction.lucene.TestNorthPFWithLuceneSpatialIndex;
import org.apache.jena.query.spatial.pfunction.lucene.TestSouthPFWithLuceneSpatialIndex;
import org.apache.jena.query.spatial.pfunction.lucene.TestWestPFWithLuceneSpatialIndex;
import org.apache.jena.query.spatial.pfunction.solr.TestEastPFWithEmbeddedSolrSpatialIndex;
import org.apache.jena.query.spatial.pfunction.solr.TestIntersectsBoxPFWithEmbeddedSolrSpatialIndex;
import org.apache.jena.query.spatial.pfunction.solr.TestIsNearByPFWithEmbeddedSolrSpatialIndex;
import org.apache.jena.query.spatial.pfunction.solr.TestIsWithinBoxPFWithEmbeddedSolrSpatialIndex;
import org.apache.jena.query.spatial.pfunction.solr.TestIsWithinCirclePFWithEmbeddedSolrSpatialIndex;
import org.apache.jena.query.spatial.pfunction.solr.TestNorthPFWithEmbeddedSolrSpatialIndex;
import org.apache.jena.query.spatial.pfunction.solr.TestSouthPFWithEmbeddedSolrSpatialIndex;
import org.apache.jena.query.spatial.pfunction.solr.TestWestPFWithEmbeddedSolrSpatialIndex;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	
		TestIsWithinCirclePFWithLuceneSpatialIndex.class,
		TestIsNearByPFWithLuceneSpatialIndex.class,
		TestIsWithinBoxPFWithLuceneSpatialIndex.class,
		TestIntersectsBoxPFWithLuceneSpatialIndex.class,
		TestNorthPFWithLuceneSpatialIndex.class,
		TestSouthPFWithLuceneSpatialIndex.class,
		TestEastPFWithLuceneSpatialIndex.class,
		TestWestPFWithLuceneSpatialIndex.class,
		
		TestIsWithinCirclePFWithEmbeddedSolrSpatialIndex.class,
		TestIsNearByPFWithEmbeddedSolrSpatialIndex.class,
		TestIsWithinBoxPFWithEmbeddedSolrSpatialIndex.class,
		TestIntersectsBoxPFWithEmbeddedSolrSpatialIndex.class,
		TestNorthPFWithEmbeddedSolrSpatialIndex.class,
		TestSouthPFWithEmbeddedSolrSpatialIndex.class,
		TestEastPFWithEmbeddedSolrSpatialIndex.class,
		TestWestPFWithEmbeddedSolrSpatialIndex.class,
		
		TestTDBDatasetWithLuceneSpatialIndex.class,
		TestIndexingSpatialDataWithLucene.class,

		TestEntityDefinitionAssembler.class,
		TestSpatialDatasetAssembler.class,
		TestSpatialIndexLuceneAssembler.class,
		TestSpatialPredicatePairValue.class
		
		})
public class TS_Spatial {
}
