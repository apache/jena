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

package org.apache.jena.geosparql.query;

import java.util.stream.Stream;

import org.apache.shadedJena550.geosparql.configuration.GeoSPARQLOperations;
import org.apache.shadedJena550.geosparql.spatial.SpatialIndexException;
import org.apache.shadedJena550.geosparql.spatial.index.v2.SpatialIndexLib;
import org.apache.shadedJena550.graph.Graph;
import org.apache.shadedJena550.rdfs.RDFSFactory;
import org.apache.shadedJena550.riot.Lang;
import org.apache.shadedJena550.riot.RDFParser;
import org.apache.shadedJena550.sparql.core.DatasetGraph;
import org.apache.shadedJena550.sparql.core.DatasetGraphFactory;
import org.apache.shadedJena550.sparql.core.Quad;
import org.apache.shadedJena550.sparql.exec.QueryExec;
import org.apache.shadedJena550.sparql.exec.RowSetOps;

public class SpatialQueryTask550
    implements SpatialQueryTask
{
    private DatasetGraph baseDsg = null;
    private DatasetGraph effectiveDsg = null;
    private String query;

    @Override
    public void setData(String trigString) throws Exception {
        baseDsg = RDFParser.create().fromString(trigString).lang(Lang.TRIG).toDatasetGraph();
    }

    @Override
    public void setQuery(String queryString) throws Exception {
        this.query = queryString;
    }

    @Override
    public void setInferenceMode(boolean enableInferences, boolean materialize) {
        if (enableInferences) {
            Graph vocab = GeoSPARQLOperations.loadGeoSPARQLSchema().getGraph();
            DatasetGraph virtualDsg = RDFSFactory.datasetRDFS(baseDsg, vocab);
            if (materialize) {
                effectiveDsg = DatasetGraphFactory.create();

                // Bugged in 5.5.0 because find() is not overridden to yield inferences:
                // effectiveDsg.addAll(virtualDsg);

                try (Stream<Quad> stream = virtualDsg.stream(null, null, null, null)) {
                    stream.forEach(effectiveDsg::add);
                }
            } else {
                effectiveDsg = virtualDsg;
            }
        } else {
            effectiveDsg = baseDsg;
        }

        // RDFDataMgr.write(System.err, effectiveDsg, RDFFormat.TRIG_PRETTY);
    }

    @Override
    public void setIndex(boolean isEnabled) {
        if (isEnabled) {
            try {
                SpatialIndexLib.buildSpatialIndex(effectiveDsg);
            } catch (SpatialIndexException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public long exec() {
        try (QueryExec qe = QueryExec.dataset(effectiveDsg).query(query).build()) {
            long count = RowSetOps.count(qe.select());
            return count;
        }
    }
}
