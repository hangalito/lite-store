module lite.store {
    requires java.base;

    opens dev.hangalito.test to java.base;

    exports dev.hangalito.annotations;
    exports dev.hangalito.exceptions;
    exports dev.hangalito.storage;
}
