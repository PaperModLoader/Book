package xyz.papermodloader.book.mapping;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MappedMethod {
    private JsonObject object;
    private String obf;
    private String deobf;
    private String descriptor;
    private String javadoc;
    private MappedParameter[] parameters;

    public MappedMethod(JsonObject object) {
        this.object = object;
        this.obf = this.object.get("obf").getAsString();
        this.deobf = this.object.get("deobf").getAsString();
        this.descriptor = this.object.get("desc").getAsString();
        this.javadoc = this.object.get("javadoc").getAsString();
        JsonArray parameters = object.get("parameters").getAsJsonArray();
        this.parameters = new MappedParameter[parameters.size()];
        for (int i = 0; i < this.parameters.length; i++) {
            this.parameters[i] = new MappedParameter(parameters.get(i).getAsJsonObject());
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

    public String getDescriptor() {
        return this.descriptor;
    }

    public String getJavadoc() {
        return this.javadoc;
    }

    public MappedParameter[] getParameters() {
        return this.parameters;
    }
}
