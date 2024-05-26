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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.JA;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.assembler.assemblers.ModelAssembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.MappingRegistry;

/**
 * Assembler for a secured model.
 *
 * <p>
 * The assembler file should include the following
    * <pre>
 * &lt;&gt; ja:loadClass    "org.apache.jena.permission.SecuredAssembler" .
 *
 * sec:Model rdfs:subClassOf ja:NamedModel .
 *
 * sec:evaluator rdfs:domain sec:Model ;
 *               rdfs:range sec:Evaluator .
 * </pre>
 *
 * The model definition should include something like.
 *
 * <pre>
 * my:secModel a sec:Model ;
 *    sec:baseModel my:baseModel ;
 *    ja:modelName "http://example.com/securedModel" ;
 *    sec:evaluatorFactory "org.apache.jena.permission.MockSecurityEvaluator" ;
 *    .
 * </pre>
 *
 * Terms used in above example:
 *
 * <dl>
 * <dt>my:secModel</dt>
 * <dd>The secured model as referenced in the assembler file.</dd>
 *
 * <dt>sec:Model</dt>
 * <dd>Identifes my:secModel as a secured model</dd>
 *
 * <dt>sec:baseModel</dt>
 * <dd>Identifies my:baseModel as the base model we are applying permissions
 * to</dd>
 *
 * <dt>my:baseModel</dt>
 * <dd>a ja:Model (or subclass) defined elsewhere in the assembler file</dd>
 *
 * <dt>ja:modelName</dt>
 * <dd>The name of the graph as it will be addressed in the permission
 * environment (see ja:NamedModel examples from Jena)</dd>
 *
 * <dt>sec:evaluatorFactory</dt>
 * <dd>Identifies "org.apache.jena.permission.MockSecurityEvaluator" as the java
 * class that implements an Evaluator Factory. The Factory must have static
 * method <code>getInstance()</code> that returns a SecurityEvaluator.</dd>
 * </dl>
 *
 * or if using an evaluator assembler
 *
 * <pre>
 * my:secModel a sec:Model ;
 *    sec:baseModel my:baseModel ;
 *    ja:modelName "http://example.com/securedModel" ;
 *    sec:evaluatorImpl ex:myEvaluator;
 *    .
 *
 * ex:myEvaluator a sec:Evaluator ;
 *    ex:arg1 "argument 1 for my evaluator constructor" ;
 *    ex:arg2 "argument 2 for my evaluator constructor" ;
 *    .
 * </pre>
 *
 * Terms used in above example:
 *
 * <dl>
 * <dt>my:secModel</dt>
 * <dd>The secured model as referenced in the assembler file.</dd>
 *
 * <dt>sec:Model</dt>
 * <dd>Identifies my:secModel as a secured model</dd>
 *
 * <dt>sec:baseModel</dt>
 * <dd>Identifies my:baseModel as the base model we are applying permissions
 * to</dd>
 *
 * <dt>my:baseModel</dt>
 * <dd>a ja:Model (or subclass) defined elsewhere in the assembler file</dd>
 *
 * <dt>ja:modelName</dt>
 * <dd>The name of the graph as it will be addressed in the permission
 * environment (see ja:NamedModel examples from Jena)</dd>
 *
 * <dt>sec:evaluatorImpl</dt>
 * <dd>Identifies ex:myEvaluator as a SecurityEvaluator defined elsewhere in the
 * assembler file. It must subclass as a sec:Evaluator.</dd>
 *
 * <dt>ex:arg1 and ex:arg2</dt>
 * <dd>Arguments as defined by the user defined security evaluator
 * assembler.</dd>
 * </dl>
 */
public class SecuredAssembler extends ModelAssembler implements AssemblerConstants {
    private static boolean initialized;

    // message formats
    private static final String ERROR_FINDING_FACTORY = "Error finding factory class %s:  %s";

    static {
        init();
    }

    /**
     * Initialize the assembler. Registers the prefix "sec" with the uri
     * http://apache.org/jena/permission/Assembler# and registers this assembler
     * with the uri http://apache.org/jena/permission/Assembler#Model
     */
    static synchronized public void init() {
        if (initialized)
            return;
        MappingRegistry.addPrefixMapping("sec", URI);
        registerWith(Assembler.general);
        initialized = true;
    }

