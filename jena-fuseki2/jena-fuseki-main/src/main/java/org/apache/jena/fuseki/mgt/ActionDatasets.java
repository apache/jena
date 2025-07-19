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

package org.apache.jena.fuseki.mgt;

import static java.lang.String.format;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.build.DatasetDescriptionMap;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.fuseki.ctl.ActionContainerItem;
import org.apache.jena.fuseki.ctl.JsonDescription;
import org.apache.jena.fuseki.metrics.MetricsProvider;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.fuseki.servlets.ActionLib;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.system.G;
import org.apache.jena.tdb1.TDB1;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.web.HttpSC;

public class ActionDatasets extends ActionContainerItem {
    static private Property pServiceName = FusekiVocab.pServiceName;

    private static final String paramDatasetName    = "dbName";
    private static final String paramDatasetType    = "dbType";
    private static final String tDatabaseTDB        = "tdb";
    private static final String tDatabaseTDB2       = "tdb2";
    private static final String tDatabaseMem        = "mem";

    public ActionDatasets() { super(); }

    @Override
    public void validate(HttpAction action) { }

    // ---- GET : return details of dataset or datasets.
    @Override
    protected JsonValue execGetContainer(HttpAction action) {
        action.log.info(format("[%d] GET datasets", action.id));
        JsonBuilder builder = new JsonBuilder();
        builder.startObject("D");
        builder.key(ServerConst.datasets);
        JsonDescription.arrayDatasets(builder, action.getDataAccessPointRegistry());
        builder.finishObject("D");
        return builder.build();
    }

    @Override
    protected JsonValue execGetItem(HttpAction action) {
        String item = getItemDatasetName(action);
        action.log.info(format("[%d] GET dataset %s", action.id, item));
        JsonBuilder builder = new JsonBuilder();
        DataAccessPoint dsDesc = getItemDataAccessPoint(action, item);
        if ( dsDesc == null )
            ServletOps.errorNotFound("Not found: dataset "+item);
        JsonDescription.describe(builder, dsDesc);
        return builder.build();
    }

    // ---- POST

