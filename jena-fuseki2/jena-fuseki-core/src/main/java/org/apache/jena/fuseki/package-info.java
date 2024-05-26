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

/** Documentation for dispatch-execute.
 * <p>
 * <b>Classes</b>
 * <ul>
 * <li><em>DataAccessPointRegistry</em> :: Per server registry of {@code DataAccessPoint}s</li>
 * <li><em>DataAccessPoint</em> :: Name to DataService binding</li>
 * <li><em>DataService</em> :: RDF Dataset, counters, endpoints</li>
 * <li><em>Operation</em> :: Internal name of a service, not the name used in a configuration file.</li>
 * <li><em>Endpoint</em> :: Instance of an operation, toether with its counters.</li>
 * <li><em>DataAccessPointRegistry</em> :: Per server registry of DataAccessPoint</li>
 * </ul>
 * <b>Servlets</b>
 * <ul>
 * <li><em>FusekiFilter</em> :: Routes requests to Fuseki (handles the dynamic nature dataset naming) by calling ServiceRouter.</li>
 * <li><em>ServiceRouterServlet</em> :: Routes requests to the appropriate service (i.e. implementing servlet).</li>
 * <li><em>ActionBase</em> :: Creates a basic {@code HttpAction} and defines {@code execCommonWorker}.</li>
 * <li><em>ActionService</em> :: Fills in {@code HttpAction} with dataset and endpoint. Calls {@code setRequest} on an {@code HttpAction}.
 * It implements {@code execCommonWorker} as a lifecycle =&gt; {@code executeAction} =&gt; {@code executeLifecycle} =&gt; {@code validate - perform}
 * <li><em>ServiceRouter</em> :: Routing of request to the cocrete servlet implementations.
 * </ul>
 * <pre>
 * ServiceDispatchServlet &lt; ActionService &lt; ActionBase
 * Services               &lt; ActionService &lt; ActionBase
 * Admin operations       &lt; ActionCtl    &lt; ActionBase
 * Task management        &lt; ActionTasks  &lt; ActionBase
 * </pre>
 * <p>
 * <b>Registries</b>
 * <p>Located in {@code FusekiRegistries}.
 * <ul>
 * <li><em>ContentTypeToOperation</em>:: Map&lt;content-type, Operation&gt;</li>
 * <li><em>ContentTypeToOperation</em>:: Map&lt;String, Operation&gt;</li>
 * <li><em>Dispatch</em> :: {@literal Map<Operation, ActionService>}</li>
 * </ul>
 */

package org.apache.jena.fuseki;

