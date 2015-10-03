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

import java.io.IOException ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
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

public class TextIndexSolr implements TextIndex
{
    private static final Logger log = LoggerFactory.getLogger(TextIndexSolr.class) ;
    private final SolrServer solrServer ;
    private final EntityDefinition docDef ;
    private static final int MAX_N    = 10000 ;

    public TextIndexSolr(SolrServer server, EntityDefinition def) {
        this.solrServer = server ;
        this.docDef = def ;
    }

    @Override
    public void updateEntity(Entity entity) {
        throw new RuntimeException("TextIndexSolr.updateEntity not implemented.") ;
    }

    @Override
    public void prepareCommit() {}

    @Override
    public void commit() {
        try {
            solrServer.commit() ;
        }
        catch (SolrServerException e) {
            throw new TextIndexException(e) ;
        }
        catch (IOException e) {
            throw new TextIndexException(e) ;
        }
    }

    @Override
    public void rollback() {
        try {
            solrServer.rollback() ;
        }
        catch (SolrServerException e) {
            throw new TextIndexException(e) ;
        }
        catch (IOException e) {
            throw new TextIndexException(e) ;
        }
    }

    @Override
    public void close() {
        if ( solrServer != null )
            solrServer.shutdown() ;
    }

    @Override
    public void addEntity(Entity entity) {
        // log.info("Add entity: "+entity) ;
        try {
            SolrInputDocument doc = solrDoc(entity) ;
            solrServer.add(doc) ;
        }
        catch (Exception e) {
            exception(e) ;
        }
    }

    @Override
    public void deleteEntity(Entity entity) {
         //to be implemented
    }

    private SolrInputDocument solrDoc(Entity entity)
    {
        SolrInputDocument doc = new SolrInputDocument() ;
        doc.addField(docDef.getEntityField(), entity.getId()) ;

        String graphField = docDef.getGraphField() ;
        if ( graphField != null ) {
            doc.addField(graphField, entity.getGraph()) ;
        }

        // the addition needs to be done as a partial update
        // otherwise, if we have multiple fields, each successive
        // addition will replace the previous one and we are left
        // with only the last field indexed.
        // see
        // http://stackoverflow.com/questions/12183798/solrj-api-for-partial-document-update
        // and
        // https://svn.apache.org/repos/asf/lucene/dev/trunk/solr/solrj/src/test/org/apache/solr/client/solrj/SolrExampleTests.java
        HashMap<String, Object> map = new HashMap<>() ;
        for ( Entry<String, Object> e : entity.getMap().entrySet() ) {
            map.put("add", e.getValue()) ;
            doc.addField(e.getKey(), map) ;
        }
        return doc ;
    }

    @Override
    public Map<String, Node> get(String uri) {
        String escaped = ClientUtils.escapeQueryChars(uri) ;
        String qs = docDef.getEntityField() + ":" + escaped ;
        SolrDocumentList solrResults = solrQuery(qs, 1) ;

        List<Map<String, Node>> records = process(solrResults) ;
        if ( records.size() == 0 )
            return null ;
        if ( records.size() > 1 )
            log.warn("Multiple docs for one URI: " + uri) ;
        return records.get(0) ;
    }

    private List<Map<String, Node>> process(SolrDocumentList solrResults) {
        List<Map<String, Node>> records = new ArrayList<>() ;

        for ( SolrDocument sd : solrResults ) {
            Map<String, Node> record = new HashMap<>() ;
            String uriStr = (String)sd.getFieldValue(docDef.getEntityField()) ;
            Node entity = NodeFactory.createURI(uriStr) ;
            record.put(docDef.getEntityField(), entity) ;

            for ( String f : docDef.fields() ) {
                // log.info("Field: "+f) ;
                Object obj = sd.getFieldValue(f) ;
                // log.info("Value: "+obj) ;
                if ( obj == null )
                    continue ;
                // Multivalued -> array.
                // Null means "not stored" or "not present"
                if ( obj instanceof List<? > ) {
                    @SuppressWarnings("unchecked")
                    List<String> vals = (List<String>)obj ;
                    continue ;
                }

                String v = (String)obj ;
                Node n = entryToNode(v) ;
                record.put(f, n) ;
            }

            // log.info("Entity: "+uriStr) ;
            records.add(record) ;
        }
        return records ;
    }

    @Override
    public List<TextHit> query(Node property, String qs) { return query(property, qs, 0) ; }

    @Override
    public List<TextHit> query(Node property, String qs, int limit) {
        SolrDocumentList solrResults = solrQuery(qs, limit) ;
        List<TextHit> results = new ArrayList<>() ;

        for ( SolrDocument sd : solrResults ) {
            String str = (String)sd.getFieldValue(docDef.getEntityField()) ;
            // log.info("Entity: "+uriStr) ;
            Node n = TextQueryFuncs.stringToNode(str) ;
            Float score = (Float) sd.getFirstValue("score");
            // capture literal value, if stored
            Node literal = null;
            String field = (property != null) ? docDef.getField(property) : docDef.getPrimaryField();
            String value = (String) sd.getFirstValue(field);
            if (value != null) {
                literal = NodeFactory.createLiteral(value); // FIXME: language and datatype
            }
            TextHit hit = new TextHit(n, score.floatValue(), literal);
            results.add(hit) ;
        }

        if ( limit > 0 && results.size() > limit )
            results = results.subList(0, limit) ;

        return results ;
    }

    private SolrDocumentList solrQuery(String qs, int limit) {
        SolrQuery sq = new SolrQuery(qs) ;
        sq.setIncludeScore(true) ;
        if ( limit > 0 )
            sq.setRows(limit) ;
        else
            sq.setRows(MAX_N) ; // The Solr default is 10.
        try {
            // Set default field.
            sq.add(CommonParams.DF, docDef.getPrimaryField()) ;
            QueryResponse rsp = solrServer.query(sq) ;
            SolrDocumentList docs = rsp.getResults() ;
            return docs ;
        }
        catch (SolrServerException e) {
            exception(e) ;
            return null ;
        }
    }

    @Override
    public EntityDefinition getDocDef() {
        return docDef ;
    }

    private Node entryToNode(String v) {
        // TEMP
        return NodeFactoryExtra.createLiteralNode(v, null, null) ;
    }

    public SolrServer getServer() {
        return solrServer ;
    }

    private static Void exception(Exception ex) {
        throw new TextIndexException(ex) ;
    }
}

