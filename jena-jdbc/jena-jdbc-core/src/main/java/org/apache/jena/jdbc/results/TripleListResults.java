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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.apache.jena.atlas.iterator.PeekIterator;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.jdbc.results.metadata.TripleResultsMetadata;
import org.apache.jena.jdbc.statements.JenaStatement;
import org.apache.jena.query.QueryExecution ;

/**
 * Represents results of a CONSTRUCT/DESCRIBE query where the results are
 * materialized
 * 
 */
public class TripleListResults extends MaterializedResults<Triple> {

    private TripleResultsMetadata metadata;
    private List<Triple> triples;
    private String subjColumn, predColumn, objColumn;
    private int numColumns;

    /**
     * Creates a new result set which is backed by a triple iterator
     * 
     * @param statement
     *            Statement
     * @param qe
     *            Query Execution
     * @param ts
     *            Triple Iterator
     * @param commit
     *            Whether a commit is necessary when the result set is closed
     * @throws SQLException
     *             Thrown if there is a problem creating the results
     */
    public TripleListResults(JenaStatement statement, QueryExecution qe, List<Triple> ts, boolean commit) throws SQLException {
        super(statement, qe, commit);
        if (ts == null)
            throw new SQLException("Triple Iterator cannot be null");
        this.triples = ts;
        this.metadata = statement.getJenaConnection().applyPostProcessors(
                new TripleResultsMetadata(this, new PeekIterator<Triple>(ts.iterator())));
        this.numColumns = this.metadata.getColumnCount();
        this.subjColumn = this.metadata.getSubjectColumnLabel();
        this.predColumn = this.metadata.getPredicateColumnLabel();
        this.objColumn = this.metadata.getObjectColumnLabel();
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        if (this.subjColumn != null && this.subjColumn.equals(columnLabel)) {
            return TripleResultsMetadata.COLUMN_INDEX_SUBJECT;
        } else if (this.predColumn != null && this.predColumn.equals(columnLabel)) {
            return this.subjColumn == null ? TripleResultsMetadata.COLUMN_INDEX_SUBJECT
                    : TripleResultsMetadata.COLUMN_INDEX_PREDICATE;
        } else if (this.objColumn != null && this.objColumn.equals(columnLabel)) {
            return this.subjColumn == null && this.predColumn == null ? TripleResultsMetadata.COLUMN_INDEX_SUBJECT
                    : (this.subjColumn == null || this.predColumn == null ? TripleResultsMetadata.COLUMN_INDEX_PREDICATE
                            : TripleResultsMetadata.COLUMN_INDEX_OBJECT);
        } else {
            throw new SQLException("Column " + columnLabel + " does not exist in these results");
        }
    }

    @Override
    protected boolean hasNext() {
        // No null check here because superclass will not call us after we are
        // closed and set to null
        return this.getRow() < this.triples.size();
    }

    @Override
    protected Triple moveNext() {
        return this.triples.get(this.getRow());
    }

    @Override
    protected boolean hasPrevious() {
        return this.getRow() > 1;
    }

    @Override
    protected Triple movePrevious() {
        return this.triples.get(this.getRow() - 1);
    }

    @Override
    protected int getTotalRows() {
        return this.triples.size();
    }

    @Override
    protected void closeStreamInternal() {
        if (this.triples != null) {
            this.triples = null;
        }
    }

    @Override
    public ResultSetMetaData getMetaData() {
        return metadata;
    }

    @Override
    protected String findColumnLabel(int columnIndex) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");
        if (columnIndex >= 1 && columnIndex <= this.numColumns) {
            switch (columnIndex) {
            case TripleResultsMetadata.COLUMN_INDEX_SUBJECT:
                return this.subjColumn != null ? this.subjColumn : (this.predColumn != null ? this.predColumn : this.objColumn);
            case TripleResultsMetadata.COLUMN_INDEX_PREDICATE:
                return this.subjColumn != null && this.predColumn != null ? this.predColumn : this.objColumn;
            case TripleResultsMetadata.COLUMN_INDEX_OBJECT:
                return this.objColumn;
            default:
                throw new SQLException("Column Index is out of bounds");
            }
        } else {
            throw new SQLException("Column Index is out of bounds");
        }
    }

    @Override
    protected Node getNode(String columnLabel) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result Set is closed");
        if (this.getCurrentRow() == null)
            throw new SQLException("Not currently at a row");
        Triple t = this.getCurrentRow();
        if (this.subjColumn != null && this.subjColumn.equals(columnLabel)) {
            return t.getSubject();
        } else if (this.predColumn != null && this.predColumn.equals(columnLabel)) {
            return t.getPredicate();
        } else if (this.objColumn != null && this.objColumn.equals(columnLabel)) {
            return t.getObject();
        } else {
            throw new SQLException("Unknown column label");
        }
    }
}
