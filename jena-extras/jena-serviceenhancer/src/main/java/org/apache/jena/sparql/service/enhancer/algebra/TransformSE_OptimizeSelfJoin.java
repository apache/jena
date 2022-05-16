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

package org.apache.jena.sparql.service.enhancer.algebra;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.service.enhancer.impl.ServiceOpts;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;

/** It seems that preemtive optimization before execution does not work with property
 *  functions. So this class is for now not used. */
public class TransformSE_OptimizeSelfJoin
    extends TransformCopy
{
    // Optimizer for rewriting self
    protected Rewrite selfRewrite;

    public TransformSE_OptimizeSelfJoin(Rewrite selfRewrite) {
        super();
        this.selfRewrite = selfRewrite;
    }

    @Override
    public Op transform(OpService opService, Op subOp) {
        Op result;
        ServiceOpts so = ServiceOpts.getEffectiveService(
                new OpService(opService.getService(), subOp, opService.getSilent()));

        OpService targetService = so.getTargetService();
        if (ServiceEnhancerConstants.SELF.equals(targetService.getService())) {
            String optimizerOpt = so.getFirstValue(ServiceOpts.SO_OPTIMIZE, "on", "on");

            if (!optimizerOpt.equalsIgnoreCase("off")) {
                Op newSub = selfRewrite.rewrite(targetService.getSubOp());

                so.removeKey(ServiceOpts.SO_OPTIMIZE);
                // so.add(ServiceOpts.SO_OPTIMIZE, "off");
                // so.add(ServiceOpts.SO_OPTIMIZE, "on");
                result = new ServiceOpts(
                        new OpService(targetService.getService(), newSub, targetService.getSilent()),
                        so.getOptions()).toService();
            } else {
                result = so.toService();
            }
        } else {
            result = so.toService();
        }

        return result;
    }
}
