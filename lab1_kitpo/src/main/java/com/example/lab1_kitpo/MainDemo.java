package com.example.lab1_kitpo;

import com.example.lab1_kitpo.list.SinglyLinkedList;
import com.example.lab1_kitpo.persist.TextSerializer;
import com.example.lab1_kitpo.type.Comparator;
import com.example.lab1_kitpo.type.UserFactory;
import com.example.lab1_kitpo.type.UserType;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Консольная демонстрация работы структуры данных.
 * Один и тот же сценарий операций прогоняется для каждого
 * хранимого типа, взятого из {@link UserFactory}.
 */
public class MainDemo {

    public static void main(String[] args) {
        UserFactory factory = new UserFactory();

        for (String typeName : factory.getTypeNameList()) {
            System.out.println("\n============================================");
            System.out.println("  Тип: " + typeName);
            System.out.println("============================================");
            testType(factory, factory.getBuilderByName(typeName));
        }
    }

    /** Единый сценарий тестирования для произвольного типа. */
    private static void testType(UserFactory factory, UserType proto) {
        SinglyLinkedList list = new SinglyLinkedList();
        String[] initial = getInitial(proto);
        String insertHead = getInsertHead(proto);
        String insertMid = getInsertMid(proto);

        // 1. addToEnd
        System.out.println("\n[1] addToEnd x " + initial.length);
        for (String s : initial) {
            list.addToEnd(proto.parseValue(s));
        }
        print(list);

        // 2. insert
        System.out.println("\n[2] insert(0, " + insertHead + ")");
        list.insert(0, proto.parseValue(insertHead));
        print(list);

        System.out.println("\n[2] insert(2, " + insertMid + ")");
        list.insert(2, proto.parseValue(insertMid));
        print(list);

        // 3. get
        System.out.println("\n[3] get(0) = " + list.get(0));
        System.out.println("[3] get(2) = " + list.get(2));
        System.out.println("[3] get(последний) = " + list.get(list.size() - 1));

        // 4. remove
        System.out.println("\n[4] remove(0)");
        System.out.println("    удалён: " + list.remove(0));
        print(list);

        // 5. forEach
        System.out.println("\n[5] forEach:");
        list.forEach(obj -> System.out.println("    -> " + obj));

        // 6. sort
        System.out.println("\n[6] sort — сортировка по компаратору типа:");
        Comparator comp = proto.getTypeComparator();
        list.sort(comp);
        print(list);

        // 7. firstThat
        System.out.println("\n[7] firstThat(toString().length() > 5):");
        Object found = list.firstThat(obj -> obj.toString().length() > 5);
        System.out.println("    найдено: " + found);

        // 8. save
        Path file = Path.of("demo_" + proto.typeName() + ".txt");
        System.out.println("\n[8] save -> " + file.toAbsolutePath());
        try {
            TextSerializer.save(proto, list, file);
            System.out.println("    OK");
        } catch (IOException e) {
            System.out.println("    ОШИБКА: " + e.getMessage());
            return;
        }

        // 9. load
        System.out.println("\n[9] load <- " + file);
        try {
            SinglyLinkedList loaded = TextSerializer.load(factory, file);
            System.out.println("    загружено " + loaded.size() + " элементов:");
            loaded.forEach(obj -> System.out.println("    -> " + obj));
        } catch (IOException e) {
            System.out.println("    ОШИБКА: " + e.getMessage());
        }

        // 10. Итоговый toString
        System.out.println("\n[10] итоговый toString: " + list);
    }

    private static void print(SinglyLinkedList list) {
        System.out.println("    (" + list.size() + ") " + list);
    }

    // ====== Тестовые данные по типам ======

    private static String[] getInitial(UserType proto) {
        return switch (proto.typeName()) {
            case "IntegerBitSet" -> new String[]{
                    "{1, 5, 10}", "{3, 7}", "{2}", "{0, 4, 8, 12}", "{6}"
            };
            case "String" -> new String[]{
                    "banana", "apple", "cherry", "date", "elderberry"
            };
            default -> new String[]{"a", "b", "c", "d", "e"};
        };
    }

    private static String getInsertHead(UserType proto) {
        return switch (proto.typeName()) {
            case "IntegerBitSet" -> "{99}";
            case "String" -> "zzz";
            default -> "x";
        };
    }

    private static String getInsertMid(UserType proto) {
        return switch (proto.typeName()) {
            case "IntegerBitSet" -> "{42}";
            case "String" -> "mmm";
            default -> "y";
        };
    }
}