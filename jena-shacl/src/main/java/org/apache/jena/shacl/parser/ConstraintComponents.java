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

package org.apache.jena.shacl.parser;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.jena.shacl.lib.ShLib.displayStr;

import java.util.*;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.other.G;
import org.apache.jena.shacl.engine.Parameter;
import org.apache.jena.shacl.engine.constraint.ConstraintComponentSPARQL;
import org.apache.jena.shacl.engine.constraint.SparqlComponent;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.vocabulary.SHACL;

public class ConstraintComponents {
    // 6.2.1 Parameter Declarations (sh:parameter)
    // 6.2.2 Label Templates (sh:labelTemplate)

    // 6.1 An Example SPARQL-based Constraint Component
    // Constraint components provide instructions to validation engines on how to
    // identify and validate constraints within a shape.
    //
    // In general, if a shape S has
    // a value for a property p, and there is a constraint component C that specifies
    // p as a parameter, and S has values for all mandatory parameters of C, then the
    // set of these parameter values (including the optional parameters) declare a
    // constraint and the validation engine uses a suitable validator from C to
    // perform the validation of this constraint.
    //
    // In the example above,
    // sh:PatternConstraintComponent declares the mandatory parameter sh:pattern, the
    // optional parameter sh:flags, and a validator that can be used to perform
    // validation against either node shapes or property shapes.

    // 6.3 Validation with SPARQL-based Constraint Components

    /*package*/ MultiValuedMap<Node, SparqlComponent> paramPathToComponents = MultiMapUtils.newListValuedHashMap();
    /*package*/ Set<Parameter> parameters = new HashSet<>();

    /*package*/ ConstraintComponents() {}
    /*package*/ boolean hasParameters() { return parameters.isEmpty(); }

    /** Stage 1 : find all Constraint Components */
    public static ConstraintComponents parseSparqlConstraintComponents(Graph shapesGraph) {
        ConstraintComponents x = new ConstraintComponents();
        // SHACL.SPARQLConstraintComponent is not a subclass of SHACL.ConstraintComponent
        // SHACL.SPARQLConstraintComponent is an instance of SHACL.ConstraintComponent.
        // Need to process by actual property present.
        G.allNodesOfTypeRDFS(shapesGraph, SHACL.ConstraintComponent).forEach(sccNode->{
            SparqlComponent c = sparqlConstraintComponent(shapesGraph, sccNode);
            if ( c != null ) {
                for ( Parameter p : c.getParams() ) {
                    x.paramPathToComponents.put(p.getParameterPath(), c);
                    x.parameters.add(p);
                }
            }
        });
        return x;
    }

    /**
     * Stage 2 :
     * Build all the Constraints for Validators for this shape.
     * Must have all the required parameters (else ignore).
     */
    public static List<Constraint> processShape(Graph shapesGraph, ConstraintComponents components, Shape shape) {
        List<Constraint> constraints = new ArrayList<>();

        // Only add set of requires once, not once per parameter.
        Set<Set<Node>> seen = new HashSet<>();

        for ( Parameter param : components.parameters ) {
            if ( param.isOptional() )
                continue;
            // The parameter's property node
            Node paramPath = param.getParameterPath();

            // Does the shape use the parameter?
            boolean b = G.contains(shapesGraph, shape.getShapeNode(), paramPath, null);
            if ( !b )
                continue;

//          Alternative approach
//            // Find all shapes uses the parameter.
//            List<Node> shapes = G1.find(shapesGraph, null, paramPath, null).mapWith(Triple::getSubject).toList();
            Node shNode = shape.getShapeNode();

            // All components with this parameter.
            Collection<SparqlComponent> sccs = components.paramPathToComponents.get(paramPath);
            // Which shapes conform (have all required parameters)?
            // And we have not seen before for this shape?
            sccs.forEach(scc->{
                List<Node> required = scc.getRequiredParameters();

                // Check not seen.
                Set<Node> x = Set.copyOf(required);
                if ( seen.contains(x) ) {
                    return;
                }
                seen.add(x);

                if ( Parameters.doesShapeHaveAllParameters(shapesGraph, shape.getShapeNode(), required) ) {
                    // shape -> parameters
                    // Is this a cross product and can it be avoided?
                    MultiValuedMap<Parameter, Node> parameterValues = constraintParameterValues(shapesGraph, shNode, scc);
                    // [PARSE]
                    // Syntax rule: https://www.w3.org/TR/shacl/#syntax-rule-multiple-parameters
                    //  If there are >1 parameters, each must be single valued.

                    if ( parameterValues.keySet().size() > 1 ) {
                        Map<Parameter, Node> parameterMap = new HashMap<>();
                        parameterValues.asMap().forEach((p,values)->{
                           if ( values.size() > 1 )
                               throw new ShaclParseException("Multiple values for parameter "+p+" in constraint with multiple parameters");
                        });
                    }
                    ConstraintComponentSPARQL constraintComponentSPARQL = new ConstraintComponentSPARQL(scc, parameterValues);
                    constraints.add(constraintComponentSPARQL);
                }
                else {
                    // Incomplete.
                    //System.out.println("shape: no: "+shNode);
                }
            });
        }
        return constraints;
    }

