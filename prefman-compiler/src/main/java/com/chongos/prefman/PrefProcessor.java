package com.chongos.prefman;

import com.chongos.prefman.PrefManWriter.Builder;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

/**
 * @author ChongOS
 * @since 06-Jul-2017
 */
@AutoService(Processor.class)
public final class PrefProcessor extends AbstractProcessor {

  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;

  private static final List<String> SUPPORTED_TYPES = Arrays.asList("int", "long", "boolean",
      "float", "java.lang.String", "java.lang.String[]", "java.util.Set<java.lang.String>");

  private static final ImmutableMap<String, String> PRIMITIVE_OBJ_TYPES = new ImmutableMap.Builder<String, String>()
      .put("java.lang.Integer", "int")
      .put("java.lang.Long", "long")
      .put("java.lang.Boolean", "boolean")
      .put("java.lang.Float", "float")
      .build();

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<>();
    types.add(PrefMan.class.getCanonicalName());
    types.add(PrefModel.class.getCanonicalName());
    types.add(Ignore.class.getCanonicalName());
    return types;
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
    Map<TypeElement, PrefManWriter> bindingMap = findAndParseTargets(env);
    for (Map.Entry<TypeElement, PrefManWriter> entry : bindingMap.entrySet()) {
      TypeElement typeElement = entry.getKey();
      PrefManWriter binding = entry.getValue();

      JavaFile javaFile = binding.brewJava();
      try {
        javaFile.writeTo(filer);
      } catch (IOException e) {
        error(typeElement, "Unable to write PreferenceManager for type %s: %s", typeElement,
            e.getMessage());
      }
    }
    return false;
  }

  private Map<TypeElement, PrefManWriter> findAndParseTargets(RoundEnvironment env) {
    Map<TypeElement, PrefManWriter.Builder> builderMap = new LinkedHashMap<>();
    Map<String, TypeElement> models = new LinkedHashMap<>();

    for (Element element : env.getElementsAnnotatedWith(PrefModel.class)) {
      if (validateElement(element, PrefModel.class)) {
        models.put(element.asType().toString(), (TypeElement) element);
      }
    }

    for (Element element : env.getElementsAnnotatedWith(PrefMan.class)) {
      if (!validateElement(element, PrefMan.class)) {
        continue;
      }

      TypeElement typeElement = (TypeElement) element;
      PrefManWriter.Builder builder = PrefManWriter.Companion.newBuilder(typeElement);
      for (Element enclosed : element.getEnclosedElements()) {
        if (enclosed instanceof VariableElement) {
          try {
            Field field = parseField((VariableElement) enclosed, models);
            if (field != null) {
              builder.addField(field);
            }
          } catch (IllegalElementException e) {
            error(e.getElement(), e.getMessage());
          }
        }
      }

      builderMap.put(typeElement, builder);
    }

    Deque<Entry<TypeElement, Builder>> entries = new ArrayDeque<>(builderMap.entrySet());
    Map<TypeElement, PrefManWriter> prefMap = new LinkedHashMap<>();
    while (!entries.isEmpty()) {
      Map.Entry<TypeElement, Builder> entry = entries.removeFirst();
      TypeElement type = entry.getKey();
      Builder builder = entry.getValue();
      prefMap.put(type, builder.build());
    }

    return prefMap;
  }

  private Field parseField(VariableElement element, Map<String, TypeElement> modelsType) {
    if (element.getModifiers().contains(Modifier.PRIVATE)
        || element.getModifiers().contains(Modifier.PROTECTED)) {
      throw new IllegalElementException(
          String.format("Field `%s` can not be private or protected.", element.toString()),
          element);
    }

    if (element.getAnnotation(Ignore.class) != null) {
      return null;
    }

    String type = element.asType().toString();
    if (SUPPORTED_TYPES.contains(type)) {
      return new Field(element, false);
    } else if (PRIMITIVE_OBJ_TYPES.containsKey(type)) {
      throw new IllegalElementException(
          String.format("use `%s` instead of `%s`", PRIMITIVE_OBJ_TYPES.get(type), type),
          element);
    } else if (modelsType.containsKey(type)) {
      Field field = new Field(element, true);

      for (Element enclosed : modelsType.get(type).getEnclosedElements()) {
        if (enclosed instanceof VariableElement) {
          Field child = parseField((VariableElement) enclosed, modelsType);

          if (child != null) {
            child.setParent(field);
            field.addChild(child);
          }

        }
      }
      return field;
    }

    throw new IllegalElementException(
        String.format("%s is not Primitive type, can be annotated with @%s",
            element.asType().toString(), PrefModel.class.getSimpleName()), element);
  }

  private boolean validateElement(Element element, Class annotation) {
    if (element.getKind() == ElementKind.CLASS) {
      TypeElement typeElement = (TypeElement) element;

      // Check if an empty public constructor is given
      for (Element enclosed : element.getEnclosedElements()) {
        if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
          ExecutableElement constructorElement = (ExecutableElement) enclosed;
          if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
              .contains(Modifier.PUBLIC)) {
            // Found an empty constructor
            return true;
          }
        }
      }

      // No empty constructor found
      error(element, "The class %s must provide an public empty default constructor",
          typeElement.getQualifiedName().toString());
      return false;
    }

    // Not class
    error(element, "only classes can be annotated with @%s", annotation.getSimpleName());
    return false;
  }

  private void error(Element element, String message, Object... args) {
    printMessage(Kind.ERROR, element, message, args);
  }

  private void note(Element element, String message, Object... args) {
    printMessage(Kind.NOTE, element, message, args);
  }

  private void warn(Element element, String message, Object... args) {
    printMessage(Kind.WARNING, element, message, args);
  }

  private void printMessage(Kind kind, Element element, String message, Object[] args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }

    processingEnv.getMessager().printMessage(kind, message, element);
  }
}
