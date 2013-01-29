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

package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.Map;

/**
 * Interface for all nodes in the network.
 */
public interface RETENode {
    
    /**
     * Clone this node in the network across to a different context.
     * @param netCopy a map from RETENodes to cloned instance so far.
     * @param context the new context to which the network is being ported
     */
    public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext context) ;

}
