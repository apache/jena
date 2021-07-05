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

package org.apache.jena.arq.querybuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.arq.querybuilder.clauses.PrologClause;
import org.apache.jena.arq.querybuilder.clauses.WhereClause;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.arq.querybuilder.updatebuilder.CollectionQuadHolder;
import org.apache.jena.arq.querybuilder.updatebuilder.ModelQuadHolder;
import org.apache.jena.arq.querybuilder.updatebuilder.PrefixHandler;
import org.apache.jena.arq.querybuilder.updatebuilder.QBQuadHolder;
import org.apache.jena.arq.querybuilder.updatebuilder.QuadCollectionHolder;
import org.apache.jena.arq.querybuilder.updatebuilder.QuadHolder;
import org.apache.jena.arq.querybuilder.updatebuilder.SingleQuadHolder;
import org.apache.jena.arq.querybuilder.updatebuilder.WhereQuadHolder;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Class to build update requests.
 *
 */
public class UpdateBuilder {

	private final PrefixHandler prefixHandler;
	private final WhereQuadHolder whereProcessor;
	private List<QuadHolder> inserts = new ArrayList<QuadHolder>();
	private List<QuadHolder> deletes = new ArrayList<QuadHolder>();
	private Map<Var, Node> values;
	private Node with;

	/**
	 * Creates an UpdateBuilder with an empty prefix mapping.
	 */
	public UpdateBuilder() {
		this.prefixHandler = new PrefixHandler();
		this.whereProcessor = new WhereQuadHolder(prefixHandler);
		this.values = new HashMap<Var, Node>();
		this.with = null;
	}

	/**
	 * Creates an UpdateBuilder with the prefixes defined in the prolog clause.
	 *  <b>May modify the
	 * contents of the prefix mapping in the prolog handler</b>
	 * 
	 * @param prologClause
	 *            the default prefixes for this builder.
	 */
	public UpdateBuilder(PrologClause<?> prologClause) {
		this(prologClause.getPrologHandler().getPrefixes());
	}

	/**
	 * Creates an UpdateBuilder with the specified PrefixMapping.
	 * <b>May modify the contents of the prefix mapping</b>
	 * 
	 * @param pMap
	 *            the prefix mapping to use.
	 */
	public UpdateBuilder(PrefixMapping pMap) {
		this.prefixHandler = new PrefixHandler(pMap);
		this.whereProcessor = new WhereQuadHolder(prefixHandler);
	}

	/**
	 * Convert a collection of QuadHolder to an iterator on Quads.
	 * @param holders the Collection of QuadHolder objects
	 * @return an iterator over the Quads.
	 */
	private ExtendedIterator<Quad> getQuads(Collection<QuadHolder> holders) {
		ExtendedIterator<Quad> result = NiceIterator.emptyIterator();
		for (QuadHolder holder : holders) {
			result = result.andThen(holder.setValues(values).getQuads());
		}
		return result;
	}

	/**
	 * Build the update.
	 * 
	 * <b>Note: the update does not include the prefix statements</b> use
	 * buildRequest() or appendTo() methods to include the prefix statements.
	 * 
	 * @return the update.
	 */
	public Update build() {

		if (deletes.isEmpty() && inserts.isEmpty()) {
			throw new IllegalStateException("At least one delete or insert must be specified");
		}

		if (whereProcessor.isEmpty()) {
			return buildNoWhere();
		}
		return buildWhere();
	}

	/**
	 * Build as an UpdateRequest with prefix mapping set.
	 * 
	 * @return a new UpdateRequest
	 */
	public UpdateRequest buildRequest() {
		UpdateRequest req = new UpdateRequest(build());
		req.setPrefixMapping(prefixHandler.getPrefixes());
		return req;
	}

	/**
	 * Appends the new Update to the UpdateRequest.
	 * 
	 * @param req
	 *            the UpdateRequest to append this Update to.
	 * @return the req parameter for chaining.
	 */
	public UpdateRequest appendTo(UpdateRequest req) {
		req.add(build());
		for (Map.Entry<String, String> entry : prefixHandler.getPrefixes().getNsPrefixMap().entrySet()) {
			req.setPrefix(entry.getKey(), entry.getValue());
		}
		return req;
	}

