package co.matrixdevelopment.lava.asm.transformers;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class WriterOfDebug extends ClassVisitor {

    ClassVisitor cv;

    public WriterOfDebug(int api, ClassVisitor cv) {
        super(Opcodes.ASM6, cv);
        this.cv = cv;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.contains("injectIntoClassLoader")) {
            return super.visitMethod(access, name, "(net/minecraftforge/fml/relauncher/DebuggableLaunchLoader;)V",
                    signature, exceptions);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

}