    /** Create dataset */
    @Override
    protected JsonValue execPostContainer(HttpAction action) {
        UUID uuid = UUID.randomUUID();

        ContentType ct = ActionLib.getContentType(action);

        boolean hasParams = action.getRequestParameterNames().hasMoreElements();

        if ( ct == null && ! hasParams )
            ServletOps.errorBadRequest("Bad request - Content-Type or both parameters dbName and dbType required");

        boolean succeeded = false;
        // Used in clear-up.
        String configFile = null;
        String systemFileCopy = null;

        FusekiServerCtl serverCtl = FusekiServerCtl.get(action.getServletContext());
        DatasetDescriptionMap registry = new DatasetDescriptionMap();

        synchronized (serverCtl.getServerlock()) {
            try {
                // Get the request input.
                Model modelFromRequest = ModelFactory.createDefaultModel();
                StreamRDF dest = StreamRDFLib.graph(modelFromRequest.getGraph());

                boolean templatedRequest = false;

                try {
                    if ( hasParams || WebContent.isHtmlForm(ct) ) {
                        assemblerFromForm(action, dest);
                        templatedRequest = true;
                        // dbName, dbType
                    } else if ( WebContent.isMultiPartForm(ct) ) {
                        // Cannot be enabled.
                        ServletOps.errorBadRequest("Service configuration from a multipart upload not supported");
                        //assemblerFromUpload(action, dest);
                    } else {
                        if ( ! FusekiAdmin.allowConfigFiles() )
                            ServletOps.errorBadRequest("Service configuration from an upload file not supported");
                        assemblerFromBody(action, dest);
                    }
                } catch (RiotException ex) {
                    ActionLib.consumeBody(action);
                    action.log.warn(format("[%d] Failed to read configuration: %s", action.id, ex.getMessage()));
                    ServletOps.errorBadRequest("Failed to read configuration");
                }

                // ----
                // Add the dataset and graph wiring for assemblers
                Model model = ModelFactory.createDefaultModel();
                model.add(modelFromRequest);
                model = AssemblerUtils.prepareForAssembler(model);

                // ----
                // Process configuration.
                // Returns the "service fu:name NAME" statement
                Statement stmt = findService(model);
                if ( stmt == null ) {
                    action.log.warn(format("[%d] No service name", action.id));
                    ServletOps.errorBadRequest(format("No service name"));
                }

                Resource subject = stmt.getSubject();
                Literal object = stmt.getObject().asLiteral();

                if ( object.getDatatype() != null && ! object.getDatatype().equals(XSDDatatype.XSDstring) )
                    action.log.warn(format("[%d] Service name '%s' is not a string", action.id, FmtUtils.stringForRDFNode(object)));

                final String datasetPath;
                {
                    String datasetName = object.getLexicalForm();
                    // This duplicates the code FusekiBuilder.buildDataAccessPoint to give better error messages and HTTP status code."

                    // ---- Check and canonicalize name.
                    // Various explicit check for better error messages.

                    if ( datasetName.isEmpty() ) {
                        action.log.warn(format("[%d] Empty dataset name", action.id));
                        ServletOps.errorBadRequest("Empty dataset name");
                    }
                    if ( StringUtils.isBlank(datasetName) ) {
                        action.log.warn(format("[%d] Whitespace dataset name: '%s'", action.id, datasetName));
                        ServletOps.errorBadRequest(format("Whitespace dataset name: '%s'", datasetName));
                    }
                    if ( datasetName.contains(" ") ) {
                        action.log.warn(format("[%d] Bad dataset name (contains spaces) '%s'", action.id, datasetName));
                        ServletOps.errorBadRequest(format("Bad dataset name (contains spaces) '%s'", datasetName));
                    }
                    if ( datasetName.equals("/") ) {
                        action.log.warn(format("[%d] Bad dataset name '%s'", action.id, datasetName));
                        ServletOps.errorBadRequest(format("Bad dataset name '%s'", datasetName));
                    }

                    // The service names must be a valid URI path
                    try {
                        ValidString validServiceName = Validators.serviceName(datasetName);
                    } catch (FusekiConfigException ex) {
                        action.log.warn(format("[%d] Invalid service name: '%s'", action.id, datasetName));
                        ServletOps.error(HttpSC.BAD_REQUEST_400, format("Invalid service name: '%s'", datasetName));
                    }

                    // Canonical - starts with "/",does not end in "/"
                    datasetPath = DataAccessPoint.canonical(datasetName);

                    // For this operation, check additionally that the path does not go outside the expected file area.
                    // This imposes the path component-only rule and does not allow ".."
                    if ( ! isValidServiceName(datasetPath) ) {
                        action.log.warn(format("[%d] Database service name not acceptable: '%s'", action.id, datasetName));
                        ServletOps.error(HttpSC.BAD_REQUEST_400, format("Database service name not acceptable: '%s'", datasetName));
                    }
                }

                // ---- Check whether it already exists
                if ( action.getDataAccessPointRegistry().isRegistered(datasetPath) ) {
                    action.log.warn(format("[%d] Name already registered '%s'", action.id, datasetPath));
                    ServletOps.error(HttpSC.CONFLICT_409, format("Name already registered '%s'", datasetPath));
                }

                // -- Validate any TDB locations.
                // If this is a templated request, there is no need to do this
                // because the location is "datasetPath" which has been checked.
                if ( ! templatedRequest ) {
                    List<String> tdbLocations = tdbLocations(action, model.getGraph());
                    for(String tdbLocation : tdbLocations ) {
                        if ( ! isValidTDBLocation(tdbLocation) ) {
                            action.log.warn(format("[%d] TDB database location not acceptable: '%s'", action.id, tdbLocation));
                            ServletOps.error(HttpSC.BAD_REQUEST_400, format("TDB database location not acceptable: '%s'", tdbLocation));
                        }
                    }
                }

                // ----
                // Keep a persistent copy with a globally unique name.
                // This is not used for anything other than being "for the record".
                systemFileCopy = FusekiServerCtl.dirSystemFileArea.resolve(uuid.toString()).toString();
                RDFWriter.source(modelFromRequest).lang(Lang.TURTLE).output(systemFileCopy);

                // ----
                action.log.info(format("[%d] Create database : name = %s", action.id, datasetPath));

                List<String> existing = FusekiServerCtl.existingConfigurationFile(datasetPath);
                if ( ! existing.isEmpty() )
                    ServletOps.error(HttpSC.CONFLICT_409, "Configuration file for '"+datasetPath+"' already exists");

                configFile = FusekiServerCtl.generateConfigurationFilename(datasetPath);

                // ---- Build the service
                DataAccessPoint dataAccessPoint = FusekiConfig.buildDataAccessPoint(subject.getModel().getGraph(), subject.asNode(), registry);
                if ( dataAccessPoint == null ) {
                    FmtLog.error(action.log, "Failed to build DataAccessPoint: datasetPath = %s; DataAccessPoint name = %s", datasetPath, dataAccessPoint);
                    ServletOps.errorBadRequest("Failed to build DataAccessPoint");
                    return null;
                }
                dataAccessPoint.getDataService().setEndpointProcessors(action.getOperationRegistry());

                // Write to configuration directory (without assembler additional details).
                RDFWriter.source(modelFromRequest).lang(Lang.TURTLE).output(configFile);

                if ( ! datasetPath.equals(dataAccessPoint.getName()) )
                    FmtLog.warn(action.log, "Inconsistent names: datasetPath = %s; DataAccessPoint name = %s", datasetPath, dataAccessPoint);

                dataAccessPoint.getDataService().goActive();
                succeeded = true;

                // At this point, a server restarting will find the new service.
                // This next line makes it dispatchable in this running server.
                action.getDataAccessPointRegistry().register(dataAccessPoint);

                // Add to metrics
                MetricsProvider metricProvider = action.getMetricsProvider();
                if ( metricProvider != null )
                    action.getMetricsProvider().addDataAccessPointMetrics(dataAccessPoint);

                action.setResponseContentType(WebContent.contentTypeTextPlain);
                ServletOps.success(action);
            } finally {
                // Clear-up on failure.
                if ( ! succeeded ) {
                    if ( systemFileCopy != null ) FileOps.deleteSilent(systemFileCopy);
                    if ( configFile != null ) FileOps.deleteSilent(configFile);
                }
            }
            return null;
        }
    }

