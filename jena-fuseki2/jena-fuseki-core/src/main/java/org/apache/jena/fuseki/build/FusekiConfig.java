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

package org.apache.jena.fuseki.build;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.jena.fuseki.build.BuildLib.displayStr;
import static org.apache.jena.fuseki.build.BuildLib.getZeroOrOne;
import static org.apache.jena.fuseki.server.FusekiVocabG.*;
import static org.apache.jena.riot.RDFLanguages.filenameToLang;
import static org.apache.jena.riot.RDFParserRegistry.isRegistered;
import static org.apache.jena.system.G.isResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.JA;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.auth.AuthPolicyList;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.Lang;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.NamedDatasetAssembler;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.G;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;

/** Functions to setup and act on the configuration of a Fuseki server */
public class FusekiConfig {
    private static Logger log = Fuseki.configLog;

    // The default setup of a DataService.
    private static Map<String, Operation> stdRead =
            Map.of("sparql",   Operation.Query,
                   "query",    Operation.Query,
                   "data",     Operation.GSP_R,
                   "get",      Operation.GSP_R);

    private static Map<String, Operation> stdWrite =
            Map.of("sparql",   Operation.Query,
                   "query",    Operation.Query,
                   "update",   Operation.Update,
                   "data",     Operation.GSP_RW,
                   "get",      Operation.GSP_R,
                   "patch",    Operation.Patch);

    private static Set<Operation> stdDatasetRead =
            Set.of(Operation.Query,
                   Operation.GSP_R);

    private static Set<Operation> stdDatasetWrite =
            Set.of(Operation.Query,
                   Operation.Update,
                   Operation.GSP_RW,
                   Operation.Patch);

    /** Convenience operation to populate a {@link DataService} with the conventional default services. */
    public static DataService.Builder populateStdServices(DataService.Builder dataServiceBuilder, boolean allowUpdate) {
        Set<Endpoint> endpoints = new HashSet<>();
        if ( allowUpdate ) {
            stdWrite.forEach((name, op) -> accEndpoint(endpoints, op, name));
            stdDatasetWrite.forEach(op -> accEndpoint(endpoints, op));
        } else {
            stdRead.forEach((name, op) -> accEndpoint(endpoints, op, name));
            stdDatasetRead.forEach(op -> accEndpoint(endpoints, op));
            if ( FusekiExt.extraOperationServicesRead != null )
                FusekiExt.extraOperationServicesRead.forEach((name, op) -> accEndpoint(endpoints, op, name));
        }
        endpoints.forEach(dataServiceBuilder::addEndpoint);
        return dataServiceBuilder;
    }

//    private static void addDataService(DataAccessPointRegistry dataAccessPoints, String name, DataService dataService) {
//        name = DataAccessPoint.canonical(name);
//        if ( dataAccessPoints.isRegistered(name) )
//            throw new FusekiConfigException("Data service name already registered: "+name);
//        DataAccessPoint dap = new DataAccessPoint(name, dataService);
//        dataAccessPoints.register(dap);
//    }
//
//    public static void addDataset(DataAccessPointRegistry dataAccessPoints, String name, DatasetGraph dsg, boolean withUpdate) {
//        name = DataAccessPoint.canonical(name);
//        if ( dataAccessPoints.isRegistered(name) )
//            throw new FusekiConfigException("Data service name already registered: "+name);
//        DataService dataService = buildDataServiceStd(dsg, withUpdate);
//        DataAccessPoint dap = new DataAccessPoint(name, dataService);
//        dataAccessPoints.register(dap);
//    }

    public static DataService buildDataServiceStd(DatasetGraph dsg, boolean withUpdate) {
        return DataService.newBuilder(dsg)
                .withStdServices(withUpdate)
                .build();
    }

    public static void removeDataset(DataAccessPointRegistry dataAccessPoints, String name) {
        name = DataAccessPoint.canonical(name);
        dataAccessPoints.remove(name);
    }

