package net.ndrei.jsonrecipemanager

import com.google.gson.JsonObject
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.common.crafting.JsonContext
import net.minecraftforge.registries.IForgeRegistry

/**
 * Created by CF on 2017-07-10.
 */
interface IRecipeImporter {
    fun import(json: JsonObject, context: JsonContext, registry: IForgeRegistry<IRecipe>)
}
