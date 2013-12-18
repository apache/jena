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

package org.apache.jena.fuseki;

import static java.lang.String.format ;
import static org.apache.jena.fuseki.Fuseki.serverLog ;

import java.util.List ;

import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.fuseki.server.ServiceRef ;

import com.hp.hpl.jena.sparql.mgt.ARQMgt ;

// XXX Organise!

public class X_Config {
    
    public static void configureDatasets(List<DatasetRef> datasets) {
        for (DatasetRef dsDesc : datasets)
            X_Config.configureOneDataset(dsDesc) ;
    }

    public static void configureOneDataset(DatasetRef dsDesc) {
        String datasetPath = DatasetRef.canocialDatasetPath(dsDesc.name) ;
        registerDataset(datasetPath, dsDesc) ;
        // Add JMX beans to record dataset and it's services.
        addJMX(dsDesc) ;
    }
    
    public static void registerDataset(String datasetPath, DatasetRef dsDesc) {
        dsDesc.enable() ;
        if ( DatasetRegistry.get().isRegistered(datasetPath) )
            throw new FusekiConfigException("Already registered: key = "+datasetPath) ;
        DatasetRegistry.get().put(datasetPath, dsDesc) ;
        serverLog.info(format("Dataset path = %s", datasetPath)) ;
    }
    
    public static void addJMX() {
        DatasetRegistry registry = DatasetRegistry.get() ;
        for (String ds : registry.keys()) {
            DatasetRef dsRef = registry.get(ds) ;
            addJMX(dsRef) ;
        }
    }

    private static void addJMX(DatasetRef dsRef) {
        String x = dsRef.name ;
        // if ( x.startsWith("/") )
        // x = x.substring(1) ;
        ARQMgt.register(Fuseki.PATH + ".dataset:name=" + x, dsRef) ;
        // For all endpoints
        for (ServiceRef sRef : dsRef.getServiceRefs()) {
            ARQMgt.register(Fuseki.PATH + ".dataset:name=" + x + "/" + sRef.name, sRef) ;
        }
    }

    public static void removeJMX() {
        DatasetRegistry registry = DatasetRegistry.get() ;
        for (String ds : registry.keys()) {
            DatasetRef ref = registry.get(ds) ;
            removeJMX(ref) ;
        }
    }

    private static void removeJMX(DatasetRef dsRef) {
        String x = dsRef.getName() ;
        ARQMgt.unregister(Fuseki.PATH + ".dataset:name=" + x) ;
        for (ServiceRef sRef : dsRef.getServiceRefs()) {
            ARQMgt.unregister(Fuseki.PATH + ".dataset:name=" + x + "/" + sRef.name) ;
        }
    }

}

