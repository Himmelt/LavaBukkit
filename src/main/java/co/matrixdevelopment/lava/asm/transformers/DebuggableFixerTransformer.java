package co.matrixdevelopment.lava.asm.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import static org.objectweb.asm.Opcodes.*;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;

public class DebuggableFixerTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(ITweaker.class.getCanonicalName())) {
            ClassNode classNode = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            cr.accept(classNode, 0);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            WriterOfDebug wod = new WriterOfDebug(0, classWriter);

            MethodVisitor mv = wod.visitMethod(ACC_PUBLIC, "injectIntoClassLoader",
                    "(Lnet/minecraft/launchwrapper/LaunchLoader;)V", null, null);
            mv.visitEnd();
            
            return classWriter.toByteArray();
        } else
            return basicClass;
    }
}