package com.shasthobondhu.app.data.repository;

import com.shasthobondhu.app.data.model.DrugLabel;
import com.shasthobondhu.app.data.remote.model.OpenFdaLabelResponse;

import java.util.List;
import java.util.Locale;

/** Null-safe mapping and common Bangladesh brand/generic alias normalization. */
public final class DrugLabelMapper {

    private DrugLabelMapper() {
    }

    public static String normalizeSearchName(String medicineName) {
        String normalized = medicineName == null ? ""
                : medicineName.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("paracetamol") || normalized.contains("napa")) {
            return "acetaminophen";
        }
        return normalized;
    }

    public static DrugLabel map(OpenFdaLabelResponse response, String fallbackName) {
        if (response == null || response.results == null || response.results.isEmpty()
                || response.results.get(0) == null) {
            return null;
        }
        OpenFdaLabelResponse.LabelResult result = response.results.get(0);
        String name = first(result.openFda == null ? null : result.openFda.genericName);
        if (name.isEmpty()) {
            name = first(result.openFda == null ? null : result.openFda.brandName);
        }
        if (name.isEmpty()) {
            name = fallbackName == null ? "" : fallbackName.trim();
        }
        String indications = first(result.indicationsAndUsage);
        String warnings = first(result.warnings);
        if (indications.isEmpty() && warnings.isEmpty()) {
            return null;
        }
        return new DrugLabel(name, indications, warnings);
    }

    private static String first(List<String> values) {
        if (values == null || values.isEmpty() || values.get(0) == null) {
            return "";
        }
        return values.get(0).trim();
    }
}
