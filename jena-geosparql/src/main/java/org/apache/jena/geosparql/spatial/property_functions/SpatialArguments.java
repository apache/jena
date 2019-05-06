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
package org.apache.jena.geosparql.spatial.property_functions;

import java.util.Objects;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.spatial.SearchEnvelope;

/**
 *
 *
 */
public class SpatialArguments {

    protected final int limit;
    protected final GeometryWrapper geometryWrapper;
    protected final SearchEnvelope searchEnvelope;

    public SpatialArguments(int limit, GeometryWrapper geometryWrapper, SearchEnvelope searchEnvelope) {
        this.limit = limit;
        this.geometryWrapper = geometryWrapper;
        this.searchEnvelope = searchEnvelope;
    }

    public int getLimit() {
        return limit;
    }

    public GeometryWrapper getGeometryWrapper() {
        return geometryWrapper;
    }

    public SearchEnvelope getSearchEnvelope() {
        return searchEnvelope;
    }

    @Override
    public String toString() {
        return "SpatialArguments{" + "limit=" + limit + ", geometryWrapper=" + geometryWrapper + ", searchEnvelope=" + searchEnvelope + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.limit;
        hash = 53 * hash + Objects.hashCode(this.geometryWrapper);
        hash = 53 * hash + Objects.hashCode(this.searchEnvelope);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SpatialArguments other = (SpatialArguments) obj;
        if (this.limit != other.limit) {
            return false;
        }
        if (!Objects.equals(this.geometryWrapper, other.geometryWrapper)) {
            return false;
        }
        return Objects.equals(this.searchEnvelope, other.searchEnvelope);
    }

}
