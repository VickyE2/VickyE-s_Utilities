package org.vicky.forge.annotationssystem;

import org.vicky.forge.entity.bridge.AnnotationScanner;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PostAnnotationScanEvent {
    private final List<AnnotationScanner.ScanResult> results;

    public PostAnnotationScanEvent(List<AnnotationScanner.ScanResult> results) {
        this.results = Collections.unmodifiableList(results);
    }

    public List<AnnotationScanner.ScanResult> getResults() {
        return results;
    }

    public List<AnnotationScanner.ScanResult> getResultsFor(Class<? extends Annotation> annotation) {
        return results.stream().filter(it -> Objects.equals(it.annotationClassName, annotation.getName()))
                .toList();
    }
}
