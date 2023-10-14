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

package org.apache.jena.shex.writer;

import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.writer.DirectiveStyle;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.ShexShape;
import org.apache.jena.shex.expressions.*;
import org.apache.jena.shex.sys.SysShex;

/** Print in ShExC format */
public class WriterShExC {

    public static void print(OutputStream out, ShexSchema schema) {
        IndentedWriter iOut = new IndentedWriter(out);
        int row = iOut.getRow();
        int col = iOut.getCol();
        WriterShExC.print(iOut, schema);
        iOut.flush();
        // Do not close because that closes the underlying stream.
    }

    public static void print(IndentedWriter out, ShexSchema schema) {
        boolean hasHeader = false;
        if ( schema.getBase() != null ) {
            out.println("BASE   <"+schema.getBase()+">");
            hasHeader = true;
        }
        if ( schema.getPrefixMap() != null && ! schema.getPrefixMap().isEmpty() ) {
            RiotLib.writePrefixes(out, schema.getPrefixMap(), DirectiveStyle.KEYWORD);
            hasHeader = true;
        }
        if ( schema.getImports() != null && ! schema.getImports().isEmpty() ) {
            schema.getImports().forEach(importURI -> out.println("IMPORT <"+importURI+">"));
            hasHeader = true;
        }
        //schema.getSource();
        // ShexShape start = schema.getStart();
        boolean printNL = hasHeader;
        NodeFormatter formatter = new NodeFormatterTTL(schema.getBase(), schema.getPrefixMap());

        // XXX [Print] printNL needs to flow across calls.
        //PrintCxt cxt = new PrintCxt(out, schema.getBase(), schema.getPrefixMap());
        schema.getShapes().forEach( shape->print(out, formatter, shape, printNL) );
    }

    private static void print(IndentedWriter out, NodeFormatter formatter, ShexShape shape, boolean printNL) {
        if (printNL)
            out.println();
        if ( shape.getLabel() != null ) {

            Node n = shape.getLabel();
            if ( n.equals(SysShex.startNode) )
                out.print("START=");
            else
                formatter.format(out, n);
            out.print(" ");
        }

        PrinterShExC shexPrinter = new PrinterShExC(out, formatter);
        ShapeExpression shapeEx = shape.getShapeExpression();
        shexPrinter.printShapeExpression(shapeEx);
    }

    static <X> void printList(IndentedWriter out, List<X> items, String start, String finish, String sep, Consumer<X> action) {
        if (items.isEmpty())
            return;
        if (items.size() == 1 ) {
            action.accept(items.get(0));
            return;
        }
        if ( start != null ) {
            out.print(start);
            out.print(" ");
        }
        boolean first = true;
        for ( X elt : items ) {
            if ( first ) {
                first = false;
            } else {
                if ( sep != null )
                    out.print(sep);
                out.print(" ");
            }
            action.accept(elt);
        }
        if ( finish != null ) {
            out.print(" ");
            out.print(finish);
        }
    }

