package com.teamwizardry.wizardry.common.block;

import com.teamwizardry.librarianlib.features.base.block.tile.BlockModContainer;
import com.teamwizardry.wizardry.client.render.block.TileOrbHolderRenderer;
import com.teamwizardry.wizardry.common.tile.TileOrbHolder;
import com.teamwizardry.wizardry.init.ModItems;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Created by Demoniaque on 5/7/2016.
 */
public class BlockOrbHolder extends BlockModContainer {

	private static final AxisAlignedBB AABB_PEARL_HOLDER = new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.75, 0.875);

	public BlockOrbHolder() {
		super("pearl_holder", Material.WOOD);
		setSoundType(SoundType.WOOD);
		setHardness(2.0f);
		setResistance(15.0f);
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileOrbHolder.class, new TileOrbHolderRenderer());
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = playerIn.getHeldItem(hand);

		TileOrbHolder te = getTE(worldIn, pos);

		if (!te.containsCell()) {
			if (heldItem.getItem() == ModItems.ORB || heldItem.getItem() == ModItems.PEARL_NACRE) {
				te.setItemStack(heldItem.copy());
				te.getItemStack().setCount(1);
				heldItem.shrink(1);
			} else return false;

		} else {
			ItemStack stack = te.getItemStack().copy();

			te.setItemStack(ItemStack.EMPTY);
			if (playerIn.inventory.addItemStackToInventory(stack)) playerIn.openContainer.detectAndSendChanges();
			else {
				EntityItem entityItem = new EntityItem(worldIn, pos.getX(), pos.getY() + 1, pos.getZ(), stack);
				worldIn.spawnEntity(entityItem);
			}
		}
		te.markDirty();
		return true;
	}

	private TileOrbHolder getTE(World world, BlockPos pos) {
		return (TileOrbHolder) world.getTileEntity(pos);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState iBlockState) {
		return new TileOrbHolder();
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB_PEARL_HOLDER;
	}
}
