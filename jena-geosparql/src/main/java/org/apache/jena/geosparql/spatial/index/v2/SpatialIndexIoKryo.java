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

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.geosparql.configuration.GeoSPARQLOperations;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.kryo.GeometrySerializerJtsWkb;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.query.Dataset;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class SpatialIndexIoKryo {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static SpatialIndex buildSpatialIndex(Dataset dataset,
                                                 String srsURI,
                                                 Path spatialIndexFile) throws SpatialIndexException {

        SpatialIndexPerGraph spatialIndex = load(spatialIndexFile);

        if (spatialIndex.isEmpty()) {
            spatialIndex = SpatialIndexUtils.buildSpatialIndex(dataset.asDatasetGraph(), srsURI);
            save(spatialIndexFile, spatialIndex);
        }
        spatialIndex.setLocation(spatialIndexFile);

        SpatialIndexUtils.setSpatialIndex(dataset, spatialIndex);
        return spatialIndex;
    }

    public static SpatialIndex buildSpatialIndex(Dataset dataset, Path spatialIndexFile) throws SpatialIndexException {
        String srsURI = GeoSPARQLOperations.findModeSRS(dataset);
        SpatialIndex spatialIndex = buildSpatialIndex(dataset, srsURI, spatialIndexFile);
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
        Path absPath = spatialIndexFile.toAbsolutePath();
        if (spatialIndexFile != null) {
            LOGGER.info("Saving Spatial Index - Started: {}", absPath);

            String filename = absPath.toString();
            Path file = Path.of(filename);
            Path tmpFile = IOX.uniqueDerivedPath(file, null);
            try {
                Files.deleteIfExists(file);
            } catch (IOException ex) {
                throw new SpatialIndexException("Failed to delete file: " + ex.getMessage());
            }
            try {
                IOX.safeWriteOrCopy(file, tmpFile, out -> writeToOutputStream(out, index));
            } catch (RuntimeIOException ex) {
                throw new SpatialIndexException("Save Exception: " + ex.getMessage());
            } finally {
                LOGGER.info("Saving Spatial Index - Completed: {}", absPath);
            }

        }
    }

    /**
     * Write spatial index as Kryo serialization to given OutputStream.
     * @param os output stream
     * @param index spatial index
     */
    public static void writeToOutputStream(OutputStream os, SpatialIndexPerGraph index) {
        GeometrySerializerJtsWkb geometrySerde = new GeometrySerializerJtsWkb();

        SpatialIndexHeader header = new SpatialIndexHeader();
        header.setType(SpatialIndexHeader.TYPE_VALUE);
        header.setVersion("2.0.0");
        header.setSrsUri(index.getSrsInfo().getSrsURI());
        header.setGeometrySerializerClass(geometrySerde.getClass().getName());

        Kryo kryo = new Kryo();
        KryoRegistratorSpatialIndexV2.registerClasses(kryo, geometrySerde);
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

        if (spatialIndexFile != null && Files.exists(spatialIndexFile)) {
            spatialIndexFile = spatialIndexFile.toAbsolutePath();
            LOGGER.info("Loading Spatial Index - Started: {}", spatialIndexFile);

            try (Input input = new Input(Files.newInputStream(spatialIndexFile))) {
                SpatialIndexHeader header = readHeader(input);

                String type = header.getType();
                if (!"jena-spatial-index".equals(type)) {
                    throw new RuntimeException("Type does not indicate a spatial index file.");
                }

                String version = header.getVersion();
                if (!"2.0.0".equals(version)) {
                    throw new SpatialIndexException("The version of the spatial index does not match the version of this loader class.");
                }

                srsUri = header.getSrsUri();

                // Get the geometrySerializer attribute
                String geometrySerdeName = header.getGeometrySerializerClass();
                Objects.requireNonNull(geometrySerdeName, "Field 'geometrySerde' not set.");

                Serializer<Geometry> geometrySerializer;
                try {
                    Class<?> geometrySerdeClass = Class.forName(geometrySerdeName);
                    geometrySerializer = (Serializer<Geometry>)geometrySerdeClass.getConstructor().newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | ClassCastException | SecurityException e) {
                    throw new SpatialIndexException("Failed to load index", e);
                }

                Kryo kryo = new Kryo();
                KryoRegistratorSpatialIndexV2.registerClasses(kryo, geometrySerializer);

                index = kryo.readObject(input, STRtreePerGraph.class);
                LOGGER.info("Loading Spatial Index - Completed: {}", spatialIndexFile);
            } catch (IOException ex) {
                throw new SpatialIndexException("Loading Exception: " + ex.getMessage(), ex);
            }
        } else {
            LOGGER.info("File {} does not exist. Creating empty Spatial Index.", (spatialIndexFile != null ? spatialIndexFile.toAbsolutePath() : "null"));
            srsUri = SRS_URI.DEFAULT_WKT_CRS84;
            index = new STRtreePerGraph();
        }

        SRSInfo srsInfo = SRSRegistry.getSRSInfo(srsUri);
        SpatialIndexPerGraph spatialIndex = new SpatialIndexPerGraph(srsInfo, index, spatialIndexFile);
        spatialIndex.setLocation(spatialIndexFile);
        return spatialIndex;
    }
}
