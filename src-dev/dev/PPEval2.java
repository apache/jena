/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.LinkedHashSet ;
import java.util.Set ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQNotImplemented ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.path.* ;

public class PPEval2
{
        // Calculate ?x path{0} ?y 
        private static void evalZeroLengthPath(Collection<Node> acc, Node s, Path path, Node o)
        {
            /*
             * zeropath(?x (path){0} ?y, G) = { μ | μ(?x->iri) and μ(?y->iri) for all IRIs
             *    which are subject or objects of a triple in G }
             *
             * zeropath(iri (path){0} ?z) = { μ | μ(?z->iri) }
             * 
             * zeropath(iri1 (path){0} iri2) matches if iri1 = iri2.             
             */
        }
        
        // Calculate ?x path+ ?y 
        private static void evalArbitraryLengthPath(Collection<Node> acc, Node s, Path path, Node o)
        {
            /*
             * ArbitraryLengthPath(X (path)+ Y) = eval(X, path, Y, {})
             * where
             *   X - a set of nodes, or a variable
             *   Y - a set of nodes, or a variable
             *   S - a set of nodes traversed
             *   R - the set of bindings that are solutions
             * 
             * eval(X:Variable, path, Y, S) =
             *   R = {}
             *   for each subject x in G
             *     R = R + { (X,x) } UNION eval(x, path Y, S) 
             *   result is R
             * 
             * eval(x:RDFTerm, path, Y, S) =
             *     S := S + {x}
             *     T = evalPath({z} path Y)
             *     for solution μ in T:
             *        R := R + {(Y, y)} if Y is a variable
             *        fi
             *     end
             *     S := S \ {x}
             *   result is R
             */
        }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */