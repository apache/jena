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

package org.apache.jena.ontapi.impl.objects;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.ontapi.OntModelControls;
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Named entity with {@code rdf:type = rdfs:Datatype}.
 * Supported by OWL2 only.
 * In OWL1.1 and RDFS specifications {@code rdfs:Datatype} represents as {@link OntClass} object.
 */
@SuppressWarnings("WeakerAccess")
public class OntNamedDataRangeImpl extends OntObjectImpl implements OntDataRange.Named {

    public OntNamedDataRangeImpl(Node n, EnhGraph g) {
        super(checkNamed(n), g);
    }

    @Override
    public Class<Named> objectType() {
        return Named.class;
    }

    @Override
    public boolean isBuiltIn() {
        return getModel().isBuiltIn(this);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getOptionalRootStatement(this, RDFS.Datatype);
    }

    @Override
    public Stream<OntDataRange> equivalentClasses() {
        if (!OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_CLASS_EQUIVALENT_FEATURE)) {
            return Stream.empty();
        }
        return objects(OWL2.equivalentClass, OntDataRange.class);
    }

    @Override
    public OntStatement addEquivalentClassStatement(OntDataRange other) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_CLASS_EQUIVALENT_FEATURE, "owl:equivalentClass");
        return addStatement(OWL2.equivalentClass, other);
    }

    @Override
    public Named removeEquivalentClass(Resource other) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_CLASS_EQUIVALENT_FEATURE, "owl:equivalentClass");
        remove(OWL2.equivalentClass, other);
        return this;
    }

    @Override
    public RDFDatatype toRDFDatatype() {
        return getModel().getRDFDatatype(getURI());
    }
}
