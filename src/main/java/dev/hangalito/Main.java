package dev.hangalito;

import dev.hangalito.test.Car;
import dev.hangalito.test.Customer;
import dev.hangalito.test.ProgrammingLanguage;

import java.lang.reflect.Field;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        var cars = List.of(
                new Car(1, "Toyota", "Rav-4"),
                new Car(2, "Toyota", "Pagero"),
                new Car(3, "Toyota", "Starlet"),
                new Car(4, "Hyundai", "Elantra"),
                new Car(5, "Hyundai", "Accent"),
                new Car(6, "Hyundai", "Santa Fé"),
                new Car(7, "Nissan", "Patrol")
        );
        Datasource<Car, Integer> datasource = new Datasource<>(LocationService.getInstance());
        datasource.init(Car.class);
        datasource.index("brand");
        var toyotas = datasource.findBy("brand", "Toyota");
        toyotas.forEach(System.out::println);
    }
}
