package xyz.papermodloader.book.converter.enigma;

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
        return obf;
    }

    public String getDeobf() {
        return deobf;
    }

    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    public List<MethodMapping> getMethodMappings() {
        return methodMappings;
    }

    public List<ClassMapping> getInnerClasses() {
        return innerClasses;
    }
}
