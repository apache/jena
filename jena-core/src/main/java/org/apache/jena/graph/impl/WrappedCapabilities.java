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

package org.apache.jena.graph.impl;

import org.apache.jena.graph.Capabilities ;

@SuppressWarnings("deprecation")
public class WrappedCapabilities implements Capabilities {

    protected final Capabilities other ;

    public WrappedCapabilities(Capabilities other) {
        this.other = other;
    }

    @Override
    public boolean sizeAccurate() {
        return other.sizeAccurate() ;
    }

    @Override
    public boolean addAllowed() {
        return other.addAllowed() ;
    }

    @Override
    public boolean deleteAllowed() {
        return other.deleteAllowed() ;
    }

    @Override
    public boolean addAllowed(boolean everyTriple) {
        return other.addAllowed(everyTriple) ;
    }

    @Override
    public boolean deleteAllowed(boolean everyTriple) {
        return other.deleteAllowed(everyTriple) ;
    }

    @Override
    public boolean iteratorRemoveAllowed() {
        return other.iteratorRemoveAllowed() ;
    }

    @Override
    public boolean canBeEmpty() {
        return other.canBeEmpty() ;
    }

    @Override
    public boolean findContractSafe() {
        return other.findContractSafe() ;
    }

    @Override
    public boolean handlesLiteralTyping() {
        return other.handlesLiteralTyping() ;
    }

}
