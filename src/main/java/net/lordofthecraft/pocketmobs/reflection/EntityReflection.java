package net.lordofthecraft.pocketmobs.reflection;

import org.bson.internal.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.UUID;

public class EntityReflection {

    // Getting the craftbukkit package prefix, needed for fully qualified class names like net.lordofthecraft.pocketmobs.reflection.EntityReflection
    final private static String CRAFTBUKKIT_PATH = "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".";
    // Getting the minecraft package prefix, for the same reason as above
    final private static String PATH = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".";
    private static Class<?> NBTBase;
    private static Method getNMSEntity;
    private static Method loadEntityFromNBT;
    private static Constructor<?> compoundConstructor;
    private static Constructor<?> listConstructor;
    private static Constructor<?> doubleConstructor;
    private static Method saveEntityToJson;
    private static Method setCompoundUUID;
    private static Method setCompoundFloat;
    private static Method setBase;
    private static Method listAdd;

    private static Method streamToolsLoadCompoundFromInput;
    private static Method streamToolsWriteCompoundToOutput;

    static {
        try {
            // This is the NBTTagCompound, basically a big JSON object that minecraft uses to save entities and itemstacks and stuff
            Class<?> NBTTagCompound = Class.forName(PATH + "NBTTagCompound");
            // This is the NBTTagList, basically a JSON array. If we need to store a list of object values, this is needed
            Class<?> NBTTagList = Class.forName(PATH + "NBTTagList");
            // This is the base of NBT tags, essentially the generic version of all NBT tags that they extend.
            NBTBase = Class.forName(PATH + "NBTBase");
            // This is the class used for saving/storing doubles in NBTTagCompounds
            Class<?> NBTDouble = Class.forName(PATH + "NBTTagDouble");
            // This is the minecraft entity object, the underlying power behind entity logic. Needed for saving them to a compound
            Class<?> NMSEntity = Class.forName(PATH + "Entity");
            // This is the wrapper around the minecraft entity, which is the impltmeentation of the Bukkit Entity interface. Needed for some non-exposed methods in the Bukkit API
            Class<?> craftBukkitEntity = Class.forName(CRAFTBUKKIT_PATH + "entity.CraftEntity");
            // This class is weird, but basically to properly serialize and deserialize the NBTTagCompound this utility file located in Minecraft's source is needed
            Class<?> NBTCompressedStreamTools = Class.forName(PATH + "NBTCompressedStreamTools");
            // This method on compound is where we set floats, and we'll later use this to set the entities health
            setCompoundFloat = NBTTagCompound.getMethod("setFloat", String.class, float.class);
            // This is the method for adding new NBT tags to an NBTTagCompound
            setBase = NBTTagCompound.getMethod("set", String.class, NBTBase);
            // This is the method for adding a new array list to the NBTTagCompound
            listAdd = NBTTagList.getMethod("add", NBTBase);

            // Here we have a method that doesn't have a proper method name. The above are clearly "add" and "set" functions, while this one might be named
            // aD(). So, we have to do a bit of digging. First, we loop through all methods on the Minecraft Entity class
            for (Method method : NMSEntity.getMethods()) {
                // Here we go through the arguments the method accepts. For example, myMethod(double d, Player p) would have two "generic parameter types", Double.class and Player.class
                // We can use this information to find a very specific method that we know what values it takes
                for (Type type : method.getGenericParameterTypes()) {
                    // Here, we check to see if this method accepts a tagcompound and then we check what it returns - if it returns NOTHING (or void) AND accepts an NBTTagCompound we know
                    // that this method is what minecraft uses to write entities to an NBTTagCompound!
                    // This might look like public void ab(NBTTagCompound compound)
                    if (type.getTypeName().equalsIgnoreCase(NBTTagCompound.getTypeName())
                            && method.getReturnType().equals(Void.TYPE)) {

                        // Now that we've found the method we want, store it and let's move on
                        loadEntityFromNBT = method;
                        break;
                    }
                }
            }

            // Here, just like above, we're going through the methods of the TagCompound class. There are a few methods we need that also don't have usable names
            for (Method method : NBTTagCompound.getMethods()) {
                boolean stringMatch = false;
                // Looping through the method parameters
                for (Type type : method.getGenericParameterTypes()) {
                    // So, basically what this is needed for is the method that we need accepts two variables; a String (the key value for an object) and a UUID (the value for the key)
                    // As we're looping individually through parameters, we're going to mark when the first argument is a String and then check if the next one is a UUID
                    // That might look like this: public void cb(String name, UUID value)
                    if (type.getTypeName().equalsIgnoreCase(String.class.getTypeName())
                            && !stringMatch) {
                        stringMatch = true;
                    }

                    // If we've found a method that accepts a string then a UUID and returns nothing then we've found the method we were looking for
                    if (stringMatch
                            && type.getTypeName().equalsIgnoreCase(UUID.class.getTypeName())
                            && method.getReturnType().equals(Void.TYPE)) {
                        setCompoundUUID = method;
                        break;
                    }
                }
            }

            // Again, same as above. We're locating a specific, non-named method
            for (Method method : NBTCompressedStreamTools.getMethods()) {
                // This is a bit more complicated as we have several arguments we need here. So we're gathering an array of all the parameter types to check it at once
                Type[] parameterTypes = method.getGenericParameterTypes();
                // We're looking for a method that returns nothing, has two parameters, the first parameter is a NBTTagCompound and the second is an OutputStream
                // That might look like this: public void aBd(NBTTagCompound compound, OutputStream stream)
                if (method.getReturnType().equals(Void.TYPE)) {
                    if (parameterTypes.length == 2
                            && parameterTypes[0].getTypeName().equals(NBTTagCompound.getTypeName())
                            && parameterTypes[1].getTypeName().equals(OutputStream.class.getTypeName())) {
                        streamToolsWriteCompoundToOutput = method;
                    }
                    // But we also need another method! This one returns a compound and needs an input stream.
                    // It might look like this: public NBTTagCompound dF(InputStream stream)
                } else if (method.getReturnType().equals(NBTTagCompound)) {
                    if (parameterTypes.length == 1
                            && parameterTypes[0].getTypeName().equals(InputStream.class.getTypeName())) {
                        streamToolsLoadCompoundFromInput = method;
                    }
                }
            }
            // Here, we gather the constructors of a few classes when we need them later
            compoundConstructor = NBTTagCompound.getConstructor();
            listConstructor = NBTTagList.getConstructor();
            doubleConstructor = NBTDouble.getConstructor(double.class);

            // And now we need the actual method to save the entity to a tag compound
            saveEntityToJson = NMSEntity.getMethod("save", NBTTagCompound);

            // This is a craftbukkit handle that allows us to convert our bukkit entity into a minecraft entity
            getNMSEntity = craftBukkitEntity.getMethod("getHandle");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    /**
     * This method accepts a bukkit entity, converts it to a minecraft entity class, saves it to nbt and then serializes it, and finally encodes it in base64 and returns that value
     *
     * Handy!
     *
     * @param entity The entity to save
     * @return A base64 encoded string that comprises of an entities' serialized NBT data
     */
    public static String getEntityAsBytes(Entity entity) {
        try {
            // First, convert our bukkit entity to a minecraft entity.
            Object nmsEntity = getMinecraftEntity(entity);
            // Invoke the empty NBTCompoundConstructor to create a new compount, like new NBTTagCompound()
            Object compound = compoundConstructor.newInstance();
            // Create a new output stream that we're going to have minecraft write some data to
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            // Here we're saving the entity to an NBTTagCompound so we can serialize it
            saveEntityToJson.invoke(nmsEntity, compound);
            // Now we're writing the compound to a byte stream so that we can save it as a string, aka serialization
            streamToolsWriteCompoundToOutput.invoke(null, compound, stream);
            // Now we've got the serialized entity as a collection of bytes and serialization is complete
            byte[] val = stream.toByteArray();
            // Always close IO streams, good practice
            stream.close();

            // And finally, using base64 to encode it as a string
            return Base64.encode(val);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * This method will load entity data that has been stored in nbt into the entity. This will create a LITERAL COPY OF THE SAVED ENTITY. Basically, everything will be the exact same.
     * Except for UUID and health. UUID is because same uuid entities cause fucking problems. Health because otherwise the entity is fuckin dead
     *
     * @param entity A base entity to overwrite. As we're loading entity data, you're gonna have to give us an entity to brainwash.
     * @param nbt    The nbt string to parse and load into the entity.
     */
    public static void loadEntityFromNBT(Entity entity, String nbt) {
        try {
            // Create a new input stream after decoding our previously serialized data. We'll use this for deserialization
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.decode(nbt));
            // Now we're going to use a utility file in the minecraft source code to translate the serialized string to a fully functional nbt tag compound
            Object compound = streamToolsLoadCompoundFromInput.invoke(null, (InputStream) stream);
            // Close your streams! Just good practice
            stream.close();
            // If an entity is alive we need to do a few important things, like changing it's health. Also, location is stored in NBT of entities but it's new location will be much different!
            if (entity instanceof LivingEntity) {
                // Grab the location of the entity BEFORE we change anything
                Location l = entity.getLocation();
                // Here we're saving the health of the entity we're about to respawn into the entities data. We're just maxing out his health to prevent any weirdness.
                setCompoundFloat.invoke(compound, "Health", (float) ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                // Here we're writing the location to the entity, as the entities' location is stored in NBT and we're going to need to overwrite it.
                setBase.invoke(compound, "Pos", buildList(l.getX(), l.getY(), l.getZ()));
            }

            // Converting our bukkit entity to a minecraft entity
            Object nmsEntity = getMinecraftEntity(entity);
            // THIS IS VERY IMPORTANT! The OLD entity who was serialized has his own UUID, but that entity is marked as dead by minecraft/bukkit and the new entity is the one that's being tracked!
            // If we used the old UUID bukkit might have a seizure! So we're replacing in the NBTTagCompound the old entities' UUID with the UUID of the entity we're overwriting
            setCompoundUUID.invoke(compound, "UUID", entity.getUniqueId());
            // And finally, we're loading all of our entity data into this new entity. And boom, our random animal is now our favorite, formerly lost pet!
            loadEntityFromNBT.invoke(nmsEntity, compound);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static Object getMinecraftEntity(Entity entity) throws Exception {
        return getNMSEntity.invoke(entity);
    }

    private static Object buildList(double... doubles) throws Exception {
        Object list = listConstructor.newInstance();
        double[] ds = doubles;
        int i = ds.length;

        for (int j = 0; j < i; j++) {
            double d1 = ds[j];

            listAdd.invoke(list, NBTBase.cast(doubleConstructor.newInstance(d1)));
        }

        return list;
    }

}
