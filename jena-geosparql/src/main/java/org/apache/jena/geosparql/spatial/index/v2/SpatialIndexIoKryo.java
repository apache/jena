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
package org.apache.jena.geosparql.spatial.index.v2;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.apache.jena.geosparql.kryo.GeometrySerializerJtsWkb;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.query.Dataset;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class SpatialIndexIoKryo {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // Flag whether additional serializers for storing geometries in the index should be registered.
    // The current version of the index only stores the envelopes so this feature is not needed.
    private static boolean enableGeometrySerde = false;

    /** Kryo4-based serialization is now obsolete. */
    private static final Set<String> OBSOLETE_VERSIONS = Set.of("2.0.0");

    /** The version of the index that is created by this class. */
    public static final String VERSION = "3.0.0";

    public static SpatialIndex loadOrBuildSpatialIndex(Dataset dataset, Path spatialIndexFile) throws SpatialIndexException {
        SpatialIndex spatialIndex = loadOrBuildSpatialIndex(dataset, null, spatialIndexFile);
        return spatialIndex;
    }

    private static boolean isNonEmptyFile(Path path) {
        boolean result = false;
        if (path != null && Files.exists(path)) {
            try {
                result = Files.size(path) > 0;
            } catch (IOException e) {
                throw IOX.exception(e);
            }
        }
        return result;
    }

    public static SpatialIndex loadOrBuildSpatialIndex(Dataset dataset,
                                                 String srsURI,
                                                 Path spatialIndexFile) throws SpatialIndexException {
        SpatialIndexPerGraph spatialIndex = null;

        // If the spatial index file exists and has non-zero size then load it.
        // Otherwise build one.
        if (isNonEmptyFile(spatialIndexFile)) {
            spatialIndex = load(spatialIndexFile);
            SpatialIndexLib.setSpatialIndex(dataset, spatialIndex);
        } else {
            spatialIndex = buildSpatialIndex(dataset, srsURI, spatialIndexFile);
        }

        return spatialIndex;
    }

    public static SpatialIndexPerGraph buildSpatialIndex(Dataset dataset,
                                                 String srsURI,
                                                 Path spatialIndexFile) throws SpatialIndexException {
        SpatialIndexPerGraph spatialIndex = SpatialIndexLib.buildSpatialIndex(dataset.asDatasetGraph(), srsURI);
        if (spatialIndexFile != null) {
            // Register the source file with the index.
            spatialIndex.setLocation(spatialIndexFile);
            save(spatialIndexFile, spatialIndex);
        }
        return spatialIndex;
    }

    /**
     * Save SpatialIndex to file.
     *
     * @param spatialIndexFile the file being saved to
     * @param index the spatial index
     * @throws SpatialIndexException
     */
    public static final void save(Path spatialIndexFile, SpatialIndexPerGraph index) throws SpatialIndexException {
        Path originalFile = spatialIndexFile.toAbsolutePath();
        LOGGER.info("Saving Spatial Index - Started: " + originalFile);

        // Create a temporary file for writing the new index.
        Path tmpFile = IOX.uniqueDerivedPath(originalFile, null);

        // As long as the new file has not been successfully written:
        // Move the original file out of the way but don't delete it yet.
        Path originalBackup = IOX.uniqueDerivedPath(originalFile, baseName -> baseName + ".bak");
        if (Files.exists(originalFile)) {
            IOX.moveAllowCopy(originalFile, originalBackup);
        }

        try {
            IOX.safeWriteOrCopy(originalFile, tmpFile, out -> writeToOutputStream(out, index));
            LOGGER.info("Saving Spatial Index - Success: " + originalFile);
        } catch (RuntimeIOException ex) {
            LOGGER.info("Failure writing spatial index: " + originalFile, ex);
            // Attempt to restore original file from backed up one.
            try {
                IOX.moveAllowCopy(originalBackup, originalFile);
            } catch (RuntimeException ex2) {
                LOGGER.warn("Failed to restore " + originalFile + " + from backup file " + originalBackup, ex2);
            }

            throw new SpatialIndexException("Save Exception: " + originalFile + " (via temp file: " + tmpFile + ")", ex);
        }

        // Delete backup
        try {
            Files.deleteIfExists(originalBackup);
        } catch (IOException ex) {
            LOGGER.warn("Failed to remove no longer needed backup: " + originalBackup, ex);
        }
    }

    /**
     * Write spatial index as Kryo serialization to given OutputStream.
     * @param os output stream
     * @param index spatial index
     */
    public static void writeToOutputStream(OutputStream os, SpatialIndexPerGraph index) {
        SpatialIndexHeader header = new SpatialIndexHeader();
        header.setType(SpatialIndexHeader.TYPE_VALUE);
        header.setVersion(VERSION);
        header.setSrsUri(index.getSrsInfo().getSrsURI());

        GeometrySerializerJtsWkb geometrySerializer = null;
        if (enableGeometrySerde) {
            geometrySerializer = new GeometrySerializerJtsWkb();
            header.setGeometrySerializerClass(geometrySerializer.getClass().getName());
        }

        Kryo kryo = new Kryo();
        KryoRegistratorSpatialIndexV2.registerClasses(kryo, geometrySerializer);
        try (Output output = new Output(os)) {
            writeHeader(output, header);
            STRtreePerGraph trees = index.getIndex();
            kryo.writeObject(output, trees);
            output.flush();
        }
    }

    public static void writeHeader(Output output, SpatialIndexHeader header) {
        Gson gson = new Gson();
        String headerStr = gson.toJson(header.getJson());
        output.writeString(headerStr);
    }

    /** Read a string from input and return it as JSON. */
    public static SpatialIndexHeader readHeader(Input input) {
        String headerStr = input.readString();
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(headerStr, JsonObject.class);
        return new SpatialIndexHeader(obj);
    }

    /**
     * Load a SpatialIndex from file.<br>
     * Index will be built and empty if file does not exist or is null.
     *
     * @param spatialIndexFile
     * @return Built Spatial Index.
     * @throws SpatialIndexException
     */
    @SuppressWarnings("unchecked")
    public static final SpatialIndexPerGraph load(Path spatialIndexFile) throws SpatialIndexException {
        String srsUri;
        STRtreePerGraph index;

        spatialIndexFile = spatialIndexFile.toAbsolutePath();
        LOGGER.info("Loading Spatial Index - Started: {}", spatialIndexFile);

        try (Input input = new Input(Files.newInputStream(spatialIndexFile))) {
            SpatialIndexHeader header = readHeader(input);

            String type = header.getType();
            if (!"jena-spatial-index".equals(type)) {
                throw new RuntimeException("Type does not indicate a spatial index file.");
            }

            String version = header.getVersion();
            if (!VERSION.equals(version)) {
                if (OBSOLETE_VERSIONS.contains(version)) {
                    throw new SpatialIndexException("Spatial index version " + version + " is no longer supported. Move or delete this file to allow for creation of a new index in its place: " + spatialIndexFile);
                } else {
                    throw new SpatialIndexException("Spatial index version " + version + " is not supported (expected version: " + VERSION + "). Offending file: " + spatialIndexFile);
                }
            }

            srsUri = header.getSrsUri();

            Serializer<Geometry> geometrySerializer = null;
            if (enableGeometrySerde) {
                String geometrySerdeName = header.getGeometrySerializerClass();
                Objects.requireNonNull(geometrySerdeName, "Field 'geometrySerde' not set.");

                try {
                    Class<?> geometrySerdeClass = Class.forName(geometrySerdeName);
                    geometrySerializer = (Serializer<Geometry>)geometrySerdeClass.getConstructor().newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | ClassCastException | SecurityException e) {
                    throw new SpatialIndexException("Failed to load index", e);
                }
            }

            Kryo kryo = new Kryo();
            KryoRegistratorSpatialIndexV2.registerClasses(kryo, geometrySerializer);

            index = kryo.readObject(input, STRtreePerGraph.class);
            LOGGER.info("Loading Spatial Index - Completed: {}", spatialIndexFile);
        } catch (IOException ex) {
            throw new SpatialIndexException("Loading Exception: " + ex.getMessage(), ex);
        }

        SRSInfo srsInfo = SRSRegistry.getSRSInfo(srsUri);
        SpatialIndexPerGraph spatialIndex = new SpatialIndexPerGraph(srsInfo, index, spatialIndexFile);
        spatialIndex.setLocation(spatialIndexFile);
        return spatialIndex;
    }
}
