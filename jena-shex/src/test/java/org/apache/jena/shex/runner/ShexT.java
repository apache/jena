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

package org.apache.jena.shex.runner;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/** Shex test vocabulary. */
public class ShexT {
    /*
--------------------
| p                |
====================
| sht:shape        | **
| sht:data         | **
| sht:schema       | **
| sht:focus        | **
| sht:trait        |
| sht:map          |
| sht:shapeExterns |
| sht:semActs      |
--------------------

-------------------------
| classes               |
=========================
| sht:ValidationTest    |
| sht:ValidationFailure |
-------------------------

Also class:
    sht:RepresentationTest
    and sx:

     */

    public static final String BASE_URI = "http://www.w3.org/ns/shacl/test-suite";

    public static final String NS = BASE_URI + "#";

    // Classes
    public final static Resource cValidationTest        = ResourceFactory.createResource(NS + "ValidationTest");
    public final static Resource cValidationFailure     = ResourceFactory.createResource(NS + "ValidationFailure");
    public final static Resource cRepresentationTest    = ResourceFactory.createResource(NS + "RepresentationTest");

    // Properties
    public final static Property shape = ResourceFactory.createProperty(NS + "shape");
    public final static Property data = ResourceFactory.createProperty(NS + "data");
    public final static Property schema = ResourceFactory.createProperty(NS + "schema");
    public final static Property focus = ResourceFactory.createProperty(NS + "focus");

    public final static Property trait = ResourceFactory.createProperty(NS + "trait");
    public final static Property map = ResourceFactory.createProperty(NS + "map");
    public final static Property shapeExterns = ResourceFactory.createProperty(NS + "shapeExterns");
    public final static Property semActs = ResourceFactory.createProperty(NS + "semActs");

    public static final String NS_SX = "https://shexspec.github.io/shexTest/ns#";
    public final static Property sx_shex = ResourceFactory.createProperty(NS_SX + "shex");
    public final static Property sx_json = ResourceFactory.createProperty(NS_SX + "json");
    public final static Property sx_ttl  = ResourceFactory.createProperty(NS_SX + "ttl");

