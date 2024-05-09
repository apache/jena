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

package org.apache.jena.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary definition for the
 * <a href="https://www.w3.org/Submission/SWRL/">SWRL: A Semantic Web Rule Language Combining OWL and RuleML</a>.
 * <p>
 * See also OWLAPI-api {@code org.semanticweb.owlapi.vocab.SWRLVocabulary}
 *
 * @see <a href="https://www.w3.org/Submission/SWRL/swrl.rdf">SWRL schema</a>
 * @see SWRLB
 */
public class SWRL {
    public final static String URI = "http://www.w3.org/2003/11/swrl";
    public final static String NS = URI + "#";

    public static final Resource Imp = resource("Imp");
    public static final Resource IndividualPropertyAtom = resource("IndividualPropertyAtom");
    public static final Resource DatavaluedPropertyAtom = resource("DatavaluedPropertyAtom");
    public static final Resource ClassAtom = resource("ClassAtom");
    public static final Resource DataRangeAtom = resource("DataRangeAtom");
    public static final Resource Variable = resource("Variable");
    public static final Resource AtomList = resource("AtomList");
    public static final Resource SameIndividualAtom = resource("SameIndividualAtom");
    public static final Resource DifferentIndividualsAtom = resource("DifferentIndividualsAtom");
    public static final Resource BuiltinAtom = resource("BuiltinAtom");
    public static final Resource Builtin = resource("Builtin");

    public static final Property head = property("head");
    public static final Property body = property("body");
    public static final Property classPredicate = property("classPredicate");
    public static final Property dataRange = property("dataRange");
    public static final Property propertyPredicate = property("propertyPredicate");
    public static final Property builtin = property("builtin");
    public static final Property arguments = property("arguments");
    public static final Property argument1 = property("argument1");
    public static final Property argument2 = property("argument2");

    protected static Resource resource(String local) {
        return ResourceFactory.createResource(NS + local);
    }

    protected static Property property(String local) {
        return ResourceFactory.createProperty(NS + local);
    }

}
