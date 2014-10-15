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

package org.apache.jena.query.text.assembler;

import static org.apache.jena.query.text.assembler.TextVocab.pEntityMap ;
import static org.apache.jena.query.text.assembler.TextVocab.pServer ;
import org.apache.jena.query.text.EntityDefinition ;
import org.apache.jena.query.text.TextDatasetFactory ;
import org.apache.jena.query.text.TextIndex ;
import org.apache.jena.query.text.TextIndexException ;
import org.apache.solr.client.solrj.SolrServer ;
import org.apache.solr.client.solrj.impl.HttpSolrServer ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;

public class TextIndexSolrAssembler extends AssemblerBase
{
    /*
    <#index> a :TextIndexSolr ;
        text:server <http://localhost:8983/solr/COLLECTION> ;
        #text:server <embedded:SolrARQ> ;
        text:entityMap <#endMap> ;
        .
    */

    @Override
    public TextIndex open(Assembler a, Resource root, Mode mode) {
        String uri = GraphUtils.getResourceValue(root, pServer).getURI() ;
        SolrServer server ;
        if ( uri.startsWith("embedded:") ) {
            throw new TextIndexException("Embedded Solr server not supported (change code and dependencies to enable)") ;
//            String coreName = uri.substring("embedded:".length()) ;
//            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
//            CoreContainer coreContainer ;
//            try { coreContainer = initializer.initialize() ; } 
//            catch (Exception e) { throw new TextIndexException("Filed to initialize embedded solr", e) ; }
//            server = new EmbeddedSolrServer(coreContainer, coreName);
        } 
        else if ( uri.startsWith("http://") )
            server = new HttpSolrServer( uri );
        else
            throw new TextIndexException("URI for the server must begin 'http://'") ;
        
        Resource r = GraphUtils.getResourceValue(root, pEntityMap) ;
        EntityDefinition docDef = (EntityDefinition)a.open(r) ; 
        return TextDatasetFactory.createSolrIndex(server, docDef) ;
    }
}