    // Traits
    public final static Resource tAbstract    = ResourceFactory.createResource(NS + "Abstract");
    public final static Resource tAndShapeShapeession    = ResourceFactory.createResource(NS + "AndShapeShapeession");
    public final static Resource tAndValueExpression    = ResourceFactory.createResource(NS + "AndValueExpression");
    public final static Resource tAnnotation    = ResourceFactory.createResource(NS + "Annotation");
    public final static Resource tBacktick    = ResourceFactory.createResource(NS + "Backtick");
    public final static Resource tBNodeShapeLabel    = ResourceFactory.createResource(NS + "BNodeShapeLabel");
    public final static Resource tBooleanEquivalence    = ResourceFactory.createResource(NS + "BooleanEquivalence");
    public final static Resource tClosed    = ResourceFactory.createResource(NS + "Closed");
    public final static Resource tComparatorFacet    = ResourceFactory.createResource(NS + "ComparatorFacet");
    public final static Resource tDatatype    = ResourceFactory.createResource(NS + "Datatype");
    public final static Resource tDatatypedLiteralEquivalence    = ResourceFactory.createResource(NS + "DatatypedLiteralEquivalence");
    public final static Resource tDotCardinality    = ResourceFactory.createResource(NS + "DotCardinality");
    public final static Resource tEachOf    = ResourceFactory.createResource(NS + "EachOf");
    public final static Resource tEmpty    = ResourceFactory.createResource(NS + "Empty");
    public final static Resource tErrorReport    = ResourceFactory.createResource(NS + "ErrorReport");
    public final static Resource tExhaustive    = ResourceFactory.createResource(NS + "Exhaustive");
    public final static Resource tExtends    = ResourceFactory.createResource(NS + "Extends");
    public final static Resource tExternalSemanticAction    = ResourceFactory.createResource(NS + "ExternalSemanticAction");
    public final static Resource tExternalShape    = ResourceFactory.createResource(NS + "ExternalShape");
    public final static Resource tExtra    = ResourceFactory.createResource(NS + "Extra");
    public final static Resource tFocusConstraint    = ResourceFactory.createResource(NS + "FocusConstraint");
    public final static Resource tFractionDigitsFacet    = ResourceFactory.createResource(NS + "FractionDigitsFacet");
    public final static Resource tGreedy    = ResourceFactory.createResource(NS + "Greedy");
    public final static Resource tImport    = ResourceFactory.createResource(NS + "Import");
    public final static Resource tInclude    = ResourceFactory.createResource(NS + "Include");
    public final static Resource tIriEquivalence    = ResourceFactory.createResource(NS + "IriEquivalence");
    public final static Resource tLanguageTagEquivalence    = ResourceFactory.createResource(NS + "LanguageTagEquivalence");
    public final static Resource tLengthFacet    = ResourceFactory.createResource(NS + "LengthFacet");
    public final static Resource tLexicalBNode    = ResourceFactory.createResource(NS + "LexicalBNode");
    public final static Resource tMissedMatchables    = ResourceFactory.createResource(NS + "MissedMatchables");
    public final static Resource tMultiExtends    = ResourceFactory.createResource(NS + "MultiExtends");
    public final static Resource tNodeKind    = ResourceFactory.createResource(NS + "NodeKind");
    public final static Resource tNonDotCardinality    = ResourceFactory.createResource(NS + "NonDotCardinality");
    public final static Resource tNotValueExpression    = ResourceFactory.createResource(NS + "NotValueExpression");
    public final static Resource tNumericEquivalence    = ResourceFactory.createResource(NS + "NumericEquivalence");
    public final static Resource tOneOf    = ResourceFactory.createResource(NS + "OneOf");
    public final static Resource tOrderedSemanticActions    = ResourceFactory.createResource(NS + "OrderedSemanticActions");
    public final static Resource tOrValueExpression    = ResourceFactory.createResource(NS + "OrValueExpression");
    public final static Resource tOutsideBMP    = ResourceFactory.createResource(NS + "OutsideBMP");
    public final static Resource tPaternFacet    = ResourceFactory.createResource(NS + "PaternFacet");
    public final static Resource tRecursiveData    = ResourceFactory.createResource(NS + "RecursiveData");
    public final static Resource trelativeIRI    = ResourceFactory.createResource(NS + "relativeIRI");
    public final static Resource tRepeatedGroup    = ResourceFactory.createResource(NS + "RepeatedGroup");
    public final static Resource tRepeatedOneOf    = ResourceFactory.createResource(NS + "RepeatedOneOf");
    public final static Resource tSemanticAction    = ResourceFactory.createResource(NS + "SemanticAction");
    public final static Resource tShapeMap    = ResourceFactory.createResource(NS + "ShapeMap");
    public final static Resource tShapeReference    = ResourceFactory.createResource(NS + "ShapeReference");
    public final static Resource tStart    = ResourceFactory.createResource(NS + "Start");
    public final static Resource tStem    = ResourceFactory.createResource(NS + "Stem");
    public final static Resource tToldBNode    = ResourceFactory.createResource(NS + "ToldBNode");
    public final static Resource tTotalDigitsFacet    = ResourceFactory.createResource(NS + "TotalDigitsFacet");
    public final static Resource tTriplePattern    = ResourceFactory.createResource(NS + "TriplePattern");
    public final static Resource tUnsatisfiable    = ResourceFactory.createResource(NS + "Unsatisfiable");
    public final static Resource tValidLexicalForm    = ResourceFactory.createResource(NS + "ValidLexicalForm");
    public final static Resource tValueReference    = ResourceFactory.createResource(NS + "ValueReference");
    public final static Resource tValueSet    = ResourceFactory.createResource(NS + "ValueSet");
    public final static Resource tVapidExtra    = ResourceFactory.createResource(NS + "VapidExtra");
    public final static Resource tWildcard    = ResourceFactory.createResource(NS + "Wildcard");
}
