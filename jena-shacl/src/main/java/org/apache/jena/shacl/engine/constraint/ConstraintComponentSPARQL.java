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

import java.util.Objects;
import java.util.Set;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.Parameter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.path.Path;

/** SPARQL Constraint (ASK or SELECT) */
public class ConstraintComponentSPARQL implements Constraint {
    protected final SparqlComponent sparqlConstraintComponent;
    protected final Multimap<Parameter, Node> parameterMap;
    protected final Query query;

    public ConstraintComponentSPARQL(SparqlComponent sparqlConstraintComponent, 
                                     Multimap<Parameter, Node> parameterMap) {
        //sh:labelTemplate
        this.sparqlConstraintComponent = sparqlConstraintComponent;
        this.parameterMap = parameterMap;

        String qs =  sparqlConstraintComponent.getSparqlString();
        try {
            this.query = QueryFactory.create(sparqlConstraintComponent.getSparqlString());
            if ( !query.isAskType() && !query.isSelectType() )
                throw new ShaclParseException("Not a SELECT or ASK query");
        } catch (QueryParseException ex) {
            throw new ShaclParseException("Bad query: "+ex.getMessage());
        }
    }

    @Override
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        SparqlValidation.validate(vCxt, data, shape, focusNode, null, focusNode, query, parameterMap,
                                  sparqlConstraintComponent.getMessage(),
                                  new ReportConstraint(sparqlConstraintComponent.getReportComponent()));
    }

    @Override
    public void validatePropertyShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode, Path path, Set<Node> valueNodes) {
        valueNodes.forEach(vn->
                SparqlValidation.validate(vCxt, data, shape, focusNode, path, vn, query, parameterMap,
                                          sparqlConstraintComponent.getMessage(),
                                          new ReportConstraint(sparqlConstraintComponent.getReportComponent())));
    }

    @Override
    public Node getComponent() {
        return SHACL.SPARQLConstraintComponent;
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {}

    @Override
    public String toString() {
        if ( sparqlConstraintComponent.isSelect() )
            return "SELECT"+parameterMap;
        else
            return "ASK"+parameterMap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterMap, query, sparqlConstraintComponent);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ConstraintComponentSPARQL other = (ConstraintComponentSPARQL)obj;
        return Objects.equals(parameterMap, other.parameterMap) && Objects.equals(query, other.query)
               && Objects.equals(sparqlConstraintComponent, other.sparqlConstraintComponent);
    }
}
