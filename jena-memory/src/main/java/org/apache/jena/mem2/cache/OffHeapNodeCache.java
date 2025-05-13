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

package org.apache.jena.mem2.cache;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.mem2.MemoryStats;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A node cache implementation that stores node data off-heap to reduce
 * garbage collection overhead.
 * <p>
 * This implementation serializes RDF nodes to off-heap memory buffers,
 * allowing more efficient memory usage for large datasets and reducing
 * the impact of garbage collection.
 */
public class OffHeapNodeCache implements NodeCache {
    
    private static final Logger LOG = LoggerFactory.getLogger(OffHeapNodeCache.class);
    
    // Buffer allocation size
    private static final int INITIAL_BUFFER_SIZE = 1024 * 1024; // 1MB
    private static final int MAX_BUFFER_SIZE = 1024 * 1024 * 1024; // 1GB
    
    // Node location record (position in buffer)
    private static class NodeLocation {
        final int bufferIndex;
        final int position;
        final int length;
        
        NodeLocation(int bufferIndex, int position, int length) {
            this.bufferIndex = bufferIndex;
            this.position = position;
            this.length = length;
        }
    }
    
    // Off-heap buffers
    private ByteBuffer[] directBuffers;
    private UnsafeBuffer[] unsafeBuffers;
    private int currentBufferIndex;
    private int currentPosition;
    
    // Memory tracking
    private final AtomicLong offHeapBytesUsed = new AtomicLong(0);
    private final AtomicLong offHeapBytesReserved = new AtomicLong(0);
    
    // Node mapping
    private final Map<Node, NodeLocation> nodeToLocation = new ConcurrentHashMap<>();
    private final Map<String, Node> hashToNode = new ConcurrentHashMap<>();
    
    // Stats
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // State
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    /**
     * Create a new off-heap node cache with default initial capacity.
     */
    public OffHeapNodeCache() {
        this(INITIAL_BUFFER_SIZE);
    }
    
    /**
     * Create a new off-heap node cache with the specified initial capacity.
     * 
     * @param initialCapacity The initial capacity in bytes
     */
    public OffHeapNodeCache(int initialCapacity) {
        // Initialize buffers
        int bufferSize = Math.max(INITIAL_BUFFER_SIZE, initialCapacity);
        this.directBuffers = new ByteBuffer[1];
        this.unsafeBuffers = new UnsafeBuffer[1];
        this.directBuffers[0] = ByteBuffer.allocateDirect(bufferSize);
        this.unsafeBuffers[0] = new UnsafeBuffer(directBuffers[0]);
        this.currentBufferIndex = 0;
        this.currentPosition = 0;
        
        // Update memory tracking
        offHeapBytesReserved.set(bufferSize);
        
        LOG.debug("Created OffHeapNodeCache with initial capacity: {} bytes", bufferSize);
    }
    
    /**
     * Get or add a node to the cache.
     */
    @Override
    public Node getOrAdd(Node node) {
        if (node == null) {
            return null;
        }
        
        checkNotClosed();
        
        // Check if the node is already in the cache
        NodeLocation location = nodeToLocation.get(node);
        if (location != null) {
            cacheHits.incrementAndGet();
            return node;
        }
        
        // Not in cache, add it
        cacheMisses.incrementAndGet();
        
        // Serialize the node
        byte[] nodeBytes = serializeNode(node);
        if (nodeBytes == null || nodeBytes.length == 0) {
            LOG.warn("Failed to serialize node: {}", node);
            return node;
        }
        
        // Store the node in the off-heap buffer
        NodeLocation newLocation = storeNodeBytes(nodeBytes);
        if (newLocation == null) {
            LOG.warn("Failed to store node bytes for node: {}", node);
            return node;
        }
        
        // Add to the cache
        nodeToLocation.put(node, newLocation);
        
        // Store the node by hash to avoid creating duplicate Node objects
        String nodeHash = computeNodeHash(node);
        hashToNode.put(nodeHash, node);
        
        // Update memory tracking
        offHeapBytesUsed.addAndGet(nodeBytes.length);
        
        return node;
    }
    
