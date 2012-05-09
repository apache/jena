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

package dev;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.larq.IndexBuilderModel;
import org.apache.jena.larq.IndexBuilderString;
import org.apache.jena.larq.IndexWriterFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;

public class Run {

    public static void main(String[] args) throws IOException {
        if ( args.length != 2 ) {
            System.out.println("Usage: Run <tdb location> <lucene index location>");
            System.exit(-1);
        }
        
        Location location = new Location(args[0]);
        Dataset dataset = TDBFactory.createDataset(location);
        
        FSDirectory dir = FSDirectory.open(new File(args[1]));
        IndexWriter indexWriter = IndexWriterFactory.create(dir) ;
        IndexBuilderModel larqBuilder = new IndexBuilderString(indexWriter) ; 
        
        Model defaultModel = dataset.getDefaultModel();
        index (larqBuilder, defaultModel);
        for ( Iterator<String> iter = dataset.listNames() ; iter.hasNext() ; ) {
            String g = iter.next() ;
            index(larqBuilder, dataset.getNamedModel(g)) ;
        }
    }
    
    private static void index(IndexBuilderModel larqBuilder, Model model) {
        StmtIterator sIter = model.listStatements() ;
        larqBuilder.indexStatements(sIter) ;
    }

}
