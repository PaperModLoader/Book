package xyz.papermodloader.book.converter.exporter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum Exporters {
    JSON(new JSONExporter()),
    ENIGMA(new EnigmaExporter());

    private static final Map<String, Exporter> EXPORTERS = new HashMap<>();
    private Exporter exporter;

    Exporters(Exporter exporter) {
        this.exporter = exporter;
    }

    static {
        for (Exporters exporter : Exporters.values()) {
            Exporters.EXPORTERS.put(exporter.name().toLowerCase(Locale.ENGLISH), exporter.exporter);
        }
    }

    public static Exporter getExporter(String id) {
        return Exporters.EXPORTERS.get(id);
    }
}
