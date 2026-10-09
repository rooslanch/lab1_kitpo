package com.example.lab1_kitpo.type.impl;

import com.example.lab1_kitpo.type.Comparator;
import com.example.lab1_kitpo.type.UserType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.StringJoiner;

/**
 * Множество целых неотрицательных чисел, представленное битовым массивом.
 * i-й разряд 1/0 — наличие/отсутствие числа i в множестве.
 * Операции: объединение, пересечение, разность, добавление, проверка вхождения.
 * Сравнение — по мощности (cardinality).
 */
public class IntegerBitSet implements UserType {

    private final BitSet bits;

    public IntegerBitSet() {
        this.bits = new BitSet();
    }

    public IntegerBitSet(BitSet bits) {
        this.bits = (BitSet) bits.clone();
    }

    // ==================== Операции множества ====================

    /** Добавляет элемент в множество. */
    public void add(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Отрицательные числа не поддерживаются: " + value);
        }
        bits.set(value);
    }

    /** Проверяет, входит ли число в множество. */
    public boolean contains(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Отрицательные числа не поддерживаются: " + value);
        }
        return bits.get(value);
    }

    /** Объединение: this = this ∪ other. */
    public void union(IntegerBitSet other) {
        bits.or(other.bits);
    }

    /** Пересечение: this = this ∩ other. */
    public void intersection(IntegerBitSet other) {
        bits.and(other.bits);
    }

    /** Разность: this = this \ other. */
    public void difference(IntegerBitSet other) {
        bits.andNot(other.bits);
    }

    /** Мощность множества (количество элементов). */
    public int cardinality() {
        return bits.cardinality();
    }

    // ==================== Реализация UserType ====================

    @Override
    public String typeName() {
        return "IntegerBitSet";
    }

    @Override
    public Object create() {
        return new IntegerBitSet();
    }

    @Override
    public Object clone() {
        return new IntegerBitSet(bits);
    }

    /**
     * Читает одну строку из потока (до символа '\n') и парсит её.
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
            throw new RuntimeException("Ошибка чтения IntegerBitSet из потока", e);
        }
    }

    /**
     * Парсит строку вида "{1, 3, 5}", "1, 3, 5", "1 3 5" или "{}".
     * Разделители — запятые и/или пробелы. Фигурные скобки необязательны.
     */
    @Override
    public Object parseValue(String ss) {
        IntegerBitSet result = new IntegerBitSet();
        if (ss == null || ss.isBlank()) {
            return result;
        }
        String cleaned = ss.trim();
        if (cleaned.startsWith("{")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("}")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        if (cleaned.isBlank()) {
            return result;
        }
        String[] parts = cleaned.split("[,\\s]+");
        for (String part : parts) {
            if (!part.isBlank()) {
                result.add(Integer.parseInt(part.trim()));
            }
        }
        return result;
    }

    /**
     * Компаратор — по мощности множества (cardinality).
     * Возвращает отрицательное, если o1 «меньше» o2, и т.д.
     */
    @Override
    public Comparator getTypeComparator() {
        return (o1, o2) -> {
            int c1 = ((IntegerBitSet) o1).cardinality();
            int c2 = ((IntegerBitSet) o2).cardinality();
            return Integer.compare(c1, c2);
        };
    }

    // ==================== Отображение ====================

    /** Формат: "{1, 3, 5}" или "{}" для пустого. Используется и для сериализации. */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
            joiner.add(String.valueOf(i));
        }
        return joiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return bits.equals(((IntegerBitSet) o).bits);
    }

    @Override
    public int hashCode() {
        return bits.hashCode();
    }
}