    /**
     * Check whether a service name is acceptable.
     * A service name is used as a filesystem path component,
     * except it may have a leading "/"., to store the database and the configuration.
     * <p>
     * The canonical name for a service (see {@link DataAccessPoint#canonical})
     * starts with a "/" and this will be added if necessary.
     */
    private boolean isValidServiceName(String datasetPath) {
        // Leading "/" is OK , nowhere else is.
        int idx = datasetPath.indexOf('/', 1);
        if ( idx > 0 )
            return false;
        // No slash, except maybe at the start so a meaningful use of .. can only be at the start.
        if ( datasetPath.startsWith("/.."))
            return false;
        // Character restrictions done by Validators.serviceName
        return true;
    }

    // This works for TDB1 as well.
    private boolean isValidTDBLocation(String tdbLocation) {
        Location location = Location.create(tdbLocation);
        if ( location.isMem() )
            return true;
        // No ".."
        if (tdbLocation.startsWith("..") || tdbLocation.contains("/..") ) {
            // That test was too strict.
            List<String> components = FileOps.pathComponents(tdbLocation);
            if ( components.contains("..") )
                return false;
        }
        return true;
    }

    /** Find the service resource. There must be only one in the configuration. */
    private Statement findService(Model model) {
        // Try to find by unique pServiceName (max backwards compatibility)
        // then try to find by rdf:type fuseki:Service.

        Statement stmt = getOne(model, null, pServiceName, null);
        // null means 0 or many, not one.

        if ( stmt == null ) {
            // This calculates { ?x rdf:type fu:Service ; ?x fu:name ?name }
            // One and only one service.
            Statement stmt2 = getOne(model, null, RDF.type, FusekiVocab.fusekiService);
            if ( stmt2 == null ) {
                int count = model.listStatements(null, RDF.type, FusekiVocab.fusekiService).toList().size();
                if ( count == 0 )
                    ServletOps.errorBadRequest("No triple rdf:type fuseki:Service found");
                else
                    ServletOps.errorBadRequest("Multiple Fuseki service descriptions");
            }
            Statement stmt3 = getOne(model, stmt2.getSubject(), pServiceName, null);
            if ( stmt3 == null ) {
                StmtIterator sIter = model.listStatements(stmt2.getSubject(), pServiceName, (RDFNode)null );
                if ( ! sIter.hasNext() )
                    ServletOps.errorBadRequest("No name given in description of Fuseki service");
                sIter.next();
                if ( sIter.hasNext() )
                    ServletOps.errorBadRequest("Multiple names given in description of Fuseki service");
                throw new InternalErrorException("Inconsistent: getOne didn't fail the second time");
            }
            stmt = stmt3;
        }

        if ( stmt == null )
            return null;

        if ( ! stmt.getObject().isLiteral() )
            ServletOps.errorBadRequest("Found "+FmtUtils.stringForRDFNode(stmt.getObject())+" : Service names are strings, which are then used to build the external URI");

        return stmt;
    }

