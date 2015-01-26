/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine;

/** Evaluate to one row of no columns (join identity).
 *  This is not same as a no-op - that's {@link StepJoinIdentity}
 *  @see StepPassThrough
 */
public class StepJoinIdentity<X> implements Step<X>
{
    public StepJoinIdentity() {}
    
    @Override
    public RowList<X> execute(RowList<X> input) {
        return RowLib.identityRowList() ;
    }

    @Override
    public String toString() { return "Step/JoinIdentity" ; }
}

