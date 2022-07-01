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

package org.apache.jena.fuseki.server;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.fuseki.servlets.HttpAction;

/**
 * A pairing of name and {@link DataService}, a dataset and its endpoints (which may
 * in turn be named), in the URL space of the server
 */
public class DataAccessPoint {
    private final ValidString name;
    private final DataService dataService;
    private AtomicLong requests = new AtomicLong(0);

    public DataAccessPoint(String name, DataService dataService) {
        Objects.requireNonNull(name, "DataAccessPoint name");
        Objects.requireNonNull(dataService, "DataService");
        name = canonical(name);
        ValidString vs = Validators.serviceName(name);
        this.name = vs;
        this.dataService = dataService;
        dataService.noteDataAccessPoint(this);
    }

    public String getName()     { return name.string; }

    /**
     * Canonical name (path) for a dataset.
     * This always starts with "/".
     * It is the name within the Fuseki server, not the servlet context path.
     */
    public static String canonical(String datasetPath) {
        if ( datasetPath == null )
            return datasetPath;
        if ( datasetPath.equals("/") )
            return datasetPath;
        if ( datasetPath.equals("") )
            return "/";
        if ( !datasetPath.startsWith("/") )
            datasetPath = "/" + datasetPath;
        if ( datasetPath.endsWith("/") )
            datasetPath = datasetPath.substring(0, datasetPath.length() - 1);
        return datasetPath;
    }

    /**
     * Is the name canonical? (starts with "/").
     * It is the name within the Fuseki server, not the servlet context path.
     */
    public static boolean isCanonical(String datasetPath) {
        return datasetPath != null && datasetPath.startsWith("/");
    }

    public DataService getDataService() {
        return dataService;
    }

    public long requestCount()                          { return requests.get(); }

    public void startRequest(HttpAction httpAction)     { requests.incrementAndGet(); }

    public void finishRequest(HttpAction httpAction)    { requests.getAndDecrement(); }

    @Override public String toString() {
        return "DataAccessPoint["+name+"]";
    }
}

