package xyz.papermodloader.book.mapping;

import com.google.gson.Gson;

public class MappedMethod extends MappedField {
    private String[] parameters;

    public String[] getParameters() {
        return this.parameters;
    }

    @Override
    public String toString() {
        return "MappedMethod" + new Gson().toJson(this);
    }
}
