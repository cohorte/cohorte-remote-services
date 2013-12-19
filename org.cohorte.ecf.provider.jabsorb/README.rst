.. Readme for the ECF provider

Cohorte-Jabsorb.ng ECF Provider
###############################

Compilation
***********

This ECF provider depends on ECF (obviously), on
``org.cohorte.remote.utilities`` from this repository and on ``org.jabsorb.ng``
from another repository.

This provider is built using Tycho and depends on Maven artifacts, therefore the
build order matters as both artifacts can't be built in a single run.


Step 1: Get the code
====================

Using any Git client, clone the repositories from:

* https://github.com/isandlaTech/cohorte-org.jabsorb.ng
* https://github.com/isandlaTech/cohorte-remote-services

.. code-block:: bash

   $ git clone https://github.com/isandlaTech/cohorte-org.jabsorb.ng.git
   $ git clone https://github.com/isandlaTech/cohorte-remote-services.git


Step 2: Compile Jabsorb.ng
==========================

The first compilation step is to compile Jabsorb.ng and install it in the
local Maven repository.

.. code-block:: bash

   $ cd cohorte-org.jabsorb.ng
   $ mvn clean install
   $ cd ..


Step 3: Compile Cohorte Remote Services
=======================================

The second compilation step is to install Cohorte Remote Services artifacts in
the local Maven repository.
They depend on Jabsorb.ng too.

.. code-block:: bash

   $ cd cohorte-remote-services
   $ mvn clean install
   

Step 4: Compile the ECF Provider
================================

Finally, compile the ECF Provider the same way.
It is in a subdirectory the Cohorte Remote Services repository.

.. code-block:: bash

   $ cd org.cohorte.ecf.provider.jabsorb
   $ mvn clean install

And your done.

The bundles to install in your ECF application are:

* ``cohorte-org.jabsorb.ng/target/org.jabsorb.ng-1.0.0-SNAPSHOT.jar``
* ``cohorte-remote-services/org.cohorte.remote.utilities/target/org.cohorte.remote.utilities-1.1.0-SNAPSHOT.jar``
* ``cohorte-remote-services/org.cohorte.ecf.provider.jabsorb/target/org.cohorte.ecf.provider.jabsorb-1.0.0-SNAPSHOT.jar``


Usage
*****

The Jabsorb ECF Provider requires an OSGi HTTP Service to be activated.

To use this provider, simply add the following property when register the
service to export:

+------------------------------+-------------+
| Property                     | Value       |
+==============================+=============+
| ``service.exported.configs`` | ecf.jabsorb |
+------------------------------+-------------+
