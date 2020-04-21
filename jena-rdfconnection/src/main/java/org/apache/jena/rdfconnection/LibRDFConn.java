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

/** package-wide utilities etc */
/*package*/ class LibRDFConn {
    private static String dftName =  "default" ;

    /*package*/ static boolean isDefault(String name) {
        return name == null || name.equals(dftName) ;
    }

    private static String queryStringForGraph(String ch, String graphName) {
        return
            ch +
                (LibRDFConn.isDefault(graphName)
                ? "default"
                : "graph="+graphName) ;
    }

    /*package*/ static String urlForGraph(String graphStoreProtocolService, String graphName) {
        // If query string
        String ch = "?";
        if ( graphStoreProtocolService.contains("?") )
            // Already has a query string, append with "&"
            ch = "&";
        return graphStoreProtocolService + queryStringForGraph(ch, graphName) ;
    }

    /*package*/ static String formServiceURL(String destination, String srvEndpoint) {
        if ( srvEndpoint == null )
            return null;
        if ( srvEndpoint == RDFConnectionRemoteBuilder.SameAsDestination )
            return destination;
        if ( destination == null )
            return srvEndpoint;

        // If the srvEndpoint looks like an absolute URL, use as given.
        if ( srvEndpoint.startsWith("http:/") || srvEndpoint.startsWith("https:/") )
            return srvEndpoint;
        String queryString = null;
        String dest = destination;
        if ( destination.contains("?") ) {
            // query string : remove and append later.
            int i = destination.indexOf('?');
            queryString = destination.substring(i);
            dest = destination.substring(0, i);
        }
        if ( dest.endsWith("/") )
            dest = dest.substring(0, dest.length()-1);
        dest = dest+"/"+srvEndpoint;
        if ( queryString != null )
           dest = dest+queryString;
        return dest;
    }
}
