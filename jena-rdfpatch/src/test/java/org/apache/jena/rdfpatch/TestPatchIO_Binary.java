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

package org.apache.jena.rdfpatch;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdfpatch.changes.RDFChangesCollector;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TestPatchIO_Binary extends AbstractTestPatchIO {

    @Override
    protected void write(OutputStream out, RDFPatch path) {
        RDFPatchOps.writeBinary(out, path);
    }

    @Override
    protected RDFPatch read(InputStream in) {
        return RDFPatchOps.readBinary(in);
    }

    @Test(expected = PatchException.class)
    public void junk_01() {
        byte[] junkData = "junk".getBytes(StandardCharsets.UTF_8);
        read(new ByteArrayInputStream(junkData));
    }

    @Test(expected = PatchException.class)
    public void junk_02() {
        byte[] junkData = "aaaa".getBytes(StandardCharsets.UTF_8);
        read(new ByteArrayInputStream(junkData));
    }

    @Test(expected = PatchException.class)
    public void junk_03() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        RDFChangesCollector collector = new RDFChangesCollector();
        collector.add(NodeFactory.createURI("http://g"), NodeFactory.createURI("http://s"),
                      NodeFactory.createURI("http://p"), NodeFactory.createURI("http://o"));
        write(output, collector.getRDFPatch());
        // Intentionally truncating a valid binary patch
        byte[] junkData = Arrays.copyOfRange(output.toByteArray(), 0, output.size() - 10);
        read(new ByteArrayInputStream(junkData));
    }
}
