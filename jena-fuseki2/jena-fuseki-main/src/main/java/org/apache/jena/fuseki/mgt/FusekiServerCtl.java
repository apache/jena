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
import static org.apache.jena.atlas.lib.Lib.getenv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import jakarta.servlet.ServletContext;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.build.DatasetDescriptionMap;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.FusekiVocabG;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.system.G;

/**
 * Each server with management has a {@code FusekiServerCtl} object for its per-server configuration.
 */
public class FusekiServerCtl {
    public static final String envFusekiBase          = "FUSEKI_BASE";
    public static final String envFusekiShiro         = "FUSEKI_SHIRO";
    public static final String DFT_SHIRO_INI          = "shiro.ini";

    public static FusekiServerCtl get(ServletContext cxt) {
        if ( cxt == null )
           return null;
        FusekiServerCtl fusekiServerCtl = (FusekiServerCtl)cxt.getAttribute(Fuseki.attrFusekiServerCtl);
        if ( fusekiServerCtl == null )
            Log.warn(FusekiServerCtl.class, "No FusekiServerCtl in ServletContext");
        return fusekiServerCtl;
    }

    // Relative names of directories in the Fuseki base area.
    private static final String databasesLocationBase = "databases";
    // Place to put Lucene text and spatial indexes.
    private static final String databaseIndexesDir    = "text_indexes";

    private static final String backupDirNameBase     = "backups";
    private static final String configDirNameBase     = "configuration";
    private static final String logsNameBase          = "logs";
    private static final String systemFileAreaBase    = "system_files";
    private static final String templatesNameBase     = "templates";
    private static final String DFT_CONFIG            = "config.ttl";

    private static int BaseFusekiAutoModuleLevel      = 500;
    public static int levelFModAdmin                  = BaseFusekiAutoModuleLevel;
    public static int levelFModUI                     = BaseFusekiAutoModuleLevel+10;
    public static int levelFModShiro                  = BaseFusekiAutoModuleLevel+20;

    /** Directory for TDB databases - this is known to the assembler templates */
    public static Path        dirDatabases       = null;

    /** Directory for writing backups */
    public static Path        dirBackups         = null;

    /** Directory for assembler files */
    public static Path        dirConfiguration   = null;

    /** Directory for assembler files */
    public static Path        dirLogs            = null;

    /** Directory for files uploaded (e.g upload assembler descriptions); not data uploads. */
    public static Path        dirSystemFileArea  = null;

    /** Directory for assembler files */
    public static Path        dirTemplates       = null;

    // Marks the end of successful initialization.
    /*package*/static boolean serverInitialized  = false;

    // Default - "run" in the current directory.
    public static final String dftFusekiBase    = "run";

    private Path fusekiBase = null;

    // Server-wide lock for configuration changes.
    private final Object serverLock            = new Object();

    public FusekiServerCtl(Path location) {
        if ( location == null )
            location = envFusekiBase();

        this.fusekiBase = location;
    }

    public Path getFusekiBase() {
        return fusekiBase;
    }

    public Object getServerlock() {
        return serverLock;
    }

    private Path envFusekiBase() {
        // Does not guarantee existence
        if ( fusekiBase != null )
            return fusekiBase;
        String valueFusekiBase = getenv("FUSEKI_BASE");
        if ( valueFusekiBase == null )
            valueFusekiBase = dftFusekiBase;
        Path setting = Path.of(valueFusekiBase);
        setting = setting.toAbsolutePath();
        return setting;
    }

    /**
     *  Set up the area if not already formatted.
     */
    public void setup() {
        // Ensure the BASE area exists on disk.
        setBaseAreaOnDisk();
        // Format the BASE area.
        ensureBaseArea();
    }

    private void setBaseAreaOnDisk() {
        FmtLog.info(Fuseki.configLog, "Fuseki Base = %s", fusekiBase);
        if ( ! Files.exists(fusekiBase) ) {
            try {
                Files.createDirectories(fusekiBase);
            } catch (IOException e) {
                throw new FusekiConfigException("Failed to create Fuseki Base: "+fusekiBase);
            }
        }
        // Further checks in ensureBaseArea
    }

