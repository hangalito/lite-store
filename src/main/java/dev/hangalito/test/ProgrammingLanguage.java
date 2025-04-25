package dev.hangalito.test;

import dev.hangalito.annotations.Key;
import dev.hangalito.annotations.Storage;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Storage
public class ProgrammingLanguage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger counter = new AtomicInteger(0);

    @Key
    private Integer id;
    private String name;
    private Double rating;

    public ProgrammingLanguage(Integer id, String name, double rating) {
        this.id = id;
        this.name = name;
        this.rating = rating;
    }


    public ProgrammingLanguage(String name, double rating) {
        this(counter.incrementAndGet(), name, rating);
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ProgrammingLanguage that = (ProgrammingLanguage) object;
        return Double.compare(rating, that.rating) == 0 && Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return "ProgrammingLanguage{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", rating=" + rating +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, rating);
    }

}
