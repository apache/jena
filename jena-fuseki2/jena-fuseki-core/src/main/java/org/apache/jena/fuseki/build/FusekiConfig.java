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
import static org.apache.jena.fuseki.server.FusekiVocab.*;
import static org.apache.jena.riot.RDFLanguages.filenameToLang;
import static org.apache.jena.riot.RDFParserRegistry.isRegistered;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.JA;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.Lang;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;

/** Functions to setup and act onthe configuration of a Fuseki server */  
public class FusekiConfig {
    static { Fuseki.init(); }

    private static Logger log = Fuseki.configLog;
    
    /** Build a DataService starting at Resource svc, with the standard (default) set of services. */
    public static DataService buildDataServiceStd(DatasetGraph dsg, boolean allowUpdate) {
        DataService dataService = new DataService(dsg);
        populateStdServices(dataService, allowUpdate);
        return dataService;
    }

    /** Convenience operation to populate a {@link DataService} with the conventional default services. */
    public static void populateStdServices(DataService dataService, boolean allowUpdate) {
        Set<Endpoint> endpoints = new HashSet<>();
        
        accEndpoint(endpoints, Operation.Query,      "query");
        accEndpoint(endpoints, Operation.Query,      "sparql");
        if ( ! allowUpdate ) {
            accEndpoint(endpoints, Operation.GSP_R,      "data");
        } else {
            accEndpoint(endpoints, Operation.GSP_RW,     "data");
            accEndpoint(endpoints, Operation.GSP_R,      "get");
            accEndpoint(endpoints, Operation.Update,     "update");
            accEndpoint(endpoints, Operation.Upload,     "upload");
        }
        // Dataset
        accEndpoint(endpoints, Operation.Query);
        accEndpoint(endpoints, Operation.GSP_R);
        if ( allowUpdate ) {
            accEndpoint(endpoints, Operation.Update);
            accEndpoint(endpoints, Operation.GSP_RW);
        }

        // Add to DataService.
        endpoints.forEach(dataService::addEndpoint);
    }

    /** Add an operation to a {@link DataService} for the dataset. */
    public static void addDatasetEP(DataService dataService, Operation operation) {
        addDatasetEP(dataService, operation, null);
    }

    /** Add an operation to a {@link DataService} for the dataset. */
    public static void addDatasetEP(DataService dataService, Operation operation, AuthPolicy authPolicy) {
        dataService.addEndpointNoName(operation, authPolicy);
    }

    /** Add an operation to a {@link DataService} with a given endpoint name */
    public static void addServiceEP(DataService dataService, Operation operation, String endpointName) {
        addServiceEP(dataService, operation, endpointName, null);
    }

