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

package org.apache.jena.sdb.script;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.assembler.assemblers.AssemblerBase ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sdb.assembler.AssemblerVocab ;
import org.apache.jena.sparql.resultset.ResultsFormat ;
import org.apache.jena.sparql.util.graph.GraphUtils ;

// EXPERIMENTAL - Move to ARQ?

public class QueryCommandAssembler extends AssemblerBase implements Assembler
{

    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        // Query
        Resource queryDesc = getUniqueResource(root, AssemblerVocab.pQuery) ;
        Query query = (Query)a.open(a, queryDesc, mode) ;
        
        // Dataset
        Resource datasetDesc = getUniqueResource(root, AssemblerVocab.pDataset) ;
        Dataset dataset = (Dataset)a.open(a, datasetDesc, mode) ;
        
        // Output format
        String s = GraphUtils.getStringValue(root, AssemblerVocab.pOutputFormat) ;
        if ( s == null ) 
            s = "text" ;
        ResultsFormat format = ResultsFormat.lookup(s) ;
        
        QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ;
        
        return new QExec(query, qExec, format) ;
    }
}
