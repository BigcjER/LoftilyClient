package net.minecraft.client.settings;

import java.util.ArrayList;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class HotbarSnapshot extends ArrayList<ItemStack>
{
    public static final int field_192835_a = InventoryPlayer.getHotbarSize();

    public HotbarSnapshot()
    {
        this.ensureCapacity(field_192835_a);

        for (int i = 0; i < field_192835_a; ++i)
        {
            this.add(ItemStack.field_190927_a);
        }
    }

    public NBTTagList func_192834_a()
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < field_192835_a; ++i)
        {
            nbttaglist.appendTag(((ItemStack)this.get(i)).writeToNBT(new NBTTagCompound()));
        }

        return nbttaglist;
    }

    public void func_192833_a(NBTTagList p_192833_1_)
    {
        for (int i = 0; i < field_192835_a; ++i)
        {
            this.set(i, new ItemStack(p_192833_1_.getCompoundTagAt(i)));
        }
    }

    public boolean isEmpty()
    {
        for (int i = 0; i < field_192835_a; ++i)
        {
            if (!((ItemStack)this.get(i)).isEmptyStack())
            {
                return false;
            }
        }

        return true;
    }
}
