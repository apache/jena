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

package org.apache.jena.hadoop.rdf.io.input.compressed;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.TripleWritable;

/**
 * Abstract tests for Triple input formats
 * 
 * 
 * 
 */
public abstract class AbstractCompressedTriplesInputFormatTests extends
        AbstractCompressedNodeTupleInputFormatTests<Triple, TripleWritable> {
    
    private static final Charset utf8 = Charset.forName("utf-8");

    @Override
    protected void generateTuples(OutputStream output, int num) throws IOException {
        for (int i = 0; i < num; i++) {
            output.write(("<http://subjects/" + i + "> <http://predicate> \"" + i + "\" .\n").getBytes(utf8));
        }
        output.flush();
        output.close();
    }

    @Override
    protected void generateBadTuples(OutputStream output, int num) throws IOException {
        for (int i = 0; i < num; i++) {
            output.write("<http://broken\n".getBytes(utf8));
        }
        output.flush();
        output.close();
    }

    @Override
    protected void generateMixedTuples(OutputStream output, int num) throws IOException {
        boolean bad = false;
        for (int i = 0; i < num; i++, bad = !bad) {
            if (bad) {
                output.write("<http://broken\n".getBytes(utf8));
            } else {
                output.write(("<http://subjects/" + i + "> <http://predicate> \"" + i + "\" .\n").getBytes(utf8));
            }
        }
        output.flush();
        output.close();
    }
}
