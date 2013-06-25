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

import java.io.File ;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;

public class TestDatasetWithEmbeddedSolrTextIndex extends AbstractTestDatasetWithTextIndex {
    
    private static final String  DATA_PATH      = "target/test/SolrARQ/data";
    private static final File    DATA_DIR       = new File(DATA_PATH);
    private static final String  INDEX_PATH     = DATA_PATH + "/index";
    private static final File    INDEX_DIR      = new File(INDEX_PATH);
    private static final String  TEST_ASSEM     = "testing/TextQuery/text-solr-config.ttl" ;

	@BeforeClass public static void beforeClass() {
	    deleteOldFiles();
	    INDEX_DIR.mkdirs();
	    TextQuery.init() ;
	    TextSearchUtil.createEmptyIndex(INDEX_DIR);
	    dataset = TextDatasetFactory.create(TEST_ASSEM) ;
	}

	@AfterClass public static void afterClass() {
		TextIndexSolr index = (TextIndexSolr) dataset.getContext().get(TextQuery.textIndex) ;
		index.getServer().shutdown();
		deleteOldFiles();
	}

	public static void deleteOldFiles() {
		if (DATA_DIR.exists()) 
			TextSearchUtil.emptyAndDeleteDirectory(DATA_DIR);
	}
}
