package de.luisbeu.sort_savvy.events

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.api.WebServer
import de.luisbeu.sort_savvy.config.ConfigManager
import de.luisbeu.sort_savvy.persistence.PersistentManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

class ServerStartedHandler : ServerLifecycleEvents.ServerStarted {
    override fun onServerStarted(server: MinecraftServer) {
        // Set out globals
        SortSavvy.LifecycleGlobals.setMinecraftServer(server)

        // Also load all down stream dependencies new
        ConfigManager.load()
        PersistentManager.load()

        // Start Web Server on world load
        WebServer.start()
    }
}