	// build updates without where clauses
	private Update buildNoWhere() {
		if (inserts.isEmpty()) {
			QuadDataAcc quadData = new QuadDataAcc(getQuads(deletes).mapWith(new Function<Quad, Quad>() {
				@Override
				public Quad apply(Quad arg0) {
					return check(arg0);
				}
			}).toList());
			return new UpdateDataDelete(quadData);
		}
		if (deletes.isEmpty()) {
			QuadDataAcc quadData = new QuadDataAcc(getQuads(inserts).mapWith(new Function<Quad, Quad>() {

				@Override
				public Quad apply(Quad t) {
					return check(t);
				}

			}).toList());
			return new UpdateDataInsert(quadData);
		}

		throw new IllegalStateException("Can not have both insert and delete without a where clause");
	}

	// build updates with where clauses
	private Update buildWhere() {

		UpdateModify retval = new UpdateModify();
		if (with != null)
		{
			Node graph = values.get(with);
			if (graph == null) {
				graph = with;
			}
			retval.setWithIRI(graph);
		}
		QuadAcc acc;
		Iterator<Quad> iter;

		if (!inserts.isEmpty()) {
			retval.setHasInsertClause(true);
			acc = retval.getInsertAcc();
			iter = getQuads(inserts);
			while (iter.hasNext()) {
				acc.addQuad(iter.next());
			}
		}
		if (!deletes.isEmpty()) {
			retval.setHasDeleteClause(true);
			acc = retval.getDeleteAcc();

			iter = getQuads(deletes);
			while (iter.hasNext()) {
				acc.addQuad(iter.next());
			}
		}
		
		retval.setElement(whereProcessor.setVars(values).build());
		
		return retval;

	}

	/**
	 * Make a triple path from the objects.
	 * 
	 * For subject, predicate and objects nodes
	 * <ul>
	 * <li>Will return Node.ANY if object is null.</li>
	 * <li>Will return the enclosed Node from a FrontsNode</li>
	 * <li>Will return the object if it is a Node.</li>
	 * <li>If the object is a String
	 * 	<ul>
	 * <li>For <code>predicate</code> only will attempt to parse as a path</li>
	 * <li>for subject, predicate and object will call NodeFactoryExtra.parseNode() 
	 * using the currently defined prefixes if the object is a String</li>
	 * </ul></li>
	 * <li>Will create a literal representation if the parseNode() fails or for
	 * any other object type.</li>
	 * </ul>
	 * 
	 * @param s The subject object
	 * @param p the predicate object
	 * @param o the object object.
	 * @return a TriplePath
	 */
	public TriplePath makeTriplePath(Object s, Object p, Object o) {
		final Object po = Converters.makeNodeOrPath( p, prefixHandler.getPrefixes() );
		if (po instanceof Path)
		{
			return new TriplePath(makeNode(s), (Path)po, makeNode(o));
		}
		return new TriplePath( new Triple( makeNode(s), (Node)po, makeNode(o)));
	}
	
	/**
	 * Convert the object to a node.
	 * 
	 * Shorthand for AbstractQueryBuilder.makeNode( o, prefixes )
	 * 
	 * @see AbstractQueryBuilder#makeNode(Object)
	 * 
	 * @param o
	 *            the object to convert to a node.
	 * @return the Node.
	 */
	public Node makeNode(Object o) {
		return Converters.makeNode(o, prefixHandler.getPrefixes());
	}

	/**
	 * Convert the object to a node.
	 * 
	 * Shorthand for AbstractQueryBuilder.makeVar( o )
	 * 
	 * @see Converters#makeVar(Object)
	 * 
	 * @param o
	 *            the object to convert to a var.
	 * @return the Var.
	 * @deprecated use {@link Converters#makeVar(Object)}
	 */
	@Deprecated
	public Var makeVar(Object o) {
		return Converters.makeVar(o);
	}

	/**
	 * Quote a string.
	 * 
	 * Shorthand for AbstractQueryBuilder.quote( s )
	 * 
	 * @see Converters#quoted(String)
	 * 
	 * @deprecated Use quoted()
	 * @param s
	 *            the string to quote.
	 * @return the quoted string.
	 * @deprecated use {@link Converters#quoted(String)}
	 */
	@Deprecated
	public String quote(String s) {
		return Converters.quoted(s);
	}

