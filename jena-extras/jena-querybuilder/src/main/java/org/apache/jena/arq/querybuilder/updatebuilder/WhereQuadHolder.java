/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.updatebuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.Converters;
import org.apache.jena.arq.querybuilder.clauses.SelectClause;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.arq.querybuilder.rewriters.BuildElementVisitor;
import org.apache.jena.arq.querybuilder.rewriters.ElementRewriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * The where processor. Generally handles update where clause.
 *
 * @see <a href=
 * "http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rGroupGraphPattern">
 * SPARQL 11 Query Language - Group Graph Pattern</a>
 *
 */
public class WhereQuadHolder implements QuadHolder {

    private Element whereClause;
    private final PrefixHandler prefixHandler;

    /**
     * Constructor.
     *
     * @param prefixHandler the prefix handler to use.
     */
    public WhereQuadHolder(PrefixHandler prefixHandler) {
        this.prefixHandler = prefixHandler;
    }

    /**
     * True if there are no elements in the where processor.
     *
     * @return true if there are no elements.
     */
    public boolean isEmpty() {
        return whereClause == null || (whereClause instanceof ElementGroup && ((ElementGroup) whereClause).isEmpty());
    }

    @Override
    public ExtendedIterator<Quad> getQuads() {
        return getQuads(Quad.defaultGraphNodeGenerated);
    }

    public ExtendedIterator<Quad> getQuads(Node defaultGraphName) {
        if (isEmpty()) {
            return NiceIterator.emptyIterator();
        }
        QuadIteratorBuilder builder = new QuadIteratorBuilder(defaultGraphName);
        whereClause.visit(builder);
        return builder.iter;
    }

    /**
     * Add all where attributes from the Where Handler argument.
     *
     * @param whereHandler The Where Handler to copy from.
     */
    public void addAll(WhereHandler whereHandler) {

        Element e = whereHandler.getQueryPattern();
        if (e != null) {
            // clone the Element
            ElementRewriter rewriter = new ElementRewriter(Collections.emptyMap());
            e.visit(rewriter);
            Element clone = rewriter.getResult();

            if (whereClause == null) {
                whereClause = clone;
            } else {
                ElementGroup eg = null;
                if (whereClause instanceof ElementGroup) {
                    eg = (ElementGroup) whereClause;
                } else {
                    eg = new ElementGroup();
                    eg.addElement(whereClause);
                }
                if (clone instanceof ElementGroup) {
                    for (Element ele : ((ElementGroup) clone).getElements()) {
                        eg.addElement(ele);
                    }
                } else {
                    eg.addElement(clone);
                }
                whereClause = eg;
            }
        }
    }

    /**
     * Get the element group for the clause. if The element group is not set, create
     * and set it.
     *
     * Public for ExprFactory use.
     *
     * @return The element group.
     */
    public ElementGroup getClause() {
        Element e = whereClause;
        if (e == null) {
            e = new ElementGroup();
            whereClause = e;
        }
        if (e instanceof ElementGroup) {
            return (ElementGroup) e;
        }

        ElementGroup eg = new ElementGroup();
        eg.addElement(e);
        whereClause = eg;
        return eg;
    }