    /**
     * Check if a node is in the cache.
     */
    @Override
    public boolean contains(Node node) {
        if (node == null) {
            return false;
        }
        
        checkNotClosed();
        
        return nodeToLocation.containsKey(node);
    }
    
    /**
     * Remove a node from the cache.
     */
    @Override
    public boolean remove(Node node) {
        if (node == null) {
            return false;
        }
        
        checkNotClosed();
        
        // Remove from the cache
        NodeLocation location = nodeToLocation.remove(node);
        if (location == null) {
            return false;
        }
        
        // Remove from the hash map
        String nodeHash = computeNodeHash(node);
        hashToNode.remove(nodeHash);
        
        // Update memory tracking
        offHeapBytesUsed.addAndGet(-location.length);
        
        // Note: We don't actually free the memory in the buffer
        // It will be reused on the next compaction
        
        return true;
    }
    
    /**
     * Clear all nodes from the cache.
     */
    @Override
    public void clear() {
        checkNotClosed();
        
        // Clear the mappings
        nodeToLocation.clear();
        hashToNode.clear();
        
        // Reset the buffer position
        currentPosition = 0;
        
        // Update memory tracking
        offHeapBytesUsed.set(0);
        
        LOG.debug("Cleared OffHeapNodeCache");
    }
    
    /**
     * Get the number of nodes in the cache.
     */
    @Override
    public int size() {
        return nodeToLocation.size();
    }
    
    /**
     * Get memory usage statistics for this cache.
     */
    @Override
    public MemoryStats getMemoryStats() {
        long totalOffHeapBytes = offHeapBytesReserved.get();
        long usedOffHeapBytes = offHeapBytesUsed.get();
        
        // Estimate on-heap overhead
        long onHeapBytes = nodeToLocation.size() * 64L + hashToNode.size() * 64L;
        
        return MemoryStats.builder()
            .timestamp(Instant.now())
            .onHeapBytesUsed(onHeapBytes)
            .onHeapBytesReserved(onHeapBytes)
            .offHeapBytesUsed(usedOffHeapBytes)
            .offHeapBytesReserved(totalOffHeapBytes)
            .nodeCount(nodeToLocation.size())
            .cachedNodeCount(nodeToLocation.size())
            .gcCount(ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .mapToLong(gc -> gc.getCollectionCount())
                .sum())
            .gcTimeMs(ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .mapToLong(gc -> gc.getCollectionTime())
                .sum())
            .build();
    }
    
    /**
     * Optimize the cache for memory usage.
     */
    @Override
    public void optimize() {
        checkNotClosed();
        
        // Compact the buffers to reclaim unused space
        compactBuffers();
        
        LOG.debug("Optimized OffHeapNodeCache, current stats: {}", getMemoryStats());
    }
    
    /**
     * Close the cache and release resources.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            // Clear the cache
            clear();
            
            // Release buffer references
            for (int i = 0; i < directBuffers.length; i++) {
                unsafeBuffers[i] = null;
                directBuffers[i] = null;
            }
            
            // Reset memory tracking
            offHeapBytesReserved.set(0);
            
            LOG.debug("Closed OffHeapNodeCache");
        }
    }
    
    /**
     * Check if the cache is closed.
     */
    @Override
    public boolean isClosed() {
        return closed.get();
    }
    
