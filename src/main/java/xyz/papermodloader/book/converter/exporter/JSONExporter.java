package xyz.papermodloader.book.converter.exporter;

import com.google.gson.*;
import org.apache.commons.lang3.StringEscapeUtils;
import xyz.papermodloader.book.mapping.enigma.ArgumentMapping;
import xyz.papermodloader.book.mapping.enigma.ClassMapping;
import xyz.papermodloader.book.mapping.enigma.FieldMapping;
import xyz.papermodloader.book.mapping.enigma.MethodMapping;
import xyz.papermodloader.book.util.ProgressLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JSONExporter implements Exporter {
    private ProgressLogger logger;
    private int current;
    private int total;

    @Override
    public void export(File input, File output, ProgressLogger logger) throws IOException {
        if (!input.exists()) {
            throw new RuntimeException("Input doesn't exist!");
        }

        this.logger = logger;

        List<ClassMapping> mappings = new ArrayList<>();
        List<ClassMapping> parents = new ArrayList<>();
        ClassMapping lastClass = null;
        MethodMapping lastMethod = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("CLASS")) {
                    int index = line.indexOf("CLASS");
                    if (index == 0) {
                        parents.clear();
                        String[] parts = line.split(" ");
                        String obf = parts[1].replace("none/", "");
                        lastClass = new ClassMapping(obf, parts.length == 3 ? parts[2] : obf);
                        parents.add(lastClass);
                        mappings.add(lastClass);
                    } else {
                        String[] parts = line.split(" ");
                        String obf = parts[1].replace("none/", "");
                        lastClass = new ClassMapping(obf, parts.length == 3 ? parts[2] : obf);
                        parents.get(index - 1).getInnerClasses().add(lastClass);
                        parents.add(lastClass);
                    }
                } else if (line.startsWith("\tFIELD")) {
                    String[] parts = line.split(" ");
                    if (lastClass != null) {
                        String obf = parts[3].replace("none/", "");
                        lastClass.getFieldMappings().add(new FieldMapping(parts[1], parts[2], obf));
                    }
                } else if (line.startsWith("\tMETHOD")) {
                    String[] parts = line.split(" ");
                    if (parts.length == 3) {
                        String obf = parts[2].replace("none/", "");
                        lastMethod = new MethodMapping(parts[1], parts[1], obf);
                    } else {
                        String obf = parts[3].replace("none/", "");
                        lastMethod = new MethodMapping(parts[1], parts[2], obf);
                    }
                    lastClass.getMethodMappings().add(lastMethod);
                } else if (line.startsWith("\t\tARG")) {
                    String[] parts = line.split(" ");
                    if (lastMethod != null) {
                        lastMethod.getArgumentMappings().add(new ArgumentMapping(Integer.parseInt(parts[1]), parts[2]));
                    }
                }
            }
        }

        JsonObject root = new JsonObject();

        this.write(root, null, mappings);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        PrintWriter out = new PrintWriter(output);
        out.println(StringEscapeUtils.unescapeJson(gson.toJson(root)));
        out.close();
    }

    private void write(JsonObject root, ClassMapping parent, List<ClassMapping> mappings) {
        this.total += mappings.size();
        for (ClassMapping mapping : mappings) {
            this.logger.onProgress(this.current++, this.total);
            JsonObject object = new JsonObject();
            object.addProperty("name", (parent != null && !mapping.getObf().equals(mapping.getDeobf()) ? parent.getDeobf() + "$" : "") + mapping.getDeobf());
            JsonObject fieldsObject = new JsonObject();
            for (FieldMapping fieldMapping : mapping.getFieldMappings()) {
                JsonObject fieldObject = new JsonObject();
                fieldObject.addProperty("name", fieldMapping.getDeobf());
                fieldObject.addProperty("descriptor", fieldMapping.getDescriptor());
                fieldsObject.add(fieldMapping.getObf(), fieldObject);
            }
            object.add("fields", fieldsObject);
            JsonObject methodsObject = new JsonObject();
            for (MethodMapping methodMapping : mapping.getMethodMappings()) {
                JsonObject methodObject = new JsonObject();
                methodObject.addProperty("name", methodMapping.getDeobf());
                methodObject.addProperty("descriptor", methodMapping.getDescriptor());
                JsonArray argumentsObject = new JsonArray();
                for (ArgumentMapping argumentMapping : methodMapping.getArgumentMappings()) {
                    argumentsObject.add(new JsonPrimitive(argumentMapping.getDeobf()));
                }
                methodObject.add("parameters", argumentsObject);
                methodsObject.add(methodMapping.getObf(), methodObject);
            }
            object.add("methods", methodsObject);
            this.write(root, mapping, mapping.getInnerClasses());
            root.add(mapping.getObf(), object);
        }
    }
}
