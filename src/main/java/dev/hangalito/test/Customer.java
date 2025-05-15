package dev.hangalito.test;

import dev.hangalito.annotations.Key;
import dev.hangalito.annotations.Storage;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Storage(value = "customer")
public class Customer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Key
    private Integer id;
    private String name;
    private String email;

    public Customer(Integer id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Customer(String name, String email) {
        this(COUNTER.incrementAndGet(), name, email);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) && Objects.equals(name, customer.name) && Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