    private static MultiValuedMap<Parameter, Node> constraintParameterValues(Graph shapesGraph, Node shNode, SparqlComponent scc) {
        Node nodeValidatorNode = G.getZeroOrOneSP(shapesGraph, shNode, SHACL.nodeValidator);
        Node propertyValidatorNode = G.getZeroOrOneSP(shapesGraph, shNode, SHACL.propertyValidator);
        Node validatorNode = G.getZeroOrOneSP(shapesGraph, shNode, SHACL.validator);
        Node vNode = firstNonNull(nodeValidatorNode, propertyValidatorNode, validatorNode);
        MultiValuedMap<Parameter, Node> parameterValues = Parameters.parameterValues(shapesGraph, shNode, scc);
        return parameterValues;
    }

    /** This handles all ConstraintComponents; only SPARQL ones are currently supported. */
    private static SparqlComponent sparqlConstraintComponent(Graph shapesGraph, Node constraintComponentNode) {
        if ( SHACL.JSConstraintComponent.equals(constraintComponentNode) )
            return null;
        List<Parameter> params = Parameters.parseParameters(shapesGraph, constraintComponentNode);

        // 6.2.3 Validators
        //
        // For every supported shape type (i.e., property shape or node shape) the constraint component
        // declares a suitable validator. For a given constraint, a validator is selected from the
        // constraint component using the following rules, in order:
        //
        //     1. For node shapes, use one of the values of sh:nodeValidator, if present.
        //     2. For property shapes, use one of the values of sh:propertyValidator, if present.
        //     3. Otherwise, use one of the values of sh:validator.
        //
        // If no suitable validator can be found, a SHACL-SPARQL processor ignores the constraint.
        //
        // SHACL-SPARQL includes two types of validators, based on SPARQL SELECT (for sh:nodeValidator
        // and sh:propertyValidator) or SPARQL ASK queries (for sh:validator).

        SparqlComponent x = possibleValidator(shapesGraph, constraintComponentNode, SHACL.nodeValidator, params);
        if ( x == null )
            x = possibleValidator(shapesGraph, constraintComponentNode, SHACL.propertyValidator, params);
        if ( x == null )
            x = possibleValidator(shapesGraph, constraintComponentNode, SHACL.validator, params);
        return x;
    }

    private static SparqlComponent possibleValidator(Graph shapesGraph, Node constraintComponentNode, Node vProperty, List<Parameter> params) {
        List<Node> x = G.listSP(shapesGraph, constraintComponentNode, vProperty);
        if ( x.isEmpty() )
            return null;
        for(Node valNode : x ) {
            SparqlComponent cs = possibleSparqlValidator(shapesGraph, valNode, params, constraintComponentNode);
            if ( cs != null )
                return cs;
        }
        return null;
    }

    private static SparqlComponent possibleSparqlValidator(Graph shapesGraph, Node valNode, List<Parameter> params,
                                                           /* for reporting */ Node constraintComponentNode) {
        // Check for SHACL-JS
        Node xJSFunctionName = G.getZeroOrOneSP(shapesGraph, valNode, SHACL.jsFunctionName);
        if ( xJSFunctionName != null )
            Log.warn(ConstraintComponents.class, "Found javascript validator - ignored (JavaScript not currently supported)");

        // One of sh:select or sh:ask.
        Node xSelect = G.getZeroOrOneSP(shapesGraph, valNode, SHACL.select);
        Node xAsk = G.getZeroOrOneSP(shapesGraph, valNode, SHACL.ask);
        if ( xSelect == null && xAsk == null )
            return null;
        if ( xSelect != null && xAsk != null )
            throw new ShaclParseException("SparqlConstraintComponent: Multiple SPARQL queries: "+displayStr(constraintComponentNode));
        String prefixes = ShLib.prefixes(shapesGraph, valNode);
        String queryString = firstNonNull(xSelect, xAsk).getLiteralLexicalForm().trim();
        String message = asString(G.getZeroOrOneSP(shapesGraph, valNode, SHACL.message));
        if ( ! prefixes.isEmpty() )
            queryString = prefixes+"\n"+queryString;
        boolean isSelect = (xSelect!=null);
        SparqlComponent cs = SparqlComponent.constraintComponent(constraintComponentNode, queryString, params, message);
        if ( cs.getQuery().isSelectType() != isSelect )
            throw new ShaclParseException("Query type does not match property");
        return cs;
    }

    private static String asString(Node x) {
        if ( x == null )
            return null;
        if ( ! x.isLiteral() )
            return null;
        return x.getLiteralLexicalForm();
    }
}
