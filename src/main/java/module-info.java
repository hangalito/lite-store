/**
 * Lite store module declaration.
 */
module lite.store {
    requires java.base;

    opens dev.hangalito.litestore to java.base;

    exports dev.hangalito.litestore.annotations;
    exports dev.hangalito.litestore.exceptions;
    exports dev.hangalito.litestore;
}