    /** Write a string - basic escaping, no quote escaping. */
    // XXX [Print] -> ShexParserLib -> ShexRegexStr
    static void regexStringEsc(AWriter out, String s) {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            // \\ Escape always possible.
//                if (c == '\\') {
//                    out.print('\\') ;
//                    out.print(c) ;
//                    continue ;
//                }
            switch(c) {
                case '\n':  out.print("\\n"); continue;
                case '\t':  out.print("\\t"); continue;
                case '\r':  out.print("\\r"); continue;
                case '\f':  out.print("\\f"); continue;
                case'/':
//                    case '\\': case '|': case '.' : case'?':
//                    case'*': case'+': case'(': case')': case'{': case'}': case'$':
//                    case'-': case'[': case']': case'/':
                //case '^':
                    out.print("\\");
                    out.print(c);
                    continue;
                default:
                    out.print(c);
            }
        }
    }

    private static class PrinterShExC implements ShapeExprVisitor, TripleExprVisitor, NodeConstraintVisitor {
        final IndentedWriter out;
        final NodeFormatter formatter;

        public PrinterShExC(IndentedWriter out, NodeFormatter formatter) {
            this.out = out;
            this.formatter = formatter;
        }

        private void printShapeExpression(ShapeExpression shapeExpression) {
            shapeExpression.visit(this);
        }

        private void printTripleExpression(TripleExpression tripleExpr) {
            tripleExpr.visit(this);
            out.print(" ;");
        }

        private void printTripleExpressionNoSep(TripleExpression tripleExpr) {
            tripleExpr.visit(this);
        }

        private void printNodeConstraint(NodeConstraintComponent ncc) {
            ncc.visit(this);
        }

        private void printNode(Node node) {
            formatter.format(out, node);
        }

        private boolean inValueSet = false;

        private void printValueSetRange(ValueSetRange vsr) {
            // [LAYOUT] Gaps.
            // Need ValueSet object.

            boolean b = inValueSet;
            if ( !b ) {
                out.print("[ ");
                inValueSet = true;
            }

            out.print(" ");
            // [LAYOUT]
            // Need enum.
            String type = vsr.type();
            if ( type.equals("unknown") )
                out.print(".");
            else
                out.print(vsr.type());
            printValueSetItem(vsr.item());
            if ( vsr.numExclusions() > 0 ) {
                // [LAYOUT] Which is it?
                //out.print(" -");
                vsr.exclusions(vsi->{
                    out.print(" -");
                    printValueSetItem(vsi);
                });
            }

            if ( !b ) {
                out.println(" ]");
                inValueSet = false;
            }
        }

        private void printValueSetItem(ValueSetItem item) {
            if ( item.isEmpty() ) {
                out.print(".");
                return;
            }
            item.print(out, formatter);
        }

        @Override
        public void visit(ShapeExprAND shape) {
            List<ShapeExpression> shapes = shape.expressions();
            printList(out, shape.expressions(), null, null, " AND",
                      x->{
                          // Avoid flattening S1 AND ( S2 AND S3 )
                          boolean needParens = ( x instanceof ShapeExprAND || x instanceof ShapeExprOR );
                          if ( needParens )
                              out.print("( ");
                          printShapeExpression(x);
                          if ( needParens )
                              out.print(" )");
                      });
        }

        @Override
        public void visit(ShapeExprOR shape) {
            List<ShapeExpression> shapes = shape.expressions();
            printList(out, shape.expressions(), null, null, " OR",
                      x->{
                          // Avoid flattening S1 OR ( S2 OR S3 )
                          boolean needParens = ( x instanceof ShapeExprAND || x instanceof ShapeExprOR );
                          if ( needParens )
                              out.print("( ");
                          printShapeExpression(x);
                          if ( needParens )
                              out.print(" )");
                      });
        }

        @Override
        public void visit(ShapeExprNOT shape) {
            out.print("NOT ");
            ShapeExpression shExpr = shape.subShape();
            boolean needParens = true;

            if ( shExpr instanceof ShapeExprTripleExpr )
                needParens = false;
            else if ( shExpr instanceof ShapeNodeConstraint )
                needParens = false;
            else if ( shExpr instanceof ShapeExprAtom )
                needParens = false;

            if ( needParens ) {
                out.print("( ");
            }
            printShapeExpression(shape.subShape());
            if ( needParens ) {
                out.print(" ) ");
            }
        }

        @Override
        public void visit(ShapeExprDot shape) {
            out.print(". ");
        }

        @Override
        public void visit(ShapeExprAtom shape) {
            final boolean multiLine = false;
            if ( shape.getShape() == null )
                return;
            if ( multiLine ) {
                out.println("(");
                out.incIndent();
                printShapeExpression(shape.getShape());
                out.decIndent();
                out.println(")");
            } else {
                out.print("( ");
                out.incIndent();
                printShapeExpression(shape.getShape());
                out.decIndent();
                out.print(" ) ");
            }
        }

        @Override
        public void visit(ShapeExprFalse shape) { out.print("FALSE"); }

        @Override
        public void visit(ShapeExprNone shape) {
            out.print("{ }");
        }

        @Override
        public void visit(ShapeExprRef shape) {
            out.print("@");
            printNode(shape.getRef());
        }

        @Override
        public void visit(ShapeExprTrue shape) {
            out.print(" . ");
        }

        @Override
        public void visit(ShapeExprExternal shape) {
            out.println("EXTERNAL");
        }

        @Override
        public void visit(ShapeExprTripleExpr shape) {
            TripleExpression tripleExpr = shape.getTripleExpr();
            if ( shape.isClosed() )
                out.println("CLOSED ");
            if ( shape.getExtras() != null && ! shape.getExtras().isEmpty() ) {
                out.println("EXTRA ");
                shape.getExtras().forEach(n-> { formatter.format(out, n); out.print(" ");});
            }
            out.println("{");
            out.incIndent();

            printTripleExpression(tripleExpr);

            out.decIndent();
            out.println("}");
        }

        @Override
        public void visit(StrRegexConstraint constraint) {
            out.print("/");
            //[LAYOUT] Escapes
            String pattern = constraint.getPattern();
            regexStringEsc(out, pattern);
            out.print("/");
            if ( constraint.getFlagsStr() != null ) {
                out.print(constraint.getFlagsStr());
            }
        }

        @Override
        public void visit(StrLengthConstraint constraint) {
            //stringLength       ::=      "LENGTH" | "MINLENGTH" | "MAXLENGTH"
            out.print(constraint.getLengthType().label().toUpperCase(Locale.ROOT));
            out.print(" ");
            out.print(Integer.toString(constraint.getLength()));
        }

        @Override
        public void visit(DatatypeConstraint constraint) {
            formatter.formatURI(out, constraint.getDatatypeURI());
        }

        @Override
        public void visit(NodeKindConstraint constraint) {
            out.print(constraint.getNodeKind().label().toUpperCase(Locale.ROOT));
        }

        // [30]        numericFacet       ::=      numericRange numericLiteral | numericLength INTEGER

        @Override
        public void visit(NumLengthConstraint constraint) {
            // [32]        numericLength      ::=      "TOTALDIGITS" | "FRACTIONDIGITS"
            out.print(constraint.getLengthType().label().toUpperCase(Locale.ROOT));
            out.print(" ");
            out.print(Integer.toString(constraint.getLength()));
        }

        @Override
        public void visit(NumRangeConstraint constraint) {
            // [31]        numericRange       ::=      "MININCLUSIVE" | "MINEXCLUSIVE" | "MAXINCLUSIVE" | "MAXEXCLUSIVE"
            out.print(constraint.getRangeKind().label().toUpperCase(Locale.ROOT));
            out.print(" ");
            printNode(constraint.getValue());
        }

        @Override
        public void visit(ValueConstraint constraint) {
            // [49]        valueSetValue      ::=      iriRange | literalRange | languageRange | exclusion+
            out.print("[ ");
            constraint.forEach(valueSetRange->{
                printValueSetItem(valueSetRange.item());
                valueSetRange.exclusions(vsItem->{
                    out.print(" -");
                    printValueSetItem(vsItem);
                });
            });
            out.print(" ]");
        }

        @Override public void visit(TripleExprCardinality tripleExpr) {
            out.incIndent();
            out.print("( ");
            printTripleExpressionNoSep(tripleExpr.target());
            out.print(" )");
            String x = tripleExpr.cardinalityString();
            out.print(x);
            out.decIndent();
        }

        @Override public void visit(TripleExprEachOf tripleExpr) {
            printList(out, tripleExpr.expressions(), "(", ")", null, tExpr->{
                out.incIndent();
                printTripleExpression(tExpr);
                out.decIndent();
            });
            out.println();
        }
        @Override public void visit(TripleExprOneOf tripleExpr) {
            printList(out, tripleExpr.expressions(), "(", ")", "|", tExpr->{
                    out.incIndent();
                    printTripleExpression(tExpr);
                    out.decIndent();
               });
           out.println();
        }

        @Override public void visit(TripleExprNone tripleExpr) { /* Nothing */ }

        @Override public void visit(TripleExprRef tripleExpr) {
            out.print("&");
            printNode(tripleExpr.ref());
        }

        @Override public void visit(TripleConstraint tripleExpr) {
            Node predicate = tripleExpr.getPredicate();
            //formatter.format(w, node);
            if ( tripleExpr.reverse() )
                out.print("^");
            printNode(predicate);
            out.print(" ");
            printShapeExpression(tripleExpr.getShapeExpression());
            String x =  tripleExpr.cardinalityString();
            if ( x != null && ! x.isEmpty() ) {
                out.print(" ");
                out.print(x);
            }
        }

        @Override
        public void visit(ShapeNodeConstraint shape) {
            if ( shape.getNodeConstraint() != null ) {
                printList(out, shape.getNodeConstraint().components(), null, null, null,
                          nc->{
                              out.incIndent();
                              printNodeConstraint(nc);
                              out.decIndent();
                          });
            }
        }
    }
}
