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
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdfpatch.changes.RDFChangesCollector;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public abstract class AbstractTestPatchIO {
    // Write-read.

    private static Node g1 = SSE.parseNode(":g1");
    private static Node g2 = SSE.parseNode("_:g2");
    private static Node s1 = SSE.parseNode(":s1");
    private static Node s2 = SSE.parseNode("_:s2");
    private static Node s3 = SSE.parseNode("<<_:b :y 123>>");
    private static Node p1 = SSE.parseNode("<http://example/p1>");
    private static Node p2 = SSE.parseNode(":p2");
    private static Node o1 = SSE.parseNode("<http://example/o1>");
    private static Node o2 = SSE.parseNode("123");
    private static Node o3 = SSE.parseNode("<< <<_:b :q _:b>> :prop _:b >>");

    // Dubious
    private static Node zo1 = SSE.parseNode("'abc\uFFFDdef'");
    // Dubious
    private static Node zs1 = NodeFactory.createURI("http://example/space uri");


    protected abstract void write(OutputStream out, RDFPatch patch);
    protected abstract RDFPatch read(InputStream in);

    private byte[] write(RDFPatch patch) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, patch);
        return out.toByteArray();
    }

    protected RDFPatch read(byte[] bytes) {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        return read(in);
    }

    private static RDFPatch makePatch(Consumer<RDFChanges> action) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFChangesCollector changes = new RDFChangesCollector();
        changes.start();
        action.accept(changes);
        changes.finish();
        RDFPatch patch = changes.getRDFPatch();
        return patch;
    }

    private byte[] write(Consumer<RDFChanges> action) {
        RDFPatch patch = makePatch(changes->action.accept(changes));
        byte[] output = write(patch);
        return output;
    }

    private void write_read(Consumer<RDFChanges> action) {
        RDFPatch patch1 = makePatch(changes->action.accept(changes));
        byte[] bytes = write(patch1);
        RDFPatch patch2 = read(bytes);

        if ( ! patch1.equals(patch2) ) {
            System.out.println("<<<<");
            RDFPatchOps.write(System.out, patch1);
            System.out.println("----");
            RDFPatchOps.write(System.out, patch2);
            System.out.println(">>>>");
        }

        // Stored patches have .equals by value.
        // Need recursion on <<>>
        assertEquals(patch1, patch2);
    }

    @Test public void write_read_01() {
        write_read(changes->{
            changes.txnBegin();
            changes.add(g1, s1, p1, o1);
            changes.delete(g1, s1, p1, o1);
            changes.txnCommit();
        });
    }

    @Test public void write_read_02() {
        write_read(changes->{
            changes.txnBegin();
            changes.add(g2, s2, p2, o2);
            changes.txnCommit();
        });
    }

    @Test public void write_read_03() {
        write_read(changes->{
            changes.txnBegin();
            changes.add(g2, s3, p2, o1);
            //changes.add(g2, s3, p2, o3);
            changes.txnCommit();
        });
    }
}
