# lab1_kitpo — Односвязный список с типизированными элементами

> Лабораторная работа по КИТПО.  
> **Вариант СД:** 5 — односвязный список.  
> **Вариант данных:** 10 — множество целых чисел на битовом массиве (`BitSet`).

---

## 1. Общая архитектура (три слоя)

```
┌─────────────────────────────────────┐
│  Layer 3: GUI (JavaFX)              │  hello-view.fxml, HelloController
├─────────────────────────────────────┤
│  Layer 2: Структура данных (СД)     │  SinglyLinkedList, DoWith, TestIt
├─────────────────────────────────────┤
│  Layer 1: Типы данных (ТД)          │  UserType, Comparator,
│  — фабрика + конкретные реализации  │  IntegerBitSet, StringType, UserFactory
└─────────────────────────────────────┘
```

Правило: GUI не знает внутренности СД. СД не знает конкретный ТД (хранит `Object`). Новый ТД добавляется без изменений в GUI или СД.

---

## 2. Структура каталогов

```
src/main/java/
├── module-info.java                          # Java modules
├── com.example.lab1_kitpo/
│   ├── HelloApplication.java                 # Точка входа JavaFX
│   ├── Launcher.java                         # Обёртка launch
│   ├── HelloController.java                  # Контроллер FXML
│   └── MainDemo.java                         # Консольная демонстрация
├── com.example.lab1_kitpo.type/
│   ├── UserType.java                         # Интерфейс хранимого типа
│   ├── Comparator.java                       # Интерфейс компаратора
│   └── UserFactory.java                      # Фабрика прототипов
├── com.example.lab1_kitpo.type.impl/
│   ├── IntegerBitSet.java                    # Вариант 10: множество на BitSet
│   └── StringType.java                       # Второй тип: строка
├── com.example.lab1_kitpo.list/
│   ├── SinglyLinkedList.java                 # Односвязный список
│   ├── DoWith.java                           # doWith(Object)
│   └── TestIt.java                           # testIt(Object)
└── com.example.lab1_kitpo.persist/
    └── TextSerializer.java                   # Текстовая сериализация
```

---

## 3. Слой Типов Данных

### UserType — центральный интерфейс
| Метод | Назначение |
|-------|------------|
| `String typeName()` | Имя типа (для GUI и заголовка файла) |
| `Object create()` | Создать пустой объект |
| `Object clone()` | Создать копию |
| `Object readValue(InputStreamReader)` | Прочитать из потока (посимвольно до `\n`) |
| `Object parseValue(String)` | Создать объект из строки |
| `Comparator getTypeComparator()` | Компаратор для сортировки |

**Важно:** используется собственный `Comparator`, не `java.util.Comparator`.

### UserFactory — паттерн Прототип
Хранит прототипы в `LinkedHashMap<String, UserType>`.
- `getTypeNameList()` → список имён.
- `getBuilderByName(name)` → прототип.
- `register(UserType)` — добавить тип.

Добавление типа: класс `MyType implements UserType` + `register(new MyType())` в конструктор `UserFactory`.

### IntegerBitSet
- Внутри: `java.util.BitSet`.
- `add(int)`, `contains(int)` — с проверкой отрицательных.
- `union`, `intersection`, `difference` — через `or/and/andNot`.
- `cardinality()` — мощность множества.
- `toString()` → `{1, 3, 5}` (или `{}`).
- `parseValue` понимает: `{1, 3, 5}`, `1, 3, 5`, `1 3 5`, `{}`, пустую строку.
- Компаратор → по `cardinality()`.

### StringType
- Внутри: `String`. `parseValue` берёт строку как есть.
- `toString` без кавычек.
- Компаратор → лексикографический.

---

## 4. Слой Структуры Данных (`list`)

### SinglyLinkedList
**Классический односвязный список** с хвостовым указателем.

Внутренний класс:
```java
static class Node {
    Object data;
    Node next;
}
```

Поля: `Node head`, `Node tail`, `int size`.

**Методы:**

| Метод | Сложность | Примечание |
|-------|-----------|------------|
| `addToEnd(Object)` | **O(1)** | За счёт `tail` |
| `get(int)` | O(n) | От `head` вперёд |
| `insert(int, Object)` | O(n) | Учитывает `0` и `size` |
| `remove(int)` → Object | O(n) | Обновляет `tail` |
| `forEach(DoWith)` | O(n) | Обход с колбэком |
| `firstThat(TestIt)` → Object / null | O(n) | Первый подходящий |
| `sort(Comparator)` | **O(n log n)** | Merge sort |
| `toArrayList()` | O(n) | Для JavaFX `ListView` |
| `clear()` / `size()` | O(1) | — |