    /** Add an operation to a {@link DataService} with a given endpoint name */
    public static void addServiceEP(DataService dataService, Operation operation, String endpointName, AuthPolicy authPolicy) {
        if ( StringUtils.isEmpty(endpointName) )
            dataService.addEndpointNoName(operation, authPolicy);
        else
            dataService.addEndpoint(operation, endpointName, authPolicy);
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
        DataService dataService = buildDataServiceStd(dsg, withUpdate);
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
        Collection<RDFNode> allowedUsers = BuildLib.getAll(resource, "fu:"+pAllowedUsers.getLocalName());
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

    /**
     * Process a configuration file and return the {@link DataAccessPoint DataAccessPoints};
     * set the context provided for server-wide settings.
     *
     * This bundles together the steps:
     * <ul>
     * <li>{@link #findServer}
     * <li>{@link #processContext}
     * <li>{@link #processLoadClass} (legacy)
     * <li>{@link #servicesAndDatasets}
     * </ul>
     */
    public static List<DataAccessPoint> processServerConfiguration(Model configuration, Context context) {
        Resource server = findServer(configuration);
        processContext(server, context);
        processLoadClass(server);
        // Process services, whether via server ja:services or, if absent, by finding by type.
        List<DataAccessPoint> x = servicesAndDatasets(configuration);
        return x;
    }

    /**
     * Process a configuration file, starting {@code server}.
     * Return the {@link DataAccessPoint DataAccessPoints}
     * set the context provided for server-wide settings.
     *
     * This bundles together the steps:
     * <ul>
     * <li>{@link #findServer}
     * <li>{@link #processContext}
     * <li>{@link #processLoadClass} (legacy)
     * <li>{@link #servicesAndDatasets}
     * </ul>
     */
    public static List<DataAccessPoint> processServerConfiguration(Resource server, Context context) {
        Objects.requireNonNull(server);
        processContext(server, context);
        processLoadClass(server);
        // Process services, whether via server ja:services or, if absent, by finding by type.
        List<DataAccessPoint> x = servicesAndDatasets(server);
        return x;
    }


    /* Find the server resource in a configuration file.
     * Returns null if there isn't one.
     * Raises {@link FusekiConfigException} is there are more than one.
     */
    public static Resource findServer(Model model) {
        List<Resource> servers = GraphUtils.listResourcesByType(model, FusekiVocab.tServer);
        if ( servers.size() == 0 )
            // "No server" is fine.
            return null;
        if ( servers.size() > 1 )
            throw new FusekiConfigException(servers.size()
                                            + " servers found (must be exactly one in a configuration file)");
        // ---- Server
        Resource server = servers.get(0);
        return server;
    }

    /**
     * Process the configuration file declarations for {@link Context} settings.
     */
    public static void processContext(Resource server, Context cxt) {
        if ( server == null )
            return;
        AssemblerUtils.setContext(server, cxt);
    }

    /**
     * Process any {@code ja:loadClass}
     */
    public static void processLoadClass(Resource server) {
        if ( server == null )
            return;
        StmtIterator sIter = server.listProperties(JA.loadClass);
        for (; sIter.hasNext(); ) {
            Statement s = sIter.nextStatement();
            RDFNode rn = s.getObject();
            String className = null;
            if ( rn instanceof Resource ) {
                String uri = ((Resource)rn).getURI();
                if ( uri == null ) {
                    log.warn("Blank node for class to load");
                    continue;
                }
                String javaScheme = "java:";
                if ( !uri.startsWith(javaScheme) ) {
                    log.warn("Class to load is not 'java:': " + uri);
                    continue;
                }
                className = uri.substring(javaScheme.length());
            }
            if ( rn instanceof Literal )
                className = ((Literal)rn).getLexicalForm();
            /* Loader. */loadAndInit(className);
        }
    }

    /** Find and process datasets and services in a configuration file.
     * This can be a Fuseki server configuration file or a services-only configuration file.
     * It looks {@code fuseki:services ( .... )} then, if not found, all {@code rtdf:type fuseki:services}.
     * @see #processServerConfiguration
     */
    public static List<DataAccessPoint> servicesAndDatasets(Model model) {
        Resource server = findServer(model);
        return servicesAndDatasets$(server, model);
    }

    /** Find and process datasets and services in a configuration file
     * starting from {@code server} which can have a {@code fuseki:services ( .... )}
     * but, if not found, all {@code rtdf:type fuseki:services} are processed.
     */
    public static List<DataAccessPoint> servicesAndDatasets(Resource server) {
        Objects.requireNonNull(server);
        return servicesAndDatasets$(server, server.getModel());
    }

    private static List<DataAccessPoint> servicesAndDatasets$(Resource server, Model model) {
        DatasetDescriptionMap dsDescMap = new DatasetDescriptionMap();
        // ---- Services
        // Server to services.
        ResultSet rs = BuildLib.query("SELECT * { ?s fu:services [ list:member ?service ] }", model, "s", server);
        List<DataAccessPoint> accessPoints = new ArrayList<>();

        // If none, look for services by type.
        if ( ! rs.hasNext() )
            // No "fu:services ( .... )" so try looking for services directly.
            // This means Fuseki2, service configuration files (no server section) work for --conf.
            rs = BuildLib.query("SELECT ?service { ?service a fu:Service }", model);

        // rs is a result set of services to process.
        for (; rs.hasNext(); ) {
            QuerySolution soln = rs.next();
            Resource svc = soln.getResource("service");
            DataAccessPoint acc = buildDataAccessPoint(svc, dsDescMap);
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
        Path pDir = Paths.get(dir).normalize();
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
                Model m = readAssemblerFile(fn);
                readConfiguration(m, dsDescMap, dataServiceRef);
            }
        } catch (IOException ex) {
            log.warn("IOException:"+ex.getMessage(), ex);
        }
        return dataServiceRef;
    }

