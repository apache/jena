/**
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

package org.apache.jena.sparql.algebra.optimize;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.core.TriplePath ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarAlloc ;
import org.apache.jena.sparql.path.* ;

/**
 *  The path transformation step mostly per the SPARQL 1.1 spec with some enhancement e.g. expanding alternative paths
 *  into unions.
 *  <p>
 *  It does not necessarily produce very nice execution structures so ARQ uses a functionally equivalent, but different,
 *  transformation, see {@link TransformPathFlattern}, although that transformation covers fewer cases than this.  Some
 *  of the rough edges of this transform are however smoothed out by subsequent application of other transforms e.g.
 *  {@link TransformMergeBGPs} in the standard optimiser (see {@link OptimizerStd})
 *  </p>
 *  <p>
 *  However, for users who are using property paths in their queries heavily there may be benefits to using this
 *  transform over the default one.  The {@link org.apache.jena.query.ARQ#optPathFlattenAlgebra} symbol can be set in
 *  an ARQ context to enable this transform in preference to the default transform.
 *  </p>
 */
public class TransformPathFlattenAlgebra extends TransformCopy {
    private static VarAlloc varAlloc = new VarAlloc(ARQConstants.allocVarAnonMarker + "Q");

    /** Testing use only. */
    public static void resetForTest() {  varAlloc = new VarAlloc(ARQConstants.allocVarAnonMarker + "Q"); }

    public TransformPathFlattenAlgebra() {}

    @Override
    public Op transform(OpPath opPath) {
        TriplePath tp = opPath.getTriplePath();
        Op op = transformPath(opPath, tp.getSubject(), tp.getPath(), tp.getObject());
        // And combine adjacent triple patterns.
        return op;
    }

    static Op transformPath(OpPath op, Node subject, Path path, Node object) {
        PathTransform transform = new PathTransform(subject, object);
        path.visit(transform);
        Op r = transform.getResult();
        if ( r == null ) {
            if ( op == null )
                op = make(subject, path, object);
            return op;
        }

        return r;
    }

    static OpPath make(Node subject, Path path, Node object) {
        TriplePath tp = new TriplePath(subject, path, object);
        return new OpPath(tp);
    }

    private static Op join(Op op1, Op op2) {
        return OpJoin.create(op1, op2);
    }

    private static Op union(Op left, Op right) {
        return OpUnion.create(left, right);
    }

    private static class PathTransform extends PathVisitorBase {
        private final Node subject;
        private final Node object;
        private Op result = null;
        Op getResult() {
            return result;
        }

        public PathTransform(Node subject, Node object) {
            this.subject = subject;
            this.object = object;
            this.result = null;
        }

        @Override
        public void visit(P_Link pathNode) {
            Op op = new OpTriple(Triple.create(subject, pathNode.getNode(), object));
            result = op;
        }

        @Override
        public void visit(P_ReverseLink pathNode) {
            Op op = new OpTriple(Triple.create(object, pathNode.getNode(), subject));
            result = op;
        }

        /*
         * Reverse transformations.
         *    X !(^:uri1|...|^:urin) Y                  ==>  ^(X !(:uri1|...|:urin) Y)
         * Split into forward and reverse.
         * X !(:uri1|...|:urii|^:urii+1|...|^:urim) Y   ==>  { X !(:uri1|...|:urii|)Y } UNION { X !(^:urii+1|...|^:urim) Y }
         */
        @Override
        public void visit(P_NegPropSet pathNotOneOf) {
            Op opFwd = null;
            Op opBwd = null;
            List<P_Path0> forwards = new ArrayList<>();
            List<P_Path0> backwards = new ArrayList<>();

            for ( P_Path0 p : pathNotOneOf.getNodes() ) {
                if ( p.isForward() )
                    forwards.add(p);
                else
                    backwards.add(p);
            }

            if ( !forwards.isEmpty() ) {
                P_NegPropSet pFwd = new P_NegPropSet();
                for ( P_Path0 p : forwards )
                    pFwd.add(p);
                opFwd = make(subject, pFwd, object);
            }

            if ( !backwards.isEmpty() ) {
                // TODO Could reverse here.
                P_NegPropSet pBwd = new P_NegPropSet();
                for ( P_Path0 p : backwards )
                    pBwd.add(p);
                opBwd = make(subject, pBwd, object);
            }

            if ( opFwd == null && opBwd == null ) {
                result = make(subject, pathNotOneOf, object);
                return;
            }

            result = union(opFwd, opBwd);
        }

