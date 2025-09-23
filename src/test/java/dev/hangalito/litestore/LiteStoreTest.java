package dev.hangalito.litestore;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LiteStoreTest {
    static Instance a;
    static Instance b;
    static Instance c;
    static Datasource<Instance, UUID> ds;

    @BeforeAll
    static void setup() {
        a = new Instance("Instance A", LocalDate.now());
        b = new Instance("Instance B", LocalDate.now());
        c = new Instance("Instance C", LocalDate.now());

        ds = new Datasource<>();
        ds.init(Instance.class);
    }

    @Test
    @Order(1)
    void testSaveInstances() {
        assertDoesNotThrow(() -> {
            ds.save(a);
            ds.save(b);
            ds.save(c);
        });
        List<Instance> expected = new ArrayList<>(List.of(a, b, c));
        expected.sort(Comparator.comparing(Instance::getPk));
        var actual = ds.findAll();
        actual.sort(Comparator.comparing(Instance::getPk));
        assertEquals(expected, actual);
    }

    @Test
    @Order(3)
    void testFindById() {
        var expected = a.getPk();
        assertTrue(ds.findById(a.getPk()).isPresent());
        assertEquals(expected, ds.findById(a.getPk()).get().getPk());
    }

    @Test
    @Order(4)
    void testDelete() {
        ds.delete(a);
        var expected = new ArrayList<>(List.of(b, c));
        var actual = ds.findAll();
        expected.sort(Comparator.comparing(Instance::getPk));
        actual.sort(Comparator.comparing(Instance::getPk));
        assertEquals(expected, actual);
    }

    @AfterAll
    static void terminator() {
        ds.findAll().forEach(ds::delete);
    }
}
