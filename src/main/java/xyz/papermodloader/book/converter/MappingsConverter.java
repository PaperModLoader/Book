package xyz.papermodloader.book.converter;

import xyz.papermodloader.book.converter.exporter.Exporter;
import xyz.papermodloader.book.converter.exporter.Exporters;
import xyz.papermodloader.book.util.Arguments;
import xyz.papermodloader.book.util.ConsoleProgressLogger;

import java.io.File;
import java.io.IOException;

public class MappingsConverter {
    public static void main(String[] args) throws IOException {
        Arguments arguments = new Arguments(args);

        if (!arguments.has("to") || !arguments.has("input") || !arguments.has("output")) {
            throw new RuntimeException("Missing arguments!");
        }

        File input = new File(arguments.get("input"));
        File output = new File(arguments.get("output"));
        Exporter exporter = Exporters.getExporter(arguments.get("to"));

        if (exporter == null) {
            throw new RuntimeException("Invalid type '" + arguments.get("to") + "'!");
        }

        exporter.export(input, output, new ConsoleProgressLogger());
    }
}
