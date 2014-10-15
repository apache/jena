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

package org.apache.jena.jdbc.results;

import java.sql.SQLException;
import org.apache.jena.jdbc.statements.JenaStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;

/**
 * Abstract base class for result sets that are backed by a {@link QueryExecution} 
 *
 */
public abstract class QueryExecutionResults extends JenaResultSet {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryExecutionResults.class);

    private QueryExecution qe;
    private boolean commit = false;

    /**
     * Creates new Query Execution backed results
     * @param statement Statement
     * @param qe Query Execution
     * @param commit Whether a commit is needed when the results are closed
     * @throws SQLException Thrown if there is an issue creating the results
     */
    public QueryExecutionResults(JenaStatement statement, QueryExecution qe, boolean commit) throws SQLException {
        super(statement);
        if (qe == null) throw new SQLException("Query Execution cannot be null");
        this.qe = qe;
        this.commit = commit;
    }

    /**
     * Closes the results which also closes the underlying {@link QueryExecution}
     */
    @Override
    public final void close() throws SQLException {
        if (this.qe != null) {
            try {
                // Close the query execution
                this.qe.close();
            } catch (Exception e) {
                LOGGER.error("Unexpected error closing underlying Jena query execution", e);
                throw new SQLException("Unexpected error closing the query execution", e);
            } finally {
                this.qe = null;
                
                // Commit if necessary
                if (this.commit) {
                    LOGGER.info("Result Set associated with an auto-committing transaction, performing a commit now");
                    this.getStatement().getConnection().commit();
                }
            }
        }
        this.closeInternal();
    }
    
    /**
     * Method which derived classes must implement to provide their own close logic
     * @throws SQLException Thrown if there is an issue closing the results
     */
    protected abstract void closeInternal() throws SQLException;

    @Override
    public final boolean isClosed() throws SQLException {
        return this.qe == null;
    }
    
    

}
