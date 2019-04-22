/*
 * Copyright 2019 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.spatial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.locationtech.jts.geom.Envelope;

/**
 * Spatial Index Items in a Serializable form for file reading or writing.
 *
 */
public class SpatialIndexStorage implements Serializable {

    private final String srsURI;
    private final List<StorageItem> storageItems;

    public SpatialIndexStorage(Collection<SpatialIndexItem> spatialIndexItems, String srsURI) {

        this.srsURI = srsURI;

        this.storageItems = new ArrayList<>(spatialIndexItems.size());

        for (SpatialIndexItem spatialIndexItem : spatialIndexItems) {
            StorageItem storageItem = new StorageItem(spatialIndexItem.getEnvelope(), spatialIndexItem.getItem());
            storageItems.add(storageItem);
        }
    }

    public String getSrsURI() {
        return srsURI;
    }

    public Collection<SpatialIndexItem> getIndexItems() {

        List<SpatialIndexItem> indexItems = new ArrayList<>(storageItems.size());

        for (StorageItem storageItem : storageItems) {
            SpatialIndexItem indexItem = storageItem.getIndexItem();
            indexItems.add(indexItem);
        }

        return indexItems;
    }

    public SpatialIndex getSpatialIndex() throws SpatialIndexException {
        return new SpatialIndex(getIndexItems(), srsURI);
    }

    private class StorageItem implements Serializable {

        private final Envelope envelope;
        private final String uri;

        public StorageItem(Envelope envelope, Resource item) {
            this.envelope = envelope;
            this.uri = item.getURI();
        }

        public Envelope getEnvelope() {
            return envelope;
        }

        public String getUri() {
            return uri;
        }

        public Resource getItem() {
            return ResourceFactory.createResource(uri);
        }

        public SpatialIndexItem getIndexItem() {
            return new SpatialIndexItem(envelope, getItem());
        }

        @Override
        public String toString() {
            return "StorageItem{" + "envelope=" + envelope + ", uri=" + uri + '}';
        }

    }
}
