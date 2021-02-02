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

package org.apache.jena.sparql.modify.request;

import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.modify.request.UpdateDataWriter.UpdateMode ;
import org.apache.jena.sparql.serializer.FormatterElement ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.syntax.Element ;
import org.apache.jena.sparql.system.SinkQuadBracedOutput;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.sparql.util.NodeToLabelMapBNode ;

public class UpdateWriterVisitor implements UpdateVisitor {
    protected static final int BLOCK_INDENT = 2;
    protected final IndentedWriter out;
    protected final SerializationContext sCxt;

    public UpdateWriterVisitor(IndentedWriter out, SerializationContext sCxt) {
        this.out = out;
        this.sCxt = sCxt;
    }

    protected void visitDropClear(String name, UpdateDropClear update) {
        out.ensureStartOfLine();
        out.print(name);
        out.print(" ");
        if ( update.isSilent() )
            out.print("SILENT ");
        printTarget(update.getTarget());
    }

    protected void printTarget(Target target) {
        if ( target.isAll() ) {
            out.print("ALL");
        } else if ( target.isAllNamed() ) {
            out.print("NAMED");
        } else if ( target.isDefault() ) {
            out.print("DEFAULT");
        } else if ( target.isOneNamedGraph() ) {
            out.print("GRAPH ");
            String s = FmtUtils.stringForNode(target.getGraph(), sCxt);
            out.print(s);
        } else {
            out.print("Target BROKEN");
            throw new ARQException("Malformed Target");
        }
    }

    @Override
    public void visit(UpdateDrop update)
    { visitDropClear("DROP", update) ; }

    @Override
    public void visit(UpdateClear update)
    { visitDropClear("CLEAR", update) ; }

    @Override
    public void visit(UpdateCreate update) {
        out.ensureStartOfLine();
        out.print("CREATE");
        out.print(" ");
        if ( update.isSilent() )
            out.print("SILENT ");
        out.print("GRAPH");
        out.print(" ");
        String s = FmtUtils.stringForNode(update.getGraph(), sCxt);
        out.print(s);
    }

    @Override
    public void visit(UpdateLoad update) {
        out.ensureStartOfLine();
        out.print("LOAD");
        out.print(" ");
        if ( update.getSilent() )
            out.print("SILENT ");

        outputStringAsURI(update.getSource());

        if ( update.getDest() != null ) {
            out.print(" INTO GRAPH ");
            output(update.getDest());
        }
    }

    protected void outputStringAsURI(String uriStr) {
        String x = FmtUtils.stringForURI(uriStr, sCxt);
        out.print(x);
    }

    protected void printTargetUpdate2(Target target) {
        if ( target.isDefault() ) {
            out.print("DEFAULT");
        } else if ( target.isOneNamedGraph() ) {
            // out.print("GRAPH ") ;
            String s = FmtUtils.stringForNode(target.getGraph(), sCxt);
            out.print(s);
        } else {
            out.print("Target BROKEN / Update2");
            throw new ARQException("Malformed Target / Update2");
        }
    }

    protected void printUpdate2(UpdateBinaryOp update, String name) {
        out.print(name);
        if ( update.getSilent() )
            out.print(" SILENT");
        out.print(" ");
        printTargetUpdate2(update.getSrc());
        out.print(" TO ");
        printTargetUpdate2(update.getDest());
    }

    @Override
    public void visit(UpdateAdd update)
    { printUpdate2(update, "ADD") ; }

    @Override
    public void visit(UpdateCopy update)
    { printUpdate2(update, "COPY") ; }

    @Override
    public void visit(UpdateMove update)
    { printUpdate2(update, "MOVE") ; }

    @Override
    public void visit(UpdateDataInsert update) {
        UpdateDataWriter udw = new UpdateDataWriter(UpdateMode.INSERT, out, sCxt);
        udw.open();
        try {
            Iter.sendToSink(update.getQuads().iterator(), udw);
        }
        finally {
            udw.close();
        }
    }

    @Override
    public void visit(UpdateDataDelete update) {
        UpdateDataWriter udw = new UpdateDataWriter(UpdateMode.DELETE, out, sCxt);
        udw.open();
        try {
            Iter.sendToSink(update.getQuads().iterator(), udw);
        }
        finally {
            udw.close();
        }
    }

    protected void outputQuadsBraced(List<Quad> quads) {
        if ( quads.size() == 0 ) {
            out.print("{ }");
            return;
        }

        SinkQuadBracedOutput sink = new SinkQuadBracedOutput(out, sCxt);
        sink.open();
        Iter.sendToSink(quads.iterator(), sink);
    }

    protected void output(Node node) {
        String $ = FmtUtils.stringForNode(node, sCxt);
        out.print($);
    }

    @Override
    public void visit(UpdateDeleteWhere update) {
        out.ensureStartOfLine();
        out.println("DELETE WHERE ");
        outputQuadsBraced(update.getQuads());
    }

    @Override
    public void visit(UpdateModify update) {
        out.ensureStartOfLine();
        if ( update.getWithIRI() != null ) {
            // out.ensureStartOfLine() ;
            out.print("WITH ");
            output(update.getWithIRI());
        }

        if ( update.hasDeleteClause() ) {
            List<Quad> deleteQuads = update.getDeleteQuads();
            out.ensureStartOfLine();
            out.print("DELETE ");
            outputQuadsBraced(deleteQuads);
        }

        if ( update.hasInsertClause() ) {
            List<Quad> insertQuads = update.getInsertQuads();
            out.ensureStartOfLine();
            out.print("INSERT ");
            outputQuadsBraced(insertQuads);
        }

        if ( !update.hasInsertClause() && !update.hasDeleteClause() ) {
            // Fake a clause to make it legal syntax.
            out.ensureStartOfLine();
            out.println("INSERT { }");
        }

        for ( Node x : update.getUsing() ) {
            out.ensureStartOfLine();
            out.print("USING ");
            output(x);
        }

        for ( Node x : update.getUsingNamed() ) {
            out.ensureStartOfLine();
            out.print("USING NAMED ");
            output(x);
        }

        Element el = update.getWherePattern() ;
        out.ensureStartOfLine() ;
        out.print("WHERE") ;
        out.incIndent(BLOCK_INDENT) ;
        out.newline() ;

        if ( el != null ) {
            FormatterElement fmtElement = prepareElementFormatter();
            fmtElement.visitAsGroup(el);
        } else
            out.print("{}");
        out.decIndent(BLOCK_INDENT);
    }

    protected FormatterElement prepareElementFormatter() {
        SerializationContext sCxt1 = new SerializationContext(sCxt);
        // The label prefix is different to the template writer just for clarity.
        sCxt1.setBNodeMap(new NodeToLabelMapBNode("x", false));
        return new FormatterElement(out, sCxt1);
    }
}
