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

package org.apache.jena.rdfpatch.binary;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfpatch.*;
import org.apache.jena.rdfpatch.changes.RDFChangesCollector;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.wire.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;

/**
 * Read a binary patch.
 * @see PatchProcessor
 */
public class RDFPatchReaderBinary implements PatchProcessor {
    private final InputStream input;

    private RDFPatchReaderBinary(InputStream input) {
        this.input = input;
    }

    @Override
    public void apply(RDFChanges processor) {
        read(input, processor);
    }

    public static PatchProcessor create(InputStream input) { return new RDFPatchReaderBinary(input); }

    /**
     * Read input stream and produce an {@link RDFPatch}.
     * This operation actively reads the patch into memory.
     * See {@link RDFPatchReaderBinary#read(InputStream, RDFChanges)}
     * to stream it to a chnage processor.
     * Create an {@code PatchReaderBinary} object with {@link RDFPatchReaderBinary#create}
     * to create a delayed read processor.
     * See {@link RDFPatchOps#collect} to make sure a patch has been read.
     */
    public static RDFPatch read(InputStream input) {
        RDFChangesCollector changes = new RDFChangesCollector();
        RDFPatchReaderBinary.read(input, changes);
        return changes.getRDFPatch();
    }

    /**
     * Read input stream and produce a {@link PatchHeader}.
     * The stream is read during this call.
     */
    public static PatchHeader readHeader(InputStream input) {
        TProtocol protocol = TRDF.protocol(input);
        return readHeader(protocol);
    }

    /**
     * Read input stream and produce a {@link PatchHeader}.
     * The stream is read during this call.
     */
    private static PatchHeader readHeader(TProtocol protocol) {
        RDF_Patch_Row row = new RDF_Patch_Row();
        Map<String, Node> header = new LinkedHashMap<>();

        for (;;) {
            row.clear();
            try { row.read(protocol) ; }
            catch (TTransportException e) {
                if ( e.getType() == TTransportException.END_OF_FILE )
                    break;
                throw new PatchException("Thrift exception", e);
            }
            catch (TException e) {
                throw new PatchException("Thrift exception", e);
            }

            if ( row.isSetHeader() ) {
                Patch_Header h = row.getHeader();
                Node n = RDFPatchReaderBinary.fromThrift(h.getValue());
                header.put(h.getName(), n);
                continue;
            }
            break;
        }
        return new PatchHeader(header);

    }

    /** Read and apply */
    public static void read(InputStream input, RDFChanges changes) {
        read(TRDF.protocol(input), changes);
    }

    public static void read(TProtocol protocol, RDFChanges changes) {
        RDF_Patch_Row row = new RDF_Patch_Row();
        changes.start();
        for (;;) {
            row.clear();
            try { row.read(protocol) ; }
            catch (TTransportException e) {
                if ( e.getType() == TTransportException.END_OF_FILE ) {
                    changes.finish();
                    return;
                }
                throw new PatchException("Thrift exception", e);
            }
            catch (TException e) {
                throw new PatchException("Thrift exception", e);
            }

            dispatch(row, changes);
        }
    }

    private static void dispatch(RDF_Patch_Row row, RDFChanges changes) {
        if ( row.isSetHeader() ) {
            Patch_Header h = row.getHeader();
            Node n = RDFPatchReaderBinary.fromThrift(h.getValue());
            changes.header(h.getName(), n);
            return;
        }

        if ( row.isSetDataAdd() ) {
            Patch_Data_Add add = row.getDataAdd();
            Node s = RDFPatchReaderBinary.fromThrift(add.getS());
            Node p = RDFPatchReaderBinary.fromThrift(add.getP());
            Node o = RDFPatchReaderBinary.fromThrift(add.getO());
            Node g = null;
            if ( add.isSetG() )
                g = RDFPatchReaderBinary.fromThrift(add.getG());
            changes.add(g, s, p, o);
            return;
        }

        if ( row.isSetDataDel() ) {
            Patch_Data_Del del = row.getDataDel();
            Node s = RDFPatchReaderBinary.fromThrift(del.getS());
            Node p = RDFPatchReaderBinary.fromThrift(del.getP());
            Node o = RDFPatchReaderBinary.fromThrift(del.getO());
            Node g = null;
            if ( del.isSetG() )
                g = RDFPatchReaderBinary.fromThrift(del.getG());
            changes.delete(g, s, p, o);
            return;
        }

        if ( row.isSetPrefixAdd()) {
            Patch_Prefix_Add add = row.getPrefixAdd();
            Node gn = null;
            if ( add.isSetGraphNode() )
                gn = fromThrift(add.getGraphNode());
            changes.addPrefix(gn, add.getPrefix(), add.getIriStr());
            return;
        }

        if ( row.isSetPrefixDel()) {
            Patch_Prefix_Del del = row.getPrefixDel();
            Node gn = null;
            if ( del.isSetGraphNode() )
                gn = fromThrift(del.getGraphNode());
            changes.deletePrefix(gn, del.getPrefix());
            return;
        }

        if ( row.isSetTxn() ) {
            PatchTxn txn = row.getTxn();
            switch (txn) {
                case TX : changes.txnBegin(); break;
                case TC : changes.txnCommit(); break;
                case TA : changes.txnAbort(); break;
                case Segment : changes.segment(); break;
            }
            return;
        }

        throw new PatchException("Unrecogized :"+row);
    }

    public static Node fromThrift(RDF_Term term) {
        if ( term.isSetIri() )
            return NodeFactory.createURI(term.getIri().getIri());

        if ( term.isSetBnode() )
            return NodeFactory.createBlankNode(term.getBnode().getLabel());

        if ( term.isSetLiteral() ) {
            RDF_Literal lit = term.getLiteral();
            String lex = lit.getLex();
            String dtString = null;
            if ( lit.isSetDatatype() )
                dtString = lit.getDatatype();
            RDFDatatype dt = NodeFactory.getType(dtString);

            String lang = lit.getLangtag();
            return NodeFactory.createLiteral(lex, lang, dt);
        }

        if ( term.isSetTripleTerm() ) {
            RDF_Triple rt = term.getTripleTerm();
            Node s = fromThrift(rt.getS()) ;
            Node p = fromThrift(rt.getP()) ;
            Node o = fromThrift(rt.getO()) ;
            Triple t = Triple.create(s, p, o) ;
            return NodeFactory.createTripleNode(t);
        }

        throw new PatchException("No conversion to a Node: "+term.toString());
    }
}
