package net.ndrei.jsonrecipemanager

import com.google.gson.*
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.JsonUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.JsonContext
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.FMLLog
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryModifiable
import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * Created by CF on 2017-07-10.
 */
object JsonRecipeManagerEvents {
    private val GSON = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    @SubscribeEvent
    fun registerRecipes(ev: RegistryEvent.Register<IRecipe>) {
        this.parseFiles(ev.registry)
    }

    fun parseFiles(registry: IForgeRegistry<IRecipe>, moment: String = "") {
        val config = File(JsonRecipeManagerMod.instance.configFolder, "JSONRecipes")
        if (config.exists() && config.isDirectory) {
            val ctx = JsonContext(JsonRecipeManagerMod.MODID)

            val consts = File(config, "_constants.json")
            if (consts.exists() && consts.isFile) {
                var reader: BufferedReader? = null
                try {
                    reader = consts.bufferedReader()
                    val json = JsonUtils.fromJson(GSON, reader, JsonArray::class.java)
                    if (json != null) {
                        val method = ctx.javaClass.getDeclaredMethod("loadConstants", Array<JsonObject>::class.java)
                        method.isAccessible = true
                        val items = json.map { it.asJsonObject }.toTypedArray()
                        method.invoke(ctx, items)
                    }
                } catch (e: JsonParseException) {
                    JsonRecipeManagerMod.logger.error("Parsing error loading constants file: '_constants.json'.", e)
                } catch (e: IOException) {
                    JsonRecipeManagerMod.logger.error("Couldn't read constants from file '_constants.json'.", e)
                } catch (e: NoSuchMethodException) {
                    JsonRecipeManagerMod.logger.error("Unexpected JsonContext members. Please update the mod.", e)
                } finally {
                    IOUtils.closeQuietly(reader)
                }
            }

            config
                    .listFiles({ f: File -> !f.name.startsWith("_") && f.extension.toLowerCase() == "json" })
                    .forEach {
                        var reader: BufferedReader? = null
                        try {
                            reader = it.bufferedReader()
                            val json = JsonUtils.fromJson(GSON, reader, JsonElement::class.java) ?: return

                            if (json.isJsonArray) {
                                json.asJsonArray.forEach {
                                    VanillaRecipeImporter.import(it.asJsonObject, ctx, registry)
                                }
                            }
                            else if (json.isJsonObject) {
                                val node = json.asJsonObject
                                val jsonMoment = if (node.has("when")) node.get("when").asString else ""
                                if (jsonMoment == moment) {
                                    if (node.has("remove")) {
                                        if ((registry is IForgeRegistryModifiable<IRecipe>) && !registry.isLocked) {
                                            val toRemove = mutableListOf<ResourceLocation>()
                                            node.getAsJsonArray("remove").forEach {
                                                val info = it.asJsonObject
                                                if (info.has("name")) {
                                                    val name = info.get("name").asString
                                                    registry.forEach {
                                                        if ((it.registryName != null) && (it.registryName.toString() == name)) {
                                                            toRemove.add(it.registryName!!)
                                                        }
                                                    }
                                                } else if (info.has("mod")) {
                                                    val mod = info.get("mod").asString
                                                    registry.forEach {
                                                        if ((it.registryName != null) && (it.registryName?.resourcePath == mod)) {
                                                            toRemove.add(it.registryName!!)
                                                        }
                                                    }
                                                } else if (info.has("item")) {
                                                    val item = info.get("item").asString
                                                    val data = if (info.has("data"))
                                                        info.get("data").asInt
                                                    else
                                                        -1
                                                    registry.forEach {
                                                        if ((it.registryName != null) && (it.recipeOutput.item.registryName.toString() == item) &&
                                                                ((data == -1) || (it.recipeOutput.metadata == data))) {
                                                            toRemove.add(it.registryName!!)
                                                        }
                                                    }
                                                }
                                            }
                                            toRemove.forEach { registry.remove(it) }
                                        }

                                        if (node.has("add")) {
                                            node.getAsJsonArray("add").forEach {
                                                VanillaRecipeImporter.import(it.asJsonObject, ctx, registry)
                                            }
                                        }
                                    } else {
                                        VanillaRecipeImporter.import(node, ctx, registry)
                                    }
                                }
                            }
                            else
                                throw JsonParseException("Unknown root node type.")
                        } catch (e: JsonParseException) {
                            JsonRecipeManagerMod.logger.error("Parsing error loading recipe file: '${it.name}'.", e)
                        } catch (e: IOException) {
                            JsonRecipeManagerMod.logger.error("Couldn't read recipe from file '${it.path}'.", e)
                        } finally {
                            IOUtils.closeQuietly(reader)
                        }
                    }
        }
    }
}