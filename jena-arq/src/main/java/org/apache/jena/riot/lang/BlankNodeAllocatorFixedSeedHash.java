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

package org.apache.jena.riot.lang;

import java.util.UUID;

/**
 * A variant of {@link BlankNodeAllocatorHash} where a fixed seed is used so
 * repeated runs produce identical allocations
 * 
 */
public class BlankNodeAllocatorFixedSeedHash extends BlankNodeAllocatorHash {

    private UUID seed;

    /**
     * Creates a new allocator which will use a new random seed but that seed
     * will remain fixed
     */
    public BlankNodeAllocatorFixedSeedHash() {
        this(UUID.randomUUID());
    }

    /**
     * Creates a new allocator which will use a fixed seed
     * 
     * @param seed
     *            Seed
     */
    public BlankNodeAllocatorFixedSeedHash(UUID seed) {
        super();
        if (seed == null)
            throw new NullPointerException("seed cannot be null");
        this.seed = seed;
        this.reset();
    }

    @Override
    protected UUID freshSeed() {
        // NB - The parent constructor calls reset() so we have to provide a
        // fake value here temorarily which we then replace with the user
        // specified seed by calling reset() again in our own constructor
        return this.seed == null ? UUID.randomUUID() : this.seed;
    }
}
