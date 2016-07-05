package xyz.papermodloader.book.mapping;

import com.google.gson.JsonObject;

public class MappedParameter {
    private JsonObject object;
    private int id;
    private String deobf;

    public MappedParameter(JsonObject object) {
        this.object = object;
        this.id = this.object.get("id").getAsInt();
        this.deobf = this.object.get("deobf").getAsString();
    }

    public JsonObject getObject() {
        return this.object;
    }

    public int getID() {
        return this.id;
    }

    public String getDeobf() {
        return this.deobf;
    }
}
