package com.doctordark.hcf.combatlog;

import net.minecraft.server.v1_7_R4.EntityTypes;

import java.lang.reflect.Field;
import java.util.Map;

public class CustomEntityRegistration {

    public static void registerCustomEntities() {
        try {
            registerCustomEntity(LoggerEntity.class, "CraftSkeleton", 51);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void registerCustomEntity(final Class entityClass, final String name, final int id) {
        //GenericUtils.setFieldPrivateStaticMap("c", name, entityClass);
        setFieldPrivateStaticMap("d", entityClass, name);
        //GenericUtils.setFieldPrivateStaticMap("e", id, entityClass);
        setFieldPrivateStaticMap("f", entityClass, id);
        //GenericUtils.setFieldPrivateStaticMap("g", id, entityClass);
    }

    public static void unregisterCustomEntities() {

    }

    public static void setFieldPrivateStaticMap(String fieldName, Object key, Object value) {
        try {
            Field field = EntityTypes.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Map map = (Map) field.get(null);
            map.put(key, value);
            field.set(null, map);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public static void setField(String fieldName, Object key, Object value) {
        try {
            Field field = key.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(key, value);
            field.setAccessible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
