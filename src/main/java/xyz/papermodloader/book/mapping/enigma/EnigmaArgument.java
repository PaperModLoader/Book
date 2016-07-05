package xyz.papermodloader.book.mapping.enigma;

public class EnigmaArgument {
    private int index;
    private String deobf;

    public EnigmaArgument(int index, String deobf) {
        this.index = index;
        this.deobf = deobf;
    }

    public int getIndex() {
        return this.index;
    }

    public String getDeobf() {
        return this.deobf;
    }
}