	/**
	 * Add a quad to the insert statement.
	 * 
	 * Arguments are converted to nodes using the makeNode() method.
	 * 
	 * @see #makeNode(Object)
	 * @param g
	 *            the graph
	 * @param s
	 *            the subject
	 * @param p
	 *            the predicate
	 * @param o
	 *            the object
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(Object g, Object s, Object p, Object o) {
		return addInsert(new Quad(makeNode(g), makeNode(s), makeNode(p), makeNode(o)));
	}

	/**
	 * Add a quad to the insert statement.
	 * 
	 * 
	 * @param quad
	 *            the quad to add.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(Quad quad) {
		inserts.add(new SingleQuadHolder(quad));
		return this;
	}

	/**
	 * Add a triple to the insert statement.
	 * 
	 * Arguments are converted to nodes using the makeNode() method.
	 * 
	 * @see #makeNode(Object)
	 * @param s
	 *            the subject
	 * @param p
	 *            the predicate
	 * @param o
	 *            the object
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(Object s, Object p, Object o) {
		addInsert(new Triple(makeNode(s), makeNode(p), makeNode(o)));
		return this;
	}

	/**
	 * Add a triple to the insert statement.
	 * 
	 * @param t
	 *            the triple to add.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(Triple t) {
		inserts.add(new SingleQuadHolder( t ));
		return this;
	}

	/**
	 * Add a triple in a specified graph to the insert statement.
	 * 
	 * The graph object is converted by a call to makeNode().
	 * 
	 * @see #makeNode(Object)
	 * @param g
	 *            the graph for the triple.
	 * @param t
	 *            the triple to add.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(Object g, Triple t) {
		Quad q = new Quad(makeNode(g), t);
		inserts.add(new SingleQuadHolder(q));
		return this;
	}
	
	/**
	 * Add all the statements in the model to the insert statement.
	 * Uses Quad.defaultGraphNodeGenerated as the graph name.
	 * 
	 * @param model The model to insert.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(Model model) {
		inserts.add(new ModelQuadHolder( model ));
		return this;
	}

	/**
	 * Add all the triples in the model to the insert statement.
	 * Uses Quad.defaultGraphNodeGenerated as the graph name.
	 * 
	 * @param collection The triples to insert.
	 * @return this builder for chaining.
	 * @see Quad#defaultGraphNodeGenerated
	 */
	public UpdateBuilder addInsert(Collection<Triple> collection) {
		inserts.add(new CollectionQuadHolder( collection ));
		return this;
	}
	
	/**
	 * Add all the quads in the collection to the insert statement.
	 * 
	 * @param collection The quads to insert.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsertQuads(Collection<Quad> collection) {
		inserts.add(new QuadCollectionHolder( collection ));
		return this;
	}
	
	/**
	 * Add all the triples to the insert statement.
	 * Uses Quad.defaultGraphNodeGenerated as the graph name.
	 * 
	 * @param iter The iterator of triples to insert.
	 * @return this builder for chaining.
	 * @see Quad#defaultGraphNodeGenerated
	 */
	public UpdateBuilder addInsert(Iterator<Triple> iter) {
		inserts.add(new CollectionQuadHolder( iter ));
		return this;
	}
	
