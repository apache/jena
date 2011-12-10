Apache Jena - single module instructions
========================================

This file contain the instructions for building this module from the
source-release artifact.  The build instructions to create the whole of
this Jena release are in the apache-jena-VER.zip file.

The build process assumes the dependencies of this module are available in
the local maven repository.

jena-top -> jena-iri -> jena-core -> jena-arq -> apache-jena (the download).

The instructions are the same for each jena-* module.

We assume you are building "jena-zzz" in these instructions.

== Download

Download the "source-release" zip file, and also the associated
signatures and checksums (file extensions .asc, .md5, .sha1).

http://www.apache.org/dist/incubator/dist/

== Verify the signature

Get the public keys for the signature.  The file KEYS in the distribution
area contains the keys of the release managers.  These only need to be
imported once.  You can check the signatures at http://pgp.mit.edu/

(For Gnu PrivacyGuard)

    gpg --import < KEYS

The file with extension .asc contains the 

For the zip file of the zzz module:

    gpg --verify jena-zzz-VER-incubator-source-release.zip.asc

== Verify a checksum

The .md5 and .sha1 files contain the MD5 and SHA1 checksum of the file
respectively.  Calculate the checksum on the downloaded file 

Exampale (linux):

    md5sum jena-zzz-VER-incubator-source-release.zip

== Unpack the file.

   unzip -q jena-zzz-VER-incubator-source-release.zip

will create a directory "jena-zzz-VER-incubator" with the files needed to
recreate the distribution.

   cd jena-zzz-VER-incubator

== Ensure the dependecies are available

Either build the previous modules in the dependency chain or get them from
a public maven repository:

    mvn dependency:resolve

== Build

To build the artifacts for this module:

    mvn clean package

or to make them available to other projscts on the local machine:

    mvn clean install

-------------------------------------------------------
Please do hestiate in contacting the Jena developers:
  jena-users@incubator.apache.org
  jena-dev@incubator.apache.org
