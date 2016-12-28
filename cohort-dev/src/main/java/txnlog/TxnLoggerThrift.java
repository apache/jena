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

import static txnlog.TxnLog.END ;
import static txnlog.TxnLog.byteToQuadAction ;

import java.io.InputStream ;

import org.apache.jena.graph.Node ;
import org.apache.jena.riot.thrift.TRDF ;
import org.apache.jena.riot.thrift.ThriftConvert ;
import org.apache.jena.riot.thrift.wire.RDF_Term ;
import org.apache.jena.sparql.core.DatasetChanges ;
import org.apache.jena.sparql.core.QuadAction ;
import org.apache.thrift.TException ;
import org.apache.thrift.protocol.TProtocol ;
import org.seaborne.dboe.DBOpEnvException ;

public class TxnLoggerThrift {

    /** Replay a write transaction. */
    public static void replay(InputStream in, DatasetChanges processor) {
        try { 
        TProtocol protocol = TRDF.protocol(in) ;
        
        for(;;) {
            byte b = protocol.readByte() ;
            if ( b == END )
                break ;
            QuadAction quadAction = byteToQuadAction(b) ;
            Node g = read(protocol) ;
            Node s = read(protocol) ;
            Node p = read(protocol) ;
            Node o = read(protocol) ;
            processor.change(quadAction, g, s, p, o); 
        }
        } catch (TException ex) { throw new DBOpEnvException(ex) ; } 
    }

    static RDF_Term rdfTerm = new RDF_Term() ;

    private static Node read(TProtocol protocol) throws TException {
        // Recycle.
        rdfTerm.clear() ;
        rdfTerm.read(protocol);
        return ThriftConvert.convert(rdfTerm, null) ;
    }
}


