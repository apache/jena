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

package org.apache.jena.reasoner.rulesys;


import org.apache.jena.reasoner.rulesys.impl.TestLPBRuleCloseBug;
import org.apache.jena.reasoner.rulesys.impl.TestLPBRuleEngine;
import org.apache.jena.reasoner.rulesys.impl.TestLPBRuleEngineLeak;
import org.apache.jena.reasoner.rulesys.impl.TestRestartableLBRule;
import org.apache.jena.reasoner.rulesys.test.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestConfigVocabulary.class,
    TestGenericRuleReasonerConfig.class,
    TestBasics.class,

    TestComparatorBuiltins.class,
    TestRuleUtil.class,

    TestBackchainer.class,
    TestBasicLP.class,

    TestLPDerivation.class,
    TestLPBRuleEngine.class,
    TestRestartableLBRule.class,
    TestFBRules.class,
    TestGenericRules.class,
    TestRETE.class,
    TestSetRules.class,
    TestLPBRuleEngineLeak.class,
    OWLUnitTest.class,
    TestRuleSystemBugs.class,
    TestOWLMisc.class,
    FRuleEngineIFactoryTest.class,
    TestLPBRuleCloseBug.class,

    ConcurrencyTest.class,
    TestRestrictionsDontNeedTyping.class
})
public class TS3_RuleReasoners {}
