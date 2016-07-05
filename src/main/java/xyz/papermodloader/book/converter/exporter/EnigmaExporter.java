package xyz.papermodloader.book.converter.exporter;

import xyz.papermodloader.book.mapping.MappedField;
import xyz.papermodloader.book.mapping.MappedMethod;
import xyz.papermodloader.book.mapping.Mappings;
import xyz.papermodloader.book.util.ProgressLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnigmaExporter implements Exporter {
    @Override
    public void export(File input, File output, ProgressLogger logger) throws IOException {
        if (!input.exists()) {
            throw new RuntimeException("Input doesn't exist!");
        }

        InputStream stream = new FileInputStream(input);
        Mappings mappings = new Mappings(stream);
        stream.close();

        PrintWriter out = new PrintWriter(output);
        List<Map.Entry<String, String>> list = new ArrayList<>(mappings.getClassMappings().entrySet());
        for (int i = 0; i < list.size(); i++) {
            Map.Entry<String, String> entry = list.get(i);
            logger.onProgress(i, list.size() - 1);
            out.println("CLASS " + entry.getKey() + (entry.getKey().equals(entry.getValue()) ? "" : " " + entry.getValue()));
            for (MappedField field : mappings.getFieldMappings(entry.getKey())) {
                out.println("\tFIELD " + field.getUnmappedName() + " " + field.getName() + " " + field.getDescriptor());
            }
            for (MappedMethod method : mappings.getMethodMappings(entry.getKey())) {
                out.println("\tMETHOD " + method.getUnmappedName() + " " + method.getName() + " " + method.getDescriptor());
                for (int j = 0; j < method.getParameters().length; j++) {
                    out.println("\t\tARG " + j + " " + method.getParameters()[j]);
                }
            }
        }
        out.close();
    }
}
