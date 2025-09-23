package dev.hangalito.litestore;

import dev.hangalito.litestore.annotations.Key;
import dev.hangalito.litestore.annotations.Storable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Storable
public class Instance implements Serializable {
    @Key
    private UUID pk;
    private String field;
    private LocalDate instant;

    public Instance() {
        pk = UUID.randomUUID();
    }

    public Instance(String field, LocalDate instant) {
        this();
        this.field = field;
        this.instant = instant;
    }

    public UUID getPk() {
        return pk;
    }

    public void setPk(UUID pk) {
        this.pk = pk;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public LocalDate getInstant() {
        return instant;
    }

    public void setInstant(LocalDate instant) {
        this.instant = instant;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Instance instance)) return false;

        return Objects.equals(pk, instance.pk) && Objects.equals(
                field, instance.field) && Objects.equals(instant, instance.instant);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(pk);
        result = 31 * result + Objects.hashCode(field);
        result = 31 * result + Objects.hashCode(instant);
        return result;
    }

    @Override
    public String toString() {
        return field;
    }
}
