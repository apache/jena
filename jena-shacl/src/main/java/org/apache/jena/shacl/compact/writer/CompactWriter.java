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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.CollectionUtils;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.writer.WriterConst;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.compact.SHACLC;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.TargetType;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shared.PrefixMapping;

public class CompactWriter {

    /** Write shapes with directives BASE/PREFIX/IMPORTS.*/
    public static void print(IndentedWriter out, Shapes shapes) {
        // Output PrefixMap
        PrefixMap graphPrefixMap = shapes.getPrefixMap();

        // Formatter PrefixMap - with the std prefixes if not overridden.
        PrefixMap pmapWithStd = SHACLC.withStandardPrefixes();
        // Add to copy of the prefixes standard so we can later override any standard settings.
        pmapWithStd.putAll(graphPrefixMap);
        NodeFormatter nodeFmt = new NodeFormatterTTL(null, pmapWithStd);

        boolean someOutput = false;

        // BASE - output if and only if there is exactly one.
        String baseURI = shapes.getBaseURI();
        if ( baseURI != null ) {
            if ( someOutput )
                out.println();
            RiotLib.writeBase(out, baseURI, true);
            someOutput = true;
        }

        // PREFIX
        if ( ! graphPrefixMap.isEmpty() ) {
            if ( someOutput )
                out.println();
            RiotLib.writePrefixes(out, graphPrefixMap, true);
            someOutput = true;
        }

        // IMPORTS
        if ( shapes.getImports() != null ) {
            if ( ! shapes.getImports().isEmpty() ) {
                if ( someOutput )
                    out.println();
                shapes.getImports().forEach(n->{
                    out.print("IMPORTS ");
                    out.pad(WriterConst.PREFIX_IRI);
                    nodeFmt.format(out, n);
                    out.println();
                });
            }
        }

        PrefixMapping prefixMappingWithStd = Prefixes.adapt(pmapWithStd);
        ShapeOutputVisitor visitor = new ShapeOutputVisitor(prefixMappingWithStd, nodeFmt, out);
        shapes.iteratorAll().forEachRemaining(sh->{
            out.println();
            writeOneShapeCompact(out, nodeFmt, visitor, sh);
            //writeOneShapeCompactOrSkip(out, nodeFmt, visitor, sh);
        });
        out.flush();
    }

    /** Write in compact syntax or throw {@link ShaclNotCompactException} */
    private static void writeOneShapeCompact(IndentedWriter out, NodeFormatter nodeFmt, ShapeOutputVisitor visitor, Shape sh) {
        // Write-or-crash.
        // Provides indicate of information lost.
        if ( sh.getShapeNode().isURI() ) {
            CompactWriter.output(out, nodeFmt, visitor, sh);
        }
    }

    /** Write in compact syntax or skip, noting the fact in a comment */
    private static void writeOneShapeCompactOrSkip(IndentedWriter out, NodeFormatter nodeFmt, ShapeOutputVisitor visitor, Shape sh) {
        // Write a shape is we can, else comment.
        try {
            try ( IndentedLineBuffer out2 = new IndentedLineBuffer() ) {
                // Need new visitor to hold the IndentedLineBuffer
                ShapeOutputVisitor visitorMem = visitor.fork(out2);
                if ( sh.getShapeNode().isURI() )
                    CompactWriter.output(out2, nodeFmt, visitorMem, sh);
                out.print(out2.asString());
            }
        } catch (ShaclNotCompactException ex) {
            out.print("## Can't write in compact syntax: ");
            nodeFmt.format(out, sh.getShapeNode());
            out.println();
        }
    }


    private static NodeFormatter formatterPrefixMap(PrefixMapping prefixMapping) {
        PrefixMap pmap = prefixMapWithStd(prefixMapping);
        NodeFormatter nodeFmt = new NodeFormatterTTL(null, pmap);
        return nodeFmt;
    }

    private static PrefixMap prefixMapWithStd(PrefixMapping prefixMapping) {
        PrefixMap pmap = SHACLC.withStandardPrefixes();
        prefixMapping.getNsPrefixMap().forEach((p,u)->pmap.add(p, u));
        return pmap;
    }

    public static void output(IndentedWriter out, NodeFormatter nodeFmt, ShapeOutputVisitor visitor, Shape sh) {
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
                CompactWriter.notShaclc("Implicit classes and targetClass");
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

    /** Write recursively.
     * Used for an inline sh:node.
     * @param out
     * @param nodeFmt
     * @param sh
     */
    public static void output(IndentedWriter out, NodeFormatter nodeFmt, Shape sh) {
        // If this were critical for performance, having a "serialization context"
        // with out, nodeFmt and prefixes" would be better. But this is the only place the
        PrefixMapping prefixMappingWithStd = SHACLC.withStandardPrefixes(sh.getShapeGraph().getPrefixMapping());
        ShapeOutputVisitor visitor = new ShapeOutputVisitor(prefixMappingWithStd, nodeFmt, out);
        sh.visit(visitor);
    }

    /** Return the single constraint of a Node shape or return null. */
    public static Constraint getCompactPrintable(Shape other) {
        if ( other.isPropertyShape() )
            return null;
        if ( ! other.getPropertyShapes().isEmpty() )
            return null;
        Collection<Constraint> constraints = other.getConstraints();
        if ( constraints.size() != 1 )
            return null;
        return CollectionUtils.oneElt(constraints);
    }

    private static void notShaclc(String string) {
        throw new ShaclException("Not supported in SHACLC: "+string);
    }
}
