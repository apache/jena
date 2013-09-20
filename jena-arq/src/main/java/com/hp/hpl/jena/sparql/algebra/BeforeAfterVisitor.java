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

package com.hp.hpl.jena.sparql.algebra;

import com.hp.hpl.jena.sparql.algebra.op.* ;

public class BeforeAfterVisitor extends OpVisitorByType//implements OpVisitor
{
    OpVisitor beforeVisitor = null ;
    OpVisitor afterVisitor = null ;
    OpVisitor mainVisitor = null ;
    
    public BeforeAfterVisitor(OpVisitor mainVisitor ,
                              OpVisitor beforeVisitor, 
                              OpVisitor afterVisitor) 
    {
        this.mainVisitor = mainVisitor ;
        this.beforeVisitor = beforeVisitor ;
        this.afterVisitor = afterVisitor ;
    }
    
    private void before(Op op) { 
        if ( beforeVisitor != null )
            op.visit(beforeVisitor) ;
    }

    private void after(Op op) {
        if ( afterVisitor != null )
            op.visit(afterVisitor) ;
    }

    @Override
    protected void visit0(Op0 op) { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

    @Override
    protected void visit1(Op1 op) { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

    @Override
    protected void visit2(Op2 op) { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

    @Override
    protected void visitN(OpN op) { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

    @Override
    protected void visitFilter(OpFilter op) { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

    @Override
    protected void visitLeftJoin(OpLeftJoin op) { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }

    @Override
    protected void visitExt(OpExt op) { 
        before(op) ; op.visit(mainVisitor) ; after(op) ;
    }
}
