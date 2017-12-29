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

package org.apache.jena.util;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.IdentityFinishCollector.UnorderedIdentityFinishCollector;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public abstract class ModelCollector implements UnorderedIdentityFinishCollector<Model, Model> {

    @Override
    public Supplier<Model> supplier() {
        return ModelFactory::createDefaultModel;
    }
    
    public ConcurrentModelCollector concurrent() {
        return new ConcurrentModelCollector(this);
    }

    public static class ConcurrentModelCollector extends ModelCollector
            implements ConcurrentUnorderedIdentityFinishCollector<Model, Model> {

        private final ModelCollector collector;

        public ConcurrentModelCollector(ModelCollector col) {
            this.collector = col;
        }

        @Override
        public BiConsumer<Model, Model> accumulator() {
            return (m1, m2) -> m2.executeInTxn(() -> m1.executeInTxn(() -> collector.accumulator().accept(m1, m2)));
        }

        @Override
        public BinaryOperator<Model> combiner() {
            return collector.combiner();
        }
    }

    public static class UnionModelCollector extends ModelCollector {

        @Override
        public BinaryOperator<Model> combiner() {
            return ModelFactory::createUnion;
        }

        @Override
        public BiConsumer<Model, Model> accumulator() {
            return Model::add;
        }
    }

    public static class IntersectionModelCollector extends ModelCollector {

        @Override
        public BinaryOperator<Model> combiner() {
            return Model::intersection;
        }

        @Override
        public BiConsumer<Model, Model> accumulator() {
            return (m1, m2) -> m1.remove(m1.difference(m2));
        }
    }
}