    /** Read a configuration in a model.
     * Allow dataset descriptions to be carried over from another place.
     * Add to a list.
     */
    private static void readConfiguration(Model m, DatasetDescriptionMap dsDescMap, List<DataAccessPoint> dataServiceRef) {
        List<Resource> services = GraphUtils.listResourcesByType(m, FusekiVocab.fusekiService);

        if ( services.size() == 0 ) {
            log.error("No services found");
            throw new FusekiConfigException();
        }

        for ( Resource service : services ) {
            DataAccessPoint acc = buildDataAccessPoint(service, dsDescMap);
            dataServiceRef.add(acc);
        }
    }

    /** Build a DataAccessPoint, including DataService, from the description at Resource svc */
    public static DataAccessPoint buildDataAccessPoint(Resource svc, DatasetDescriptionMap dsDescMap) {
        RDFNode n = BuildLib.getOne(svc, "fu:name");
        if ( ! n.isLiteral() )
            throw new FusekiConfigException("Not a literal for access point name: "+FmtUtils.stringForRDFNode(n));
        Literal object = n.asLiteral();

        if ( object.getDatatype() != null && ! object.getDatatype().equals(XSDDatatype.XSDstring) )
            Fuseki.configLog.error(format("Service name '%s' is not a string", FmtUtils.stringForRDFNode(object)));

        String name = object.getLexicalForm();
        name = DataAccessPoint.canonical(name);
        DataService dataService = buildDataService(svc, dsDescMap);
        AuthPolicy allowedUsers = allowedUsers(svc);
        dataService.setAuthPolicy(allowedUsers);
        DataAccessPoint dataAccess = new DataAccessPoint(name, dataService);
        return dataAccess;
    }

    /** Build a DatasetRef starting at Resource svc, having the services as described by the descriptions. */
    private static DataService buildDataService(Resource svc, DatasetDescriptionMap dsDescMap) {
        Resource datasetDesc = ((Resource)BuildLib.getOne(svc, "fu:dataset"));

        Dataset ds = getDataset(datasetDesc, dsDescMap);
        DataService dataService = new DataService(ds.asDatasetGraph());
        Set<Endpoint> endpoints = new HashSet<>();
        
        accEndpoint(endpoints, Operation.Query,    svc,  pServiceQueryEP);
        accEndpoint(endpoints, Operation.Update,   svc,  pServiceUpdateEP);
        accEndpoint(endpoints, Operation.Upload,   svc,  pServiceUploadEP);
        accEndpoint(endpoints, Operation.GSP_R,    svc,  pServiceReadGraphStoreEP);
        accEndpoint(endpoints, Operation.GSP_RW,   svc,  pServiceReadWriteGraphStoreEP);

        endpoints.forEach(dataService::addEndpoint);

        // TODO
        // Setting timeout. This needs sorting out -- here, it is only on the whole server, not per dataset or even per service.
//        // Extract timeout overriding configuration if present.
//        if ( svc.hasProperty(FusekiVocab.pAllowTimeoutOverride) ) {
//            sDesc.allowTimeoutOverride = svc.getProperty(FusekiVocab.pAllowTimeoutOverride).getObject().asLiteral().getBoolean();
//            if ( svc.hasProperty(FusekiVocab.pMaximumTimeoutOverride) ) {
//                sDesc.maximumTimeoutOverride = (int)(svc.getProperty(FusekiVocab.pMaximumTimeoutOverride).getObject().asLiteral().getFloat() * 1000);
//            }
//        }
        return dataService;
    }
    
    private static boolean endpointsContains(Collection<Endpoint> endpoints, Operation operation) {
        return endpoints.stream().anyMatch(ep->operation.equals(ep.getOperation()));
    }

