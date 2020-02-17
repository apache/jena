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
package org.apache.jena.geosparql.spatial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.jena.geosparql.configuration.GeoSPARQLOperations;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.STRtree;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpatialIndex for testing bounding box collisions between geometries within a
 * Dataset.<br>
 * Queries must be performed using the same SRS URI as the SpatialIndex.<br>
 * The SpatialIndex is added to the Dataset Context when it is built.<br>
 * QueryRewriteIndex is also stored in the SpatialIndex as its content is
 * Dataset specific.
 *
 */
public class SpatialIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final Symbol SPATIAL_INDEX_SYMBOL = Symbol.create("http://jena.apache.org/spatial#index");

    private transient final SRSInfo srsInfo;
    private boolean isBuilt;
    private final STRtree strTree;
    private static final int MINIMUM_CAPACITY = 2;

    private SpatialIndex() {
        this.strTree = new STRtree(MINIMUM_CAPACITY);
        this.isBuilt = true;
        this.strTree.build();
        this.srsInfo = SRSRegistry.getSRSInfo(SRS_URI.DEFAULT_WKT_CRS84);
    }

    /**
     * Unbuilt Spatial Index with provided capacity.
     *
     * @param capacity
     * @param srsURI
     */
    public SpatialIndex(int capacity, String srsURI) {
        int indexCapacity = capacity < MINIMUM_CAPACITY ? MINIMUM_CAPACITY : capacity;
        this.strTree = new STRtree(indexCapacity);
        this.isBuilt = false;
        this.srsInfo = SRSRegistry.getSRSInfo(srsURI);
    }

    /**
     * Built Spatial Index with provided capacity.
     *
     * @param spatialIndexItems
     * @param srsURI
     * @throws SpatialIndexException
     */
    public SpatialIndex(Collection<SpatialIndexItem> spatialIndexItems, String srsURI) throws SpatialIndexException {
        int indexCapacity = spatialIndexItems.size() < MINIMUM_CAPACITY ? MINIMUM_CAPACITY : spatialIndexItems.size();
        this.strTree = new STRtree(indexCapacity);
        insertItems(spatialIndexItems);
        this.strTree.build();
        this.isBuilt = true;
        this.srsInfo = SRSRegistry.getSRSInfo(srsURI);
    }

    /**
     *
     * @return Information about the SRS used by the SpatialIndex.
     */
    public SRSInfo getSrsInfo() {
        return srsInfo;
    }

    /**
     *
     * @return True if the SpatialIndex is empty.
     */
    public boolean isEmpty() {
        return strTree.isEmpty();
    }

    /**
     *
     * @return True if the SpatialIndex has been built.
     */
    public boolean isBuilt() {
        return isBuilt;
    }

    /**
     * Build the Spatial Index. No more items can be added.
     */
    public void build() {
        if (!isBuilt) {
            strTree.build();
            isBuilt = true;
        }
    }

    /**
     * Items to add to an unbuilt Spatial Index.
     *
     * @param indexItems
     * @throws SpatialIndexException
     */
    public final void insertItems(Collection<SpatialIndexItem> indexItems) throws SpatialIndexException {

        for (SpatialIndexItem indexItem : indexItems) {
            insertItem(indexItem.getEnvelope(), indexItem.getItem());
        }
    }

    /**
     * Item to add to an unbuilt Spatial Index.
     *
     * @param envelope
     * @param item
     * @throws SpatialIndexException
     */
    public final void insertItem(Envelope envelope, Resource item) throws SpatialIndexException {
        if (!isBuilt) {
            strTree.insert(envelope, item);
        } else {
            throw new SpatialIndexException("SpatialIndex has been built and cannot have additional items.");
        }
    }

    @SuppressWarnings("unchecked")
    public HashSet<Resource> query(Envelope searchEnvelope) {
        if (!strTree.isEmpty()) {
            return new HashSet<>(strTree.query(searchEnvelope));
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public String toString() {
        return "SpatialIndex{" + "srsInfo=" + srsInfo + ", isBuilt=" + isBuilt + ", strTree=" + strTree + '}';
    }

    /**
     * Retrieve the SpatialIndex from the Context.
     *
     * @param execCxt
     * @return SpatialIndex contained in the Context.
     * @throws SpatialIndexException
     */
    public static final SpatialIndex retrieve(ExecutionContext execCxt) throws SpatialIndexException {

        Context context = execCxt.getContext();
        SpatialIndex spatialIndex = (SpatialIndex) context.get(SPATIAL_INDEX_SYMBOL, null);

        if (spatialIndex == null) {
            throw new SpatialIndexException("Dataset Context does not contain SpatialIndex.");
        }

        return spatialIndex;
    }

    /**
     *
     * @param execCxt
     * @return True if a SpatialIndex is defined in the ExecutionContext.
     */
    public static final boolean isDefined(ExecutionContext execCxt) {
        Context context = execCxt.getContext();
        return context.isDefined(SPATIAL_INDEX_SYMBOL);
    }

    /**
     * Set the SpatialIndex into the Context of the Dataset for later retrieval
     * and use in spatial functions.
     *
     * @param dataset
     * @param spatialIndex
     */
    public static final void setSpatialIndex(Dataset dataset, SpatialIndex spatialIndex) {
        Context context = dataset.getContext();
        context.set(SPATIAL_INDEX_SYMBOL, spatialIndex);
    }

    /**
     * Build Spatial Index from all graphs in Dataset.<br>
     * Dataset contains SpatialIndex in Context.<br>
     * Spatial Index written to file.
     *
     * @param dataset
     * @param srsURI
     * @param spatialIndexFile
     * @return SpatialIndex constructed.
     * @throws SpatialIndexException
     */
    public static SpatialIndex buildSpatialIndex(Dataset dataset, String srsURI, File spatialIndexFile) throws SpatialIndexException {

        SpatialIndex spatialIndex = load(spatialIndexFile);

        if (spatialIndex.isEmpty()) {
            Collection<SpatialIndexItem> spatialIndexItems = findSpatialIndexItems(dataset, srsURI);
            save(spatialIndexFile, spatialIndexItems, srsURI);
            spatialIndex = new SpatialIndex(spatialIndexItems, srsURI);
            spatialIndex.build();
        }

        setSpatialIndex(dataset, spatialIndex);
        return spatialIndex;
    }

    /**
     * Build Spatial Index from all graphs in Dataset.<br>
     * Dataset contains SpatialIndex in Context.<br>
     * SRS URI based on most frequent found in Dataset.<br>
     * Spatial Index written to file.
     *
     * @param dataset
     * @param spatialIndexFile
     * @return SpatialIndex constructed.
     * @throws SpatialIndexException
     */
    public static SpatialIndex buildSpatialIndex(Dataset dataset, File spatialIndexFile) throws SpatialIndexException {
        String srsURI = GeoSPARQLOperations.findModeSRS(dataset);
        SpatialIndex spatialIndex = buildSpatialIndex(dataset, srsURI, spatialIndexFile);
        return spatialIndex;
    }

    /**
     * Build Spatial Index from all graphs in Dataset.<br>
     * Dataset contains SpatialIndex in Context.
     *
     * @param dataset
     * @param srsURI
     * @return SpatialIndex constructed.
     * @throws SpatialIndexException
     */
    public static SpatialIndex buildSpatialIndex(Dataset dataset, String srsURI) throws SpatialIndexException {
        LOGGER.info("Building Spatial Index - Started");

        Collection<SpatialIndexItem> items = findSpatialIndexItems(dataset, srsURI);
        SpatialIndex spatialIndex = new SpatialIndex(items, srsURI);
        spatialIndex.build();
        setSpatialIndex(dataset, spatialIndex);
        LOGGER.info("Building Spatial Index - Completed");
        return spatialIndex;
    }

    /**
     * Find Spatial Index Items from all graphs in Dataset.<br>
     *
     * @param dataset
     * @param srsURI
     * @return SpatialIndexItems found.
     * @throws SpatialIndexException
     */
    public static Collection<SpatialIndexItem> findSpatialIndexItems(Dataset dataset, String srsURI) throws SpatialIndexException {
        //Default Model
        dataset.begin(ReadWrite.READ);
        Model defaultModel = dataset.getDefaultModel();
        Collection<SpatialIndexItem> items = getSpatialIndexItems(defaultModel, srsURI);

        //Named Models
        Iterator<String> graphNames = dataset.listNames();
        while (graphNames.hasNext()) {
            String graphName = graphNames.next();
            Model namedModel = dataset.getNamedModel(graphName);
            Collection<SpatialIndexItem> graphItems = getSpatialIndexItems(namedModel, srsURI);
            items.addAll(graphItems);
        }

        dataset.end();

        return items;
    }

    /**
     * Build Spatial Index from all graphs in Dataset.<br>
     * Dataset contains SpatialIndex in Context.<br>
     * SRS URI based on most frequent found in Dataset.
     *
     * @param dataset
     * @return SpatialIndex constructed.
     * @throws SpatialIndexException
     */
    public static SpatialIndex buildSpatialIndex(Dataset dataset) throws SpatialIndexException {
        String srsURI = GeoSPARQLOperations.findModeSRS(dataset);
        SpatialIndex spatialIndex = buildSpatialIndex(dataset, srsURI);
        return spatialIndex;
    }

    /**
     * Wrap Model in a Dataset and build SpatialIndex.
     *
     * @param model
     * @param srsURI
     * @return Dataset with default Model and SpatialIndex in Context.
     * @throws SpatialIndexException
     */
    public static final Dataset wrapModel(Model model, String srsURI) throws SpatialIndexException {

        Dataset dataset = DatasetFactory.createTxnMem();
        dataset.setDefaultModel(model);
        buildSpatialIndex(dataset, srsURI);

        return dataset;
    }

    /**
     * Wrap Model in a Dataset and build SpatialIndex.
     *
     * @param model
     * @return Dataset with default Model and SpatialIndex in Context.
     * @throws SpatialIndexException
     */
    public static final Dataset wrapModel(Model model) throws SpatialIndexException {
        Dataset dataset = DatasetFactory.createTxnMem();
        dataset.setDefaultModel(model);
        String srsURI = GeoSPARQLOperations.findModeSRS(dataset);
        buildSpatialIndex(dataset, srsURI);

        return dataset;
    }

    /**
     * Find items from the Model transformed to the SRS URI.
     *
     * @param model
     * @param srsURI
     * @return Items found in the Model in the SRS URI.
     * @throws SpatialIndexException
     */
    public static final Collection<SpatialIndexItem> getSpatialIndexItems(Model model, String srsURI) throws SpatialIndexException {

        List<SpatialIndexItem> items = new ArrayList<>();

        //Only add one set of statements as a converted dataset will duplicate the same info.
        if (model.contains(null, Geo.HAS_GEOMETRY_PROP, (Resource) null)) {
            LOGGER.info("Feature-hasGeometry-Geometry statements found.");
            if (model.contains(null, SpatialExtension.GEO_LAT_PROP, (Literal) null)) {
                LOGGER.warn("Lat/Lon Geo predicates also found but will not be added to index.");
            }
            Collection<SpatialIndexItem> geometryLiteralItems = getGeometryLiteralIndexItems(model, srsURI);
            items.addAll(geometryLiteralItems);
        } else if (model.contains(null, SpatialExtension.GEO_LAT_PROP, (Literal) null)) {
            LOGGER.info("Geo predicate statements found.");
            Collection<SpatialIndexItem> geoPredicateItems = getGeoPredicateIndexItems(model, srsURI);
            items.addAll(geoPredicateItems);
        }

        return items;
    }

    /**
     *
     * @param model
     * @param srsURI
     * @return GeometryLiteral items prepared for adding to SpatialIndex.
     * @throws SpatialIndexException
     */
    private static Collection<SpatialIndexItem> getGeometryLiteralIndexItems(Model model, String srsURI) throws SpatialIndexException {
        List<SpatialIndexItem> items = new ArrayList<>();
        StmtIterator stmtIt = model.listStatements(null, Geo.HAS_GEOMETRY_PROP, (Resource) null);
        while (stmtIt.hasNext()) {
            Statement stmt = stmtIt.nextStatement();

            Resource feature = stmt.getSubject();
            Resource geometry = stmt.getResource();

            ExtendedIterator<RDFNode> nodeIter = model.listObjectsOfProperty(geometry, Geo.HAS_SERIALIZATION_PROP);
            if (!nodeIter.hasNext()) {
                NodeIterator wktNodeIter = model.listObjectsOfProperty(geometry, Geo.AS_WKT_PROP);
                NodeIterator gmlNodeIter = model.listObjectsOfProperty(geometry, Geo.AS_GML_PROP);
                nodeIter = wktNodeIter.andThen(gmlNodeIter);
            }

            while (nodeIter.hasNext()) {
                Literal geometryLiteral = nodeIter.next().asLiteral();
                GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometryLiteral);

                try {
                    //Ensure all entries in the target SRS URI.
                    GeometryWrapper transformedGeometryWrapper = geometryWrapper.convertSRS(srsURI);

                    Envelope envelope = transformedGeometryWrapper.getEnvelope();
                    SpatialIndexItem item = new SpatialIndexItem(envelope, feature);
                    items.add(item);
                } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
                    throw new SpatialIndexException("Transformation Exception: " + geometryLiteral + ". " + ex.getMessage());
                }

            }
        }
        return items;
    }

    /**
     *
     * @param model
     * @param srsURI
     * @return Geo predicate objects prepared for adding to SpatialIndex.
     */
    private static Collection<SpatialIndexItem> getGeoPredicateIndexItems(Model model, String srsURI) throws SpatialIndexException {
        List<SpatialIndexItem> items = new ArrayList<>();
        ResIterator resIt = model.listResourcesWithProperty(SpatialExtension.GEO_LAT_PROP);

        while (resIt.hasNext()) {
            Resource feature = resIt.nextResource();

            Literal lat = feature.getRequiredProperty(SpatialExtension.GEO_LAT_PROP).getLiteral();
            Literal lon = feature.getRequiredProperty(SpatialExtension.GEO_LON_PROP).getLiteral();

            Literal latLonPoint = ConvertLatLon.toLiteral(lat.getFloat(), lon.getFloat());
            GeometryWrapper geometryWrapper = GeometryWrapper.extract(latLonPoint);

            try {
                //Ensure all entries in the target SRS URI.
                GeometryWrapper transformedGeometryWrapper = geometryWrapper.convertSRS(srsURI);

                Envelope envelope = transformedGeometryWrapper.getEnvelope();
                SpatialIndexItem item = new SpatialIndexItem(envelope, feature);
                items.add(item);
            } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
                throw new SpatialIndexException("Transformation Exception: " + geometryWrapper.getLexicalForm() + ". " + ex.getMessage());
            }
        }
        return items;
    }

    /**
     * Load a SpatialIndex from file.<br>
     * Index will be built and empty if file does not exist or is null.
     *
     * @param spatialIndexFile
     * @return Built Spatial Index.
     * @throws SpatialIndexException
     */
    public static final SpatialIndex load(File spatialIndexFile) throws SpatialIndexException {

        if (spatialIndexFile != null && spatialIndexFile.exists()) {
            LOGGER.info("Loading Spatial Index - Started: {}", spatialIndexFile.getAbsolutePath());
            //Cannot directly store the SpatialIndex due to Resources not being serializable, use SpatialIndexStorage class.
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(spatialIndexFile))) {
                SpatialIndexStorage storage = (SpatialIndexStorage) in.readObject();

                SpatialIndex spatialIndex = storage.getSpatialIndex();
                LOGGER.info("Loading Spatial Index - Completed: {}", spatialIndexFile.getAbsolutePath());
                return spatialIndex;
            } catch (ClassNotFoundException | IOException ex) {
                throw new SpatialIndexException("Loading Exception: " + ex.getMessage(), ex);
            }
        } else {
            return new SpatialIndex();
        }
    }

    /**
     * Save SpatialIndex contents to file.
     *
     * @param spatialIndexFileURI
     * @param spatialIndexItems
     * @param srsURI
     * @throws SpatialIndexException
     */
    public static final void save(String spatialIndexFileURI, Collection<SpatialIndexItem> spatialIndexItems, String srsURI) throws SpatialIndexException {
        save(new File(spatialIndexFileURI), spatialIndexItems, srsURI);
    }

    /**
     * Save SpatialIndex contents to file.
     *
     * @param spatialIndexFile
     * @param spatialIndexItems
     * @param srsURI
     * @throws SpatialIndexException
     */
    public static final void save(File spatialIndexFile, Collection<SpatialIndexItem> spatialIndexItems, String srsURI) throws SpatialIndexException {

        //Cannot directly store the SpatialIndex due to Resources not being serializable, use SpatialIndexStorage class.
        if (spatialIndexFile != null) {
            LOGGER.info("Saving Spatial Index - Started: {}", spatialIndexFile.getAbsolutePath());
            SpatialIndexStorage storage = new SpatialIndexStorage(spatialIndexItems, srsURI);
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(spatialIndexFile))) {
                out.writeObject(storage);
                LOGGER.info("Saving Spatial Index - Completed: {}", spatialIndexFile.getAbsolutePath());
            } catch (Exception ex) {
                throw new SpatialIndexException("Save Exception: " + ex.getMessage());
            }
        }
    }

}
