package de.luisbeu.sort_savvy.events

import de.luisbeu.sort_savvy.web.initWebServer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

class ServerStartingHandler : ServerLifecycleEvents.ServerStarting {
    override fun onServerStarting(server: MinecraftServer) {
        // Initialize Web Server
        initWebServer(server)
    }
}