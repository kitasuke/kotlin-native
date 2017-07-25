package org.jetbrains.kotlin.backend.common.ir.cfg

import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.descriptors.isIntrinsic
import org.jetbrains.kotlin.backend.konan.llvm.ContextUtils
import org.jetbrains.kotlin.backend.konan.llvm.getFields
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

data class CfgDeclarations(
        private val functions: MutableMap<FunctionDescriptor, Function>,
        private val classes: MutableMap<ClassDescriptor, Klass>
) {
    fun getFunc(descriptor: FunctionDescriptor) : Function {
        if (functions[descriptor] == null) {
            functions[descriptor] = Function(descriptor.fqNameSafe.asString())
        }
        return functions[descriptor]!!
    }

    fun getClass(descriptor: ClassDescriptor) : Klass {
        if (classes[descriptor] == null) {
            classes[descriptor] = Klass(descriptor.fqNameSafe.asString())
        }
        return classes[descriptor]!!
    }
}

internal fun createCfgDeclarations(context: Context): CfgDeclarations
        = with(CfgDeclarationsGenerator(context)) {
    context.ir.irModule.acceptVoid(this)
    CfgDeclarations(functions, classes)
}

private class CfgDeclarationsGenerator(override val context: Context) :
        IrElementVisitorVoid, ContextUtils {
    val classes = mutableMapOf<ClassDescriptor, Klass>()
    val functions = mutableMapOf<FunctionDescriptor, Function>()

    override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

    override fun visitClass(declaration: IrClass) {
        if (!declaration.descriptor.isIntrinsic) {
            classes[declaration.descriptor] = createClassDeclaration(declaration)
        }
        super.visitClass(declaration)
    }

    override fun visitFunction(declaration: IrFunction) {
        functions[declaration.descriptor] = createFunctionDeclaration(declaration)
        super.visitFunction(declaration)
    }

    override fun visitField(declaration: IrField) {
        super.visitField(declaration)
    }

    private fun createClassDeclaration(declaration: IrClass): Klass {
        val descriptor = declaration.descriptor

        val klass = Klass(descriptor.fqNameSafe.toString())

        getFields(descriptor).forEach {
           //klass.fields.add()
        }


        return klass
    }

    private fun createFunctionDeclaration(declaration: IrFunction): Function {
        return Function(declaration.descriptor.fqNameSafe.toString())
    }

}