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

package com.hp.hpl.jena.query;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.iri.IRI;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * <p>
 * A Parameterized SPARQL String is a SPARQL query/update into which values may
 * be injected.
 * </p>
 * <h3>Injecting Values</h3>
 * <p>
 * Values may be injected in several ways:
 * </p>
 * <ul>
 * <li>By treating a variable in the SPARQL string as a parameter</li>
 * <li>Using JDBC style positional parameters</li>
 * <li>Appending values directly to the command text being built</li>
 * </ul>
 * <h4>Variable Parameters</h3>
 * <p>
 * Any variable in the command may have a value injected to it, injecting a
 * value replaces all usages of that variable in the command i.e. substitutes
 * the variable for a constant, injection is done by textual substitution.
 * </p> <h4>Positional Parameters</h4>
 * <p>
 * You can use JDBC style positional parameters if you prefer, a JDBC style
 * parameter is a single {@code ?} followed by whitespace or certain punctuation
 * characters (currently {@code ; , .}). Positional parameters have a unique
 * index which reflects the order in which they appear in the string. Positional
 * parameters use a zero based index.
 * </p>
 * <h4>Buffer Usage</h3> </p> Additionally you may use this purely as a
 * {@link StringBuffer} replacement for creating queries since it provides a
 * large variety of convenience methods for appending things either as-is or as
 * nodes (which causes appropriate formatting to be applied). </p>
 * <h3>Intended Usage</h3>
 * <p>
 * The intended usage of this is where using a {@link QuerySolutionMap} as
 * initial bindings is either inappropriate or not possible e.g.
 * </p>
 * <ul>
 * <li>Generating query/update strings in code without lots of error prone and
 * messy string concatenation</li>
 * <li>Preparing a query/update for remote execution</li>
 * <li>Where you do not want to simply say some variable should have a certain
 * value but rather wish to insert constants into the query/update in place of
 * variables</li>
 * <li>Defending against SPARQL injection when creating a query/update using
 * some external input, see SPARQL Injection notes for limitations.</li>
 * <li>Provide a more convenient way to prepend common prefixes to your query</li>
 * </ul>
 * <p>
 * This class is useful for preparing both queries and updates hence the generic
 * name as it provides programmatic ways to replace variables in the query with
 * constants and to add prefix and base declarations. A {@link Query} or
 * {@link UpdateRequest} can be created using the {@link #asQuery()} and
 * {@link #asUpdate()} methods assuming the command an instance represents is
 * actually valid as a query/update.
 * </p>
 * <h3>Warnings</h3>
 * <ol>
 * <li>Note that this class does not in any way check that your command is
 * syntactically correct until such time as you try and parse it as a
 * {@link Query} or {@link UpdateRequest}.</li>
 * <li>Also note that injection is done purely based on textual replacement, it
 * does not understand or respect variable scope in any way. For example if your
 * command text contains sub queries you should ensure that variables within the
 * sub query which you don't want replaced have distinct names from those in the
 * outer query you do want replaced (or vice versa)</li>
 * </ol>
 * <h3>SPARQL Injection Notes</h3>
 * <p>
 * While this class was in part designed to prevent SPARQL injection it is by no
 * means foolproof because it works purely at the textual level. The current
 * version of the code addresses some possible attack vectors that the
 * developers have identified but we do not claim to be sufficiently devious to
 * have thought of and prevented every possible attack vector.
 * </p>
 * <p>
 * Therefore we <strong>strongly</strong> recommend that users concerned about
 * SPARQL Injection attacks perform their own validation on provided parameters
 * and test their use of this class themselves prior to its use in any security
 * conscious deployment. We also recommend that users do not use easily
 * guess-able variable names for their parameters as these can allow a chained
 * injection attack though generally speaking the code should prevent these.
 * </p>
 */
public class ParameterizedSparqlString implements PrefixMapping {

    private Model model = ModelFactory.createDefaultModel();

    private StringBuilder cmd = new StringBuilder();
    private String baseUri;
    private Map<String, Node> params = new HashMap<>();
    private Map<Integer, Node> positionalParams = new HashMap<>();
    private PrefixMapping prefixes;

    /**
     * Creates a new parameterized string
     * 
     * @param command
     *            Raw Command Text
     * @param map
     *            Initial Parameters to inject
     * @param base
     *            Base URI
     * @param prefixes
     *            Prefix Mapping
     */
    public ParameterizedSparqlString(String command, QuerySolutionMap map, String base, PrefixMapping prefixes) {
        if (command != null)
            this.cmd.append(command);
        this.setParams(map);
        this.baseUri = (base != null && !base.equals("") ? base : null);
        this.prefixes = new PrefixMappingImpl();
        if (prefixes != null)
            this.prefixes.setNsPrefixes(prefixes);
    }

    /**
     * Creates a new parameterized string
     * 
     * @param command
     *            Raw Command Text
     * @param map
     *            Initial Parameters to inject
     * @param base
     *            Base URI
     */
    public ParameterizedSparqlString(String command, QuerySolutionMap map, String base) {
        this(command, map, base, null);
    }

    /**
     * Creates a new parameterized string
     * 
     * @param command
     *            Raw Command Text
     * @param map
     *            Initial Parameters to inject
     * @param prefixes
     *            Prefix Mapping
     */
    public ParameterizedSparqlString(String command, QuerySolutionMap map, PrefixMapping prefixes) {
        this(command, map, null, prefixes);
    }

    /**
     * Creates a new parameterized string
     * 
     * @param command
     *            Raw Command Text
     * @param map
     *            Initial Parameters to inject
     */
    public ParameterizedSparqlString(String command, QuerySolutionMap map) {
        this(command, map, null, null);
    }

    /**
     * Creates a new parameterized string
     * 
     * @param command
     *            Raw Command Text
     * @param base
     *            Base URI
     * @param prefixes
     *            Prefix Mapping
     */
    public ParameterizedSparqlString(String command, String base, PrefixMapping prefixes) {
        this(command, null, base, prefixes);
    }

    /**
     * Creates a new parameterized string
     * 
     * @param command
     *            Raw Command Text
     * @param prefixes
     *            Prefix Mapping
     */
    public ParameterizedSparqlString(String command, PrefixMapping prefixes) {
        this(command, null, null, prefixes);
    }

    /**
     * Creates a new parameterized string
     * 
     * @param command
     *            Raw Command Text
     * @param base
     *            Base URI
     */
    public ParameterizedSparqlString(String command, String base) {
        this(command, null, base, null);
    }

    /**
     * Creates a new parameterized string
     * 
     * @param command
     *            Raw Command Text
     */
    public ParameterizedSparqlString(String command) {
        this(command, null, null, null);
    }

    /**
     * Creates a new parameterized string
     * 
     * @param map
     *            Initial Parameters to inject
     * @param prefixes
     *            Prefix Mapping
     */
    public ParameterizedSparqlString(QuerySolutionMap map, PrefixMapping prefixes) {
        this(null, map, null, prefixes);
    }

    /**
     * Creates a new parameterized string
     * 
     * @param map
     *            Initial Parameters to inject
     */
    public ParameterizedSparqlString(QuerySolutionMap map) {
        this(null, map, null, null);
    }

    /**
     * Creates a new parameterized string
     * 
     * @param prefixes
     *            Prefix Mapping
     */
    public ParameterizedSparqlString(PrefixMapping prefixes) {
        this(null, null, null, prefixes);
    }

    /**
     * Creates a new parameterized string with an empty command text
     */
    public ParameterizedSparqlString() {
        this("", null, null, null);
    }

    /**
     * Sets the command text, overwriting any existing command text. If you want
     * to append to the command text use one of the {@link #append(String)},
     * {@link #appendIri(String)}, {@link #appendLiteral(String)} or
     * {@link #appendNode(Node)} methods instead
     * 
     * @param command
     *            Command Text
     */
    public void setCommandText(String command) {
        this.cmd = new StringBuilder();
        this.cmd.append(command);
    }

    /**
     * Appends some text as-is to the existing command text, to ensure correct
     * formatting when used as a constant consider using the
     * {@link #appendLiteral(String)} or {@link #appendIri(String)} method as
     * appropriate
     * 
     * @param text
     *            Text to append
     */
    public void append(String text) {
        this.cmd.append(text);
    }

    /**
     * Appends a character as-is to the existing command text, to ensure correct
     * formatting when used as a constant consider using one of the
     * {@code appendLiteral()} methods
     * 
     * @param c
     *            Character to append
     */
    public void append(char c) {
        this.cmd.append(c);
    }

    /**
     * Appends a boolean as-is to the existing command text, to ensure correct
     * formatting when used as a constant consider using the
     * {@link #appendLiteral(boolean)} method
     * 
     * @param b
     *            Boolean to append
     */
    public void append(boolean b) {
        this.cmd.append(b);
    }

    /**
     * Appends a double as-is to the existing command text, to ensure correct
     * formatting when used as a constant consider using the
     * {@link #appendLiteral(double)} method
     * 
     * @param d
     *            Double to append
     */
    public void append(double d) {
        this.cmd.append(d);
    }

    /**
     * Appends a float as-is to the existing command text, to ensure correct
     * formatting when used as a constant consider using the
     * {@link #appendLiteral(float)} method
     * 
     * @param f
     *            Float to append
     */
    public void append(float f) {
        this.cmd.append(f);
    }

    /**
     * Appends an integer as-is to the existing command text, to ensure correct
     * formatting when used as a constant consider using the
     * {@link #appendLiteral(int)} method
     * 
     * @param i
     *            Integer to append
     */
    public void append(int i) {
        this.cmd.append(i);
    }

    /**
     * Appends a long as-is to the existing command text, to ensure correct
     * formatting when used as a constant consider using the
     * {@link #appendLiteral(long)} method
     * 
     * @param l
     *            Long to append
     */
    public void append(long l) {
        this.cmd.append(l);
    }

    /**
     * Appends an object as-is to the existing command text, to ensure correct
     * formatting when used as a constant consider converting into a more
     * specific type and using the appropriate {@code appendLiteral()},
     * {@code appendIri()} or {@code appendNode} methods
     * 
     * @param obj
     *            Object to append
     */
    public void append(Object obj) {
        this.cmd.append(obj);
    }

    /**
     * Appends a Node to the command text as a constant using appropriate
     * formatting
     * 
     * @param n
     *            Node to append
     */
    public void appendNode(Node n) {
        SerializationContext context = new SerializationContext(this.prefixes);
        context.setBaseIRI(this.baseUri);
        this.cmd.append(this.stringForNode(n, context));
    }

    /**
     * Appends a Node to the command text as a constant using appropriate
     * formatting
     * 
     * @param n
     *            Node to append
     */
    public void appendNode(RDFNode n) {
        this.appendNode(n.asNode());
    }

    /**
     * Appends a URI to the command text as a constant using appropriate
     * formatting
     * 
     * @param uri
     *            URI to append
     */
    public void appendIri(String uri) {
        this.appendNode(NodeFactory.createURI(uri));
    }

    /**
     * Appends an IRI to the command text as a constant using appropriate
     * formatting
     * 
     * @param iri
     *            IRI to append
     */
    public void appendIri(IRI iri) {
        this.appendNode(NodeFactory.createURI(iri.toString()));
    }

    /**
     * Appends a simple literal as a constant using appropriate formatting
     * 
     * @param value
     *            Lexical Value
     */
    public void appendLiteral(String value) {
        this.appendNode(NodeFactoryExtra.createLiteralNode(value, null, null));
    }

    /**
     * Appends a literal with a lexical value and language to the command text
     * as a constant using appropriate formatting
     * 
     * @param value
     *            Lexical Value
     * @param lang
     *            Language
     */
    public void appendLiteral(String value, String lang) {
        this.appendNode(NodeFactoryExtra.createLiteralNode(value, lang, null));
    }

    /**
     * Appends a Typed Literal to the command text as a constant using
     * appropriate formatting
     * 
     * @param value
     *            Lexical Value
     * @param datatype
     *            Datatype
     */
    public void appendLiteral(String value, RDFDatatype datatype) {
        this.appendNode(NodeFactoryExtra.createLiteralNode(value, null, datatype.getURI()));
    }

    /**
     * Appends a boolean to the command text as a constant using appropriate
     * formatting
     * 
     * @param b
     *            Boolean to append
     */
    public void appendLiteral(boolean b) {
        this.appendNode(this.model.createTypedLiteral(b));
    }

    /**
     * Appends an integer to the command text as a constant using appropriate
     * formatting
     * 
     * @param i
     *            Integer to append
     */
    public void appendLiteral(int i) {
        this.appendNode(NodeFactoryExtra.intToNode(i));
    }

    /**
     * Appends a long to the command text as a constant using appropriate
     * formatting
     * 
     * @param l
     *            Long to append
     */
    public void appendLiteral(long l) {
        this.appendNode(NodeFactoryExtra.intToNode(l));
    }

    /**
     * Appends a float to the command text as a constant using appropriate
     * formatting
     * 
     * @param f
     *            Float to append
     */
    public void appendLiteral(float f) {
        this.appendNode(this.model.createTypedLiteral(f));
    }

    /**
     * Appends a double to the command text as a constant using appropriate
     * formatting
     * 
     * @param d
     *            Double to append
     */
    public void appendLiteral(double d) {
        this.appendNode(this.model.createTypedLiteral(d));
    }

    /**
     * Appends a date time to the command text as a constant using appropriate
     * formatting
     * 
     * @param dt
     *            Date Time to append
     */
    public void appendLiteral(Calendar dt) {
        this.appendNode(this.model.createTypedLiteral(dt));
    }

    /**
     * Gets the basic Command Text
     * <p>
     * <strong>Note:</strong> This will not reflect any injected parameters, to
     * see the command with injected parameters invoke the {@link #toString()}
     * method
     * </p>
     * 
     * @return Command Text
     */
    public String getCommandText() {
        return this.cmd.toString();
    }

    /**
     * Sets the Base URI which will be prepended to the query/update
     * 
     * @param base
     *            Base URI
     */
    public void setBaseUri(String base) {
        this.baseUri = base;
    }

    /**
     * Gets the Base URI which will be prepended to a query
     * 
     * @return Base URI
     */
    public String getBaseUri() {
        return this.baseUri;
    }

    /**
     * Helper method which does the validation of the parameters
     * 
     * @param n
     *            Node
     */
    protected void validateParameterValue(Node n) {
        if (n.isURI()) {
            if (n.getURI().contains(">"))
                throw new ARQException("Value for the parameter contains a SPARQL injection risk");
        }
    }

    /**
     * Sets the Parameters
     * 
     * @param map
     *            Parameters
     */
    public void setParams(QuerySolutionMap map) {
        if (map != null) {
            Iterator<String> iter = map.varNames();
            while (iter.hasNext()) {
                String var = iter.next();
                this.setParam(var, map.get(var).asNode());
            }
        }
    }

    /**
     * Sets a Positional Parameter
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(int)} for the given variable
     * </p>
     * 
     * @param index
     *            Positional Index
     * @param n
     *            Node
     */
    public void setParam(int index, Node n) {
        if (index < 0)
            throw new IndexOutOfBoundsException();
        if (n != null) {
            this.validateParameterValue(n);
            this.positionalParams.put(index, n);
        } else {
            this.positionalParams.remove(index);
        }
    }

    /**
     * Sets a variable parameter
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(String)} for the given variable
     * </p>
     * 
     * @param var
     *            Variable
     * @param n
     *            Value
     * 
     */
    public void setParam(String var, Node n) {
        if (var == null)
            throw new IllegalArgumentException("var cannot be null");
        if (var.startsWith("?") || var.startsWith("$"))
            var = var.substring(1);
        if (n != null) {
            this.validateParameterValue(n);
            this.params.put(var, n);
        } else {
            this.params.remove(var);
        }
    }

    /**
     * Sets a positional parameter
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(String)} for the given variable
     * </p>
     * 
     * @param index
     *            Positional Index
     * @param n
     *            Node
     */
    public void setParam(int index, RDFNode n) {
        this.setParam(index, n.asNode());
    }

    /**
     * Sets a variable parameter
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(String)} for the given variable
     * </p>
     * 
     * @param var
     *            Variable
     * @param n
     *            Value
     */
    public void setParam(String var, RDFNode n) {
        this.setParam(var, n.asNode());
    }

    /**
     * Sets a positional parameter to an IRI
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(int)} for the given index
     * </p>
     * 
     * @param index
     *            Positional Index
     * @param iri
     *            IRI
     */
    public void setIri(int index, String iri) {
        this.setParam(index, NodeFactory.createURI(iri));
    }

    /**
     * Sets a variable parameter to an IRI
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(String)} for the given variable
     * </p>
     * 
     * @param var
     *            Variable
     * @param iri
     *            IRI
     */
    public void setIri(String var, String iri) {
        this.setParam(var, NodeFactory.createURI(iri));
    }

    /**
     * Sets a positional parameter to an IRI
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(int)} for the given index
     * </p>
     * 
     * @param index
     *            Positional Index
     * @param iri
     *            IRI
     */
    public void setIri(int index, IRI iri) {
        this.setIri(index, iri.toString());
    }

    /**
     * Sets a variable parameter to an IRI
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(String)} for the given variable
     * </p>
     * 
     * @param var
     *            Variable
     * @param iri
     *            IRI
     */
    public void setIri(String var, IRI iri) {
        this.setIri(var, iri.toString());
    }

    /**
     * Sets a positional parameter to an IRI
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(int)} for the given index
     * </p>
     * 
     * @param index
     *            Positional Index
     * @param url
     *            URL
     */
    public void setIri(int index, URL url) {
        this.setIri(index, url.toString());
    }

    /**
     * Sets a variable parameter to an IRI
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(String)} for the given variable
     * </p>
     * 
     * @param var
     *            Variable
     * @param url
     *            URL used as IRI
     * 
     */
    public void setIri(String var, URL url) {
        this.setIri(var, url.toString());
    }

    /**
     * Sets a positional parameter to a Literal
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(int)} for the given index
     * </p>
     * 
     * @param index
     *            Positional Index
     * @param lit
     *            Value
     * 
     */
    public void setLiteral(int index, Literal lit) {
        this.setParam(index, lit.asNode());
    }

    /**
     * Sets a variable parameter to a Literal
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(String)} for the given variable
     * </p>
     * 
     * @param var
     *            Variable
     * @param lit
     *            Value
     * 
     */
    public void setLiteral(String var, Literal lit) {
        this.setParam(var, lit.asNode());
    }

    /**
     * Sets a positional parameter to a literal
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(int)} for the given index
     * </p>
     * 
     * @param index
     *            Positional Index
     * @param value
     *            Lexical Value
     * 
     */
    public void setLiteral(int index, String value) {
        this.setParam(index, NodeFactoryExtra.createLiteralNode(value, null, null));
    }

    /**
     * Sets a variable parameter to a literal
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(String)} for the given variable
     * </p>
     * 
     * @param var
     *            Variable
     * @param value
     *            Lexical Value
     * 
     */
    public void setLiteral(String var, String value) {
        this.setParam(var, NodeFactoryExtra.createLiteralNode(value, null, null));
    }

    /**
     * Sets a positional parameter to a literal with a language
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(int)} for the given index
     * </p>
     * 
     * @param index
     *            Positional index
     * @param value
     *            Lexical Value
     * @param lang
     *            Language
     * 
     */
    public void setLiteral(int index, String value, String lang) {
        this.setParam(index, NodeFactoryExtra.createLiteralNode(value, lang, null));
    }

    /**
     * Sets a variable parameter to a literal with a language
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(String)} for the given variable
     * </p>
     * 
     * @param var
     *            Variable
     * @param value
     *            Lexical Value
     * @param lang
     *            Language
     * 
     */
    public void setLiteral(String var, String value, String lang) {
        this.setParam(var, NodeFactoryExtra.createLiteralNode(value, lang, null));
    }

    /**
     * Sets a positional arameter to a typed literal
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(int)} for the given index
     * </p>
     * 
     * @param index
     *            Positional Index
     * @param value
     *            Lexical Value
     * @param datatype
     *            Datatype
     * 
     */
    public void setLiteral(int index, String value, RDFDatatype datatype) {
        this.setParam(index, this.model.createTypedLiteral(value, datatype));
    }

    /**
     * Sets a variable parameter to a typed literal
     * <p>
     * Setting a parameter to null is equivalent to calling
     * {@link #clearParam(String)} for the given variable
     * </p>
     * 
     * @param var
     *            Variable
     * @param value
     *            Lexical Value
     * @param datatype
     *            Datatype
     * 
     */
    public void setLiteral(String var, String value, RDFDatatype datatype) {
        this.setParam(var, this.model.createTypedLiteral(value, datatype));
    }

    /**
     * Sets a positional parameter to a boolean literal
     * 
     * @param index
     *            Positional Index
     * @param value
     *            boolean
     */
    public void setLiteral(int index, boolean value) {
        this.setParam(index, this.model.createTypedLiteral(value));
    }

    /**
     * Sets a variable parameter to a boolean literal
     * 
     * @param var
     *            Variable
     * @param value
     *            boolean
     */
    public void setLiteral(String var, boolean value) {
        this.setParam(var, this.model.createTypedLiteral(value));
    }

    /**
     * Sets a positional parameter to an integer literal
     * 
     * @param index
     *            Positional Index
     * @param i
     *            Integer Value
     */
    public void setLiteral(int index, int i) {
        this.setParam(index, NodeFactoryExtra.intToNode(i));
    }

    /**
     * Sets a variable parameter to an integer literal
     * 
     * @param var
     *            Variable
     * @param i
     *            Integer Value
     */
    public void setLiteral(String var, int i) {
        this.setParam(var, NodeFactoryExtra.intToNode(i));
    }

    /**
     * Sets a positional parameter to an integer literal
     * 
     * @param index
     *            Positional Index
     * @param l
     *            Integer Value
     */
    public void setLiteral(int index, long l) {
        this.setParam(index, NodeFactoryExtra.intToNode(l));
    }

    /**
     * Sets a variable parameter to an integer literal
     * 
     * @param var
     *            Variable
     * @param l
     *            Integer Value
     */
    public void setLiteral(String var, long l) {
        this.setParam(var, NodeFactoryExtra.intToNode(l));
    }

    /**
     * Sets a positional parameter to a float literal
     * 
     * @param index
     *            Positional Index
     * @param f
     *            Float value
     */
    public void setLiteral(int index, float f) {
        this.setParam(index, NodeFactoryExtra.floatToNode(f));
    }

    /**
     * Sets a variable parameter to a float literal
     * 
     * @param var
     *            Variable
     * @param f
     *            Float value
     */
    public void setLiteral(String var, float f) {
        this.setParam(var, NodeFactoryExtra.floatToNode(f));
    }

    /**
     * Sets a positional parameter to a double literal
     * 
     * @param index
     *            Positional Index
     * @param d
     *            Double value
     */
    public void setLiteral(int index, double d) {
        this.setParam(index, this.model.createTypedLiteral(d));
    }

    /**
     * Sets a variable parameter to a double literal
     * 
     * @param var
     *            Variable
     * @param d
     *            Double value
     */
    public void setLiteral(String var, double d) {
        this.setParam(var, this.model.createTypedLiteral(d));
    }

    /**
     * Sets a positional parameter to a date time literal
     * 
     * @param index
     *            Positional Index
     * @param dt
     *            Date Time value
     */
    public void setLiteral(int index, Calendar dt) {
        this.setParam(index, this.model.createTypedLiteral(dt));
    }

    /**
     * Sets a variable parameter to a date time literal
     * 
     * @param var
     *            Variable
     * @param dt
     *            Date Time value
     */
    public void setLiteral(String var, Calendar dt) {
        this.setParam(var, this.model.createTypedLiteral(dt));
    }

    /**
     * Gets the current value for a variable parameter
     * 
     * @param var
     *            Variable
     * @return Current value or null if not set
     */
    public Node getParam(String var) {
        return this.params.get(var);
    }

    /**
     * Gets the current value for a positional parameter
     * 
     * @param index
     *            Positional Index
     * @return Current value or null if not set
     */
    public Node getParam(int index) {
        return this.positionalParams.get(index);
    }

    /**
     * Gets the variable names which are currently treated as variable
     * parameters (i.e. have values set for them)
     * 
     * @return Iterator of variable names
     */
    @Deprecated
    public Iterator<String> getVars() {
        return this.params.keySet().iterator();
    }

    /**
     * Gets the map of currently set variable parameters, this will be an
     * unmodifiable map
     * 
     * @return Map of variable names and values
     */
    public Map<String, Node> getVariableParameters() {
        return Collections.unmodifiableMap(this.params);
    }

    /**
     * Gets the map of currently set positional parameters, this will be an
     * unmodifiable map
     * 
     * @return Map of positional indexes and values
     */
    public Map<Integer, Node> getPositionalParameters() {
        return Collections.unmodifiableMap(this.positionalParams);
    }

    // TODO: Detecting eligible variable parameters
    // public Iterator<String> getEligibleVariableParameters() {
    //
    // }

    /**
     * Gets the eligible positional parameters i.e. detected positional
     * parameters that may be set in the command string as it currently stands
     * 
     * @return Iterator of eligible positional parameters
     */
    public Iterator<Integer> getEligiblePositionalParameters() {
        Pattern p = Pattern.compile("(\\?)[\\s;,.]");
        List<Integer> positions = new ArrayList<>();
        int index = 0;
        Matcher matcher = p.matcher(this.cmd.toString());
        while (matcher.find()) {
            positions.add(index);
            index++;
        }
        return positions.iterator();
    }

    /**
     * Clears the value for a variable parameter so the given variable will not
     * have a value injected
     * 
     * @param var
     *            Variable
     */
    public void clearParam(String var) {
        this.params.remove(var);
    }

    /**
     * Clears the value for a positional parameter
     * 
     * @param index
     *            Positional Index
     */
    public void clearParam(int index) {
        this.positionalParams.remove(index);
    }

    /**
     * Clears all values for both variable and positional parameters
     */
    public void clearParams() {
        this.params.clear();
        this.positionalParams.clear();
    }

    /**
     * Helper method which checks whether it is safe to inject to a variable
     * parameter the given value
     * 
     * @param command
     *            Current command string
     * @param var
     *            Variable
     * @param n
     *            Value to inject
     * @throws ARQException
     *             Thrown if not safe to inject, error message will describe why
     *             it is unsafe to inject
     */
    protected void validateSafeToInject(String command, String var, Node n) throws ARQException {
        // Looks for the known injection attack vectors and throws an error if
        // any are encountered

        // A ?var surrounded by " or ' where the variable is a literal is an
        // attack vector
        Pattern p = Pattern.compile("\"[?$]" + var + "\"|'[?$]" + var + "'");

        if (p.matcher(command).find() && n.isLiteral()) {
            throw new ARQException(
                    "Command string is vunerable to injection attack, variable ?"
                            + var
                            + " appears surrounded directly by quotes and is bound to a literal which provides a SPARQL injection attack vector");
        }

        // Parse out delimiter info
        DelimiterInfo delims = this.findDelimiters(command);

        // Check each occurrence of the variable for safety
        p = Pattern.compile("([?$]" + var + ")([^\\w]|$)");
        Matcher matcher = p.matcher(command);
        while (matcher.find()) {
            MatchResult posMatch = matcher.toMatchResult();

            if (n.isLiteral()) {
                if (delims.isInsideLiteral(posMatch.start(1), posMatch.end(1))) {
                    throw new ARQException(
                            "Command string is vunerable to injection attack, variable ?"
                                    + var
                                    + " appears inside of a literal and is bound to a literal which provides a SPARQL injection attack vector");
                }
            }
        }
    }

    /**
     * Helper method which checks whether it is safe to inject to a positional
     * parameter the given value
     * 
     * @param command
     *            Current command string
     * @param index
     *            Positional parameter index
     * @param position
     *            Position within the command string at which the positional
     *            parameter occurs
     * @param n
     *            Value to inject
     * @throws ARQException
     *             Thrown if not safe to inject, error message will describe why
     *             it is unsafe to inject
     */
    protected void validateSafeToInject(String command, int index, int position, Node n) throws ARQException {
        // Parse out delimiter info
        DelimiterInfo delims = this.findDelimiters(command);

        // Check each occurrence of the variable for safety
        if (n.isLiteral()) {
            if (delims.isInsideLiteral(position, position)) {
                throw new ARQException(
                        "Command string is vunerable to injection attack, a positional paramter (index "
                                + index
                                + ") appears inside of a literal and is bound to a literal which provides a SPARQL injection attack vector");
            }
        }
    }

    /**
     * Helper method which does light parsing on the command string to find the
     * position of all relevant delimiters
     * 
     * @param command
     *            Command String
     * @return DelimiterInfo
     */
    protected final DelimiterInfo findDelimiters(String command) {
        DelimiterInfo delims = new DelimiterInfo();
        delims.parseFrom(command);
        return delims;
    }

    protected final String stringForNode(Node n, SerializationContext context) {
        String str = FmtUtils.stringForNode(n, context);
        if (n.isLiteral() && str.contains("'")) {
            // Should escape ' to avoid a possible injection vulnerability
            str = str.replace("'", "\\'");
        }
        return str;
    }

    /**
     * <p>
     * This method is where the actual work happens, the original command text
     * is always preserved and we just generated a temporary command string by
     * prepending the defined Base URI and namespace prefixes at the start of
     * the command and injecting the set parameters into a copy of that base
     * command string and return the resulting command.
     * </p>
     * <p>
     * This class makes no guarantees about the validity of the returned string
     * for use as a SPARQL Query or Update, for example if a variable parameter
     * was injected which was mentioned in the SELECT variables list you'd have
     * a syntax error when you try to parse the query. If you run into issues
     * like this try using a mixture of variable and positional parameters.
     * </p>
     * 
     * @throws ARQException
     *             May be thrown if the code detects a SPARQL Injection
     *             vulnerability because of the interaction of the command
     *             string and the injected variables
     */
    @Override
    public String toString() {
        String command = this.cmd.toString();
        Pattern p;

        // Go ahead and inject Variable Parameters
        SerializationContext context = new SerializationContext(this.prefixes);
        context.setBaseIRI(this.baseUri);
        for (String var : this.params.keySet()) {
            Node n = this.params.get(var);
            if (n == null) {
                continue;
            }
            this.validateSafeToInject(command, var, n);

            p = Pattern.compile("([?$]" + var + ")([^\\w]|$)");
            command = p.matcher(command).replaceAll(Matcher.quoteReplacement(this.stringForNode(n, context)) + "$2");
        }

        // Then inject Positional Parameters
        // To do this we need to find the ? we will replace
        p = Pattern.compile("(\\?)[\\s;,.]");
        int index = -1;
        int adj = 0;
        Matcher matcher = p.matcher(command);
        while (matcher.find()) {
            index++;
            MatchResult posMatch = matcher.toMatchResult();

            Node n = this.positionalParams.get(index);
            if (n == null)
                continue;
            this.validateSafeToInject(command, index, posMatch.start(1) + adj, n);

            String nodeStr = this.stringForNode(n, context);
            command = command.substring(0, posMatch.start() + adj) + nodeStr
                    + command.substring(posMatch.start() + adj + 1);
            // Because we are using a matcher over the string state prior to
            // starting replacements we need to
            // track the offset adjustments to make
            adj += nodeStr.length() - 1;
        }

        // Build the final command string
        StringBuilder finalCmd = new StringBuilder();

        // Add BASE declaration
        if (this.baseUri != null) {
            finalCmd.append("BASE ");
            finalCmd.append(FmtUtils.stringForURI(this.baseUri, null, null));
            finalCmd.append('\n');
        }

        // Then pre-pend prefixes

        for (String prefix : this.prefixes.getNsPrefixMap().keySet()) {
            finalCmd.append("PREFIX ");
            finalCmd.append(prefix);
            finalCmd.append(": ");
            finalCmd.append(FmtUtils.stringForURI(this.prefixes.getNsPrefixURI(prefix), null, null));
            finalCmd.append('\n');
        }

        finalCmd.append(command);
        return finalCmd.toString();
    }

    /**
     * Attempts to take the command text with parameters injected from the
     * {@link #toString()} method and parse it as a {@link Query}
     * 
     * @return Query if the command text is a valid SPARQL query
     * @exception QueryException
     *                Thrown if the command text does not parse
     */
    public Query asQuery() throws QueryException {
        return QueryFactory.create(this.toString());
    }

    /**
     * Attempts to take the command text with parameters injected from the
     * {@link #toString()} method and parse it as a {@link UpdateRequest}
     * 
     * @return Update if the command text is a valid SPARQL Update request
     *         (one/more update commands)
     */
    public UpdateRequest asUpdate() {
        return UpdateFactory.create(this.toString());
    }

    /**
     * Makes a full copy of this parameterized string
     * 
     * @return Copy of the string
     */
    public ParameterizedSparqlString copy() {
        return this.copy(true, true, true);
    }

    /**
     * Makes a copy of the command text, base URI and prefix mapping and
     * optionally copies parameter values
     * 
     * @param copyParams
     *            Whether to copy parameters
     * @return Copy of the string
     */
    public ParameterizedSparqlString copy(boolean copyParams) {
        return this.copy(copyParams, true, true);
    }

    /**
     * Makes a copy of the command text and optionally copies other aspects
     * 
     * @param copyParams
     *            Whether to copy parameters
     * @param copyBase
     *            Whether to copy the Base URI
     * @param copyPrefixes
     *            Whether to copy the prefix mappings
     * @return Copy of the string
     */
    public ParameterizedSparqlString copy(boolean copyParams, boolean copyBase, boolean copyPrefixes) {
        ParameterizedSparqlString copy = new ParameterizedSparqlString(this.cmd.toString(), null,
                (copyBase ? this.baseUri : null), (copyPrefixes ? this.prefixes : null));
        if (copyParams) {
            Iterator<String> vars = this.getVars();
            while (vars.hasNext()) {
                String var = vars.next();
                copy.setParam(var, this.getParam(var));
            }
            for (Entry<Integer, Node> entry : this.positionalParams.entrySet()) {
                copy.setParam(entry.getKey(), entry.getValue());
            }
        }
        return copy;
    }

    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri) {
        return this.prefixes.setNsPrefix(prefix, uri);
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix) {
        return this.prefixes.removeNsPrefix(prefix);
    }

    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping other) {
        return this.prefixes.setNsPrefixes(other);
    }

    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> map) {
        return this.prefixes.setNsPrefixes(map);
    }

    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping map) {
        return this.prefixes.withDefaultMappings(map);
    }

    @Override
    public String getNsPrefixURI(String prefix) {
        return this.prefixes.getNsPrefixURI(prefix);
    }

    @Override
    public String getNsURIPrefix(String uri) {
        return this.prefixes.getNsURIPrefix(uri);
    }

    @Override
    public Map<String, String> getNsPrefixMap() {
        return this.prefixes.getNsPrefixMap();
    }

    @Override
    public String expandPrefix(String prefixed) {
        return this.prefixes.expandPrefix(prefixed);
    }

    @Override
    public String shortForm(String uri) {
        return this.prefixes.shortForm(uri);
    }

    @Override
    public String qnameFor(String uri) {
        return this.prefixes.qnameFor(uri);
    }

    @Override
    public PrefixMapping lock() {
        return this.prefixes.lock();
    }

    @Override
    public boolean samePrefixMappingAs(PrefixMapping other) {
        return this.prefixes.samePrefixMappingAs(other);
    }

    /**
     * Represents information about delimiters in a string
     * 
     */
    private class DelimiterInfo {
        private List<Pair<Integer, String>> starts = new ArrayList<>();
        private Map<Integer, Integer> stops = new HashMap<>();

        /**
         * Parse delimiters from a string, discards any previously parsed
         * information
         * 
         * @param command
         *            Command string
         */
        public void parseFrom(String command) {
            this.starts.clear();
            this.stops.clear();

            char[] cs = command.toCharArray();
            for (int i = 0; i < cs.length; i++) {
                switch (cs[i]) {
                case '"':
                    // Start of a Literal
                    // Is it a long literal?
                    if (i < cs.length - 2 && cs[i + 1] == '"' && cs[i + 2] == '"') {
                        this.addStart(i, "\"\"\"");
                        for (int j = i + 3; j < cs.length - 2; j++) {
                            if (cs[j] == '"' && cs[j + 1] == '"' && cs[j + 2] == '"') {
                                this.addStop(i, j + 2);
                                i = j + 2;
                            }
                        }
                        // Was unterminated
                    } else {
                        // Normal literal, scan till we see a " which is not
                        // preceded by a \
                        this.addStart(i, "\"");
                        for (int j = i + 1; j < cs.length; j++) {
                            if (cs[j] == '"' && cs[j - 1] != '\\') {
                                this.addStop(i, j);
                                i = j;
                                continue;
                            }
                        }
                        // Was unterminated
                    }
                    break;
                case '<':
                    // Start of a URI
                    this.addStart(i, "<");
                    for (int j = i + 1; j < cs.length; j++) {
                        if (cs[j] == '>' && cs[j - 1] != '\\') {
                            this.addStop(i, j);
                            i = j;
                            continue;
                        }
                    }
                    // Was unterminated
                    break;
                case '\'':
                    // Start of alternative literal form
                    // Start of a Literal
                    // Is it a long literal?
                    if (i < cs.length - 2 && cs[i + 1] == '\'' && cs[i + 2] == '\'') {
                        this.addStart(i, "'''");
                        for (int j = i + 3; j < cs.length - 2; j++) {
                            if (cs[j] == '\'' && cs[j + 1] == '\'' && cs[j + 2] == '\'') {
                                this.addStop(i, j + 2);
                                i = j + 2;
                            }
                        }
                        // Was unterminated
                    } else {
                        // Normal literal, scan till we see a ' which is not
                        // preceded by a \
                        this.addStart(i, "'");
                        for (int j = i + 1; j < cs.length; j++) {
                            if (cs[j] == '\'' && cs[j - 1] != '\\') {
                                this.addStop(i, j);
                                i = j;
                                continue;
                            }
                        }
                        // Was unterminated
                    }
                    break;
                case '#':
                    // Start of a comment
                    // Scan to next newline
                    this.addStart(i, "#");
                    for (int j = i + 1; j < cs.length; j++) {
                        if (cs[j] == '\n' || cs[j] == '\r') {
                            this.addStop(i, j);
                            i = j;
                            continue;
                        }
                    }
                    this.addStop(i, cs.length - 1);
                    break;
                case '\n':
                case '\r':
                case '.':
                case ',':
                case ';':
                case '(':
                case ')':
                case '{':
                case '}':
                case '[':
                case ']':
                    // Treat various punctuation as delimiters
                    this.addStart(i, new String(new char[] { cs[i] }));
                    this.addStop(i, i);
                    break;
                }
            }
        }

        public void addStart(int index, String delim) {
            this.starts.add(new Pair<>(index, delim));
        }

        public void addStop(int start, int stop) {
            this.stops.put(start, stop);
        }

        public Pair<Integer, String> findBefore(int index) {
            Pair<Integer, String> found = null;
            for (Pair<Integer, String> pair : this.starts) {
                if (pair.getLeft() < index)
                    found = pair;
                if (pair.getLeft() >= index)
                    break;
            }
            return found;
        }

        public Pair<Integer, String> findAfter(int index) {
            for (Pair<Integer, String> pair : this.starts) {
                if (pair.getLeft() > index)
                    return pair;
            }
            return null;
        }

        public boolean isInsideLiteral(int start, int stop) {
            Pair<Integer, String> pair = this.findBefore(start);
            if (pair == null)
                return false;
            if (pair.getRight().equals("\"")) {
                Integer nearestStop = this.stops.get(pair.getLeft());
                if (nearestStop == null)
                    return true; // Inside unterminated literal
                return (nearestStop > stop); // May be inside a literal
            } else {
                // Not inside a literal
                return false;
            }
        }

        public boolean isInsideAltLiteral(int start, int stop) {
            Pair<Integer, String> pair = this.findBefore(start);
            if (pair == null)
                return false;
            if (pair.getRight().equals("'")) {
                Integer nearestStop = this.stops.get(pair.getLeft());
                if (nearestStop == null)
                    return true; // Inside unterminated literal
                return (nearestStop > stop); // May be inside a literal
            } else {
                // Not inside a literal
                return false;
            }
        }

        public boolean isBetweenLiterals(int start, int stop) {
            Pair<Integer, String> pairBefore = this.findBefore(start);
            if (pairBefore == null)
                return false;
            if (pairBefore.getRight().equals("\"")) {
                Integer stopBefore = this.stops.get(pairBefore.getLeft());
                if (stopBefore == null)
                    return false; // Inside unterminated literal

                // We occur after a literal, is there a subsequent literal?
                Pair<Integer, String> pairAfter = this.findAfter(stop);
                return pairAfter != null && pairAfter.getRight().equals("\"");
            } else {
                // Previous deliminator is not that of a literal
                return false;
            }
        }

    }
}
