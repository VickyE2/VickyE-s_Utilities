package org.vicky.forge.annotationssystem;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class AnnotationRegisterEvent {
    private final List<Class<? extends Annotation>> annotations = new ArrayList<>();

    public AnnotationRegisterEvent() {
        super();
    }

    public void addAnnotation(Class<? extends Annotation> annotation) {
        this.annotations.add(annotation);
    }

    public List<Class<? extends Annotation>> getAnnotations() {
        return annotations;
    }
}
