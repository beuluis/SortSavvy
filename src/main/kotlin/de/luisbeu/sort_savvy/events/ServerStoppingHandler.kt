package de.luisbeu.sort_savvy.events

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.api.WebServer
import de.luisbeu.sort_savvy.persistence.PersistentManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

class ServerStoppingHandler : ServerLifecycleEvents.ServerStopping {
    override fun onServerStopping(server: MinecraftServer?) {
        // Save all data if any
        PersistentManager.saveData()

        // Delete our globals
        SortSavvy.LifecycleGlobals.destroy()

        // Stop web server with server stop
        WebServer.stop()
    }
}