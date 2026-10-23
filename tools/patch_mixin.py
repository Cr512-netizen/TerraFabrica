#!/usr/bin/env python3
"""Одноразовый патч: правильная сигнатура CraftingMenuMixin под 1.21.1.

Оставлен в репозитории как запись о том, какой была цель инъекции:
slotChangedCraftingGrid(AbstractContainerMenu, Level, Player, CraftingContainer,
ResultContainer, RecipeHolder<CraftingRecipe>) — шестой параметр появился в 1.21.1,
без него миксин падает с InvalidInjectionException при загрузке.
"""
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PATH = os.path.join(ROOT, "src", "main", "java", "com", "terrafabrica", "mixin", "CraftingMenuMixin.java")

OLD_SIG = '''    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    private static void terrafabrica$gateResearchRecipes(AbstractContainerMenu menu, Level level, Player player,
                                                        CraftingContainer container, ResultContainer resultContainer,
                                                        CallbackInfo ci) {'''

NEW_SIG = '''    // Сигнатура обязана совпадать с целью: в 1.21.1 это
    // (AbstractContainerMenu, Level, Player, CraftingContainer, ResultContainer,
    //  RecipeHolder<CraftingRecipe>). Последний параметр — уже найденный ванилью
    // рецепт, поэтому используем его вместо повторного поиска.
    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    private static void terrafabrica$gateResearchRecipes(AbstractContainerMenu menu, Level level, Player player,
                                                        CraftingContainer container, ResultContainer resultContainer,
                                                        RecipeHolder<CraftingRecipe> recipeHolder,
                                                        CallbackInfo ci) {'''

OLD_BODY = '''        RecipeManager manager = level.getRecipeManager();
        Optional<RecipeHolder<CraftingRecipe>> found =
                manager.getRecipeFor(RecipeType.CRAFTING, container.asCraftInput(), level);
        if (found.isEmpty()) {
            return;
        }
        CraftingRecipe recipe = found.get().value();'''

NEW_BODY = '''        RecipeHolder<CraftingRecipe> holder = recipeHolder;
        if (holder == null) {
            // Ваниль передаёт null, если рецепт не найден; на всякий случай
            // повторяем поиск, чтобы гейтинг не терялся.
            Optional<RecipeHolder<CraftingRecipe>> found = level.getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, container.asCraftInput(), level);
            if (found.isEmpty()) {
                return;
            }
            holder = found.get();
        }
        CraftingRecipe recipe = holder.value();'''


def main():
    with open(PATH, encoding="utf-8") as f:
        text = f.read()
    if NEW_SIG in text:
        print("уже пропатчено")
        return
    assert OLD_SIG in text, "не найдена старая сигнатура инъекции"
    assert OLD_BODY in text, "не найдено тело метода"
    text = text.replace(OLD_SIG, NEW_SIG).replace(OLD_BODY, NEW_BODY)
    text = text.replace("import net.minecraft.world.item.crafting.RecipeManager;\n", "")
    with open(PATH, "w", encoding="utf-8", newline="") as f:
        f.write(text)
    print("CraftingMenuMixin: сигнатура исправлена под 1.21.1")


if __name__ == "__main__":
    main()
