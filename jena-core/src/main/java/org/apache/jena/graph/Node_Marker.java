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

package org.apache.jena.graph;

import org.apache.jena.shared.PrefixMapping;

/**
 * Marker special nodes for datastructures.
 * The application is responsible for allocating unique strings.
 */
public class Node_Marker extends Node_Ext<String> {

    public static Node marker(String str) { return new Node_Marker(str); }

    protected Node_Marker(String label) {
        super(label);
    }

    @Override
    public int hashCode() {
        return Node.hashExt + super.get().hashCode();
    }

    @Override
    public boolean isConcrete() {
        return false;
    }

    @Override
    public String toString( PrefixMapping pmap ) { return toString(); }

    @Override
    public String toString() {
        return null;
    }
}
