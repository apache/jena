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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.jena.fuseki.server.FusekiVocab.pAllowedUsers;

import java.util.Collection;
import java.util.List;

import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.graph.GraphUtils;

/**
 * Helper functions use to construct Fuseki servers.
 * @see FusekiConfig
 */
public class FusekiBuilder
{
    /** Build a DataService starting at Resource svc, with the standard (default) set of services. */
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

    /** Add an operation to a {@link DataService} with a given endpoint name */
    public static void addServiceEP(DataService dataService, Operation operation, String endpointName, AuthPolicy requestAuth) {
        dataService.addEndpoint(operation, endpointName, requestAuth) ;
    }

    public static void addServiceEP(DataService dataService, Operation operation, Resource svc, Property property) {
        String p = "<"+property.getURI()+">" ;
        ResultSet rs = FusekiBuildLib.query("SELECT * { ?svc " + p + " ?ep}", svc.getModel(), "svc", svc) ;
        for ( ; rs.hasNext() ; ) {
            QuerySolution soln = rs.next() ;
            AuthPolicy requestAuth = null;
            RDFNode ep = soln.get("ep");
            String epName = null;
            if ( ep.isLiteral() )
                epName = soln.getLiteral("ep").getLexicalForm() ;
            else if ( ep.isResource() ) {
                Resource r = (Resource)ep;
                try {
                    // Look for possible:
                    // [ fuseki:name "" ; fuseki:allowedUsers ( "" "" ) ]
                    epName = r.getProperty(FusekiVocab.pServiceName).getString();
                    List<RDFNode> x = GraphUtils.multiValue(r, FusekiVocab.pAllowedUsers);
                    if ( x.size() > 1 )
                        throw new FusekiConfigException("Multiple fuseki:"+FusekiVocab.pAllowedUsers.getLocalName()+" for "+r); 
                    if ( ! x.isEmpty() )
                        requestAuth = FusekiBuilder.allowedUsers(r);
                } catch(JenaException | ClassCastException ex) {
                    throw new FusekiConfigException("Failed to parse endpoint: "+r);
                }
            } else {
                throw new FusekiConfigException("Unrecognized: "+ep);
            }
            addServiceEP(dataService, operation, epName, requestAuth); 
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

    /** Get the allowed users on a resource.
     *  Returns null if the resource is null or if there were no settings. 
     *  
     * @param resource
     * @return RequestAuthorization
     */
    public static AuthPolicy allowedUsers(Resource resource) {
        if ( resource == null )
            return null;
        Collection<RDFNode> allowedUsers = FusekiBuildLib.getAll(resource, "fu:"+pAllowedUsers.getLocalName());
        if ( allowedUsers == null )
            // Indicate no settings.
            return null;
        // Check all values are simple strings  
        List<String> bad = allowedUsers.stream()
            .map(RDFNode::asNode)
            .filter(rn -> ! Util.isSimpleString(rn))
            .map(rn->rn.toString())
            .collect(toList());
        if ( ! bad.isEmpty() ) {
            //Fuseki.configLog.error(format("User names must be a simple string: bad = %s", bad));
            throw new FusekiConfigException(format("User names should be a simple string: bad = %s", bad));
        }
        // RDFNodes/literals to strings.
        Collection<String> userNames = allowedUsers.stream()
            .map(RDFNode::asNode)
            .map(Node::getLiteralLexicalForm)
            .collect(toList());
        return Auth.policyAllowSpecific(userNames);
    }
}

