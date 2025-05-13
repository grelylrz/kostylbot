package icu.grely;

import arc.Core;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Base64Coder;
import discord4j.common.util.Snowflake;
import icu.grely.annotatins.SaveSetting;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class SettingsLoader {
    public static void loadSettings() {
        for (Field field : Vars.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(SaveSetting.class)) {
                field.setAccessible(true);
                SaveSetting annotation = field.getAnnotation(SaveSetting.class);
                String key = annotation.key().isEmpty() ? field.getName() : annotation.key();

                try {
                    Class<?> type = field.getType();
                    if (type == String.class) {
                        field.set(null, Core.settings.getString(key, (String) field.get(null)));
                    } else if (type == long.class || type == Long.class) {
                        field.set(null, Core.settings.getLong(key, field.getLong(null)));
                    } else if (type == int.class || type == Integer.class) {
                        field.set(null, Core.settings.getInt(key, field.getInt(null)));
                    } else if (Seq.class.isAssignableFrom(type)) {
                        Type genericType = field.getGenericType();
                        if (genericType instanceof ParameterizedType parameterizedType) {
                            Type arg = parameterizedType.getActualTypeArguments()[0];
                            String raw = Core.settings.getString(key, "");
                            Seq<Object> list = new Seq<>();
                            if (!raw.isEmpty()) {
                                for (String entry : raw.split(";")) {
                                    if (arg == Snowflake.class) {
                                        list.add(Snowflake.of(entry));
                                    } else if (arg == String.class) {
                                        list.add(Base64Coder.decodeString(entry));
                                    } else if (arg == Long.class || arg == long.class) {
                                        list.add(Long.parseLong(entry));
                                    } else if (arg == Integer.class || arg == int.class) {
                                        list.add(Integer.parseInt(entry));
                                    } // можно добавить другие типы при необходимости, но я не думаю, что она будет.
                                }
                            }
                            field.set(null, list);
                        }
                    }
                } catch (Exception e) {
                    Log.err(e);
                }
            }
        }
    }
    public static void saveSettings() {
        for (Field field : Vars.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(SaveSetting.class)) {
                field.setAccessible(true);
                SaveSetting annotation = field.getAnnotation(SaveSetting.class);
                String key = annotation.key().isEmpty() ? field.getName() : annotation.key();

                try {
                    Object value = field.get(null);
                    if (value instanceof String str) {
                        Core.settings.put(key, str);
                    } else if (value instanceof Long l) {
                        Core.settings.put(key, l);
                    } else if (value instanceof Integer i) {
                        Core.settings.put(key, i);
                    } else if (value instanceof Seq<?> seq) {
                        if (!seq.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (Object obj : seq) {
                                if (obj instanceof Snowflake s) {
                                    sb.append(s.asString());
                                } else {
                                    sb.append(Base64Coder.encodeString(obj.toString()));
                                }
                                sb.append(';');
                            }
                            Core.settings.put(key, sb.toString());
                        }
                    } else if(value instanceof Boolean) {
                        boolean b = (Boolean) value;
                        Core.settings.put(key, b);
                    }
                } catch (Exception e) {
                    Log.err(e);
                }
            }
        }
    }
}
