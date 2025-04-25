package dev.hangalito;

import dev.hangalito.storage.Datasource;
import dev.hangalito.test.ProgrammingLanguage;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        var languages = List.of(
                new ProgrammingLanguage("Java", 5d),
                new ProgrammingLanguage("Kotlin", 4d),
                new ProgrammingLanguage("Java", 4.),
                new ProgrammingLanguage("Java", .5),
                new ProgrammingLanguage("Java", 3.5),
                new ProgrammingLanguage("Java", 4),
                new ProgrammingLanguage("Java", 5d),
                new ProgrammingLanguage("Java", 5d),
                new ProgrammingLanguage("C", 3.5),
                new ProgrammingLanguage("C++", 3d),
                new ProgrammingLanguage("C#", 2d),
                new ProgrammingLanguage("Rust", 4d),
                new ProgrammingLanguage("Dart", 4d),
                new ProgrammingLanguage("JavaScript", 4d),
                new ProgrammingLanguage("JavaScript", 3.5),
                new ProgrammingLanguage("JavaScript", 4.5),
                new ProgrammingLanguage("JavaScript", 4d),
                new ProgrammingLanguage("JavaScript", 2.5),
                new ProgrammingLanguage("CSS", 1d),
                new ProgrammingLanguage("HTML", 4d),
                new ProgrammingLanguage("HTML", 2d),
                new ProgrammingLanguage("HTML", 1d),
                new ProgrammingLanguage("Groovy", 2d),
                new ProgrammingLanguage("Groovy", 1d)
        );
        Datasource<ProgrammingLanguage, Integer> ds = new Datasource<>();
        ds.init(ProgrammingLanguage.class);

        String lang = "Java";
        System.out.println("Searching for the programming language " + lang);
        ds.where("name", lang).forEach(System.out::println);
    }
}
