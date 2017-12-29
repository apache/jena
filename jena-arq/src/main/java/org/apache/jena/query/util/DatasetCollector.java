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

package org.apache.jena.query.util;

import static org.apache.jena.atlas.iterator.Iter.filter;
import static org.apache.jena.system.Txn.executeRead;
import static org.apache.jena.system.Txn.executeWrite;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.IdentityFinishCollector.UnorderedIdentityFinishCollector;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;

public abstract class DatasetCollector implements UnorderedIdentityFinishCollector<Dataset, Dataset> {

    @Override
    public Supplier<Dataset> supplier() {
        return DatasetFactory::createGeneral;
    }

    public ConcurrentDatasetCollector concurrent() {
        return new ConcurrentDatasetCollector(this);
    }

    /**
     * Use only with {@link Dataset}s that support transactions.
     */
    public static class ConcurrentDatasetCollector extends DatasetCollector
            implements ConcurrentUnorderedIdentityFinishCollector<Dataset, Dataset> {

        private final DatasetCollector collector;

        public ConcurrentDatasetCollector(DatasetCollector col) {
            this.collector = col;
        }

        @Override
        public BinaryOperator<Dataset> combiner() {
            return collector.combiner();
        }

        @Override
        public BiConsumer<Dataset, Dataset> accumulator() {
            return (d1, d2) -> executeRead(d2, () -> executeWrite(d1, () -> collector.accumulator().accept(d1, d2)));
        }
    }

    public static class UnionDatasetCollector extends DatasetCollector {

        @Override
        public BinaryOperator<Dataset> combiner() {
            return DatasetLib::union;
        }

        @Override
        public BiConsumer<Dataset, Dataset> accumulator() {
            return (d1, d2) -> {
                d1.getDefaultModel().add(d2.getDefaultModel());
                d2.listNames().forEachRemaining(
                        name -> d1.replaceNamedModel(name, d1.getNamedModel(name).union(d2.getNamedModel(name))));
            };
        }
    }

    public static class IntersectionDatasetCollector extends DatasetCollector {

        @Override
        public BinaryOperator<Dataset> combiner() {
            return DatasetLib::intersection;
        }

        @Override
        public BiConsumer<Dataset, Dataset> accumulator() {
            return (d1, d2) -> {
                d1.setDefaultModel(d1.getDefaultModel().intersection(d2.getDefaultModel()));
                filter(d2.listNames(), d1::containsNamedModel).forEachRemaining(name -> {
                    Model intersection = d1.getNamedModel(name).intersection(d2.getNamedModel(name));
                    d1.replaceNamedModel(name, intersection);
                });
            };
        }
    }
    
    static DatasetCollector union() { 
        return new UnionDatasetCollector();
    }
    
    static DatasetCollector intersect() { 
        return new IntersectionDatasetCollector();
    }
}
