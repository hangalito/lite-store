import java.io.Serializable;

@Storable
class Gerenciavel implements Serializable {

    @Key
    private int key;
    //... outros atributos
}
