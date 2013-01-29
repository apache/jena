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

/**
 * Base class for stack frame objects. Originally this was used to provide
 * pool-based allocated but it turns out the normal Java GC outperforms
 * manual pool-based allocation anyway.
 */
public class FrameObject {

    /** Used to link the frame to the prior frame in the (tree) stack or the pool */
    FrameObject link;
        
    /**
     * Link this frame to an existing frame. In the future this might do some ref count
     * tricks.
     */
    public void linkTo(FrameObject prior) {
        link = prior;
    }
    
    /**
     * Link this frame to an existing frame. This will never do any funny ref count tricks.
     */
    public void fastLinkTo(FrameObject prior) {
        link = prior;
    }
    
    /**
     * Return the prior frame in the tree.
     */
    public FrameObject getLink() {
        return link;
    }
    
    /**
     * Close the frame actively. This frees any internal resources, frees this frame and
     * frees the frame to which this is linked.
     */
    public void close() {
    }
    
}
