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

package org.apache.jena.shacl.engine.exec;

import static java.lang.String.format;
import static org.apache.jena.shacl.engine.ShaclPaths.pathToString;

import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.sparql.path.Path;

public class ValidationOp {
    private final Shape      shape;
    private final Target     target;
    private final Path       path;
    private final Constraint constraint;

    public ValidationOp(Shape shape, Target target, Path path, Constraint constraint) {
        super();
        this.shape = shape;
        this.target = target;
        this.path = path;
        this.constraint = constraint;
    }

    @Override
    public String toString() {
        return format("%s :: %s :: %s :: %s",
            shape.toString(), target,
            path==null ? "----" : pathToString(shape.getShapeGraph(), path),
            constraint);
    }

    public Shape getShape() {
        return shape;
    }

    public Target getTarget() {
        return target;
    }

    public Path getPath() {
        return path;
    }

    public Constraint getConstraint() {
        return constraint;
    }
}