	/**
	 * Add all the statements in the model a specified graph to the insert statement.
	 * 
	 * The graph object is converted by a call to makeNode().
	 * 
	 * @see #makeNode(Object)
	 * @param g
	 *            the graph for the triple.
	 * @param model
	 *            the model to add.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(Object g, Model model) {
		inserts.add( new ModelQuadHolder( makeNode(g), model));
		return this;
	}

	/**
	 * Add triples to the insert statement.
	 * 
	 * @param g the name of the graph to add the triples to.
	 * @param collection The triples to insert.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(Object g, Collection<Triple> collection) {
		inserts.add(new CollectionQuadHolder( makeNode(g), collection ));
		return this;
	}
	
	/**
	 * Add triples to the insert statement.
	 * @param  g the name of the  graph to add the triples to.
	 * @param iter The iterator of triples to insert.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(Object g, Iterator<Triple> iter) {
		inserts.add(new CollectionQuadHolder( makeNode(g), iter ));
		return this;
	}
	
	/**
	 * Add the statements from the where clause in the specified query builder
	 * to the insert statement.
 	 * Uses Quad.defaultGraphNodeGenerated as the graph name.
	 * @see #makeNode(Object)
	 * @see Quad#defaultGraphNodeGenerated
	 * @param queryBuilder
	 *            The query builder to extract the where clause from.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(AbstractQueryBuilder<?> queryBuilder) {
		inserts.add(new QBQuadHolder( queryBuilder));
		return this;
	}

	/**
	 * Add the statements from the where clause in the specified query builder
	 * to the insert statements for the specified graph.
	 * 
	 * The graph object is converted by a call to makeNode().
	 * 
	 * @see #makeNode(Object)
	 * @param graph
	 *            the graph to add the statements to.
	 * @param queryBuilder
	 *            The query builder to extract the where clause from.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addInsert(Object graph, AbstractQueryBuilder<?> queryBuilder) {
		inserts.add(new QBQuadHolder(makeNode(graph), queryBuilder));
		return this;
	}

	/**
	 * Add a quad to the delete statement.
	 * 
	 * Arguments are converted to nodes using the makeNode() method.
	 * 
	 * @see #makeNode(Object)
	 * @param g
	 *            the graph
	 * @param s
	 *            the subject
	 * @param p
	 *            the predicate
	 * @param o
	 *            the object
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addDelete(Object g, Object s, Object p, Object o) {
		return addDelete(new Quad(makeNode(g),
				makeNode(s),
				makeNode(p),
				makeNode(o)));
	}

	/**
	 * Add a quad to the delete statement.
	 * 
	 * @param quad
	 *            the quad to add.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addDelete(Quad quad) {
		deletes.add(new SingleQuadHolder(quad));
		return this;
	}
	
	/**
	 * Add all the quads collection to the delete statement.
	 * 
	 * @param collection The quads to insert.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addDeleteQuads(Collection<Quad> collection) {
		deletes.add(new QuadCollectionHolder( collection ));
		return this;
	}

	/**
	 * Add a triple to the delete statement.
	 * 
	 * Arguments are converted to nodes using the makeNode() method.
	 * 
	 * @see #makeNode(Object)
	 * @param s
	 *            the subject
	 * @param p
	 *            the predicate
	 * @param o
	 *            the object
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addDelete(Object s, Object p, Object o) {
		addDelete(new Triple(makeNode(s), makeNode(p), makeNode(o)));
		return this;
	}

	/**
	 * Add a triple to the delete statement.
	 * Uses Quad.defaultGraphNodeGenerated as the graph name.
	 * 
	 * @param t
	 *            the triple to add.
	 * @return this builder for chaining.
	 * @see Quad#defaultGraphNodeGenerated
	 */
	public UpdateBuilder addDelete(Triple t) {
		deletes.add(new SingleQuadHolder(t));
		return this;
	}

	/**
	 * Add a triple to the delete statement.
	 * 
	 * The graph object is converted by a call to makeNode().
	 * 
	 * @see #makeNode(Object)
	 * @param g
	 *            the graph for the triple.
	 * @param t
	 *            the triple to add.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addDelete(Object g, Triple t) {
		Quad q = new Quad(makeNode(g), t);
		deletes.add(new SingleQuadHolder(q));
		return this;
	}

	/**
	 * Add all the statements in the model to the delete statement.
	 * Uses Quad.defaultGraphNodeGenerated as the graph name.
	 * 
	 * @param model The model to insert.
	 * @return this builder for chaining.
	 * @see Quad#defaultGraphNodeGenerated
	 */
	public UpdateBuilder addDelete(Model model) {
		deletes.add(new ModelQuadHolder( model ));
		return this;
	}

	/**
	 * Add all triples to the delete statement.
	 * Uses Quad.defaultGraphNodeGenerated as the graph name.
	 * 
	 * @param collection The collection of triples to insert.
	 * @return this builder for chaining.
	 * @see Quad#defaultGraphNodeGenerated
	 */
	public UpdateBuilder addDelete(Collection<Triple> collection) {
		deletes.add(new CollectionQuadHolder( collection ));
		return this;
	}
	
