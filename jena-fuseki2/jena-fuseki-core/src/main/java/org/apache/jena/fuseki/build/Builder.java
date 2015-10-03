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
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.vocabulary.RDF ;
import org.slf4j.Logger ;
public class Builder
{
    private static Logger log = Fuseki.builderLog ;
    
    /** Build a DataAccessPoint, including DataServiceat Resource svc */
    public static DataAccessPoint buildDataAccessPoint(Resource svc) {
        RDFNode n = FusekiLib.getOne(svc, "fu:name") ;
        if ( ! n.isLiteral() )
            throw new FusekiConfigException("Not a literal for access point name: "+FmtUtils.stringForRDFNode(n));
        Literal object = n.asLiteral() ;

        if ( object.getDatatype() != null && ! object.getDatatype().equals(XSDDatatype.XSDstring) )
            Fuseki.configLog.error(format("Service name '%s' is not a string", FmtUtils.stringForRDFNode(object)));
        String name = object.getLexicalForm() ;
        name = DataAccessPoint.canonical(name) ;

        DataService dataService = Builder.buildDataService(svc) ;
        DataAccessPoint dataAccess = new DataAccessPoint(name) ;
        dataAccess.setDataService(dataService) ;
        return dataAccess ;
    }

    /** Build a DatasetRef starting at Resource svc */
    public static DataService buildDataService(Resource svc) {
        //log.debug("Service: " + nodeLabel(svc)) ;
        // DO REAL WORK
        Resource datasetDesc = ((Resource)getOne(svc, "fu:dataset")) ;
        
        // Check if it is in the model.
        if ( !datasetDesc.hasProperty(RDF.type) )
            throw new FusekiConfigException("No rdf:type for dataset " + nodeLabel(datasetDesc)) ;
        Dataset ds = (Dataset)Assembler.general.open(datasetDesc) ;
        // In case the assembler included ja:contents
        DataService dataService = new DataService(ds.asDatasetGraph()) ;
        addServiceEP(dataService, OperationName.Query,  svc,    "fu:serviceQuery") ;
        addServiceEP(dataService, OperationName.Update, svc,    "fu:serviceUpdate") ;
        addServiceEP(dataService, OperationName.Upload, svc,    "fu:serviceUpload") ;
        addServiceEP(dataService, OperationName.GSP_R,  svc,    "fu:serviceReadGraphStore") ;
        addServiceEP(dataService, OperationName.GSP,    svc,    "fu:serviceReadWriteGraphStore") ;
        
        if ( ! dataService.getOperation(OperationName.GSP).isEmpty() )
            dataService.addEndpoint(OperationName.Quads, "") ;
        else if ( ! dataService.getOperation(OperationName.GSP_R).isEmpty() )
            dataService.addEndpoint(OperationName.Quads, "") ;
        
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
    
    /** Build a DataService starting at Resource svc */
    public static DataService buildDataService(DatasetGraph dsg, boolean allowUpdate) {
        DataService dataService = new DataService(dsg) ;
        addServiceEP(dataService, OperationName.Query, "query") ;
        addServiceEP(dataService, OperationName.Query, "sparql") ;
        if ( ! allowUpdate ) {
            addServiceEP(dataService, OperationName.Quads, "quads") ;
            addServiceEP(dataService, OperationName.GSP_R, "data") ;
            return dataService ;
        }
        addServiceEP(dataService, OperationName.GSP,    "data") ;
        addServiceEP(dataService, OperationName.Update, "update") ;
        addServiceEP(dataService, OperationName.Upload, "upload") ;
        addServiceEP(dataService, OperationName.Quads,  "") ;
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
    

    private static void addServiceEP(DataService dataService, OperationName opName, Resource svc, String property) {
        ResultSet rs = query("SELECT * { ?svc " + property + " ?ep}", svc.getModel(), "svc", svc) ;
        for ( ; rs.hasNext() ; ) {
            QuerySolution soln = rs.next() ;
            String epName = soln.getLiteral("ep").getLexicalForm() ;
            Endpoint operation = new Endpoint(opName, epName) ;
            addServiceEP(dataService, opName, epName); 
            //log.info("  " + opName.name + " = " + dataAccessPoint.getName() + "/" + epName) ;
        }
    }


}

