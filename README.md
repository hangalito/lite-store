<div style="text-align: center;">LiteStore</div>
============================================================
<div style="text-align: center; font-size: 18px">The Solution for Standalone Application Persistence</div>
------------------------------------------------------------

LiteStore is the solution for standalone application persistence. It allows you to persist your
application-generated data on device. No SQL knowledge is required, it's all Java based.

# Features

- Data persistence
- Data retrieval
- Indexed retrieval
- Data update
- Data deletion

# How it works

LiteStore creates a **.dat** file with the name your persisting class in the user application data
directory. It then creates an index of these data to facilitate the retrieval of these data.
By default, only the primary key of the storable classes are indexed but you can also create custom
indexes for an improved and customized querying.

# Code Examples

## Preparing a class for storage

To store you application data, you must annotate the classes whose instance you want to be persisted
with the **@Storable** and the primary key with **@Key**.

````java
import dev.hangalito.annotations.Key;
import dev.hangalito.annotations.Storable;

@Storable
public class MyClass {
    @Key
    private int id;
    // other fields
}
````

## Storing the storable instances

You start by initializing the datasource after its instantiation, then you pass the instance you
want to be persisted. Note that you must initialize the datasource before calling other methods.
By doing so you ensure that the index is loaded and you have the correct data. Calling a
persistence-related method (save, update, delete, findAll, and findBy) will result in
**DatasourceNotInitializedException**

````java
import dev.hangalito.storage.Datasource;

void main() {
    Datasource<MyClass, Integer> ds = new Datasource<>();
    ds.init(MyClass.class);

    MyClass mc = new MyClass();
    ds.save(mc);
}
````

## Retrieving all data

You can retrieve all your data by calling the *findAll* method. This method will never return null,
if no there is no saved data it will simply return an empty list.

````java
import dev.hangalito.storage.Datasource;

void main() {
    Datasource<MyClass, Integer> ds = new Datasource<>();
    ds.init(MyClass.class);
    List<MyClass> myClassList = ds.findAll();
}
````

## Creating and Retrieving by Custom Fields

To query by custom fields, say, by name on a *User* storable class, you start by creating an index
on this field

````java
import dev.hangalito.storage.Datasource;

void main() {
    Datasource<User, Long> ds = new Datasource<>();
    ds.init(User.class);
    ds.createIndex("name");
}
````

then you can query in this field the following way

````java
void main() {
    Datasource<User, Long> ds = new Datasource<>();
    ds.init(User.class);
    ds.findBy("name", "John");
}   
````

## Updating Data

LiteStore also lets you update your saved instances. You can do it as shown below

````java
import dev.hangalito.storage.Datasource;

void main() {
    Datasource<User, Long> ds = new Datasource<>();
    ds.init(User.class);

    User user = new User();
    user.setName("John");
    ds.save(user);

    user.setName("Dee");
    ds.update(user.getId(), user);
}
````

## Deleting Data

You can delete data simply by calling the `delete` method in the `Datasource` passing the instance
you want to delete.

````java
import dev.hangalito.storage.Datasource;

void main() {
    Datasource<User, Long> ds = new Datasource<>();
    ds.init(User.class);

    ds.findByIndex(100L).ifPresent(user -> ds.delete(user));
}
````

This code snippet retrieves the user with ID of 100 and then deletes it from the datasource.
