How to build the Jena Maven artifacts:

1. Run the default ant target to copy the single Jena source tree
into two separate trees corresponding to jena and jenatest.

2. If necessary, edit the version number in the pom.xml in both
jena/ *AND* jenatest/

3. cd jena ; mvn install
to generate the Jena artifact in the local repository (by default:
~/.m2/repository/com/hp/hpl/jena/jena/${version}

4. cd ../jenatest ; mvn install
to repeat for the jenatest artifact.
