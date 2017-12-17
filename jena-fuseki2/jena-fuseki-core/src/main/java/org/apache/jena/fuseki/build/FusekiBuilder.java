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

import static java.lang.String.format ;
import static org.apache.jena.fuseki.FusekiLib.nodeLabel ;
import static org.apache.jena.fuseki.FusekiLib.query ;
import static org.apache.jena.fuseki.server.FusekiVocab.pServiceQueryEP;
import static org.apache.jena.fuseki.server.FusekiVocab.pServiceReadGraphStoreEP;
import static org.apache.jena.fuseki.server.FusekiVocab.pServiceReadQuadsEP;
import static org.apache.jena.fuseki.server.FusekiVocab.pServiceReadWriteGraphStoreEP;
import static org.apache.jena.fuseki.server.FusekiVocab.pServiceReadWriteQuadsEP;
import static org.apache.jena.fuseki.server.FusekiVocab.pServiceUpdateEP;
import static org.apache.jena.fuseki.server.FusekiVocab.pServiceUploadEP;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.server.DataAccessPoint ;
import org.apache.jena.fuseki.server.DataService ;
import org.apache.jena.fuseki.server.Operation ;
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

public class FusekiBuilder
{
    /** Build a DataAccessPoint, including DataService, from the description at Resource svc */ 
    public static DataAccessPoint buildDataAccessPoint(Resource svc, DatasetDescriptionRegistry dsDescMap) {
        RDFNode n = FusekiLib.getOne(svc, "fu:name") ;
        if ( ! n.isLiteral() )
            throw new FusekiConfigException("Not a literal for access point name: "+FmtUtils.stringForRDFNode(n));
        Literal object = n.asLiteral() ;

        if ( object.getDatatype() != null && ! object.getDatatype().equals(XSDDatatype.XSDstring) )
            Fuseki.configLog.error(format("Service name '%s' is not a string", FmtUtils.stringForRDFNode(object)));
        String name = object.getLexicalForm() ;
        name = DataAccessPoint.canonical(name) ;

        DataService dataService = buildDataServiceCustom(svc, dsDescMap) ;
        DataAccessPoint dataAccess = new DataAccessPoint(name, dataService) ;
        return dataAccess ;
    }

    /** Build a DatasetRef starting at Resource svc, having the services as described by the descriptions. */
    private static DataService buildDataServiceCustom(Resource svc, DatasetDescriptionRegistry dsDescMap) {
        Resource datasetDesc = ((Resource)getOne(svc, "fu:dataset")) ;
        Dataset ds = getDataset(datasetDesc, dsDescMap);
 
        // In case the assembler included ja:contents
        DataService dataService = new DataService(ds.asDatasetGraph()) ;

        addServiceEP(dataService, Operation.Query,  svc,    pServiceQueryEP) ;
        addServiceEP(dataService, Operation.Update, svc,    pServiceUpdateEP) ;
        addServiceEP(dataService, Operation.Upload, svc,    pServiceUploadEP);
        addServiceEP(dataService, Operation.GSP_R,  svc,    pServiceReadGraphStoreEP) ;
        addServiceEP(dataService, Operation.GSP_RW, svc,    pServiceReadWriteGraphStoreEP) ;

        addServiceEP(dataService, Operation.Quads_R, svc,   pServiceReadQuadsEP) ;
        addServiceEP(dataService, Operation.Quads_RW, svc,  pServiceReadWriteQuadsEP) ;
        
        // Quads - actions directly on the dataset URL are different.
        // In the config file they are also implicit when using GSP.
        
        if ( ! dataService.getEndpoints(Operation.GSP_RW).isEmpty() || ! dataService.getEndpoints(Operation.Quads_RW).isEmpty() ) {
            // ReadWrite available.
            // Dispatch needs introspecting on the HTTP request.
            dataService.addEndpoint(Operation.DatasetRequest_RW, "") ;
        } else if ( ! dataService.getEndpoints(Operation.GSP_R).isEmpty() || ! dataService.getEndpoints(Operation.Quads_R).isEmpty() ) {
            // Read-only available.
            // Dispatch needs introspecting on the HTTP request.
            dataService.addEndpoint(Operation.DatasetRequest_R, "") ;
        }
        
        // XXX 
        // This needs sorting out -- here, it is only on the whole server, not per dataset or even per service.
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
    
    /** Build a DataService starting at Resource svc, with the standard (default) set of services */
    public static DataService buildDataServiceStd(DatasetGraph dsg, boolean allowUpdate) {
        DataService dataService = new DataService(dsg) ;
        populateStdServices(dataService, allowUpdate);
        return dataService ;
    }        
        
    /** Convenience operation to populate a {@link DataService} with the conventional default services. */ 
    public static void populateStdServices(DataService dataService, boolean allowUpdate) {
        addServiceEP(dataService, Operation.Query,      "query") ;
        addServiceEP(dataService, Operation.Query,      "sparql") ;
        if ( ! allowUpdate ) {
            addServiceEP(dataService, Operation.GSP_R,      "data") ;
            addServiceEP(dataService, Operation.DatasetRequest_R,    "") ;
            return;
        }
        addServiceEP(dataService, Operation.GSP_RW,     "data") ;
        addServiceEP(dataService, Operation.GSP_R,      "get") ;
        addServiceEP(dataService, Operation.Update,     "update") ;
        addServiceEP(dataService, Operation.Upload,     "upload") ;
        addServiceEP(dataService, Operation.DatasetRequest_RW, "") ;
    }

    /** Add an operation to a {@link DataService} with a given endpoint name */
    public static void addServiceEP(DataService dataService, Operation operation, String endpointName) {
        dataService.addEndpoint(operation, endpointName) ; 
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

    private static void addServiceEP(DataService dataService, Operation operation, Resource svc, Property property) {
        String p = "<"+property.getURI()+">" ;
        ResultSet rs = query("SELECT * { ?svc " + p + " ?ep}", svc.getModel(), "svc", svc) ;
        for ( ; rs.hasNext() ; ) {
            QuerySolution soln = rs.next() ;
            String epName = soln.getLiteral("ep").getLexicalForm() ;
            addServiceEP(dataService, operation, epName); 
            //log.info("  " + operation.name + " = " + dataAccessPoint.getName() + "/" + epName) ;
        }
    }


}

