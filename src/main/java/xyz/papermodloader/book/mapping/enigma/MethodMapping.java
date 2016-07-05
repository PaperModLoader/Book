package xyz.papermodloader.book.mapping.enigma;

import java.util.ArrayList;
import java.util.List;

public class MethodMapping {
    private String obf;
    private String deobf;
    private String descriptor;
    private List<ArgumentMapping> argumentMappings = new ArrayList<>();

    public MethodMapping(String obf, String deobf, String descriptor) {
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

    public List<ArgumentMapping> getArgumentMappings() {
        return argumentMappings;
    }
}
