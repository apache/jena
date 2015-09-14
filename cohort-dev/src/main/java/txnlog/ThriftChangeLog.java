/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */


package txnlog;

import java.io.BufferedOutputStream ;
import java.io.OutputStream ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.graph.Node ;
import org.apache.jena.riot.thrift.TRDF ;
import org.apache.jena.riot.thrift.ThriftConvert ;
import org.apache.jena.riot.thrift.wire.RDF_Term ;
import org.apache.jena.sparql.core.DatasetChanges ;
import org.apache.jena.sparql.core.QuadAction ;
import org.apache.thrift.TException ;
import org.apache.thrift.protocol.TProtocol ;

public class ThriftChangeLog implements DatasetChanges {
    private static final int BUFSIZE_OUT = 128*1024 ;
    private final TProtocol protocol;
    private RDF_Term termBuffer = new RDF_Term() ;

    public ThriftChangeLog(OutputStream out) {
        BufferedOutputStream bout = new BufferedOutputStream(out, BUFSIZE_OUT) ;
        this.protocol = TRDF.protocol(bout) ;
    }

    @Override
    public void start() {}

    @Override
    public void change(QuadAction qaction, Node g, Node s, Node p, Node o) {
        byte b = TxnLog.quadActionToByte(qaction);
        try {
            protocol.writeByte(b);
            write(g);
            write(s);
            write(p);
            write(o);
        } catch (TException ex) { throw new InternalErrorException(ex) ; } 
    }

    private void write(Node n) throws TException {
        termBuffer.clear(); 
        ThriftConvert.toThrift(n, null, termBuffer, true) ;
        termBuffer.write(protocol);
    }

    @Override
    public void finish() {
        try {
            protocol.writeByte(TxnLog.END);
            protocol.getTransport().flush() ; 
        } catch (TException ex) { throw new InternalErrorException(ex) ; } 
    }

    @Override
    public void reset() {}

}
