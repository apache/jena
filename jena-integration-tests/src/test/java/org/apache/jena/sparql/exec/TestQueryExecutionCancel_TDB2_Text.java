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

package org.apache.jena.sparql.exec;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;

public class TestQueryExecutionCancel_TDB2_Text
    extends AbstractTestQueryExecutionCancel
{
    @Override
    public Dataset createDataset() {
        String spec = """
            prefix :     <http://www.example.org/>
            prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            prefix ja:   <http://jena.hpl.hp.com/2005/11/Assembler#>
            prefix tdb2: <http://jena.apache.org/2016/tdb#>
            prefix text: <http://jena.apache.org/text#>

            :text_dataset rdf:type     text:TextDataset ;
                text:dataset   :dataset_tdb2 ;
                text:index     :indexLucene ;
                .

            :indexLucene a text:TextIndexLucene ;
                text:directory "mem" ;
                text:entityMap :entMap ;
                .

            :entMap a text:EntityMap ;
                text:entityField      "uri" ;
                text:defaultField     "rdfs-label" ;
                text:uidField         "uid" ;
                text:map (
                     [ text:field "rdfs-label" ; text:predicate rdfs:label ]
                     )
                .

            :dataset_tdb2 rdf:type tdb2:DatasetTDB2 ;
                tdb2:location "--mem--" ;
                ja:context [ ja:cxtName "arq:queryTimeout" ; ja:cxtValue "1000" ] ;
                .
        """;
        Model specModel = RDFParser.fromString(spec, Lang.TURTLE).toModel();

        Dataset ds = DatasetFactory.assemble(specModel, "http://www.example.org/text_dataset");
        return ds;
    }
}
