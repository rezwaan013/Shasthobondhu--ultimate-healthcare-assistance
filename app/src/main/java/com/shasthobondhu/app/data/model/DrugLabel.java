package com.shasthobondhu.app.data.model;

/** Safe app-facing subset of an openFDA drug label. */
public final class DrugLabel {
    public final String displayName;
    public final String indications;
    public final String warnings;

    public DrugLabel(String displayName, String indications, String warnings) {
        this.displayName = displayName;
        this.indications = indications;
        this.warnings = warnings;
    }
}
