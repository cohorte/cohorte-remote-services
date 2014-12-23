Cohorte - Remote Services (Java)
################################

This project is an implementation of the OSGi Remote Services specification.
It is uses JSON-RPC as the transport protocol and is based on the
`Cohorte Jabsorb Fork <https://github.com/isandlaTech/cohorte-org.jabsorb.ng>`_.

The specification of OSGi Remote Services is available in the
`OSGi Enterprise specification <http://www.osgi.org/download/r5/osgi.enterprise-5.0.0.pdf>`_, chapter 100.
The specification of endpoint description beans is given in the
`OSGi Compendium specification <http://www.osgi.org/download/r5/osgi.cmpn-5.0.0.pdf>`_, chapter 122.

For more information, see the `wiki <https://github.com/isandlaTech/cohorte-remote-services/wiki>`_.

Compilation
***********

This project is built using Maven 3.

First, you'll need to install the Jabsorb fork artifact:

.. code-block:: bash

   $ git clone https://github.com/isandlaTech/cohorte-org.jabsorb.ng.git
   $ cd cohorte-org.jabsorb.ng
   $ mvn clean install
   $ cd ..


Then, you'll be able to build this project:

.. code-block:: bash

   $ git clone https://github.com/isandlaTech/cohorte-remote-services.git
   $ cd cohorte-remote-services
   $ mvn clean install
   $ cd ..

Produced bundles will be in the *target* directory of each sub-project
directory.


License
*******

This project is released under the terms of the
`Apache Software License 2.0 <http://www.apache.org/licenses/LICENSE-2.0)>`_.