### DoWith и TestIt
Функциональные интерфейсы (лямбды совместимы):
```java
public interface DoWith { void doWith(Object obj); }
public interface TestIt { boolean testIt(Object obj); }
```

---

## 5. Слой Персистентности (`persist`)

### TextSerializer
**Формат файла** (текстовый, UTF-8):
```
IntegerBitSet
{1, 5, 10}
{3, 7}
{2}
```

- Первая строка — имя типа.
- Остальные — `toString()` элементов.
- `load` берёт имя типа, получает прототип из `UserFactory`, парсит через `parseValue`.

При загрузке файла другого типа GUI автоматически переключает `ComboBox`.

---

## 6. Ключевой алгоритм: Merge Sort

Реализован второй вариант из задания: **«упорядочивает ссылки в самой СД»**.

- **0 новых Node** создаётся.
- **0 клонов** объектов.
- Переставляются **только** `next` у существующих узлов.
- После сортировки `tail` обновляется.

**Механика:**
1. Разделение: `getMiddle` («черепаха и заяц»).
2. Рекурсивная сортировка половин.
3. `merge` с `dummy`-заглушкой.
4. Устойчивость: при равных берётся из левой половины (`<=`).

---

## 7. Слой GUI

### HelloApplication
Загружает `hello-view.fxml`, размер окна **750 × 550**.

### hello-view.fxml
- **Строка 1:** `ComboBox` (тип) + `TextField` (значение) + **«В конец»**, **«Найти»**.
- **Строка 2:** `TextField` (индекс) + **«Вставить»**, **«Получить»**, **«Удалить»**.
- **Строка 3:** **«Сортировать»**, **«Сохранить»**, **«Загрузить»**, **«Очистить»**.
- **`ListView`** — отображение списка.
- **`Label`** — статус/ошибки.

### HelloController
- Хранит `UserFactory`, `SinglyLinkedList`, текущий `UserType`.
- При смене типа список очищается (если не загрузка).
- Все операции в `try-catch`, ошибки в строку состояния.


## 8. Консольная демонстрация (`MainDemo`)

Требование задания (п. 9): «main демонстрирует один и тот же набор операций с разными UserType».  
`MainDemo.main()`: создаёт `UserFactory`, для каждого типа вызывает `testType(proto)` с единым сценарием:  
`addToEnd` → `insert` ×2 → `get` ×3 → `remove` → `forEach` → `sort` → `firstThat` → `save` → `load` → `toString`.

---

## 9. Как запустить

**Консоль:**
```bash
cd lab1_kitpo
mvnw.cmd clean compile
java -cp target\classes com.example.lab1_kitpo.MainDemo
```

**GUI:**
```bash
mvnw.cmd clean javafx:run
```

---

## 10. Тонкости для нейросети-аналитика

| Вопрос | Ответ |
|--------|-------|
| Почему `Object`, не `<T>`? | Задание: «Шаблоны в Java не настоящие, можно обойтись без них». |
| Почему свой `Comparator`? | Задание предлагает написать свой, без привязки к `java.util`. |
| Почему `readValue(InputStreamReader)`? | Задание требует именно эту сигнатуру. Реализация читает посимвольно до `\n`. |
| Почему `DoWith` / `TestIt`? | Задание явно предписывает эти имена и сигнатуры. |
| Флаг `skipClearOnTypeChange` | При `load` файла другого типа ComboBox переключается, но список не сбрасывается. |
| Пустой `StringType` при load? | `TextSerializer.load` не пропускает пустые строки — пустой `StringType("")` сохраняется/загружается корректно. |

---

## 11. Чек-лист соответствия заданию

- [x] Односвязный список (вариант 5)
- [x] Множество на битовом массиве (вариант 10)
- [x] Операции множества: ∪, ∩, \, add, contains
- [x] Сравнение по количеству элементов
- [x] ≥2 типа + фабрика
- [x] Добавление типа = класс + 1 строка в фабрике
- [x] addToEnd, get, insert, remove по индексу
- [x] forEach через колбэк
- [x] Сортировка `O(n log n)` — merge sort, перестановка ссылок
- [x] Сериализация в текстовый файл
- [x] Оконное приложение (JavaFX)
- [x] `main` прогоняет один сценарий для разных типов

