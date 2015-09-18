/**
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

package org.apache.jena.system;

import java.util.List ;

/**
 * A {@code JenaSubsystemRegistry} is a set of objects implementing {@link JenaSubsystemLifecycle}.
 * <p>
 * It is a set - at most one entry.
 * <p>Discovered in some way.
 */
public interface JenaSubsystemRegistry {
    
    /** Load - peform some kinds of search for {@link JenaSubsystemLifecycle} implementations.
     * This is called once only.
     */
    public void load();
    
    /** Add to the colection. */
    public void add(JenaSubsystemLifecycle module);

    /** check whether registered */
    public boolean isRegistered(JenaSubsystemLifecycle module);

    /** Remove from the colection. */
    public void remove(JenaSubsystemLifecycle module);

    public int size();

    public boolean isEmpty();

    /**
     * Return the registered items a copied list.
     * The list is detached from the
     * registry and the caller can mutate it.
     */
    public List<JenaSubsystemLifecycle> snapshot();

}
