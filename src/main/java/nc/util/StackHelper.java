package nc.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.*;

import javax.annotation.Nonnull;
import java.util.List;

public class StackHelper {
	
	public static ItemStack fixItemStack(Object object) {
		if (object instanceof ItemStack stack) {
			ItemStack copy = stack.copy();
			if (copy.getCount() == 0) {
				copy.setCount(1);
			}
			return copy;
		}
		else if (object instanceof Item item) {
			return new ItemStack(item, 1);
		}
		else if (object instanceof Block block) {
			return new ItemStack(block, 1);
		}
		else {
			throw new RuntimeException(String.format("Invalid ItemStack: %s", object));
		}
	}
	
	public static ItemStack blockStateToStack(IBlockState blockState) {
		if (blockState == null || blockState.getMaterial().equals(Material.AIR)) {
			return ItemStack.EMPTY;
		}
		Block block = blockState.getBlock();
		if (block == null) {
			return ItemStack.EMPTY;
		}
		int meta = block.getMetaFromState(blockState);
		return new ItemStack(block, 1, meta);
	}
	
	public static IBlockState getBlockStateFromStack(ItemStack stack) {
		if (stack == null) {
			return null;
		}
		if (stack.getItem() == Items.AIR) {
			return Blocks.AIR.getDefaultState();
		}
		if (stack.isEmpty()) {
			return null;
		}
		int meta = getMetadata(stack);
		Item item = stack.getItem();
		if (!(item instanceof ItemBlock itemBlock)) {
			return null;
		}
		return itemBlock.getBlock().getStateFromMeta(meta);
	}
	
	public static int getMetadata(ItemStack stack) {
		return Items.DIAMOND.getMetadata(stack);
	}
	
	public static boolean isWildcard(ItemStack stack) {
		return getMetadata(stack) == 32767;
	}
	
	public static ItemStack changeStackSize(ItemStack stack, int size) {
		ItemStack newStack = stack.copy();
		newStack.setCount(size);
		return newStack;
	}
	
	public static String stackPath(ItemStack stack) {
		ResourceLocation loc = Item.REGISTRY.getNameForObject(stack.getItem());
		if (loc == null) {
			return null;
		}
		else {
			return loc.getPath();
		}
	}
	
	public static String stackName(ItemStack stack) {
		ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(stack.getItem());
		return resourcelocation == null ? "null" : resourcelocation + ":" + getMetadata(stack);
	}
	
	public static String stackListNames(List<ItemStack> list) {
		StringBuilder names = new StringBuilder();
		for (ItemStack stack : list) {
			names.append(", ").append(stackName(stack));
		}
		return names.substring(2);
	}
	
	/**
	 * Stack tag comparison without checking capabilities such as radiation
	 */
	public static boolean areItemStackTagsEqual(ItemStack stackA, ItemStack stackB) {
		boolean isAEmpty = stackA.isEmpty(), isBEmpty = stackB.isEmpty();
		if (isAEmpty && isBEmpty) {
			return true;
		}
		else if (!isAEmpty && !isBEmpty) {
			NBTTagCompound stackANBT = stackA.getTagCompound(), stackBNBT = stackB.getTagCompound();
			if (stackANBT == null) {
				return stackBNBT == null;
			}
			else {
				return stackANBT.equals(stackBNBT);
			}
		}
		
		return false;
	}
	
	public static ItemStack getBucket(@Nonnull FluidStack fluidStack) {
		return FluidUtil.getFilledBucket(fluidStack);
	}
	
	public static ItemStack getBucket(String fluidName) {
		return getBucket(new FluidStack(FluidRegistry.getFluid(fluidName), Fluid.BUCKET_VOLUME));
	}
}