    @Override
    protected JsonValue execPostItem(HttpAction action) {
        String name = getItemDatasetName(action);
        if ( name == null )
            name = "''";
        action.log.info(format("[%d] POST dataset %s", action.id, name));

        // Not in the action - this not an ActionService.
        DataAccessPoint dap = getItemDataAccessPoint(action, name);

        if ( dap == null )
            ServletOps.errorNotFound("Not found: dataset "+name);

        DataService dSrv = dap.getDataService();
        if ( dSrv == null )
            // If not set explicitly, take from DataAccessPoint
            dSrv = action.getDataAccessPoint().getDataService();

        String s = action.getRequestParameter("state");
        if ( s == null || s.isEmpty() )
            ServletOps.errorBadRequest("No state change given");
        return null;
    }

    // ---- DELETE

    @Override
    protected void execDeleteItem(HttpAction action) {
        // Does not exist?
        String name = getItemDatasetName(action);
        if ( name == null )
            name = "";
        action.log.info(format("[%d] DELETE dataset=%s", action.id, name));

        if ( ! action.getDataAccessPointRegistry().isRegistered(name) )
            ServletOps.errorNotFound("No such dataset registered: "+name);

        boolean succeeded = false;
        FusekiServerCtl serverCtl = FusekiServerCtl.get(action.getServletContext());

        synchronized(serverCtl.getServerlock()) {
            try {
                // Redo check inside transaction.
                DataAccessPoint ref = action.getDataAccessPointRegistry().get(name);
                if ( ref == null )
                    ServletOps.errorNotFound("No such dataset registered: "+name);

                // Get a reference before removing.
                DataService dataService = ref.getDataService();

                // Remove from the registry - operation dispatch will not find it any more.
                action.getDataAccessPointRegistry().remove(name);

                // Find the configuration.
                String filename = name.startsWith("/") ? name.substring(1) : name;
                List<String> configurationFiles = FusekiServerCtl.existingConfigurationFile(filename);

                if ( configurationFiles.isEmpty() ) {
                    // -- Unmanaged
                    action.log.warn(format("[%d] Can't delete database configuration - not a managed database", action.id, name));
//                ServletOps.errorOccurred(format("Can't delete database - not a managed configuration", name));
                    succeeded = true;
                    ServletOps.success(action);
                    return;
                }

                if  ( configurationFiles.size() > 1 ) {
                    // -- This should not happen.
                    action.log.warn(format("[%d] There are %d configuration files, not one.", action.id, configurationFiles.size()));
                    ServletOps.errorOccurred(format("There are %d configuration files, not one. Delete not performed; manual clean up of the filesystem needed.",
                                                    configurationFiles.size()));
                    return;
                }

                // -- Remove managed database.
                String cfgPathname = configurationFiles.get(0);

                // Delete configuration file.
                // Once deleted, server restart will not have the database.
                FileOps.deleteSilent(cfgPathname);

                // Delete the database for real only if it is in the server
                // "run/databases" area. Don't delete databases that reside
                // elsewhere. We have already deleted the configuration file, so the
                // databases will not be associated with the server anymore.

                @SuppressWarnings("removal")
                boolean isTDB1 = org.apache.jena.tdb1.sys.TDBInternal.isTDB1(dataService.getDataset());
                boolean isTDB2 = org.apache.jena.tdb2.sys.TDBInternal.isTDB2(dataService.getDataset());

                try {
                    dataService.shutdown();
                } catch (JenaException ex) {
                    return;
                }
                // JENA-1481: Really delete files.
                if ( ( isTDB1 || isTDB2 ) ) {
                    // Delete databases created by the UI, or the admin operation, which are
                    // in predictable, unshared locations on disk.
                    // There may not be any database files, the in-memory case.
                    // (TDB supports an in-memory mode.)
                    Path pDatabase = FusekiServerCtl.dirDatabases.resolve(filename);
                    if ( Files.exists(pDatabase)) {
                        try {
                            if ( Files.isSymbolicLink(pDatabase)) {
                                action.log.info(format("[%d] Database is a symbolic link, not removing files", action.id, pDatabase));
                            } else {
                                IO.deleteAll(pDatabase);
                                action.log.info(format("[%d] Deleted database files %s", action.id, pDatabase));
                            }
                        } catch (RuntimeIOException ex) {
                            action.log.error(format("[%d] Error while deleting database files %s: %s", action.id, pDatabase, ex.getMessage()), ex);
                            // But we have managed to remove it from the running server, and removed its configuration, so declare victory.
                        }
                    }
                }

                succeeded = true;
                ServletOps.success(action);
            } finally {
                // No clearup needed
            }
        }
    }

    private static void assemblerFromBody(HttpAction action, StreamRDF dest) {
        bodyAsGraph(action, dest);
    }

