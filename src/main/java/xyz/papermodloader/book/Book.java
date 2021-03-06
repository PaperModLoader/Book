package xyz.papermodloader.book;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import xyz.papermodloader.book.asm.BookClassVisitor;
import xyz.papermodloader.book.mapping.Mappings;
import xyz.papermodloader.book.util.Arguments;
import xyz.papermodloader.book.util.ConsoleProgressLogger;
import xyz.papermodloader.book.util.ProgressLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public enum Book {
    INSTANCE;

    public static final String VERSION = "0.1.0";

    public static void main(String[] args) throws Exception {
        Arguments arguments = new Arguments(args);

        if (!arguments.has("input") || !arguments.has("output") || !arguments.has("mappings")) {
            throw new RuntimeException("Missing arguments!");
        }

        File mappingsFile = new File(arguments.get("mappings"));
        File inputFile = new File(arguments.get("input"));
        File outputFile = new File(arguments.get("output"));

        if (!inputFile.exists()) {
            throw new RuntimeException("Input doesn't exist!");
        }

        if (!mappingsFile.exists()) {
            throw new RuntimeException("Mappings don't exist!");
        }

        Mappings mappings = Mappings.parseMappings(new FileInputStream(mappingsFile));

        long startTime = System.currentTimeMillis();

        try {
            Book.INSTANCE.map(mappings, inputFile, outputFile, new ConsoleProgressLogger());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Mapped jar in " + (System.currentTimeMillis() - startTime) + " millis!");
    }

    public void map(Mappings mappings, File input, File output, ProgressLogger logger) throws IOException {
        Map<String, List<String>> inheritance = new HashMap<>();
        Map<String, ClassNode> classes = new HashMap<>();
        int total = 0;
        int progress = 0;
        ZipFile inputZip = new ZipFile(input);
        if (!output.exists()) {
            if (!output.getParentFile().exists()) {
                output.getParentFile().mkdirs();
            }
            output.createNewFile();
        }
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(output));
        Enumeration<? extends ZipEntry> entries = inputZip.entries();
        List<ClassNode> process = new LinkedList<>();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                if (entry.getName().endsWith(".class")) {
                    String name = entry.getName().substring(0, entry.getName().length() - ".class".length());
                    ClassReader classReader = new ClassReader(inputZip.getInputStream(entry));
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, 0);
                    List<String> parents = new LinkedList<>();
                    for (String parent : classNode.interfaces) {
                        parents.add(parent);
                    }
                    if (classNode.superName != null) {
                        parents.add(classNode.superName);
                    }
                    inheritance.put(name, parents);
                    classes.put(name, classNode);
                    out.closeEntry();
                    process.add(classNode);
                    total++;
                }
            }
        }
        inputZip.close();
        mappings.setInheritance(inheritance);
        mappings.setClassNodes(classes);
        for(ClassNode node : process) {
            String name = node.name;
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(new BookClassVisitor(writer, mappings, Opcodes.ASM5));
            out.putNextEntry(new ZipEntry(mappings.getClassMapping(name) + ".class"));
            out.write(writer.toByteArray());
            out.closeEntry();
            logger.onProgress(progress++, total);
        }
        out.close();
    }
}
