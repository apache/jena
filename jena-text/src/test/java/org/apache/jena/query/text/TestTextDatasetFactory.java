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

package org.apache.jena.query.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

public class TestTextDatasetFactory {
	
	static private final EntityDefinition entityDefinition = new EntityDefinition("text", "rdfs:label");

	static private final TextIndexConfig config = new TextIndexConfig(entityDefinition);
	
	@Test
	public void testOneLuceneTextIndexObjectPerLuceneDirectory_fs() throws IOException {
		TextIndex index1 = null;
		TextIndex index2 = null;
		try {
		    Directory directory = createTempFSDirectory();
		    index1 = TextDatasetFactory.createLuceneIndex(directory, config);
		    index2 = TextDatasetFactory.createLuceneIndex(directory, config);
		    assertEquals(index1, index2);
		} finally {
		    if (index1 != null) index1.close();
		    if (index2 != null) index2.close();
		}
	}
	
	@Test 
	public void testOneLuceneTextIndexObjectPerLuceneDirectory_mem() throws IOException {
		TextIndex index1 = null;
		TextIndex index2 = null;
		try {
		    Directory directory = new RAMDirectory();
		    index1 = TextDatasetFactory.createLuceneIndex(directory, config);
		    index2 = TextDatasetFactory.createLuceneIndex(directory, config);
		    assertEquals(index1, index2);
		} finally {
		    if (index1 != null) index1.close();
		    if (index2 != null) index2.close();
		}
	}	
	
	@Test
	public void testnewLuceneIndexObjectAfterClose_fs() throws IOException {
		TextIndex index1 = null;
		TextIndex index2 = null;
		try {
		    Directory directory = createTempFSDirectory();
		    index1 = TextDatasetFactory.createLuceneIndex(directory, config);
		    index1.close();
		    index2 = TextDatasetFactory.createLuceneIndex(directory, config);
		    assertNotEquals(index1, index2);
		} finally {
		    if (index1 != null) index1.close();
		    if (index2 != null) index2.close();
		}
	}

	
	@Test 
	public void testNewLuceneIndexObjectAfterClose_mem() throws IOException {
		TextIndex index1 = null;
		TextIndex index2 = null;
		try {
		    Directory directory = new RAMDirectory();
		    index1 = TextDatasetFactory.createLuceneIndex(directory, config);
		    index1.close();
		    index2 = TextDatasetFactory.createLuceneIndex(directory, config);
		    assertNotEquals(index1, index2);
		} finally {
		    if (index1 != null) index1.close();
		    if (index2 != null) index2.close();
		}
	}
	
	private Directory createTempFSDirectory() throws IOException {
		Path fsDir = Files.createTempDirectory("DB-lucene");
		fsDir.toFile().deleteOnExit();		
		return FSDirectory.open(fsDir.toFile());		
	}
}
