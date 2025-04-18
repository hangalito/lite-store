package dev.hangalito;

import dev.hangalito.test.ProgrammingLanguage;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Datasource<ProgrammingLanguage, Integer> datasource = new Datasource<>(LocationService.getInstance());
        datasource.init(ProgrammingLanguage.class, Integer.class);
        System.out.println(datasource.load(6));

        Class<ProgrammingLanguage> plc = ProgrammingLanguage.class;
        ProgrammingLanguage typescript = plc.getDeclaredConstructor(String.class, Double.class).newInstance("TypeScript", 3.5);
        System.out.println("typescript = " + typescript);

    }

    private static void populate() throws Exception {
        ProgrammingLanguage java = new ProgrammingLanguage("Java", 5.0);
        ProgrammingLanguage python = new ProgrammingLanguage("Python", 4.5);
        ProgrammingLanguage javascript = new ProgrammingLanguage("JavaScript", 3.0);
        ProgrammingLanguage html = new ProgrammingLanguage("HTML", 4.0);
        ProgrammingLanguage css = new ProgrammingLanguage("CSS", 1.0);
        ProgrammingLanguage c = new ProgrammingLanguage("C", 2.5);
        ProgrammingLanguage cs = new ProgrammingLanguage("C#", .5);
        ProgrammingLanguage cpp = new ProgrammingLanguage("C++", 3.5);
        ProgrammingLanguage kotlin = new ProgrammingLanguage("Kotlin", 4.0);
        ProgrammingLanguage dart = new ProgrammingLanguage("Dart", 2.0);
        var programmingLanguages = List.of(java, python, javascript, html, css, c, cs, cpp, kotlin, dart);
        Datasource<ProgrammingLanguage, Integer> datasource = new Datasource<>(LocationService.getInstance());
        datasource.init(ProgrammingLanguage.class, Integer.class);
        for (var programmingLanguage : programmingLanguages) {
            datasource.save(programmingLanguage, programmingLanguage.id());
            System.out.println("Object: " + programmingLanguage);
            System.out.println();
        }
        System.out.println("Data stored at " + LocationService.getInstance().getAsString());
    }
}
