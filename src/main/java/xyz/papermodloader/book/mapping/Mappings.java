package xyz.papermodloader.book.mapping;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;
import xyz.papermodloader.book.asm.MappingSignatureVisitor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mappings {
    private Map<String, JsonObject> classMappings;
    private Map<String, List<MappedField>> fieldMappings;
    private Map<String, List<MappedMethod>> methodMappings;

    public Mappings(InputStream stream) {
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();
        JsonObject root = parser.parse(new InputStreamReader(stream)).getAsJsonObject();
        this.classMappings = new HashMap<>();
        this.fieldMappings = new HashMap<>();
        this.methodMappings = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();
            this.classMappings.put(entry.getKey(), object);
            List<MappedField> fields = new ArrayList<>();
            for (Map.Entry<String, JsonElement> field : object.get("fields").getAsJsonObject().entrySet()) {
                MappedField mappedField = gson.fromJson(field.getValue(), MappedField.class);
                mappedField.setUnmappedName(field.getKey());
                fields.add(mappedField);
            }
            this.fieldMappings.put(entry.getKey(), fields);
            List<MappedMethod> methods = new ArrayList<>();
            for (Map.Entry<String, JsonElement> method : object.get("methods").getAsJsonObject().entrySet()) {
                MappedMethod mappedMethod = gson.fromJson(method.getValue(), MappedMethod.class);
                mappedMethod.setUnmappedName(method.getKey());
                methods.add(mappedMethod);
            }
            this.methodMappings.put(entry.getKey(), methods);
        }
    }

    public Map<String, String> getClassMappings() {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, JsonObject> entry : this.classMappings.entrySet()) {
            map.put(entry.getKey(), entry.getValue().get("name").getAsString());
        }
        return map;
    }

    public boolean hasClassMappings(String cls) {
        return this.classMappings.containsKey(cls);
    }

    public String getClassMapping(String cls) {
        if (cls != null && this.hasClassMappings(cls)) {
            return this.classMappings.get(cls).get("name").getAsString();
        } else {
            return cls;
        }
    }

    public List<MappedField> getFieldMappings(String cls) {
        if (this.hasClassMappings(cls)) {
            return this.fieldMappings.get(cls);
        } else {
            return new ArrayList<>();
        }
    }

    public List<MappedMethod> getMethodMappings(String cls) {
        if (this.hasClassMappings(cls)) {
            return this.methodMappings.get(cls);
        } else {
            return new ArrayList<>();
        }
    }

    public MappedMethod getMethodMapping(String cls, String method, String descriptor) {
        for (MappedMethod mapping : this.getMethodMappings(cls)) {
            if (mapping.getUnmappedName().equals(method) && mapping.getDescriptor().equals(descriptor)) {
                return mapping;
            }
        }
        return null;
    }

    public MappedField getFieldMapping(String cls, String field, String descriptor) {
        for (MappedField mapping : this.getFieldMappings(cls)) {
            if (mapping.getUnmappedName().equals(field) && mapping.getDescriptor().equals(descriptor)) {
                return mapping;
            }
        }
        return null;
    }

    public String getMethodMappingName(String cls, String method, String descriptor) {
        MappedMethod mapping = this.getMethodMapping(cls, method, descriptor);
        return mapping != null ? mapping.getName() : method;
    }

    public String getFieldMappingName(String cls, String field, String descriptor) {
        MappedField mapping = this.getFieldMapping(cls, field, descriptor);
        return mapping != null ? mapping.getName() : field;
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
            if (c == 'L') {
                clazz = new StringBuilder();
            } else if (c == ';') {
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
        MappedMethod method = this.getMethodMapping(owner, name, descriptor);
        if (method != null) {
            String[] parameters = method.getParameters();
            if (parameters != null && parameterIndex < parameters.length) {
                return parameters[parameterIndex];
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
}
