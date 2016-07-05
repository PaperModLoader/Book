package xyz.papermodloader.book.mapping.enigma;

import java.util.ArrayList;
import java.util.List;

public class EnigmaClass {
    private String obf;
    private String deobf;
    private List<EnigmaField> enigmaFields = new ArrayList<>();
    private List<EnigmaMethod> enigmaMethods = new ArrayList<>();
    private List<EnigmaClass> innerClasses = new ArrayList<>();
    private EnigmaClass parent;

    public EnigmaClass(String obf, String deobf) {
        this.obf = obf;
        this.deobf = deobf;
    }

    public String getObf() {
        return this.obf;
    }

    public String getDeobf() {
        return this.deobf;
    }

    public List<EnigmaField> getEnigmaFields() {
        return this.enigmaFields;
    }

    public List<EnigmaMethod> getEnigmaMethods() {
        return this.enigmaMethods;
    }

    public List<EnigmaClass> getInnerClasses() {
        return this.innerClasses;
    }

    public EnigmaClass getParent() {
        return this.parent;
    }

    public void addInnerClass(EnigmaClass enigmaClass) {
        this.innerClasses.add(enigmaClass);
        enigmaClass.parent = this;
    }

    public int getIndentation() {
        return this.parent != null ? this.parent.getIndentation() + 1 : 0;
    }
}
