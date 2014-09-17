<style type="text/css">
<!--
.tab { margin-left: 40px; }
.td-right { text-align: right; }
.td-left { text-align: left; }
.td-center { text-align: center; }
.td-top { vertical-align: top; }
.td-middle { vertical-align: middle; }
.td-bottom { vertical-align: bottom; }
.center-align { text-align: center; }
.center-div { margin-left: auto; margin-right: auto; }
.right-div { margin-left: auto; margin-right: 0; }
.left-div { margin-left: 0; margin-right: auto; }
.td_align1 { 
	display: table-cell;
    vertical-align: top; 
}
.td_align2 { 
	vertical-align: bottom;
    text-align:left;
   }
.table_f1 {
		border: none;
       border-spacing: 0;
       border-collapse: collapse;
    }            
-->
</style>

**SPARQL commands in Jena rules**

SPARQL is a powerful query language to manipulating RDF data. Rule-based inference over RDF, as Jena reasoner supports, allows storing and manipulation of knowledge. Furthermore, a RDF rule-based inference allows to overcome some of OWL expressiveness limitations. Combining SPARQL with rules brings a more powerful way to represent knowledge and increases the expressiveness of Jena's rule engine. 

<br>

SPARQL commands in Jena rules enables to combine concepts from object oriented languages, query languages, and rule-based systems. We are able to a) link class definitions with SPARQL queries to capture constraints and rules that formalize the expected construction of those classes, b) to capture reusable SPARQL queries, c) representing a wide range of business rules.

SPARQL Inferencing Notation (SPIN) [1][2][3], is a SPARQL-based rule and constraint language for the Semantic Web. SPIN is also a mechanism to represent reusable SPARQL queries as templates and to define new SPARQL functions with a web-friendly syntax. Despite  SPARQL commands in Jena rules is not a implementation of SPIN in Jena, we share the main motivations, providing Jena with the mechanisms to take the same expressiveness as the spin frameworks. Making use of the motivations of SPIN that we share, "an example application area of SPIN templates is to extend existing rule profiles,
		such as <a href="http://composing-the-semantic-web.blogspot.com/2010/09/rdfs-plus-as-subset-of-owl-rl-in-sparql.html">OWL 2 RL</a>,
		by defining the profile as set of SPIN templates and adding new rules through templates and SPIN rules.
		Other examples of SPIN template libraries have been published for checking
		constraints defined in the SKOS standard and other <a href="http://semwebquality.org">data
		quality constraints</a>.
		These approaches demonstrate how SPIN can be used to transparently implement
		semantics by mixing standard RDFS and OWL definitions with generic, reusable rule
		libraries and application- or ontology-specific rules."[1]
    

<br>
**Rules based on SPARQL commands**

In our <i>RuleStore</i> we can have conclusions based on SPARQL commands. Rules based on SPARQL commands are evaluated in all engines mode: <i>backward</i>, <i>forward</i>, <i>forwardRETE</i> and <i>hybrid</i>. This is useful everytime that our knowledge is better represented by a SPARQL command, making use of the power of SPARQL. Furthermore, there are operations, e.g. aggregations, that are not supported by Jena's rule engines. In a SPARQL command in a rule can be used the query forms <i>SELECT</i> and <i>ASK</i>. 

Considering the <a href="https://jena.apache.org/documentation/inference/#RULEsyntax">Rule syntax and structure</a> of Jena, the syntax of a rule based on a SPARQL command is the follow: 

<i>bare-rule</i> := <i>SparqlCommand</i> -&gt; <i>hterm</i>, ... <i>hterm</i>    // forward rule
<br>
or
<br>
<i>bare-rule</i> := <i>bhterm</i> &lt;- <i>SparqlCommand</i> // backward rule
<br><br>

<br><br>
A SPARQL command is defined in a rule enclosed by \\\\\\SPARQL ... \\\\\\SPARQL :<br>

<i>SparqlCommand</i> := \\\\\\SPARQL \<SPARQL command\> \\\\\\SPARQL
<br>

<br>

**Examples**

Next are presented some examples of rules based on SPARQL commands.


	
1) Define a class based on a SPARQL command - A rectangle is square if the width is equal to the height.

	 (?r rdf:type ex:Square) <- 
		 (\\\SPARQL 
			 select ?r 
			 where { 
				 ?r ex:width ?width .  
				 ?r ex:height ?height . 
				 FILTER(?width = ?height) . 
			 } 
		 \\\SPARQL).  


