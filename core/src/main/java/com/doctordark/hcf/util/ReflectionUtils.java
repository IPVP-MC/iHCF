package com.doctordark.hcf.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static MetadataValue getPlayerMetadata(Player subject, String metadataKey, Plugin owningPlugin) {
        try {
            Method getPlayerMetadata = owningPlugin.getServer().getClass().getDeclaredMethod("getPlayerMetadata");
            getPlayerMetadata.setAccessible(true);
            Object metadata = getPlayerMetadata.invoke(owningPlugin.getServer());
            Field metadataMapField = metadata.getClass().getSuperclass().getDeclaredField("metadataMap");
            metadataMapField.setAccessible(true);
            Map<String, Map<Plugin, MetadataValue>> metadataMap = (Map<String, Map<Plugin, MetadataValue>>) metadataMapField.get(metadata);
            Method disambiguate = metadata.getClass().getDeclaredMethod("disambiguate", OfflinePlayer.class, String.class);
            disambiguate.setAccessible(true);
            String key = (String) disambiguate.invoke(metadata, subject, metadataKey);
            Map<Plugin, MetadataValue> values = metadataMap.get(key);
            return values == null ? null : values.get(owningPlugin);
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
