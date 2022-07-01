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

package tdb2;

import java.net.URLConnection;

import org.apache.jena.dboe.base.file.BinaryDataFile;
import org.apache.jena.dboe.base.file.BinaryDataFileRandomAccess;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.tdb2.store.nodetable.TReadAppendFileTransport;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

/** Read the node binary data file and print its contents */
public class tdbdumpnodes {

    public static void main(String... args) throws TException {
        if ( args.length != 1 ) {
            System.err.println("Usage: tdbdumpnodes NodeFile -- e.g \"Database2/Data-0001/nodes-data.dat\"");
            System.exit(1);
        }

        String FN = args[0];

        URLConnection x;

        BinaryDataFile f = new BinaryDataFileRandomAccess(FN);
        f.open();
        TReadAppendFileTransport transport = new TReadAppendFileTransport(f);

        TProtocol protocol = TRDF.protocol(transport) ;
        transport.readPosition(0) ;
//        [0x        1BFEA0FD]
//            <http://data.europa.eu/esco/occupation/99492920-e5a5-4dba-9e5a-93193147198c>
//            [0x        1BFEA14C] ** Bad read ** don't know what type: 14
//        transport.readPosition(0x1BFEA0FD);
        long limit = f.length();
//        limit = 0x1C2092FF;

        System.out.printf("File length: %,d [0x%16X]\n", limit, limit);

        //transport.readPosition(0x1C1E78F3);

        while (true) {
            long locn = transport.readPosition();
            if ( locn >= limit )
                break;
            try {
                Node n = readOne(protocol);
                System.out.printf("[0x%16X] %s\n",locn, FmtUtils.stringForNode(n));
            } catch (Exception ex) {
                System.out.printf("[0x%16X] ** Bad read ** %s\n",locn, ex.getMessage());
                long jump = 100;
                long i = locn;
                for ( ; i < locn+jump ; i++ ) {
                    transport.readPosition(i);
                    try {
                        Node n = readOne(protocol);
                        System.out.printf("Resync: %,d  [0x%16X] ==> [0x%16X]\n",i-locn, locn, i);
                        System.out.printf("[0x%16X] ** %s\n",locn, FmtUtils.stringForNode(n));
                    } catch (Exception ex2) {}
                }
                if ( locn - i >= jump )
                    System.out.printf("No resync: %,d  [0x%16X] ==> [0x%16X]\n",i-locn, locn, i);

//                // Problems - back up and dump.
//                byte bytes[] = new byte[256];
//                int len = f.read(locn, bytes);
//                StringBuilder sBuff = new StringBuilder() ;
//                for ( int i = 0 ; i < len ; i++ ) {
//                    byte b = bytes[i] ;
//                    int hi = (b & 0xF0) >> 4 ;
//                    int lo = b & 0xF ;
//                    if ( i != 0 ) {
//                        if (i % 20 == 0  )
//                            sBuff.append("\n");
//                        else
//                            sBuff.append(" ");
//                    }
//                    sBuff.append(Chars.hexDigitsUC[hi]) ;
//                    sBuff.append(Chars.hexDigitsUC[lo]) ;
//                }
//                String str = sBuff.toString();
//                if ( !str.endsWith("\n") )
//                    str = str+"\n";
//                System.out.print(str);
//                System.exit(1);
            }
        }
    }

    private static Node readOne(TProtocol protocol) throws TException {
        RDF_Term term = new RDF_Term() ;
        term.read(protocol) ;
        Node n = ThriftConvert.convert(term) ;
        return n ;
    }

}
