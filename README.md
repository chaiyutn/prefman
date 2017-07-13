Prefman
============

Prefman is an Android library generates **PreferenceManager** code.

Usage
--------

```java
@PrefMan
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
### Default value
You can define default value to field
```java
@PrefMan
class MyPreference {

    // define default value
    int intValue = 100;
    ...
}
```
OR
```java
MyPreference myPref = new MyPreference();

// change default intValue field to 100
myPref.intValue = 100;

MyPreferenceManager manager = MyPreferenceManager.getManager(this, myPref);
...
```

### Naming
By default, Prefman uses the class name as the shared preference name. If you want the shared preference to have a different name, set the `name` property of the `@PrefMan` annotation, as shown in the following code snippet:

```java
@PrefMan(name = "MyPref")
class MyPreference {
   ...
}
```
OR
```java
MyPreferenceManager manager = MyPreferenceManager.getManager(context, "MyPref");
```

Prefman sets shared preference with Context.MODE_PRIVATE mode. Also, you can set the `mode` property of the `@PrefMan` annotation, as shown in the following code snippet:

```java
@PrefMan(name = "MyPref", mode = Context.MODE_PRIVATE)
class MyPreference {
   ...
}
```
OR
```java
MyPreferenceManager manager = MyPreferenceManager.getManager(context, "MyPref", Context.MODE_PRIVATE);
```

Model
--------
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

Download  [ ![Download](https://api.bintray.com/packages/chongos/maven/prefman/images/download.svg) ](https://bintray.com/chongos/maven/prefman/_latestVersion)
--------

```groovy
dependencies {
  compile "com.chongos:prefman:${latest.version}"
  annotationProcessor "com.chongos:prefman-compiler:${latest.version}"
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
