package xyz.papermodloader.book.converter.exporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringEscapeUtils;
import xyz.papermodloader.book.mapping.enigma.EnigmaArgument;
import xyz.papermodloader.book.mapping.enigma.EnigmaClass;
import xyz.papermodloader.book.mapping.enigma.EnigmaField;
import xyz.papermodloader.book.mapping.enigma.EnigmaMethod;
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

        List<EnigmaClass> mappings = new ArrayList<>();
        List<EnigmaClass> parents = new ArrayList<>();
        EnigmaClass lastClass = null;
        EnigmaMethod lastMethod = null;

        int prevClassIndex = 0;
        int total = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trim = line.trim();
                if (trim.startsWith("CLASS")) {
                    int index = line.indexOf("CLASS");
                    total++;
                    if (index == 0) {
                        parents.clear();
                        String[] parts = line.split(" ");
                        String obf = parts[1].replace("none/", "");
                        lastClass = new EnigmaClass(obf, parts.length == 3 ? parts[2] : obf);
                        parents.add(lastClass);
                        mappings.add(lastClass);
                    } else {
                        if (index <= prevClassIndex) {
                            parents.remove(parents.size() - 1);
                        }
                        String[] parts = line.split(" ");
                        String obf = parts[1].replace("none/", "");
                        lastClass = new EnigmaClass(obf, parts.length == 3 ? parts[2] : obf);
                        parents.get(index - 1).getInnerClasses().add(lastClass);
                        parents.add(lastClass);
                    }
                    prevClassIndex = index;
                } else if (trim.startsWith("FIELD")) {
                    int index = line.indexOf("FIELD");
                    String[] parts = line.split(" ");
                    String obf = parts[3].replace("none/", "");
                    parents.get(index - 1).getEnigmaFields().add(new EnigmaField(parts[1], parts[2], obf));
                } else if (trim.startsWith("METHOD")) {
                    int index = line.indexOf("METHOD");
                    String[] parts = line.split(" ");
                    if (parts.length == 3) {
                        String obf = parts[2].replace("none/", "");
                        lastMethod = new EnigmaMethod(parts[1], parts[1], obf);
                    } else {
                        String obf = parts[3].replace("none/", "");
                        lastMethod = new EnigmaMethod(parts[1], parts[2], obf);
                    }
                    parents.get(index - 1).getEnigmaMethods().add(lastMethod);
                } else if (trim.startsWith("ARG")) {
                    String[] parts = line.split(" ");
                    if (lastMethod != null) {
                        lastMethod.getEnigmaArguments().add(new EnigmaArgument(Integer.parseInt(parts[1]), parts[2]));
                    }
                }
            }
        }
        System.out.println(total);

        JsonArray root = new JsonArray();

        this.write(root, mappings);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        PrintWriter out = new PrintWriter(output);
        out.println(StringEscapeUtils.unescapeJson(gson.toJson(root)));
        out.close();
    }

    private void write(JsonArray root, List<EnigmaClass> mappings) {
        this.total += mappings.size();
        for (EnigmaClass mapping : mappings) {
            this.logger.onProgress(this.current++ + 1, this.total);
            JsonObject object = new JsonObject();
            object.addProperty("obf", mapping.getObf().substring(mapping.getObf().lastIndexOf("$") + 1));
            object.addProperty("deobf", mapping.getDeobf().substring(mapping.getDeobf().lastIndexOf("$") + 1));
            object.addProperty("javadoc", "");
            JsonArray fields = new JsonArray();
            for (EnigmaField field : mapping.getEnigmaFields()) {
                JsonObject fieldObject = new JsonObject();
                fieldObject.addProperty("obf", field.getObf());
                fieldObject.addProperty("deobf", field.getDeobf());
                fieldObject.addProperty("desc", field.getDescriptor());
                fieldObject.addProperty("javadoc", "");
                fields.add(fieldObject);
            }
            object.add("fields", fields);
            JsonArray methods = new JsonArray();
            for (EnigmaMethod method : mapping.getEnigmaMethods()) {
                JsonObject methodObject = new JsonObject();
                methodObject.addProperty("obf", method.getObf());
                methodObject.addProperty("deobf", method.getDeobf());
                methodObject.addProperty("desc", method.getDescriptor());
                methodObject.addProperty("javadoc", "");
                JsonArray parameters = new JsonArray();
                for (EnigmaArgument argument : method.getEnigmaArguments()) {
                    JsonObject argumentObject = new JsonObject();
                    argumentObject.addProperty("id", argument.getIndex());
                    argumentObject.addProperty("deobf", argument.getDeobf());
                    parameters.add(argumentObject);
                }
                methodObject.add("parameters", parameters);
                methods.add(methodObject);
            }
            object.add("methods", methods);
            JsonArray classes = new JsonArray();
            this.write(classes, mapping.getInnerClasses());
            object.add("classes", classes);
            root.add(object);
        }
    }
}
