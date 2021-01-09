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
package examples;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.*;
import org.apache.jena.sparql.util.QueryExecUtils;

/**
 * Simple example class to test the {@link org.apache.jena.query.text.es.assembler.TextIndexESAssembler}
 * For this class to work properly, an elasticsearch node should be up and running, otherwise it will fail.
 * You can find the details of downloading and running an ElasticSearch version here: https://www.elastic.co/downloads/past-releases/elasticsearch-5-2-1
 * Unzip the file in your favourite directory and then execute the appropriate file under the bin directory.
 * It will take less than a minute.
 * In order to visualize what is written in ElasticSearch, you need to download and run Kibana: https://www.elastic.co/downloads/kibana
 * To run kibana, just go to the bin directory and execute the appropriate file.
 * We need to resort to this mechanism as ElasticSearch has stopped supporting embedded ElasticSearch.
 *
 * In addition we cant have it in the test package because ElasticSearch
 * detects the thread origin and stops us from instantiating a client.
 */
public class JenaESTextExample {

    public static void main(String[] args) {

        queryData(loadData(createAssembler()));
    }


    private static Dataset createAssembler() {
        String assemblerFile = "text-config-es.ttl";
        Dataset ds = DatasetFactory.assemble(assemblerFile, "http://localhost/jena_example/#text_dataset") ;
        return ds;
    }

    private static Dataset loadData(Dataset ds) {
        JenaTextExample1.loadData(ds, "data-es.ttl");
        return ds;
    }

    /**
     * Query Data
     * @param ds
     */
    private static void queryData(Dataset ds) {
        queryDataWithoutProperty(ds);
    }

    public static void queryDataWithoutProperty(Dataset dataset)
    {


        String pre = StrUtils.strjoinNL
                ( "PREFIX : <http://example/>"
                        , "PREFIX text: <http://jena.apache.org/text#>"
                        , "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>") ;

        String qs = StrUtils.strjoinNL
                ( "SELECT * "
//                        , " { ?s text:query (rdfs:comment 'this' 'lang:en') ;"
//                        , " { ?s text:query ('this' 'lang:en*') ;"
                        ,"{ ?s text:query ('2016-12-01T15:31:10-05:00') ;"
//                        , " { ?s text:query ('this' 'lang:en-GB') ;"
//                        , " { ?s text:query (rdfs:comment 'this' 'lang:en-GB') ;"
//                        , " { ?s text:query (rdfs:comment 'this' 'lang:*') ;"
//                        , " { ?s text:query (rdfs:comment 'this' 'lang:none') ;"
//                        , " { ?s text:query (rdfs:comment 'this') ;"
//                        , " { ?s text:query ('X1' 'lang:en') ;"
                        , "      rdfs:label ?label"
                        , " }") ;

        dataset.begin(ReadWrite.READ) ;
        try {
            Query q = QueryFactory.create(pre+"\n"+qs) ;
            QueryExecution qexec = QueryExecutionFactory.create(q , dataset) ;
            QueryExecUtils.executeQuery(q, qexec) ;
        } finally { dataset.end() ; }


    }
}
