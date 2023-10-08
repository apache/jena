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

package org.apache.jena.query.text ;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.tdb1.TDB1;
import org.apache.jena.vocabulary.RDFS ;
import org.apache.lucene.store.ByteBuffersDirectory ;
import org.apache.lucene.store.Directory ;
import org.junit.Test ;

/** Test the examples of building a test dataset */
public class TestBuildTextDataset
{
    static final String DIR = "testing/TextQuery" ;

    @Test
    public void buildText_01() {
        createAssembler("text-config.ttl") ;
    }

    @Test
    public void buildText_02() {
        Dataset ds = createAssembler("text-config-union.ttl") ;
        assertTrue(ds.getContext().isDefined(TextQuery.textIndex)) ;
        assertTrue(ds.getContext().isDefined(TDB1.symUnionDefaultGraph)) ;
    }

    @Test
    public void buildText_03() {
        createCode() ;
    }

    @Test
    public void buildText_04() {
        Dataset ds = createAssembler("text-config.ttl") ;
        loadData(ds) ;
        queryData(ds) ;
    }

    @Test
    public void buildText_05() {
        Dataset ds = createCode() ;
        loadData(ds) ;
        queryData(ds) ;
    }

    private static void loadData(Dataset dataset) {
        dataset.begin(ReadWrite.WRITE) ;
        try {
            Model m = dataset.getDefaultModel() ;
            RDFDataMgr.read(m, DIR + "/data1.ttl") ;
            dataset.commit() ;
        }
        finally {
            dataset.end() ;
        }
    }

    public static void queryData(Dataset dataset) {
        String pre = StrUtils.strjoinNL("PREFIX : <http://example/>", "PREFIX text: <http://jena.apache.org/text#>",
                                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>") ;

        String qs = StrUtils.strjoinNL("SELECT * ", " { ?s text:query (rdfs:label 'X1') ;", "      rdfs:label ?label",
                                       " }") ;

        dataset.begin(ReadWrite.READ) ;
        int x ;
        try {
            Query q = QueryFactory.create(pre + "\n" + qs) ;
            try(QueryExecution qexec = QueryExecutionFactory.create(q, dataset)) {
                x = ResultSetFormatter.consume(qexec.execSelect()) ;
            }
        }
        finally {
            dataset.end() ;
        }
        assertEquals("Unexpected result count", 2, x) ;
    }

    public static Dataset createCode() {
        // Base data
        Dataset ds1 = DatasetFactory.create() ;

        // Define the index mapping
        EntityDefinition entDef = new EntityDefinition("uri", "text");
        entDef.setPrimaryPredicate(RDFS.label);

        // Lucene, in memory.
        Directory dir = new ByteBuffersDirectory() ;

        // Join together into a dataset
        Dataset ds = TextDatasetFactory.createLucene(ds1, dir, new TextIndexConfig(entDef)) ;

        return ds ;
    }

    public static Dataset createAssembler(String assemblerFile) {
        Dataset ds = DatasetFactory.assemble("testing/TextQuery/" + assemblerFile,
                                             "http://localhost/jena_example/#text_dataset") ;
        return ds ;
    }

}
