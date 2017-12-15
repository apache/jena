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
package org.apache.jena.obfuscate.transform;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.obfuscate.ObfuscationProvider;
import org.apache.jena.obfuscate.Obfuscator;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Algebra transformation that applies obfuscation
 */
public class TransformObfuscate extends TransformCopy {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformObfuscate.class);

    private final ObfuscationProvider obfuscator;

    public TransformObfuscate(ObfuscationProvider provider) {
        if (provider == null)
            throw new NullPointerException("Obfuscation Provider cannot be null");
        this.obfuscator = provider;
    }

    @Override
    public Op transform(OpAssign opAssign, Op subOp) {
        VarExprList obscuredExprs = new VarExprList();
        for (Var v : opAssign.getVarExprList().getVars()) {
            Var newVar = obscureAssignmentVariable(obscuredExprs, v);

            obscuredExprs.add(newVar, opAssign.getVarExprList().getExpr(v));
        }
        return OpAssign.assign(subOp, obscuredExprs);
    }

    @Override
    public Op transform(OpExtend opExtend, Op subOp) {
        VarExprList obscuredExprs = new VarExprList();
        for (Var v : opExtend.getVarExprList().getVars()) {
            Var newVar = obscureAssignmentVariable(obscuredExprs, v);

            obscuredExprs.add(newVar, opExtend.getVarExprList().getExpr(v));
        }
        return OpExtend.create(subOp, obscuredExprs);
    }

    protected final Var obscureAssignmentVariable(VarExprList obscuredExprs, Var v) {
        Var newVar = Var.alloc(obfuscator.obfuscateNode(v.asNode()));
        int i = 1;

        // Depending on how good the obfuscation is this might be unnecessary
        // but
        // for more naive obfuscation providers it is possible that two distinct
        // variables might be obfuscated into the same variable
        // Therefore if the assignment list already contains the obscured
        // variable append numbers until we find a free variable name
        // This will mean that variable names might not be consistent throughout
        // the obfuscated algebra but if that is a problem users should use a
        // better obfuscation scheme
        while (obscuredExprs.contains(newVar)) {
            if (i == 1) {
                LOGGER.warn(
                        "ObfuscationProvider generated the same obfuscation output for two distinct variable names, resulting obfuscated algebra may have variable name inconsistencies");
            }
            newVar = Var.alloc(String.format("%s%d", newVar.getVarName(), i));
            i++;
        }
        return newVar;
    }

    @Override
    public Op transform(OpDatasetNames opDatasetNames) {
        return new OpDatasetNames(obfuscator.obfuscateNode(opDatasetNames.getGraphNode()));
    }

    @Override
    public Op transform(OpGroup opGroup, Op subOp) {
        VarExprList obscuredGroupExprs = new VarExprList();
        for (Var v : opGroup.getGroupVars().getVars()) {
            Var newVar = obscureAssignmentVariable(obscuredGroupExprs, v);

            obscuredGroupExprs.add(newVar, opGroup.getGroupVars().getExpr(v));
        }

        // TODO Need to obscure aggregators
        return new OpGroup(subOp, obscuredGroupExprs, opGroup.getAggregators());
    }

    @Override
    public Op transform(OpProject opProject, Op subOp) {
        List<Var> obscuredVars = new ArrayList<>();
        for (Var v : opProject.getVars()) {
            obscuredVars.add(Var.alloc(obfuscator.obfuscateNode(v.asNode())));
        }
        return new OpProject(subOp, obscuredVars);
    }

    protected final BasicPattern obscureBasicPattern(BasicPattern orig) {
        BasicPattern obscured = new BasicPattern();
        for (Triple t : orig.getList()) {
            obscured.add(Obfuscator.obfuscate(obfuscator, t));
        }
        return obscured;
    }

    protected final QuadPattern obscureQuadPattern(QuadPattern quads) {
        QuadPattern obscured = new QuadPattern();
        for (Quad q : quads.getList()) {
            obscured.add(Obfuscator.obfuscate(obfuscator, q));
        }
        return obscured;
    }

    @Override
    public Op transform(OpBGP opBGP) {
        return new OpBGP(obscureBasicPattern(opBGP.getPattern()));
    }

    @Override
    public Op transform(OpQuadPattern opQuadPattern) {
        return new OpQuadPattern(obfuscator.obfuscateNode(opQuadPattern.getGraphNode()),
                obscureBasicPattern(opQuadPattern.getBasicPattern()));
    }

    @Override
    public Op transform(OpQuadBlock opQuadBlock) {
        return new OpQuadBlock(obscureQuadPattern(opQuadBlock.getPattern()));
    }

    @Override
    public Op transform(OpQuad opQuad) {
        return new OpQuad(Obfuscator.obfuscate(obfuscator, opQuad.getQuad()));
    }

    @Override
    public Op transform(OpGraph opGraph, Op subOp) {
        return new OpGraph(obfuscator.obfuscateNode(opGraph.getNode()), subOp);
    }

    @Override
    public Op transform(OpPath opPath) {
        // TODO Need to handle obfuscation inside of paths
        return super.transform(opPath);
    }

}
