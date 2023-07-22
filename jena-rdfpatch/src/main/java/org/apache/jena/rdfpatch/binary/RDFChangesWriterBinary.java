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

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfpatch.PatchException;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.text.RDFChangesWriterText;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.wire.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

/**
 * Write RDF Patch in binary (thrift encoded).
 * <p>
 * This class is not thread safe.
 *
 * @see RDFChangesWriterText
 */
public class RDFChangesWriterBinary implements RDFChanges {
    public static void write(RDFPatch patch, String filename) {
        try ( OutputStream out = IO.openOutputFile(filename) ) {
            write(patch, out);
        } catch (IOException ex) { IO.exception(ex); }
    }

    /** Write a patch in binary. */
    public static void write(RDFPatch patch, OutputStream out) {
        writeBinary(out, c -> patch.apply(c) );
    }

    /** {@link RDFChanges} that writes in binary. */
    public static void writeBinary(OutputStream out, Consumer<RDFChanges> action) {
        TProtocol protocol = TRDF.protocol(out);
        RDFChangesWriterBinary writer = new RDFChangesWriterBinary(protocol);
        writer.start();
        action.accept(writer);
        writer.finish();
    }

    /**
     * Return an {@link RDFChanges} that writes in binary. Must call
     * {@link RDFChanges#start()} and {@link RDFChanges#finish()}.
     */
    private static RDFChanges writerBinary(OutputStream out) {
        TProtocol protocol = TRDF.protocol(out);
        RDFChangesWriterBinary writer = new RDFChangesWriterBinary(protocol);
        return writer;
    }


    // Workspace - reused objects
    private final RDF_Term tv = new RDF_Term();
    private final RDF_Term ts = new RDF_Term();
    private final RDF_Term tp = new RDF_Term();
    private final RDF_Term to = new RDF_Term();
    private final RDF_Term tg = new RDF_Term();
    private final Patch_Header header = new Patch_Header();
    private final Patch_Data_Add dataAdd = new Patch_Data_Add();
    private final Patch_Data_Del dataDel = new Patch_Data_Del();
    private final Patch_Prefix_Add prefixAdd = new Patch_Prefix_Add();
    private final Patch_Prefix_Del prefixDel = new Patch_Prefix_Del();
    private final RDF_Patch_Row row = new RDF_Patch_Row();

    private final TProtocol protocol;
    public RDFChangesWriterBinary(TProtocol protocol) {
        this.protocol = protocol;
    }

    private void write() {
        try { row.write(protocol); }
        catch (TException e) {
            throw new PatchException("Thrift exception", e);
        }
        row.clear();
    }

    @Override
    public void start() {}

    @Override
    public void finish() { TRDF.flush(protocol); }

    @Override
    public void header(String field, Node value) {
        header.clear();
        tv.clear();
        header.setName(field);
        RDFChangesWriterBinary.toThrift(value, tv);
        header.setValue(tv);
        row.setHeader(header);
        write();
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        dataAdd.clear();
        set(g,s,p,o);
        dataAdd.setS(ts);
        dataAdd.setP(tp);
        dataAdd.setO(to);
        if ( g != null )
            dataAdd.setG(tg);
        row.setDataAdd(dataAdd);
        write();
    }

    private void set(Node g, Node s, Node p, Node o) {
        ts.clear(); RDFChangesWriterBinary.toThrift(s, ts);
        tp.clear(); RDFChangesWriterBinary.toThrift(p, tp);
        to.clear(); RDFChangesWriterBinary.toThrift(o, to);
        tg.clear();
        if ( g != null )
           RDFChangesWriterBinary.toThrift(g, tg);
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        dataDel.clear();
        set(g,s,p,o);
        dataDel.setS(ts);
        dataDel.setP(tp);
        dataDel.setO(to);
        if ( g != null )
            dataDel.setG(tg);
        row.setDataDel(dataDel);
        write();
    }

    @Override
    public void addPrefix(Node gn, String prefix, String uriStr) {
        prefixAdd.clear();
        if ( gn != null ) {
            tv.clear();
            toThrift(gn, tv);
            prefixAdd.setGraphNode(tv);
        }
        prefixAdd.setPrefix(prefix);
        prefixAdd.setIriStr(uriStr);
        row.setPrefixAdd(prefixAdd);
        write();
    }

    @Override
    public void deletePrefix(Node gn, String prefix) {
        prefixDel.clear();
        if ( gn != null ) {
            tv.clear();
            toThrift(gn, tv);
            prefixDel.setGraphNode(tv);
        }
        prefixDel.setPrefix(prefix);
        row.setPrefixDel(prefixDel);
        write();
    }

    @Override
    public void txnBegin() {
        row.setTxn(PatchTxn.TX);
        write();
    }

    @Override
    public void txnCommit() {
        row.setTxn(PatchTxn.TC);
        write();
    }

    @Override
    public void txnAbort() {
        row.setTxn(PatchTxn.TA);
        write();
    }

    @Override
    public void segment() {
        row.setTxn(PatchTxn.Segment);
        write();
    }

    /*package*/ static void toThrift(Node node, RDF_Term term) {
        if ( node.isURI() ) {
            RDF_IRI iri = new RDF_IRI(node.getURI());
            term.setIri(iri);
            return;
        }

        if ( node.isBlank() ) {
            RDF_BNode b = new RDF_BNode(node.getBlankNodeLabel());
            term.setBnode(b);
            return;
        }

        if ( node.isURI() ) {
            RDF_IRI iri = new RDF_IRI(node.getURI());
            term.setIri(iri);
            return;
        }

        if ( node.isLiteral() ) {
            String lex = node.getLiteralLexicalForm();
            String dt = node.getLiteralDatatypeURI();
            String lang = node.getLiteralLanguage();

            // General encoding.
            RDF_Literal literal = new RDF_Literal(lex);
            if ( node.getLiteralDatatype().equals(XSDDatatype.XSDstring) ||
                    node.getLiteralDatatype().equals(RDFLangString.rdfLangString) ) {
                dt = null;
            }

            if ( dt != null ) {
                literal.setDatatype(dt);
            }
            if ( lang != null && ! lang.isEmpty() )
                literal.setLangtag(lang);
            term.setLiteral(literal);
            return;
        }

        if ( node.isVariable() ) {
            RDF_VAR var = new RDF_VAR(node.getName());
            term.setVariable(var);
            return;
        }

        if ( node.isNodeTriple() ) {
            Triple triple = node.getTriple();

            RDF_Term sTerm = new RDF_Term();
            toThrift(triple.getSubject(), sTerm);

            RDF_Term pTerm = new RDF_Term();
            toThrift(triple.getPredicate(), pTerm);

            RDF_Term oTerm = new RDF_Term();
            toThrift(triple.getObject(), oTerm);

            RDF_Triple tripleTerm = new RDF_Triple(sTerm, pTerm, oTerm);
            term.setTripleTerm(tripleTerm);
            return ;
        }

//        if ( Node.ANY.equals(node)) {
//            term.setAny(ANY);
//            return;
//        }
        throw new PatchException("Node converstion not supported: "+node);
    }
}