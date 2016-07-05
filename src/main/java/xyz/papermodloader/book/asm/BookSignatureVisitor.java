package xyz.papermodloader.book.asm;

import org.objectweb.asm.signature.SignatureVisitor;
import xyz.papermodloader.book.mapping.Mappings;

import java.util.Stack;

public class BookSignatureVisitor extends SignatureVisitor {
    private SignatureVisitor visitor;
    private Mappings mappings;

    private Stack<String> classes = new Stack<>();

    public BookSignatureVisitor(int api, SignatureVisitor visitor, Mappings mappings) {
        super(api);
        this.visitor = visitor;
        this.mappings = mappings;
    }

    @Override
    public void visitClassType(String name) {
        this.classes.push(name);
        this.visitor.visitClassType(this.mappings.getClassMapping(name));
    }

    @Override
    public void visitInnerClassType(String name) {
        String outerClass = this.classes.pop();
        String newName = outerClass + "$" + name;
        this.classes.push(newName);
        String mappedOuter = this.mappings.getClassMapping(outerClass) + "$";
        String mappedName = this.mappings.getClassMapping(newName);
        this.visitor.visitInnerClassType(mappedName.substring(mappedName.startsWith(mappedOuter) ? mappedOuter.length() : mappedName.indexOf('$') + 1));
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        this.visitor.visitFormalTypeParameter(name);
    }

    @Override
    public void visitTypeVariable(String name) {
        this.visitor.visitTypeVariable(name);
    }

    @Override
    public SignatureVisitor visitArrayType() {
        this.visitor.visitArrayType();
        return this;
    }

    @Override
    public void visitBaseType(char descriptor) {
        this.visitor.visitBaseType(descriptor);
    }

    @Override
    public SignatureVisitor visitClassBound() {
        this.visitor.visitClassBound();
        return this;
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        this.visitor.visitExceptionType();
        return this;
    }

    @Override
    public SignatureVisitor visitInterface() {
        this.visitor.visitInterface();
        return this;
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        this.visitor.visitInterfaceBound();
        return this;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        this.visitor.visitParameterType();
        return this;
    }

    @Override
    public SignatureVisitor visitReturnType() {
        this.visitor.visitReturnType();
        return this;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        this.visitor.visitSuperclass();
        return this;
    }

    @Override
    public void visitTypeArgument() {
        this.visitor.visitTypeArgument();
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        this.visitor.visitTypeArgument(wildcard);
        return this;
    }

    @Override
    public void visitEnd() {
        this.classes.pop();
        this.visitor.visitEnd();
    }
}
