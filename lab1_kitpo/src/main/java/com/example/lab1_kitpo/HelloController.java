package com.example.lab1_kitpo;

import com.example.lab1_kitpo.list.SinglyLinkedList;
import com.example.lab1_kitpo.persist.TextSerializer;
import com.example.lab1_kitpo.type.Comparator;
import com.example.lab1_kitpo.type.UserFactory;
import com.example.lab1_kitpo.type.UserType;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HelloController {

    private final UserFactory factory = new UserFactory();
    private SinglyLinkedList currentList = new SinglyLinkedList();
    private UserType currentPrototype;
    private boolean skipClearOnTypeChange = false;

    @FXML
    private ComboBox<String> typeSelector;
    @FXML
    private TextField valueField;
    @FXML
    private TextField indexField;
    @FXML
    private ListView<String> listView;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        typeSelector.getItems().addAll(factory.getTypeNameList());
        typeSelector.getSelectionModel().selectFirst();
    }

    @FXML
    private void onTypeChanged() {
        String name = typeSelector.getValue();
        if (name == null) return;
        currentPrototype = factory.getBuilderByName(name);
        if (currentPrototype == null) return;
        if (!skipClearOnTypeChange) {
            currentList.clear();
        }
        updateValuePrompt();
        updateListView();
        statusLabel.setText("Тип: " + name + ".  Элементов: " + currentList.size());
    }

    @FXML
    private void onAddEnd() {
        if (!checkPrototype()) return;
        try {
            Object item = currentPrototype.parseValue(valueField.getText());
            currentList.addToEnd(item);
            updateListView();
            statusLabel.setText("Добавлено в конец: " + item);
            valueField.clear();
        } catch (Exception e) {
            statusLabel.setText("Ошибка ввода: " + e.getMessage());
        }
    }

    @FXML
    private void onInsert() {
        if (!checkPrototype()) return;
        try {
            int index = parseIndex();
            Object item = currentPrototype.parseValue(valueField.getText());
            currentList.insert(index, item);
            updateListView();
            statusLabel.setText("Вставлено [" + index + "]: " + item);
            valueField.clear();
        } catch (NumberFormatException e) {
            statusLabel.setText("Ошибка: индекс — целое число");
        } catch (Exception e) {
            statusLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void onGet() {
        if (!checkPrototype()) return;
        try {
            int index = parseIndex();
            Object item = currentList.get(index);
            statusLabel.setText("Элемент [" + index + "] = " + item);
            listView.getSelectionModel().select(index);
        } catch (NumberFormatException e) {
            statusLabel.setText("Ошибка: индекс — целое число");
        } catch (Exception e) {
            statusLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void onRemove() {
        if (!checkPrototype()) return;
        try {
            int index = parseIndex();
            Object removed = currentList.remove(index);
            updateListView();
            statusLabel.setText("Удалён [" + index + "]: " + removed);
        } catch (NumberFormatException e) {
            statusLabel.setText("Ошибка: индекс — целое число");
        } catch (Exception e) {
            statusLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void onSort() {
        if (!checkPrototype()) return;
        try {
            Comparator comp = currentPrototype.getTypeComparator();
            currentList.sort(comp);
            updateListView();
            statusLabel.setText("Список отсортирован");
        } catch (Exception e) {
            statusLabel.setText("Ошибка сортировки: " + e.getMessage());
        }
    }

    @FXML
    private void onFind() {
        if (!checkPrototype()) return;
        try {
            String search = valueField.getText();
            Object found = currentList.firstThat(obj -> obj.toString().contains(search));
            if (found != null) {
                statusLabel.setText("Найдено: " + found);
                int idx = currentList.toArrayList().indexOf(found);
                if (idx >= 0) listView.getSelectionModel().select(idx);
            } else {
                statusLabel.setText("Не найдено: \"" + search + "\"");
            }
        } catch (Exception e) {
            statusLabel.setText("Ошибка поиска: " + e.getMessage());
        }
    }

    @FXML
    private void onSave() {
        if (!checkPrototype()) return;
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Сохранить список");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
            fc.setInitialFileName("list_" + currentPrototype.typeName() + ".txt");
            File file = fc.showSaveDialog(getWindow());
            if (file == null) return;
            TextSerializer.save(currentPrototype, currentList, file.toPath());
            statusLabel.setText("Сохранено: " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("Ошибка сохранения: " + e.getMessage());
        }
    }

    @FXML
    private void onLoad() {
        if (!checkPrototype()) return;
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Загрузить список");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
            File file = fc.showOpenDialog(getWindow());
            if (file == null) return;
            Path path = file.toPath();
            String fileTypeName;
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                fileTypeName = reader.readLine();
            }
            if (fileTypeName != null && !fileTypeName.trim().equals(currentPrototype.typeName())) {
                skipClearOnTypeChange = true;
                typeSelector.setValue(fileTypeName.trim());
                skipClearOnTypeChange = false;
            }
            SinglyLinkedList loaded = TextSerializer.load(factory, path);
            currentList = loaded;
            updateListView();
            statusLabel.setText("Загружено " + loaded.size() + " элементов из " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("Ошибка загрузки: " + e.getMessage());
        }
    }

    @FXML
    private void onClear() {
        currentList.clear();
        updateListView();
        statusLabel.setText("Список очищен");
    }

    // ==================== Служебные методы ====================

    private boolean checkPrototype() {
        if (currentPrototype == null) {
            statusLabel.setText("Сначала выберите тип данных");
            return false;
        }
        return true;
    }

    private int parseIndex() {
        return Integer.parseInt(indexField.getText().trim());
    }

    private Window getWindow() {
        return listView.getScene().getWindow();
    }

    private void updateListView() {
        listView.getItems().clear();
        for (Object obj : currentList.toArrayList()) {
            listView.getItems().add(obj.toString());
        }
    }

    private void updateValuePrompt() {
        if (currentPrototype == null) return;
        switch (currentPrototype.typeName()) {
            case "IntegerBitSet" -> valueField.setPromptText("{1, 5, 10} или 1 5 10");
            case "String" -> valueField.setPromptText("Введите текст");
            default -> valueField.setPromptText("Значение");
        }
    }
}
