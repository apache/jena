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

package org.apache.jena.riot;

import java.io.OutputStream ;
import java.io.Writer ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.writer.WriterGraphRIOTBase ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.sparql.util.Context ;

/**
 * A RIOT serializer for a language.
 * This covers both graphs and datasets.
 * A WriterRIOT is a one-time use object (they may accumulate state, e.g. pretty writers).
 * @see WriterGraphRIOTBase
 */
public interface WriterGraphRIOT
{
    /**
     * @param out           OutputStream
     * @param graph         Graph to be written
     * @param prefixMap     PrefixMap - maybe null (default should be to use the prefixmapping from the Graph)
     * @param baseURI       base URI - may be null for "none"
     * @param context       Context (see specific implementation for details) 
     */
    public void  write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) ;

    /** Use of Writer is discouraged - let the serializer manage character sets in accordance with the format
     * @param out           Writer
     * @param graph         Graph to be written
     * @param prefixMap     PrefixMap - maybe null (default should be to use the prefixmapping from the Graph)
     * @param baseURI       base URI - may be null for "none"
     * @param context       Context (see specific implementation for details) 
     */
    public void  write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) ;
    
    public Lang  getLang() ;
}
