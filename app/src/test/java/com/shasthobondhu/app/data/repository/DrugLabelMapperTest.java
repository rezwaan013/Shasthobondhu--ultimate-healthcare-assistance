package com.shasthobondhu.app.data.repository;

import com.shasthobondhu.app.data.model.DrugLabel;
import com.shasthobondhu.app.data.remote.model.OpenFdaLabelResponse;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DrugLabelMapperTest {

    @Test
    public void normalizesCommonBangladeshParacetamolNames() {
        assertEquals("acetaminophen", DrugLabelMapper.normalizeSearchName("Paracetamol"));
        assertEquals("acetaminophen", DrugLabelMapper.normalizeSearchName("Napa Extra"));
        assertEquals("amoxicillin", DrugLabelMapper.normalizeSearchName(" Amoxicillin "));
    }

    @Test
    public void mapsNestedNullableLabelData() {
        OpenFdaLabelResponse response = new OpenFdaLabelResponse();
        OpenFdaLabelResponse.LabelResult result = new OpenFdaLabelResponse.LabelResult();
        result.indicationsAndUsage = Collections.singletonList("Reduces fever");
        result.warnings = Collections.singletonList("Ask a doctor");
        result.openFda = new OpenFdaLabelResponse.OpenFda();
        result.openFda.genericName = Collections.singletonList("ACETAMINOPHEN");
        response.results = Collections.singletonList(result);

        DrugLabel label = DrugLabelMapper.map(response, "Napa");
        assertEquals("ACETAMINOPHEN", label.displayName);
        assertEquals("Reduces fever", label.indications);
        assertEquals("Ask a doctor", label.warnings);
    }

    @Test
    public void emptyResultsMapToNull() {
        OpenFdaLabelResponse response = new OpenFdaLabelResponse();
        response.results = Collections.emptyList();
        assertNull(DrugLabelMapper.map(response, "Unknown"));
    }
}
