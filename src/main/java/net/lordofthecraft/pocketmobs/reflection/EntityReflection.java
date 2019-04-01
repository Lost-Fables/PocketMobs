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

    final private static String CRAFTBUKKIT_PATH = "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".";
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
            Class<?> NBTTagCompound = Class.forName(PATH + "NBTTagCompound");
            Class<?> NBTTagList = Class.forName(PATH + "NBTTagList");
            NBTBase = Class.forName(PATH + "NBTBase");
            Class<?> NBTDouble = Class.forName(PATH + "NBTTagDouble");
            Class<?> NMSEntity = Class.forName(PATH + "Entity");
            Class<?> craftBukkitEntity = Class.forName(CRAFTBUKKIT_PATH + "entity.CraftEntity");
            Class<?> NBTCompressedStreamTools = Class.forName(PATH + "NBTCompressedStreamTools");
            setCompoundFloat = NBTTagCompound.getMethod("setFloat", String.class, float.class);
            setBase = NBTTagCompound.getMethod("set", String.class, NBTBase);
            listAdd = NBTTagList.getMethod("add", NBTBase);
            for (Method method : NMSEntity.getMethods()) {
                for (Type type : method.getGenericParameterTypes()) {
                    if (type.getTypeName().equalsIgnoreCase(NBTTagCompound.getTypeName())
                            && method.getReturnType().equals(Void.TYPE)) {
                        loadEntityFromNBT = method;
                        break;
                    }
                }
            }

            for (Method method : NBTTagCompound.getMethods()) {
                boolean stringMatch = false;
                for (Type type : method.getGenericParameterTypes()) {
                    if (type.getTypeName().equalsIgnoreCase(String.class.getTypeName())
                            && !stringMatch) {
                        stringMatch = true;
                    }
                    if (stringMatch
                            && type.getTypeName().equalsIgnoreCase(UUID.class.getTypeName())
                            && method.getReturnType().equals(Void.TYPE)) {
                        setCompoundUUID = method;
                        break;
                    }
                }
            }

            for (Method method : NBTCompressedStreamTools.getMethods()) {
                Type[] parameterTypes = method.getGenericParameterTypes();
                if (method.getReturnType().equals(Void.TYPE)) {
                    if (parameterTypes.length == 2
                            && parameterTypes[0].getTypeName().equals(NBTTagCompound.getTypeName())
                            && parameterTypes[1].getTypeName().equals(OutputStream.class.getTypeName())) {
                        streamToolsWriteCompoundToOutput = method;
                    }
                } else if (method.getReturnType().equals(NBTTagCompound)) {
                    if (parameterTypes.length == 1
                            && parameterTypes[0].getTypeName().equals(InputStream.class.getTypeName())) {
                        streamToolsLoadCompoundFromInput = method;
                    }
                }
            }
            compoundConstructor = NBTTagCompound.getConstructor();
            listConstructor = NBTTagList.getConstructor();
            doubleConstructor = NBTDouble.getConstructor(double.class);

            saveEntityToJson = NMSEntity.getMethod("save", NBTTagCompound);

            getNMSEntity = craftBukkitEntity.getMethod("getHandle");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public static String getEntityAsBytes(Entity entity) {
        try {
            Object nmsEntity = getMinecraftEntity(entity);
            Object compound = compoundConstructor.newInstance();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            saveEntityToJson.invoke(nmsEntity, compound);
            streamToolsWriteCompoundToOutput.invoke(null, compound, stream);
            byte[] val = stream.toByteArray();
            stream.close();

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
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.decode(nbt));
            Object compound = streamToolsLoadCompoundFromInput.invoke(null, (InputStream) stream);
            stream.close();
            if (entity instanceof LivingEntity) {
                Location l = entity.getLocation();
                setCompoundFloat.invoke(compound, "Health", (float) ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                setBase.invoke(compound, "Pos", buildList(l.getX(), l.getY(), l.getZ()));
            }

            Object nmsEntity = getMinecraftEntity(entity);
            setCompoundUUID.invoke(compound, "UUID", entity.getUniqueId());
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
