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

package com.hp.hpl.jena.sdb.test.junit;

import static org.apache.jena.atlas.lib.StrUtils.strjoinNL ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sdb.SDB ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sdb.StoreDesc ;
import com.hp.hpl.jena.sdb.store.StoreFactory ;
import com.hp.hpl.jena.sdb.util.Pair ;
import com.hp.hpl.jena.sdb.util.Vocab ;
import com.hp.hpl.jena.util.FileManager ;

public class StoreList
{
    static Property description = Vocab.property(SDB.namespace, "description") ;
    static Property list = Vocab.property(SDB.namespace, "list") ;
    static Resource storeListClass = Vocab.property(SDB.namespace, "StoreList") ;
    
    static boolean formatStores     = false ;
    static String queryString = strjoinNL
            (   
             "PREFIX sdb:      <http://jena.hpl.hp.com/2007/sdb#>" ,
             "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" ,
             "PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>" ,
             "PREFIX list:     <http://jena.hpl.hp.com/ARQ/list#>" ,
             "SELECT ?desc ?label" ,
             "{ [] rdf:type sdb:StoreList ;" ,
             "     sdb:list ?l ." ,
             "  ?l list:member [ rdfs:label ?label ; sdb:description ?desc ]",
            "}") ;
    
    // Not Java's finest hour ...
    static Transform<Pair<String, String>, Pair<String, StoreDesc>> t1 = new  Transform<Pair<String, String>, Pair<String, StoreDesc>>()
    {
        @Override
        public Pair<String, StoreDesc> convert(Pair<String, String> pair)
        {
            return new Pair<String, StoreDesc>(pair.car(), StoreDesc.read(pair.cdr())) ;
        }
    } ;

    static Transform<Pair<String, StoreDesc>, Pair<String, Store>> t2 = new Transform<Pair<String, StoreDesc>, Pair<String, Store>>()
    {
        @Override
        public Pair<String, Store> convert(Pair<String, StoreDesc> pair)
        {
            Store store = testStore(pair.cdr()) ;
            return new Pair<String, Store>(pair.car(), store) ;
        }
    } ;
    
    public static Store testStore(StoreDesc desc)
    {
        Store store = StoreFactory.create(desc) ;
        // HSQL and H2 (in memory) need formatting
        // Better would be to know in memory/on disk
        // Relies on StoreDesc getting the label correctly (SDBConnectionFactory)
        String jdbcURL = store.getConnection().getJdbcURL() ;
        boolean isInMem =  (jdbcURL==null ? false : jdbcURL.contains(":mem:") ) ;
        if ( formatStores || inMem(store) )
            store.getTableFormatter().create() ;
        return store ;
    }
    
    public static boolean inMem(Store store)
    {
        String jdbcURL = store.getConnection().getJdbcURL() ;
        return  jdbcURL==null ? false : jdbcURL.contains(":mem:") ;
    }
    
    public static List<Pair<String, StoreDesc>> stores(String fn)
    {
        List<Pair<String, String>> x = storesByQuery(fn) ;
        List<Pair<String, StoreDesc>> z = Iter.iter(x).map(t1).toList() ;
        //List<Pair<String, Store>> z = Iter.iter(x).map(t1).map(t2).toList() ;
        return z ;
    }
    
    public static List<Pair<String, StoreDesc>> storeDesc(String fn)
    {
        List<Pair<String, String>> x = storesByQuery(fn) ;
        List<Pair<String, StoreDesc>> y = Iter.iter(x).map(t1).toList() ;
        return y ;
    }
    
    
    private static List<Pair<String, String>> storesByQuery(String fn)
    {
        Model model = FileManager.get().loadModel(fn) ;
        List<Pair<String, String>> data = new ArrayList<Pair<String, String>>();
        Query query = QueryFactory.create(queryString) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, model) ) {
            ResultSet rs = qExec.execSelect() ;
            
            for ( ; rs.hasNext() ; )
            {
                QuerySolution qs = rs.nextSolution() ;
                String label = qs.getLiteral("label").getLexicalForm() ;
                String desc = qs.getResource("desc").getURI() ;
                data.add(new Pair<String, String>(label, desc)) ;
            }
        }
        return data ;
    }
}
