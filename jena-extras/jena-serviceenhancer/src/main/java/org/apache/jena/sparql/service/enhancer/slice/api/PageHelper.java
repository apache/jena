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

package org.apache.jena.sparql.service.enhancer.slice.api;

/**
 * Interface to ease working with fixed size pages.
 */
public interface PageHelper {
    long getPageSize();

    default long getPageOffsetForPageId(long pageId) {
        long pageSize = getPageSize();
        return getPageOffsetForPageId(pageId, pageSize);
    }

    default long getPageIdForOffset(long offset) {
        long pageSize = getPageSize();
        return getPageIdForOffset(offset, pageSize);
    }

    default long getIndexInPageForOffset(long offset) {
        long pageSize = getPageSize();
        return getIndexInPageForOffset(offset, pageSize);
    }

    public static long getPageIdForOffset(long offset, long pageSize) {
        long result = offset / pageSize;
        return result;
    }

    public static long getIndexInPageForOffset(long offset, long pageSize) {
        return offset % pageSize;
    }

    public static long getPageOffsetForPageId(long pageId, long pageSize) {
        return pageId * pageSize;
    }

    public static long getLastPageId(long size, long pageSize) {
        return size / pageSize;
    }
}
