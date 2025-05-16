package dev.hangalito;

import dev.hangalito.storage.Datasource;
import dev.hangalito.test.Car;

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
        Datasource<Car, Integer> datasource = new Datasource<>();
        datasource.init(Car.class);

        var hyundai = datasource.findBy("brand", "Hyundai");
        var nissan = datasource.findBy("brand", "Nissan");
        var toyota = datasource.findBy("brand", "Toyota");

        System.out.println("Hyundai cars:");
        hyundai.forEach(System.out::println);
        System.out.println();
        System.out.println("Nissan cars:");
        nissan.forEach(System.out::println);
        System.out.println();
        System.out.println("Toyota cars:");
        toyota.forEach(System.out::println);
    }
}
