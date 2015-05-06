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

package org.apache.jena.riot.other;

import java.util.Iterator ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/** A collection of Graph/Triple/Node related functions */
public class GLib
{
    /** Convert null to Node.ANY */
    public static Node nullAsAny(Node x) { return nullAsDft(x, Node.ANY) ; }
    
    /** Convert null to some default Node */
    public static Node nullAsDft(Node x, Node dft) { return x==null ? dft : x ; }
    
    // DISTINCT means these are space using.
    /** List the subjects in a graph (no duplicates) */
    public static Iterator<Node> listSubjects(Graph graph)
    {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        return Iter.iter(iter).map(Triple::getSubject).distinct() ;
    }

    /** List the predicates in a graph (no duplicates) */
    public static Iterator<Node> listPredicates(Graph graph)
    {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        return Iter.iter(iter).map(Triple::getPredicate).distinct() ;
    }
    
    /** List the objects in a graph (no duplicates) */
    public static Iterator<Node> listObjects(Graph graph)
    {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        return Iter.iter(iter).map(Triple::getObject).distinct() ;
    }

    /** Project quads to triples */
    public static Iter<Triple> quads2triples(Iterator<Quad> iter)
    {
        return Iter.iter(iter).map(Quad::asTriple) ;
    }

    /** Project quad to graphname */
    public static Iterator<Node> quad2graphName(Iterator<Quad> iter)
    { return Iter.map(iter, Quad::getGraph) ; }
    
    /** Project quad to graphname */
    public static Iterator<Node> quad2subject(Iterator<Quad> iter)
    { return Iter.map(iter, Quad::getSubject) ; }
    
    /** Project quad to predicate */
    public static Iterator<Node> quad2predicate(Iterator<Quad> iter)
    { return Iter.map(iter, Quad::getPredicate) ; }
    
    /** Project quad to object */
    public static Iterator<Node> quad2object(Iterator<Quad> iter)
    { return Iter.map(iter, Quad::getObject) ; }
    
    /** Project triple to subject */ 
    public static Iterator<Node> triple2subject(Iterator<Triple> iter)
    { return Iter.map(iter, Triple::getSubject) ; }
    
    /** Project triple to predicate */ 
    public static Iterator<Node> triple2predicate(Iterator<Triple> iter)
    { return Iter.map(iter, Triple::getPredicate) ; }
    
    /** Project triple to object */ 
    public static Iterator<Node> triple2object(Iterator<Triple> iter)
    { return Iter.map(iter, Triple::getObject) ; }
}

