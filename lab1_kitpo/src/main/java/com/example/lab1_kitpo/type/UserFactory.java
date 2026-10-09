package com.example.lab1_kitpo.type;

import com.example.lab1_kitpo.type.impl.IntegerBitSet;
import com.example.lab1_kitpo.type.impl.StringType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Фабрика хранимых типов данных.
 * Хранит зарегистрированные прототипы {@link UserType} по имени типа.
 * Для добавления нового ТД достаточно создать класс, реализующий {@link UserType},
 * и добавить его экземпляр через {@link #register(UserType)}.
 * Никакие другие изменения в код не требуются.
 */
public class UserFactory {

    private final Map<String, UserType> prototypes = new LinkedHashMap<>();

    public UserFactory() {
        register(new IntegerBitSet());
        register(new StringType());
    }

    /** Регистрирует прототип нового хранимого типа. */
    public void register(UserType type) {
        prototypes.put(type.typeName(), type);
    }

    /** Возвращает список имён зарегистрированных типов (для выпадающего списка в GUI). */
    public ArrayList<String> getTypeNameList() {
        return new ArrayList<>(prototypes.keySet());
    }

    /** Возвращает прототип-строитель по имени типа. */
    public UserType getBuilderByName(String name) {
        return prototypes.get(name);
    }
}
