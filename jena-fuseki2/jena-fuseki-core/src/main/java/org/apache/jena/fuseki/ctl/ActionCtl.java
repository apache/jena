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

package org.apache.jena.fuseki.ctl;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.servlets.ActionLifecycle;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletProcessor;
import org.apache.jena.fuseki.system.ActionCategory;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Base class for control actions. These are servlets and do not go through Fuseki
 * dynamic dispatch. No statistics.
 */
public abstract class ActionCtl extends ServletProcessor implements ActionLifecycle {
    protected ActionCtl() {
        super(Fuseki.adminLog, ActionCategory.ADMIN);
    }

    @Override
    final public void process(HttpAction action) {
        executeLifecycle(action);
    }

    /**
     * Simple execution lifecycle for a SPARQL Request. No statistics.
     *
     * @param action
     */
    protected void executeLifecycle(HttpAction action) {
        validate(action);
        execute(action);
    }

    /** Get the item name - the part after the URI for the servlet (which is the container). */
    public static String getItemName(HttpAction action) {
        return action.request.getPathInfo();
    }

    /**
     * Get the item name - the part after the URI for the servlet (which is
     * the container) - treated as a dataset name.
     */
    public static String getItemDatasetName(HttpAction action) {
        String x = getItemName(action);
        if ( x == null )
            return null;
        while ( x.startsWith("//") )
            x = x.substring(1);
        return DataAccessPoint.canonical(x);
    }

    /**
     * Get the DataAccessPoint corresponding to the item name, or null.
     * @see #getItemDatasetName
     */
    public static DataAccessPoint getItemDataAccessPoint(HttpAction action) {
        String name = getItemDatasetName(action);
        return getItemDataAccessPoint(action, name);
    }

    /**
     * Get the DatasetGraph corresponding to the item name, or null.
     * @see #getItemDatasetName
     */
    public static DatasetGraph getItemDataset(HttpAction action) {
        DataAccessPoint dap = getItemDataAccessPoint(action);
        if ( dap == null )
            return null;
        return dap.getDataService().getDataset();
    }

    /**
     * Get the DataAccessPoint corresponding to the item name, or null.
     * @see #getItemDatasetName
     */
    public static DataAccessPoint getItemDataAccessPoint(HttpAction action, String name) {
        return action.getDataAccessPointRegistry().get(name);
    }
}
