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

package org.apache.jena.sparql.util.compose;

import static org.apache.jena.sys.Txn.*;

import java.util.function.*;

import org.apache.jena.atlas.lib.IdentityFinishCollector.UnorderedIdentityFinishCollector;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;

public abstract class DatasetCollector implements UnorderedIdentityFinishCollector<Dataset, Dataset> {

    @Override
    public Supplier<Dataset> supplier() {
        return DatasetFactory::createTxnMem;
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
            return (d1, d2) ->  calculateRead(d2, () -> calculateWrite(d1, () -> collector.combiner().apply(d1, d2)));
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
                d2.listNames().forEachRemaining(name -> {
                    Model union = d1.getNamedModel(name).union(d2.getNamedModel(name));
                    d1.replaceNamedModel(name, union);
                });
            };
        }
    }

    public static class IntersectionDatasetCollector extends DatasetCollector {

        /**
         * The first element is treated differently because
         * {@link DatasetCollector#supplier()} does not provide an identity element for
         * intersection.
         */
        private volatile boolean afterFirstElement = false;

        @Override
        public BinaryOperator<Dataset> combiner() {
            return DatasetLib::intersection;
        }

        @Override
        public BiConsumer<Dataset, Dataset> accumulator() {
            return (d1, d2) -> {
                if (afterFirstElement) {
                    d1.setDefaultModel(d1.getDefaultModel().intersection(d2.getDefaultModel()));
                    d1.listNames().forEachRemaining(name -> {
                        if (d2.containsNamedModel(name)) {
                            Model intersection = d1.getNamedModel(name).intersection(d2.getNamedModel(name));
                            d1.replaceNamedModel(name, intersection);
                        } else d1.removeNamedModel(name);
                    });
                } else {
                    // first element of the stream
                    d1.setDefaultModel(d2.getDefaultModel());
                    d2.listNames().forEachRemaining(name -> d1.replaceNamedModel(name, d2.getNamedModel(name)));
                    afterFirstElement = true;
                }
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