    /**
     * Register this assembler in the assembler group.
     *
     * @param group The assembler group to register with.
     */
    static void registerWith(AssemblerGroup group) {
        if (group == null)
            group = Assembler.general;
        group.implementWith(SECURED_MODEL, new SecuredAssembler());
        group.implementWith(EVALUATOR_ASSEMBLER, new SecurityEvaluatorAssembler());
    }

    @Override
    public Model open(Assembler a, Resource root, Mode mode) {

        Resource rootModel = getUniqueResource(root, BASE_MODEL);
        if (rootModel == null) {
            throw new AssemblerException(root, String.format(NO_X_PROVIDED, BASE_MODEL, root));
        }
        Model baseModel = a.openModel(rootModel, Mode.ANY);

        Literal modelName = getUniqueLiteral(root, JA.modelName);
        if (modelName == null) {
            throw new AssemblerException(root, String.format(NO_X_PROVIDED, JA.modelName, root));
        }

        Literal factoryName = getUniqueLiteral(root, EVALUATOR_FACTORY);
        Resource evaluatorImpl = getUniqueResource(root, EVALUATOR_IMPL);
        if (factoryName == null && evaluatorImpl == null) {
            throw new AssemblerException(root, String.format("Either a %s or a %s must be provided for %s",
                    EVALUATOR_FACTORY, EVALUATOR_IMPL, root));
        }
        if (factoryName != null && evaluatorImpl != null) {
            throw new AssemblerException(root, String.format("May not specify both a %s and a %s for %s",
                    EVALUATOR_FACTORY, EVALUATOR_IMPL, root));
        }
        SecurityEvaluator securityEvaluator = null;
        if (factoryName != null) {
            securityEvaluator = executeEvaluatorFactory(root, factoryName);
        }
        if (evaluatorImpl != null) {
            securityEvaluator = getEvaluatorImpl(a, evaluatorImpl);
        }
        return Factory.getInstance(securityEvaluator, modelName.asLiteral().getString(), baseModel);

    }

    @Override
    protected Model openEmptyModel(Assembler a, Resource root, Mode mode) {
        return open(a, root, mode);
    }

    private SecurityEvaluator executeEvaluatorFactory(Resource root, Literal factoryName) {
        try {
            Class<?> factoryClass = Class.forName(factoryName.getString());
            Method method = factoryClass.getMethod("getInstance");
            if (!SecurityEvaluator.class.isAssignableFrom(method.getReturnType())) {
                throw new AssemblerException(root,
                        String.format(
                                "%s (found at %s for %s) getInstance() must return an instance of SecurityEvaluator",
                                factoryName, EVALUATOR_FACTORY, root));
            }
            if (!Modifier.isStatic(method.getModifiers())) {
                throw new AssemblerException(root,
                        String.format("%s (found at %s for %s) getInstance() must be a static method", factoryName,
                                EVALUATOR_FACTORY, root));
            }
            return (SecurityEvaluator) method.invoke(null);
        } catch (SecurityException e) {
            throw new AssemblerException(root, String.format(ERROR_FINDING_FACTORY, factoryName, e.getMessage()), e);
        } catch (IllegalArgumentException e) {
            throw new AssemblerException(root, String.format(ERROR_FINDING_FACTORY, factoryName, e.getMessage()), e);
        } catch (ClassNotFoundException e) {
            throw new AssemblerException(root, String.format("Class %s (found at %s for %s) could not be loaded",
                    factoryName, EVALUATOR_FACTORY, root));
        } catch (NoSuchMethodException e) {
            throw new AssemblerException(root, String.format(
                    "%s (found at %s for %s) must implement a static getInstance() that returns an instance of SecurityEvaluator",
                    factoryName, EVALUATOR_FACTORY, root));
        } catch (IllegalAccessException e) {
            throw new AssemblerException(root, String.format(ERROR_FINDING_FACTORY, factoryName, e.getMessage()), e);
        } catch (InvocationTargetException e) {
            throw new AssemblerException(root, String.format(ERROR_FINDING_FACTORY, factoryName, e.getMessage()), e);
        }
    }

    private SecurityEvaluator getEvaluatorImpl(Assembler a, Resource evaluatorImpl) {
        Object obj = a.open(a, evaluatorImpl, Mode.ANY);
        if (obj instanceof SecurityEvaluator) {
            return (SecurityEvaluator) obj;
        }
        throw new AssemblerException(evaluatorImpl,
                String.format("%s does not specify a SecurityEvaluator instance", evaluatorImpl));
    }

}
