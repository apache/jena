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

import java.util.Iterator ;

import org.apache.jena.riot.system.* ;
import org.apache.jena.riot.thrift.wire.RDF_StreamRow ;
import org.apache.thrift.TException ;
import org.apache.thrift.protocol.TProtocol ;
import org.apache.thrift.transport.TTransportException ;

/**
 *  Iterator over a Thrift-encoded RDF stream.
 */
public class IteratorThriftRDF extends IteratorStreamRDF implements Iterator<StreamRowRDF> {

    private final PrefixMap pmap = PrefixMapFactory.create() ;
    private final StreamRDFCollectOne collector = new StreamRDFCollectOne(pmap) ;
    private final Thrift2StreamRDF converter = new Thrift2StreamRDF(pmap, collector) ;
    
    private final RDF_StreamRow row = new RDF_StreamRow() ;
    private final TProtocol protocol ;
    private StreamRDFCollectOne slot ;
    private boolean finished = false ;

    public IteratorThriftRDF(TProtocol protocol) {
        this.protocol = protocol ;
    }
    
    @Override
    protected boolean hasMore() {
        return true ;
    }

    @Override
    protected StreamRowRDF moveToNext() {
        if ( ! protocol.getTransport().isOpen() )
            return null ;

        try { row.read(protocol) ; }
        catch (TTransportException e) {
            if ( e.getType() == TTransportException.END_OF_FILE )
                return null ;
        }
        catch (TException ex) { TRDF.exception(ex) ; }
        
        TRDF.visit(row, converter); 
        
        row.clear() ;
        return collector.getRow() ;
    }

}