	/**
	 * Add all the triples in the iterator to the delete statement.
	 * Uses Quad.defaultGraphNodeGenerated as the graph name.
	 * 
	 * @param iter The iterator of triples to insert.
	 * @return this builder for chaining.
	 * @see Quad#defaultGraphNodeGenerated
	 */
	public UpdateBuilder addDelete(Iterator<Triple> iter) {
		deletes.add(new CollectionQuadHolder( iter ));
		return this;
	}
	
	/**
	 * Add all the statements in the model a specified graph to the delete statement.
	 * 
	 * The graph object is converted by a call to makeNode().
	 * 
	 * @see #makeNode(Object)
	 * @param g
	 *            the graph for the triples.
	 * @param model
	 *            the model to add.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addDelete(Object g, Model model) {
		deletes.add( new ModelQuadHolder( makeNode(g), model));
		return this;
	}


	/**
	 * Add all the statements in the model to the delete statement.
	 * 
	 * @param g
	 *            the graph for the triples.
	 * @param collection The collection of triples to insert.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addDelete(Object g, Collection<Triple> collection) {
		deletes.add(new CollectionQuadHolder( makeNode(g), collection ));
		return this;
	}
	
	/**
	 * Add all the statements in the model to the delete statement.
	 * 
	 * @param g
	 *            the graph for the triples.
	 * @param iter The iterator of triples to insert.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addDelete(Object g, Iterator<Triple> iter) {
		deletes.add(new CollectionQuadHolder( makeNode(g), iter ));
		return this;
	}

	/**
	 * Add the statements from the where clause in the specified query builder
	 * to the delete statement.
	 * Uses Quad.defaultGraphNodeGenerated as the graph name.
	 * 
	 * @see #makeNode(Object)
	 * @see Quad#defaultGraphNodeGenerated
	 * @param queryBuilder
	 *            The query builder to extract the where clause from.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addDelete(AbstractQueryBuilder<?> queryBuilder) {
		deletes.add(new QBQuadHolder( queryBuilder));
		return this;
	}

	/**
	 * Add the statements from the where clause in the specified query builder
	 * to the delete statements for the specified graph.
	 * 
	 * The graph object is converted by a call to makeNode().
	 * 
	 * @see #makeNode(Object)
	 * @param graph
	 *            the graph to add the statements to.
	 * @param queryBuilder
	 *            The query builder to extract the where clause from.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder addDelete(Object graph, AbstractQueryBuilder<?> queryBuilder) {
		deletes.add(new QBQuadHolder(makeNode(graph), queryBuilder));
		return this;
	}

	/**
	 * Add the prefix to the prefix mapping.
	 * 
	 * @param pfx
	 *            the prefix to add.
	 * @param uri
	 *            the uri for the prefix.
	 * @return this builder for chaining
	 */
	public UpdateBuilder addPrefix(String pfx, Resource uri) {
		return addPrefix(pfx, uri.getURI());
	}

	/**
	 * Add the prefix to the prefix mapping.
	 * 
	 * @param pfx
	 *            the prefix to add.
	 * @param uri
	 *            the uri for the prefix.
	 * @return this builder for chaining
	 */
	public UpdateBuilder addPrefix(String pfx, Node uri) {
		return addPrefix(pfx, uri.getURI());
	}

	/**
	 * Add the prefix to the prefix mapping.
	 * 
	 * @param pfx
	 *            the prefix to add.
	 * @param uri
	 *            the uri for the prefix.
	 * @return this builder for chaining
	 */
	public UpdateBuilder addPrefix(String pfx, String uri) {
		prefixHandler.addPrefix(pfx, uri);
		return this;
	}

	/**
	 * Add the prefixes to the prefix mapping.
	 * 
	 * @param prefixes
	 *            the prefixes to add.
	 * @return this builder for chaining
	 */

	public UpdateBuilder addPrefixes(Map<String, String> prefixes) {
		prefixHandler.addPrefixes(prefixes);
		return this;
	}
	
	/**
	 * Add the prefixes to the prefix mapping.
	 * 
	 * @param prefixes
	 *            the prefix mapping to add.
	 * @return this builder for chaining
	 */

	public UpdateBuilder addPrefixes(PrefixMapping prefixes) {
		prefixHandler.addPrefixes(prefixes);
		return this;
	}

