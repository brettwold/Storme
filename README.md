# Storme

Storme is an ultra simple object storage implementation specifically for use with SQLite on Android.

It is intended to allow users to store simple java objects into a SQLite DB. It does not have any
functionality to handle joins or object hierarchy of any type and does NOT handle database upgrades.
Currently if you upgrade the DB version number for you project any existing data stored will simply be
dropped. You have been warned. It is not intended to be a fully featured ORM for Android, if this is 
what you need then I recommend you take a look at some of the other existing ORMs for this purpose such 
as SugarORM, RushORM or ActiveAndroid.

If however, like me, what you need is a super simple way just to store a bunch of objects into a table
then Storme may be for you.
 
Another reason for the existence of Storme is that it is fully unit testable outside of a device or 
emulator using Robolectric.

## Installation

Grab the latest jar file from GitHub and place into the libs folder in your project.

## Usage

First of all you'll need to create the object you want to store. All you need to do is extend `StormeBaseModel`
and declare your variables. Currently Storme supports the variable types int, long, float, double, boolean, Date
and String.

```java

public class MyStorableObject extends StormeBaseModel {

    private int integerField;
    private long longField;
    private float floatField;
    private double doubleField;
    private boolean booleanField;
    private Date dateField;
    private String stringField;
}

```

Next you need to create your factory or store object. To make this totally easy there's a handy `StormeBaseHelper`
that will do all the heavy lifting for you. Below is an example of the minimum you need to do.

```java
public class MyStorableObjectStore extends StormeBaseHelper {

    private static final String DATABASE_NAME = "my_records";
    private static final String TABLE_PREFIX = "storme_";
    private static final int DATABASE_VERSION = 1;

    private static List<Class<? extends StormeModel>> DB_CLASSES = new ArrayList<Class<? extends StormeModel>>();

    static {
        DB_CLASSES.add(MyStorableObject.class);
    }

    public MyStorableObjectStore(Context context)
    {
        super(context, DATABASE_NAME, DATABASE_VERSION, TABLE_PREFIX, DB_CLASSES);
    }
}
```

The `StormeBaseHelper` contains the standard set of get, find, count, save methods you'd expect for any DAO 
and includes the ability to set where, order by and limit clauses.

As you can see from the example we are passing a `List` of storable model types to Storme in the `DB_CLASSES`
object. This allows you to create multiple tables for multiple object types. As mentioned above there
is no support of relationships between these models but of course you can build that in if you need it.

Once you have these two objects setup you can then use the `MyStorableObjectStore` class to save and get
your model objects to/from SQLite.

```java

MyStorableObject record = new MyStorableObject();

// setup your record object

// get hold of the store passing your application/activity context
MyStorableObjectStore store = new MyStorableObjectStore(context);

// save your record
store.save(MyStorableObject.class, record);
long recordId = record.getId();

...

// get your object back later
MyStorableObject savedResult = store.get(MyStorableObject.class, recordId);

```


