PREFIX rdf:       <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX pmo:       <http://www.bimtoolset.org/ontologies/pmo.owl#>
PREFIX bim:       <http://www.bimtoolset.org/ontologies/IntUBE-EnergyBIM.owl#>
PREFIX gbx:       <http://www.bimtoolset.org/data/Byers-ori.owl#>
PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>
 
CREATE SILENT GRAPH <http://www.bimtoolset.org/ontologies/IntUBE-EnergyBIM-Data.owl> ;
 
INSERT # INTO <http://www.bimtoolset.org/ontologies/IntUBE-EnergyBIM-Data.owl>
{
    GRAPH <http://www.bimtoolset.org/ontologies/IntUBE-EnergyBIM-Data.owl>
   {
   _:a a bim:ProjectManager;
      bim:currentProject _:b;
      pmo:hasPart_directly _:b.
   _:b a bim:Project;
      bim:name "";
      bim:description "" ;
      bim:latestVersion _:c;
      pmo:hasPart_directly _:c.
   _:c a bim:ProjectVersion;
      bim:name "";
      bim:description "" ;
      bim:creationDate ?Date;
      bim:lastModifiedDate ?Date;
      bim:version "1.0";
      pmo:hasPart_directly [
         a bim:Building ;
         bim:name ?BuildingName ;
         bim:description "" ;
         bim:topFloorHeight "0.0"^^xsd:float ;      
      ].
   }
}
WHERE
{
   GRAPH <http://www.example.com/model>
   {
      [] gbx:id-Building ?BuildingName.
      [] gbx:date-CreatedBy ?Date.
   }
}