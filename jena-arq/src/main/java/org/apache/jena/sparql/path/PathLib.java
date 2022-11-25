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

package org.apache.jena.sparql.path;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.riot.other.G;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpPath ;
import org.apache.jena.sparql.algebra.op.OpSequence ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.PathBlock ;
import org.apache.jena.sparql.core.TriplePath ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.iterator.*;
import org.apache.jena.sparql.mgt.Explain ;
import org.apache.jena.sparql.path.eval.PathEval ;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.graph.GraphUtils ;

public class PathLib
{
    /** Convert any paths of exactly one predicate to a triple pattern */
    public static Op pathToTriples(PathBlock pattern) {
        BasicPattern bp = null;
        Op op = null;

        for ( TriplePath tp : pattern ) {
            if ( tp.isTriple() ) {
                if ( bp == null )
                    bp = new BasicPattern();
                bp.add(tp.asTriple());
                continue;
            }
            // Path form.
            op = flush(bp, op);
            bp = null;

            OpPath opPath2 = new OpPath(tp);
            op = OpSequence.create(op, opPath2);
            continue;
        }

        // End. Finish off any outstanding BGP.
        op = flush(bp, op);
        return op;
    }

    static private Op flush(BasicPattern bp, Op op) {
        if ( bp == null || bp.isEmpty() )
            return op;

        OpBGP opBGP = new OpBGP(bp);
        op = OpSequence.create(op, opBGP);
        return op;
    }

    /** Install a path as a property function in the global property function registry */
    public static void install(String uri, Path path)
    { install(uri, path, PropertyFunctionRegistry.get()) ; }

    /** Install a path as a property function in a given registry */
    public static void install(String uri, final Path path, PropertyFunctionRegistry registry) {
        PropertyFunctionFactory pathPropFuncFactory = (u) -> new PathPropertyFunction(path) ;
        registry.put(uri, pathPropFuncFactory) ;
    }

    public static QueryIterator execTriplePath(Binding binding, TriplePath triplePath, ExecutionContext execCxt) {
        if ( triplePath.isTriple() ) {
            // Fake it. This happens only for API constructed situations.
            Path path = new P_Link(triplePath.getPredicate());
            triplePath = new TriplePath(triplePath.getSubject(), path, triplePath.getObject());
        }

        return execTriplePath(binding,
                              triplePath.getSubject(),
                              triplePath.getPath(),
                              triplePath.getObject(),
                              execCxt) ;
    }

    public static QueryIterator execTriplePath(Binding binding, Node s, Path path, Node o, ExecutionContext execCxt) {
        Explain.explain(s, path, o, execCxt.getContext()) ;
        s = Var.lookup(binding, s) ;
        o = Var.lookup(binding, o) ;
        Iterator<Node> iter = null ;
        Node endNode = null ;
        Graph graph = execCxt.getActiveGraph() ;

        // Both variables.
        if ( Var.isVar(s) && Var.isVar(o) ) {
            if ( s.equals(o) )
                return execUngroundedPathSameVar(binding, graph, Var.alloc(s), path, execCxt);
            else
                return execUngroundedPath(binding, graph, Var.alloc(s), path, Var.alloc(o), execCxt);
        }

        // Both constants.
        if ( !Var.isVar(s) && !Var.isVar(o) )
            return evalGroundedPath(binding, graph, s, path, o, execCxt);

        // One variable, one constant
        if ( Var.isVar(s) ) {
            // Var subject, concrete object - do backwards.
            iter = PathEval.evalReverse(graph, o, path, execCxt.getContext());
            endNode = s;
        } else {
            iter = PathEval.eval(graph, s, path, execCxt.getContext());
            endNode = o;
        }
        return evalGroundedOneEnd(binding, iter, endNode, execCxt);
    }

    private static QueryIterator evalGroundedOneEnd(Binding binding, Iterator<Node> iter, Node endNode, ExecutionContext execCxt) {
        List<Binding> results = new ArrayList<>() ;

        if (! Var.isVar(endNode))
            throw new ARQInternalErrorException("Non-variable endNode in evalGroundedOneEnd") ;

        Var var = Var.alloc(endNode) ;
        // Assign.
        for (; iter.hasNext();) {
            Node n = iter.next() ;
            results.add(BindingFactory.binding(binding, var, n)) ;
        }
        return QueryIterPlainWrapper.create(results.iterator(), execCxt) ;
    }

    // Subject and object are nodes.
    private static QueryIterator evalGroundedPath(Binding binding,
                                                  Graph graph, Node subject, Path path, Node object,
                                                  ExecutionContext execCxt) {
        Iterator<Node> iter = PathEval.eval(graph, subject, path, execCxt.getContext()) ;
        // Now count the number of matches.

        int count = 0 ;
        for ( ; iter.hasNext() ; ) {
            Node n = iter.next() ;
            if ( n.sameValueAs(object) )
                count++ ;
        }

        return new QueryIterYieldN(count, binding, execCxt) ;
    }

    // Evaluation of a TriplePath where neither subject nor object are bound
    private static QueryIterator execUngroundedPath(Binding binding, Graph graph, Var sVar, Path path, Var oVar, ExecutionContext execCxt) {
        // Starting at the subject, forward direction path
        Iterator<Node> iter = ungroundedStartingSet(graph, path, execCxt) ;
        QueryIterator input = new QueryIterExtendByVar(binding, sVar, iter, execCxt);
        Function<Binding, QueryIterator> mapper = b -> {
            Iterator<Node> pathIter = PathEval.eval(graph, b.get(sVar), path, execCxt.getContext());
            QueryIterator qIter = evalGroundedOneEnd(b, pathIter, oVar, execCxt);
            return qIter;
        };
        return QueryIter.flatMap(input, mapper, execCxt);
    }

