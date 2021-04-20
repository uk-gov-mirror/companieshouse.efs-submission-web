package uk.gov.companieshouse.efs.web.categorytemplates.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;

public class CategoryTemplateTestFixture {/*
 *   //////////////
 *  // FAMILY_1 //
 * //////////////
 *                     ROOT_CATEGORY
 *                      /          \
 *                     /            \
 *                    /              \
 *                   CAT_TOP_LEVEL   INSOLVENCY
 *                   /        \        \ _ _ _ _ _ _
 *                  /          \                    \
 *           CAT1_SUB_LEVEL1   CAT2_SUB_LEVEL1  INS_SUB_LEVEL1
 *               /        \                          \
 *              /          \                          \
 *     CAT1_SUB_LEVEL2   CAT2_SUB_LEVEL2          INS_SUB_LEVEL2
 *
 */
    public static final CategoryTemplateApi FAMILY_1_ROOT_LEVEL =
        new CategoryTemplateApi("", "FAMILY_1", "Dummy ROOT level category", "", null);
    public static final CategoryTemplateApi CAT_TOP_LEVEL =
        new CategoryTemplateApi("CAT_TOP_LEVEL", "FAMILY_1", "Dummy top level category", "", null);
    public static final CategoryTemplateApi INSOLVENCY =
        new CategoryTemplateApi("INS", "FAMILY_1", "INSOLVENCY", "", null);
    public static final CategoryTemplateApi INS_SUB_LEVEL1 =
        new CategoryTemplateApi("INS_SUB_LEVEL1", null,
            "Dummy insolvency category 1, subcategory level 1", "INS", null);
    public static final CategoryTemplateApi INS_SUB_LEVEL2 =
        new CategoryTemplateApi("INS_SUB_LEVEL2", null,
            "Dummy insolvency category 1, subcategory level 2", "INS_SUB_LEVEL1", null);
    public static final CategoryTemplateApi CAT1_SUB_LEVEL1 =
        new CategoryTemplateApi("CAT1_SUB_LEVEL1", null, "Dummy category 1, subcategory level 1",
            "CAT_TOP_LEVEL", null);
    public static final CategoryTemplateApi CAT2_SUB_LEVEL1 =
        new CategoryTemplateApi("CAT2_SUB_LEVEL1", null, "Dummy category 2, subcategory level 1",
            "CAT_TOP_LEVEL", null);
    public static final CategoryTemplateApi CAT1_SUB_LEVEL2 =
        new CategoryTemplateApi("CAT1_SUB_LEVEL2", null, "Dummy category1, subcategory level 2",
            "CAT1_SUB_LEVEL1", null);
    public static final CategoryTemplateApi CAT2_SUB_LEVEL2 =
        new CategoryTemplateApi("CAT2_SUB_LEVEL2", null, "Dummy category 2, subcategory level 2",
            "CAT1_SUB_LEVEL1", null);
    public static final List<CategoryTemplateApi> FAMILY_1_CATEGORIES =
        Arrays.asList(CAT_TOP_LEVEL, INSOLVENCY, CAT1_SUB_LEVEL1, CAT2_SUB_LEVEL1, CAT1_SUB_LEVEL2,
            CAT2_SUB_LEVEL2);/*
     *   //////////////
     *  // FAMILY_2 //
     * //////////////
     *                     ROOT_CATEGORY
     *                      /          \
     *                     /            \
     *                    /              \
     *             CAT_TOP_LEVEL_1   CAT_TOP_LEVEL_2
     */
    public static final CategoryTemplateApi CAT_TOP_LEVEL_1 =
        new CategoryTemplateApi("CAT_TOP_LEVEL_1", "FAMILY_2", "Dummy top level category", "",
            null);
    public static final CategoryTemplateApi CAT_TOP_LEVEL_2 =
        new CategoryTemplateApi("CAT_TOP_LEVEL_2", "FAMILY_2", "Dummy top level category", "",
            null);
    public static final List<CategoryTemplateApi> FAMILY_2_CATEGORIES =
        Arrays.asList(CAT_TOP_LEVEL_1, CAT_TOP_LEVEL_2);
    public static final List<CategoryTemplateApi> ALL_CATEGORIES =
        Stream.of(FAMILY_1_CATEGORIES, FAMILY_2_CATEGORIES)
            .flatMap(List::stream)
            .collect(Collectors.toList());
}