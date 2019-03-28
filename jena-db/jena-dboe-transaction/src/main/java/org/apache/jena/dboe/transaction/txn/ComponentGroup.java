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

package org.apache.jena.dboe.transaction.txn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.jena.atlas.logging.Log;

final
public class ComponentGroup {
    private Map<ComponentId, TransactionalComponent> group = new HashMap<>();

    public ComponentGroup(TransactionalComponent...components) {
        Arrays.asList(components).forEach(this::add);
    }

    private ComponentGroup(Map<ComponentId, TransactionalComponent> group) {
        this.group.putAll(group);
    }

    public void add(TransactionalComponent component) {
        Objects.requireNonNull(component);
        //Log.info(this, "add("+component.getComponentId()+")");
        if ( component.getComponentId() == null )
            Log.warn(this,  "Null component id - likely to be overwritten: "+component);

        if ( group.containsKey(component.getComponentId()) ) {
            Log.warn(this,  component.getComponentId().toString());
            Log.warn(this,  "Add component already in the group: "+component);
        }

        group.put(component.getComponentId(), component);
    }

    /*package*/ void remove(ComponentId componentId) {
        group.remove(componentId);
    }

    public TransactionalComponent findComponent(ComponentId componentId) {
        return group.get(componentId);
    }

    public void forEachComponent(Consumer<? super TransactionalComponent> action) {
        group.values().forEach(action);
    }

    public void forEach(BiConsumer<ComponentId, TransactionalComponent> action) {
        group.forEach(action);
    }

    public void addAll(ComponentGroup components) {
        this.group.putAll(components.group);
    }

    public int size() { return group.size(); }
}

