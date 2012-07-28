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

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.Capabilities;

/**
    A default implementation of capabilities, in which everything is allowed,
    size is accurate, and graphs may be completely empty.
 */

public class AllCapabilities implements Capabilities
    {
    @Override
    public boolean sizeAccurate() { return true; }
    @Override
    public boolean addAllowed() { return addAllowed( false ); }
    @Override
    public boolean addAllowed( boolean every ) { return true; } 
    @Override
    public boolean deleteAllowed() { return deleteAllowed( false ); }
    @Override
    public boolean deleteAllowed( boolean every ) { return true; } 
    @Override
    public boolean canBeEmpty() { return true; }
    @Override
    public boolean iteratorRemoveAllowed() { return true; }
    @Override
    public boolean findContractSafe() { return true; }
    @Override
    public boolean handlesLiteralTyping() { return true; }
    }
