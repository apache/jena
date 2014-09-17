**Work with Sparql commands in rules**

copy/replace the following files to the specified folder:

	/jena-core/src/main/java/com/hp/hpl/jena/reasoner/rulesys
		Rule.java
		SparqlQuery.java

	/jena-core/src/main/java/com/hp/hpl/jena/util
		Tokenizer.java

	/jena-core/src/main/java/com/hp/hpl/jena/reasoner/rulesys/impl
		RuleClauseCode.java
		RETEConflictSet.java
		TripleMatchFrame.java
		LPInterpreter.java
		SparqlInRulesGenericFunctions.java
		RETEEngine.java
		FRuleEngine.java
		SRE_fireRuleTerms.java
		SparqlRuleEngine.java
		ExecSparqlCommand.java
		RETEClauseCombinedSparqlRules.java
		SparqlInRuleTabling.java
		Answer_SparqlInRules.java
		SparqlInRulesRuleContext.java
		ResultList.java
		RuleClauseCode.java
		ResultRow.java
		ResultsOp.java
		ResultInt.java
		Result.java


jena-arq should be added to dependencies of jena-core. If you build in maven and get an error in TestPackage.java, remove it (/jena-core/src/test/java/jena/test/TestPackage.java). To build with the tests, create the package com.hp.hpl.jena.reasoner.sparqlinrules.test and add the following files:
	
	CompareResults.java
	ConvertResult.java
	ResultField.java
	SparqlinRulesTest.java
	SparqlinRulesTest1.java
	SparqlinRulesTest2.java
	TestException.java
	TestPackage.java
	TestsGenericClass.java