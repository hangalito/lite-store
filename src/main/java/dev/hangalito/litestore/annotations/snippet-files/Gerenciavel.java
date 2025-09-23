import dev.hangalito.litestore.annotations.Key;
import dev.hangalito.litestore.annotations.Storable;

import java.io.Serializable;

@Storable
class Gerenciavel implements Serializable {

    @Key
    private int key;
    //... outros atributos
}
