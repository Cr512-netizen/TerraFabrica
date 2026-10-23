package com.terrafabrica.core.research;

/**
 * Маркерный интерфейс рецептов, заблокированных исследованием.
 *
 * <p>Реализуется:
 * <ul>
 *   <li>рецептами крафта {@code ResearchShapedRecipe}/{@code ResearchShapelessRecipe}
 *       (проверка — в mixin'е {@code CraftingMenuMixin});</li>
 *   <li>машинными рецептами {@code MachineRecipe} (проверка — в BE машины).</li>
 * </ul>
 */
public interface IResearchGatedRecipe {

    /**
     * ID требуемого узла исследования.
     *
     * @return id узла или {@code null}, если рецепт не требует исследований
     */
    String requiredNode();
}
