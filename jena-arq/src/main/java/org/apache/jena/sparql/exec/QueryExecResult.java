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

package org.apache.jena.sparql.exec;

import java.util.Iterator;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.resultset.SPARQLResult;

/**
 * This class is for the outcome of {@link QueryExec}.
 * <p>
 * See {@link SPARQLResult} for The Model-level equivalent.
 */

public class QueryExecResult {
    private boolean       hasBeenSet       = false;

    private RowSet        resultSet        = null;
    private Boolean       booleanResult    = null;
    private Graph         graph            = null;
    private DatasetGraph  dataset          = null;
    private Iterator<JsonObject> jsonItems = null;

    // Delayed choice of result type.
    protected QueryExecResult() {}

    public QueryExecResult(Graph model) {
        set(graph);
    }

    public QueryExecResult(RowSet rowSet) {
        set(rowSet);
    }

    public QueryExecResult(boolean booleanResult) {
        set(booleanResult);
    }

    public QueryExecResult(DatasetGraph dataset) {
        set(dataset);
    }

    public QueryExecResult(Iterator<JsonObject> jsonItems) {
        set(jsonItems);
    }

    public boolean isSet() {
        return hasBeenSet;
    }

    public boolean isRowSet() {
        if ( !hasBeenSet )
            throw new RowSet.Exception("Not set");
        return resultSet != null;
    }

    public boolean isGraph() {
        if ( !hasBeenSet )
            throw new RowSet.Exception("Not set");
        return graph != null;
    }

    public boolean isDataset() {
        if ( !hasBeenSet )
            throw new RowSet.Exception("Not set");
        return dataset != null;
    }

    public boolean isBoolean() {
        if ( !hasBeenSet )
            throw new RowSet.Exception("Not set");
        return booleanResult != null;
    }

    public boolean isJson()
    {
        if ( !hasBeenSet )
            throw new RowSet.Exception("Not set");
        return jsonItems != null;
    }

    public RowSet rowSet() {
        if ( !hasBeenSet )
            throw new RowSet.Exception("Not set");
        if ( !isRowSet() )
            throw new RowSet.Exception("Not a RowSet result");
        return resultSet;
    }

    public Boolean booleanResult() {
        if ( !hasBeenSet )
            throw new RowSet.Exception("Not set");
        if ( !isBoolean() )
            throw new RowSet.Exception("Not a boolean result");
        return booleanResult;
    }

    public Graph graph() {
        if ( !hasBeenSet )
            throw new RowSet.Exception("Not set");
        if ( !isGraph() )
            throw new RowSet.Exception("Not a graph result");
        return graph;
    }

    public DatasetGraph dataset() {
        if ( !hasBeenSet )
            throw new RowSet.Exception("Not set");
        if ( !isDataset() )
            throw new RowSet.Exception("Not a dataset result");
        return dataset;
    }

    public Iterator<JsonObject> jsonItems()
    {
        if ( !hasBeenSet )
            throw new RowSet.Exception("Not set");
        if ( !isJson() )
            throw new RowSet.Exception("Not a JSON result");
        return jsonItems;
    }

    protected void clear() {
        hasBeenSet    = false;
        resultSet     = null;
        booleanResult = null;
        graph         = null;
        dataset       = null;
        jsonItems     = null;

    }

    protected void set(RowSet rs) {
        resultSet = rs;
        hasBeenSet = true;
    }

    protected void set(Graph g) {
        graph = g;
        hasBeenSet = true;
    }

    protected void set(DatasetGraph dsg) {
        dataset = dsg;
        hasBeenSet = true;
    }

    protected void set(Boolean r) {
        booleanResult = r;
        hasBeenSet = true;
    }

    protected void set(Iterator<JsonObject> jsonItems) {
        this.jsonItems = jsonItems;
        hasBeenSet = true;
    }


}