    private static Map<String, String> dbTypeToTemplate = new HashMap<>();
    static {
        dbTypeToTemplate.put(tDatabaseTDB,  Template.templateTDB1_FN);
        dbTypeToTemplate.put(tDatabaseTDB2, Template.templateTDB2_FN);
        dbTypeToTemplate.put(tDatabaseMem,  Template.templateTIM_MemFN);
    }

    private static void assemblerFromForm(HttpAction action, StreamRDF dest) {
        String dbType = action.getRequestParameter(paramDatasetType);
        String dbName = action.getRequestParameter(paramDatasetName);
        // Test for null, empty or only whitespace.
        if ( StringUtils.isBlank(dbType) || StringUtils.isBlank(dbName) ) {
            action.log.warn(format("[%d] Both parameters 'dbName' and 'dbType' required and not be blank", action.id));
            ServletOps.errorBadRequest("Received HTML form. Both parameters 'dbName' and 'dbType' required");
        }

        Map<String, String> params = new HashMap<>();
        params.put(Template.NAME, dbName);

        FusekiServerCtl serverCtl = FusekiServerCtl.get(action.getServletContext());
        if ( serverCtl != null )
            serverCtl.addGlobals(params);
        else {
            ServletOps.errorOccurred("No admin area");
            // No return.
        }

        // -- Get the template
        String template = dbTypeToTemplate.get(dbType.toLowerCase(Locale.ROOT));
        if ( template == null ) {
            List<String> keys = new ArrayList<>(dbTypeToTemplate.keySet());
            Collections.sort(keys);
            ServletOps.errorBadRequest(format("dbType can be only one of %s", keys));
        }

        String instance = TemplateFunctions.templateFile(serverCtl.getFusekiBase(), template, params, Lang.TTL);
        RDFParser.create().source(new StringReader(instance)).base("http://base/").lang(Lang.TTL).parse(dest);
    }

    private static void assemblerFromUpload(HttpAction action, StreamRDF dest) {
        throw new NotImplemented();
        //DataUploader.incomingData(action, dest);
    }

    // ---- Auxiliary functions

    private static Quad getOne(DatasetGraph dsg, Node g, Node s, Node p, Node o) {
        Iterator<Quad> iter = dsg.findNG(g, s, p, o);
        if ( ! iter.hasNext() )
            return null;
        Quad q = iter.next();
        if ( iter.hasNext() )
            return null;
        return q;
    }

    private static Statement getOne(Model m, Resource s, Property p, RDFNode o) {
        StmtIterator iter = m.listStatements(s, p, o);
        if ( ! iter.hasNext() )
            return null;
        Statement stmt = iter.next();
        if ( iter.hasNext() )
            return null;
        return stmt;
    }

    private static void bodyAsGraph(HttpAction action, StreamRDF dest) {
        HttpServletRequest request = action.getRequest();
        String base = ActionLib.wholeRequestURL(request);
        ContentType ct = FusekiNetLib.getContentType(request);
        Lang lang = RDFLanguages.contentTypeToLang(ct.getContentTypeStr());
        if ( lang == null ) {
            ServletOps.errorBadRequest("Unknown content type for triples: " + ct);
            return;
        }
        dest.prefix("root", base+"#");
        ActionLib.parse(action, dest, lang, base);
    }

    // ---- POST

    private static final String NL = "\n";

    @SuppressWarnings("removal")
    private static final String queryStringLocations =
            "PREFIX tdb1:   <"+TDB1.namespace+">"+NL+
            "PREFIX tdb2:   <"+TDB2.namespace+">"+NL+
            """
            SELECT * {
               ?x ( tdb2:location | tdb1:location) ?location
            }
            """ ;

    private static final Query queryLocations = QueryFactory.create(queryStringLocations);

    private static List<String> tdbLocations(HttpAction action, Graph configGraph) {
        try ( QueryExec exec =  QueryExec.graph(configGraph).query(queryLocations).build() ) {
            RowSet results = exec.select();
            List<String> locations = new ArrayList<>();
            results.forEach(b->{
                Node loc = b.get("location");
                String location;
                if ( loc.isURI() )
                    location = loc.getURI();
                else if ( Util.isSimpleString(loc) )
                    location = G.asString(loc);
                else {
                    //action.log.warn(format("[%d] Database location is not a string nor a URI", action.id));
                    // No return
                    ServletOps.errorBadRequest("TDB database location is not a string");
                    location = null;
                }
                locations.add(location);
            });
            return locations;
        } catch (Exception ex) {
            // No return
            ServletOps.errorBadRequest("TDB database location can not be deterined");
            return null;
        }
    }
}
