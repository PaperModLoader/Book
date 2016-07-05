package xyz.papermodloader.book.mapping.enigma;

import java.util.ArrayList;
import java.util.List;

public class ClassMapping {
    private String obf;
    private String deobf;
    private List<FieldMapping> fieldMappings = new ArrayList<>();
    private List<MethodMapping> methodMappings = new ArrayList<>();
    private List<ClassMapping> innerClasses = new ArrayList<>();

    public ClassMapping(String obf, String deobf) {
        this.obf = obf;
        this.deobf = deobf;
    }

    public String getObf() {
        return this.obf;
    }

    public String getDeobf() {
        return this.deobf;
    }

    public List<FieldMapping> getFieldMappings() {
        return this.fieldMappings;
    }

    public List<MethodMapping> getMethodMappings() {
        return this.methodMappings;
    }

    public List<ClassMapping> getInnerClasses() {
        return this.innerClasses;
    }
}
