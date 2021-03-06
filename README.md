cucumber-rest plugin
======================

This small library adds a common cucumber DSL for writing cucumber tests to test restfull endpoints.  The library
provides the following:

* Step definition library
* Communication library (groovy http builder)
* Grails aware aimed to work with hauner/grails-cucumber

The key component of this library is a consistent way of writing gherkin syntax specifically around the common actions
associated with setting up, transmitting and interrogating restfull transactions.

Currently the library does not support XML, it supports JSON.  I plan to add XML shortly.

Installation 
=============

The installation steps are required if you want to compile from sources.

```bash
git clone http://github.com/grantcurrey/cucumber-rest
cd cucumber-rest
gradle install
```

_Note_
Although you will not upload anything with gradle install, it is likely to still complain about not being able to find
the mavenUser and mavenPassword properties.  If this is the case, do the following:

```bash
echo "mavenUser=user" >> ~/.gradle/gradle.properties
echo "mavenPassword=password" >> ~/.gradle/gradle.properties
```

Alternatively, you can use the binary version already published to maven by adding the following repository to your
maven configuration:

```xml
<repositories>
    <repository>
        <id>grantcurrey-maven2</id>
        <url>https://github.com/grantcurrey/maven2/raw/master/</url>
    </repository>
</repositories>
```

In your project simply add the dependency ```"com.wotifgroup.cucumber-rest:cucumber-rest:1.1.17"```

Config / Usage
===============

There are a number of ways to bootstrap a cucumber test suite. This guide will assume you are using cucumber-junit.  But
the key concepts presented are applicable to all frameworks.

Given the following example:

```java
package com.wotifgroup.cucumber.example;
 
import org.junit.runner.RunWith;
import cucumber.junit.Cucumber;
 
@RunWith(Cucumber.class)
@Cucumber.Options(format={"pretty", "html:target/cucumber"}, glue={"com.wotifgroup.cucumber.jsonglue"})
public class RunTests {
}
```

Note, the Cucumber.Options glue property set above.  In order to leverage the hooks and step definitions provided by
this libary you need to tell the cucumber jvm where to look for the definitions.

Test Structure
---------------

Testing resource files, ```someRequest.json``` are assumed to be located in ```src/test/resources/json```.  This is not
yet configurable.

Grails Users
------------

When your cucumber tests startup, a Hooks file executes which interogates your project and determines if you are 
using grails or not.  If you are, it reads your Config.groovy file and sets the base url automaticly for you based on
the ```conf.grails.serverUrl``` setting (more in the Provided Step Definitions section).  

You can override this using the ```the base url is "http://localhost:8080/someurl"``` in your gherkin file.

Testing resource files, ```someRequest.json``` are assume to be located in ```test/cucumber/json```.  

Usage
======

Activating the plugin
------------------------

You need tag your scenario of feature with ```@cucumberRest```

The library ships with a hooks file which registers a groovy binding updater based on the tag above.  If you do not
tag your scenario or feature with ```@cucumberRest``` the dsl outlined below will not function correctly.


Step definition Overview
-------------------------

**The following are intended to be used in a Background action:**

- ```the date format is "(.*)"```
- ```the base url is \"(.*)\"```
- ```the ssl keystore is \"(.*)\" and the keystore password is \"(.*)\"```
- ```the ssl truststore is \"(.*)\" and the truststore password is \"(.*)\"```

**The remaining are intended to be used anywhere in a Gherkin file:**

