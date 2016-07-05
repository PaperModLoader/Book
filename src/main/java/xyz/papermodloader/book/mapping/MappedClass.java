package xyz.papermodloader.book.mapping;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MappedClass {
    private JsonObject object;
    private String obf;
    private String deobf;
    private String javadoc;
    private MappedField[] fields;
    private MappedMethod[] methods;
    private MappedClass[] classes;

    public MappedClass(JsonObject object) {
        this.object = object;
        this.obf = this.object.get("obf").getAsString();
        this.deobf = this.object.get("deobf").getAsString();
        this.javadoc = this.object.get("javadoc").getAsString();
        JsonArray fields = this.object.get("fields").getAsJsonArray();
        this.fields = new MappedField[fields.size()];
        for (int i = 0; i < this.fields.length; i++) {
            this.fields[i] = new MappedField(fields.get(i).getAsJsonObject());
        }
        JsonArray methods = this.object.get("methods").getAsJsonArray();
        this.methods = new MappedMethod[methods.size()];
        for (int i = 0; i < this.methods.length; i++) {
            this.methods[i] = new MappedMethod(methods.get(i).getAsJsonObject());
        }
        JsonArray classes = this.object.get("classes").getAsJsonArray();
        this.classes = new MappedClass[classes.size()];
        for (int i = 0; i < this.classes.length; i++) {
            this.classes[i] = new MappedClass(classes.get(i).getAsJsonObject());
        }
    }

    public JsonObject getObject() {
        return this.object;
    }

    public String getObf() {
        return this.obf;
    }

    public String getDeobf() {
        return this.deobf;
    }

    public String getJavadoc() {
        return this.javadoc;
    }

    public MappedField[] getFields() {
        return this.fields;
    }

    public MappedMethod[] getMethods() {
        return this.methods;
    }

    public MappedClass[] getClasses() {
        return this.classes;
    }
}
