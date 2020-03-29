package net.forkk.greenstone.computer

import drawer.getFrom
import drawer.put
import java.util.function.Supplier
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag

class ComputerBlockEntity : BlockEntity(TYPE) {
    companion object {
        val TYPE: BlockEntityType<ComputerBlockEntity> = // Is this correct? idk, I copied it from Stockpile
            BlockEntityType.Builder.create(
                Supplier { ComputerBlockEntity() },
                ComputerBlock()
            ).build(null)
    }

    private var context = ComputerData()

    override fun toTag(tag: CompoundTag): CompoundTag {
        ComputerData.serializer().put(context, inTag = tag)
        return super.toTag(tag)
    }

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        context = ComputerData.serializer().getFrom(tag)
    }
}
