package uk.gov.companieshouse.efs.web.categorytemplates.controller;

import java.util.EnumSet;
import java.util.Optional;

public enum CategoryTypeConstants {
    ROOT(""),
    RESOLUTIONS("RESOLUTIONS"),
    CHANGE_OF_CONSTITUTION("CC"),
    ARTICLES("MA"),
    INSOLVENCY("INS"),
    OTHER("*");

    CategoryTypeConstants(final String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }

    public static Optional<CategoryTypeConstants> nameOf(final String value) {
        return EnumSet.allOf(CategoryTypeConstants.class).stream().filter(
                v -> v.getValue().equals(value)).findAny();
    }
}