	/**
	 * Get an ExprFactory that uses the prefixes from this builder.
	 * 
	 * @return the ExpressionFactory.
	 */
	public ExprFactory getExprFactory() {
		return prefixHandler.getExprFactory();
	}

	/**
	 * Set a variable replacement. During build all instances of var in the
	 * query will be replaced with value. If value is null the replacement is
	 * cleared.
	 * 
	 * @param var
	 *            The variable to replace
	 * @param value
	 *            The value to replace it with or null to remove the
	 *            replacement.
	 */
	public void setVar(Var var, Node value) {
		if (value == null) {
			values.remove(var);
		} else {
			values.put(var, value);
		}
	}

	/**
	 * Set a variable replacement. During build all instances of var in the
	 * query will be replaced with value. If value is null the replacement is
	 * cleared.
	 * 
	 * See {@link #makeVar} for conversion of the var param. See
	 * {@link #makeNode} for conversion of the value param.
	 * 
	 * @param var
	 *            The variable to replace.
	 * @param value
	 *            The value to replace it with or null to remove the
	 *            replacement.
	 */
	public void setVar(Object var, Object value) {
		if (value == null) {
			setVar(Converters.makeVar(var), null);
		} else {
			setVar(Converters.makeVar(var), makeNode(value));
		}
	}

	private Quad check(Quad q) {
		if (Var.isVar(q.getGraph()))
			throw new QueryParseException("Variables not permitted in data quad", -1, -1);
		if (Var.isVar(q.getSubject()) || Var.isVar(q.getPredicate()) || Var.isVar(q.getObject()))
			throw new QueryParseException("Variables not permitted in data quad", -1, -1);
		if (q.getSubject().isLiteral())
			throw new QueryParseException("Literals not allowed as subjects in data", -1, -1);
		return q;
	}

	/**
	 * Add all where attributes from the Where Handler argument.
	 * 
	 * @param whereHandler
	 *            The Where Handler to copy from.
	 */
	public UpdateBuilder addAll(WhereHandler whereHandler) {
		whereProcessor.addAll(whereHandler);
		return this;
	}

	/**
	 * Add the triple path to the where clause
	 * 
	 * @param t
	 *            The triple path to add.
	 * @throws IllegalArgumentException
	 *             If the triple path is not a valid triple path for a where
	 *             clause.
	 */
	public UpdateBuilder addWhere(TriplePath t) throws IllegalArgumentException {
		whereProcessor.addWhere(t);
		return this;
	}

	/**
	 * Add the WhereClause 
	 * 
	 * @param whereClause
	 * @throws IllegalArgumentException
	 *             If the triple path is not a valid triple path for a where
	 *             clause.
	 */
	public UpdateBuilder addWhere(WhereClause<?> whereClause) throws IllegalArgumentException {
		whereProcessor.addAll(whereClause.getWhereHandler());
		return this;
	}
	/**
	 * Add an optional triple to the where clause
	 * 
	 * @param t
	 *            The triple path to add.
	 * @return The Builder for chaining.
	 * @throws IllegalArgumentException
	 *             If the triple is not a valid triple for a where clause.
	 */
	public UpdateBuilder addOptional(TriplePath t) throws IllegalArgumentException {
		whereProcessor.addOptional(t);
		return this;
	}

	/**
	 * Add the contents of a where handler as an optional statement.
	 * 
	 * @param whereHandler
	 *            The where handler to use as the optional statement.
	 */
	public UpdateBuilder addOptional(WhereHandler whereHandler) {
		whereProcessor.addOptional(whereHandler);
		return this;
	}

	/**
	 * Add an expression string as a filter.
	 * 
	 * @param expression
	 *            The expression string to add.
	 * @return The Builder for chaining.
	 * @throws ParseException
	 *             If the expression can not be parsed.
	 */
	public UpdateBuilder addFilter(String expression) throws ParseException {
		whereProcessor.addFilter(expression);
		return this;
	}

	/**
	 * Add a subquery to the where clause.
	 * 
	 * @param subQuery
	 *            The sub query to add.
	 * @return The Builder for chaining.
	 */
	public UpdateBuilder addSubQuery(AbstractQueryBuilder<?> subQuery) {
		whereProcessor.addSubQuery(subQuery);
		return this;
	}

