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

package org.apache.jena.sparql.sse.writers;

import static org.apache.jena.sparql.util.FmtUtils.stringForString;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.Tags;

public class WriterExpr
{
    public static String asString(Expr expr) {
        IndentedLineBuffer b = new IndentedLineBuffer();
        output(b, expr, new SerializationContext());
        return b.asString();
    }

    public static void output(IndentedWriter out, ExprList exprs, SerializationContext sCxt) {
        output(out, exprs, true, true, sCxt);
    }

    public static void output(IndentedWriter out, ExprList exprs, boolean withTag, boolean unlist, SerializationContext sCxt) {
        if ( exprs.size() == 0 ) {
            out.print("()");
            return;
        }

        if ( exprs.size() == 1 && unlist ) {
            output(out, exprs.get(0), sCxt);
            return;
        }

        out.print("(");
        if ( withTag ) {
            out.print(Tags.tagExprList);
            out.print(" ");
        }

        for ( int i = 0; i < exprs.size(); i++ ) {
            if ( i != 0 )
                out.print(" ");
            output(out, exprs.get(i), sCxt);
        }
        out.print(")");
        out.flush();
    }

    private static void outputTail(IndentedWriter out, ExprList exprs, SerializationContext sCxt) {
        for ( int i = 0; i < exprs.size(); i++ ) {
            out.print(" ");
            output(out, exprs.get(i), sCxt);
        }
        out.print(")");
    }

    public static void output(IndentedWriter out, Expr expr, SerializationContext sCxt) {
        FmtExprPrefixVisitor fmt = new FmtExprPrefixVisitor(out, sCxt);
        expr.visit(fmt);
        out.flush();
    }

    // ----
    static final boolean ONELINE = true;
    static class FmtExprPrefixVisitor extends ExprVisitorFunction {
        IndentedWriter       out;
        SerializationContext context;

        public FmtExprPrefixVisitor(IndentedWriter writer, SerializationContext cxt) {
            out = writer;
            context = cxt;
        }

        @Override
        protected void visitExprFunction(ExprFunction func) {
            out.print("(");

            String n = null;

            if ( func.getOpName() != null )
                n = func.getOpName();

            if ( n == null )
                n = func.getFunctionName(context);

            out.print(n);
            // Hidden state.
            if ( func instanceof E_IRI ) {
                E_IRI iriExpr = (E_IRI)func;
                String parserBase = iriExpr.getParserBase();
                if ( parserBase != null ) {
                    out.print(' ');
                    out.print(stringForString(parserBase));
                }
            }
            if ( func instanceof E_IRI2 ) {
                E_IRI2 iriExpr = (E_IRI2)func;
                String parserBase = iriExpr.getParserBase();
                if ( parserBase != null ) {
                    out.print(' ');
                    out.print(stringForString(parserBase));
                }
            }

            out.incIndent();
            for ( int i = 1;; i++ ) {
                Expr expr = func.getArg(i);
                if ( expr == null )
                    break;
                // endLine();
                out.print(' ');
                expr.visit(this);
            }
            out.print(")");
            out.decIndent();
        }

        @Override
        public void visit(ExprFunctionOp funcOp) {
            out.print("(");

            // How far we are from current indent to current location
            // (beginning of operator name)
            int x = out.getCurrentOffset();
            // Indent to "("
            out.incIndent(x);

            out.print(funcOp.getFunctionName(context));
            out.incIndent();

            Op op = funcOp.getGraphPattern();
            if ( oneLine(op) )
                out.print(" ");
            else
                out.ensureStartOfLine();

            // Ensures we are unit indent under the (operator ...)
            // Without trappings.
            WriterOp.outputNoPrologue(out, funcOp.getGraphPattern(), context);
            out.decIndent();
            out.decIndent(x);
            out.print(")");
            return;
        }

        private static boolean oneLine(Op op) {
            if ( OpBGP.isBGP(op) ) {
                BasicPattern bgp = ((OpBGP)op).getPattern();
                if ( bgp.getList().size() <= 1 )
                    return true;
            }
            return false;
        }

        @Override
        public void visit(NodeValue nv) {
            // "asNode" forces node creating for values.
            WriterNode.output(out, nv.asNode(), context);
        }

        @Override
        public void visit(ExprTripleTerm tripleTerm) {
            WriterNode.output(out, tripleTerm.getNode(), context);
        }

        @Override
        public void visit(ExprNone none) {
            out.print("NONE");
        }

        @Override
        public void visit(ExprVar nv) {
            out.print(nv.toPrefixString());
        }

        @Override
        public void visit(ExprAggregator eAgg) {
            out.print(eAgg.getAggregator().toPrefixString());
        }

        private void endLine() {
            if ( ONELINE )
                out.print(' ');
            else
                out.println();
        }
    }
}
