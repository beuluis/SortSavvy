package de.luisbeu.sort_savvy.events

import de.luisbeu.sort_savvy.api.WebServer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

class ServerStartedHandler : ServerLifecycleEvents.ServerStarted {
    override fun onServerStarted(server: MinecraftServer) {
        // Start Web Server on world load
        WebServer.start(server)
    }
}