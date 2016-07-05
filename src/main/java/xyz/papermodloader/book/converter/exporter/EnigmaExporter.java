package xyz.papermodloader.book.converter.exporter;

import xyz.papermodloader.book.mapping.*;
import xyz.papermodloader.book.util.ProgressLogger;

import java.io.*;

public class EnigmaExporter implements Exporter {
    private ProgressLogger logger;
    private int current;
    private int total;

    @Override
    public void export(File input, File output, ProgressLogger logger) throws IOException {
        if (!input.exists()) {
            throw new RuntimeException("Input doesn't exist!");
        }

        this.logger = logger;

        InputStream stream = new FileInputStream(input);
        Mappings mappings = Mappings.parseMappings(stream);
        stream.close();

        PrintWriter out = new PrintWriter(output);

        MappedClass[] classes = mappings.getClasses();
        this.total = classes.length;
        for (MappedClass mappedClass : classes) {
            this.write(out, mappedClass, mappings, "", "", 0);
        }
        out.close();
    }

    private void write(PrintWriter out, MappedClass mappedClass, Mappings mappings, String parentNameObf, String parentNameDeobf, int indentation) {
        this.logger.onProgress(this.current+++ 1, this.total);
        String obf = parentNameObf + mappedClass.getObf();
        String deobf = parentNameDeobf + mappedClass.getDeobf();
        out.println(this.indent("CLASS " + mappings.addNone(obf) + (mappedClass.getObf().equals(mappedClass.getDeobf()) ? "" : " " + mappedClass.getDeobf()), indentation));
        for (MappedClass inner : mappedClass.getClasses()) {
            this.total++;
            this.write(out, inner, mappings, obf + "$", deobf + "$", indentation + 1);
        }
        for (MappedField field : mappings.getFieldMappings(obf)) {
            out.println(this.indent("FIELD " + field.getObf() + (field.getObf().equals(field.getDeobf()) ? "" : " " + field.getDeobf()) + " " + mappings.addNoneDescriptor(field.getDescriptor()), indentation + 1));
        }
        for (MappedMethod method : mappings.getMethodMappings(obf)) {
            out.println(this.indent("METHOD " + method.getObf() + (method.getObf().equals(method.getDeobf()) ? "" : " " + method.getDeobf()) + " " + mappings.addNoneDescriptor(method.getDescriptor()), indentation + 1));
            for (MappedParameter parameter : method.getParameters()) {
                out.println(this.indent("ARG " + parameter.getID() + " " + parameter.getDeobf(), indentation + 2));
            }
        }
    }

    private String indent(String text, int amount) {
        StringBuilder builder = new StringBuilder(text);
        for (int i = 0; i < amount; i++) {
            builder.insert(0, "\t");
        }
        return builder.toString();
    }
}
