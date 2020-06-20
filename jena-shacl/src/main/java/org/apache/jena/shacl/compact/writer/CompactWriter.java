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

package org.apache.jena.shacl.compact.writer;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.TargetType;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;

public class CompactWriter {
    private static final boolean DEV = true;

    public static void print(OutputStream output, Shapes shapes) {
        IndentedWriter out = new IndentedWriter(output);
        out.setUnitIndent(4);
        if ( DEV )
            out.setFlushOnNewline(true);
        print(out, shapes);
    }

    public static void print(IndentedWriter out, Shapes shapes) {
        PrefixMap pmap = PrefixMapFactory.create();
        shapes.getGraph().getPrefixMapping().getNsPrefixMap().forEach((p,u)->pmap.add(p, u));

        // BASE
        // if contains

        // prefixes
        if ( ! pmap.isEmpty() ) {
            RiotLib.writePrefixes(out, pmap, true);
            out.println();
        }

        // Imports.
        // XXX Imports

        NodeFormatter nodeFmt = new NodeFormatterTTL(null, pmap);
        Collection<Shape> items = shapes.getRootShapes();
        if ( items.isEmpty() ) {}
        ShapeOutputVisitor visitor = new ShapeOutputVisitor(pmap, nodeFmt, out);
        // imports
        // shapes
        for ( Shape sh : shapes ) {
            if ( sh.getShapeNode().isURI() ) {
                output(out, nodeFmt, visitor, sh);
            }
        }
        out.flush();
    }

    // XXX NodeShape
    // XXX shapeClass - needs graph.or tag in the NodeShape.

    private static void output(IndentedWriter out, NodeFormatter nodeFmt, ShapeOutputVisitor visitor, Shape sh) {
        List<Target> targetImplicitClasses = sh.getTargets().stream()
            .filter(t->t.getTargetType()==TargetType.implicitClass)
            .collect(Collectors.toList());
        List<Target> targetClasses = sh.getTargets().stream()
            .filter(t->t.getTargetType()==TargetType.targetClass)
            .collect(Collectors.toList());

        if ( targetImplicitClasses.isEmpty() ) {
            out.print("shape ");
            nodeFmt.format(out, sh.getShapeNode());
            if ( ! targetClasses.isEmpty() ) {
                out.print(" ->");
                targetClasses.forEach(t-> {
                    out.print(" ");
                    nodeFmt.format(out, t.getObject());
                });
            }
        } else {
            if ( targetImplicitClasses.size() > 1 )
                CompactWriter.notShaclc("Multiple implicit classes");
            if ( ! targetClasses.isEmpty() )
                CompactWriter.notShaclc("Implciit classe and targetClass");
            Target target = targetImplicitClasses.get(0);
            out.print("shapeClass ");
            nodeFmt.format(out, target.getObject());
        }
        out.println(" {");
        out.incIndent();

        sh.visit(visitor);

        out.decIndent();
        out.ensureStartOfLine();
        out.println("}");
    }

    // XXX Needs work.
    public static void output(IndentedWriter out, NodeFormatter nodeFmt, Shape sh) {
        PrefixMap pmap = PrefixMapFactory.create();
        sh.getShapeGraph().getPrefixMapping().getNsPrefixMap().forEach((p,u)->pmap.add(p, u));
        ShapeOutputVisitor visitor = new ShapeOutputVisitor(pmap, nodeFmt, out);
        sh.visit(visitor);
    }

    /** Return the single constraint of a Node shape or return null. */
    public static Constraint getCompactPrintable(Shape other) {
        if ( other.isPropertyShape() )
            return null;
        if ( ! other.getPropertyShapes().isEmpty() )
            return null;
        List<Constraint> constraints = other.getConstraints();
        if ( constraints.size() != 1 )
            return null;
        return constraints.get(0);
    }

    private static void notShaclc(String string) {
        throw new ShaclException("Not supported in SHACLC: "+string);
    }
}