        @Override
        public void visit(P_Inverse inversePath) {
            PathTransform pt = new PathTransform(object, subject);
            inversePath.getSubPath().visit(pt);
            result = pt.getResult();
            if (result == null) {
                // Further transform of the sub-path was not possible BUT can still compile out the inverse
                result = make(object, inversePath.getSubPath(), subject);
            }
        }

        @Override
        public void visit(P_Mod pathMod) {
            if (pathMod.isFixedLength() && pathMod.getFixedLength() > 0) {
                // Treat as a fixed length path and convert that way instead
                Path p = PathFactory.pathFixedLength(pathMod.getSubPath(), pathMod.getFixedLength());
                Op op = transformPath(null, subject, p, object);
                result = op;
                return;
            }

            if (pathMod.getMin() < 0 || pathMod.getMax() < 0)
            {
                // Handle :p{N,}
                if (pathMod.getMin() > 0) {
                    // Handles :p{N,}
                    Node v = varAlloc.allocVar();
                    if (!subject.isVariable() || object.isVariable()) {
                        Path p1 = PathFactory.pathFixedLength(pathMod.getSubPath(), pathMod.getMin());
                        Path p2 = PathFactory.pathZeroOrMoreN(pathMod.getSubPath());
                        Op op1 = transformPath(null, subject, p1, v);
                        Op op2 = transformPath(null, v, p2, object);
                        result = OpSequence.create(op1, op2);
                    } else {
                        Path p1 = PathFactory.pathZeroOrMoreN(pathMod.getSubPath());
                        Path p2 = PathFactory.pathFixedLength(pathMod.getSubPath(), pathMod.getMin());
                        Op op1 = transformPath(null, subject, p1, v);
                        Op op2 = transformPath(null, v, p2, object);
                        result = OpSequence.create(op2, op1);
                    }
                    return;
                }

                // For :p{,M} we don't do anything currently
                // Could potentially expand into a union of all paths up to length M (plus 0 length) but not clear
                // that would actually improve performance
                result = null;
                return;
            }

            // General expansion of p{N,M} into a Union of paths of each fixed length between N and M
            // We've already handled the cases of N==M and either N or M being undefined prior to this check
            if ( pathMod.getMin() > pathMod.getMax() )
                throw new ARQException("Bad path: " + pathMod);

            Op op = null;
            for ( long i = pathMod.getMin() ; i <= pathMod.getMax() ; i++ ) {
                Path p = PathFactory.pathFixedLength(pathMod.getSubPath(), i);
                Op sub = transformPath(null, subject, p, object);
                op = union(op, sub);
            }
            result = op;
        }

        @Override
        public void visit(P_FixedLength pFixedLength) {
            Op op = null;
            Var v1 = null;
            for ( int i = 0 ; i < pFixedLength.getCount() ; i++ ) {
                Var v2 = varAlloc.allocVar();
                Node s = (v1 == null) ? subject : v1;
                Node o = (i == pFixedLength.getCount() - 1) ? object : v2;
                Op op1 = transformPath(null, s, pFixedLength.getSubPath(), o);
                op = join(op, op1);
                v1 = v2;
            }
            result = op;
        }

        @Override
        public void visit(P_Alt pathAlt) {
            Op op1 = transformPath(null, subject, pathAlt.getLeft(), object);
            Op op2 = transformPath(null, subject, pathAlt.getRight(), object);
            result = union(op1, op2);
        }

        @Override
        public void visit(P_Seq pathSeq) {
            Var v = varAlloc.allocVar();
            Op op1 = transformPath(null, subject, pathSeq.getLeft(), v);
            Op op2 = transformPath(null, v, pathSeq.getRight(), object);
            result = join(op1, op2);
        }
    }
}
