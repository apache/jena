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
package org.apache.jena.geosparql.implementation.index;

import static org.apache.jena.geosparql.implementation.index.CacheConfiguration.NO_MAP;
import static org.apache.jena.geosparql.implementation.index.CacheConfiguration.UNLIMITED_MAP;

import java.util.UUID;

import org.apache.jena.geosparql.implementation.registry.MathTransformRegistry;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;

/**
 *
 *
 */
public class IndexConfiguration {

    public enum IndexOption {
        NONE, MEMORY
    }

    /*
     * Index Configuration Parameters
     */
    private static IndexOption indexOptionEnum = IndexOption.MEMORY;

    public static final void setConfig(IndexOption indexOption) {
        indexOptionEnum = indexOption;

        switch (indexOptionEnum) {
            case MEMORY:
                IndexConfiguration.setupMemoryIndex();
                break;
            default:
                IndexConfiguration.setupNoIndex();
        }
    }

    /**
     * Indexes are cleared, stopped and set to minimum storage size.
     */
    private static void setupNoIndex() {
        IndexConfiguration.resetIndexes();
        IndexConfiguration.stopIndexes();
        GeometryLiteralIndex.reset(NO_MAP, 0);
        GeometryTransformIndex.reset(NO_MAP, 0);
        QueryRewriteIndex.setMaxSize(NO_MAP);
    }

    /**
     * Indexes are set to unlimited storage and started.
     */
    private static void setupMemoryIndex() {
        GeometryLiteralIndex.reset(UNLIMITED_MAP, 0);
        GeometryTransformIndex.reset(UNLIMITED_MAP, 0);
        QueryRewriteIndex.setMaxSize(UNLIMITED_MAP);
        IndexConfiguration.startIndexes();
    }

    /**
     * Indexes are made active and started.
     */
    public static void startIndexes() {
        GeometryLiteralIndex.setIndexActive(true);
        GeometryTransformIndex.setIndexActive(true);
        //QueryRewriteIndex are on a Dataset basis.
    }

    /**
     * Indexes are made active and stopped.
     */
    public static void stopIndexes() {
        GeometryLiteralIndex.setIndexActive(false);
        GeometryTransformIndex.setIndexActive(false);
        //QueryRewriteIndex are on a Dataset basis.
    }

    public static final void resetIndexes() {
        GeometryLiteralIndex.clear();
        GeometryTransformIndex.clear();
        //QueryRewriteIndex are on a Dataset basis.
    }

    public static final void resetIndexesAndRegistries() {
        GeometryLiteralIndex.clear();
        GeometryTransformIndex.clear();
        SRSRegistry.reset();
        MathTransformRegistry.clear();
    }

    public static final IndexOption getIndexOption() {
        return indexOptionEnum;
    }

    public static final String createURI(String namespaceURI, String prefix) {
        return namespaceURI + prefix + "-" + UUID.randomUUID().toString();
    }

}
