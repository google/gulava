package musubi.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;

/**
 * Contains information derived from the {@code MakeLogicValue} annotation.
 */
public final class MakeLogicValueMetadata {
  public static final String ANNOTATION_NAME = "musubi.annotation.MakeLogicValue";

  private final String name;
  private final List<String> fields;

  private MakeLogicValueMetadata(String name, List<String> fields) {
    this.name = name;
    this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
  }

  public String getName() {
    return name;
  }

  public List<String> getFields() {
    return fields;
  }

  /**
   * Returns the metadata stored in a single annotation of type {@code MakeLogicValue}.
   */
  public static MakeLogicValueMetadata forAnnotation(AnnotationMirror annotation) {
    String name = null;
    List<String> fields = null;

    for (ExecutableElement key : annotation.getElementValues().keySet()) {
      AnnotationValue value = annotation.getElementValues().get(key);
      Name keyName = key.getSimpleName();
      if (keyName.contentEquals("name")) {
        name = (String) value.getValue();
      } else if (keyName.contentEquals("fields")) {
        fields = new ArrayList<>();
        for (Object fieldNameValue : (List<?>) value.getValue()) {
          fields.add((String) ((AnnotationValue) fieldNameValue).getValue());
        }
      } else {
        throw new UnsupportedOperationException("Unknown attribute property name: " + keyName);
      }
    }

    return new MakeLogicValueMetadata(name, fields);
  }

  /**
   * Returns all the metadata instances for the annotations on a package.
   */
  public static List<MakeLogicValueMetadata> forPackage(PackageElement pkg) {
    List<MakeLogicValueMetadata> metadatas = new ArrayList<>();

    for (AnnotationMirror annotation : pkg.getAnnotationMirrors()) {
      if (((TypeElement) annotation.getAnnotationType().asElement())
          .getQualifiedName()
          .contentEquals(ANNOTATION_NAME)) {
        metadatas.add(forAnnotation(annotation));
      }
    }

    return metadatas;
  }
}
