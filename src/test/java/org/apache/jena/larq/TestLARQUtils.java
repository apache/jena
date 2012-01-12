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

package org.apache.jena.larq;

import java.io.IOException;
import java.util.Iterator ;

import org.apache.jena.larq.IndexBuilderModel;
import org.apache.jena.larq.IndexLARQ;
import org.apache.jena.larq.LARQ;
import org.apache.lucene.index.IndexReader;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.util.iterator.NiceIterator ;

public class TestLARQUtils
{
    public static QueryExecution query(Model model, String pattern)
    { return query(model, pattern, null) ; }

    public static QueryExecution query(Model model, String pattern, IndexLARQ index)
    {
        String queryString = StrUtils.strjoin("\n", 
            "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>" ,
            "PREFIX :       <http://example/>" ,
            "PREFIX pf:     <http://jena.hpl.hp.com/ARQ/property#>",
            "PREFIX  dc:    <http://purl.org/dc/elements/1.1/>",
            "SELECT *",
            pattern ) ;
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
        if ( index != null )
            LARQ.setDefaultIndex(qExec.getContext(), index) ;
        return qExec ;
    }
    
    public static int count(ResultSet rs)
    {
        return ResultSetFormatter.consume(rs) ;
    }
    
    public static int count(Iterator<?> iter)
    {
        int count = 0 ; 
        for ( ; iter.hasNext() ; )
        {
            iter.next();
            count++ ;
        }
        NiceIterator.close(iter) ;
        return count ;
    }
    
    public static IndexLARQ createIndex(String datafile, IndexBuilderModel indexBuilder)
    { 
        return createIndex(ModelFactory.createDefaultModel(), datafile, indexBuilder) ; 
    }
    
    public static IndexLARQ createIndex(Model model, String datafile, IndexBuilderModel indexBuilder)
    {
        model.register(indexBuilder) ;
        FileManager.get().readModel(model, datafile) ;
        model.unregister(indexBuilder) ;
        indexBuilder.closeWriter() ;
        return indexBuilder.getIndex() ;
    }

    public static IndexReader openIfChanged (IndexReader reader) throws IOException
    {
        IndexReader newIndexReader = IndexReader.openIfChanged(reader) ;
        return newIndexReader != null ? newIndexReader : reader ;
    }
    
}
