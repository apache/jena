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

import java.util.Objects;

import org.apache.jena.enhanced.EnhGraph ;
import org.apache.jena.enhanced.EnhNode ;
import org.apache.jena.enhanced.Implementation ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.rdf.model.* ;

/**
 * An implementation of statement terms (RDf 1.2 triple terms).
 */

public class StatementTermImpl extends EnhNode implements StatementTerm {

    final static public Implementation factory = new Implementation() {
        @Override
        public boolean canWrap( Node n, EnhGraph eg )
            { return !n.isLiteral(); }
        @Override
        public EnhNode wrap(Node n,EnhGraph eg) {
            if (n.isLiteral()) throw new ResourceRequiredException( n );
            return new StatementTermImpl(n,eg);
        }
    };
    final static public Implementation rdfNodeFactory = new Implementation() {
        @Override
        public boolean canWrap( Node n, EnhGraph eg )
        { return true; }
        @Override
        public EnhNode wrap(Node n,EnhGraph eg) {
            if ( n.isURI() || n.isBlank() )
                return new StatementTermImpl(n,eg);
            if ( n.isLiteral() )
                return new LiteralImpl(n,eg);
            return null;
        }
    };

    private final Statement statement;

    public StatementTermImpl(Statement stmt) {
        this( stmt, (ModelCom)(stmt.getModel()));
    }

    public StatementTermImpl(Statement statement, ModelCom m) {
        super( NodeFactory.createTripleTerm(statement.asTriple()), m);
        this.statement = Objects.requireNonNull(statement);
    }

    public StatementTermImpl( Node n, EnhGraph m ) {
        super( n, m );
        if ( ! n.isTripleTerm() )
            throw new StmtTermRequiredException(n);
        this.statement = StatementImpl.toStatement(n.getTriple(), (ModelCom)m);
    }

    @Override
    public Resource asResource()
    { throw new ResourceRequiredException( asNode() ); }

    @Override
    public Literal asLiteral()
    { throw new LiteralRequiredException( asNode() ); }

    @Override
    public StatementTerm asStatementTerm() {
        return this;
    }

    @Override
    public Statement getStatement() {
        return statement;
    }

    @Override
    public Model getModel() {
        return (Model)enhGraph;
    }

    @Override
    public Object visitWith(RDFVisitor rv) {
        return rv.visitStmt(this, this.getStatement());
    }

    @Override
    public StatementTerm inModel( Model m ) {
        if ( getModel() == m )
            return this;
        return new StatementTermImpl(statement, (ModelCom)m);
    }
}