    @Deprecated
    public static AuthPolicy allowedUsers(Resource server) {
        return allowedUsers(server.getModel().getGraph(), server.asNode());
    }

    /** Get the allowed users on a resource.
     *  Returns null if the resource is null or if there were no settings.
     *
     * @return RequestAuthorization
     */
    public static AuthPolicy allowedUsers(Graph graph, Node resource) {
        if ( resource == null )
            return null;
        Collection<Node> allowedUsers = BuildLib.getMultiple(graph, resource, pAllowedUsers);
        if ( allowedUsers == null )
            // Indicate no settings.
            return null;
        // Check all values are simple strings
        List<String> bad = allowedUsers.stream()
            .filter(rn -> ! Util.isSimpleString(rn))
            .map(rn->rn.toString())
            .collect(toList());
        if ( ! bad.isEmpty() ) {
            //Fuseki.configLog.error(format("User names must be a simple string: bad = %s", bad));
            throw new FusekiConfigException(format("User names should be a simple string: bad = %s", bad));
        }
        // RDFNodes/literals to strings.
        Collection<String> userNames = allowedUsers.stream()
            .map(Node::getLiteralLexicalForm)
            .collect(toList());
        return Auth.policyAllowSpecific(userNames);
    }

    /**
     * Process a configuration and return the {@link DataAccessPoint DataAccessPoints};
     * set the context provided for server-wide settings.
     *
     * This bundles together the steps:
     * <ul>
     * <li>{@link #findServer}
     * <li>{@link #parseContext}
     * <li>{@link #processLoadClass} (legacy)
     * <li>{@link #servicesAndDatasets}
     * </ul>
     */
    public static List<DataAccessPoint> processServerConfiguration(Graph configuration, Context context) {
        Node server = findServer(configuration);
        if ( server != null ) {
            Resource rServer = resource(configuration, server);
            mergeContext(configuration,server, context);
            processLoadClass(configuration,server);
        }
        // Process services, whether via server ja:services or, if absent, by finding by type.
        return servicesAndDatasets$(configuration, server);
    }

    /**
     * Process a configuration and return the {@link DataAccessPoint DataAccessPoints};
     * set the context provided for server-wide settings.
     *
     * This bundles together the steps:
     * <ul>
     * <li>{@link #findServer}
     * <li>{@link #parseContext}
     * <li>{@link #processLoadClass} (legacy)
     * <li>{@link #servicesAndDatasets}
     * </ul>
     */
    public static List<DataAccessPoint> processServerConfiguration(Model configuration, Context context) {
        return processServerConfiguration(configuration.getGraph(), context);
    }
    /*package*/ static Resource resource(Graph graph, Node node) {
        Model m = ModelFactory.createModelForGraph(graph);
        RDFNode rNode = m.asRDFNode(node);
        if ( ! rNode.isResource() )
            throw new FusekiConfigException("Not a resource: "+node);
        return rNode.asResource();
    }

    @Deprecated
    public static Resource findServer(Model model) {
        Node server = findServer(model.getGraph());
        if ( server == null )
            return null;
        return resource(model.getGraph(), server);
    }

    /* Find the server resource in a configuration file.
     * Returns null if there isn't one.
     * Raises {@link FusekiConfigException} is there are more than one.
     */
    public static Node findServer(Graph graph) {
        List<Node> servers = G.nodesOfTypeAsList(graph, FusekiVocabG.tServer);
        if ( servers.size() == 0 )
            // "No server" is fine.
            return null;
        if ( servers.size() > 1 )
            throw new FusekiConfigException(servers.size()
                                            + " servers found (must be exactly one in a configuration file)");
        // ---- Server
        Node server = servers.get(0);
        return server;
    }

    /**
     * Process the resource for {@link Context} settings.
     * Return a new {@link Context}
     */
    private static Context parseContext(Graph configuration, Node resource) {
        if ( resource == null )
            return null;
        Resource r = resource(configuration, resource);
        return AssemblerUtils.parseContext(r);
    }

