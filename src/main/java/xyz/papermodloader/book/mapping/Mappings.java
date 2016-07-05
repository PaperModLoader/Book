package xyz.papermodloader.book.mapping;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;
import xyz.papermodloader.book.asm.MappingSignatureVisitor;

import java.io.InputStream;
import java.io.InputStreamReader;

public class Mappings {
    private MappedClass[] classes;

    private Mappings(MappedClass[] classes) {
        this.classes = classes;
    }

    public static Mappings parseMappings(InputStream stream) {
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(new InputStreamReader(stream)).getAsJsonArray();
        MappedClass[] classes = new MappedClass[array.size()];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = new MappedClass(array.get(i).getAsJsonObject());
        }
        return new Mappings(classes);
    }

    public MappedClass getMappedClass(String obf) {
        if (obf == null) {
            return null;
        }
        String[] split = obf.contains("$") ? obf.split("\\$") : null;
        if (split == null) {
            for (MappedClass mappedClass : this.classes) {
                if (mappedClass.getObf().equals(obf)) {
                    return mappedClass;
                }
            }
        } else {
            MappedClass prevParent = null;
            for (String parent : split) {
                if (prevParent == null) {
                    prevParent = this.getMappedClass(parent);
                    if (prevParent == null) {
                        return null;
                    }
                } else {
                    MappedClass innerClass =  null;
                    for (MappedClass inner : prevParent.getClasses()) {
                        if (inner.getObf().equals(parent)) {
                            innerClass = inner;
                            break;
                        }
                    }
                    if (innerClass == null) {
                        return null;
                    }
                    prevParent = innerClass;
                }
            }
            return prevParent;
        }
        return null;
    }

    public String getClassMapping(String obf) {
        if (obf == null) {
            return null;
        }
        String[] split = obf.contains("$") ? obf.split("\\$") : null;
        String name = "";
        if (split == null) {
            for (MappedClass mappedClass : this.classes) {
                if (mappedClass.getObf().equals(obf)) {
                    return mappedClass.getDeobf();
                }
            }
        } else {
            MappedClass prevParent = null;
            for (String parent : split) {
                if (prevParent == null) {
                    prevParent = this.getMappedClass(parent);
                    if (prevParent == null) {
                        return obf;
                    }
                    name = prevParent.getDeobf();
                } else {
                    MappedClass innerClass =  null;
                    for (MappedClass inner : prevParent.getClasses()) {
                        if (inner.getObf().equals(parent)) {
                            innerClass = inner;
                            break;
                        }
                    }
                    if (innerClass == null) {
                        return obf;
                    }
                    name += "$" + innerClass.getDeobf();
                    prevParent = innerClass;
                }
            }
            return name;
        }
        return obf;
    }

    public MappedMethod getMethodMapping(String owner, String name, String descriptor) {
        MappedClass mapping = this.getMappedClass(owner);
        if (mapping != null) {
            for (MappedMethod method : mapping.getMethods()) {
                if (method.getObf().equals(name) && method.getDescriptor().equals(descriptor)) {
                    return method;
                }
            }
        }
        return null;
    }

    public MappedField getFieldMapping(String owner, String name, String descriptor) {
        MappedClass mapping = this.getMappedClass(owner);
        if (mapping != null) {
            for (MappedField field : mapping.getFields()) {
                if (field.getObf().equals(name) && field.getDescriptor().equals(descriptor)) {
                    return field;
                }
            }
        }
        return null;
    }

    public MappedClass[] getClasses() {
        return this.classes;
    }

    public String[] mapArray(String[] array) {
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                array[i] = this.getClassMapping(array[i]);
            }
        }
        return array;
    }

    public String mapDescriptor(String descriptor) {
        if (descriptor == null) {
            return null;
        }
        StringBuilder mapped = new StringBuilder();
        StringBuilder clazz = null;
        for (int i = 0; i < descriptor.length(); i++) {
            char c = descriptor.charAt(i);
            if (c == 'L' && clazz == null) {
                clazz = new StringBuilder();
            } else if (c == ';' && clazz != null) {
                mapped.append('L');
                mapped.append(this.getClassMapping(clazz.toString()));
                mapped.append(';');
                clazz = null;
            } else {
                if (clazz == null) {
                    mapped.append(c);
                } else {
                    clazz.append(c);
                }
            }
        }
        return mapped.toString();
    }

    public Type mapType(Type type) {
        switch (type.getSort()) {
            case Type.OBJECT:
                return Type.getObjectType(this.getClassMapping(type.getInternalName()));
            case Type.ARRAY:
                String descriptor = this.mapDescriptor(type.getDescriptor());
                for (int i = 0; i < type.getDimensions(); i++) {
                    descriptor = "[" + descriptor;
                }
                return Type.getType(descriptor);
            case Type.METHOD:
                return Type.getMethodType(this.mapDescriptor(type.getDescriptor()));
        }
        return type;
    }

    public String getParameterName(String owner, String name, String descriptor, int parameterIndex) {
        if (parameterIndex >= 0) {
            MappedMethod method = this.getMethodMapping(owner, name, descriptor);
            if (method != null) {
                MappedParameter[] parameters = method.getParameters();
                if (parameters != null) {
                    for (MappedParameter parameter : parameters) {
                        if (parameter.getID() == parameterIndex) {
                            return parameter.getDeobf();
                        }
                    }
                }
            }
        }
        return null;
    }

    public String mapSignature(String signature, boolean isType, int api) {
        if (signature != null) {
            SignatureReader reader = new SignatureReader(signature);
            final SignatureWriter writer = new SignatureWriter();
            SignatureVisitor visitor = new MappingSignatureVisitor(api, writer, this);
            if (isType) {
                reader.acceptType(visitor);
            } else {
                reader.accept(visitor);
            }
            return writer.toString();
        }
        return null;
    }

    public MappedField[] getFieldMappings(String obf) {
        MappedClass mappedClass = this.getMappedClass(obf);
        return mappedClass != null ? mappedClass.getFields() : new MappedField[0];
    }

    public MappedMethod[] getMethodMappings(String obf) {
        MappedClass mappedClass = this.getMappedClass(obf);
        return mappedClass != null ? mappedClass.getMethods() : new MappedMethod[0];
    }

    public String addNone(String obf) {
        if (!obf.contains("/")) {
            obf = "none/" + obf;
        }
        return obf;
    }

    public String addNoneDescriptor(String descriptor) {
        if (descriptor == null) {
            return null;
        }
        StringBuilder fixed = new StringBuilder();
        StringBuilder clazz = null;
        for (int i = 0; i < descriptor.length(); i++) {
            char c = descriptor.charAt(i);
            if (c == 'L' && clazz == null) {
                clazz = new StringBuilder();
            } else if (c == ';' && clazz != null) {
                fixed.append('L');
                fixed.append(this.addNone(clazz.toString()));
                fixed.append(';');
                clazz = null;
            } else {
                if (clazz == null) {
                    fixed.append(c);
                } else {
                    clazz.append(c);
                }
            }
        }
        return fixed.toString();
    }
}
