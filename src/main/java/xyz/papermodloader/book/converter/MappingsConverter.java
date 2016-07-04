package xyz.papermodloader.book.converter;

import com.google.gson.*;
import org.apache.commons.lang3.StringEscapeUtils;
import xyz.papermodloader.book.util.Arguments;
import xyz.papermodloader.book.converter.enigma.ArgumentMapping;
import xyz.papermodloader.book.converter.enigma.ClassMapping;
import xyz.papermodloader.book.converter.enigma.FieldMapping;
import xyz.papermodloader.book.converter.enigma.MethodMapping;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MappingsConverter {
    public static void main(String[] args) throws IOException {
        Arguments arguments = new Arguments(args);

        if (!arguments.has("input") || !arguments.has("output")) {
            throw new RuntimeException("Missing arguments!");
        }

        File input = new File(arguments.get("input"));
        File output = new File(arguments.get("output"));

        if (!input.exists()) {
            throw new RuntimeException("Input doesn't exist!");
        }

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

        MappingsConverter.export(root, null, mappings);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        PrintWriter out = new PrintWriter(output);
        out.println(StringEscapeUtils.unescapeJson(gson.toJson(root)));
        out.close();
    }

    private static void export(JsonObject root, ClassMapping parent, List<ClassMapping> mappings) {
        for (ClassMapping mapping : mappings) {
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
            MappingsConverter.export(root, mapping, mapping.getInnerClasses());
            root.add(mapping.getObf(), object);
        }
    }
}
