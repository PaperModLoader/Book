package xyz.papermodloader.book.mapping;

import com.google.gson.JsonObject;

public class MappedField {
    private JsonObject object;
    private String obf;
    private String deobf;
    private String descriptor;
    private String javadoc;

    public MappedField(JsonObject object) {
        this.object = object;
        this.obf = this.object.get("obf").getAsString();
        this.deobf = this.object.get("deobf").getAsString();
        this.descriptor = this.object.get("desc").getAsString();
        this.javadoc = this.object.get("javadoc").getAsString();
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

    public String getDescriptor() {
        return this.descriptor;
    }

    public String getJavadoc() {
        return this.javadoc;
    }
}