    /**
     * Create directories if found to be missing.
     */
    private void ensureBaseArea() {
        Path baseArea = fusekiBase;
        if ( Files.exists(baseArea) ) {
            if ( ! Files.isDirectory(baseArea) )
                throw new FusekiConfigException("Fuseki base is not a directory: "+baseArea);
            if ( ! Files.isWritable(baseArea) )
                throw new FusekiConfigException("Fuseki base is not writable: "+baseArea);
        } else {
            ensureDir(baseArea);
        }

        // Ensure the Fuseki base area has the assumed directories.
        dirTemplates        = writeableDirectory(baseArea, templatesNameBase);
        dirDatabases        = writeableDirectory(baseArea, databasesLocationBase);
        dirBackups          = writeableDirectory(baseArea, backupDirNameBase);
        dirConfiguration    = writeableDirectory(baseArea, configDirNameBase);
        dirLogs             = writeableDirectory(baseArea, logsNameBase);
        dirSystemFileArea   = writeableDirectory(baseArea, systemFileAreaBase);

        // ---- Initialize with files.

        // Copy missing files into the Fuseki base
        // Interacts with FMod_Shiro.
        if ( Lib.getenv(FusekiServerCtl.envFusekiShiro) == null ) {
            copyFileIfMissing(null, DFT_SHIRO_INI, baseArea);
            System.setProperty(FusekiServerCtl.envFusekiShiro, baseArea.resolve(DFT_SHIRO_INI).toString());
        }

        copyFileIfMissing(null, DFT_CONFIG, baseArea);
        for ( String n : Template.templateNames ) {
            copyFileIfMissing(null, n, baseArea);
        }

        serverInitialized = true;
    }

    private static List<DataAccessPoint> processServerConfigFile(String configFilename) {
        if ( ! FileOps.exists(configFilename) ) {
            Fuseki.configLog.warn("Configuration file '" + configFilename+"' does not exist");
            return Collections.emptyList();
        }
        //Fuseki.configLog.info("Configuration file: " + configFilename);
        Model model = AssemblerUtils.readAssemblerFile(configFilename);
        if ( model.size() == 0 )
            return Collections.emptyList();
        List<DataAccessPoint> x = FusekiConfig.processServerConfiguration(model, Fuseki.getContext());
        return x;
    }

    private DataAccessPoint configFromTemplate(String templateFile, String datasetPath,
                                                      boolean allowUpdate, Map<String, String> params) {
        // ---- Setup
        if ( params == null ) {
            params = new HashMap<>();
            params.put(Template.NAME, datasetPath);
        } else {
            if ( ! params.containsKey(Template.NAME) ) {
                Fuseki.configLog.warn("No NAME found in template parameters (added)");
                params.put(Template.NAME, datasetPath);
            }
        }
        //-- Logging
        Fuseki.configLog.info("Template file: " + templateFile);
        String dir = params.get(Template.DIR);
        if ( dir != null ) {
            if ( ! Objects.equals(dir, Names.memName) && !FileOps.exists(dir) )
                throw new CmdException("Directory not found: " + dir);
        }
        //-- Logging

        datasetPath = DataAccessPoint.canonical(datasetPath);

        // DRY -- ActionDatasets (and others?)
        addGlobals(params);

        String str = TemplateFunctions.templateFile(fusekiBase, templateFile, params, Lang.TTL);
        Lang lang = RDFLanguages.filenameToLang(str, Lang.TTL);

        Graph configuration = RDFParser.fromString(str, lang).toGraph();
        List<Node> x = G.listPO(configuration, FusekiVocabG.pServiceName, null);
        if ( x.isEmpty() )
            ServletOps.errorBadRequest("No name given in description of Fuseki service");
        if ( x.size() > 1 )
            ServletOps.errorBadRequest("Multiple names given in description of Fuseki service");
        Node fusekiService = x.get(0);
        DatasetDescriptionMap registry = new DatasetDescriptionMap();
        DataAccessPoint dap = FusekiConfig.buildDataAccessPoint(configuration, fusekiService, registry);
        return dap;
    }

    public void addGlobals(Map<String, String> params) {
        if ( params == null ) {
            Fuseki.configLog.warn("FusekiApp.addGlobals : params is null", new Throwable());
            return;
        }

        if ( ! params.containsKey("FUSEKI_BASE") )
            params.put("FUSEKI_BASE", pathStringOrElse(fusekiBase, "unset"));
    }

    /** Copy a file from src to dst under name fn.
     * If src is null, try as a classpath resource
     * @param src   Source directory, or null meaning use java resource.
     * @param fn    File name, a relative path.
     * @param dst   Destination directory.
     *
     */
    private static void copyFileIfMissing(Path src, String fn, Path dst) {
        // fn may be a path.
        Path dstFile = dst.resolve(fn);
        if ( Files.exists(dstFile) )
            return;
        if ( src != null ) {
            Path srcFile = src.resolve(fn);
            if ( ! Files.exists(dstFile) )
                throw new FusekiConfigException("File not found: "+srcFile);
            try {
                IOX.safeWrite(dstFile, output->Files.copy(srcFile, output));
            } catch (RuntimeIOException e) {
                throw new FusekiConfigException("Failed to copy file "+srcFile+" to "+dstFile, e);
            }
        } else {
            copyFileFromResource(fn, dstFile);
        }
    }

