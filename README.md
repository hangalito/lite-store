# LiteStore

LiteStore is a lightweight Java library for object persistence, designed as an alternative to SQLite. It allows developers to store and retrieve Java objects directly from the filesystem, with no need to write SQL or configure a full database engine.

## Features

- Store and retrieve Java objects using simple annotations
- No SQL or external DB required
- Custom annotations: `@Storage` and `@Key`
- Indexing by any field using Java reflection
- Efficient read/write using `ObjectOutputStream` and `RandomAccessFile`

## Getting Started

### Requirements

- Java 17 or later

### Installation

Clone the development branch:

```bash
git clone -b dev https://github.com/hangalito/lite-store.git
```

## Example Usage

```java
@Storage
public class Pet implements Serializable {
    @Key
    private String id;
    private String name;
    private int age;

    // Constructor, getters, and setters
}
```

```java
import dev.hangalito.LocationService;
import dev.hangalito.Datasource;

void main() {
    Datasource<Pet, String> ds = new Datasource<>(LocationService.getInstance());
    ds.init(Pet.class);

    // Save the entity
    ds.save(new Pet("1", "Rex", 4));

    // Loads the entity
    Optional<Pet> optional = ds.load("1");
    // ... work on optional

    // List all
    List<Pet> allPets = ds.findAll();

    // Create an index on name field
    ds.index("name");
    List<Pet> petsNamedRex = ds.findBy("name", "Rex");
}
```

## How It Works

Objects annotated with @Storage and a @Key are serialized and saved to disk using ObjectOutputStream. A lightweight indexing mechanism stores each object's byte offset and size in the file, allowing fast access with RandomAccessFile. You can also create additional indexes based on any field using reflection.

## Project Structure

    Datasource<S, K>: Main API for working with stored objects

    @Storage: Marks a class as persistable

    @Key: Identifies the primary key field 
