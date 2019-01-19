package com.creeperface.nukkitx.nospace

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.StringEntityData
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerPreLoginEvent
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.TextFormat
import cn.nukkit.utils.Utils
import org.joor.Reflect
import java.io.File

/**
 * @author CreeperFace
 */
class NoSpace : PluginBase(), Listener {

    override fun onEnable() {
        this.server.pluginManager.registerEvents(this, this)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPreLogin(e: PlayerPreLoginEvent) {
        val name = e.player.name

        if(!name.contains(' ')) {
            return
        }

        val newName = name.replace(' ', '_')

        if(this.server.onlinePlayers.filterValues { it.name.equals(newName, true) }.isNotEmpty()) {
            e.kickMessage = "${TextFormat.RED}The same nick is already playing"
            e.setCancelled()
            return
        }

        try {
            val oldDataFile = File(server.dataPath + "players/${name.toLowerCase()}.dat")
            if (oldDataFile.exists()) {
                val newDataFile = File(server.dataPath + "players/${newName.toLowerCase()}.dat")
                if (newDataFile.exists()) {
                    newDataFile.renameTo(File(server.dataPath + "players/${newName.toLowerCase()}.dat_old"))
                    newDataFile.delete()
                }

                Utils.writeFile(newDataFile, oldDataFile.inputStream())
                oldDataFile.delete()
            }

            val p = e.player
            Reflect.on(p)
                .set("username", newName)
                .set("displayName", newName)
                .set("iusername", newName.toLowerCase())

            p.setDataProperty(StringEntityData(Entity.DATA_NAMETAG, newName), false)
        } catch (ex: Exception) {
            MainLogger.getLogger().logException(ex)
            e.setCancelled()
            e.kickMessage = "${TextFormat.RED}An error occurred during login"
        }
    }
}