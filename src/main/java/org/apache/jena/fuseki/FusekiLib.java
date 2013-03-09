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

package org.apache.jena.fuseki;

import java.util.Iterator ;

import javax.servlet.http.HttpServletRequest ;

import org.apache.commons.lang.StringUtils ;
import org.apache.jena.atlas.lib.MultiMap ;
import org.apache.jena.atlas.web.MediaType ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Convert ;

public class FusekiLib
{
    public static MediaType contentType(HttpServletRequest request)
    {
        String x = request.getHeader(HttpNames.hContentType) ;
        if ( x == null )
            return null ;
        return MediaType.create(x) ;
    }

    static String fmtRequest(HttpServletRequest request)
    {
        StringBuffer sbuff = new StringBuffer() ;
        sbuff.append(request.getMethod()) ;
        sbuff.append(" ") ;
        sbuff.append(Convert.decWWWForm(request.getRequestURL()));
        
        String qs = request.getQueryString();
        if (qs != null)
        {
            String tmp = request.getQueryString() ;
            tmp = Convert.decWWWForm(tmp) ;
            tmp = tmp.replace('\n', ' ') ;
            tmp = tmp.replace('\r', ' ') ;
            sbuff.append("?").append(tmp);
        }
        return sbuff.toString() ;
    }

    /** Parse the query string - do not process the body even for a form */  
    public static MultiMap<String, String> parseQueryString(HttpServletRequest req)
    {
        MultiMap<String, String> map = MultiMap.createMapList() ;
        
        // Don't use ServletRequest.getParameter or getParamterNames
        // as that reads form data.  This code parses just the query string.
        if ( req.getQueryString() != null )
        {
            String[] params = req.getQueryString().split("&") ;
            for ( int i = 0 ; i < params.length ; i++ )
            {
                String p = params[i] ;
                String[] x = p.split("=",2) ;
                String name = null ;
                String value = null ;
    
                if ( x.length == 0 )
                {   // No "="
                    name = p ;
                    value = "" ;
                }
                else if ( x.length == 1 )
                {   // param=
                    name = x[0] ;
                    value = "" ;
                }
                else
                {   // param=value
                    name = x[0] ;
                    value = x[1] ;
                }
                map.put(name, value) ;
            }
        }
        return map ;
    }
    
    public static String safeParameter(HttpServletRequest request, String pName)
    {
        String value = request.getParameter(pName) ;
        value = StringUtils.replaceChars(value, "\r", "") ;
        value = StringUtils.replaceChars(value, "\n", "") ;
        return value ; 
    }
    
    // Do the addition directly on the dataset
    public static void addDataInto(Graph data, DatasetGraph dsg, Node graphName)
    {
        if ( graphName == null )
            graphName = Quad.defaultGraphNodeGenerated ;
            
        Iterator<Triple> iter = data.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = iter.next();
            dsg.add(graphName, t.getSubject(), t.getPredicate(), t.getObject()) ;
        }
    }



}
