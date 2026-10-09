module com.example.lab1_kitpo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.lab1_kitpo to javafx.fxml;
    exports com.example.lab1_kitpo;
    exports com.example.lab1_kitpo.type;
    exports com.example.lab1_kitpo.list;
    exports com.example.lab1_kitpo.persist;
}