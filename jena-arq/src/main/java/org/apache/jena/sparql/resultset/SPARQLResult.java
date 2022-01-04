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

package org.apache.jena.sparql.resultset;

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.exec.QueryExecResult;

/**
 * The class "ResultSet" is reserved for the SELECT result format. This class
 * can hold a ResultSet, a boolean or a Model.
 */

public class SPARQLResult {
    private boolean   hasBeenSet    = false;

    private ResultSet resultSet     = null;
    private Boolean   booleanResult = null;
    private Model     model         = null;
    private Dataset   dataset       = null;
    private Iterator<JsonObject> jsonItems = null;

    public static SPARQLResult adapt(QueryExecResult result) {
        Objects.requireNonNull(result);
        if ( result.isRowSet())
            return new SPARQLResult(ResultSet.adapt(result.rowSet()));
        if ( result.isBoolean())
            return new SPARQLResult(result.booleanResult());
        if ( result.isGraph())
            return new SPARQLResult(ModelFactory.createModelForGraph(result.graph()));
        if ( result.isDataset())
            return new SPARQLResult(DatasetFactory.wrap(result.dataset()));
        if ( result.isJson())
            return new SPARQLResult(result.jsonItems());
        throw new IllegalArgumentException("Can not convert to a SPARQLresult object");
    }

    protected SPARQLResult() {}

    public SPARQLResult(Model model) {
        set(model);
    }

    public SPARQLResult(ResultSet resultSet) {
        set(resultSet);
    }

    public SPARQLResult(boolean booleanResult) {
        set(booleanResult);
    }

    public SPARQLResult(Dataset dataset) {
        set(dataset);
    }

    public SPARQLResult(Iterator<JsonObject> jsonItems) {
        set(jsonItems);
    }

    public boolean isResultSet() {
        if ( !hasBeenSet )
            throw new ResultSetException("Not set");
        return resultSet != null;
    }

    /** Synonym for isGraph */
    public boolean isModel() {
        return isGraph();
    }

    public boolean isGraph() {
        if ( !hasBeenSet )
            throw new ResultSetException("Not set");
        return model != null;
    }

    public boolean isDataset() {
        if ( !hasBeenSet )
            throw new ResultSetException("Not set");
        return dataset != null;
    }

    public boolean isBoolean() {
        if ( !hasBeenSet )
            throw new ResultSetException("Not set");
        return booleanResult != null;
    }

    public boolean isJson()
    {
        if ( !hasBeenSet )
            throw new ResultSetException("Not set");
        return jsonItems != null;
    }

    public ResultSet getResultSet() {
        if ( !hasBeenSet )
            throw new ResultSetException("Not set");
        if ( !isResultSet() )
            throw new ResultSetException("Not a ResultSet result");
        return resultSet;
    }

    public Boolean getBooleanResult() {
        if ( !hasBeenSet )
            throw new ResultSetException("Not set");
        if ( !isBoolean() )
            throw new ResultSetException("Not a boolean result");
        return booleanResult;
    }

    public Model getModel() {
        if ( !hasBeenSet )
            throw new ResultSetException("Not set");
        if ( !isModel() )
            throw new ResultSetException("Not a graph result");
        return model;
    }

    public Dataset getDataset() {
        if ( !hasBeenSet )
            throw new ResultSetException("Not set");
        if ( !isDataset() )
            throw new ResultSetException("Not a dataset result");
        return dataset;
    }

    public Iterator<JsonObject> getJsonItems()
    {
        if ( !hasBeenSet )
            throw new ResultSetException("Not set");
        if ( !isJson() )
            throw new ResultSetException("Not a JSON result");
        return jsonItems;
    }

    public boolean isHasBeenSet() {
        return hasBeenSet;
    }

    protected void set(ResultSet rs) {
        resultSet = rs;
        hasBeenSet = true;
    }

    protected void set(Model m) {
        model = m;
        hasBeenSet = true;
    }

    protected void set(Dataset d) {
        dataset = d;
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
