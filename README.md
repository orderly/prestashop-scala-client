prestasac - a Scala library for accessing the PrestaShop Web Service.

prestasac (PrestaShop Scala API Client) allows you to access the PrestaShop Web Service from Scala. Learn more about the PrestaShop Web Service from the [Official Documentation](http://wiki.prestashop.com/display/enEN/Using+the+REST+webservice) ([PDF version](http://wiki.prestashop.com/download/attachments/720902/CRUD+Tutorial+EN.pdf?version=1&modificationDate=1301486324000)).

prestasac is a direct port of the official PrestaShop PHP API Client, PSWebServiceLibrary.php. We keep the latest version of this library in the /php folder for reference. Similar to PSWebServiceLibrary.php, prestasac is a thin wrapper around the PrestaShop Web Service: it takes care of making the call to your PrestaShop instance's Web Service, supports the Web Service's HTTP-based CRUD operations (handling any errors) and then returns the XML ready for you to work with in Scala.

#Credits

Thanks to Prestashop SA for their great work on PrestaShop and on the PHP reference library, PSWebServiceLibrary.php

#Installation

The latest build of prestasac is always available as a .jar from the Downloads dropdown in GitHub.

#Quick Start

First download and install the prestasac jar. Now fire up scala and load the jar into your classpath (changing the paths as appropriate):

    scala> :cp [YOUR JAR PATH HERE]/prestasac-0.0.1.jar

Now you can import the PrestaShopWebService class:

    scala> import co.orderly.prestasac.PrestaShopWebService

Create a PrestaShopWebService (changing your PrestaShop credentials):

### Rest of quick start still to write ###

#API Documentation

Documentation for the PrestaShop Web Service can be found on the PrestaShop wiki:
[Using the REST webservice](http://wiki.prestashop.com/display/enEN/Using+the+REST+webservice)

# Differences and Gotchas

There are a few differences from the PHP library, PSWebServiceLibrary.php:

* prestasac needs the API URL for configuration, not the shop URL. Passing a trailing slash on the API URL is optional
* Rather than internally-overloaded methods taking generic parameters, prestasac has different methods for each possible set of parameters

#Forking 

Forking from GitHub and making changes to the code is easy if you use sbt:

    git clone git://github.com/orderly/prestasac
    vi src/main/scala/example.scala
    << Update example.scala to have your own PrestaShop credentials >>     
    cd ..
    sbt
    > run

You should see prestasac compiling main sources, running ExampleOperations and then displaying some XML results.

#Copyright and License

Copyright 2012 Orderly Ltd

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
