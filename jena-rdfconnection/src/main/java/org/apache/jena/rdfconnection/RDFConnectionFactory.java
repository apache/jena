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

package org.apache.jena.rdfconnection;

import org.apache.jena.query.Dataset;
import org.apache.jena.system.JenaSystem;

public class RDFConnectionFactory {
    static { JenaSystem.init(); }
    
    /** Create a connection to a remote location by URL.
     * This is the URL for the dataset.
     * 
     *  This call assumes the names of services as:
     *  <ul>
     *  <li>SPARQL Query endpoint : "sparql"
     *  <li>SPARQL Update endpoint : "update"
     *  <li>SPARQL Graph Store Protocol : "data"
     *  </ul>
     *  These are the default names in <a href="http://jena.apache.org/documentation/fuseki2">Fuseki</a> 
     *  Other names can be specificied using {@link #connect(String, String, String, String)}
     *     
     * @param destination
     * @return RDFConnection
     * @see #connect(String, String, String, String)
     */
    public static RDFConnection connect(String destination) {
        return new RDFConnectionRemote(destination);
    }

    /** Create a connection specifying the URLs of the service.
     * 
     * @param queryServiceEndpoint
     * @param updateServiceEndpoint
     * @param graphStoreProtocolEndpoint
     * @return RDFConnection
     */
    public static RDFConnection connect(String queryServiceEndpoint,
                                        String updateServiceEndpoint,
                                        String graphStoreProtocolEndpoint) {
        return new RDFConnectionRemote(queryServiceEndpoint, updateServiceEndpoint, graphStoreProtocolEndpoint);
   }

    
    /** Create a connection to a remote location by URL.
     * This is the URL for the dataset.
     * Each service is then specified by a URL which is relative to the {@code datasetURL}.
     * 
     * @param datasetURL
     * @param queryServiceEndpoint
     * @param updateServiceEndpoint
     * @param graphStoreProtocolEndpoint
     * @return RDFConnection
     */
    public static RDFConnection connect(String datasetURL,
                                        String queryServiceEndpoint,
                                        String updateServiceEndpoint,
                                        String graphStoreProtocolEndpoint) {
        return new RDFConnectionRemote(datasetURL, queryServiceEndpoint, updateServiceEndpoint, graphStoreProtocolEndpoint);
    }

    /**
     * Connect to a local (same JVM) dataset.
     * @param dataset
     * @return RDFConnection
     */
    public static RDFConnection connect(Dataset dataset) {
        return new RDFConnectionLocal(dataset);
    }

}
