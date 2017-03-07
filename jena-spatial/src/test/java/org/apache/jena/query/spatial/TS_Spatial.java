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

import org.apache.jena.query.spatial.assembler.TestEntityDefinitionAssembler ;
import org.apache.jena.query.spatial.assembler.TestSpatialDatasetAssembler ;
import org.apache.jena.query.spatial.assembler.TestSpatialIndexLuceneAssembler ;
import org.apache.jena.query.spatial.pfunction.lucene.* ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.junit.runners.Suite.SuiteClasses ;

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
		TestTDBDatasetWithLuceneSpatialIndex.class,
		TestIndexingSpatialDataWithLucene.class,

		TestEntityDefinitionAssembler.class,
		TestSpatialDatasetAssembler.class,
		TestSpatialIndexLuceneAssembler.class,
		TestSpatialPredicatePairValue.class
		
		})
public class TS_Spatial {
}
