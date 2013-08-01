/**
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

package org.apache.jena.jdbc.postprocessing;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.jena.jdbc.results.metadata.AskResultsMetadata;
import org.apache.jena.jdbc.results.metadata.SelectResultsMetadata;
import org.apache.jena.jdbc.results.metadata.TripleResultsMetadata;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ResultSet;

/**
 * A trivial results post-processor implementation which simply returns the
 * input
 * 
 */
public class ResultsEcho implements ResultsPostProcessor {

    @Override
    public void initialize(Properties props) throws SQLException {
        // No op
    }

    @Override
    public ResultSet postProcessResults(ResultSet results) {
        return results;
    }

    @Override
    public Iterator<Triple> postProcessResults(Iterator<Triple> triples) {
        return triples;
    }

    @Override
    public boolean postProcessResults(boolean result) {
        return result;
    }

    @Override
    public SelectResultsMetadata postProcessResultsMetadata(SelectResultsMetadata metadata) {
        return metadata;
    }

    @Override
    public TripleResultsMetadata postProcessResultsMetadata(TripleResultsMetadata metadata) {
        return metadata;
    }

    @Override
    public AskResultsMetadata postProcessResultsMetadata(AskResultsMetadata metadata) {
        return metadata;
    }

}
