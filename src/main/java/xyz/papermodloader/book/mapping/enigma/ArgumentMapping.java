package xyz.papermodloader.book.mapping.enigma;

public class ArgumentMapping {
    private int index;
    private String deobf;

    public ArgumentMapping(int index, String deobf) {
        this.index = index;
        this.deobf = deobf;
    }

    public int getIndex() {
        return index;
    }

    public String getDeobf() {
        return deobf;
    }
}