    private static void copyFileFromResource(String fn, Path dstFile) {
        try {
            // Get from the file from area "org/apache/jena/fuseki/server"
            String absName = "org/apache/jena/fuseki/server/"+fn;
            InputStream input = FusekiServerCtl.class
                    // Else prepends classname as path
                    .getClassLoader()
                    .getResourceAsStream(absName);

            if ( input == null )
                throw new FusekiConfigException("Failed to find resource '"+absName+"'");
            IOX.safeWrite(dstFile, (output)-> input.transferTo(output));
        }
        catch (RuntimeException e) {
            throw new FusekiConfigException("Failed to copy "+fn+" to "+dstFile, e);
        }
    }

    private static String pathStringOrElse(Path path, String dft) {
        if ( path == null )
            return dft;
        return path.toString();
    }

    // DRY -- ActionDatasets (and others?)
    private static Statement getOne(Model m, Resource s, Property p, RDFNode o) {
        StmtIterator iter = m.listStatements(s, p, o);
        if ( ! iter.hasNext() )
            return null;
        Statement stmt = iter.next();
        if ( iter.hasNext() )
            return null;
        return stmt;
    }

    private static DataAccessPoint datasetDefaultConfiguration(String name, DatasetGraph dsg, boolean allowUpdate) {
        name = DataAccessPoint.canonical(name);
        DataService ds = FusekiConfig.buildDataServiceStd(dsg, allowUpdate);
        DataAccessPoint dap = new DataAccessPoint(name, ds);
        return dap;
    }

    // ---- Helpers

    /** Ensure a directory exists, creating it if necessary.
     */
    private static void ensureDir(Path directory) {
        File dir = directory.toFile();
        if ( ! dir.exists() ) {
            boolean b = dir.mkdirs();
            if ( ! b )
                throw new FusekiConfigException("Failed to create directory: "+directory);
        }
        else if ( ! dir.isDirectory())
            throw new FusekiConfigException("Not a directory: "+directory);
    }

    private static void mustExist(Path directory) {
        File dir = directory.toFile();
        if ( ! dir.exists() )
            throw new FusekiConfigException("Does not exist: "+directory);
        if ( ! dir.isDirectory())
            throw new FusekiConfigException("Not a directory: "+directory);
    }

    private static boolean emptyDir(Path dir) {
        return dir.toFile().list().length <= 2;
    }

    private static boolean exists(Path directory) {
        File dir = directory.toFile();
        return dir.exists();
    }

    private static Path writeableDirectory(Path root , String relName ) {
        Path p = makePath(root, relName);
        ensureDir(p);
        if ( ! Files.isWritable(p) )
            throw new FusekiConfigException("Not writable: "+p);
        return p;
    }

    /** Running a full-features server sets some global state. Clear this up. (mainly for tests.)*/
    public static void clearUpSystemState() {
        Lib.unsetenv(FusekiServerCtl.envFusekiShiro);
        Lib.unsetenv(FusekiServerCtl.envFusekiBase);
        FusekiMain.resetCustomisers();
    }

    private static Path makePath(Path root , String relName ) {
        Path path = root.resolve(relName);
        // Must exist
//        try { path = path.toRealPath(); }
//        catch (IOException e) { IO.exception(e); }
        return path;
    }

    /**
     * Dataset set name to configuration file name. Return a configuration file name -
     * existing one or ".ttl" form if new
     */
    public static String datasetNameToConfigurationFile(HttpAction action, String dsName) {
        List<String> existing = existingConfigurationFile(dsName);
        if ( ! existing.isEmpty() ) {
            if ( existing.size() > 1 ) {
                action.log.warn(format("[%d] Multiple existing configuration files for %s : %s",
                                       action.id, dsName, existing));
                ServletOps.errorBadRequest("Multiple existing configuration files for "+dsName);
                return null;
            }
            return existing.get(0).toString();
        }

        return generateConfigurationFilename(dsName);
    }

    /** New configuration file name - absolute filename */
    public static String generateConfigurationFilename(String dsName) {
        String filename = dsName;
        // Without "/"
        if ( filename.startsWith("/"))
            filename = filename.substring(1);
        Path p = FusekiServerCtl.dirConfiguration.resolve(filename+".ttl");
        return p.toString();
    }

    /** Return the filenames of all matching files in the configuration directory (absolute paths returned ). */
    public static List<String> existingConfigurationFile(String serviceName) {
        String filename = DataAccessPoint.isCanonical(serviceName) ? serviceName.substring(1) : serviceName;
        try {
            List<String> paths = new ArrayList<>();
            // This ".* is a file glob pattern, not a regular expression  - it looks for file extensions.
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(FusekiServerCtl.dirConfiguration, filename+".*") ) {
                stream.forEach((p)-> paths.add(FusekiServerCtl.dirConfiguration.resolve(p).toString() ));
            }
            return paths;
        } catch (IOException ex) {
            throw new InternalErrorException("Failed to read configuration directory "+FusekiServerCtl.dirConfiguration);
        }
    }
}
