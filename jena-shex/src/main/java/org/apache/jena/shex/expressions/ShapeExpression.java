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

package org.apache.jena.shex.expressions;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shex.sys.ValidationContext;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public abstract class ShapeExpression {

    public ShapeExpression() { }

    /** The "satisfies" function. Return true for OK, false for not OK. */
    public abstract boolean satisfies(ValidationContext vCxt, Node data);

    private static PrefixMap displayPrefixMap = PrefixMapFactory.createForOutput();
    static {
        displayPrefixMap.add("owl",  OWL.getURI());
        displayPrefixMap.add("rdf",  RDF.getURI());
        displayPrefixMap.add("rdfs", RDFS.getURI());
        displayPrefixMap.add("xsd",  XSD.getURI());
    }

    public static NodeFormatter nodeFmtAbbrev = new NodeFormatterTTL(null, displayPrefixMap);

    public abstract void print(IndentedWriter out, NodeFormatter nFmt);

    public String asString() {
        IndentedLineBuffer x = new IndentedLineBuffer();
        x.setFlatMode(true);
        print(x, nodeFmtAbbrev);
        return x.asString();
    }

    public abstract void visit(ShapeExprVisitor visitor);

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
