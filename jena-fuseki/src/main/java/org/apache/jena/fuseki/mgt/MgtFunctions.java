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

package org.apache.jena.fuseki.mgt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.DatasetRef;
import org.apache.jena.fuseki.server.DatasetRegistry;
import org.apache.jena.fuseki.server.ServiceRef;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.serializer.PrologueSerializer;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;

/** Avoid code in JSPs */
public class MgtFunctions
{
    /** Return the name of the current dataset */ 
    public static String dataset(HttpServletRequest request, String dftValue)
    {
        String ds = dataset(request) ;
        if ( ds == null )
            return dftValue ;
        return ds ;
    }
    
    /** Return the name of the current dataset */ 
    public static String dataset(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false) ;
        if ( session == null )
            return "No session";
        String ds = (String)session.getAttribute("dataset") ;
        return ds ;
    }

    /** Return the dataset description reference for currnet dataset */  
    public static DatasetRef datasetDesc(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false) ;
        if ( session == null )
            return null ;
        String ds = (String)session.getAttribute("dataset") ;
        return DatasetRegistry.get().get(ds) ;
    }

    /** Return lists of datasets */ 
    public static List<String> datasets(HttpServletRequest request)
    {
        return Iter.toList(DatasetRegistry.get().keys()) ;
    }
    
    /** Map of datasets with the corresponding query service */ 
    public static Map<String, String> datasetsQuery(HttpServletRequest request)
    {
    	HashMap<String, String> dsQueryEndpoint = new HashMap<String, String>();
        for (String ds: DatasetRegistry.get().keys()) {
        	dsQueryEndpoint.put(ds, serviceQuery(ds));
        }
        return dsQueryEndpoint;
    }
    /** Map of datasets with the corresponding update service */ 
    public static Map<String, String> datasetsUpdate(HttpServletRequest request)
    {
    	HashMap<String, String> dsUpdateEndpoint = new HashMap<String, String>();
    	for (String ds: DatasetRegistry.get().keys()) {
    		dsUpdateEndpoint.put(ds, serviceUpdate(ds));
    	}
    	return dsUpdateEndpoint;
    }

    /** Return name of */  
    public static String actionDataset(HttpServletRequest request)
    {
        return PageNames.actionDatasetNames ;
    }

    // Service name getters ...
    
    /** Return a SPARQL query service name for the dataset */
    public static String serviceQuery(String dataset)
    {
        String dft = "sparql" ; 
        DatasetRef ref = getFromRegistry(dataset) ;
        if ( ref == null )
            return dft ;
        return serviceNameOrDefault(ref.query, dft) ;
    }
    
    /** Return a SPARQL update service name for the dataset */
    public static String serviceUpdate(String dataset)
    {
        String dft = "update" ; 
        DatasetRef ref = getFromRegistry(dataset) ;
        if ( ref == null )
            return dft ;
        return serviceNameOrDefault(ref.update, dft) ;
    }
    
    /** Return a SPARQL upload service name for the dataset */
    public static String serviceUpload(String dataset)
    {
        String dft = "upload" ;
        DatasetRef ref = getFromRegistry(dataset) ;
        if ( ref == null )
            return dft ;
        return serviceNameOrDefault(ref.upload, dft) ;
    }
    /** Return a SPARQL upload service name for all datasets */
    public static Map<String, String> serviceUploads()
    {
    	HashMap<String, String> dsUpload = new HashMap<String, String>();
    	for (String ds: DatasetRegistry.get().keys()) {
    		dsUpload.put(ds, serviceUpload(ds));
    	}
    	return dsUpload;
    }

    /** Return a SPARQL Graph Store Protocol (Read) service name for the dataset */
    public static String serviceGraphRead(String dataset)
    {
        String dft = "get" ;
        DatasetRef ref = getFromRegistry(dataset) ;
        if ( ref == null )
            return dft ;
        return serviceNameOrDefault(ref.readGraphStore, dft) ;
    }

    /** Return a SPARQL Graph Store Protocol (Read-Write) service name for the dataset */
    public static String serviceGraphReadWrite(String dataset)
    {
        String dft = "data" ;
        DatasetRef ref = getFromRegistry(dataset) ;
        if ( ref == null )
            return dft ;
        return serviceNameOrDefault(ref.readWriteGraphStore, dft) ;
    }

    private static DatasetRef getFromRegistry(String dataset)
    {
        DatasetRegistry registry = DatasetRegistry.get() ;
        if ( registry == null )
        {
            Fuseki.serverLog.warn("No dataset registry") ;
            return null ;
        }
        
        DatasetRef ref = registry.get(dataset) ;
        if ( ref == null )
            Fuseki.serverLog.warn("Dataset not found: "+dataset) ;
        return ref ;
    }

    private static String serviceNameOrDefault(ServiceRef service, String defaultValue)
    {
        if ( service.endpoints.isEmpty() )
            return defaultValue ;
        String x = service.endpoints.get(0) ;
        if ( x.startsWith("/") )
            x = x.substring(1) ;
        return x ;
    }
    
    /** Return prefixes for the datasets, SPARQL syntax. */ 
    public static String prefixes(HttpServletRequest request)
    {
        String dsName = dataset(request) ;
        DatasetRef desc = getFromRegistry(dsName) ;
        if ( desc == null )
            return "<not found>" ;
        DatasetGraph dsg = desc.dataset ; 
        
        if ( dsg instanceof DatasetGraphTDB )
        {
            PrefixMapping pmap = ((DatasetGraphTDB)dsg).getPrefixes().getPrefixMapping() ;
            Prologue prologue = new Prologue(pmap) ;
            IndentedLineBuffer buff = new IndentedLineBuffer() ;
            PrologueSerializer.output(buff, prologue) ;
            buff.append("\n") ;
            return buff.asString() ;
        }
        return "" ;
    }
}
