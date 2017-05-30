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

import java.io.File;
import java.io.IOException;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SpatialSearchUtil {
    private static final Analyzer analyzer = new StandardAnalyzer();
	private static final String LUCENE_INDEX_PATH = "target/test/LuceneSpatialIndex";
	private static final File LUCENE_Index_DIR = new File(LUCENE_INDEX_PATH);
	
    public static void emptyAndDeleteDirectory(File dir) {
        File[] contents = dir.listFiles() ;
        if (contents != null) {
            for (File content : contents) {
                if (content.isDirectory()) {
                    emptyAndDeleteDirectory(content) ;
                } else {
                    content.delete() ;
                }
            }
        }
        dir.delete() ;
    }

    public static void createEmptyIndex(File indexDir) {
        try {
            Directory directory = FSDirectory.open(indexDir.toPath()) ;
            IndexWriterConfig wConfig = new IndexWriterConfig(analyzer) ;
            // force creation of the index files
            try ( IndexWriter indexWriter = new IndexWriter(directory, wConfig) ){}
        } catch (IOException ex) {
            IO.exception(ex) ;
        }
	}
    
    public static Dataset initInMemoryDatasetWithLuceneSpatitalIndex() throws IOException{
    	return initInMemoryDatasetWithLuceneSpatitalIndex(LUCENE_Index_DIR);
    }
    
    public static Dataset initInMemoryDatasetWithLuceneSpatitalIndex(File indexDir) throws IOException{
		deleteOldFiles(indexDir);
		indexDir.mkdirs();
		return createDatasetByCode(indexDir);
    }
    
    public static Dataset initTDBDatasetWithLuceneSpatitalIndex(File indexDir, File TDBDir) throws IOException{
		deleteOldFiles(indexDir);
		deleteOldFiles(TDBDir);
		indexDir.mkdirs();
		TDBDir.mkdir();
		return createDatasetByCode(indexDir, TDBDir);
    }
    
	public static void deleteOldLuceneIndexDir() {
		deleteOldFiles(LUCENE_Index_DIR);
	}

	public static void deleteOldFiles(File indexDir) {
		if (indexDir.exists())
			emptyAndDeleteDirectory(indexDir);
	}
	
	private static Dataset createDatasetByCode(File indexDir) throws IOException {
		// Base data
		Dataset ds1 = DatasetFactory.create();
		return joinDataset(ds1, indexDir);
	}
	
	private static Dataset createDatasetByCode(File indexDir, File TDBDir) throws IOException {
		// Base data
		Dataset ds1 = TDBFactory.createDataset(TDBDir.getAbsolutePath());
		return joinDataset(ds1, indexDir);
	}
	
	private static Dataset joinDataset(Dataset baseDataset, File indexDir) throws IOException{
		EntityDefinition entDef = new EntityDefinition("uri", "geo");

		// Lucene, index in File system.
		Directory dir = FSDirectory.open(indexDir.toPath());

		// Join together into a dataset
		Dataset ds = SpatialDatasetFactory.createLucene(baseDataset, dir, entDef);

		return ds;
	}

}
