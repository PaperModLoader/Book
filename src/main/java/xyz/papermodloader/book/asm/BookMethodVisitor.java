package xyz.papermodloader.book.asm;

import org.objectweb.asm.*;
import xyz.papermodloader.book.mapping.MappedField;
import xyz.papermodloader.book.mapping.MappedMethod;
import xyz.papermodloader.book.mapping.Mappings;
import xyz.papermodloader.book.util.DefaultedHashMap;

import java.util.Map;

public class BookMethodVisitor extends MethodVisitor {
    private Mappings mappings;

    private String name;
    private String owner;
    private String descriptor;
    private String ownerSignature;

    private Map<String, Integer> variableIndex = new DefaultedHashMap<>(0);

    public BookMethodVisitor(String name, String owner, String descriptor, String ownerSignature, Mappings mappings, int api, MethodVisitor visitor) {
        super(api, visitor);
        this.name = name;
        this.owner = owner;
        this.descriptor = descriptor;
        this.ownerSignature = ownerSignature;
        this.mappings = mappings;
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        AnnotationVisitor visitor = this.mv.visitAnnotationDefault();
        return visitor != null ? new BookAnnotationVisitor(visitor, this.mappings, this.api) : null;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor visitor = this.mv.visitAnnotation(this.mappings.mapDescriptor(descriptor), visible);
        return visitor != null ? new BookAnnotationVisitor(visitor, this.mappings, this.api) : null;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        AnnotationVisitor visitor = this.mv.visitParameterAnnotation(parameter, this.mappings.mapDescriptor(descriptor), visible);
        return visitor != null ? new BookAnnotationVisitor(visitor, this.mappings, this.api) : null;
    }

    @Override
    public void visitFrame(int type, int localCount, Object[] locals, int stackCount, Object[] stack) {
        super.visitFrame(type, localCount, this.mapFrame(locals, localCount), stackCount, this.mapFrame(stack, stackCount));
    }

    private Object[] mapFrame(Object[] array, int count) {
        if (array == null) {
            return null;
        }
        for (int i = 0; i < count; i++) {
            Object object = array[i];
            array[i] = object instanceof String ? this.mappings.getClassMapping((String) object) : object;
        }
        return array;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        MappedField fieldMapping = this.mappings.getFieldMapping(owner, name, descriptor);
        super.visitFieldInsn(opcode, this.mappings.getClassMapping(owner), fieldMapping != null ? fieldMapping.getDeobf() : name, this.mappings.mapDescriptor(descriptor));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        MappedMethod methodMapping = this.mappings.getMethodMapping(owner, name, descriptor, ownerSignature);
        super.visitMethodInsn(opcode, this.mappings.getClassMapping(owner), methodMapping != null ? methodMapping.getDeobf() : name, this.mappings.mapDescriptor(descriptor), isInterface);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, this.mappings.mapType(Type.getObjectType(type)).getInternalName());
    }

    @Override
    public void visitLdcInsn(Object cst) {
        super.visitLdcInsn(this.mappings.mapValue(cst, 0, ownerSignature));
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int dimensions) {
        super.visitMultiANewArrayInsn(this.mappings.mapDescriptor(descriptor), dimensions);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, this.mappings.getClassMapping(type));
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name, this.mappings.mapDescriptor(descriptor), (Handle) this.mappings.mapValue(bsm, 0, ownerSignature), bsmArgs);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        descriptor = this.mappings.mapDescriptor(descriptor);
        if (!name.equals("this")) {
            String mappedParameterName = this.mappings.getParameterName(this.owner, this.name, this.descriptor, index, 0, ownerSignature);
            if (mappedParameterName == null) {
                String className = Type.getType(descriptor).getClassName();
                String simpleClassName = (className.contains(".") ? className.substring(className.lastIndexOf('.') + 1) : className);
                String newClassName = className;
                switch (className) {
                    case "boolean":
                    case "java.lang.Boolean":
                        newClassName = "z";
                        break;
                    case "char":
                    case "java.lang.Character":
                        newClassName = "c";
                        break;
                    case "byte":
                    case "java.lang.Byte":
                        newClassName = "b";
                        break;
                    case "short":
                    case "java.lang.Short":
                        newClassName = "s";
                        break;
                    case "int":
                    case "java.lang.Integer":
                        newClassName = "i";
                        break;
                    case "float":
                    case "java.lang.Float":
                        newClassName = "f";
                        break;
                    case "long":
                    case "java.lang.Long":
                        newClassName = "l";
                        break;
                    case "double":
                    case "java.lang.Double":
                        newClassName = "d";
                        break;
                    case "java.lang.Class":
                        newClassName = "cls";
                        break;
                    case "java.lang.Enum":
                        newClassName = "e";
                        break;
                }
                if (newClassName.endsWith("[]")) {
                    newClassName = newClassName.replaceAll("\\[\\]", "Array");
                }
                if (newClassName.contains("$")) {
                    newClassName = newClassName.substring(newClassName.lastIndexOf('$') + 1);
                }
                if (newClassName.length() > 0) {
                    name = (newClassName.contains(".") ? newClassName.substring(newClassName.lastIndexOf('.') + 1) : newClassName);
                    int variableIndex = this.variableIndex.get(name);
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                    this.variableIndex.put(name, variableIndex++);
                    name += (variableIndex == 1 && !(simpleClassName.equals(name)) ? "" : variableIndex);
                }
            } else {
                name = mappedParameterName;
            }
        }
        super.visitLocalVariable(name, descriptor, this.mappings.mapSignature(signature, true, this.api), start, end, index);
    }
}
