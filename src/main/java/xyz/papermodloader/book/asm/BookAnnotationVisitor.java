package xyz.papermodloader.book.asm;

import org.objectweb.asm.AnnotationVisitor;
import xyz.papermodloader.book.mapping.Mappings;

public class BookAnnotationVisitor extends AnnotationVisitor {
    private Mappings mappings;

    public BookAnnotationVisitor(AnnotationVisitor visitor, Mappings mappings, int api) {
        super(api, visitor);
        this.mappings = mappings;
    }

    @Override
    public void visit(String name, Object value) {
        super.visit(name, this.mappings.mapValue(value, 0));
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        super.visitEnum(name, this.mappings.mapDescriptor(descriptor), value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        AnnotationVisitor visitor = super.visitAnnotation(name, this.mappings.mapDescriptor(descriptor));
        return visitor != null ? (visitor == this.av ? this : new BookAnnotationVisitor(visitor, this.mappings, this.api)) : null;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        AnnotationVisitor visitor = super.visitArray(name);
        return visitor != null ? (visitor == this.av ? this : new BookAnnotationVisitor(visitor, this.mappings, this.api)) : null;
    }
}