    private static void accEndpoint(Collection<Endpoint> endpoints, Operation operation, Resource svc, Property property) {
        String p = "<"+property.getURI()+">";
        ResultSet rs = BuildLib.query("SELECT * { ?svc " + p + " ?ep}", svc.getModel(), "svc", svc);
        for (; rs.hasNext(); ) {
            QuerySolution soln = rs.next();
            // No policy yet - set below if one is found.
            AuthPolicy authPolicy = null;
            RDFNode ep = soln.get("ep");
            String epName = null;
            if ( ep.isLiteral() )
                epName = soln.getLiteral("ep").getLexicalForm();
            else if ( ep.isResource() ) {
                Resource r = (Resource)ep;
                try {
                    // Look for possible:
                    // [ fuseki:name ""; fuseki:allowedUsers ( "" "" ) ]
                    epName = r.getProperty(FusekiVocab.pServiceName).getString();
                    List<RDFNode> x = GraphUtils.multiValue(r, FusekiVocab.pAllowedUsers);
                    if ( x.size() > 1 )
                        throw new FusekiConfigException("Multiple fuseki:"+FusekiVocab.pAllowedUsers.getLocalName()+" for "+r);
                    if ( ! x.isEmpty() )
                        authPolicy = allowedUsers(r);
                } catch(JenaException | ClassCastException ex) {
                    throw new FusekiConfigException("Failed to parse endpoint: "+r);
                }
            } else {
                throw new FusekiConfigException("Unrecognized: "+ep);
            }
            
            if ( StringUtils.isEmpty(epName) )
                epName = null;
            Endpoint endpoint = new Endpoint(operation, epName, authPolicy);
            endpoints.add(endpoint);
        }
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
        Endpoint endpoint = new Endpoint(operation, endpointName, authPolicy);
        endpoints.add(endpoint);
    }

    public static Dataset getDataset(Resource datasetDesc, DatasetDescriptionMap dsDescMap) {
        // check if this one already built
        Dataset ds = dsDescMap.get(datasetDesc);
        if (ds == null) {
            // Check if the description is in the model.
            if ( !datasetDesc.hasProperty(RDF.type) )
                throw new FusekiConfigException("No rdf:type for dataset " + BuildLib.nodeLabel(datasetDesc));

            // Should have been done already. e.g. ActionDatasets.execPostContainer,
            // Assemblerutils.readAssemblerFile < FusekiServer.parseConfigFile.
            //AssemblerUtils.addRegistered(datasetDesc.getModel());
            ds = (Dataset)Assembler.general.open(datasetDesc);
        }
        // Some kind of check that it is "the same" dataset.
        // It can be different if two descriptions in different files have the same URI.
        dsDescMap.register(datasetDesc, ds);
        return ds;
    }


    // ---- System database
    /** Read the system database */
    public static List<DataAccessPoint> readSystemDatabase(Dataset ds) {
        // Webapp only.
        DatasetDescriptionMap dsDescMap = new DatasetDescriptionMap();
        String qs = StrUtils.strjoinNL
            (FusekiPrefixes.PREFIXES ,
             "SELECT * {" ,
             "  GRAPH ?g {",
             "     ?s fu:name ?name;" ,
             "        fu:status ?status ." ,
             "  }",
             "}"
             );

        List<DataAccessPoint> refs = new ArrayList<>();

        ds.begin(ReadWrite.WRITE);
        try {
            ResultSet rs = BuildLib.query(qs, ds);

    //        ResultSetFormatter.out(rs);
    //        ((ResultSetRewindable)rs).reset();

            for (; rs.hasNext(); ) {
                QuerySolution row = rs.next();
                Resource s = row.getResource("s");
                Resource g = row.getResource("g");
                Resource rStatus = row.getResource("status");
                //String name = row.getLiteral("name").getLexicalForm();
                DataServiceStatus status = DataServiceStatus.status(rStatus);

                Model m = ds.getNamedModel(g.getURI());
                // Rebase the resource of the service description to the containing graph.
                Resource svc = m.wrapAsResource(s.asNode());
                DataAccessPoint ref = buildDataAccessPoint(svc, dsDescMap);
                refs.add(ref);
            }
            ds.commit();
            return refs;
        } finally { ds.end(); }
    }
}
