/*
 * Copyright 2018 .
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

import org.apache.jena.rdf.model.Resource;
import org.locationtech.jts.geom.Envelope;

/**
 *
 *
 */
public class SpatialIndexItem {

    private final Envelope envelope;
    private final Resource item;

    public SpatialIndexItem(Envelope envelope, Resource item) {
        this.envelope = envelope;
        this.item = item;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public Resource getItem() {
        return item;
    }

    @Override
    public String toString() {
        return "SpatialIndexItem{" + "envelope=" + envelope + ", item=" + item + '}';
    }

}
