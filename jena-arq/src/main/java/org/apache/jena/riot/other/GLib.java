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
import org.apache.jena.atlas.iterator.Transform ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

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
        return Iter.iter(iter).map(projectTripleSubject).distinct() ;
    }

    /** List the predicates in a graph (no duplicates) */
    public static Iterator<Node> listPredicates(Graph graph)
    {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        return Iter.iter(iter).map(projectTriplePredicate).distinct() ;
    }
    
    /** List the objects in a graph (no duplicates) */
    public static Iterator<Node> listObjects(Graph graph)
    {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        return Iter.iter(iter).map(projectTripleObject).distinct() ;
    }
    
    private static Transform<Quad, Triple> transformQuad2Triple = new Transform<Quad, Triple> () {
        @Override
        public Triple convert(Quad quad)    { return quad.asTriple() ; }
    } ;

    /** Project quads to triples */
    public static Iter<Triple> quads2triples(Iterator<Quad> iter)
    {
        return Iter.iter(iter).map(transformQuad2Triple) ;
    }

    /** Project quad to graphname */
    public static Iterator<Node> quad2graphName(Iterator<Quad> iter)
    { return Iter.map(iter, projectQuadGraphName) ; }
    
    /** Project quad to graphname */
    public static Iterator<Node> quad2subject(Iterator<Quad> iter)
    { return Iter.map(iter, projectQuadSubject) ; }
    
    /** Project quad to predicate */
    public static Iterator<Node> quad2predicate(Iterator<Quad> iter)
    { return Iter.map(iter, projectQuadPredicate) ; }
    
    /** Project quad to object */
    public static Iterator<Node> quad2object(Iterator<Quad> iter)
    { return Iter.map(iter, projectQuadObject) ; }
    
    /** Project triple to subject */ 
    public static Iterator<Node> triple2subject(Iterator<Triple> iter)
    { return Iter.map(iter, projectTripleSubject) ; }
    
    /** Project triple to predicate */ 
    public static Iterator<Node> triple2predicate(Iterator<Triple> iter)
    { return Iter.map(iter, projectTriplePredicate) ; }
    
    /** Project triple to object */ 
    public static Iterator<Node> triple2object(Iterator<Triple> iter)
    { return Iter.map(iter, projectTripleObject) ; }

    /** Transform quad to graphname */
    public static Transform<Quad, Node> projectQuadGraphName = new Transform<Quad, Node>() {
        @Override  public Node convert(Quad quad) { return quad.getGraph() ; }
    } ;
    /** Transform quad to subject */
    public static Transform<Quad, Node> projectQuadSubject = new Transform<Quad, Node>() {
        @Override  public Node convert(Quad quad) { return quad.getSubject() ; }
    } ;
    /** Transform quad to predicate */
    public static Transform<Quad, Node> projectQuadPredicate = new Transform<Quad, Node>() {
        @Override public Node convert(Quad quad) { return quad.getPredicate() ; }
    } ;
    /** Transform quad to object */
    public static Transform<Quad, Node> projectQuadObject = new Transform<Quad, Node>() {
        @Override public Node convert(Quad quad) { return quad.getObject() ; }
    } ;
    /** Transform triple to subject */ 
    public static Transform<Triple, Node> projectTripleSubject   = new Transform<Triple, Node>() {
        @Override public Node convert(Triple triple) { return triple.getSubject() ; }
    } ;
    /** Transform triple to predicate */ 
    public static Transform<Triple, Node> projectTriplePredicate = new Transform<Triple, Node>() {
        @Override public Node convert(Triple triple) { return triple.getPredicate() ; }
    } ;
    /** Transform triple to object */ 
    public static Transform<Triple, Node> projectTripleObject    = new Transform<Triple, Node>() {
        @Override public Node convert(Triple triple) { return triple.getObject() ; }
    } ;
}