2) Calculate the value of a property based on other properties. 

	 (?r ex:area ?area) <- 
		 (\\\SPARQL 
			 select ?r ?area 
			 where { 
				 ?r ex:width ?width . 
				 ?r ex:height ?height . 
				 bind( ?width * ?height as ?area ) . 
			 }
		 \\\SPARQL). 


3) Representing business rules - The last time a product was ordered.

	 (?p ex:lastOrderDate ?d) <- 
		(\\\SPARQL 
			select ?p (max(?dt) AS ?d) 
		 	where { 
				 . . .  
		 	} 
		\\\SPARQL). 


4) We can define relationships between resources that we cannot represent either in OWL or with rules - A given student is diligent in a given course if he doesn't fail more than two times.

	 (?s ex:isDiligent ?c) <- 
	 	(\\\SPARQL 
			(select ?s ?c 
			 where { 
				 ?s ex:enroledAt ?c . 
				 MINUS { 
					 select ?s ?c 
					 { 
					 select ?s ?c  
					 where { 
						 ?s ex:failsTo ?l . 
						 ?l ex:isLessonOf ?c . 
					 } 
					 group by ?s ?c 
					 having (count(1) > 2) 
				 }	 	
			 }  
		\\\SPARQL).   


It is also possible to perform limited OWL2 inference when we do not have an OWL2 reasoner - An Ironman is an athlete that plays at least 3 sports.

**OWL2** 

	 <owl:Class rdf:about="&ex;IronMan"> 
		 <rdfs:label>IronMan</rdfs:label> 
		 <owl:equivalentClass> 
			 <owl:Class> 
				 <owl:intersectionOf	rdf:parseType="Collection"> 
					 <rdf:Description rdf:about="&ex;Person"/> 
					 <owl:Restriction> 
						 <owl:onProperty rdf:resource="&ex;playSport"/> 
						 <owl:onClass rdf:resource="&ex;Sport"/> 
						 <owl:minQualifiedCardinality rdf:datatype="&xsd;nonNegativeInteger">3
						</owl:minQualifiedCardinality> 
						 </owl:Restriction> 
				 </owl:intersectionOf> 
			 </owl:Class> 
		 </owl:equivalentClass> 
	 </owl:Class> 

**Jena rule with SPARQL** 

	 (?x rdf:type ex:IronMan) <-
	 	(\\\SPARQL 	
			 (Select ?x 	
			 Where {	
			 	 ?x ex:playSport ?y . 	
			 } 	
			 group by ?x 	
			 having (count(1) >=3)
		 \\\SPARQL).	


5) We can deducted more than one conclusion from a SPARQL command.

 	(\\\SPARQL 	
		 (Select ?x (count(1) as ?n)	
		 Where {	
		 	 ?x ex:playSport ?y . 	
		 } 	
		 group by ?x 	
		 having (count(1) >=3)
	 \\\SPARQL) -> 	 
	 	(?x rdf:type ex:IronMan),
	 	()?x ex:numberOfSportsPlayed ?n).



6) Data quality management - It is also possible to capture data inconsistency when we don't have an OWL2 reasoner or even inconsistency rules that we cannot represent in OWL2.

Each product must have an unique identifier

	 (?p_id ex:violationRule ex:ViolationUnique) <- 
	 	(\\\SPARQL 	
			 (Select ?p_id 
			 where { 
				 ?p rdf:type ex:Product . 
				 ?p ex:productCode ?p_id . 
			 } 
			 group by ?p_id 
			 having count(1) > 1)
		\\\SPARQL).

7) A Sparql command can be combined with other rule terms.
	
	 (?x rdf:type ex:IronMan) <-
	    ex:ironMan ex:numberOfSports ?n .
	    (\\\SPARQL  
	         (Select ?x     
	         Where {    
	             ?x ex:playSport ?y .   
	         }  
	         group by ?x    
	         having (count(1) >=&n)
	     \\\SPARQL). 

To make reference to an external variable in a Sparql command, a variable instantiated in a rule term, we use the symbol "&". 

Note: in example above, "&n" in sparql command makes reference to "?n" in the rule term. 




1. Holger Knublauch, James A. Hendler, Kingsley Idehen, Spin - overview and motivation, 2011, http://www.w3.org/Submission/2011/SUBM-spin-overview-20110222/.
2. Holger Knublauch. Spin - SPARQL syntax, 2011, http://www.w3.org/Submission/2011/SUBM-spin-SPARQL-20110222/. 
3. Spin - SPARQL inference notation, http://spinrdf.org/. 

