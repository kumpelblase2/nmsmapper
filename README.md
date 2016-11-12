# nmsmapper
An annotation processor to create NMS wrapper classes to aid with the use of internal minecraft class

Please note that right now this just a few hours of work in and it doesn't represent the expected result and workings that I'm intending.

## Idea

When dealing with NMS (Native Minecraft Server) classes it is a lot of work to keep methods in sync with the decompiled names of those classes.
Most of the times after a version change you have to go through each used method and check if it still does what you think it does. Because they're decompiled, the names of those methods are just random strings and don't really tell anything about what they're supposed to do making it hard to keep track of.

`nmsmapper` tries to help with this by letting you create an interface with properly named methods that you can use to interact with NMS classes. `nmsmapper` will internally create a class that bridges those calls to the NMS instance. This happens at the time of compilation ensuring that if you do make a mistake, the compiler will be able to catch it.

## Usage

### Setup
In oder to use this, you first need to add it to your project:

#### Gradle

```Groovy
compile 'de.eternalwings:nmsmapper:1.0-SNAPSHOT'
```

#### Maven

```XML
<dependency>
	<groupId>de.eternalwings</groupId>
	<artifactId>nmsmapper</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```
 
Then make sure you have Annotation Processors enabled for your project. Please refer to the help page of your IDE for how to set this up. If you're using gradle or maven, you're already good to go.

### Example

`nmsmapper` provides two annotations that you need in order to make a mapping possible: `@NMS` and `@NMSMethod`.
The `@NMS` annotation is used for the interface that you want to mark as a mapping interface and the `@NMSMethod` for a method that you want to map to a method on the NMS class. 

The example below shows how one would create a mapping for the `World` NMS class.
```Java
@NMS("net.minecraft.server.World") // The value describes the target NMS class name
public interface WorldMapping {
	@NMSMethod(value = "i", isField = true) // If we want to map to a field, we can do that too
	List entitiesToTick();

	@NMSMethod("b") // We want to map this method to the `b` method on World
	Block getFirstSurfaceBlock(int p1, int p2);
	
	// More mappings may follow
}
```

We can then use the generated class to interface with a `World` instance:
```Java
WorldMapping myWorld = new WorldMapping$NMS(originalWorld);
myWorld.entitiesToTick();
```

## TODO

- [ ] If multiple methods with same name, try to get the one with the same params
- [ ] Generated class should extend the NMS class
- [ ] Create static methods to get the mapped class from an existing NMS instance