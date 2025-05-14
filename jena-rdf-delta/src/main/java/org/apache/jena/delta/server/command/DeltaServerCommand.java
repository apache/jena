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

package org.apache.jena.delta.server.command;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.delta.client.DeltaLinkHTTP;
import org.apache.jena.delta.conflict.ConflictType;
import org.apache.jena.delta.conflict.ResolutionStrategy;
import org.apache.jena.delta.server.PatchLogServer;
import org.apache.jena.delta.server.PatchLogServer.LogEntry;
import org.apache.jena.delta.server.ServerBuilder;
import org.apache.jena.delta.server.cluster.DistributedPatchLogServer;
import org.apache.jena.delta.server.http.DeltaServer;
import org.apache.jena.delta.server.local.FileStore;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.changes.PatchSummary;
import org.apache.jena.rdfpatch.system.Id;
import org.apache.jena.sys.JenaSystem;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;

/**
 * Command-line interface for the Delta patch server.
 */
public class DeltaServerCommand extends CmdGeneral {
    
    // Top level command categories
    static String[] cmdClasses = {
        "server",   "Start a patch log server",
        "list",     "List patch logs on a server",
        "create",   "Create a new patch log",
        "info",     "Get information about a patch log",
        "patches",  "List patches in a patch log",
        "backup",   "Backup a patch log",
        "restore",  "Restore a patch log backup",
    };
    
    // Server options
    private static ArgDecl serverPortDecl = new ArgDecl(ArgDecl.HasValue, "port");
    private static ArgDecl serverStoreDecl = new ArgDecl(ArgDecl.HasValue, "store");
    private static ArgDecl serverZkDecl = new ArgDecl(ArgDecl.HasValue, "zk");
    private static ArgDecl serverJmxDecl = new ArgDecl(ArgDecl.NoValue, "jmx");
    
    // Conflict resolution options
    private static ArgDecl conflictDetectionDecl = new ArgDecl(ArgDecl.NoValue, "conflict-detection");
    private static ArgDecl conflictStrategyDecl = new ArgDecl(ArgDecl.HasValue, "conflict-strategy");
    private static ArgDecl conflictObjectStrategyDecl = new ArgDecl(ArgDecl.HasValue, "object-conflict-strategy");
    private static ArgDecl conflictCacheExpiryDecl = new ArgDecl(ArgDecl.HasValue, "conflict-cache-expiry");
    
    // Client options
    private static ArgDecl clientServerDecl = new ArgDecl(ArgDecl.HasValue, "server");
    private static ArgDecl clientOutputDecl = new ArgDecl(ArgDecl.HasValue, "output");
    private static ArgDecl clientFormatDecl = new ArgDecl(ArgDecl.HasValue, "format");
    
    // Operation options
    private static ArgDecl fromVersionDecl = new ArgDecl(ArgDecl.HasValue, "from");
    private static ArgDecl toVersionDecl = new ArgDecl(ArgDecl.HasValue, "to");
    
    // Command mode
    private String mode = null;
    
    // Server options
    private int serverPort = 1066;
    private String serverStorePath = null;
    private String serverZkConnect = null;
    private boolean serverJmx = false;
    
    // Conflict resolution options
    private boolean conflictDetection = false;
    private String conflictStrategy = "last-write-wins";
    private String objectConflictStrategy = null;
    private long conflictCacheExpiry = 60000;
    
    // Client options
    private String clientServerUrl = null;
    private String clientOutputPath = null;
    private String clientFormat = "text";
    
    // Operation options
    private String fromVersion = null;
    private String toVersion = null;
    
    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        // Initialize Jena
        JenaSystem.init();
        
