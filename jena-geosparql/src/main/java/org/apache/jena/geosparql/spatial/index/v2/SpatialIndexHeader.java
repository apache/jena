/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jena.geosparql.spatial.index.v2;

import java.util.Objects;

import com.google.gson.JsonObject;

/**
 * The header for a spatial index.
 * For extensibility, this is merely a view over a JSON object.
 */
public class SpatialIndexHeader {
    public static final String TYPE_KEY = "type";
    public static final String VERSION_KEY = "version";
    public static final String SRS_KEY = "srs";
    public static final String GEOMETRY_SERIALIZER_KEY = "geometrySerializer";

    public static final String TYPE_VALUE = "jena-spatial-index";

    protected JsonObject json;

    public SpatialIndexHeader() {
        this(new JsonObject());
    }

    public SpatialIndexHeader(JsonObject json) {
        super();
        this.json = Objects.requireNonNull(json);
    }

    public JsonObject getJson() {
        return json;
    }

    public void setType(String type) {
        getJson().addProperty(TYPE_KEY, type);
    }

    public String getType() {
        return getJson().get(TYPE_KEY).getAsString();
    }

    public void setVersion(String version) {
        getJson().addProperty(VERSION_KEY, version);
    }

    public String getVersion() {
        return getJson().get(VERSION_KEY).getAsString();
    }

    public void setSrsUri(String type) {
        getJson().addProperty(SRS_KEY, type);
    }

    public String getSrsUri() {
        return getJson().get(SRS_KEY).getAsString();
    }

    public void setGeometrySerializerClass(String className) {
        getJson().addProperty(GEOMETRY_SERIALIZER_KEY, className);
    }

    public String getGeometrySerializerClass() {
        return getJson().get(GEOMETRY_SERIALIZER_KEY).getAsString();
    }
}
