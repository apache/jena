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
package org.apache.jena.obfuscate.transform;

import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.obfuscate.ObfuscationProvider;
import org.apache.jena.obfuscate.Obfuscator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryVisitor;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;

/**
 *
 */
public class QueryObfuscateVisitor implements QueryVisitor {
    
    // TODO WIP Needs lots more implementation

    private final ObfuscationProvider provider;
    private Query obfuscated = new Query();

    public QueryObfuscateVisitor(ObfuscationProvider provider) {
        this.provider = provider;
    }

    @Override
    public void startVisit(Query query) {
        obfuscated = new Query();
    }

    @Override
    public void visitPrologue(Prologue prologue) {
        for (Entry<String, String> prefixDef : prologue.getPrefixMapping().getNsPrefixMap().entrySet()) {
            Node nsUri = this.provider.obfuscateNode(NodeFactory.createURI(prefixDef.getValue()));
            this.obfuscated.getPrologue().setPrefix(prefixDef.getKey(), nsUri.getURI());
        }

        if (prologue.explicitlySetBaseURI()) {
            Node baseUri = this.provider.obfuscateNode(NodeFactory.createURI(prologue.getBaseURI()));
            this.obfuscated.setBaseURI(baseUri.getURI());
        }
    }

    @Override
    public void visitResultForm(Query query) {

    }

    @Override
    public void visitSelectResultForm(Query query) {
        this.obfuscated.setQuerySelectType();
        this.obfuscated.setQueryResultStar(query.isQueryResultStar());

        if (!query.isQueryResultStar()) {
            VarExprList projections = query.getProject();
            if (!projections.isEmpty()) {
                for (Var v : projections.getVars()) {
                    Node newVar = this.provider.obfuscateNode(v.asNode());
                    if (projections.hasExpr(v)) {
                        Expr e = projections.getExpr(v);
                        Expr newExpr = Obfuscator.obfuscate(this.provider, e);
                        this.obfuscated.addResultVar(v, newExpr);
                    } else {
                        this.obfuscated.addResultVar(newVar);
                    }
                }
            }
        }
    }

    @Override
    public void visitConstructResultForm(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitDescribeResultForm(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitAskResultForm(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitDatasetDecl(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitQueryPattern(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitGroupBy(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitHaving(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitOrderBy(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitLimit(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitOffset(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitValues(Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public void finishVisit(Query query) {
        // TODO Auto-generated method stub

    }

}
