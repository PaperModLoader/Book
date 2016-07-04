package xyz.papermodloader.book.mapping;

import com.google.gson.Gson;

public class MappedField {
    private String unmappedName;
    private String name;
    private String descriptor;
    private String javadoc;

    public void setUnmappedName(String unmappedName) {
        this.unmappedName = unmappedName;
    }

    public String getUnmappedName() {
        return this.unmappedName;
    }

    public String getName() {
        return this.name;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public String getJavadoc() {
        return this.javadoc;
    }

    @Override
    public String toString() {
        return "MappedField" + new Gson().toJson(this);
    }
}
