Apache Jena - jena-larq
=======================

This file contain the instructions for building this module from the
source-release artifact.

== Download

Download the "source-release" zip file, and also the associated
signatures and checksums (file extensions .asc, .md5, .sha1).

http://www.apache.org/dist/jena/

== Verify the signature

Get the public keys for the signature.  The file KEYS in the distribution
area contains the keys of the release managers.  These only need to be
imported once.  You can check the signatures at http://pgp.mit.edu/

(For Gnu PrivacyGuard)

    gpg --import < KEYS

The file with extension .asc contains the 

For the zip file of the zzz module:

    gpg --verify jena-zzz-VER-source-release.zip.asc

== Verify a checksum

The .md5 and .sha1 files contain the MD5 and SHA1 checksum of the file
respectively.  Calculate the checksum on the downloaded file 

Example (linux):

    md5sum jena-zzz-VER-source-release.zip

== Unpack the file.

   unzip -q jena-zzz-VER-source-release.zip

will create a directory "jena-zzz-VER" with the files needed to
recreate the distribution.

   cd jena-zzz-VER

== Build

To build the artifacts for this module:

    mvn clean package

or to make them available to other projects on the local machine:

    mvn clean install

-------------------------------------------------------
If you have any questions, please do not hesitate in contacting the Jena project:
  users@jena.apache.org
  dev@jena.apache.org
