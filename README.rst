Cohorte - Remote Services (Java)
################################

This project is an implementation of the OSGi Remote Services specification.
It is uses JSON-RPC as the transport protocol and is based on the
`Cohorte Jabsorb Fork <https://github.com/isandlaTech/cohorte-org.jabsorb.ng>`_.


Compilation
***********

This project is built using Maven 3.

First, you'll need to install the Jabsorb fork artifact:

  $ git clone https://github.com/isandlaTech/cohorte-org.jabsorb.ng.git
  $ cd cohorte-org.jabsorb.ng
  $ mvn clean install
  $ cd ..


Then, you'll be able to build this project:

  $ git clone https://github.com/isandlaTech/cohorte-remote-services.git
  $ cd cohorte-remote-services
  $ mvn clean install
  $ cd ..

Produced bundles will be in the *target* directory of each sub-project
directory.


License
*******

This project is released under the Apache License 2.0
