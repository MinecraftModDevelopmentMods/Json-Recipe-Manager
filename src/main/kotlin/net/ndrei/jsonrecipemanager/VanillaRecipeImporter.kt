package net.ndrei.jsonrecipemanager

import com.google.gson.JsonObject
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.JsonUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.JsonContext
import net.minecraftforge.registries.IForgeRegistry

/**
 * Created by CF on 2017-07-10.
 */
object VanillaRecipeImporter: IRecipeImporter {
    private var recipeIndex = 0

    override fun import(json: JsonObject, context: JsonContext, registry: IForgeRegistry<IRecipe>) {
        if (!json.has("conditions") || CraftingHelper.processConditions(JsonUtils.getJsonArray(json, "conditions"), context)) {
            val recipe = CraftingHelper.getRecipe(json, context)

            if (recipe.registryName == null) {
                recipe.registryName = ResourceLocation(JsonRecipeManagerMod.MODID, "recipe_${this.recipeIndex++}")
            }

            registry.register(recipe)
        }
    }
}
