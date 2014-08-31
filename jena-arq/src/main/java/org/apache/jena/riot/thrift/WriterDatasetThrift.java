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

package org.apache.jena.riot.thrift;

import static org.apache.jena.riot.RDFLanguages.THRIFT ;

import java.io.OutputStream ;
import java.io.Writer ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.WriterDatasetRIOT ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.StreamOps ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Write a dataset as RDF Thrift */
public class WriterDatasetThrift implements WriterDatasetRIOT
{
    private final boolean withValues ;
    public WriterDatasetThrift(RDFFormat fmt) {
        this.withValues = RDFFormat.RDF_THRIFT_VALUES.equals(fmt) ;
    }
    @Override
    public Lang getLang() {
        return THRIFT ;
    }
    @Override
    public void write(Writer out, DatasetGraph dsg, PrefixMap prefixMap, String baseURI, Context context) {
        throw new RiotThriftException("Writing binary data to a java.io.Writer is not supported. Please use an OutputStream") ;
    }
    @Override
    public void write(OutputStream out, DatasetGraph dsg, PrefixMap prefixMap, String baseURI, Context context) {
        StreamRDF stream = BinRDF.streamToOutputStream(out, withValues) ;
        stream.start();
        StreamOps.sendDatasetToStream(dsg, stream, prefixMap) ;
        stream.finish();
    }
}