    /**
     * Process the resource for {@link Context} settings
     * and update an existing {@link Context}.
     */
    private static void mergeContext(Graph configuration, Node resource, Context context) {
        if ( resource == null )
            return ;
        Resource r = resource(configuration, resource);
        AssemblerUtils.mergeContext(r, context);
    }

    /**
     * Legacy support for {@code ja:loadClass}
     */
    public static void processLoadClass(Graph configuration, Node server) {
        if ( server == null )
            return;
        List<Node> x = G.listSP(configuration, server, JA.loadClass.asNode());

        for ( Node rn : x ) {
            String className = null;
            if ( rn.isBlank() ) {
                log.warn("Blank node for class to load");
                continue;
            }
            if ( rn.isURI() ) {
                String uri = rn.getURI();
                String javaScheme = "java:";
                if ( !uri.startsWith(javaScheme) ) {
                    log.warn("Class to load is not 'java:': " + uri);
                    continue;
                }
                className = uri.substring(javaScheme.length());
            } else if ( G.isString(rn) )
                className = rn.getLiteralLexicalForm();

            if ( className == null )
                throw new FusekiConfigException("Not a class name: "+displayStr(configuration, rn));
            loadAndInit(className);
        }
    }

    /** Find and process datasets and services in a configuration file.
     * This can be a Fuseki server configuration file or a services-only configuration file.
     * It looks {@code fuseki:services ( .... )} then, if not found, all {@code rtdf:type fuseki:services}.
     * @see #processServerConfiguration
     */
    public static List<DataAccessPoint> servicesAndDatasets(Graph graph) {
        Node server = findServer(graph);
        return servicesAndDatasets$(graph, server);
    }

    @Deprecated
    public static List<DataAccessPoint> servicesAndDatasets(Model model) {
        return servicesAndDatasets(model.getGraph());
    }

    private static List<DataAccessPoint> servicesAndDatasets$(Graph configuration, Node server) {
        DatasetDescriptionMap dsDescMap = new DatasetDescriptionMap();
        NamedDatasetAssembler.sharedDatasetPool.clear();
        // ---- Services
        // Server to services.
        RowSet rs = BuildLib.query("SELECT * { ?s fu:services [ list:member ?service ] }", configuration, "s", server);

        // If none, look for services by type.
        if ( ! rs.hasNext() )
            // No "fu:services ( .... )" so try looking for services directly.
            // This means Fuseki2, service configuration files (no server section) work for --conf.
            rs = BuildLib.query("SELECT ?service { ?service a fu:Service }", configuration);

        List<Node> services = rs.stream().map(b->b.get("service")).toList();
        List<DataAccessPoint> accessPoints = new ArrayList<>();
        for (Node svc : services ) {
            DataAccessPoint acc = buildDataAccessPoint(configuration, svc, dsDescMap);
            if ( acc != null )
                accessPoints.add(acc);
        }
        return accessPoints;
    }

    private static void loadAndInit(String className) {
        try {
            Class<? > classObj = Class.forName(className);
            log.info("Loaded " + className);
            Method initMethod = classObj.getMethod("init");
            initMethod.invoke(null);
        }
        catch (ClassNotFoundException ex) {
            log.warn("Class not found: " + className);
        }
        catch (Exception e) {
            throw new FusekiConfigException(e);
        }
    }

    private static Model readAssemblerFile(String filename) {
        return AssemblerUtils.readAssemblerFile(filename);
    }

    // ---- Directory of assemblers