    /**
     * Serialize a node to a byte array.
     */
    private byte[] serializeNode(Node node) {
        try {
            // This is a simple implementation - a more optimized version
            // would use a binary format specific for Node objects
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            UnsafeBuffer unsafeBuffer = new UnsafeBuffer(buffer);
            
            // Create a stream that writes to our buffer
            StreamRDF writer = new StreamRDFBase() {
                private int position = 0;
                
                @Override
                public void triple(org.apache.jena.graph.Triple triple) {
                    // Not used
                }
                
                @Override
                public void node(Node node) {
                    // Write the node type
                    byte nodeType = getNodeType(node);
                    unsafeBuffer.putByte(position++, nodeType);
                    
                    // Write the node value based on type
                    switch (nodeType) {
                        case 1: // URI
                            writeString(node.getURI());
                            break;
                        case 2: // Blank node
                            writeString(node.getBlankNodeLabel());
                            break;
                        case 3: // Literal
                            writeString(node.getLiteralLexicalForm());
                            if (node.getLiteralLanguage() != null && !node.getLiteralLanguage().isEmpty()) {
                                // Language string
                                unsafeBuffer.putByte(position++, (byte)1);
                                writeString(node.getLiteralLanguage());
                            } else if (node.getLiteralDatatype() != null) {
                                // Typed literal
                                unsafeBuffer.putByte(position++, (byte)2);
                                writeString(node.getLiteralDatatypeURI());
                            } else {
                                // Simple literal
                                unsafeBuffer.putByte(position++, (byte)0);
                            }
                            break;
                        case 4: // Variable
                            writeString(node.getName());
                            break;
                    }
                }
                
                private void writeString(String value) {
                    // Write string length
                    byte[] bytes = value.getBytes();
                    unsafeBuffer.putInt(position, bytes.length);
                    position += 4;
                    
                    // Write string bytes
                    unsafeBuffer.putBytes(position, bytes);
                    position += bytes.length;
                }
            };
            
            // Write the node
            writer.node(node);
            
            // Create a byte array with the serialized data
            byte[] result = new byte[buffer.position()];
            buffer.flip();
            buffer.get(result);
            
            return result;
        } catch (Exception e) {
            LOG.error("Error serializing node: {}", node, e);
            return null;
        }
    }
    
    /**
     * Get the type of a node.
     */
    private byte getNodeType(Node node) {
        if (node.isURI()) {
            return 1;
        } else if (node.isBlank()) {
            return 2;
        } else if (node.isLiteral()) {
            return 3;
        } else if (node.isVariable()) {
            return 4;
        } else {
            return 0; // Unknown
        }
    }
    
    /**
     * Store node bytes in the buffer.
     */
    private synchronized NodeLocation storeNodeBytes(byte[] nodeBytes) {
        // Check if there's enough space in the current buffer
        if (currentPosition + nodeBytes.length > unsafeBuffers[currentBufferIndex].capacity()) {
            // Need to allocate a new buffer or compact
            if (!allocateNextBuffer(nodeBytes.length)) {
                // Failed to allocate a new buffer or compact
                LOG.error("Failed to allocate buffer space for node bytes");
                return null;
            }
        }
        
        // Store the node bytes
        int position = currentPosition;
        unsafeBuffers[currentBufferIndex].putBytes(position, nodeBytes);
        currentPosition += nodeBytes.length;
        
        return new NodeLocation(currentBufferIndex, position, nodeBytes.length);
    }
    