    /**
     * Test that a triple is valid. Throws an IllegalArgumentException if the triple
     * is not valid.
     *
     * @param t The trip to test.
     */
    private static void testTriple(TriplePath t) {
        // verify Triple is valid
        boolean validSubject = t.getSubject().isURI() || t.getSubject().isBlank() || t.getSubject().isVariable()
                || t.getSubject().equals(Node.ANY);
        boolean validPredicate;

        if (t.isTriple()) {
            validPredicate = t.getPredicate().isURI() || t.getPredicate().isVariable()
                    || t.getPredicate().equals(Node.ANY);
        } else {
            validPredicate = t.getPath() != null;
        }

        boolean validObject = t.getObject().isURI() || t.getObject().isLiteral() || t.getObject().isBlank()
                || t.getObject().isVariable() || t.getObject().equals(Node.ANY);

        if (!validSubject || !validPredicate || !validObject) {
            StringBuilder sb = new StringBuilder();
            if (!validSubject) {
                sb.append(String.format("Subject (%s) must be a URI, blank, variable, or a wildcard. %n",
                        t.getSubject()));
            }
            if (!validPredicate) {
                sb.append(String.format("Predicate (%s) must be a Path, URI , variable, or a wildcard. %n",
                        t.getPredicate()));
            }
            if (!validObject) {
                sb.append(String.format("Object (%s) must be a URI, literal, blank, , variable, or a wildcard. %n",
                        t.getObject()));
            }
            if (!validSubject || !validPredicate) {
                sb.append(String.format("Is a prefix missing?  Prefix must be defined before use. %n"));
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }

    /**
     * Add the triple path to the where clause
     *
     * @param t The triple path to add.
     * @throws IllegalArgumentException If the triple path is not a valid triple
     * path for a where clause.
     */
    public void addWhere(TriplePath t) throws IllegalArgumentException {
        testTriple(t);
        ElementGroup eg = getClause();
        List<Element> lst = eg.getElements();
        if (lst.isEmpty()) {
            ElementPathBlock epb = new ElementPathBlock();
            epb.addTriple(t);
            eg.addElement(epb);
        } else {
            Element e = lst.get(lst.size() - 1);
            if (e instanceof ElementTriplesBlock && t.isTriple()) {
                ElementTriplesBlock etb = (ElementTriplesBlock) e;
                etb.addTriple(t.asTriple());
            } else if (e instanceof ElementPathBlock) {
                ElementPathBlock epb = (ElementPathBlock) e;
                epb.addTriple(t);
            } else {
                ElementPathBlock etb = new ElementPathBlock();
                etb.addTriple(t);
                eg.addElement(etb);
            }

        }
    }

    /**
     * Add an optional triple to the where clause
     *
     * @param t The triple path to add.
     * @throws IllegalArgumentException If the triple is not a valid triple for a
     * where clause.
     */
    public void addOptional(TriplePath t) throws IllegalArgumentException {
        testTriple(t);
        ElementPathBlock epb = new ElementPathBlock();
        epb.addTriple(t);
        ElementOptional opt = new ElementOptional(epb);
        getClause().addElement(opt);
    }

    /**
     * Add the contents of a where handler as an optional statement.
     *
     * @param whereHandler The where handler to use as the optional statement.
     */
    public void addOptional(WhereHandler whereHandler) {
        getClause().addElement(new ElementOptional(whereHandler.getClause()));
    }

    /**
     * Add an expression string as a filter.
     *
     * @param expression The expression string to add.
     * @throws ParseException If the expression can not be parsed.
     */
    public void addFilter(String expression) throws ParseException {
        getClause().addElement(new ElementFilter(parseExpr(expression)));
    }

    private Expr parseExpr(String expression) {
        Query query = new Query();
        query.setPrefixMapping(prefixHandler.getPrefixes());
        return ExprUtils.parse(query, expression, true);

    }

    /**
     * add an expression as a filter.
     *
     * @param expr The expression to add.
     */
    public void addFilter(Expr expr) {
        getClause().addElement(new ElementFilter(expr));
    }

    /**
     * Add a subquery to the where clause.
     *
     * @param subQuery The sub query to add.
     */
    public void addSubQuery(AbstractQueryBuilder<?> subQuery) {
        getClause().addElement(subQuery.asSubQuery());
    }

    /**
     * Add a union to the where clause.
     *
     * @param subQuery The subquery to add as the union.
     */
    public void addUnion(AbstractQueryBuilder<?> subQuery) {
        ElementUnion union = null;
        ElementGroup clause = getClause();
        // if the last element is a union make sure we add to it.
        if (!clause.isEmpty()) {
            Element lastElement = clause.getElements().get(clause.getElements().size() - 1);
            if (lastElement instanceof ElementUnion) {
                union = (ElementUnion) lastElement;
            } else {
                // clauses is not empty and is not a union so it is the left
                // side of the union.
                union = new ElementUnion();
                union.addElement(clause);
                whereClause = union;
            }
        } else {
            // add the union as the first element in the clause.
            union = new ElementUnion();
            clause.addElement(union);
        }
        // if there are projected vars then do a full blown subquery
        // otherwise just add the clause.
        if (subQuery instanceof SelectClause && ((SelectClause<?>) subQuery).getVars().size() > 0) {
            union.addElement(subQuery.asSubQuery());
        } else {
            prefixHandler.addPrefixes(subQuery.getPrologHandler().getPrefixes());
            union.addElement(subQuery.getWhereHandler().getClause());
        }

    }

    /**
     * Add a graph to the where clause.
     *
     * @param graph The name of the graph.
     * @param subQuery The where handler that defines the graph.
     */
    public void addGraph(Node graph, WhereHandler subQuery) {
        getClause().addElement(new ElementNamedGraph(graph, subQuery.getClause()));
    }

    /**
     * Add a binding to the where clause.
     *
     * @param expr The expression to bind.
     * @param var The variable to bind it to.
     */
    public void addBind(Expr expr, Var var) {
        getClause().addElement(new ElementBind(var, expr));
    }

    /**
     * Add a binding to the where clause.
     *
     * @param expression The expression to bind.
     * @param var The variable to bind it to.
     * @throws ParseException
     */
    public void addBind(String expression, Var var) throws ParseException {
        getClause().addElement(new ElementBind(var, parseExpr(expression)));
    }

    /**
     * replace the vars in the expressions with the nodes in the values map. Vars
     * not listed in the values map are not changed.
     *
     * Will return null if the whereClause is null.
     *
     * @param values the value map to use
     * @return A new Element instance with the values changed.
     */
    public WhereQuadHolder setVars(Map<Var, Node> values) {
        if (whereClause != null) {
            /* process when values are empty as rewriter handles Node_Variable to Var translation.
             *
             */
            ElementRewriter r = new ElementRewriter(values);
            whereClause.visit(r);
            whereClause = r.getResult();
        }
        return this;
    }

    @Override
    public QuadHolder setValues(Map<Var, Node> values) {
        setVars(values);
        return this;
    }

    /**
     * Create a list node from a list of objects as per RDF Collections.
     *
     * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#collections
     *
     * @param objs the list of objects for the list.
     * @return the first blank node in the list.
     */
    public Node list(Object... objs) {
        Node retval = NodeFactory.createBlankNode();
        Node lastObject = retval;
        for (int i = 0; i < objs.length; i++) {
            Node n = Converters.makeNode(objs[i], prefixHandler.getPrefixes());
            addWhere(new TriplePath(new Triple(lastObject, RDF.first.asNode(), n)));
            if (i + 1 < objs.length) {
                Node nextObject = NodeFactory.createBlankNode();
                addWhere(new TriplePath(new Triple(lastObject, RDF.rest.asNode(), nextObject)));
                lastObject = nextObject;
            } else {
                addWhere(new TriplePath(new Triple(lastObject, RDF.rest.asNode(), RDF.nil.asNode())));
            }

        }

        return retval;
    }

    /**
     * Add a minus operation to the where clause. The prefixes will be updated with
     * the prefixes from the abstract query builder.
     *
     * @param qb the abstract builder that defines the data to subtract.
     */
    public void addMinus(AbstractQueryBuilder<?> qb) {
        prefixHandler.addPrefixes(qb.getPrologHandler().getPrefixes());
        ElementGroup clause = getClause();
        ElementMinus minus = new ElementMinus(qb.getWhereHandler().getClause());
        clause.addElement(minus);
    }

    /**
     * @return Build the whereClause and return the element.
     */
    public Element build() {
        /*
         * cleanup union-of-one and other similar issues.
         */
        BuildElementVisitor visitor = new BuildElementVisitor();
        whereClause.visit(visitor);
        return whereClause;
    }
}
