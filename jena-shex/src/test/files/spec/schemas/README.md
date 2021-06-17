# ShEx Test Schemas

ShEx tests are organized to simplify testing and implementation reports by testing orthogonal features in separate tests.
Each test focuses on a minimal number of features.
Test schemas are pairs of a ShExC syntax and a ShExJ file.
Validation tests can be performed on either of these files.
ShExC syntax tests appear in the validation manifest in order to verify the parsed structure.

## Naming Convention

Test filenames attempt to capture the features of the test.
For instance, [1doubleMaxexclusiveDECIMALintLeadTrail](1doubleMaxexclusiveDECIMALintLeadTrail.shex) contains: `\<S1\> { \<p1\> xsd:double MAXEXCLUSIVE 05.00 }`.
The name tells us that the tested shape has:
* 1 triple constraint with a
* datatype of xsd:double
* a MAXEXCLUSIVE facet
* with an argument encoded as an xsd:decimal (e.g. 5.0)
* which is numerically equivalent to an integer (5)
* and has leading and trailing '0's ("05.00")

An RDF **term type** is represented as either:
* Turtle shorthand syntax: [numbers](https://www.w3.org/TR/turtle/#abbrev) ("decimal", "double", "integer") or [booleans](https://www.w3.org/TR/turtle/#h4_booleans) ("true", "false")
* Turtle typed literal: xsd:byte, xsd:decimal

When representing a supplied value, shorthand numbers are capitolized ("DECIMAL", "DOUBLE", "INTEGER").

#### NC - Node constraints

* **nodeKind**: IRI, BNODE
* **stringFacet** termType?: Length, MinLength, MaxLength, Pattern. -- `1decimalMaxexclusiveDECIMAL`
* **numericFacet**: {Min,Max}{In,Ex}clusive
* **valueSet**: written "vs" -- `\<S\> {}` c.f. [1focusvsANDdatatype](1focusvsANDdatatype.shex)

#### TE - Triple expression operators

* **Each**: EachOf is represented by a semicolon in ShExC: `:S1 { :p1 .; :p2 . }`. EachOf is usually indicated with just a number as indicated by in "ₙdot" and "ₙNC" in filename components.
* **One**: OneOf is represented by a pipe in ShExC: `:S1 { :p1 .| :p2 . }`.

#### SE - Shape expression operators

* **AND**: ShapeExpr conjunctions are reprented by "AND" in ShExC: `@\<S1\> AND @\<S2\>`, e.g. [1dotRefAND3](1dotRefAND3.shex) in node constraints in both value constraints and focus constraints.
* **OR**: ShapeExpr disjunctions are reprented by "OR" in ShExC: `@\<S1\> OR @\<S2\>`, e.g. [1dotRefOR3](1dotRefOR3.shex) in node constraints in both value constraints and focus constraints.

#### filename components
* 0 - a shape with no triple constraints -- `\<S\> {  }`, c.f. [0](0.shex), [0focusIRI](0focusIRI.shex]
* ₍ₘ₎Ref T?₍ₙ₎ - ₍ₙ₎references to labeled shapes of type T -- `\<S\> {  }` c.f. [1dotRef1](1dotRef1.shex), [1dotRefAND3](1dotRefAND3.shex)
* TRef - reference to T`\<vc\> @\<vc1\> OR @\<vc2\>\\n \<S1\> { \<p1\> @\<vc\> } }` c.f. [1val1vExprRefOR3.shex](1val1vExprRefOR3.shex)
* ₙdot - ₙ triple constraints with wildcard values -- `\<S\> { :p1 .; :p2 . }` c.f. [2dot](2dot.shex)
* ₙNC - ₙ triple constraints with node constraints -- `\<S\> { :p . }` NC types are lower case.
* focusNC - Node constraint on the focus node -- `\<vs\> [ \<v1\> \<v2\> ] AND \<dt1\> ` c.f. [1focusvsANDdatatype](1focusvsANDdatatype.shex)
* A - keyword for '''rdf:type''' -- `<S> { a . }` c.f. [1Adot](1Adot.shex)
* card₍ₘ,ₙ₎|Opt|Plus|Star - repetition `\<S\> { :p1 .{2,*} } ` c.f. [1card2Star](1card2Star.shex)
* datatype - a datatype constraint -- `\<S\> { :p1 \<dt1\> }` c.f. [1datatype](1datatype.shex)
* Annot -
* Semi - extra semicolon separators -- `\<S\> { :p1 .; :p2 .;| :p3 .; }` c.f. [2dotSemiSome1dotSemi](2dotSemiSome1dotSemi.shex)

## Exploration of OneOf and EachOf

The following tests cover 2-levels of combinatorics of OneOf and EachOf constructs.
The column on the right includes the same tests with extra semicolons added wherever possible.

| bare				     | extra ';'s					|
| ---				     | ---						|
| [1dot.shex](1dot)				     | [1dotSemi.shex](1dotSemi)						|
| \<S\> { :p1 . }		     | \<S\> { :p1 .; }					|
| [2dot.shex](2dot)				     | [2dotSemis.shex](2dotSemis)					|
| \<S\> { :p1 .; :p2 . }	     | \<S\> { :p1 .; :p2 .; }				|
| [1dotOr1dot.shex](1dotOr1dot)			     | [1dotSemiOr1dotSemi.shex](1dotSemiOr1dotSemi)				|
| \<S\> { :p1 .\| :p2 . }	     | \<S\> { :p1 .;\| :p2 .; }			|
| [2dotOr1dot.shex](2dotOr1dot)			     | [2dotSemiOr1dotSemi.shex](2dotSemiOr1dotSemi)				|
| \<S\> { :p1 .; :p2 .\| :p3 . }     | \<S\> { :p1 .; :p2 .;\| :p3 .; }			|
| [1dotOr2dot.shex](1dotOr2dot)			     | [1dotSemiOr2dotSemis.shex](1dotSemiOr2dotSemis)				|
| \<S\> { :p1 .\| :p2 .; :p3 . }     | \<S\> { :p1 .;\| :p2 .; :p3 .; }			|
| [open2dotclose.shex](open2dotclose)			     | [open2dotsemisclose.shex](open2dotsemisclose)				|
| \<S\> { (:p1 .; :p2 .) }	     | \<S\> { (:p1 .; :p2 .;); }			|
| [open1dotOr1dotClose.shex](open1dotOr1dotClose)		     | [open1dotSemiOr1dotSemicloseSemi.shex](open1dotSemiOr1dotSemicloseSemi)			|
| \<S\> { (:p1 .\| :p2 .) }	     | \<S\> { (:p1 .;\| :p2 .;); }			|
| [openopen1dotOr1dotclose1dotclose.shex](openopen1dotOr1dotclose1dotclose)   | [openopen1dotSemiOr1dotSemiclose1dotSemicloseSemi.shex](openopen1dotSemiOr1dotSemiclose1dotSemicloseSemi) |
| \<S\> { ((:p1 .\| :p2 .); :p3 .) } | \<S\> { ((:p1 .;\| :p2 .;); :p3 .;); }		|
| [open1dotopen1dotOr1dotcloseclose.shex](open1dotopen1dotOr1dotcloseclose)   | [open1dotopen1dotSemiOr1dotSemicloseSemicloseSemi.shex](open1dotopen1dotSemiOr1dotSemicloseSemicloseSemi) |
| \<S\> { (:p1 .; (:p2 .\| :p3 .)) } | \<S\> { (:p1 .; (:p2 .;\| :p3 .;);); }		|
| [openopen2dotcloseOr1dotclose.shex](openopen2dotcloseOr1dotclose)	     | [openopen2dotSemiscloseOr1dotSemiclose.shex](openopen2dotSemiscloseOr1dotSemiclose)		|
| \<S\> { ((:p1 .; :p2 .)\| :p3 .) } | \<S\> { ((:p1 .; :p2 .;);\| :p3 .;); }		|
| [open1dotOropen2dotcloseclose.shex](open1dotOropen2dotcloseclose)	     | [open1dotSemiOropen2dotSemiscloseclose.shex](open1dotSemiOropen2dotSemiscloseclose)		|
| \<S\> { (:p1 .\| (:p2 .; :p3 .)) } | \<S\> { (:p1 .;\| (:p2 .; :p3 .;);); }		|
| [open2dotOr1dotclose.shex](open2dotOr1dotclose)		     | [open2dotSemisOr1dotSemiclose.shex](open2dotSemisOr1dotSemiclose)			|
| \<S\> { (:p1 .; :p2 .\| :p3 .) }   | \<S\> { (:p1 .; :p2 .;\| :p3 .;); }		|
| [open1dotOr2dotclose.shex](open1dotOr2dotclose)		     | [open1dotSemiOr2dotsemisclose.shex](open1dotSemiOr2dotsemisclose)			|
| \<S\> { (:p1 .\| :p2 .; :p3 .) }   | \<S\> { (:p1 .;\| :p2 .; :p3 .;); }		|

