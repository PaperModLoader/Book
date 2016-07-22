package xyz.papermodloader.book.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import xyz.papermodloader.book.mapping.MappedField;
import xyz.papermodloader.book.mapping.MappedMethod;
import xyz.papermodloader.book.mapping.Mappings;

public class BookClassVisitor extends ClassVisitor {
    private Mappings mappings;
    private String name;
    private String obfName;
    private String signature;

    public BookClassVisitor(ClassVisitor visitor, Mappings mappings, int api) {
        super(api, visitor);
        this.mappings = mappings;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = this.mappings.getClassMapping(name);
        this.obfName = name;
        this.signature = this.mappings.mapSignature(signature, false, this.api);
        super.visit(version, access, this.name, this.signature, this.mappings.getClassMapping(superName), this.mappings.mapArray(interfaces));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MappedMethod methodMapping = this.mappings.getMethodMapping(this.obfName, name, descriptor, access, this.signature);
        MethodVisitor visitor = super.visitMethod(access, methodMapping != null ? methodMapping.getDeobf() : name, this.mappings.mapDescriptor(descriptor), this.mappings.mapSignature(signature, false, this.api), this.mappings.mapArray(exceptions));
        if (visitor != null) {
            return new BookMethodVisitor(name, this.obfName, descriptor, this.signature, this.mappings, this.api, visitor);
        }
        return null;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        MappedField fieldMapping = this.mappings.getFieldMapping(this.obfName, name, descriptor, access);
        return super.visitField(access, fieldMapping != null ? fieldMapping.getDeobf() : name, this.mappings.mapDescriptor(descriptor), this.mappings.mapSignature(signature, true, this.api), value);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        String mapped = this.mappings.getClassMapping(name);
        super.visitInnerClass(mapped, this.mappings.getClassMapping(outerName), innerName != null ? mapped.substring(mapped.lastIndexOf(mapped.contains("$") ? '$' : '/') + 1) : null, access);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        MappedMethod methodMapping = this.mappings.getMethodMapping(owner, name, descriptor, signature);
        super.visitOuterClass(this.mappings.getClassMapping(owner), methodMapping != null ? methodMapping.getDeobf() : name, this.mappings.mapDescriptor(descriptor));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor visitor = super.visitAnnotation(descriptor, visible);
        if (visitor != null) {
            return new AnnotationVisitor(this.api, visitor) {
                @Override
                public void visit(String name, Object value) {
                    super.visit(name, BookClassVisitor.this.mappings.mapValue(value, 0, BookClassVisitor.this.signature));
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
