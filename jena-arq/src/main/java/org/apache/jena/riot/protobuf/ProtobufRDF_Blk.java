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

package org.apache.jena.riot.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_Stream;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.StreamRDF;

/** Specialist.
 *
 * The normal use operations are in {@link ProtobufRDF}.
 * <p>
 * This operations assume Protobuf data is "as given" without stream delimiters.
 * A single operation will read or write a whole file.
 */
public class ProtobufRDF_Blk {
    // ==== Delimited items in a stream

    /**
     * Read an input stream as a single unit (no length delimiters). Send items to an
     * {@link StreamRDF}
     */
    public static void inputStreamBlkToStreamRDF(InputStream input, StreamRDF stream) {
        Protobuf2StreamRDF visitor = new Protobuf2StreamRDF(PrefixMapFactory.create(), stream);
        try {
            RDF_Stream rdfStream2 = RDF_Stream.parseFrom(input);
            stream.start();
            rdfStream2.getRowList().forEach( sr -> PBufRDF.visit(sr,visitor) );
            stream.finish();
        } catch (IOException ex) { IO.exception(ex); }
    }

    /**
     * Write all of the {@link StreamRDF} to an output stream without length delimiters.
     * "apply"
     */
    public static void streamToOutputStreamBlk(OutputStream outputStream, Consumer<StreamRDF> streamDest) {
        outputStream = IO.ensureBuffered(outputStream);
        try {
            StreamRDF2Protobuf.writeBlk(outputStream, streamDest, false);
        } finally { IO.flush(outputStream); }
    }

}

