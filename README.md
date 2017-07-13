Prefman
============

Prefman is an Android library generates **PreferenceManager** code.

```java
@PrefMan(name = "MyPref", mode = Context.MODE_PRIVATE)
class MyPreference {
    boolean booleanValue;
    int intValue;
    float floatValue;
    long longValue;
    
    // you can define default value like stringValue
    String stringValue = "default_value";
    
    Set<String> setStringValue;
    String[] stringArrValue;
}
```
To persist a field, Prefman must have access to it. You can not make a field private or protected.


Annotation processor automatically generates `XXXManager` class after rebuild the project.

You can use generated `putXXX`, `getXXX`, `removeXXX` methods.

```java
MyPreferenceManager prefManager = MyPreferenceManager.getManager(this);

// put method
prefManager.putBooleanValue(true);

// get method
Getter<Boolean> getterBooleanValue = prefManager.getBooleanValue();
boolean booleanValue = getterBooleanValue.asValue();
Observable<Boolean> observableBooleanValue = getterBooleanValue.asObservable(); // RxJava Observable

// remove method
prefManager.removeBooleanValue(); 
```

### Prefmodel
You can use your model class with **`@PrefModel`**

By default, Prefman creates a column for each field that's defined in the model. If an model has fields that you don't want to persist, you can annotate them using `@Ignore`

```java
@PrefModel
public class User {
  int id;
  String firstName;
  String lastName;
  
  @Ignore
  int age;
}
```

```java
@PrefMan
class MyPreference {
    User user;
    ...
}
```

### Nested
```java
@PrefModel
class Address {
    public String street;
    public String state;
    public String city;
    public int postCode;
}

@PrefModel
public class User {
  int id;
  String firstName;
  String lastName;
  
  Address address;
}
```

Download[ ![Download](https://api.bintray.com/packages/chongos/maven/prefman/images/download.svg) ](https://bintray.com/chongos/maven/prefman/_latestVersion)
--------

```groovy
dependencies {
  compile 'com.chongos:prefman:${latest.version}'
  annotationProcessor 'com.chongos:prefman-compiler:${latest.version}'
}
```

License
-------

    Copyright 2017 Chaiyut Nacharoen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
