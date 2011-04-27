INSERT
{
   GRAPH <http://mycompany.com/MyGraph>
   {
       _:b6 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://mycompany.com/exampleOntology#MyClassA> .
       _:b6 <http://www.w3.org/2000/01/rdf-schema#label> ?label .
       <http://mycompany.com/exampleOntology#MyClassB> <http://mycompany.com/exampleOntology#myPropertyX> _:b6 .
   }
}
WHERE
{ 
  BIND("my new label" AS ?label)
}
