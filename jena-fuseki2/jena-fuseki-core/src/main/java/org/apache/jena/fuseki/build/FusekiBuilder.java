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

import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService ;
import org.apache.jena.fuseki.server.Operation ;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.DatasetGraph ;

/**
 * Helper functions use to construct Fuseki servers.
 * @see FusekiConfig
 */
public class FusekiBuilder
{
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

    public static void addServiceEP(DataService dataService, Operation operation, Resource svc, Property property) {
        String p = "<"+property.getURI()+">" ;
        ResultSet rs = FusekiBuildLib.query("SELECT * { ?svc " + p + " ?ep}", svc.getModel(), "svc", svc) ;
        for ( ; rs.hasNext() ; ) {
            QuerySolution soln = rs.next() ;
            String epName = soln.getLiteral("ep").getLexicalForm() ;
            addServiceEP(dataService, operation, epName); 
            //log.info("  " + operation.name + " = " + dataAccessPoint.getName() + "/" + epName) ;
        }
    }
    
    public static void addDataService(DataAccessPointRegistry dataAccessPoints, String name, DataService dataService) {
        name = DataAccessPoint.canonical(name);
        if ( dataAccessPoints.isRegistered(name) )
            throw new FusekiConfigException("Data service name already registered: "+name);
        DataAccessPoint dap = new DataAccessPoint(name, dataService);
        dataAccessPoints.register(dap);
    }
    
    public static void addDataset(DataAccessPointRegistry dataAccessPoints, String name, DatasetGraph dsg, boolean withUpdate) {
        name = DataAccessPoint.canonical(name);
        if ( dataAccessPoints.isRegistered(name) )
            throw new FusekiConfigException("Data service name already registered: "+name);
        DataAccessPoint dap = buildDataAccessPoint(name, dsg, withUpdate);
        dataAccessPoints.register(dap);
    }
    
    private static DataAccessPoint buildDataAccessPoint(String name, DatasetGraph dsg, boolean withUpdate) { 
        // See Builder. DRY.
        DataService dataService = FusekiBuilder.buildDataServiceStd(dsg, withUpdate);
        DataAccessPoint dap = new DataAccessPoint(name, dataService);
        return dap;
    }
    
    public static void removeDataset(DataAccessPointRegistry dataAccessPoints, String name) {
        name = DataAccessPoint.canonical(name);
        dataAccessPoints.remove(name);
    }
}

