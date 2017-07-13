package com.chongos.prefman

import com.google.auto.common.MoreElements.getPackage
import com.squareup.javapoet.*
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import kotlin.collections.HashMap


/**
 * @author ChongOS
 * *
 * @since 06-Jul-2017
 */
internal class PrefManWriter(private val prefName: String,
                             private val mode: Int,
                             private val prefClass: TypeName,
                             private val manClass: ClassName,
                             private val fields: List<Field>) {

    fun brewJava(): JavaFile {
        return JavaFile.builder(manClass.packageName(), createType())
                .addFileComment("Generated code from PrefMan. Do not modify!")
                .build()
    }

    private fun createType(): TypeSpec {
        val result = TypeSpec.classBuilder(manClass.simpleName())
                .addModifiers(PUBLIC, FINAL)
                .superclass(PREFERENCE)
                .addField(ParameterizedTypeName.get(
                        ClassName.get(Map::class.java),
                        ClassName.get(String::class.java),
                        manClass),
                        VAR_INSTANCE_MAP, Modifier.PRIVATE, Modifier.STATIC)
                .addField(prefClass, VAR_DEFAULT, Modifier.PRIVATE)
                .addMethods(createGetInstanceMethod())
                .addMethod(createConstructor())

        for (field in fields) {
            result.addMethod(createPutMethod(field))
                    .addMethod(createGetMethod(field))
                    .addMethod(createRemoveMethod(field))
        }

        return result.build()
    }

    private fun createGetInstanceMethod(): List<MethodSpec> {
        val methodSpecs = ArrayList<MethodSpec>()
        methodSpecs.add(MethodSpec.methodBuilder("getManager")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(manClass)
                .addParameter(CONTEXT, "context")
                .addStatement("return getManager(context, \$S)", prefName)
                .build()
        )
        methodSpecs.add(MethodSpec.methodBuilder("getManager")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(manClass)
                .addParameter(CONTEXT, "context")
                .addParameter(String::class.java, "prefName")
                .addStatement("return getManager(context, prefName, \$L)", mode)
                .build()
        )
        methodSpecs.add(MethodSpec.methodBuilder("getManager")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(manClass)
                .addParameter(CONTEXT, "context")
                .addParameter(String::class.java, "prefName")
                .addParameter(TypeName.INT, "mode")
                .addStatement("return getManager(context, prefName, mode, new \$T(), false)", prefClass)
                .build()
        )
        methodSpecs.add(MethodSpec.methodBuilder("getManager")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(manClass)
                .addParameter(CONTEXT, "context")
                .addParameter(prefClass, "customDefault")
                .addStatement("return getManager(context, \$S, \$L, customDefault)", prefName, mode)
                .build()
        )
        methodSpecs.add(MethodSpec.methodBuilder("getManager")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .returns(manClass)
                .addParameter(CONTEXT, "context")
                .addParameter(String::class.java, "prefName")
                .addParameter(TypeName.INT, "mode")
                .addParameter(prefClass, "customDefault")
                .addStatement("return getManager(context, \$S, \$L, customDefault != null ? customDefault : new \$T(), true)", prefName, mode, prefClass)
                .build()
        )
        methodSpecs.add(MethodSpec.methodBuilder("getManager")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .returns(manClass)
                .addParameter(CONTEXT, "context")
                .addParameter(String::class.java, "prefName")
                .addParameter(TypeName.INT, "mode")
                .addParameter(prefClass, "customDefault")
                .addParameter(TypeName.BOOLEAN, "force")
                .beginControlFlow("if (\$N == null)", VAR_INSTANCE_MAP)
                .addStatement("\$N = new \$T()",
                        VAR_INSTANCE_MAP, ParameterizedTypeName.get(
                        ClassName.get(HashMap::class.java),
                        ClassName.get(String::class.java),
                        manClass))
                .endControlFlow()
                .addStatement("\$T manager = force ? null : \$N.get(\$N)", manClass, VAR_INSTANCE_MAP, "prefName")
                .beginControlFlow("if (manager == null)")
                .addStatement("manager = new \$T(context, prefName, mode, customDefault)", manClass)
                .addStatement("\$N.put(prefName, manager)", VAR_INSTANCE_MAP)
                .endControlFlow()
                .addStatement("return manager")
                .build()
        )
        return methodSpecs
    }

    private fun createConstructor(): MethodSpec {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(CONTEXT, "context")
                .addParameter(String::class.java, "prefName")
                .addParameter(TypeName.INT, "mode")
                .addParameter(prefClass, "_default")
                .addStatement("super(context.getSharedPreferences(prefName, mode))")
                .addStatement("\$N = _default", VAR_DEFAULT)
                .build()
    }

    private fun createPutMethod(field: Field): MethodSpec {
        val builder = MethodSpec.methodBuilder("put${field.name}")
                .addModifiers(Modifier.PUBLIC)
                .returns(Void.TYPE)
                .addParameter(TypeName.get(field.element.asType()), field.element.toString())
        return addPutStatement(builder, field, "editor()").build()
    }

    private fun createGetMethod(field: Field): MethodSpec {
        val retType = when (field.element.asType().toString()) {
            "int" -> ClassName.get("java.lang", "Integer")
            "long" -> ClassName.get("java.lang", "Long")
            "boolean" -> ClassName.get("java.lang", "Boolean")
            "float" -> ClassName.get("java.lang", "Float")
            else -> ClassName.get(field.element.asType())
        }
        val callMethod = MethodSpec.methodBuilder("call")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(retType)

        addGetStatement(callMethod, field, "return ").build()

        return MethodSpec.methodBuilder("get${field.name.capitalize()}")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(GETTER, retType))
                .addStatement("return new \$T(this, \$S, \$L)",
                        ParameterizedTypeName.get(GETTER, retType),
                        field.name,
                        TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(ParameterizedTypeName.get(CALLABLE, retType))
                                .addMethod(callMethod.build())
                                .build())
                .build()
    }

    private fun createRemoveMethod(field: Field): MethodSpec {
        return MethodSpec.methodBuilder("remove${field.name.capitalize()}")
                .addModifiers(Modifier.PUBLIC)
                .returns(Void.TYPE)
                .addCode(addRemoveStatement(CodeBlock.builder(), field, "editor()")
                        .addStatement(".apply()")
                        .build())
                .build()
    }

    private fun addPutStatement(builder: MethodSpec.Builder, field: Field,
                                editor: String): MethodSpec.Builder {
        if (!field.isModel) {
            val callApply = if (field.parent == null) ".apply()" else ""
            return if (field.element.asType().toString() == "java.lang.String[]")
                builder.addStatement("$editor.\$N(\$S, \$T.fromArray(\$N))$callApply",
                        getMethodName("put", field.element.asType()),
                        field.prefKey,
                        UTIL,
                        field.access)
            else builder.addStatement("$editor.\$N(\$S, \$N)$callApply",
                    getMethodName("put", field.element.asType()),
                    field.prefKey,
                    field.access)

        } else if (field.hasChild()) {
            val varEditor = "editor"
            field.parent ?: builder.addStatement("\$T \$N = \$N", EDITOR, varEditor, editor)
            builder.beginControlFlow("if (\$N != null)", field.access)
            field.parent ?: builder.addStatement("$varEditor.putLong(\$S, System.currentTimeMillis())", field.name)
            for (child in field.children) {
                addPutStatement(builder, child, varEditor)
            }
            builder.nextControlFlow("else")
            builder.addStatement(addRemoveStatement(CodeBlock.builder(), field, varEditor).build().toString())
            builder.endControlFlow()

            field.parent ?: builder.addStatement("$varEditor.apply()")
        }
        return builder
    }

    private fun addGetStatement(builder: MethodSpec.Builder, field: Field, assign: String): MethodSpec.Builder {
        val defaultAccess = "$VAR_DEFAULT.${field.access}"
        if (!field.isModel) {
            return if (field.element.asType().toString() == "java.lang.String[]")
                builder.addStatement("$assign\$T.toArray(getStringSet(\$S, \$T.fromArray(\$N)))",
                        UTIL, field.prefKey, UTIL, defaultAccess)
            else builder.addStatement("$assign\$N(\$S, \$N)",
                    getMethodName("get", field.element.asType()), field.prefKey, defaultAccess)
        } else if (field.hasChild()) {
            val typeMirror = field.element.asType()
            builder.addStatement("\$T \$N = new \$T()", typeMirror, field.name, typeMirror)
            builder.addStatement("if (\$N == null) \$N = \$N", defaultAccess, defaultAccess, field.name)
            for (child in field.children) {
                addGetStatement(builder, child, "${child.access} = ")
            }

            field.parent ?: builder.addStatement("return \$N", field.name)
        }
        return builder
    }

    private fun addRemoveStatement(builder: CodeBlock.Builder, field: Field, editor: String): CodeBlock.Builder =
            when {
                !field.isModel -> builder.add("\$N.remove(\$S)", editor, field.prefKey)
                field.hasChild() -> {
                    builder.add(editor)
                    for (child in field.children) {
                        addRemoveStatement(builder, child, "")
                    }
                    field.parent ?: builder.add(".remove(\$S)", field.name)
                    builder
                }
                else -> builder
            }

    fun getMethodName(prefix: String, type: TypeMirror): String = "$prefix${MAP_TYPE[type.toString()]}"

    internal class Builder internal constructor(private val prefName: String,
                                                private val mode: Int,
                                                private val prefClass: TypeName,
                                                private val manClass: ClassName) {

        private val fields by lazy { ArrayList<Field>() }

        fun addField(field: Field) {
            fields.add(field)
        }

        fun build(): PrefManWriter {
            return PrefManWriter(prefName, mode, prefClass, manClass, fields)
        }
    }

    companion object {

        private val CONTEXT by lazy { ClassName.get("android.content", "Context") }
        private val EDITOR by lazy { ClassName.get("android.content.SharedPreferences", "Editor") }
        private val PREFERENCE by lazy { ClassName.get("com.chongos.prefman", "Preference") }
        private val GETTER by lazy { ClassName.get("com.chongos.prefman", "Getter") }
        private val CALLABLE by lazy { ClassName.get("com.chongos.prefman", "Callable") }
        private val UTIL by lazy { ClassName.get("com.chongos.prefman", "Util") }

        private val VAR_INSTANCE_MAP = "_instance_map"
        private val VAR_DEFAULT = "_defaultValue"

        private val MAP_TYPE by lazy {
            mapOf(
                    Pair("int", "Int"),
                    Pair("java.lang.Integer", "Int"),
                    Pair("long", "Long"),
                    Pair("java.lang.Long", "Long"),
                    Pair("boolean", "Boolean"),
                    Pair("java.lang.Boolean", "Boolean"),
                    Pair("float", "Float"),
                    Pair("java.lang.Float", "Float"),
                    Pair("java.lang.String", "String"),
                    Pair("java.lang.String[]", "StringSet"),
                    Pair("java.util.Set<java.lang.String>", "StringSet")
            )
        }

        fun newBuilder(typeElement: TypeElement): Builder {
            var prefClass = TypeName.get(typeElement.asType())
            if (prefClass is ParameterizedTypeName) prefClass = prefClass.rawType

            val packageName = getPackage(typeElement).qualifiedName.toString()
            val className = typeElement.qualifiedName.toString().substring(packageName.length + 1)
            val manClass = ClassName.get(packageName, "${className}Manager")

            val prefMan = typeElement.getAnnotation(PrefMan::class.java)
            val prefName = if (prefMan.name.isNullOrBlank()) className else prefMan.name

            return Builder(prefName, prefMan.mode, prefClass, manClass)
        }
    }

}
