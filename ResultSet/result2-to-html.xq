xquery version "1.0";
(:

  XQuery script to format SPARQL Query Results XML Format into xhtml

  Copyright © 2004, 2005 World Wide Web Consortium, (Massachusetts
  Institute of Technology, European Research Consortium for
  Informatics and Mathematics, Keio University). All Rights
  Reserved. This work is distributed under the W3C® Software
  License [1] in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE.

  [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231

  $Id$

:)


declare namespace res="http://www.w3.org/2005/sparql-results#";
declare default element namespace "http://www.w3.org/1999/xhtml";


(: URI of input SPARQL Query Results document :)
declare variable $results-doc := doc( "output.xml" );


(: How to set serialization parameters? :)

(: doctype-system = "-//W3C//DTD XHTML 1.0 Transitional//EN" :)
(: doctype-public = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> :)


declare variable $variableNames :=
   for $element in $results-doc/res:sparql/res:head/res:variable
     return string( $element/@name )
;


declare function local:head($head as node()) {
  <div> 
    <h2>Header</h2> 
  {
    for $link in $head/res:link
      return <p>Link to { string( $link/@href ) }</p>
  } </div>
};


declare function local:boolean-result($bool as node()) {
  <div>
    <h2>Boolean Result</h2>
    <p>Value: { string ($bool) } </p>
  </div>
};


declare function local:vb-result($vbr as node()) {
  <div>
    <h2>Variable Bindings Results</h2>

    <p>Ordered: { string( $vbr/@ordered ) } </p>
    <p>Distinct: { string( $vbr/@distinct ) } </p>

    <table border="1">
<tr> {
  for $name in $variableNames
    return <th>{$name}</th>
} </tr>


{ for $result in $vbr/res:result
    return
<tr> {
  for $name in $variableNames 
    let $item := $result/res:binding[@name = $name]
    return  
	<td> {
	  if ($item/res:bnode) then
	     (: blank node value :)
	     ( "nodeID ", $item/res:bnode/text() )
	  else if ($item/res:uri) then 
	     (: URI value :)
	     ( "URI ", $item/res:uri/text() )
	  else if ($item/res:literal/@datatype) then 
	     (: datatyped literal value :)
	     fn:concat ( $item/res:literal, " (datatype ", $item/res:literal/@datatype, ")" )
	  else if ($item/res:literal/@xml:lang) then 
	     (: lang-string :)
	     fn:concat ( $item/res:literal, "@", $item/res:literal/@xml:lang )
	  else if ($item/res:literal/res:unbound) then 
	     (: unbound variable - empty cell :)
	     "[unbound]"
	  else if ( exists($item/res:literal/text()) ) then
	     (: present and not empty :)
	     $item/res:literal/text()
	  else if ( exists($item/res:literal) ) then
	     (: present and empty :)
	     "[empty literal]"
	  else
	     (: unbound variable - empty cell :)
	     "[unbound]"
	 } </td>
  } </tr>

} </table>

</div>
};


document {
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>SPARQL Query Results to XHTML (XQuery)</title>
  </head>
  <body>
    <h1>SPARQL Query Results to XHTML (XQuery)</h1>

{
  local:head($results-doc/res:sparql/res:head)
}

{
if ($results-doc/res:sparql/res:boolean) then 
  local:boolean-result($results-doc/res:sparql/res:boolean)
else
  local:vb-result($results-doc/res:sparql/res:results)
}

  </body>
</html>

}    
