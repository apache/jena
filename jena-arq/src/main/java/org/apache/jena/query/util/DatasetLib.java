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

import static org.apache.jena.sparql.util.Context.emptyContext;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.DifferenceDatasetGraph;
import org.apache.jena.sparql.util.IntersectionDatasetGraph;
import org.apache.jena.sparql.util.UnionDatasetGraph;

public class DatasetLib {

    public static Dataset union(final Dataset d1, final Dataset d2, Context c) {
        return DatasetFactory.wrap(new UnionDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph(), c));
    }

    public static Dataset union(final Dataset d1, final Dataset d2) {
        return union(d1, d2, emptyContext);
    }

    public static Dataset intersection(final Dataset d1, final Dataset d2, Context c) {
        return DatasetFactory.wrap(new IntersectionDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph(), c));
    }

    public static Dataset intersection(final Dataset d1, final Dataset d2) {
        return intersection(d1, d2, emptyContext);
    }

    public static Dataset difference(final Dataset d1, final Dataset d2, Context c) {
        return DatasetFactory.wrap(new DifferenceDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph(), c));
    }

    public static Dataset difference(final Dataset d1, final Dataset d2) {
        return DatasetFactory.wrap(new DifferenceDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph(), emptyContext));
    }

    public static Collectors collectors() {
        return Collectors.instance;
    }

    static class Collectors {

        static Collectors instance = new Collectors();

        public DatasetCollector union() {
            return DatasetCollector.union();
        }

        public DatasetCollector intersect() {
            return DatasetCollector.intersect();
        }
    }
}
