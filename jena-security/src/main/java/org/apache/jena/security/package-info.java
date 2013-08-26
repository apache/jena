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
/**
 * JenaSecurity is a SecurityEvaluator interface and a set of dynamic proxies that apply that 
 * interface to Jena Graphs, Models, and associated methods and classes.
 * <p>
 * The SecurityEvaluator class must be implemented.  This class provides the interface to the 
 * authentication results (e.g. <code>getPrincipal()</code>) and the authorization system.
 * </p><p>
 * <ul>
 * <li>
 * Create a SecuredGraph by calling <code>Factory.getInstance( SecurityEvaluator, String, Graph );</code>
 * </li><li>
 * Create a SecuredModel by calling <code>Factory.getInstance( SecurityEvaluator, String, Model )</code> 
 * </li><li>
 * It is not recommended that you create a model by calling the Jena <code>ModelFactory.createModelForGraph( SecuredGraph )</code>
 * See Overview for discussion.
 * </li>
 * </ul>
 * </p><p>
 * <em>NOTES:</em>
 * <ul>
 * <li>See SecurityEvaluator documentation for description of cascading security checks</li>
 * <li>Secured methods are annotated with:
 * @sec.graph for permissions required on the graph to execute the method.
 * @sec.triple for permissions required on the associated triples (if any) to execute the method.
 * </li>
 * <li>It is possible to implement a SecurityEvaluator that does not enforce security at the triple
 * level.  See SecurityEvaluator documentation for details</li>
 * </ul>
 * </p>
 */
package org.apache.jena.security;