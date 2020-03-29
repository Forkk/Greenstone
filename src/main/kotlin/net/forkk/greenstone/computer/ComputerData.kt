package net.forkk.greenstone.computer

import kotlinx.serialization.Serializable
import net.forkk.greenstone.grpl.Context

@Serializable
data class ComputerData(var context: Context = Context())