    private static QueryIterator execUngroundedPathSameVar(Binding binding, Graph graph, Var var, Path path, ExecutionContext execCxt) {
        // Starting at the subject, forward direction path
        Iterator<Node> iter = ungroundedStartingSet(graph, path, execCxt) ;
        QueryIterator input = new QueryIterExtendByVar(binding, var, iter, execCxt);
        Function<Binding, QueryIterator> mapper = b -> {
            Node n = b.get(var);
            int x = existsPath(graph, n, path, n, execCxt);
            if (x <= 0)
                return QueryIterNullIterator.create(execCxt);
            Binding b2 = BindingFactory.binding(binding, var, n);
            return new QueryIterYieldN(x, b2, execCxt);
        };
        return QueryIter.flatMap(input, mapper, execCxt);
    }

    /** Find a set of seed values.
     * <p>
     * The result must include all possibilities - it can include addition
     * elements which will be tested and rejected in the path evaluation.
     */
    private static Iterator<Node> ungroundedStartingSet(Graph graph, Path path, ExecutionContext execCxt) {
        Iterator<Node> iter = calcStartingSet(graph, path, true, execCxt);
        if ( iter != null )
            return iter;
        // If we could not find a better iterator:
        return GraphUtils.allNodes(graph) ;
    }

    /** Find a better set iterator of seed values than "everything" else return null */
    private static Iterator<Node> calcStartingSet(Graph graph, Path path, boolean forwards, ExecutionContext execCxt) {
        if ( path instanceof P_Link ) {
            // P_Link that isn't a property function.
            Node p = ((P_Link)path).getNode();
            if ( isPropertyFunction(p, execCxt.getContext()) )
                return null;
            Iterator<Node> x = forwards ? G.iterSubjectsOfPredicate(graph, p) : G.iterObjectsOfPredicate(graph, p);
            return x;
        } else if ( path instanceof P_Inverse ) {
            // ^(path) :: Flip and try inner
            Path subPath = ((P_Inverse)path).getSubPath();
            return calcStartingSet(graph, subPath, !forwards, execCxt);
        } else if ( path instanceof P_Seq ) {
            // path1 / path2 :: try the first step.
            Path subPath = ((P_Seq)path).getLeft();
            return calcStartingSet(graph, subPath, forwards, execCxt);
        } else if (path instanceof P_Alt ) {
            // path1 | path2 :: Combine both sides - must work for both sides of the "alt"
            Iterator<Node> x1 = calcStartingSet(graph, ((P_Path2)path).getLeft(), forwards, execCxt);
            if ( x1 == null )
                return null;
            Iterator<Node> x2 = calcStartingSet(graph, ((P_Path2)path).getRight(), forwards, execCxt);
            if ( x2 == null )
                return null;
            return Iter.distinct(Iter.concat(x1, x2));
        } else if ( path instanceof P_OneOrMore1 || path instanceof P_OneOrMoreN ) {
            // path+ and (^path)+ :: starting set is the starting set of the first step of the path.
            Path subPath = ((P_Path1)path).getSubPath() ;
            if ( subPath instanceof P_Link ) {
                return calcStartingSet(graph, subPath, forwards, execCxt);
            }
            if ( subPath instanceof P_Inverse ) {
                // Reversed.
                P_Inverse pInv = (P_Inverse)subPath ;
                Path reversed = pInv.getSubPath() ;
                return calcStartingSet(graph, reversed, !forwards, execCxt);
            }
        } else if ( path instanceof P_FixedLength ) {
            // path{N} :: Use first step.
            P_FixedLength fixedLengthPath = (P_FixedLength)path;
            if ( fixedLengthPath.getCount() <= 0 ) {
                // {0} which is "everything".
                return null;
            }
            Path step = ((P_FixedLength)path).getSubPath();
            return calcStartingSet(graph, step, forwards, execCxt);
        } else if ( path instanceof P_Mod ) {
            // path{N,M} :: Use first step.
            P_Mod modPath = (P_Mod)path;
            if ( modPath.getMin() == 0 || modPath.getMin() == P_Mod.UNSET ) {
                // {0,} and {,N} which is "everything".
                return null;
            }
            Path step = modPath.getSubPath();
            return calcStartingSet(graph, step, forwards, execCxt);
        }
        // Others -- P_ZeroOrMore1, P_ZeroOrMoreN, P_ZeroOrOne
        //  :p* need everything because it is always the case that "<x> :p* <x>"
        return null;
    }

    private static boolean isPropertyFunction(Node node, Context context) {
        if ( ! node.isURI() )
            return false ;
        return PropertyFunctionRegistry.chooseRegistry(context).isRegistered(node.getURI());
    }

    private static int existsPath(Graph graph, Node subject, Path path, final Node object, ExecutionContext execCxt) {
        if ( ! subject.isConcrete() || !object.isConcrete() )
            throw new ARQInternalErrorException("Non concrete node for existsPath evaluation") ;
        Iterator<Node> iter = PathEval.eval(graph, subject, path, execCxt.getContext()) ;
        Predicate<Node> filter = node -> Objects.equals(node,  object);
        // See if we got to the node we're interested in finishing at.
        iter = Iter.filter(iter, filter) ;
        long x = Iter.count(iter) ;
        return (int)x ;
    }
}
