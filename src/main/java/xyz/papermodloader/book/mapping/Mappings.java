package xyz.papermodloader.book.mapping;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import xyz.papermodloader.book.asm.BookSignatureVisitor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mappings {
    private Map<String, MappedClass> classes;
    private Map<String, List<String>> inheritance;
    private Map<String, ClassNode> classNodes;

    private Mappings(MappedClass[] classes) {
        this.classes = new HashMap<>();
        for (MappedClass mappedClass : classes) {
            this.classes.put(mappedClass.getObf(), mappedClass);
        }
    }

    public void setInheritance(Map<String, List<String>> inheritance) {
        this.inheritance = inheritance;
    }

    public void setClassNodes(Map<String, ClassNode> classes) {
        this.classNodes = classes;
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
        if (obf == null || obf.contains("/")) {
            return null;
        }
        Type type = Type.getObjectType(obf);
        if (type.getSort() == Type.ARRAY) {
            if (obf.endsWith(";")) {
                obf = type.getElementType().getDescriptor();
                obf = obf.substring(1, obf.length() - 1);
            }
        }
        String[] split = obf.contains("$") ? obf.split("\\$") : null;
        if (split == null) {
            return this.classes.get(obf);
        } else {
            MappedClass prevParent = null;
            for (String parent : split) {
                if (prevParent == null) {
                    prevParent = this.getMappedClass(parent);
                    if (prevParent == null) {
                        return null;
                    }
                } else {
                    MappedClass innerClass = null;
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
    }

    public String getClassMapping(String obf) {
        if (obf == null) {
            return null;
        }
        if (obf.contains("/")) {
            return obf;
        }
        int dimensions = 0;
        Type type = Type.getObjectType(obf);
        if (type.getSort() == Type.ARRAY) {
            if (obf.endsWith(";")) {
                obf = type.getElementType().getDescriptor();
                obf = obf.substring(1, obf.length() - 1);
                dimensions = type.getDimensions();
            }
        }
        String[] split = obf.contains("$") ? obf.split("\\$") : null;
        String mapping = obf;
        if (split == null) {
            MappedClass mappedClass = this.getMappedClass(obf);
            if (mappedClass != null) {
                mapping = mappedClass.getDeobf();
            }
        } else {
            MappedClass prevParent = null;
            for (String parent : split) {
                if (prevParent == null) {
                    prevParent = this.getMappedClass(parent);
                    if (prevParent == null) {
                        mapping = obf;
                        break;
                    }
                    mapping = prevParent.getDeobf();
                } else {
                    MappedClass innerClass = null;
                    for (MappedClass inner : prevParent.getClasses()) {
                        if (inner.getObf().equals(parent)) {
                            innerClass = inner;
                            break;
                        }
                    }
                    if (innerClass == null) {
                        mapping = obf;
                        break;
                    }
                    mapping += "$" + innerClass.getDeobf();
                    prevParent = innerClass;
                }
            }
        }
        if (dimensions > 0) {
            mapping = "L" + mapping + ";";
            for (int i = 0; i < dimensions; i++) {
                mapping = '[' + mapping;
            }
        }
        return mapping;
    }

    public MappedMethod getMethodMapping(String owner, String name, String descriptor) {
        if (owner == null || name == null) {
            return null;
        }
        return this.getMethodMapping(owner, name, descriptor, this.getAccess(false, owner, name, descriptor));
    }

    public MappedMethod getMethodMapping(String owner, String name, String descriptor, int access) {
        if (owner == null || name == null) {
            return null;
        }
        MappedClass mapping = this.getMappedClass(owner);
        if (mapping != null || access == -1) {
            MappedMethod method = mapping != null ? this.getMethodMapping(name, descriptor, mapping) : null;
            if (method != null) {
                return method;
            } else if (access == -1 || !(Modifier.isPrivate(access) || Modifier.isStatic(access))) {
                List<String> parents = this.inheritance.get(owner);
                if (parents != null) {
                    for (String parent : parents) {
                        method = this.getMethodMapping(parent, name, descriptor, access);
                        if (method != null) {
                            return method;
                        }
                    }
                }
            }
        }
        return null;
    }

    public MappedMethod getMethodMapping(String name, String descriptor, MappedClass mapping) {
        for (MappedMethod method : mapping.getMethods()) {
            if (method.getObf().equals(name) && method.getDescriptor().equals(descriptor)) {
                return method;
            }
        }
        return null;
    }

    public MappedField getFieldMapping(String owner, String name, String descriptor) {
        return this.getFieldMapping(owner, name, descriptor, this.getAccess(true, owner, name, descriptor));
    }

    public MappedField getFieldMapping(String owner, String name, String descriptor, int access) {
        MappedClass mapping = this.getMappedClass(owner);
        if (mapping != null || access == -1) {
            MappedField field = mapping != null ? this.getFieldMapping(name, descriptor, mapping) : null;
            if (field != null) {
                return field;
            } else if (access == -1 || !(Modifier.isPrivate(access) || Modifier.isStatic(access))) {
                List<String> parents = this.inheritance.get(owner);
                if (parents != null) {
                    for (String parent : parents) {
                        field = this.getFieldMapping(parent, name, descriptor, access);
                        if (field != null) {
                            return field;
                        }
                    }
                }
            }
        }
        return null;
    }

    public MappedField getFieldMapping(String name, String descriptor, MappedClass mapping) {
        for (MappedField field : mapping.getFields()) {
            if (field.getObf().equals(name) && field.getDescriptor().equals(descriptor)) {
                return field;
            }
        }
        return null;
    }

    public Map<String, MappedClass> getClasses() {
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

    public String getParameterName(String owner, String name, String descriptor, int parameterIndex, int access) {
        if (parameterIndex >= 0) {
            MappedMethod method = this.getMethodMapping(owner, name, descriptor, access);
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
            SignatureVisitor visitor = new BookSignatureVisitor(api, writer, this);
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

    public Object mapValue(Object cst, int access) {
        if (cst instanceof Type) {
            return this.mapType((Type) cst);
        } else if (cst instanceof Handle) {
            Handle handle = (Handle) cst;
            return new Handle(handle.getTag(), this.getClassMapping(handle.getOwner()), this.getMethodMapping(handle.getOwner(), handle.getName(), handle.getDesc(), access).getDeobf(), this.mapDescriptor(handle.getDesc()), handle.isInterface());
        }
        return cst;
    }

    public int getAccess(boolean field, String owner, String name, String descriptor) {
        ClassNode classNode = this.classNodes.get(owner);
        if (classNode != null) {
            if (field) {
                for (FieldNode fieldNode : classNode.fields) {
                    if (fieldNode.name.equals(name) && fieldNode.desc.equals(descriptor)) {
                        return fieldNode.access;
                    }
                }
            } else {
                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals(name) && methodNode.desc.equals(descriptor)) {
                        return methodNode.access;
                    }
                }
            }
        }
        return -1;
    }
}