        // Create and run command
        new DeltaServerCommand(args).mainRun();
    }
    
    /**
     * Create a new DeltaServerCommand.
     */
    public DeltaServerCommand(String[] args) {
        super(args);
        
        // Basic options
        add(serverPortDecl, "--port", "Port for the server (default: 1066)");
        add(serverStoreDecl, "--store", "Path to store directory");
        add(serverZkDecl, "--zk", "ZooKeeper connection string (e.g., localhost:2181)");
        add(serverJmxDecl, "--jmx", "Enable JMX monitoring");
        
        // Conflict resolution options
        add(conflictDetectionDecl, "--conflict-detection", "Enable conflict detection and resolution");
        add(conflictStrategyDecl, "--conflict-strategy", "Default strategy for conflict resolution (last-write-wins, first-write-wins, merge, etc.)");
        add(conflictObjectStrategyDecl, "--object-conflict-strategy", "Strategy for object conflicts");
        add(conflictCacheExpiryDecl, "--conflict-cache-expiry", "Time in milliseconds to keep patches in the cache for conflict detection");
        
        add(clientServerDecl, "--server", "URL of the Delta server (e.g., http://localhost:1066/)");
        add(clientOutputDecl, "--output", "Output file path");
        add(clientFormatDecl, "--format", "Output format (text, json)");
        
        add(fromVersionDecl, "--from", "Starting version for operations");
        add(toVersionDecl, "--to", "Ending version for operations");
    }
    
    @Override
    protected String getSummary() {
        return "Usage: delta-server <command> [options]";
    }
    
    @Override
    protected String getCommandName() {
        return "delta-server";
    }
    
    @Override
    protected void processModulesAndArgs() {
        // Get the command mode
        if (getPositional().isEmpty()) {
            printHelp();
            throw new CmdException("No command specified");
        }
        
        mode = getPositionalArg(0);
        
        // Check for help on command
        if (mode.equalsIgnoreCase("help") || mode.equals("--help") || mode.equals("-h")) {
            if (getPositional().size() > 1) {
                String helpCmd = getPositionalArg(1);
                printHelpForCommand(helpCmd);
            } else {
                printHelp();
            }
            throw new CmdException("Help requested");
        }
        
        // Server options
        if (contains(serverPortDecl)) {
            serverPort = Integer.parseInt(getValue(serverPortDecl));
        }
        if (contains(serverStoreDecl)) {
            serverStorePath = getValue(serverStoreDecl);
        }
        if (contains(serverZkDecl)) {
            serverZkConnect = getValue(serverZkDecl);
        }
        if (contains(serverJmxDecl)) {
            serverJmx = true;
        }
        
        // Conflict resolution options
        if (contains(conflictDetectionDecl)) {
            conflictDetection = true;
        }
        if (contains(conflictStrategyDecl)) {
            conflictStrategy = getValue(conflictStrategyDecl);
        }
        if (contains(conflictObjectStrategyDecl)) {
            objectConflictStrategy = getValue(conflictObjectStrategyDecl);
        }
        if (contains(conflictCacheExpiryDecl)) {
            conflictCacheExpiry = Long.parseLong(getValue(conflictCacheExpiryDecl));
        }
        
        // Client options
        if (contains(clientServerDecl)) {
            clientServerUrl = getValue(clientServerDecl);
        }
        if (contains(clientOutputDecl)) {
            clientOutputPath = getValue(clientOutputDecl);
        }
        if (contains(clientFormatDecl)) {
            clientFormat = getValue(clientFormatDecl);
        }
        
        // Operation options
        if (contains(fromVersionDecl)) {
            fromVersion = getValue(fromVersionDecl);
        }
        if (contains(toVersionDecl)) {
            toVersion = getValue(toVersionDecl);
        }
        
        // Validate based on mode
        if (mode.equalsIgnoreCase("server")) {
            if (serverStorePath == null) {
                throw new CmdException("Missing required --store option for server command");
            }
        } else {
            // Client commands require a server URL
            if (clientServerUrl == null) {
                throw new CmdException("Missing required --server option for " + mode + " command");
            }
        }
        
        // Specific command validations
        if (mode.equalsIgnoreCase("create") || mode.equalsIgnoreCase("info") || 
            mode.equalsIgnoreCase("patches") || mode.equalsIgnoreCase("backup")) {
            
            if (getPositional().size() < 2) {
                throw new CmdException("Missing dataset name for " + mode + " command");
            }
        }
    }
    
    @Override
    protected void exec() {
        try {
            if (mode.equalsIgnoreCase("server")) {
                execServerCommand();
            } else if (mode.equalsIgnoreCase("list")) {
                execListCommand();
            } else if (mode.equalsIgnoreCase("create")) {
                execCreateCommand();
            } else if (mode.equalsIgnoreCase("info")) {
                execInfoCommand();
            } else if (mode.equalsIgnoreCase("patches")) {
                execPatchesCommand();
            } else if (mode.equalsIgnoreCase("backup")) {
                execBackupCommand();
            } else if (mode.equalsIgnoreCase("restore")) {
                execRestoreCommand();
            } else {
                throw new CmdException("Unknown command: " + mode);
            }
        } catch (DeltaException e) {
            throw new CmdException("Error: " + e.getMessage());
        }
    }
    
    /**
     * Execute the server command.
     */
    private void execServerCommand() {
        try {
            // Use the ServerBuilder for creating the server
            ServerBuilder builder = new ServerBuilder()
                .port(serverPort)
                .storePath(serverStorePath)
                .metricsEnabled(true)
                .jmxEnabled(serverJmx)
                .prometheusEnabled(false);
            
            // Add distributed mode if ZooKeeper is configured
            if (serverZkConnect != null) {
                builder.distributed(true)
                       .zookeeperConnect(serverZkConnect);
            }
            
            // Add conflict detection if enabled
            if (conflictDetection) {
                builder.conflictDetectionEnabled(true)
                       .conflictCacheExpiryMs(conflictCacheExpiry);
                
                // Set default resolution strategy
                ResolutionStrategy strategy = ResolutionStrategy.fromName(conflictStrategy);
                if (strategy != null) {
                    builder.defaultResolutionStrategy(strategy);
                }
                
                // Set object conflict strategy if specified
                if (objectConflictStrategy != null) {
                    ResolutionStrategy objStrategy = ResolutionStrategy.fromName(objectConflictStrategy);
                    if (objStrategy != null) {
                        builder.resolutionStrategy(ConflictType.OBJECT, objStrategy);
                    }
                }
            }
            
            // Build the server
            DeltaServer server = builder.build();
            
            // Start the server
            server.start();
            
            System.out.println("Delta server started on port " + serverPort);
            System.out.println("Server URL: " + server.getURI());
            System.out.println("Using store path: " + serverStorePath);
            if (serverZkConnect != null) {
                System.out.println("ZooKeeper connection: " + serverZkConnect);
            }
            if (serverJmx) {
                System.out.println("JMX monitoring enabled");
            }
            if (conflictDetection) {
                System.out.println("Conflict detection enabled");
                System.out.println("Default resolution strategy: " + conflictStrategy);
                if (objectConflictStrategy != null) {
                    System.out.println("Object conflict strategy: " + objectConflictStrategy);
                }
            }
            System.out.println("Press Ctrl+C to stop the server");
            
            // Wait for the server to exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down Delta server...");
                server.stop();
                System.out.println("Delta server stopped");
                
                // Close the distributed server if needed
                if (patchLogServer instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) patchLogServer).close();
                    } catch (Exception e) {
                        System.err.println("Error closing server: " + e.getMessage());
                    }
                }
            }));
            
            // Wait for the server thread
            server.server.join();
            
        } catch (Exception e) {
            throw new DeltaException("Failed to start server", e);
        }
    }
    
    /**
     * Execute the list command.
     */
    private void execListCommand() {
        try {
            // Connect to the server
            DeltaLink link = DeltaLinkHTTP.connect(clientServerUrl);
            
            // Get the list of datasets
            List<String> datasets = link.listDatasets();
            
            // Output the results
            if (clientFormat.equalsIgnoreCase("json")) {
                JsonBuilder builder = new JsonBuilder();
                builder.startObject();
                builder.key("datasets").startArray();
                for (String dataset : datasets) {
                    builder.startObject();
                    builder.key("name").value(dataset);
                    builder.key("version").value(link.getDatasetVersion(dataset));
                    builder.finishObject();
                }
                builder.finishArray();
                builder.finishObject();
                
                outputJson(builder.build());
            } else {
                // Default text format
                System.out.println("Datasets on server " + clientServerUrl + ":");
                System.out.println();
                
                if (datasets.isEmpty()) {
                    System.out.println("No datasets found");
                } else {
                    System.out.println(String.format("%-20s %-40s", "Name", "Version"));
                    System.out.println(String.format("%-20s %-40s", "----", "-------"));
                    
                    for (String dataset : datasets) {
                        String version = link.getDatasetVersion(dataset);
                        System.out.println(String.format("%-20s %-40s", dataset, version));
                    }
                }
            }
        } catch (Exception e) {
            throw new DeltaException("Failed to list datasets", e);
        }
    }
    
    /**
     * Execute the create command.
     */
    private void execCreateCommand() {
        try {
            // Get the dataset name
            String datasetName = getPositionalArg(1);
            
            // Connect to the server
            DeltaLink link = DeltaLinkHTTP.connect(clientServerUrl);
            
            // Check if the dataset already exists
            List<String> datasets = link.listDatasets();
            if (datasets.contains(datasetName)) {
                throw new DeltaException("Dataset already exists: " + datasetName);
            }
            
            // Create the dataset
            link.newDataset(datasetName);
            
            // Output the results
            System.out.println("Created dataset '" + datasetName + "' on server " + clientServerUrl);
            System.out.println("Initial version: " + link.getDatasetVersion(datasetName));
        } catch (Exception e) {
            throw new DeltaException("Failed to create dataset", e);
        }
    }
    
    /**
     * Execute the info command.
     */
    private void execInfoCommand() {
        try {
            // Get the dataset name
            String datasetName = getPositionalArg(1);
            
            // Connect to the server
            DeltaLink link = DeltaLinkHTTP.connect(clientServerUrl);
            
            // Check if the dataset exists
            List<String> datasets = link.listDatasets();
            if (!datasets.contains(datasetName)) {
                throw new DeltaException("Dataset not found: " + datasetName);
            }
            
            // Get dataset info
            String version = link.getDatasetVersion(datasetName);
            
            // Output the results
            if (clientFormat.equalsIgnoreCase("json")) {
                JsonBuilder builder = new JsonBuilder();
                builder.startObject();
                builder.key("name").value(datasetName);
                builder.key("version").value(version);
                builder.finishObject();
                
                outputJson(builder.build());
            } else {
                // Default text format
                System.out.println("Dataset: " + datasetName);
                System.out.println("Server: " + clientServerUrl);
                System.out.println("Version: " + version);
            }
        } catch (Exception e) {
            throw new DeltaException("Failed to get dataset info", e);
        }
    }
    
    /**
     * Execute the patches command.
     */
    private void execPatchesCommand() {
        try {
            // Get the dataset name
            String datasetName = getPositionalArg(1);
            
            // Connect to the server
            DeltaLink link = DeltaLinkHTTP.connect(clientServerUrl);
            
            // Check if the dataset exists
            List<String> datasets = link.listDatasets();
            if (!datasets.contains(datasetName)) {
                throw new DeltaException("Dataset not found: " + datasetName);
            }
            
            // Get patches
            Id start = (fromVersion != null) ? Id.fromString(fromVersion) : null;
            Iterable<RDFPatch> patches = link.getPatches(datasetName, start);
            
            // Output the results
            if (clientFormat.equalsIgnoreCase("json")) {
                JsonBuilder builder = new JsonBuilder();
                builder.startObject();
                builder.key("dataset").value(datasetName);
                builder.key("patches").startArray();
                
                for (RDFPatch patch : patches) {
                    PatchSummary summary = PatchSummary.generate(patch);
                    
                    builder.startObject();
                    builder.key("id").value(patch.getId().toString());
                    builder.key("previous").value(
                        patch.getPrevious() != null ? patch.getPrevious().toString() : null);
                    builder.key("adds").value(summary.getAddCount());
                    builder.key("deletes").value(summary.getDeleteCount());
                    builder.finishObject();
                }
                
                builder.finishArray();
                builder.finishObject();
                
                outputJson(builder.build());
            } else {
                // Default text format
                System.out.println("Patches for dataset '" + datasetName + "' on server " + clientServerUrl + ":");
                System.out.println();
                
                System.out.println(String.format("%-40s %-40s %-10s %-10s", 
                                                "Patch ID", "Previous ID", "Adds", "Deletes"));
                System.out.println(String.format("%-40s %-40s %-10s %-10s", 
                                                "--------", "-----------", "----", "-------"));
                
                int count = 0;
                for (RDFPatch patch : patches) {
                    PatchSummary summary = PatchSummary.generate(patch);
                    
                    String prevId = patch.getPrevious() != null ? patch.getPrevious().toString() : "(none)";
                    
                    System.out.println(String.format("%-40s %-40s %-10d %-10d", 
                                                   patch.getId(), prevId, 
                                                   summary.getAddCount(), summary.getDeleteCount()));
                    count++;
                }
                
                System.out.println();
                System.out.println("Total patches: " + count);
            }
        } catch (Exception e) {
            throw new DeltaException("Failed to get patches", e);
        }
    }
    
    /**
     * Execute the backup command.
     */
    private void execBackupCommand() {
        try {
            // Get the dataset name
            String datasetName = getPositionalArg(1);
            
            // Connect to the server
            DeltaLink link = DeltaLinkHTTP.connect(clientServerUrl);
            
            // Check if the dataset exists
            List<String> datasets = link.listDatasets();
            if (!datasets.contains(datasetName)) {
                throw new DeltaException("Dataset not found: " + datasetName);
            }
            
            // Get the output path
            String outputPath = clientOutputPath;
            if (outputPath == null) {
                outputPath = datasetName + "-backup.nq";
            }
            
            // Create the backup command
            // This is a placeholder - in a real implementation, we would use proper backup logic
            System.out.println("Backing up dataset '" + datasetName + "' to file: " + outputPath);
            System.out.println("(Backup functionality not yet implemented)");
            
        } catch (Exception e) {
            throw new DeltaException("Failed to backup dataset", e);
        }
    }
    
    /**
     * Execute the restore command.
     */
    private void execRestoreCommand() {
        try {
            // Get the input file
            if (getPositional().size() < 2) {
                throw new CmdException("Missing input file for restore command");
            }
            String inputFile = getPositionalArg(1);
            
            // Get the dataset name (optional)
            String datasetName = null;
            if (getPositional().size() > 2) {
                datasetName = getPositionalArg(2);
            } else {
                // Default to file basename
                datasetName = Lib.basename(inputFile);
                if (datasetName.endsWith("-backup.nq")) {
                    datasetName = datasetName.substring(0, datasetName.length() - 11);
                }
            }
            
            // Connect to the server
            DeltaLink link = DeltaLinkHTTP.connect(clientServerUrl);
            
            // Create the restore command
            // This is a placeholder - in a real implementation, we would use proper restore logic
            System.out.println("Restoring from file '" + inputFile + "' to dataset: " + datasetName);
            System.out.println("(Restore functionality not yet implemented)");
            
        } catch (Exception e) {
            throw new DeltaException("Failed to restore dataset", e);
        }
    }
    
    /**
     * Output JSON to the specified output destination.
     */
    private void outputJson(JsonValue json) {
        try {
            PrintStream out;
            if (clientOutputPath != null) {
                out = new PrintStream(clientOutputPath);
            } else {
                out = System.out;
            }
            
            JSON.write(out, json);
            
            if (out != System.out) {
                out.close();
            }
        } catch (Exception e) {
            throw new DeltaException("Failed to output JSON", e);
        }
    }
    
    /**
     * Print help for a specific command.
     */
    private void printHelpForCommand(String cmd) {
        System.out.println("RDF Delta Server - " + lookupCommandDescription(cmd));
        System.out.println();
        
        if (cmd.equalsIgnoreCase("server")) {
            System.out.println("Usage: delta-server server --store PATH [--port PORT] [--zk CONN] [--jmx] [--conflict-detection] [--conflict-strategy STRATEGY]");
            System.out.println();
            System.out.println("Start a Delta patch log server.");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --store PATH                Path to the store directory (required)");
            System.out.println("  --port PORT                 Port to listen on (default: 1066)");
            System.out.println("  --zk CONN                   ZooKeeper connection string (for distributed mode)");
            System.out.println("  --jmx                       Enable JMX monitoring");
            System.out.println("  --conflict-detection        Enable conflict detection and resolution");
            System.out.println("  --conflict-strategy STR     Default conflict resolution strategy");
            System.out.println("                              (last-write-wins, first-write-wins, merge, etc.)");
            System.out.println("  --object-conflict-strategy STR  Strategy for object conflicts");
            System.out.println("  --conflict-cache-expiry MS  Cache expiry time in milliseconds (default: 60000)");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  delta-server server --store /tmp/delta-store --port 1066");
        } else if (cmd.equalsIgnoreCase("list")) {
            System.out.println("Usage: delta-server list --server URL [--format FORMAT] [--output FILE]");
            System.out.println();
            System.out.println("List all patch logs on a server.");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --server URL       URL of the Delta server (required)");
            System.out.println("  --format FORMAT    Output format: text, json (default: text)");
            System.out.println("  --output FILE      Output file path (default: stdout)");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  delta-server list --server http://localhost:1066/");
        } else if (cmd.equalsIgnoreCase("create")) {
            System.out.println("Usage: delta-server create DATASET --server URL");
            System.out.println();
            System.out.println("Create a new patch log.");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --server URL       URL of the Delta server (required)");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  delta-server create my-dataset --server http://localhost:1066/");
        } else if (cmd.equalsIgnoreCase("info")) {
            System.out.println("Usage: delta-server info DATASET --server URL [--format FORMAT] [--output FILE]");
            System.out.println();
            System.out.println("Get information about a patch log.");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --server URL       URL of the Delta server (required)");
            System.out.println("  --format FORMAT    Output format: text, json (default: text)");
            System.out.println("  --output FILE      Output file path (default: stdout)");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  delta-server info my-dataset --server http://localhost:1066/");
        } else if (cmd.equalsIgnoreCase("patches")) {
            System.out.println("Usage: delta-server patches DATASET --server URL [--from VERSION] [--format FORMAT] [--output FILE]");
            System.out.println();
            System.out.println("List patches in a patch log.");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --server URL       URL of the Delta server (required)");
            System.out.println("  --from VERSION     Starting version (default: all patches)");
            System.out.println("  --format FORMAT    Output format: text, json (default: text)");
            System.out.println("  --output FILE      Output file path (default: stdout)");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  delta-server patches my-dataset --server http://localhost:1066/");
        } else if (cmd.equalsIgnoreCase("backup")) {
            System.out.println("Usage: delta-server backup DATASET --server URL [--output FILE]");
            System.out.println();
            System.out.println("Backup a patch log to a file.");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --server URL       URL of the Delta server (required)");
            System.out.println("  --output FILE      Output file path (default: DATASET-backup.nq)");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  delta-server backup my-dataset --server http://localhost:1066/ --output my-backup.nq");
        } else if (cmd.equalsIgnoreCase("restore")) {
            System.out.println("Usage: delta-server restore FILE [DATASET] --server URL");
            System.out.println();
            System.out.println("Restore a patch log from a backup file.");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --server URL       URL of the Delta server (required)");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  delta-server restore my-backup.nq my-dataset --server http://localhost:1066/");
        } else {
            System.out.println("Unknown command: " + cmd);
            printHelp();
        }
    }
    
    /**
     * Look up the description for a command.
     */
    private String lookupCommandDescription(String cmd) {
        for (int i = 0; i < cmdClasses.length; i += 2) {
            if (cmdClasses[i].equalsIgnoreCase(cmd)) {
                return cmdClasses[i + 1];
            }
        }
        return "Unknown command";
    }
    
    /**
     * Print the general help message.
     */
    private void printHelp() {
        System.out.println("RDF Delta Server");
        System.out.println("Usage: delta-server <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        
        // Print command list with descriptions
        for (int i = 0; i < cmdClasses.length; i += 2) {
            String cmd = cmdClasses[i];
            String desc = cmdClasses[i + 1];
            System.out.println(String.format("  %-15s %s", cmd, desc));
        }
        
        System.out.println();
        System.out.println("For help on a specific command, use: delta-server help <command>");
    }
}