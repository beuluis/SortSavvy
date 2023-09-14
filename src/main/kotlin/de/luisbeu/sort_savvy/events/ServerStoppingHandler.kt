package de.luisbeu.sort_savvy.events

import de.luisbeu.sort_savvy.api.WebServer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

class ServerStoppingHandler : ServerLifecycleEvents.ServerStopping {
    override fun onServerStopping(server: MinecraftServer?) {
        // Stop web server with server stop
        // TODO: fix this
        WebServer.stop()
    }
}