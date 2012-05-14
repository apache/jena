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

package com.hp.hpl.jena.query;

import com.hp.hpl.jena.sparql.core.Prologue ;

/** Query visitor pattern */

public interface QueryVisitor
{
    public void startVisit(Query query) ;
    public void visitPrologue(Prologue prologue) ;

    public void visitResultForm(Query query) ;
    public void visitSelectResultForm(Query query) ;
    public void visitConstructResultForm(Query query) ;
    public void visitDescribeResultForm(Query query) ;
    public void visitAskResultForm(Query query) ;
    
    public void visitDatasetDecl(Query query) ;
    public void visitQueryPattern(Query query) ;
    
    public void visitGroupBy(Query query) ;
    public void visitHaving(Query query) ;
    public void visitOrderBy(Query query) ;
    public void visitLimit(Query query) ;
    public void visitOffset(Query query) ;
    public void visitValues(Query query) ;
    
    public void finishVisit(Query query) ;
}
