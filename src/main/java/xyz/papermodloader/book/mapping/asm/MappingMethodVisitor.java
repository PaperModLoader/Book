package xyz.papermodloader.book.mapping.asm;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import xyz.papermodloader.book.mapping.Mappings;

public class MappingMethodVisitor extends MethodVisitor {
    private Mappings mappings;

    private String name;
    private String owner;
    private String descriptor;

    private int localVariableIndex = 1;

    public MappingMethodVisitor(String name, String owner, String descriptor, Mappings mappings, int api, MethodVisitor visitor) {
        super(api, visitor);
        this.name = name;
        this.owner = owner;
        this.descriptor = descriptor;
        this.mappings = mappings;
    }

    @Override
    public void visitFrame(int type, int localCount, Object[] locals, int stackCount, Object[] stack) {
        super.visitFrame(type, localCount, this.mapFrame(locals, localCount), stackCount, this.mapFrame(stack, stackCount));
    }

    private Object[] mapFrame(Object[] array, int count) {
        for (int i = 0; i < count; i++) {
            Object object = array[i];
            array[i] = object instanceof String ? this.mappings.getClassMapping((String) object) : object;
        }
        return array;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, this.mappings.getClassMapping(owner), this.mappings.getFieldMappingName(owner, name, descriptor), this.mappings.mapDescriptor(descriptor));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, this.mappings.getClassMapping(owner), this.mappings.getMethodMappingName(owner, name, descriptor), this.mappings.mapDescriptor(descriptor), isInterface);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, this.mappings.getClassMapping(type));
    }

    @Override
    public void visitLdcInsn(Object cst) {
        if (cst instanceof Type) {
            super.visitLdcInsn(this.mappings.mapType((Type) cst));
            return;
        } else if (cst instanceof Handle) {
            Handle handle = (Handle) cst;
            super.visitLdcInsn(new Handle(handle.getTag(), this.mappings.getClassMapping(handle.getOwner()), this.mappings.getMethodMappingName(handle.getOwner(), handle.getName(), handle.getDesc()), this.mappings.mapDescriptor(handle.getDesc()), handle.isInterface()));
            return;
        }
        super.visitLdcInsn(cst);
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
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        descriptor = this.mappings.mapDescriptor(descriptor);
        String mappedParameterName = this.mappings.getParameterName(this.owner, this.name, this.descriptor, index);
        if (mappedParameterName == null && !name.equals("this")) {
            String className = Type.getType(descriptor).getClassName();
            switch (className) {
                case "int":
                    className = "i";
                    break;
                case "boolean":
                    className = "b";
                    break;
                case "double":
                    className = "d";
                    break;
                case "float":
                    className = "f";
                    break;
            }
            if (className.length() > 0) {
                name = (className.contains(".") ? className.substring(className.lastIndexOf('.') + 1) : className);
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1) + this.localVariableIndex++;
            }
        } else if (mappedParameterName != null) {
            name = mappedParameterName;
        }
        super.visitLocalVariable(name, descriptor, this.mappings.mapSignature(signature, true, this.api), start, end, index);
    }
}