    /**
     * Allocate the next buffer.
     */
    private boolean allocateNextBuffer(int requiredSize) {
        // First try to compact the current buffer
        if (compactCurrentBuffer() && currentPosition + requiredSize <= unsafeBuffers[currentBufferIndex].capacity()) {
            return true;
        }
        
        // If compaction didn't free enough space, allocate a new buffer
        int newIndex = currentBufferIndex + 1;
        
        // Check if we need to expand the buffer arrays
        if (newIndex >= directBuffers.length) {
            int newLength = directBuffers.length * 2;
            ByteBuffer[] newDirectBuffers = new ByteBuffer[newLength];
            UnsafeBuffer[] newUnsafeBuffers = new UnsafeBuffer[newLength];
            
            System.arraycopy(directBuffers, 0, newDirectBuffers, 0, directBuffers.length);
            System.arraycopy(unsafeBuffers, 0, newUnsafeBuffers, 0, unsafeBuffers.length);
            
            directBuffers = newDirectBuffers;
            unsafeBuffers = newUnsafeBuffers;
        }
        
        // Calculate the size for the new buffer
        int newBufferSize = Math.max(INITIAL_BUFFER_SIZE, requiredSize);
        newBufferSize = Math.min(newBufferSize * 2, MAX_BUFFER_SIZE); // Double size, but cap at max
        
        try {
            // Allocate a new buffer
            directBuffers[newIndex] = ByteBuffer.allocateDirect(newBufferSize);
            unsafeBuffers[newIndex] = new UnsafeBuffer(directBuffers[newIndex]);
            
            // Update current buffer index and position
            currentBufferIndex = newIndex;
            currentPosition = 0;
            
            // Update memory tracking
            offHeapBytesReserved.addAndGet(newBufferSize);
            
            LOG.debug("Allocated new buffer #{} with size: {} bytes", newIndex, newBufferSize);
            
            return true;
        } catch (OutOfMemoryError e) {
            LOG.error("Failed to allocate new buffer: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Compact the current buffer to reclaim unused space.
     */
    private boolean compactCurrentBuffer() {
        // This is a simplified compaction that just resets the buffer
        // if all nodes have been removed. A more sophisticated implementation
        // would move valid node data to the beginning of the buffer.
        
        // Check if there are any nodes in the current buffer
        boolean anyNodesInBuffer = nodeToLocation.values().stream()
            .anyMatch(loc -> loc.bufferIndex == currentBufferIndex);
        
        if (!anyNodesInBuffer) {
            // No nodes in this buffer, can reset position
            currentPosition = 0;
            return true;
        }
        
        return false;
    }
    
    /**
     * Compact all buffers to reclaim unused space.
     */
    private synchronized void compactBuffers() {
        // This is a simplified implementation that creates a new buffer
        // and copies all valid nodes to it.
        
        try {
            // Calculate total size needed
            long totalSize = nodeToLocation.values().stream()
                .mapToLong(loc -> loc.length)
                .sum();
            
            // Ensure the size is within limits and reasonable
            int newBufferSize = (int)Math.min(Math.max(totalSize * 1.2, INITIAL_BUFFER_SIZE), MAX_BUFFER_SIZE);
            
            // Create a new buffer
            ByteBuffer newDirectBuffer = ByteBuffer.allocateDirect(newBufferSize);
            UnsafeBuffer newUnsafeBuffer = new UnsafeBuffer(newDirectBuffer);
            
            // Create new mappings
            Map<Node, NodeLocation> newNodeToLocation = new ConcurrentHashMap<>(nodeToLocation.size());
            
            // Copy nodes to the new buffer
            int newPosition = 0;
            for (Map.Entry<Node, NodeLocation> entry : nodeToLocation.entrySet()) {
                Node node = entry.getKey();
                NodeLocation oldLoc = entry.getValue();
                
                // Read the node bytes from the old buffer
                byte[] nodeBytes = new byte[oldLoc.length];
                unsafeBuffers[oldLoc.bufferIndex].getBytes(oldLoc.position, nodeBytes);
                
                // Write to the new buffer
                int position = newPosition;
                newUnsafeBuffer.putBytes(position, nodeBytes);
                newPosition += nodeBytes.length;
                
                // Create a new location
                NodeLocation newLoc = new NodeLocation(0, position, nodeBytes.length);
                newNodeToLocation.put(node, newLoc);
            }
            
            // Update the buffers
            long oldReserved = offHeapBytesReserved.get();
            
            directBuffers = new ByteBuffer[] { newDirectBuffer };
            unsafeBuffers = new UnsafeBuffer[] { newUnsafeBuffer };
            currentBufferIndex = 0;
            currentPosition = newPosition;
            
            // Update mappings
            nodeToLocation.clear();
            nodeToLocation.putAll(newNodeToLocation);
            
            // Update memory tracking
            offHeapBytesReserved.set(newBufferSize);
            
            LOG.debug("Compacted buffers from {} bytes to {} bytes", oldReserved, newBufferSize);
        } catch (OutOfMemoryError e) {
            LOG.error("Failed to compact buffers: {}", e.getMessage());
        }
    }
    
    /**
     * Compute a hash for a node.
     */
    private String computeNodeHash(Node node) {
        // This is a simplified implementation - a more robust solution
        // would use a proper hashing algorithm
        StringBuilder sb = new StringBuilder();
        
        if (node.isURI()) {
            sb.append("U:");
            sb.append(node.getURI());
        } else if (node.isBlank()) {
            sb.append("B:");
            sb.append(node.getBlankNodeLabel());
        } else if (node.isLiteral()) {
            sb.append("L:");
            sb.append(node.getLiteralLexicalForm());
            
            if (node.getLiteralLanguage() != null && !node.getLiteralLanguage().isEmpty()) {
                sb.append("@");
                sb.append(node.getLiteralLanguage());
            } else if (node.getLiteralDatatype() != null) {
                sb.append("^^");
                sb.append(node.getLiteralDatatypeURI());
            }
        } else if (node.isVariable()) {
            sb.append("V:");
            sb.append(node.getName());
        }
        
        return sb.toString();
    }
    
    /**
     * Deserialize a node from its location.
     */
    private Node deserializeNode(NodeLocation location) {
        try {
            // Read the node bytes from the buffer
            byte[] nodeBytes = new byte[location.length];
            unsafeBuffers[location.bufferIndex].getBytes(location.position, nodeBytes);
            
            // Parse the node type
            byte nodeType = nodeBytes[0];
            
            // Parse the node based on type
            switch (nodeType) {
                case 1: // URI
                    String uri = parseString(nodeBytes, 1);
                    return NodeFactory.createURI(uri);
                case 2: // Blank node
                    String label = parseString(nodeBytes, 1);
                    return NodeFactory.createBlankNode(label);
                case 3: // Literal
                    String lexicalForm = parseString(nodeBytes, 1);
                    int posAfterLex = 1 + 4 + lexicalForm.getBytes().length;
                    byte literalType = nodeBytes[posAfterLex];
                    
                    if (literalType == 1) {
                        // Language string
                        String lang = parseString(nodeBytes, posAfterLex + 1);
                        return NodeFactory.createLiteral(lexicalForm, lang, null);
                    } else if (literalType == 2) {
                        // Typed literal
                        String datatypeUri = parseString(nodeBytes, posAfterLex + 1);
                        return NodeFactory.createLiteral(lexicalForm, null, NodeFactory.getType(datatypeUri));
                    } else {
                        // Simple literal
                        return NodeFactory.createLiteral(lexicalForm);
                    }
                case 4: // Variable
                    String name = parseString(nodeBytes, 1);
                    return NodeFactory.createVariable(name);
                default:
                    LOG.error("Unknown node type: {}", nodeType);
                    return null;
            }
        } catch (Exception e) {
            LOG.error("Error deserializing node", e);
            return null;
        }
    }
    
    /**
     * Parse a string from bytes.
     */
    private String parseString(byte[] bytes, int startPos) {
        // Read string length
        int length = Bytes.getInt(bytes, startPos);
        
        // Read string bytes
        byte[] stringBytes = new byte[length];
        System.arraycopy(bytes, startPos + 4, stringBytes, 0, length);
        
        return new String(stringBytes);
    }
    
    /**
     * Get cache hit statistics.
     */
    public long getCacheHits() {
        return cacheHits.get();
    }
    
    /**
     * Get cache miss statistics.
     */
    public long getCacheMisses() {
        return cacheMisses.get();
    }
    
    /**
     * Get the cache hit ratio.
     */
    public double getCacheHitRatio() {
        long hits = cacheHits.get();
        long total = hits + cacheMisses.get();
        return total > 0 ? (double)hits / total : 0.0;
    }
    
    /**
     * Throw an exception if the cache is closed.
     */
    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("Node cache has been closed");
        }
    }
}