	/**
	 * Add a union to the where clause.
	 * 
	 * @param subQuery
	 *            The subquery to add as the union.
	 * @return The Builder for chaining.
	 */
	public UpdateBuilder addUnion(AbstractQueryBuilder<?> subQuery) {
		whereProcessor.addUnion(subQuery);
		return this;
	}

	/**
	 * Add a graph to the where clause.
	 * 
	 * @param graph
	 *            The name of the graph.
	 * @param subQuery
	 *            The where handler that defines the graph.
	 */
	public UpdateBuilder addGraph(Node graph, WhereHandler subQuery) {
		whereProcessor.addGraph(graph, subQuery);
		return this;
	}

	/**
	 * Add a binding to the where clause.
	 * 
	 * @param expr
	 *            The expression to bind.
	 * @param var
	 *            The variable to bind it to.
	 */
	public UpdateBuilder addBind(Expr expr, Var var) {
		whereProcessor.addBind(expr, var);
		return this;
	}

	/**
	 * Add a binding to the where clause.
	 * 
	 * @param expression
	 *            The expression to bind.
	 * @param var
	 *            The variable to bind it to.
	 * @throws ParseException
	 */
	public UpdateBuilder addBind(String expression, Var var) throws ParseException {
		whereProcessor.addBind(expression, var);
		return this;
	}

