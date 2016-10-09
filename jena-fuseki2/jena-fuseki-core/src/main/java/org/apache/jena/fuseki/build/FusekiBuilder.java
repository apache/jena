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

package org.apache.jena.fuseki.build;

import static org.apache.jena.fuseki.server.FusekiVocab.* ;
import static java.lang.String.format ;
import static org.apache.jena.fuseki.FusekiLib.nodeLabel ;
import static org.apache.jena.fuseki.FusekiLib.query ;
import org.apache.jena.assembler.Assembler ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.server.DataAccessPoint ;
import org.apache.jena.fuseki.server.DataService ;
import org.apache.jena.fuseki.server.Endpoint ;
import org.apache.jena.fuseki.server.OperationName ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.vocabulary.RDF ;
import org.slf4j.Logger ;
public class FusekiBuilder
{
    private static Logger log = Fuseki.builderLog ;
    
    /** Build a DataAccessPoint, including DataService at Resource svc */ 
    public static DataAccessPoint buildDataAccessPoint(Resource svc, DatasetDescriptionRegistry dsDescMap) {
        RDFNode n = FusekiLib.getOne(svc, "fu:name") ;
        if ( ! n.isLiteral() )
            throw new FusekiConfigException("Not a literal for access point name: "+FmtUtils.stringForRDFNode(n));
        Literal object = n.asLiteral() ;

        if ( object.getDatatype() != null && ! object.getDatatype().equals(XSDDatatype.XSDstring) )
            Fuseki.configLog.error(format("Service name '%s' is not a string", FmtUtils.stringForRDFNode(object)));
        String name = object.getLexicalForm() ;
        name = DataAccessPoint.canonical(name) ;

        DataService dataService = FusekiBuilder.buildDataService(svc, dsDescMap) ;
        DataAccessPoint dataAccess = new DataAccessPoint(name, dataService) ;
        return dataAccess ;
    }

    /** Build a DatasetRef starting at Resource svc */
    private static DataService buildDataService(Resource svc, DatasetDescriptionRegistry dsDescMap) {
        if ( log.isDebugEnabled() ) log.debug("Service: " + nodeLabel(svc)) ;
        Resource datasetDesc = ((Resource)getOne(svc, "fu:dataset")) ;
        Dataset ds = getDataset(datasetDesc, dsDescMap);
 
        // In case the assembler included ja:contents
        DataService dataService = new DataService(ds.asDatasetGraph()) ;

        addServiceEP(dataService, OperationName.Query,  svc,    pServiceQueryEP) ;
        addServiceEP(dataService, OperationName.Update, svc,    pServiceUpdateEP) ;
        addServiceEP(dataService, OperationName.Upload, svc,    pServiceUploadEP);
        addServiceEP(dataService, OperationName.GSP_R,  svc,    pServiceReadGraphStoreEP) ;
        addServiceEP(dataService, OperationName.GSP_RW, svc,    pServiceReadWriteGraphStoreEP) ;

        addServiceEP(dataService, OperationName.Quads_R, svc,   pServiceReadQuadsEP) ;
        addServiceEP(dataService, OperationName.Quads_RW, svc,  pServiceReadWriteQuadsEP) ;
        
        // Quads - actions directly on the dataset URL are different.
        // In the config file they are also implicit when using GSP.
        if ( ! dataService.getOperation(OperationName.GSP_RW).isEmpty() || ! dataService.getOperation(OperationName.Quads_RW).isEmpty() ) {
            dataService.addEndpoint(OperationName.Quads_RW, "") ;
        } else if ( ! dataService.getOperation(OperationName.GSP_R).isEmpty() || ! dataService.getOperation(OperationName.Quads_R).isEmpty() ) {
            dataService.addEndpoint(OperationName.Quads_R, "") ;
        }
        
        // XXX 
//        // Extract timeout overriding configuration if present.
//        if ( svc.hasProperty(FusekiVocab.pAllowTimeoutOverride) ) {
//            sDesc.allowTimeoutOverride = svc.getProperty(FusekiVocab.pAllowTimeoutOverride).getObject().asLiteral().getBoolean() ;
//            if ( svc.hasProperty(FusekiVocab.pMaximumTimeoutOverride) ) {
//                sDesc.maximumTimeoutOverride = (int)(svc.getProperty(FusekiVocab.pMaximumTimeoutOverride).getObject().asLiteral().getFloat() * 1000) ;
//            }
//        }

        return dataService ;
    }
    
    static Dataset getDataset(Resource datasetDesc, DatasetDescriptionRegistry dsDescMap) {
    	// check if this one already built
    	Dataset ds = dsDescMap.get(datasetDesc);
    	if (ds == null) {
    	    // Check if the description is in the model.
            if ( !datasetDesc.hasProperty(RDF.type) )
                throw new FusekiConfigException("No rdf:type for dataset " + nodeLabel(datasetDesc)) ;
            ds = (Dataset)Assembler.general.open(datasetDesc) ;
    	}
    	// Some kind of check that it is "the same" dataset.  
    	// It can be different if two descriptions in different files have the same URI.
    	dsDescMap.register(datasetDesc, ds);
    	return ds;
    }
    
    /** Build a DataService starting at Resource svc */
    public static DataService buildDataService(DatasetGraph dsg, boolean allowUpdate) {
        DataService dataService = new DataService(dsg) ;
        addServiceEP(dataService, OperationName.Query, "query") ;
        addServiceEP(dataService, OperationName.Query, "sparql") ;
        if ( ! allowUpdate ) {
            addServiceEP(dataService, OperationName.GSP_R,      "data") ;
            addServiceEP(dataService, OperationName.Quads_R,    "") ;
            return dataService ;
        }
        addServiceEP(dataService, OperationName.GSP_RW,     "data") ;
        addServiceEP(dataService, OperationName.GSP_R,      "get") ;
        addServiceEP(dataService, OperationName.Update,     "update") ;
        addServiceEP(dataService, OperationName.Upload,     "upload") ;
        addServiceEP(dataService, OperationName.Quads_RW,   "") ;
        return dataService ;
    }

    private static void addServiceEP(DataService dataService, OperationName opName, String epName) {
        dataService.addEndpoint(opName, epName) ; 
    }

    public static RDFNode getOne(Resource svc, String property) {
        String ln = property.substring(property.indexOf(':') + 1) ;
        ResultSet rs = FusekiLib.query("SELECT * { ?svc " + property + " ?x}", svc.getModel(), "svc", svc) ;
        if ( !rs.hasNext() )
            throw new FusekiConfigException("No " + ln + " for service " + FusekiLib.nodeLabel(svc)) ;
        RDFNode x = rs.next().get("x") ;
        if ( rs.hasNext() )
            throw new FusekiConfigException("Multiple " + ln + " for service " + FusekiLib.nodeLabel(svc)) ;
        return x ;
    }

    private static void addServiceEP(DataService dataService, OperationName opName, Resource svc, Property property) {
        String p = "<"+property.getURI()+">" ;
        ResultSet rs = query("SELECT * { ?svc " + p + " ?ep}", svc.getModel(), "svc", svc) ;
        for ( ; rs.hasNext() ; ) {
            QuerySolution soln = rs.next() ;
            String epName = soln.getLiteral("ep").getLexicalForm() ;
            Endpoint operation = new Endpoint(opName, epName) ;
            addServiceEP(dataService, opName, epName); 
            //log.info("  " + opName.name + " = " + dataAccessPoint.getName() + "/" + epName) ;
        }
    }


}

