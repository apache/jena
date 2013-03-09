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

package org.apache.jena.riot.out;

import java.io.OutputStream ;
import java.util.Iterator ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

public class NQuadsWriter
{
    /** @deprecated Use {@linkplain RDFDataMgr#write(OutputStream, DatasetGraph, Lang)}
     * with {@code Lang.NQUADS}.
     */  
    @Deprecated
    public static void write(OutputStream out, DatasetGraph dsg)
    {
        RDFDataMgr.write(out, dsg, Lang.NQUADS) ;
    }
    
    /** @deprecated Use {@linkplain RDFDataMgr#write(OutputStream, Dataset, Lang)} 
     * with {@code Lang.NQUADS}.
     */
    @Deprecated
    public static void write(OutputStream out, Dataset dsg)
    {
        RDFDataMgr.write(out, dsg, Lang.NQUADS) ;
    }
    
    /** @deprecated Use {@linkplain RDFDataMgr#writeQuads} */
    @Deprecated
    public static void write(OutputStream out, Iterator<Quad> iter)
    {
        RDFDataMgr.writeQuads(out, iter) ;
    }
}
