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

import static org.apache.jena.query.text.assembler.TextVocab.NS ;

import java.util.Collection ;
import java.util.HashMap;
import java.util.List ;
import java.util.Map;

import org.apache.jena.atlas.lib.MultiMap ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.text.EntityDefinition ;
import org.apache.jena.query.text.TextIndexException ;
import org.apache.lucene.analysis.Analyzer;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class EntityDefinitionAssembler extends AssemblerBase implements Assembler
{
    /*
<#entMap> a text:EntityMap ;
    text:entityField      "uri" ;
    text:defaultField     "text" ;
    text:map (
         [ text:field "text" ; text:predicate rdfs:label ]
         [ text:field "type" ; text:predicate rdfs:type  ]
         ) .
      */
    
    @Override
    public EntityDefinition open(Assembler a, Resource root, Mode mode)
    {
        String prologue = "PREFIX : <"+NS+">   PREFIX list: <http://jena.hpl.hp.com/ARQ/list#> " ;
        Model model = root.getModel() ;

        String qs1 = StrUtils.strjoinNL(prologue,
                                        "SELECT * {" ,
                                        "  ?eMap  :entityField  ?entityField ;" ,
                                        "         :map ?map ;",
                                        "         :defaultField ?dftField ." ,
                                        "  OPTIONAL {" ,
                                        "    ?eMap :graphField ?graphField" ,
                                        "  }",
                                        "}") ;
        ParameterizedSparqlString pss = new ParameterizedSparqlString(qs1) ;
        pss.setIri("eMap", root.getURI()) ;
        
        Query query1 = QueryFactory.create(pss.toString()) ;
        QueryExecution qexec1 = QueryExecutionFactory.create(query1, model) ;
        ResultSet rs1 = qexec1.execSelect() ;
        List<QuerySolution> results = ResultSetFormatter.toList(rs1) ;
        if ( results.size() == 0 ) {
            Log.warn(this, "Failed to find a valid EntityMap for : "+root) ;
            throw new TextIndexException("Failed to find a valid EntityMap for : "+root) ;
        }
        
        if ( results.size() !=1 )  {
            Log.warn(this, "Multiple matches for EntityMap for : "+root) ;
            throw new TextIndexException("Multiple matches for EntityMap for : "+root) ;
        }
        
        QuerySolution qsol1 = results.get(0) ;
        String entityField = qsol1.getLiteral("entityField").getLexicalForm() ;
        String graphField = qsol1.contains("graphField") ? qsol1.getLiteral("graphField").getLexicalForm() : null;
        String defaultField = qsol1.contains("dftField") ? qsol1.getLiteral("dftField").getLexicalForm() : null ;
              
        MultiMap<String, Node> mapDefs = MultiMap.createMapList() ; 
        Map<String, Analyzer> analyzerDefs = new HashMap<>();
        
        Statement listStmt = root.getProperty(TextVocab.pMap);
        while (listStmt != null) {
        	RDFNode n = listStmt.getObject();
        	if (! n.isResource()) {
        		throw new TextIndexException("Text list node is not a resource : " + n);
        	}
        	Resource listResource = (Resource) n;
        	if (listResource.equals(RDF.nil)) {
        		break;  // end of the list
        	}
        	
        	Statement listEntryStmt = listResource.getProperty(RDF.first);
        	if (listEntryStmt == null) {
        		throw new TextIndexException("Text map list is not well formed.  No rdf:first property");
        	}
        	n = listEntryStmt.getObject();
        	if (! n.isResource()) {
        		throw new TextIndexException("Text map list entry is not a resource : " + n);
        	}
        	Resource listEntry = (Resource) n;
        	
        	Statement fieldStatement = listEntry.getProperty(TextVocab.pField);
        	if (fieldStatement == null) {
        		throw new TextIndexException("Text map entry has no field property");
        	}
        	n = fieldStatement.getObject();
        	if (! n.isLiteral()) {
        		throw new TextIndexException("Text map entry field property has no literal value : " + n);
        	}
        	String field = ((Literal)n).getLexicalForm();
        	
        	Statement predicateStatement = listEntry.getProperty(TextVocab.pPredicate);
        	if (predicateStatement == null) {
        		throw new TextIndexException("Text map entry has no predicate property");
        	}
        	n = predicateStatement.getObject();
        	if (! n.isURIResource()) {
        		throw new TextIndexException("Text map entry predicate property has non resource value : " + n);
        	}
        	Resource predicate = (Resource) n;
        	mapDefs.put(field, predicate.asNode()) ;
        	
        	Statement analyzerStatement = listEntry.getProperty(TextVocab.pAnalyzer);
        	if (analyzerStatement != null) {
        		n = analyzerStatement.getObject();
        		if (! n.isResource()) {
        			throw new TextIndexException("Text map entry analyzer property is not a resource : " + n);
        		}
        		Resource analyzerResource = (Resource) n;
        		Analyzer analyzer = (Analyzer) a.open(analyzerResource);
        		analyzerDefs.put(field, analyzer);
        	}
        	
        	// move on to the next element in the list
        	listStmt = listResource.getProperty(RDF.rest);
        }

        // Primary field/predicate
        if ( defaultField != null ) {
            Collection<Node> c = mapDefs.get(defaultField) ;
            if ( c == null )
                throw new TextIndexException("No definition of primary field '"+defaultField+"'") ;
        }
        
        EntityDefinition docDef = new EntityDefinition(entityField, defaultField, graphField) ;
        for ( String f : mapDefs.keys() ) {
            for ( Node p : mapDefs.get(f)) 
                docDef.set(f, p) ;
        }
        for (String f : analyzerDefs.keySet()) {
        	docDef.setAnalyzer(f, analyzerDefs.get(f));
        }
        return docDef ;
    }
}

