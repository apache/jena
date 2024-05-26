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

package org.apache.jena.sparql.sse;

import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.writers.WriterNode;
import org.apache.jena.sparql.util.FmtUtils;

public class ItemWriter
{
    public static boolean includeBase = false;
    private static boolean CloseSameLine = true;

    public static void write(OutputStream out, Item item) {
        IndentedWriter iw = new IndentedWriter(out);
        write(iw, item, null);
        iw.ensureStartOfLine();
        iw.flush();
    }

    public static void write(IndentedWriter out, Item item, SerializationContext sCxt) {
        writeInline(out, item, sCxt);
    }

    // Core function. Does not apply reverse lift. Does not write prefixes or base.
    private static void writeInline(IndentedWriter out, Item item, SerializationContext sCxt) {
        Print pv = new Print(out, sCxt);
        pv.startPrint();
        item.visit(pv);
        pv.finishPrint();
    }

    // Core function. Writes self contained output with prfixes and base.
    private static void writeFn(IndentedWriter out, Item item, SerializationContext sCxt) {
        Print pv = new Print(out, sCxt);
        pv.startPrint();
        item.visit(pv);
        pv.finishPrint();
    }

    // No compound node conversion.
    private static final boolean writeQuotedTriplesAsNodes = true;

    /**
     * Write a node in SSE syntax. This code writes the node as given, with symbol
     * translation but no compound lift conversions
     * <p>
     * {@linkplain ItemLift#lowerCompound(Item)} turns RDF term (nodes) into compound
     * form. In writers - call
     * {@link WriterNode#output(IndentedWriter, Node, SerializationContext)} which
     * converse compounds and then calls this function.
     */
    public static void write(IndentedWriter out, Node node, SerializationContext sCxt) {
        writeNode(out, node, sCxt);
    }

    private static void writeNode(IndentedWriter out, Node node, SerializationContext sCxt) {
        if ( node.isNodeTriple() ) {
            Triple t = node.getTriple();
            if ( writeQuotedTriplesAsNodes ) {
                // As <<....>>
                out.print("<< ");
                writeNode(out, t.getSubject(), sCxt);
                out.print(" ");
                writeNode(out, t.getPredicate(), sCxt);
                out.print(" ");
                writeNode(out, t.getObject(), sCxt);
                out.print(" >>");
            } else {
                // As (qtriple ...)
                out.print("(");
                out.print(Tags.tagQTriple);
                out.print(" ");
                writeNode(out, t.getSubject(), sCxt);
                out.print(" ");
                writeNode(out, t.getPredicate(), sCxt);
                out.print(" ");
                writeNode(out, t.getObject(), sCxt);
                out.print(")");
            }
            return;
        }

        if ( Node.ANY.equals(node) ) {
            out.print("ANY");
            return;
        }

        if ( NodeConst.TRUE.equals(node) ) {
            out.print("true");
            return;
        }

        if ( NodeConst.FALSE.equals(node) ) {
            out.print("false");
            return;
        }

        // Atomic SSE Node - URI, BNode, Literal, Variable.
        out.print(FmtUtils.stringForNode(node, sCxt));
    }

    private static class Print implements ItemVisitor {
        IndentedWriter out;
        SerializationContext sCxt;
        boolean doneBase = false;
        boolean donePrefix = false;

        Print(IndentedWriter out, SerializationContext sCxt) {
            if ( sCxt == null )
                sCxt = new SerializationContext();
            this.out = out;
            this.sCxt = sCxt;
        }

        void startPrint() {}

        void writeContext() {
            if ( sCxt != null ) {
                if ( includeBase && sCxt.getBaseIRI() != null ) {
                    out.print("(base ");
                    out.println(FmtUtils.stringForURI(sCxt.getBaseIRI()));
                    doneBase = true;
                    out.incIndent();
                }
                PrefixMapping pmap = sCxt.getPrefixMapping();
                if ( pmap != null ) {
                    Map<String, String> pm = pmap.getNsPrefixMap();
                    donePrefix = (pm.size() != 0);
                    if ( pm.size() != 0 ) {
                        out.println("(prefix");
                        out.incIndent();
                        printPrefixes(pm, out);
                        out.println();
                    }
                }
            }
        }
        void finishPrint() {
            if ( doneBase ) {
                out.print(")");
                out.decIndent();
            }
            if ( donePrefix ) {
                out.print(")");
                out.decIndent();
            }
        }

        @Override
        public void visit(Item item, Node node) {
            writeNode(out, node, sCxt);
        }

        @Override
        public void visit(Item item, String symbol) {
            out.print(symbol);
        }

        // Tags to leave on one line.
        private static Set<String> oneLineTags = Set.of(Tags.tagQTriple, Tags.tagTriple);

        @Override
        public void visit(Item item, ItemList list) {
            out.print("(");

            boolean listMode = false;
            for ( Item subItem : list ) {
                if ( ! oneLine(subItem) ) {
                    listMode = true;
                    break;
                }
            }

            // Lists are printed with structure.
            // If no lists, print on one line.
            if ( listMode )
                printAsList(list);
            else
                printOneLine(list);
        }

        private static boolean oneLine(Item item) {
            if ( ! item.isList() )
                return true;
            ItemList list = item.getList();
            if ( list.isEmpty() )
                return true;
            Item item0 = list.getFirst();
            if ( ! item0.isSymbol() )
                return false;
            String symbol = item0.getSymbol();
            return oneLineTags.contains(symbol);
        }

        @Override
        public void visitNil(Item item) {
            out.print("nil");
        }

        private void printAsList(ItemList list) {
            boolean first = true;
            int indentlevel = out.getUnitIndent();
            if ( list.size() >= 1 && list.get(0).isList() )
                indentlevel = 1;

            for ( Item subItem : list ) {
                if ( !first )
                    out.println();
                subItem.visit(this);
                if ( first )
                    out.incIndent(indentlevel);
                first = false;
            }

            if ( !first )
                out.decIndent(indentlevel);
            if ( !CloseSameLine )
                out.println();
            out.print(")");
        }

        private void printOneLine(ItemList list) {
            boolean first = true;

            for ( Item subItem : list ) {
                if ( !first )
                    out.print(" ");
                first = false;
                subItem.visit(this);

            }
            out.print(")");
        }

        private void printPrefixes(Map<String, String> map, IndentedWriter out) {
            if ( map.size() == 0 )
                return;

            out.print("( ");
            out.incIndent(2);

            boolean first = true;

            for ( String s : map.keySet() ) {
                if ( !first ) {
                    out.println();
                }
                first = false;
                String k = s;
                String v = map.get(k);

                out.print("(");
                out.print(k);
                out.print(':');
                // Include at least one space
                out.print(' ', 6 - k.length());
                out.print(FmtUtils.stringForURI(v));
                out.print(")");
            }
            out.decIndent(2);
            out.print(")");
        }
    }
}
