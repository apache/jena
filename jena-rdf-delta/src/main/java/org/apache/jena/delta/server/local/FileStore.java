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

package org.apache.jena.delta.server.local;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.server.PatchLogServer;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.changes.PatchSummary;
import org.apache.jena.rdfpatch.system.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of PatchLogServer that uses the local file system.
 */
public class FileStore implements PatchLogServer {
    private static final Logger LOG = LoggerFactory.getLogger(FileStore.class);
    
    private final Path root;
    private final Map<String, LogState> logStates = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * Create a new FileStore.
     * @param path The path to the store directory
     */
    public FileStore(String path) {
        this.root = Paths.get(path);
        
        // Create the directory if it doesn't exist
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new DeltaException("Failed to create store directory: " + root, e);
        }
        
        // Load the existing patch logs
        refreshLogStates();
    }
    
    /**
     * Refresh the log states from the file system.
     */
    private void refreshLogStates() {
        lock.writeLock().lock();
        try {
            logStates.clear();
            
            // Get the log directories
            try {
                List<Path> logDirs = Files.list(root)
                    .filter(p -> Files.isDirectory(p))
                    .collect(Collectors.toList());
                
                for (Path logDir : logDirs) {
                    String name = logDir.getFileName().toString();
                    
                    // Get the head file
                    Path headFile = logDir.resolve("head");
                    if (Files.exists(headFile)) {
                        try {
                            String headId = Files.readString(headFile).trim();
                            logStates.put(name, new LogState(name, Id.fromString(headId), headFile));
                        } catch (Exception e) {
                            LOG.error("Failed to read head file for log: {}", name, e);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("Failed to list log directories", e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public List<LogEntry> listPatchLogs() {
        lock.readLock().lock();
        try {
            return logStates.values().stream()
                .map(state -> new LogEntry(state.name, state.head))
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public Id createPatchLog(String name) {
        lock.writeLock().lock();
        try {
            if (logStates.containsKey(name)) {
                throw new DeltaException("Patch log already exists: " + name);
            }
            
            // Create the log directory
            Path logDir = root.resolve(name);
            try {
                Files.createDirectories(logDir);
            } catch (IOException e) {
                throw new DeltaException("Failed to create log directory: " + logDir, e);
            }
            
            // Create a blank initial patch
            RDFPatch patch = RDFPatchOps.emptyPatch(null);
            Id patchId = patch.getId();
            
            // Save the patch
            Path patchFile = logDir.resolve(patchId.toString());
            try (OutputStream out = Files.newOutputStream(patchFile)) {
                RDFPatchOps.write(out, patch);
            } catch (IOException e) {
                throw new DeltaException("Failed to write patch file: " + patchFile, e);
            }
            
            // Create the head file
            Path headFile = logDir.resolve("head");
            try {
                Files.writeString(headFile, patchId.toString());
            } catch (IOException e) {
                throw new DeltaException("Failed to write head file: " + headFile, e);
            }
            
            // Update the log state
            logStates.put(name, new LogState(name, patchId, headFile));
            
            FmtLog.info(LOG, "Created patch log: %s, head=%s", name, patchId);
            return patchId;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public LogEntry getPatchLogInfo(String name) {
        lock.readLock().lock();
        try {
            LogState state = logStates.get(name);
            if (state == null) {
                return null;
            }
            return new LogEntry(state.name, state.head);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public Id append(String name, RDFPatch patch, Id expected) {
        lock.writeLock().lock();
        try {
            // Get the log state
            LogState state = logStates.get(name);
            if (state == null) {
                throw new DeltaException("Patch log not found: " + name);
            }
            
            // Check the expected version
            if (expected != null && !expected.equals(state.head)) {
                throw new DeltaException(String.format(
                    "Expected version %s does not match current version %s for patch log %s",
                    expected, state.head, name));
            }
            
            // Set the previous patch Id
            patch = RDFPatchOps.withPrevious(patch, state.head);
            
            // Save the patch
            Path logDir = root.resolve(name);
            Path patchFile = logDir.resolve(patch.getId().toString());
            try (OutputStream out = Files.newOutputStream(patchFile)) {
                RDFPatchOps.write(out, patch);
            } catch (IOException e) {
                throw new DeltaException("Failed to write patch file: " + patchFile, e);
            }
            
            // Update the head file
            try {
                Files.writeString(state.headFile, patch.getId().toString());
            } catch (IOException e) {
                throw new DeltaException("Failed to write head file: " + state.headFile, e);
            }
            
            // Update the state
            state.head = patch.getId();
            
            PatchSummary summary = PatchSummary.generate(patch);
            FmtLog.info(LOG, "Appended patch to %s: id=%s, adds=%d, deletes=%d", 
                        name, patch.getId(), summary.getAddCount(), summary.getDeleteCount());
            
            return patch.getId();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public Iterable<RDFPatch> getPatches(String name, Id start) {
        lock.readLock().lock();
        try {
            // Get the log state
            LogState state = logStates.get(name);
            if (state == null) {
                throw new DeltaException("Patch log not found: " + name);
            }
            
            // Get the patches
            List<RDFPatch> patches = new ArrayList<>();
            
            // If start is null, start from the head
            Id current = (start != null) ? start : state.head;
            
            while (current != null) {
                // Get the patch
                RDFPatch patch = getPatch(name, current);
                if (patch == null) {
                    break;
                }
                
                // Add to the result
                patches.add(patch);
                
                // Move to the previous patch
                current = patch.getPrevious();
            }
            
            // Reverse the list to get oldest to newest
            Collections.reverse(patches);
            
            return patches;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public RDFPatch getPatch(String name, Id id) {
        lock.readLock().lock();
        try {
            // Get the log state
            LogState state = logStates.get(name);
            if (state == null) {
                throw new DeltaException("Patch log not found: " + name);
            }
            
            // Get the patch file
            Path logDir = root.resolve(name);
            Path patchFile = logDir.resolve(id.toString());
            
            if (!Files.exists(patchFile)) {
                return null;
            }
            
            try (InputStream in = Files.newInputStream(patchFile)) {
                return RDFPatchOps.read(in);
            } catch (IOException e) {
                throw new DeltaException("Failed to read patch file: " + patchFile, e);
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * State for a patch log.
     */
    private static class LogState {
        final String name;
        Id head;
        final Path headFile;
        
        LogState(String name, Id head, Path headFile) {
            this.name = name;
            this.head = head;
            this.headFile = headFile;
        }
    }
}