	/**
	 * Create a list node from a list of objects as per RDF Collections.
	 * 
	 * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#collections
	 * 
	 * See {@link AbstractQueryBuilder#makeNode} for conversion of the param
	 * values.
	 * <p>
	 * usage:
	 * <ul>
	 * <li>list( param1, param2, param3, ... )</li>
	 * <li>addWhere( list( param1, param2, param3, ... ), p, o )</li>
	 * <li>addOptional( list( param1, param2, param3, ... ), p, o )</li>
	 * </ul>
	 * </p>
	 * 
	 * @param objs
	 *            the list of objects for the list.
	 * @return the first blank node in the list.
	 */
	public Node list(Object... objs) {
		Node retval = NodeFactory.createBlankNode();
		Node lastObject = retval;
		for (int i = 0; i < objs.length; i++) {
			Node n = makeNode(objs[i]);
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
	 * Adds a triple to the where clause.
	 * 
	 * @param t
	 *            The triple path to add
	 * @return The Builder for chaining.
	 */
	public UpdateBuilder addWhere(Triple t) {
		return addWhere(new TriplePath(t));
	}

	/**
	 * Adds a triple to the where clause.
	 * 
	 * @param t
	 *            The triple to add
	 * @return The Builder for chaining.
	 */
	public UpdateBuilder addWhere(FrontsTriple t) {
		return addWhere(t.asTriple());
	}

	/**
	 * Adds a triple or triple path to the where clause.
	 * 
	 * See {@link AbstractQueryBuilder#makeTriplePath} for conversion of the
	 * param values.
	 * 
	 * @param s
	 *            The subject.
	 * @param p
	 *            The predicate.
	 * @param o
	 *            The object.
	 * @return The Builder for chaining.
	 */
	public UpdateBuilder addWhere(Object s, Object p, Object o) {
		return addWhere(makeTriplePath(s, p, o));
	}

	/**
	 * Adds an optional triple to the where clause.
	 * 
	 * @param t
	 *            The triple to add
	 * @return The Builder for chaining.
	 */
	public UpdateBuilder addOptional(Triple t) {
		return addOptional(new TriplePath(t));
	}

	/**
	 * Adds an optional triple as to the where clause.
	 * 
	 * @param t
	 *            The triple to add
	 * @return The Builder for chaining.
	 */
	public UpdateBuilder addOptional(FrontsTriple t) {
		return addOptional(t.asTriple());
	}

	/**
	 * Adds an optional triple or triple path to the where clause.
	 * 
	 * See {@link AbstractQueryBuilder#makeTriplePath} for conversion of the
	 * param values.
	 * 
	 * @param s
	 *            The subject.
	 * @param p
	 *            The predicate.
	 * @param o
	 *            The object.
	 * @return The Builder for chaining.
	 */
	public UpdateBuilder addOptional(Object s, Object p, Object o) {
		return addOptional(makeTriplePath( s, p, o ));
	}

	/**
	 * Adds an optional group pattern to the where clause.
	 * 
	 * @param t
	 *            The select builder to add as an optional pattern
	 * @return The Builder for chaining.
	 */
	public UpdateBuilder addOptional(AbstractQueryBuilder<?> t) {
		whereProcessor.addOptional(t.getWhereHandler());
		return this;
	}

	/**
	 * Adds a filter to the where clause
	 * 
	 * Use ExprFactory or NodeValue static or the AbstractQueryBuilder.makeExpr
	 * methods to create the expression.
	 * 
	 * @see ExprFactory
	 * @see org.apache.jena.sparql.expr.NodeValue
	 * @see AbstractQueryBuilder#makeExpr(String)
	 * 
	 * @param expression
	 *            the expression to evaluate for the filter.
	 * @return @return The Builder for chaining.
	 */
	public UpdateBuilder addFilter(Expr expression) {
		whereProcessor.addFilter(expression);
		return this;
	}

	/**
	 * Add a graph statement to the query as per
	 * http://www.w3.org/TR/2013/REC-sparql11
	 * -query-20130321/#rGraphGraphPattern.
	 * 
	 * See {@link AbstractQueryBuilder#makeNode} for conversion of the graph
	 * param.
	 * 
	 * @param graph
	 *            The iri or variable identifying the graph.
	 * @param subQuery
	 *            The graph to add.
	 * @return This builder for chaining.
	 */
	public UpdateBuilder addGraph(Object graph, AbstractQueryBuilder<?> subQuery) {
		whereProcessor.addGraph(makeNode(graph), subQuery.getWhereHandler());
		return this;
	}

	/**
	 * Add a bind statement to the query *
	 * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rGraphGraphPattern.
	 * 
	 * @param expression
	 *            The expression to bind to the var.
	 * @param var
	 *            The variable to bind to.
	 * @return This builder for chaining.
	 */
	public UpdateBuilder addBind(Expr expression, Object var) {
		whereProcessor.addBind(expression, makeVar(var));
		return this;
	}

	/**
	 * Add a bind statement to the query
	 * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rGraphGraphPattern.
	 * 
	 * @param expression
	 *            The expression to bind to the var.
	 * @param var
	 *            The variable to bind to.
	 * @return This builder for chaining.
	 * @throws ParseException
	 */
	public UpdateBuilder addBind(String expression, Object var) throws ParseException {
		whereProcessor.addBind(expression, makeVar(var));
		return this;
	}

	/**
	 * Add a minus clause to the query.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rMinusGraphPattern
	 * 
	 * @param t
	 *            The select builder to add as a minus pattern
	 * @return this builder for chaining
	 */
	public UpdateBuilder addMinus(AbstractQueryBuilder<?> t) {
		whereProcessor.addMinus(t);
		return this;
	}

	/**
	 * Specify the graph for all inserts and deletes.
	 * 
	 * 
	 * @see Quad#defaultGraphNodeGenerated
	 * @param iri
	 *            the IRI for the graph to use.
	 * @return this builder for chaining.
	 */
	public UpdateBuilder with(Object iri) {
		if (iri == null) {
			with = null;
		}
		Node n = makeNode(iri);
		if (n.isLiteral()) {
			throw new IllegalArgumentException(String.format("IRI '%s' must not be a literal", iri));
		}
		with = n;
		return this;
	}

	/**
	 * Create a DeleteWhere from the where clause.
	 * @return a DeleteWhere update.
	 */
	public UpdateDeleteWhere buildDeleteWhere()
	{
		QuadAcc quadAcc = new QuadAcc( whereProcessor.getQuads().toList() );
		return new UpdateDeleteWhere( quadAcc );
	}
	
	/**
	 * Create a DeleteWhere from the where clause.
	 * @param queryBuilder the query builder to extract the where clause from.
	 * @return a DeleteWhere update.
	 */
	public UpdateDeleteWhere buildDeleteWhere( AbstractQueryBuilder<?> queryBuilder)
	{	
		QuadAcc quadAcc = new QuadAcc( new QBQuadHolder( queryBuilder ).getQuads().toList() );
		return new UpdateDeleteWhere( quadAcc );
	}
}
