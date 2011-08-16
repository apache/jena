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

package reports;


import java.io.StringReader ;

import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.RIOT ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.tdb.TDBFactory ;

public class ReportLiteralCanonicalization
{
    private static enum QueryBy {
        TRIPLE_PATTERN("triple pattern",
                       "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>%n"
                       + "SELECT ?x WHERE {%n"
                       + "   ?x ?y \"%1$s\"^^xsd:%2$s .%n"
                       + "}"),
                       FILTER("filter",
                              "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>%n"
                              + "SELECT ?x WHERE {%n"
                              + "   ?x ?y ?z .%n"
                              + "   FILTER( ?z = \"%1$s\"^^xsd:%2$s )%n"
                              + "}");

        public final String _label;
        public final String _queryFmt;

        private QueryBy(String label, String queryFmt) {
            _label = label;
            _queryFmt = queryFmt;
        }
    }

    public static void main(String[] args) throws Exception {
        runQueries(getMemoryModel(), "Memory");
        runQueries(getTdbModel(), "TDB");
    }

    private static Model getMemoryModel() {

        String data = StrUtils.strjoinNL(
                                         "@prefix eg:   <http://example.com/> .",
                                         "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .",
                                         "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .",
                                         "",
        "eg:F0 rdfs:label \"47\"^^xsd:integer .") ;


        Model model = ModelFactory.createDefaultModel();
        addData(model) ;
        return model;
    }

    private static Model getTdbModel() {
        //        File tdbDir = new File("tempTestTDBData");
        //        boolean needToLoadModel = !tdbDir.exists();
        //        Model model = TDBFactory.createModel(tdbDir.getAbsolutePath());
        Model model = TDBFactory.createModel();
        //        if (needToLoadModel)
        addData(model) ;
        return model;
    }

    private static void addData(Model model)
    {
        String data = StrUtils.strjoinNL(
                                         "@prefix eg:   <http://example.com/> .",
                                         "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .",
                                         "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .",
                                         "",
        "eg:F0 rdfs:label \"47\"^^xsd:integer .") ;
        RIOT.init() ;
        StringReader sr = new StringReader(data) ;
        model.read(sr, null, "TTL") ;

    }

    private static void runQueries(Model model, String modelKind) {
        runQuery(model, modelKind, QueryBy.TRIPLE_PATTERN, "integer", "47");
        runQuery(model, modelKind, QueryBy.TRIPLE_PATTERN, "integer", "+047");
        runQuery(model, modelKind, QueryBy.TRIPLE_PATTERN, "decimal", "47");
        runQuery(model, modelKind, QueryBy.TRIPLE_PATTERN, "decimal", "+047.0");

        runQuery(model, modelKind, QueryBy.FILTER, "integer", "47");
        runQuery(model, modelKind, QueryBy.FILTER, "integer", "+047");
        runQuery(model, modelKind, QueryBy.FILTER, "decimal", "47");
        runQuery(model, modelKind, QueryBy.FILTER, "decimal", "+047.0");
    }

    private static void runQuery(Model model, String modelKind,
                                 QueryBy by, String datatype, String lexicalForm) {

        Query query = QueryFactory.create(String.format(by._queryFmt, lexicalForm, datatype));
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        int count = countQueryResults(qe.execSelect());
        System.out.format(
                          "%1$6s model:  %2$6s as %3$s by %4$-15s  %5$d results%n",
                          modelKind, lexicalForm, datatype, (by._label + ":"), count);
        if ( count == 0 )
            System.out.println(query) ;

    }

    private static int countQueryResults(ResultSet rs) {
        int count = 0;
        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            ++count;
        }
        return count;
    }

}