    /** Read service descriptions in the given directory */
    public static List<DataAccessPoint> readConfigurationDirectory(String dir) {
        Path pDir = Path.of(dir).normalize();
        File dirFile = pDir.toFile();
        if ( ! dirFile.exists() ) {
            log.warn("Not found: directory for assembler files for services: '"+dir+"'");
            return Collections.emptyList();
        }
        if ( ! dirFile.isDirectory() ) {
            log.warn("Not a directory: '"+dir+"'");
            return Collections.emptyList();
        }
        // Files that are not hidden.
        DirectoryStream.Filter<Path> filter = (entry)-> {
            File f = entry.toFile();
            final Lang lang = filenameToLang(f.getName());
            return ! f.isHidden() && f.isFile() && lang != null && isRegistered(lang);
        };

        List<DataAccessPoint> dataServiceRef = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pDir, filter)) {
            for ( Path p : stream ) {
                DatasetDescriptionMap dsDescMap = new DatasetDescriptionMap();
                String fn = IRILib.filenameToIRI(p.toString());
                log.info("Load configuration: "+fn);
                Graph m = readAssemblerFile(fn).getGraph();
                readConfiguration(m, dsDescMap, dataServiceRef);
            }
        } catch (IOException ex) {
            log.warn("IOException:"+ex.getMessage(), ex);
        }
        return dataServiceRef;
    }

    /**
     * Read a configuration in a model.
     * Allow dataset descriptions to be carried over from another place.
     * Add the {@link DataAccessPoint} to a list.
     */
    private static void readConfiguration(Graph configuration, DatasetDescriptionMap dsDescMap, List<DataAccessPoint> dataServiceRef) {
        List<Node> services = G.nodesOfTypeAsList(configuration, FusekiVocabG.fusekiService);

        if ( services.size() == 0 ) {
            log.error("No services found");
            throw new FusekiConfigException();
        }

        for ( Node service : services ) {
            DataAccessPoint acc = buildDataAccessPoint(configuration,service, dsDescMap);
            if ( acc != null )
                dataServiceRef.add(acc);
        }
    }

    @Deprecated
    public static DataAccessPoint buildDataAccessPoint(Resource svc, DatasetDescriptionMap dsDescMap) {
        return buildDataAccessPoint(svc.getModel().getGraph(), svc.asNode(), dsDescMap);
    }

    /** Build a DataAccessPoint, including DataService, from the description at Resource svc */
    public static DataAccessPoint buildDataAccessPoint(Graph configuration, Node fusekiService, DatasetDescriptionMap dsDescMap) {
        Node n = BuildLib.getOne(configuration, fusekiService, FusekiVocabG.pServiceName);
        try {
            if ( ! n.isLiteral() )
                throw new FusekiConfigException("Not a literal for access point name: "+BuildLib.displayStr(configuration, n));
            if ( ! Util.isSimpleString(n) )
                Fuseki.configLog.error(format("Service name '%s' is not a string", BuildLib.strForResource(configuration, n)));

            String name = n.getLiteralLexicalForm();
            name = DataAccessPoint.canonical(name);
            AuthPolicy allowedUsers = allowedUsers(configuration, fusekiService);
            DataService dataService = buildDataService(configuration, fusekiService, dsDescMap).setAuthPolicy(allowedUsers).build();
            DataAccessPoint dataAccess = new DataAccessPoint(name, dataService);
            return dataAccess;
        } catch (FusekiException ex) {
            Fuseki.configLog.error("Skipping: Failed to build service for "+BuildLib.displayStr(configuration, n));
            Fuseki.configLog.error("    "+ex.getMessage(), ex);
            return null;
        }
    }

    private static DataService.Builder buildDataService(Graph configuration, Node fusekiService, DatasetDescriptionMap dsDescMap) {
        Node datasetDesc = BuildLib.getOne(configuration, fusekiService, FusekiVocabG.pDataset);
        DatasetGraph dsg = getDataset(configuration, datasetDesc, dsDescMap);
        DataService.Builder dataService = DataService.newBuilder(dsg);
        Set<Endpoint> endpoints1 = new HashSet<>();
        Set<Endpoint> endpoints2 = new HashSet<>();

        // Old style.
        //    fuseki:serviceQuery "sparql";
        //or
        //    fuseki:serviceQuery [ fuseki:name "sparql" ; fuseki:allowedUsers (..) ];
        accEndpointOldStyle(endpoints1, Operation.Query,    configuration, fusekiService,  pServiceQueryEP);
        accEndpointOldStyle(endpoints1, Operation.Update,   configuration, fusekiService,  pServiceUpdateEP);
        //accEndpointOldStyle(endpoints1, Operation.Upload,   configuration, fusekiService,  pServiceUploadEP);
        accEndpointOldStyle(endpoints1, Operation.GSP_R,    configuration, fusekiService,  pServiceReadGraphStoreEP);
        accEndpointOldStyle(endpoints1, Operation.GSP_RW,   configuration, fusekiService,  pServiceReadWriteGraphStoreEP);

        // ---- Legacy for old style: a request would also try the dataset (i.e. no endpoint name).
        // If "sparql" then allow /dataset?query=
        // Instead, for old style declarations, add new endpoints to put on the dataset
        // Only complication is that authorization is the AND (all say "yes") of named service authorization.
        {
            Collection<Endpoint> endpointsCompat = oldStyleCompat(dataService, endpoints1);
            endpointsCompat.forEach(dataService::addEndpoint);
        }
        // Explicit definition overrides implied by legacy compatibility.
        // Should not happen.
        endpoints1.forEach(dataService::addEndpoint);

        // New (2019) style -- preferred
        //   fuseki:endpoint [ fuseki:operation fuseki:query ; fuseki:name "" ; fuseki:allowedUsers (....) ] ;
        //   and more.

        accFusekiEndpoints(endpoints2, configuration, fusekiService, dsDescMap);
        // This will overwrite old style entries of the same fuseki:name.
        endpoints2.forEach(dataService::addEndpoint);

        return dataService;
    }

    /**
     *  Old style compatibility.
     *  For each endpoint in "endpoints1", ensure there is an endpoint on the dataset (endpoint name "") itself.
     *  Combine the authentication as "AND" of named endpoints authentication.
     */
    private static Collection<Endpoint> oldStyleCompat(DataService.Builder dataService, Set<Endpoint> endpoints1) {
        Map<Operation, Endpoint> endpoints3 = new HashMap<>();
        endpoints1.forEach(ep->{
           Operation operation = ep.getOperation();
           AuthPolicy auth = ep.getAuthPolicy();

           if ( ! StringUtils.isEmpty(ep.getName()) ) {
               if ( endpoints3.containsKey(operation) ) {
                   Endpoint ep1 = endpoints3.get(operation);
                   // Accumulate Authorization.
                   auth = AuthPolicyList.merge(ep1.getAuthPolicy(), auth);
                   Endpoint ep2 = Endpoint.create(ep.getOperation(), "", auth);
                   endpoints3.put(operation, ep2);
               } else {
                   Endpoint ep2 = Endpoint.create(operation, "", auth);
                   endpoints3.put(operation, ep2);
               }
           }
        });
        // Now, after making all legacy endpoints, remove any that are explicit defined in endpoints1.
        // Given the small numbers involved, it is easier to do it this way than
        // additional logic in the first pass over endpoints1.
        endpoints1.stream()
            .filter(ep->StringUtils.isEmpty(ep.getName()))
            .forEach(ep->endpoints3.remove(ep.getOperation()));
        return endpoints3.values();
    }

    /** Find and parse {@code fuseki:endpoint} descriptions. */
    private
    static void accFusekiEndpoints(Set<Endpoint> endpoints, Graph configuration, Node fusekiService, DatasetDescriptionMap dsDescMap) {
        List<Node> endpointsDesc = G.listSP(configuration, fusekiService, pEndpoint);
        endpointsDesc.forEach(ep-> {
            if ( ! isResource(ep) )
                throw new FusekiConfigException("Literal for fuseki:endpoint: expected blank node or resource: "+displayStr(configuration, fusekiService));
            Endpoint endpoint = buildEndpoint(configuration, fusekiService, ep);
            endpoints.add(endpoint);
        });
    }

    /** Parse {@code fuseki:endpoint}
     * <pre>
     * fuseki:endpoint [
     *     fuseki:operation fuseki:Query ;
     *     fuseki:opImplementation <java:package.Class>
     *     fuseki:allowedUsers (....) ;
     *
     *     ja:context [ ja:cxtName "arq:queryTimeout" ;  ja:cxtValue "1000" ] ;
     *     ja:context [ ja:cxtName "arq:queryLimit" ;  ja:cxtValue "10000" ] ;
     *     ja:context [ ja:cxtName "tdb:defaultUnionGraph" ;  ja:cxtValue "true" ] ;
     *
     *     and specials:
     *         fuseki:timeout "1000,1000" ;
     *         fuseki:queryLimit 1000;
     *         arq:unionGraph true;
     *     ] ;
     * </pre>
     */
    private static Endpoint buildEndpoint(Graph configuration, Node fusekiService, Node endpoint) {
        // Endpoints are often blank nodes so use fusekiService in error messages.
        // fuseki:operation
        Node opRef = BuildLib.getOne(configuration, endpoint, pOperation);
        Operation op = null;
        if ( opRef != null ) {
            if ( G.isBlank(opRef) )
                throw exception("Blank node endpoint operation in service %s", displayStr(configuration, fusekiService));
            op = Operation.get(opRef);
        }

        // fuseki:implementation - checking only, not active.
        if ( op == null ) {
            Node rImpl = getZeroOrOne(configuration, endpoint, pImplementation);
            if ( rImpl == null )
                throw exception("No implementation for fuseki:operation '%s' in service %s", displayStr(configuration, opRef), displayStr(configuration, fusekiService));
            // Global registry. Replace existing registry.
            Pair<Operation, ActionService> x = BuildLib.loadOperationActionService(configuration, rImpl);
            Operation op2 = x.getLeft();
            ActionService proc = x.getRight();
            if ( op2 == null )
                throw exception("Failed to load implementation for fuseki:operation '%s' in service %s", displayStr(configuration, opRef), displayStr(configuration, fusekiService));
            op = op2;
            // Using a blank node (!) for the operation means this is safe!
            // OperationRegistry.get().register(op2, proc);
        }

        // fuseki:allowedUsers
        AuthPolicy authPolicy = FusekiConfig.allowedUsers(configuration, endpoint);

        // fuseki:name
        Node epNameN = getZeroOrOne(configuration, endpoint, pEndpointName);
        String epName;
        if ( epNameN == null ) {
//            // Make required to give "" for dataset, not default to dataset if missing.
//            throw exception("No service name for endpoint", fusekiService, ep, pServiceName);
            epName = Endpoint.DatasetEP.string;
        } else {
            if ( ! G.isString(epNameN) )
                throw exception("Not a literal for service name for endpoint", fusekiService, endpoint, pEndpointName);
            epName = epNameN.getLiteralLexicalForm();
        }

        Context cxt = parseContext(configuration, endpoint);

        // Per-endpoint context.
        // Could add special names:
        //   fuseki:timeout
        //   fuseki:queryLimit
        //   fuseki:unionDefaultGraph

        Endpoint ep = Endpoint.create()
            .operation(op)
            // Validates the name.
            .endpointName(epName)
            .authPolicy(authPolicy)
            .context(cxt)
            .build();
        return ep;
    }

    private static FusekiConfigException exception(String fmt, Object...args) {
        String msg = String.format(fmt,  args);
        throw new FusekiConfigException(msg);
    }

    // Old style.
    //    fuseki:serviceQuery "sparql";
    //or
    //    fuseki:serviceQuery [ fuseki:name "sparql" ; fuseki:allowedUsers (..) ];
    private static void accEndpointOldStyle(Collection<Endpoint> endpoints, Operation operation, Graph configuration, Node svc, Node endpointProperty) {

        List<Node> endPts = G.listSP(configuration, svc, endpointProperty);
        endPts.forEach(ep->{
            AuthPolicy authPolicy = null;
            String endpointName = null;
            if ( ep.isLiteral() )
                // fuseki:serviceQuery "sparql"
                endpointName = ep.getLiteralLexicalForm();
            else if ( isResource(ep) ) {
                try {
                    // [ fuseki:name ""; fuseki:allowedUsers ( "" "" ) ]

                    List<Node> named = G.listSP(configuration, ep, FusekiVocabG.pEndpointName);
                    if ( named.isEmpty() )
                        throw new FusekiConfigException("Expected property <"+FusekiVocabG.pEndpointName+"> with <"+endpointProperty.getURI()+"> for "+BuildLib.displayStr(configuration, svc));
                    if ( named.size() > 1 )
                        throw new FusekiConfigException("Multiple property values for <"+FusekiVocabG.pEndpointName+"> with <"+endpointProperty.getURI()+"> for "+BuildLib.displayStr(configuration, svc));
                    endpointName = named.get(0).getLiteralLexicalForm();
                    // Check for multiple
                    List<Node> x = G.listSP(configuration, ep, FusekiVocabG.pAllowedUsers);
                    if ( x.size() > 1 )
                        throw new FusekiConfigException("Multiple fuseki:"+FusekiVocabG.pAllowedUsers.getLocalName()+" for "+displayStr(configuration, ep));
                    // Check
                    if ( ! x.isEmpty() )
                        authPolicy = allowedUsers(configuration, ep);
                } catch(JenaException | ClassCastException ex) {
                    throw new FusekiConfigException("Failed to parse endpoint: "+displayStr(configuration, ep));
                }
            } else {
                throw new FusekiConfigException("Unrecognized: "+ep);
            }

            if ( StringUtils.isEmpty(endpointName) )
                endpointName = null;
            Endpoint endpoint = Endpoint.create(operation, endpointName, authPolicy);
            endpoints.add(endpoint);
        });
    }

    private static void accEndpoint(Collection<Endpoint> endpoints, Operation operation) {
        accEndpoint(endpoints, operation, null);
    }

    private static void accEndpoint(Collection<Endpoint> endpoints, Operation operation, String endpointName) {
        accEndpoint(endpoints, operation, endpointName, null);
    }

    private static void accEndpoint(Collection<Endpoint> endpoints, Operation operation, String endpointName, AuthPolicy authPolicy) {
        if ( StringUtils.isEmpty(endpointName) )
            endpointName = null;
        Endpoint endpoint = Endpoint.create(operation, endpointName, authPolicy);
        endpoints.add(endpoint);
    }

    public static DatasetGraph getDataset(Graph configuration, Node datasetDesc, DatasetDescriptionMap dsDescMap) {
        // check if this one already built
        // This is absolute and does not require a NamedDatasetAssembler and to have a ja:name.
        // ja:name/NamedDatasetAssembler must be used if the service datasets need to
        // wire up sharing of a graph of datasets (not TDB).

        DatasetGraph dsg = dsDescMap.get(datasetDesc);
        if ( dsg != null )
            return dsg;

        // Not seen before.
        // Check if the description is in the model.
        if ( ! G.hasProperty(configuration, datasetDesc, RDF.Nodes.type) )
            throw new FusekiConfigException("No rdf:type for dataset " + displayStr(configuration, datasetDesc));

        // Should have been done already. e.g. ActionDatasets.execPostContainer,
        //AssemblerUtils.addRegistered(datasetDesc.getModel());

        Resource r = resource(configuration, datasetDesc);
        Dataset ds = (Dataset)Assembler.general().open(r);
        if ( ds == null )
            throw new FusekiConfigException("Bad description of a dataset: " + displayStr(configuration, datasetDesc));
        dsg = ds.asDatasetGraph();
        dsDescMap.register(datasetDesc, dsg);
        return dsg;
    }
}
