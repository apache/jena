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

package org.apache.jena.sparql.service.enhancer.impl;

import java.io.Serializable;
import java.util.Objects;

/**
 * An estimated value with a flag that indicates whether it's exact.
 */
public class Estimate<T>
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    protected boolean isExact;
    protected T value;

    public Estimate(T value, boolean isExact) {
        super();
        this.value = value;
        this.isExact = isExact;
    }

    public boolean isExact() {
        return isExact;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Estimate [isExact=" + isExact + ", value=" + value + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(isExact, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Estimate<?> other = (Estimate<?>) obj;
        return isExact == other.isExact && Objects.equals(value, other.value);
    }
}
