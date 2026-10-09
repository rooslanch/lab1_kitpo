package com.example.lab1_kitpo.persist;

import com.example.lab1_kitpo.list.SinglyLinkedList;
import com.example.lab1_kitpo.type.UserFactory;
import com.example.lab1_kitpo.type.UserType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Текстовая сериализация/десериализация структуры данных.
 * Формат файла:
 * <pre>
 * строка 1:  имя типа (например, "IntegerBitSet")
 * строка 2+: toString() каждого элемента списка
 * </pre>
 */
public class TextSerializer {

    /**
     * Сохраняет список в текстовый файл.
     *
     * @param typePrototype прототип типа — используется только для имени типа
     * @param list          сохраняемый список
     * @param path          путь к файлу
     */
    public static void save(UserType typePrototype, SinglyLinkedList list, Path path) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            writer.println(typePrototype.typeName());
            list.forEach(obj -> writer.println(obj.toString()));
        }
    }

    /**
     * Загружает список из текстового файла.
     * Имя типа читается из первой строки, затем по нему
     * из фабрики берётся прототип для парсинга элементов.
     *
     * @param factory фабрика типов (для определения прототипа по имени)
     * @param path    путь к файлу
     * @return новый список с загруженными элементами
     */
    public static SinglyLinkedList load(UserFactory factory, Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String typeName = reader.readLine();
            if (typeName == null || typeName.isBlank()) {
                throw new IOException("Файл пуст или не содержит имени типа: " + path);
            }
            UserType proto = factory.getBuilderByName(typeName.trim());
            if (proto == null) {
                throw new IOException("Неизвестный тип: \"" + typeName.trim() + "\"");
            }
            SinglyLinkedList list = new SinglyLinkedList();
            String line;
            while ((line = reader.readLine()) != null) {
                list.addToEnd(proto.parseValue(line));
            }
            return list;
        }
    }
}