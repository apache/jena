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

package org.apache.jena.query;

import org.apache.jena.sparql.core.Prologue ;

/** Query visitor pattern */

public interface QueryVisitor
{
    public default void startVisit(Query query) {}
    public default void visitPrologue(Prologue prologue) {}

    public default void visitResultForm(Query query) {}
    public default void visitSelectResultForm(Query query) {}
    public default void visitConstructResultForm(Query query) {}
    public default void visitDescribeResultForm(Query query) {}
    public default void visitAskResultForm(Query query) {}
    public default void visitJsonResultForm(Query query) {}

    public default void visitDatasetDecl(Query query) {}
    public default void visitQueryPattern(Query query) {}

    public default void visitGroupBy(Query query) {}
    public default void visitHaving(Query query) {}
    public default void visitOrderBy(Query query) {}
    public default void visitLimit(Query query) {}
    public default void visitOffset(Query query) {}
    public default void visitValues(Query query) {}

    public default void finishVisit(Query query) {}
}
