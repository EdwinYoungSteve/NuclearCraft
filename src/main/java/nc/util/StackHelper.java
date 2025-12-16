package nc.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.*;

import javax.annotation.Nonnull;
import java.util.List;

public class StackHelper {
	public static ItemStack emptyStack() {
		try {
			return ItemStack.EMPTY;
		}
		catch (NoSuchFieldError | NoSuchMethodError e) {
			try {
				Item air = getRegistryAirItem();
				if (air != null) return new ItemStack(air, 0);
			}
			catch (Throwable ignored) {}
			return null;
		}
	}
	
	public static ItemStack fixItemStack(Object object) {
		if (object instanceof ItemStack stack) {
			ItemStack copy;
			try {
				copy = stack.copy();
			}
			catch (NoSuchMethodError e) {
				copy = new ItemStack(getItem(stack), getCount(stack), getMetadata(stack));
				if (hasTagCompound(stack)) {
					NBTTagCompound tag = getTagCompoundSafe(stack);
					if (tag != null) copy.setTagCompound((NBTTagCompound) tag.copy());
				}
			}
			if (StackHelper.getCount(copy) == 0) {
				setCount(copy, 1);
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
		if (stack.isEmpty()) {
			Block airBlock = getRegistryAirBlock();
			return airBlock == null ? null : airBlock.getDefaultState();
		}
		int meta = getMetadata(stack);
		Item item = getItem(stack);
		if (!(item instanceof ItemBlock itemBlock)) {
			return null;
		}
		return itemBlock.getBlock().getStateFromMeta(meta);
	}
	
	public static int getMetadata(ItemStack stack) {
		if (stack == null) return 0;
		try {
			return stack.getMetadata();
		}
		catch (NoSuchMethodError e) {
			try {
				return stack.getItem().getMetadata(stack);
			}
			catch (NoSuchMethodError e2) {
				try {
					return stack.getItemDamage();
				}
				catch (NoSuchMethodError e3) {
					return 0;
				}
			}
		}
	}

	public static Item getItem(ItemStack stack) {
		if (stack == null) return getRegistryAirItem();
		try {
			return stack.getItem();
		}
		catch (NoSuchMethodError e) {
			try {
				java.lang.reflect.Field itemField = ItemStack.class.getDeclaredField("item");
				itemField.setAccessible(true);
				Object o = itemField.get(stack);
				if (o instanceof Item) return (Item) o;
			}
			catch (Exception ignored) {}
			return getRegistryAirItem();
		}
	}

	public static int getCount(ItemStack stack) {
		if (stack == null) return 0;
		try {
			return stack.getCount();
		}
		catch (NoSuchMethodError e) {
			// Try common field names used in other mappings/versions
			String[] fieldNames = new String[] {"count", "stackSize", "stack_count", "stack"};
			for (String fieldName : fieldNames) {
				try {
					java.lang.reflect.Field f = ItemStack.class.getDeclaredField(fieldName);
					f.setAccessible(true);
					Object o = f.get(stack);
					if (o instanceof Number) return ((Number) o).intValue();
				}
				catch (Exception ignored) {}
			}
			try {
				return stack.getMaxStackSize();
			}
			catch (Throwable ignored) {}
			return 1;
		}
	}

	public static void setCount(ItemStack stack, int count) {
		if (stack == null) return;
		try {
			stack.setCount(count);
			return;
		}
		catch (NoSuchMethodError e) {
			// Try set via field names
			String[] fieldNames = new String[] {"count", "stackSize", "stack_count", "stack"};
			for (String fieldName : fieldNames) {
				try {
					java.lang.reflect.Field f = ItemStack.class.getDeclaredField(fieldName);
					f.setAccessible(true);
					if (f.getType().isPrimitive() || Number.class.isAssignableFrom(f.getType())) {
						f.set(stack, Integer.valueOf(count));
						return;
					}
				}
				catch (Exception ignored) {}
			}
			try {
				java.lang.reflect.Method m = ItemStack.class.getDeclaredMethod("setCount", int.class);
				m.setAccessible(true);
				m.invoke(stack, count);
				return;
			}
			catch (Exception ignored) {}
		}
	}

	public static boolean hasTagCompound(ItemStack stack) {
		if (stack == null) return false;
		try {
			return stack.hasTagCompound();
		}
		catch (NoSuchMethodError e) {
			try {
				java.lang.reflect.Method m = ItemStack.class.getDeclaredMethod("hasTagCompound");
				m.setAccessible(true);
				Object o = m.invoke(stack);
				if (o instanceof Boolean) return (Boolean) o;
			}
			catch (Exception ignored) {}
			try {
				java.lang.reflect.Field f = ItemStack.class.getDeclaredField("tagCompound");
				f.setAccessible(true);
				Object o = f.get(stack);
				return o != null;
			}
			catch (Exception ignored) {}
			return false;
		}
	}

	public static NBTTagCompound getTagCompoundSafe(ItemStack stack) {
		if (stack == null) return null;
		try {
			return stack.getTagCompound();
		}
		catch (NoSuchMethodError e) {
			try {
				java.lang.reflect.Field f = ItemStack.class.getDeclaredField("tagCompound");
				f.setAccessible(true);
				Object o = f.get(stack);
				if (o instanceof NBTTagCompound) return (NBTTagCompound) o;
			}
			catch (Exception ignored) {}
			return null;
		}
	}

	private static Item getRegistryAirItem() {
		try {
			Item air = Item.REGISTRY.getObject(new ResourceLocation("minecraft", "air"));
			if (air != null) return air;
		}
		catch (Throwable ignored) {}
		try {
			return Items.AIR;
		}
		catch (Throwable ignored) {}
		try {
			Block b = Block.REGISTRY.getObject(new ResourceLocation("minecraft", "air"));
			if (b != null) return Item.getItemFromBlock(b);
		}
		catch (Throwable ignored) {}
		return null;
	}

	private static Block getRegistryAirBlock() {
		try {
			Block air = Block.REGISTRY.getObject(new ResourceLocation("minecraft", "air"));
			if (air != null) return air;
		}
		catch (Throwable ignored) {}
		try {
			return Blocks.AIR;
		}
		catch (Throwable ignored) {}
		return null;
	}

	public static CreativeTabs getSearchTab() {
		try {
			return CreativeTabs.SEARCH;
		}
		catch (NoSuchFieldError | NoSuchMethodError e) {
			try {
				java.lang.reflect.Field f = CreativeTabs.class.getDeclaredField("SEARCH");
				f.setAccessible(true);
				Object o = f.get(null);
				if (o instanceof CreativeTabs) return (CreativeTabs) o;
			}
			catch (Throwable ignored) {}
			return null;
		}
	}
	
	public static boolean isWildcard(ItemStack stack) {
		return getMetadata(stack) == 32767;
	}
	
	public static ItemStack changeStackSize(ItemStack stack, int size) {
		ItemStack newStack;
		try {
			newStack = stack.copy();
		}
		catch (NoSuchMethodError e) {
			newStack = new ItemStack(getItem(stack), size, getMetadata(stack));
			if (hasTagCompound(stack)) {
				NBTTagCompound tag = getTagCompoundSafe(stack);
				if (tag != null) newStack.setTagCompound((NBTTagCompound) tag.copy());
			}
			return newStack;
		}
		newStack.setCount(size);
		setCount(newStack, size);
		return newStack;
	}
	
	public static String stackPath(ItemStack stack) {
		ResourceLocation loc = getRegistryNameForItem(getItem(stack));
		if (loc == null) return null;
		return loc.getPath();
	}
	
	public static String stackName(ItemStack stack) {
		ResourceLocation resourcelocation = getRegistryNameForItem(getItem(stack));
		return resourcelocation == null ? "null" : resourcelocation + ":" + getMetadata(stack);
	}

	private static ResourceLocation getRegistryNameForItem(Item item) {
		if (item == null) return null;
		try {
			ResourceLocation loc = Item.REGISTRY.getNameForObject(item);
			if (loc != null) return loc;
		}
		catch (Throwable ignored) {}
		try {
			java.lang.reflect.Method m = Item.class.getDeclaredMethod("getRegistryName");
			m.setAccessible(true);
			Object o = m.invoke(item);
			if (o instanceof ResourceLocation) return (ResourceLocation) o;
		}
		catch (Throwable ignored) {}
		try {
			java.lang.reflect.Method m2 = Item.class.getDeclaredMethod("getRegistryName", (Class<?>[]) null);
			m2.setAccessible(true);
			Object o2 = m2.invoke(item);
			if (o2 instanceof ResourceLocation) return (ResourceLocation) o2;
		}
		catch (Throwable ignored) {}
		return null;
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

	/**
	 * Compatibility helper for item equality. Uses ItemStack methods available at runtime.
	 */
	public static boolean isItemEqual(ItemStack a, ItemStack b) {
		if (a == null || b == null) return a == b;
		try {
			return a.isItemEqual(b);
		}
		catch (NoSuchMethodError e) {
			// Fallback: compare item and metadata and tags
			if (a.isEmpty() && b.isEmpty()) return true;
			if (a.isEmpty() || b.isEmpty()) return false;
			if (a.getItem() != b.getItem()) return false;
			if (getMetadata(a) != getMetadata(b)) return false;
			return areItemStackTagsEqual(a, b);
		}
	}
}
