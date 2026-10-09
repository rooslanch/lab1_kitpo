package com.example.lab1_kitpo.list;

import com.example.lab1_kitpo.type.Comparator;
import com.example.lab1_kitpo.type.UserType;

import java.util.ArrayList;

/**
 * Односвязный список, хранящий объекты произвольного типа.
 * Элементы доступны только последовательно — «улица с односторонним движением».
 * Поддерживает добавление в конец, доступ по логическому индексу,
 * итераторы через колбэки ({@link DoWith}, {@link TestIt}).
 */
public class SinglyLinkedList {

    /** Узел списка: данные + ссылка на следующий узел. */
    private static class Node {
        Object data;
        Node next;

        Node(Object data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    public SinglyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // ==================== Базовые операции ====================

    /**
     * Добавляет элемент в конец списка. O(1) — за счёт хвостового указателя.
     */
    public void addToEnd(Object value) {
        Node node = new Node(value);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    /**
     * Возвращает элемент по логическому индексу (0 — первый).
     *
     * @throws IndexOutOfBoundsException если индекс вне [0, size)
     */
    public Object get(int index) {
        checkIndex(index);
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    /**
     * Вставляет элемент по логическому индексу (0 — перед первым, size — в конец).
     *
     * @throws IndexOutOfBoundsException если индекс вне [0, size]
     */
    public void insert(int index, Object value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Индекс " + index + " вне [0, " + size + "]");
        }
        if (index == size) {
            addToEnd(value);
            return;
        }
        Node node = new Node(value);
        if (index == 0) {
            node.next = head;
            head = node;
        } else {
            Node prev = head;
            for (int i = 0; i < index - 1; i++) {
                prev = prev.next;
            }
            node.next = prev.next;
            prev.next = node;
        }
        size++;
    }

    /**
     * Удаляет элемент по логическому индексу и возвращает его.
     *
     * @throws IndexOutOfBoundsException если индекс вне [0, size)
     */
    public Object remove(int index) {
        checkIndex(index);
        Object removed;
        if (index == 0) {
            removed = head.data;
            head = head.next;
            if (head == null) {
                tail = null;
            }
        } else {
            Node prev = head;
            for (int i = 0; i < index - 1; i++) {
                prev = prev.next;
            }
            removed = prev.next.data;
            prev.next = prev.next.next;
            if (prev.next == null) {
                tail = prev;
            }
        }
        size--;
        return removed;
    }

    /** Очищает список. */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /** Количество элементов в списке. */
    public int size() {
        return size;
    }

    // ==================== Итераторы ====================

    /**
     * Обходит список и вызывает {@code action.doWith()} для каждого элемента.
     *
     * <pre>
     * list.forEach((obj) -> System.out.println(obj));
     * </pre>
     */
    public void forEach(DoWith action) {
        for (Node p = head; p != null; p = p.next) {
            action.doWith(p.data);
        }
    }

    /**
     * Обходит список и возвращает первый элемент, для которого
     * {@code test.testIt()} вернул {@code true}. Если такого нет — {@code null}.
     *
     * <pre>
     * Object found = list.firstThat((obj) -> obj.toString().length() > 3);
     * </pre>
     */
    public Object firstThat(TestIt test) {
        for (Node p = head; p != null; p = p.next) {
            if (test.testIt(p.data)) {
                return p.data;
            }
        }
        return null;
    }

    // ==================== Сортировка ====================

    /**
     * Сортировка слиянием (merge sort) за O(n log n).
     * Переставляет ссылки {@code next} между существующими узлами —
     * объекты-данные и узлы не создаются заново и не клонируются.
     *
     * @param comp компаратор из {@link UserType#getTypeComparator()}
     */
    public void sort(Comparator comp) {
        if (head == null || head.next == null) {
            return;
        }
        head = mergeSort(head, comp);
        // Хвост изменился — находим новый последний узел
        tail = head;
        while (tail.next != null) {
            tail = tail.next;
        }
    }

    /** Рекурсивно делит список пополам и сортирует каждую половину. */
    private Node mergeSort(Node node, Comparator comp) {
        if (node == null || node.next == null) {
            return node;
        }
        Node middle = getMiddle(node);
        Node nextToMiddle = middle.next;
        middle.next = null; // разрываем список на две половины

        Node left = mergeSort(node, comp);
        Node right = mergeSort(nextToMiddle, comp);

        return merge(left, right, comp);
    }

    /**
     * Находит узел, после которого список делится пополам.
     * Метод «черепахи и зайца»: slow — 1 шаг, fast — 2 шага.
     */
    private Node getMiddle(Node node) {
        Node slow = node;
        Node fast = node.next;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    /**
     * Сливает два отсортированных подсписка в один,
     * перецепляя {@code next} у существующих узлов.
     */
    private Node merge(Node left, Node right, Comparator comp) {
        Node dummy = new Node(null); // временный узел-заглушка, не попадает в список
        Node current = dummy;
        while (left != null && right != null) {
            if (comp.compare(left.data, right.data) <= 0) {
                current.next = left;
                left = left.next;
            } else {
                current.next = right;
                right = right.next;
            }
            current = current.next;
        }
        current.next = (left != null) ? left : right;
        return dummy.next;
    }

    // ==================== Отображение ====================

    /** Собирает элементы в ArrayList (для передачи в JavaFX ListView и т.п.). */
    public ArrayList<Object> toArrayList() {
        ArrayList<Object> result = new ArrayList<>(size);
        for (Node p = head; p != null; p = p.next) {
            result.add(p.data);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node p = head;
        while (p != null) {
            sb.append(p.data);
            if (p.next != null) {
                sb.append(", ");
            }
            p = p.next;
        }
        sb.append("]");
        return sb.toString();
    }

    // ==================== Служебное ====================

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Индекс " + index + " вне [0, " + size + ")");
        }
    }
}