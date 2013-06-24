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
import java.util.List ;

import org.apache.jena.atlas.lib.MultiMap ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.text.EntityDefinition ;
import org.apache.jena.query.text.TextIndexException ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;

public class EntityMapAssembler extends AssemblerBase implements Assembler
{
    
    // V1
    
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
                                        "         :defaultField ?dftField" , 
                                        "}") ;
        ParameterizedSparqlString pss = new ParameterizedSparqlString(qs1) ;
        pss.setIri("eMap", root.getURI()) ;
        
        Query query1 = QueryFactory.create(pss.toString()) ;
        QueryExecution qexec1 = QueryExecutionFactory.create(query1, model) ;
        ResultSet rs1 = qexec1.execSelect() ;
        List<QuerySolution> results = ResultSetFormatter.toList(rs1) ;
        if ( results.size() == 0 ) {
            //Log.warn(this, "Failed to find a valid EntityMap for : "+root) ;
            throw new TextIndexException("Failed to find a valid EntityMap for : "+root) ;
        }
        
        if ( results.size() !=1 )  {
            Log.warn(this, "Multiple matches for EntityMap for : "+root) ;
            throw new TextIndexException("Multiple matches for EntityMap for : "+root) ;
        }
        
        QuerySolution qsol1 = results.get(0) ;
        String entityField = qsol1.getLiteral("entityField").getLexicalForm() ;
        
        String defaultField = qsol1.contains("dftField") ? qsol1.getLiteral("dftField").getLexicalForm() : null ;
        
        String qs2 = StrUtils.strjoinNL("SELECT * { ?map list:member [ :field ?field ; :predicate ?predicate ] }") ;
        Query query2 = QueryFactory.create(prologue+" "+qs2) ;
        QueryExecution qexec2 = QueryExecutionFactory.create(query2, model, qsol1) ;
        ResultSet rs2 = qexec2.execSelect() ;
        List<QuerySolution> mapEntries = ResultSetFormatter.toList(rs2) ;
        
        MultiMap<String, Node> mapDefs = MultiMap.createMapList() ; 
        for ( QuerySolution qsol : mapEntries ) {
            String field =  qsol.getLiteral("field").getLexicalForm() ;
            Resource p = qsol.getResource("predicate") ;
            mapDefs.put(field, p.asNode()) ;
        }
        
        // Primary field/predicate
        if ( defaultField != null ) {
            Collection<Node> c = mapDefs.get(defaultField) ;
            if ( c == null )
                throw new TextIndexException("No definition of primary field '"+defaultField+"'") ;
        }
        
        
        EntityDefinition docDef = new EntityDefinition(entityField, defaultField) ;
        for ( String f : mapDefs.keys() ) {
            for ( Node p : mapDefs.get(f)) 
                docDef.set(f, p) ;
        }
        return docDef ;
    }
}

