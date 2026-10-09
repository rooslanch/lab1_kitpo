package com.example.lab1_kitpo.type.impl;

import com.example.lab1_kitpo.type.Comparator;
import com.example.lab1_kitpo.type.UserType;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Обёртка над строкой — простейший хранимый тип для демонстрации
 * работы структуры данных с произвольным типом.
 * Сравнение — лексикографическое (String.compareTo).
 */
public class StringType implements UserType {

    private String value;

    public StringType() {
        this.value = "";
    }

    public StringType(String value) {
        this.value = value != null ? value : "";
    }

    // ==================== Операции ====================

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value != null ? value : "";
    }

    // ==================== Реализация UserType ====================

    @Override
    public String typeName() {
        return "String";
    }

    @Override
    public Object create() {
        return new StringType("");
    }

    @Override
    public Object clone() {
        return new StringType(this.value);
    }

    /**
     * Читает одну строку из потока (до символа '\n') и оборачивает её.
     * Посимвольное чтение — чтобы не «съесть» данные следующих строк
     * при повторных вызовах на одном потоке.
     */
    @Override
    public Object readValue(InputStreamReader in) {
        try {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = in.read()) != -1 && c != '\n') {
                if (c != '\r') {
                    sb.append((char) c);
                }
            }
            return parseValue(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения StringType из потока", e);
        }
    }

    /** Строка берётся как есть — без разбора форматов. */
    @Override
    public Object parseValue(String ss) {
        return new StringType(ss);
    }

    /** Компаратор — лексикографическое сравнение строк. */
    @Override
    public Comparator getTypeComparator() {
        return (o1, o2) -> {
            String s1 = ((StringType) o1).value;
            String s2 = ((StringType) o2).value;
            return s1.compareTo(s2);
        };
    }

    // ==================== Отображение ====================

    /** Возвращает содержимое строки как есть. */
    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return value.equals(((StringType) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}