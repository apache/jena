/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.permissions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;

/**
 * A simple assembler for a SecurityEvaluator
 * <p>
 * This assembler load the specified class, locates the first constructor that
 * accepts the number of arguments in the assembler file, and calls it passing
 * the arguments. Generally the result of this assembler is passed to a
 * sec:Model construction.
 *
 *      <ul>
 *      <li>The evaluator must have one and only one public constructor that
 *      takes the number of arguments specified in the assembler file.</li>
 *
 *      <li>The evaluator may have more constructors but they may not have the
 *      same number of arguments as specified in the assembler file.</li>
 *
 *      <li>The arguments must be specified in the assembler file in the order
 *      that they should be passed to the constructor.</li>
 *      </ul>
 *
 *      <p>
 *      Literal arguments are converted to their data type before calling the
 *      constructor. For example "13"^^xsd:int will be converted to an Integer
 *      with the value of 13.
 *      </p>
 *      The assembler file should include the following
 * <pre>
 * prefix xsd:        &lt;http://www.w3.org/2001/XMLSchema#&gt;
 *
 * &lt;&gt; ja:loadClass    "org.apache.jena.security.SecuredAssembler" .
 *
 * sec:Model rdfs:subClassOf ja:NamedModel .
 *
 * </pre>
 *
 *      The model definition should include something like.
 *
 *      <pre>
 * ex:myEvaluator a sec:Evaluator ;
 *    sec:args [ rdf:_1 "argument 1 for my evaluator constructor" ;
 *               rdf:_2 "13"^^xsd:int ; ];
 *    sec:evaluatorClass "evaluatorClassname";
 *    .
 * </pre>
 *
 *      Terms used in above example:
 *
 *      <dl>
 *      <dt>my:secEvaluator</dt>
 *      <dd>The security evaluator as referenced in the assembler file.</dd>
 *
 *      <dt>sec:Evaluator</dt>
 *      <dd>Identifies my:secEvaluator as a SecurityEvaluator</dd>
 *
 *      <dt>sec:args</dt>
 *      <dd>Identifies the argument list</dd>
 *
 *      <dt>rdf:_1</dt>
 *      <dd>The first argument</dd>
 *
 *      <dt>rdf:_2</dt>
 *      <dd>The second argument (an integer in this case</dd>
 *
 *      <dt>sec:evaluatorClass</dt>
 *      <dd>The fully qualified name of the SecurityEvaluator class to call.
 *      This class must extend SecurityEvaluator, and must have one and only one
 *      constructor that takes the number of arguments specified in
 *      sec:args</dd>
 *      </dl>
 *
 * @see SecuredAssembler
 *
 */
public class SecurityEvaluatorAssembler extends AssemblerBase implements Assembler, AssemblerConstants {
    // initialization and registration is performed by SecuredAssembler

    @Override
    public SecurityEvaluator open(Assembler a, Resource root, Mode mode) {

        Literal className = getUniqueLiteral(root, EVALUATOR_CLASS);
        if (className == null) {
            throw new AssemblerException(root, String.format(NO_X_PROVIDED, EVALUATOR_CLASS, root));
        }

        Class<?> clazz;
        try {
            clazz = Class.forName(className.getString());
        } catch (ClassNotFoundException e1) {
            throw new AssemblerException(root, String.format("Can not locate class %s as specified by %s in %s",
                    className, EVALUATOR_CLASS, root));
        }
        if (!SecurityEvaluator.class.isAssignableFrom(clazz)) {
            throw new AssemblerException(root,
                    String.format("Class %s as specified by %s in %s does not implement SecurityEvaluator", className,
                            EVALUATOR_CLASS, root));
        }

        // get the arguments as specified.
        List<Object> args = new ArrayList<>();
        Resource argRes = getUniqueResource(root, ARGUMENT_LIST);
        if (argRes != null) {
            Seq seq = argRes.as(Seq.class);
            NodeIterator iter = seq.iterator();
            RDFNode n = null;
            while (iter.hasNext()) {
                n = iter.next();
                if (n.isLiteral()) {
                    args.add(n.asLiteral().getValue());
                } else if (n.isResource()) {
                    args.add(a.open(a, n.asResource(), mode));
                } else {
                    throw new AssemblerException(root, String.format("%s must be a literal or a resource", n));
                }
            }
        }

        for (Constructor<?> c : clazz.getConstructors()) {
            if (c.getParameterTypes().length == args.size()) {
                try {
                    if (args.size() == 0) {
                        return (SecurityEvaluator) c.newInstance();
                    }
                    return (SecurityEvaluator) c.newInstance(args.toArray());
                } catch (InstantiationException e) {
                    throw new AssemblerException(root, e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new AssemblerException(root, e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    throw new AssemblerException(root, e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    throw new AssemblerException(root, e.getMessage(), e);
                }
            }

        }
        throw new AssemblerException(root,
                String.format("Class %s does not have a %s argument constructor", className, args.size()));

    }

}
