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

package org.apache.jena.rdf.model.impl;

import org.apache.jena.enhanced.* ;
import org.apache.jena.graph.* ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.shared.JenaException;

/** An implementation of Statement.
 */
public class StatementImpl extends StatementBase implements Statement {

    protected Resource subject;
    protected Property predicate;
    protected RDFNode object;

    /** Creates new StatementImpl */
    public StatementImpl(Resource subject, Property predicate, RDFNode object, ModelCom model) {
        super(model);
        this.subject = subject.inModel(model);
        this.predicate = predicate.inModel(model);
        this.object = object.inModel(model);
    }

    protected static ModelCom empty = (ModelCom)ModelFactory.createDefaultModel();

    public StatementImpl(Resource subject, Property predicate, RDFNode object) {
        super(empty);
        this.subject = subject.inModel(model);
        this.predicate = predicate.inModel(model);
        this.object = object.inModel(model);
		}

    /**
	 * create a Statement from the triple _t_ in the enhanced graph _eg_. The
	 * Statement has subject, predicate, and object corresponding to those of
	 * _t_.
	 */
    public static Statement toStatement(Triple t, ModelCom eg) {
        Resource s = new ResourceImpl(t.getSubject(), eg);
        Property p = new PropertyImpl(t.getPredicate(), eg);
        RDFNode o = createObject(t.getObject(), eg);
        return new StatementImpl(s, p, o, eg);
    }

    @Override
    public Resource getSubject() {
        return subject;
    }

    @Override
    public Property getPredicate() {
        return predicate;
    }

    @Override
    public RDFNode getObject() {
        return object;
    }

    @Override
    public Statement getStatementProperty(Property p) {
        return getResource().getRequiredProperty(p);
    }

    @Override
    public Resource getResource() {
        return mustBeResource(object);
    }

    @Override
    public Statement getProperty(Property p) {
        return getResource().getRequiredProperty(p);
    }

    /**
     * get the object field of this statement, insisting that it be a Literal. If it
     * isn't, throw LiteralRequiredException.
     */
    @Override
    public Literal getLiteral() {
        if ( object instanceof Literal ) {
            return (Literal)object;
        } else {
            throw new LiteralRequiredException(object);
        }
    }

    @Override
    public Bag getBag() {
        return object.as(Bag.class);
    }

    @Override
    public Alt getAlt() {
        return object.as(Alt.class);
    }

    @Override
    public Seq getSeq() {
        return object.as(Seq.class);
    }

    @Override
    public RDFList getList() {
        return object.as(RDFList.class);
    }

    /** it turns out to be handy to return the new StatementImpl as the result */
    @Override
    protected StatementImpl replace(RDFNode n) {
        StatementImpl s = new StatementImpl(subject, predicate, n, model);
        model.remove(this).add(s);
        return s;
    }

    /**
     * .equals() defers to .sameAs so we only get the complexity of one cast.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Statement && sameAs((Statement)o);
    }

    /**
     * sameAs - is this statement equal to the statement o? We can't assume o is a
     * StatementImpl
     */
    private final boolean sameAs(Statement o) {
        return subject.equals(o.getSubject()) && predicate.equals(o.getPredicate()) && object.equals(o.getObject());
    }

    @Override
    public int hashCode() {
        return asTriple().hashCode();
    }

    @Override
    public Statement remove() {
        model.remove(this);
        return this;
    }

    @Override
    public Triple asTriple() {
        return Triple.create(subject.asNode(), predicate.asNode(), object.asNode());
    }

    /**
     * returns an array of triples corresponding to the array of statements; ie the
     * i'th element of the result is the i'th element of the input as a triple.
     *
     * @param statements the array of statements to convert
     * @return the corresponding array of triples
     */
    public static Triple[] asTriples(Statement[] statements) {
        Triple[] triples = new Triple[statements.length];
        for ( int i = 0 ; i < statements.length ; i += 1 )
            triples[i] = statements[i].asTriple();
        return triples;
    }

    /**
     * Create an RDF node from a {@link Node} for a {@link Model}.
     */
    public static RDFNode createObject(Node n, EnhGraph g) {
        if ( n.isBlank() || n.isURI() )
            return new ResourceImpl(n, g);
        if ( n.isLiteral() )
            return new LiteralImpl(n, g);
        if ( n.isTripleTerm() )
            return new StatementTermImpl(n, g);
        if (Node.ANY.equals(n) )
            return new ResourceImpl(n, g);
        throw new JenaException("Node type not compatible with Model: "+n);
    }
}
