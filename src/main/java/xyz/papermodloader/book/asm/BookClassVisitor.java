package xyz.papermodloader.book.asm;

import org.objectweb.asm.*;
import xyz.papermodloader.book.mapping.Mappings;

public class BookClassVisitor extends ClassVisitor {
    private Mappings mappings;
    private String name;
    private String obfName;

    public BookClassVisitor(ClassVisitor visitor, Mappings mappings, int api) {
        super(api, visitor);
        this.mappings = mappings;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = this.mappings.getClassMapping(name);
        this.obfName = name;
        super.visit(version, access, this.name, this.mappings.mapSignature(signature, false, this.api), this.mappings.getClassMapping(superName), this.mappings.mapArray(interfaces));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, this.mappings.getMethodMappingName(this.obfName, name, descriptor), this.mappings.mapDescriptor(descriptor), this.mappings.mapSignature(signature, false, this.api), this.mappings.mapArray(exceptions));
        if (visitor != null) {
            return new MappingMethodVisitor(name, this.obfName, descriptor, this.mappings, this.api, visitor);
        }
        return null;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return super.visitField(access, this.mappings.getFieldMappingName(this.obfName, name, descriptor), this.mappings.mapDescriptor(descriptor), this.mappings.mapSignature(signature, true, this.api), value);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        String mapped = this.mappings.getClassMapping(name);
        super.visitInnerClass(mapped, this.mappings.getClassMapping(outerName), mapped.substring(mapped.lastIndexOf(mapped.contains("$") ? '$' : '/') + 1), access);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        super.visitOuterClass(this.mappings.getClassMapping(owner), this.mappings.getMethodMappingName(owner, name, descriptor), this.mappings.mapDescriptor(descriptor));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor visitor = super.visitAnnotation(descriptor, visible);
        if (visitor != null) {
            return new AnnotationVisitor(this.api, visitor) {
                @Override
                public void visit(String name, Object value) {
                    if (value instanceof Type) {
                        super.visit(name, BookClassVisitor.this.mappings.mapType((Type) value));
                        return;
                    } else if (value instanceof Handle) {
                        Handle handle = (Handle) value;
                        super.visit(name, new Handle(handle.getTag(), BookClassVisitor.this.mappings.getClassMapping(handle.getOwner()), BookClassVisitor.this.mappings.getMethodMappingName(handle.getOwner(), handle.getName(), handle.getDesc()), BookClassVisitor.this.mappings.mapDescriptor(handle.getDesc()), handle.isInterface()));
                        return;
                    }
                    super.visit(name, value);
                }

                @Override
                public void visitEnum(String name, String descriptor, String value) {
                    this.av.visitEnum(name, BookClassVisitor.this.mappings.mapDescriptor(descriptor), value);
                }
            };
        }
        return null;
    }
}
