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
 * Interface for results post processors, post processors have the ability to
 * modify the raw SPARQL results or the JDBC results metadata before it is
 * provided for consumption to the caller of the JDBC APIs
 * 
 */
public interface ResultsPostProcessor {

    /**
     * Initializes the post-processor
     * <p>
     * Called when the post-processor is first created, properties object
     * provides access to all connection configuration parameters except
     * password
     * </p>
     * 
     * @param props
     *            Connection properties
     * @throws SQLException
     *             Thrown if there is a problem initializing the post-processor
     */
    public void initialize(Properties props) throws SQLException;

    /**
     * Post-process incoming SELECT results
     * <p>
     * This is invoked after Jena JDBC has executed the SELECT query but before
     * it gets wrapped in a JDBC result set. This allows an application to do
     * manipulations on the results before they get presented through the JDBC
     * API.
     * </p>
     * 
     * @param results
     *            Incoming results
     * @return Processed results
     * @throws SQLException
     *             Thrown if there is a problem applying the post-processor
     */
    public ResultSet postProcessResults(ResultSet results) throws SQLException;

    /**
     * Post-process incoming CONSTRUCT/DESCRIBE results
     * <p>
     * This is invoked after Jena JDBC has executed a CONSTRUCT/DESCRIBE query
     * but before it gets wrapped in a JDBC result set. This allows an
     * application to do manipulations on the results before they get presented
     * through the JDBC API.
     * </p>
     * 
     * @param triples
     *            Incoming results
     * @return Processed results
     * @throws SQLException
     *             Thrown if there is a problem applying the post-processor
     */
    public Iterator<Triple> postProcessResults(Iterator<Triple> triples) throws SQLException;

    /**
     * Post-process incoming ASK results
     * <p>
     * This is invoked after Jena JDBC has executed an ASK query but before it
     * gets wrapped in a JDBC result set. This allows an application to do
     * manipulations on the results before they get presented through the JDBC
     * API.
     * <p>
     * 
     * @param result
     *            Incoming results
     * @return Processed results
     * @throws SQLException
     *             Thrown if there is a problem applying the post-processor
     */
    public boolean postProcessResults(boolean result) throws SQLException;

    /**
     * Post-process SELECT results metadata
     * <p>
     * This is invoked after Jena JDBC has wrapped SELECT query results in a
     * JDBC result set and calculated metadata for the results but before that
     * metadata is presented through the JDBC API. This allows an application to
     * make adjustments to the metadata such as changing detected column types
     * in order to be work better with certain tools or to override the default
     * type mappings that Jena JDBC makes.
     * </p>
     * 
     * @param metadata
     *            Calculated metadata
     * @return Processed metadata
     * @throws SQLException
     *             Thrown if there is a problem applying the post-processor
     */
    public SelectResultsMetadata postProcessResultsMetadata(SelectResultsMetadata metadata) throws SQLException;

    /**
     * Post-process CONSTRUCT/DESCRIBE results metadata
     * <p>
     * This is invoked after Jena JDBC has wrapped CONSTRUCT/DESCRIBE query
     * results in a JDBC result set and calculated metadata for the results but
     * before that metadata is presented through the JDBC API. This allows an
     * application to make adjustments to the metadata such as changing detected
     * column types in order to be work better with certain tools or to override
     * the default type mappings that Jena JDBC makes.
     * </p>
     * 
     * @param metadata
     *            Calculated metadata
     * @return Processed metadata
     * @throws SQLException
     *             Thrown if there is a problem applying the post-processor
     */
    public TripleResultsMetadata postProcessResultsMetadata(TripleResultsMetadata metadata) throws SQLException;

    /**
     * Post-process ASK results metadata
     * <p>
     * This is invoked after Jena JDBC has wrapped ASK query results in a JDBC
     * result set and calculated metadata for the results but before that
     * metadata is presented through the JDBC API. This allows an application to
     * make adjustments to the metadata such as changing detected column types
     * in order to be work better with certain tools or to override the default
     * type mappings that Jena JDBC makes.
     * </p>
     * 
     * @param metadata
     *            Calculated metadata
     * @return Processed metadata
     * @throws SQLException
     *             Thrown if there is a problem applying the post-processor
     */
    public AskResultsMetadata postProcessResultsMetadata(AskResultsMetadata metadata) throws SQLException;
}
