package net.ndrei.jsonrecipemanager

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger
import java.io.File

/**
 * Created by CF on 2017-07-10.
 */
@Mod(modid = JsonRecipeManagerMod.MODID,
        dependencies = "after:*", useMetadata = true,
        modLanguage = "kotlin", modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter")
class JsonRecipeManagerMod {

    lateinit var configFolder: File
        private set

    @Mod.EventHandler
    fun preInit(ev: FMLPreInitializationEvent) {
        JsonRecipeManagerMod.logger = ev.modLog
        this.configFolder = ev.modConfigurationDirectory

        MinecraftForge.EVENT_BUS.register(JsonRecipeManagerEvents)
    }

    companion object {
        const val MODID = "jsonrecipemanager"

        @Mod.Instance
        lateinit var instance: JsonRecipeManagerMod

        lateinit var logger: Logger
    }
}