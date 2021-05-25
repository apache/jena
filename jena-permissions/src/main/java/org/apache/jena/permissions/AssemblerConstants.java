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

package org.apache.jena.permissions;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public interface AssemblerConstants {
    public static final String URI = "http://apache.org/jena/permissions/Assembler#";
    /**
     * Property named URI+"evaluatorFactory"
     */
    public static final Property EVALUATOR_FACTORY = ResourceFactory.createProperty(URI + "evaluatorFactory");
    /**
     * Property named URI+"Model"
     */
    public static final Property SECURED_MODEL = ResourceFactory.createProperty(URI + "Model");
    /**
     * Property named URI+"baseModel"
     */
    public static final Property BASE_MODEL = ResourceFactory.createProperty(URI + "baseModel");
    /**
     * Property named URI+"Evaluator"
     */
    public static final Property EVALUATOR_ASSEMBLER = ResourceFactory.createProperty(URI + "Evaluator");
    /**
     * Property named URI+"evaluatorImpl"
     */
    public static final Property EVALUATOR_IMPL = ResourceFactory.createProperty(URI + "evaluatorImpl");

    /**
     * Property named URI+"evaluatorClass"
     */
    public static final Property EVALUATOR_CLASS = ResourceFactory.createProperty(URI + "evaluatorClass");
    /**
     * Property named URI+"evaluatorImpl"
     */
    public static final Property ARGUMENT_LIST = ResourceFactory.createProperty(URI + "args");

    // message formats
    public static final String NO_X_PROVIDED = "No %s provided for %s";
}
