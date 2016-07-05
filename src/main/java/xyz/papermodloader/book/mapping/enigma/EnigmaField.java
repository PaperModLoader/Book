package xyz.papermodloader.book.mapping.enigma;

public class EnigmaField {
    private String obf;
    private String deobf;
    private String descriptor;

    public EnigmaField(String obf, String deobf, String descriptor) {
        this.obf = obf;
        this.deobf = deobf;
        this.descriptor = descriptor;
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
}
