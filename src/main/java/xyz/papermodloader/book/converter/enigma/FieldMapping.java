package xyz.papermodloader.book.converter.enigma;

public class FieldMapping {
    private String obf;
    private String deobf;
    private String descriptor;

    public FieldMapping(String obf, String deobf, String descriptor) {
        this.obf = obf;
        this.deobf = deobf;
        this.descriptor = descriptor;
    }

    public String getObf() {
        return obf;
    }

    public String getDeobf() {
        return deobf;
    }

    public String getDescriptor() {
        return descriptor;
    }
}
