# Configuring Fuseki

Config files

Fuseki version 2 in

## Relationship to Fuseki 1 configuration

Configurations from Fuseki 1, wheer all daatset and server setup is in a
single configuration file, will stil work.  It is less flexible
(you can't restart these services after stopping them in a running server).

To convert a Fuseki 1 configuration setup to Fuseki 2 style:

1. Move each data service assembler and put in it's own file under `FUSEKI_BASE/configuration/`

