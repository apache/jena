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

package org.apache.jena.query.spatial.assembler;

import static org.apache.jena.query.spatial.assembler.SpatialVocab.NS;

import java.util.List;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.spatial.EntityDefinition;
import org.apache.jena.query.spatial.SpatialIndexException;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class EntityDefinitionAssembler extends AssemblerBase implements Assembler
{
    
    // V1
    
    /*
<#definition> a spatial:EntityDefinition ;
    spatial:entityField      "uri" ;
    spatial:geoField         "geo" ;
    spatial:hasSpatialPredicatePairs (
         [ spatial:latitude <#latitude_1> ; spatial:longitude <#longitude_1> ]
         [ spatial:latitude <#latitude_2> ; spatial:longitude <#longitude_2> ]
    ) ;
    spatial:hasWKTPredicates (<#wkt_1> <#wkt_2>) ;
    spatial:spatialContextFactory
         "com.spatial4j.core.context.jts.JtsSpatialContextFactory"  .
    */
    
    @Override
    public EntityDefinition open(Assembler a, Resource root, Mode mode)
    {
        String prologue = "PREFIX : <"+NS+">   PREFIX list: <http://jena.hpl.hp.com/ARQ/list#> " ;
        Model model = root.getModel() ;

        String qs1 = StrUtils.strjoinNL(prologue,
                                        "SELECT * {" ,
                                        "  ?definition  :entityField  ?entityField ;" ,
                                        "               :geoField ?geoField" , 
                                        "}") ;
        ParameterizedSparqlString pss = new ParameterizedSparqlString(qs1) ;
        pss.setIri("definition", root.getURI()) ;
        
        Query query1 = QueryFactory.create(pss.toString()) ;
        QueryExecution qexec1 = QueryExecutionFactory.create(query1, model) ;
        ResultSet rs1 = qexec1.execSelect() ;
        List<QuerySolution> results = ResultSetFormatter.toList(rs1) ;
        if ( results.size() == 0 ) {
            //Log.warn(this, "Failed to find a valid EntityDefinition for : "+root) ;
            throw new SpatialIndexException("Failed to find a valid EntityDefinition for : "+root) ;
        }
        
        if ( results.size() !=1 )  {
            Log.warn(this, "Multiple matches for EntityMap for : "+root) ;
            throw new SpatialIndexException("Multiple matches for EntityDefinition for : "+root) ;
        }
        
        QuerySolution qsol1 = results.get(0) ;
        String entityField = qsol1.getLiteral("entityField").getLexicalForm() ;
        String geoField = qsol1.getLiteral("geoField").getLexicalForm() ;
        
        EntityDefinition docDef = new EntityDefinition(entityField, geoField) ;
        
        String qs2 = StrUtils.strjoinNL("SELECT * { ?definition :hasSpatialPredicatePairs [ list:member [ :latitude ?latitude ; :longitude ?longitude ] ]}") ;
        Query query2 = QueryFactory.create(prologue+" "+qs2) ;
        QueryExecution qexec2 = QueryExecutionFactory.create(query2, model, qsol1) ;
        ResultSet rs2 = qexec2.execSelect() ;
        List<QuerySolution> mapEntries = ResultSetFormatter.toList(rs2) ;
        
        for ( QuerySolution qsol : mapEntries ) {
        	Resource latitude = qsol.getResource("latitude") ;
            Resource longitude = qsol.getResource("longitude") ;
            docDef.addSpatialPredicatePair(latitude, longitude);
        }
               
        String qs3 = StrUtils.strjoinNL("SELECT * { ?definition :hasWKTPredicates [ list:member ?wkt ] }") ;
        Query query3 = QueryFactory.create(prologue+" "+qs3) ;
        QueryExecution qexec3 = QueryExecutionFactory.create(query3, model, qsol1) ;
        ResultSet rs3 = qexec3.execSelect() ;
        mapEntries = ResultSetFormatter.toList(rs3) ;
        
        for ( QuerySolution qsol : mapEntries ) {
        	Resource wkt = qsol.getResource("wkt") ;
            docDef.addWKTPredicate(wkt);
        }
        
        String qs4 = StrUtils.strjoinNL("SELECT * { ?definition :spatialContextFactory ?factory }") ;
        Query query4 = QueryFactory.create(prologue+" "+qs4) ;
        QueryExecution qexec4 = QueryExecutionFactory.create(query4, model, qsol1) ;
        ResultSet rs4 = qexec4.execSelect() ;
        List<QuerySolution> results4 = ResultSetFormatter.toList(rs4) ;
        if (results4.size() ==0){
        	return docDef;
        } else if ( results4.size() !=1  )  {
            Log.warn(this, "Multiple matches for SpatialContextFactory for : "+root) ;
            throw new SpatialIndexException("Multiple matches for SpatialContextFactory for : "+root) ;
        } else {
        	QuerySolution qsol4 = results4.get(0);
        	String spatialContextFactory = qsol4.getLiteral("factory").getLexicalForm() ;
        	try {
        		docDef.setSpatialContextFactory(spatialContextFactory);
        	}catch (NoClassDefFoundError e){
        		Log.warn(this, "Custom SpatialContextFactory lib is not ready in classpath:"+ e.getMessage()) ;
        	}
        	return docDef ;
        }  
    }
}

