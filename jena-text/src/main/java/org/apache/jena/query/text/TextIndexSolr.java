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

package org.apache.jena.query.text;

import java.util.* ;
import java.util.Map.Entry ;

import org.apache.solr.client.solrj.SolrQuery ;
import org.apache.solr.client.solrj.SolrServer ;
import org.apache.solr.client.solrj.SolrServerException ;
import org.apache.solr.client.solrj.response.QueryResponse ;
import org.apache.solr.client.solrj.util.ClientUtils ;
import org.apache.solr.common.SolrDocument ;
import org.apache.solr.common.SolrDocumentList ;
import org.apache.solr.common.SolrInputDocument ;
import org.apache.solr.common.params.CommonParams ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

public class TextIndexSolr implements TextIndex
{
    private static Logger log = LoggerFactory.getLogger(TextIndexSolr.class) ;
    private final SolrServer solrServer ;
    private EntityDefinition docDef ;
    private static int MAX_N    = 10000 ;

    public TextIndexSolr(SolrServer server, EntityDefinition def)
    {
        this.solrServer = server ;
        this.docDef = def ;
    }
    
    @Override
    public void startIndexing()
    {}

    @Override
    public void finishIndexing()
    {
        try { solrServer.commit() ; }
        catch (Exception ex) { exception(ex) ; }
    }

    @Override
    public void abortIndexing()
    {
        try { solrServer.rollback() ; }
        catch (Exception ex) { exception(ex) ; }
    }
    
    @Override
    public void close()
    { 
        if ( solrServer != null ) 
            solrServer.shutdown() ;
    }

    @Override
    public void addEntity(Entity entity)
    {
        //log.info("Add entity: "+entity) ;
        try {
            SolrInputDocument doc = solrDoc(entity) ;
            solrServer.add(doc) ;
        } catch (Exception e) { exception(e) ; }
    }

    private SolrInputDocument solrDoc(Entity entity)
    {
        SolrInputDocument doc = new SolrInputDocument() ;
        doc.addField(docDef.getEntityField(), entity.getId()) ;
        
        String graphField = docDef.getGraphField() ;
        if ( graphField != null )
        {
            doc.addField(graphField, entity.getGraph()) ;
        }
        
        // the addition needs to be done as a partial update
        // otherwise, if we have multiple fields, each successive
        // addition will replace the previous one and we are left
        // with only the last field indexed.
        // see http://stackoverflow.com/questions/12183798/solrj-api-for-partial-document-update
        // and https://svn.apache.org/repos/asf/lucene/dev/trunk/solr/solrj/src/test/org/apache/solr/client/solrj/SolrExampleTests.java
    	HashMap<String,Object> map = new HashMap<>();
        for ( Entry<String, Object> e : entity.getMap().entrySet() ) {
        	map.put("add", e.getValue());
            doc.addField(e.getKey(), map) ;
        }
        return doc ;
    }

    @Override
    public Map<String, Node> get(String uri)
    {
        String escaped = ClientUtils.escapeQueryChars(uri) ;
        String qs = docDef.getEntityField()+":"+escaped ; 
        SolrDocumentList solrResults = solrQuery(qs,1) ;
        
        List<Map<String, Node>> records = process(solrResults) ;
        if ( records.size() == 0 )
            return null ;
        if ( records.size() > 1 )
            log.warn("Multiple docs for one URI: "+uri) ;
        return records.get(0) ;
    }
    
    private List<Map<String, Node>> process(SolrDocumentList solrResults)
    {
        List<Map<String, Node>> records = new ArrayList<>() ;
        
        for ( SolrDocument sd : solrResults )
        {
            Map<String, Node> record = new HashMap<>() ;
            String uriStr = (String)sd.getFieldValue(docDef.getEntityField()) ;
            Node entity = NodeFactory.createURI(uriStr) ;
            record.put(docDef.getEntityField(), entity) ;
            
            for ( String f : docDef.fields() )
            {
                //log.info("Field: "+f) ;
                Object obj = sd.getFieldValue(f) ;
                //log.info("Value: "+obj) ;
                if ( obj == null )
                    continue ;
                // Multivalued -> array.
                // Null means "not stored" or "not present" 
                if ( obj instanceof List<?> )
                {
                    @SuppressWarnings("unchecked")
                    List<String> vals = (List<String>)obj ;
                    continue ;
                }

                String v = (String)obj ;
                Node n = entryToNode(v) ;
                record.put(f, n) ;
            }
            
            //log.info("Entity: "+uriStr) ;
            records.add(record) ;
        }
        return records ;
    }
    
    @Override
    public List<Node> query(String qs) { return query(qs, 0) ; } 
    
    @Override
    public List<Node> query(String qs, int limit)
    {
        SolrDocumentList solrResults = solrQuery(qs, limit) ;
        List<Node> results = new ArrayList<>() ;

        for ( SolrDocument sd : solrResults )
        {
            //** score
            // 'score' field.
            String str = (String)sd.getFieldValue(docDef.getEntityField()) ;
            //log.info("Entity: "+uriStr) ;
            Node n = TextQueryFuncs.stringToNode(str) ;
            results.add(n) ;
        }

        if ( limit > 0 && results.size() > limit )
            results = results.subList(0, limit) ;
        
        return results ; 
    }
    
    private SolrDocumentList solrQuery(String qs, int limit)
    {
        SolrQuery sq = new SolrQuery(qs) ;
        //** score
        //sq.setIncludeScore(true) ;
        if ( limit > 0 )
            sq.setRows(limit) ;
        else 
            sq.setRows(MAX_N) ;   // The Solr default is 10.
        try {
            // Set default field.
            sq.add(CommonParams.DF, docDef.getPrimaryField()) ;
            QueryResponse rsp = solrServer.query( sq ) ;
            SolrDocumentList docs = rsp.getResults();
            return docs ;
        } catch (SolrServerException e) { exception(e) ; return null ; }
    }

    @Override
    public EntityDefinition getDocDef()
    {
        return docDef ;
    }

    private Node entryToNode(String v)
    {
        // TEMP
        return NodeFactoryExtra.createLiteralNode(v, null, null) ;
    }
    
    public SolrServer getServer() { return solrServer ; }

    private static Void exception(Exception ex)
    {
        throw new TextIndexException(ex) ;
    }
}

