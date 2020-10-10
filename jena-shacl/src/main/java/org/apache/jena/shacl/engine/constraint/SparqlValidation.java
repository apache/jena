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

package org.apache.jena.shacl.engine.constraint;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.shacl.engine.Parameter;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.ModelUtils;

/** The SPARQL validator algorithms. */
/*package*/ class SparqlValidation {

    private static final boolean USE_QueryTransformOps = false;

    public static void validate(ValidationContext vCxt, Graph data, Shape shape,
                                Node focusNode, Path path, Node valueNode,
                                Query query, Multimap<Parameter, Node> parameterMap,
                                String violationTemplate, Constraint reportConstraint) {
        // Two sub-cases:
        //    Syntax rule: https://www.w3.org/TR/shacl/#syntax-rule-multiple-parameters
        //    If there are >1 parameters, each must be single valued.
        // so:
        // Multimap, one parameter, multiple values => conjunction of each, with one report.
        // Multimap, any number of parameters, single values => single validation with one report.

        if ( parameterMap != null ) {
            if ( parameterMap.keySet().size() == 1 && parameterMap.size() > 1 ) {
                for ( Entry<Parameter, Node> e : parameterMap.entries()) {
                    Map<Parameter, Node> pmap = Collections.singletonMap(e.getKey(), e.getValue());
                    boolean b = validateMap(vCxt, data, shape, focusNode, path, valueNode, query, pmap, violationTemplate, reportConstraint);
                    if ( ! b )
                        // Validation error - return early.
                        return;
                }
                return;
            }
        }
        // Convert to map.
        Map<Parameter, Node> pmap = flatten(parameterMap);
        /*boolean b =*/
        validateMap(vCxt, data, shape, focusNode, path, valueNode, query, pmap, violationTemplate, reportConstraint);
    }

    private static Map<Parameter, Node> flatten(Multimap<Parameter, Node> parameterMap) {
        if ( parameterMap == null )
            return null;
        Map<Parameter, Node> pmap = new HashMap<>(parameterMap.size());
        parameterMap.forEach((p,v)->{
            pmap.put(p, v);
        });
        return pmap;
    }

    /** return true if the validation is "conforms" */
    private static boolean validateMap(ValidationContext vCxt, Graph data, Shape shape,
                                        Node focusNode, Path path, Node valueNode,
                                        Query _query, Map<Parameter, Node> parameterMap,
                                        String violationTemplate, Constraint reportConstraint) {
        Model model = ModelFactory.createModelForGraph(data);
        QueryExecution qExec;

        Query query = _query;
        // If path is not a simple link, rewrite the query.
        if ( path != null && !(path instanceof P_Link ) )
            query = QueryTransformOps.transform(query, new ElementTransformPath(SparqlConstraint.varPath, path));

        if ( USE_QueryTransformOps ) {
            // Done with QueryTransformOps.transform
            Map<Var, Node> substitutions = parameterMapToSyntaxSubstitutions(parameterMap, focusNode, path);
            if ( query.isAskType() )
                addSubstition(substitutions, "value", valueNode);
            Query query2 = QueryTransformOps.transform(query, substitutions);
            qExec = QueryExecutionFactory.create(query2, model);
        } else {
            // Done with pre-binding.
            QuerySolutionMap qsm = parameterMapToPreBinding(parameterMap, focusNode, path, model);
            if ( query.isAskType() )
                qsm.add("value", ModelUtils.convertGraphNodeToRDFNode(valueNode, model));
            qExec = QueryExecutionFactory.create(query, model, qsm);
        }

        // ASK validator.
        if ( qExec.getQuery().isAskType() ) {
            boolean b = qExec.execAsk();
            if ( ! b ) {
                String msg = ( violationTemplate == null )
                    ? "SPARQL ASK constraint for "+ShLib.displayStr(valueNode)+" returns false"
                    : substitute(violationTemplate, parameterMap, focusNode, path, valueNode);
                vCxt.reportEntry(msg, shape, focusNode, path, valueNode, reportConstraint);
            }
            return b;
        }

        ResultSet rs = qExec.execSelect();
        if ( ! rs.hasNext() )
            return true;

        while(rs.hasNext()) {
            Binding row = rs.nextBinding();
            Node value = row.get(SparqlConstraint.varValue);
            if ( value == null )
                value = valueNode;

            String msg;
            if ( violationTemplate == null ) {
                if ( value != null )
                    msg = "SPARQL SELECT constraint for "+ShLib.displayStr(valueNode)+" returns "+ShLib.displayStr(value);
                else
                    msg = "SPARQL SELECT constraint for "+ShLib.displayStr(valueNode)+" returns row "+row;
            } else {
                msg = substitute(violationTemplate, row);
            }

            Path rPath = path;
            if ( rPath == null ) {
                Node qPath = row.get(SparqlConstraint.varPath);
                if ( qPath != null )
                    rPath = PathFactory.pathLink(qPath);
            }
            vCxt.reportEntry(msg, shape, focusNode, rPath, value, reportConstraint);
        }
        return false;
    }

    /** Result message: SELECT substitute */
    private static String substitute(String violationTemplate, Binding row) {
        String x = violationTemplate;
        Iterator<Var> iter = row.vars();
        while(iter.hasNext()) {
            Var var = iter.next();
            x = substit(x, var.getVarName(), row.get(var));
        }
        return x;
    }

    /** Result message: ASK substitute */
    private static String substitute(String violationTemplate, Map<Parameter, Node> parameterMap, Node focusNode, Path path, Node valueNode) {
        String x = violationTemplate;
        for ( Entry<Parameter, Node> e : parameterMap.entrySet() ) {
            x = substit(x, e.getKey().getSparqlName(), e.getValue());
        }
        return x;
    }

    /** Substitution */
    private static String substit(String x, String name, Node value) {
        try {
            String vn = "\\{[?$]"+Matcher.quoteReplacement(name)+"\\}";
            String val = strQuoted(value);
            return x.replaceAll(vn, val);
        } catch (RuntimeException ex) {
            Log.warn(SparqlValidation.class, "Failed to substitute into string for name="+name+" value="+value);
            return x;
        }
    }

    /** regex-safe string */
    private static String strQuoted(Node node) {
        String x =
        node.isLiteral() ?node.getLiteralLexicalForm()
        : NodeFmtLib.str(node);
        x = Matcher.quoteReplacement(x);
        return x;
    }

    private static Map<Var, Node> parameterMapToSyntaxSubstitutions(Map<Parameter, Node> parameterMap, Node thisNode, Path path) {
        Map<Var, Node> substitions = parametersToMap(parameterMap, thisNode);
        if ( path != null ) {
            addSubstition(substitions, "PATH", ShaclPaths.pathNode(path));
        }
        return substitions;
    }

    private static Map<Var, Node> parametersToMap(Map<Parameter, Node> parameterMap, Node thisNode) {
        Map<Var, Node> substitions = new HashMap<>();
        if ( parameterMap != null ) {
            parameterMap.forEach((p,n)-> addSubstition(substitions, p.getSparqlName(), n));
        }
        addSubstition(substitions, "this", thisNode);
        return substitions;
    }

    private static QuerySolutionMap parameterMapToPreBinding(Map<Parameter, Node> parameterMap, Node thisNode, Path path, Model model) {
        QuerySolutionMap qsm = new QuerySolutionMap();
        if ( parameterMap != null ) {
            parameterMap.forEach((p,n)->
                qsm.add(p.getSparqlName(), ModelUtils.convertGraphNodeToRDFNode(n, model)));
        }
        qsm.add("this", ModelUtils.convertGraphNodeToRDFNode(thisNode, model));
        if ( path != null ) {
            Node pn = ShaclPaths.pathNode(path);
            // If 'path' can not be translated into a substituted form, then ignore
            // PATH. This means that is the path is not a simple link, it could only be
            // done by textually substitution of the SPARQL query string.
            if ( pn != null ) {
                RDFNode z = ModelUtils.convertGraphNodeToRDFNode(pn, model);
                qsm.add("PATH", z);
            }
        }
        return qsm;
    }

    private static void addSubstition(Map<Var, Node> substitions, String sparqlName, Node n) {
        substitions.put(Var.alloc(sparqlName), n);
    }

    /** ${var} in a string. */
    private static String messageTemplate(String message, Map<Parameter, Node> parameterMap, Node thisNode, Path path) {
        Map<Var, Node> substitions = parametersToMap(parameterMap, thisNode);
        Pattern pattern = Pattern.compile("{[$?][^{}]+}");
        if ( path != null )
            // PATH is special.
            substitions.put(Var.alloc("PATH"), NodeFactory.createLiteral(ShaclPaths.pathToString(path)));
        return subsitute(message, substitions);
    }

    private static Pattern pattern = Pattern.compile("(\\{[\\$\\?][^{}]+\\})");
    // String substitution.
    private static String subsitute(String string, Map<Var, Node> substitions) {
        StringBuilder sb = new StringBuilder();
        Matcher m = pattern.matcher(string);
        int prev = 0 ;
        while(m.find()) {
            int i1 = m.start();
            int i2 = m.end();
            String var = m.group();
            String varName = var.substring(2, i2-i1-1);
            sb.append(string.substring(prev,i1));
            Var v = Var.alloc(varName);
            Node n = substitions.get(v);
            if ( n == null )
                sb.append(var);
            else {
                String z = NodeFmtLib.displayStr(n);
                sb.append(z);
            }
            prev = i2;
        }
        sb.append(string.substring(prev));
        return sb.toString();
    }

    /** Rewrite a path block, replacing a variable in the predicate slot of a triple with a path. */
    private static class ElementTransformPath extends ElementTransformCopyBase {
        private final Var var;
        private final Path path;

        ElementTransformPath(Var varPath, Path path) {
            this.var = varPath;
            this.path = path;
        }

        @Override
        public Element transform(ElementPathBlock el) {
            ElementPathBlock el2 = new ElementPathBlock();
            boolean changed = false ;
            PathBlock pathBlock = el.getPattern();
            List<TriplePath> x = pathBlock.getList();
            for(TriplePath tp : x ) {
                if ( ! tp.isTriple() ) {
                    el2.addTriple(tp);
                    continue;
                }
                Triple t = tp.asTriple();
                if ( !( var.equals(t.getPredicate()) ) ) {
                    el2.addTriple(tp);
                    continue;
                }
                TriplePath tp2 = new TriplePath(t.getSubject(), path, t.getObject());
                el2.addTriple(tp2);
                changed = true ;
            }
            return changed ? el2 : el ;
        }
    }
}