- ```I am sending a \"(.*)\"```
- ```I set the request \"(.*)\" property to (.*)```
- ```I set the request header \"(.*)\" to (.*)```
- ```I add \"(.*)\" to the request \"(.*)\" property```
- ```I (remove|clear|nullify) the request \"(.*)\" property```
- ```I post the "([\w ]+)" to "(.*)"```
- ```I get the "([\w ]+)" to "(.*)"```
- ```I call the "?([^"]*)"? resource``` - This is the same as the step above, only making more sense (GET)
- ```I put the "([\w ]+)" to "(.*)"```
- ```I delete the "([\w ]+)" to "(.*)"```
- ```the( http)? response code is "([0-9]*)"```
- ```the response matches ("?.*"?)```
- ```the response equals ("?.*"?)```
- ```the response contains "?(.*)"?``` 
- ```the response header \"(.*)\" has a value```
- ```the response header \"(.*)\" is ("?.*"?)```
- ```the response header \"(.*)\" matches ("?.*"?)```
- ```the response (property|element) \"(.*)\" matches ("?.*"?)```
- ```the response (property|element) \"(.*)\" has a value```
- ```the response (property|element) "(.*)" equals ("?.*"?)```
- ```the response (property|element) \"(.*)\" is ("?.*"?)```
- ```the response (property|element) \"(.*)\" contains "?(.*)"?```

DSL Explanation
-----------------

```I am sending a \"(.*)\"``` This step definition allows you to load up a JSON file ready to be manipulated and ultimately sent to a server.

Take the following example ```I am sending a "example_post```

The system will look in the test resource folder (outlined earlier) for a file called ```example_post.json```

Once a JSON template has been loaded, you can use any of the following definitions to manipulate the resource:

- ```I set the request \"(.*)\" property to (.*)```
- ```I add \"(.*)\" to the request \"(.*)\" property```
- ```I (remove|clear|nullify) the request \"(.*)\" property```

An example of using one of the above definitions is ```I set the request "parent.child" property to test``` or
```I remove the request "grandparent.parent.child" property```

Once you have configured the message you wish to send, you can use one of the standard http verbs:

- ```I post the "([\w ]+)" to "(.*)"```
- ```I get the "([\w ]+)" to "(.*)"```
- ```I put the "([\w ]+)" to "(.*)"```
- ```I delete the "([\w ]+)" to "(.*)"```

Note, ```I post the "([\w ]+)" to "(.*)"``` looks like ```I post the "example_post" to "/api/resource"``` in practice.

Once you have sent your message, you need to interrogate the response, this can be used with one of the following:

- ```the http response code is "([0-9]*)"```
- ```the response property \"(.*)\" has a value```
- ```the response property "(.*)" equals ("?.*"?)```
- ```the response property \"(.*)\" is (\"?.*\"?)```
- ```the response property \"(.*)\" contains \"?(.*)\"?```

Where you can assert a property has a value by ```the response property "parent.child" is "test"```

Special Key Words
------------------
When testing response properties or setting request properties, the following special keywords can be used:

- **today** - sets the value to todays date (use the ```tthe date format is "(.*)"``` to set the date format to use)
- **tomorrow** - sets the value to tomorrows date
- **yesterday** - sets the value to yesterdays date
- **random** - generates a new random uuid
- **"sometext"** - The parser assumes the value is a string
- **12321** - the parser assumes the value is a number
- **lastResponse.{{somepath}}** - The last response is available when setting values on a subsequent request
- **lastResponseHeaders.{{somekey}}** - The last response headers are available when setting value son a subsequent request

Note - Special key words can also be used when setting resource paths (ie, GET, PUT, POST and DELETE steps), provided
the are wraped in braces { }.  An example of such a call is ```When I call the "{lastResponseHeaders.Location}" resource```.  
Given the Location heaer is set to www.google.com, the result will be a GET request to http://www.google.com.

Full Example
-------------

```gherkin
@cucumberRest
Scenario: Cucumber Rest Example
  Given I am sending a "cucumber_rest_example"
  And I set the request "parent.child" property to 1234
  And I set the request "parent.date" property to today
  And I clear the request "parent.nextChild" property
  And I post the "cucumber_rest_example" to "/api/cucumber-rest-test"
  And the http response code is "200"
  When I am sending a "cucumber_followup_request_example"
  And I set the request "parent.child" property to lastResponse.someParent.someChild
  And I set the request "parent.nextChild" property to lastResponseHeaders.someValue
  And the response property "someProperty" is 1
  And the response property "parent.anotherProperty" is "abcd"
```

