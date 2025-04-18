package dev.hangalito;

import dev.hangalito.test.Customer;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Customer operjotta = new Customer(12, "Operjotta", "operjotta@gmail.com");
        Customer operclaudio = new Customer(15, "Operclaudio", "operclaudio@outlook.com");
        Customer paulloacg = new Customer(18, "PaulloAGC", "paulloacg@gmail.com");
        var customers = List.of(operjotta, operclaudio, paulloacg);
        Datasource<Customer, Integer> datasource = new Datasource<>(LocationService.getInstance());
        datasource.init(Customer.class, Integer.class);
        var key = 37;
        var recovered = datasource.load(key);
        recovered.ifPresentOrElse(customer -> System.out.println("Customer " + customer.getName() + " found"),
                () -> System.out.println("No customer with key " + key));
    }
}
