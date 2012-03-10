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

import java.util.Iterator ;
import java.util.List ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpSession ;

import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.openjena.atlas.io.IndentedLineBuffer ;
import org.openjena.atlas.iterator.Iter ;

import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.serializer.PrologueSerializer ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

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

    // Service name getters ...
    
    /** Return a SPARQL query service name for the dataset */
    public static String serviceQuery(String dataset)
    {
        DatasetRef ref = DatasetRegistry.get().get(dataset) ;
        return serviceNameOrDefault(ref.queryEP, "sparql") ;
    }
    
    /** Return a SPARQL update service name for the dataset */
    public static String serviceUpdate(String dataset)
    {
        DatasetRef ref = DatasetRegistry.get().get(dataset) ;
        return serviceNameOrDefault(ref.updateEP, "update") ;
    }
    
    /** Return a SPARQL upload service name for the dataset */
    public static String serviceUpload(String dataset)
    {
        DatasetRef ref = DatasetRegistry.get().get(dataset) ;
        return serviceNameOrDefault(ref.uploadEP, "upload") ;
    }

    /** Return a SPARQL Graph Store Protocol (Read) service name for the dataset */
    public static String serviceGraphRead(String dataset)
    {
        DatasetRef ref = DatasetRegistry.get().get(dataset) ;
        return serviceNameOrDefault(ref.readGraphStoreEP, "get") ;
    }

    /** Return a SPARQL Graph Store Protocol (Read-Write) service name for the dataset */
    public static String serviceGraphReadWrite(String dataset)
    {
        DatasetRef ref = DatasetRegistry.get().get(dataset) ;
        return serviceNameOrDefault(ref.readWriteGraphStoreEP, "data") ;
    }

    private static String serviceNameOrDefault(List<String> services, String defaultValue)
    {
        return ( services.isEmpty() ) ? defaultValue : services.get(0) ;
    }
    
    /* remove soon - simply for backwards compatibility with Fuseki+JSP */
    @Deprecated
    public static String datasetsAsSelectOptions(HttpServletRequest request)
    {
        StringBuilder buff = new StringBuilder() ;
        
        Iterator<String> iter = DatasetRegistry.get().keys() ;
        for ( ; iter.hasNext() ; )
        {
            String name = iter.next() ;
            buff.append("<option value=\""+name+"\">"+name+"</option>") ;
        }
        return buff.toString() ;
    }
    
    /* remove soon - simply for backwards compatibility with Fuseki+JSP */
    @Deprecated
    public static String datasetsAsListItems(HttpServletRequest request)
    {
        StringBuilder buff = new StringBuilder() ;
        
        Iterator<String> iter = DatasetRegistry.get().keys() ;
        for ( ; iter.hasNext() ; )
        {
            String name = iter.next() ;
            buff.append("  <li>"+name+"</li>") ;
        }
        return buff.toString() ;
    }

    /** Return prefixes for the datasets, SPARQL syntax. */ 
    public static String prefixes(HttpServletRequest request)
    {
        String dsName = dataset(request) ;
        DatasetRef desc = DatasetRegistry.get().get(dsName) ;
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
