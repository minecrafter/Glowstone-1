package net.glowstone.block.blocktype;

import net.glowstone.block.GlowBlock;
import net.glowstone.block.GlowBlockState;
import net.glowstone.block.ItemTable;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.net.message.play.game.BlockChangeMessage;
import net.glowstone.scheduler.PulseTask;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sam
 */
public class BlockRedstone extends BlockNeedsAttached {

    private static final BlockFace[] ADJACENT = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
    private static final BlockFace[] SIDES = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public BlockRedstone() {
        setDrops(new ItemStack(Material.REDSTONE));
    }

    public static List<BlockFace> calculateConnections(GlowBlock block) {
        List<BlockFace> value = new ArrayList<>();
        List<BlockFace> connections = new ArrayList<>();
        value.add(BlockFace.DOWN);
        for (BlockFace face : SIDES) {
            GlowBlock target = block.getRelative(face);
            switch (target.getType()) {
                case DIODE_BLOCK_ON:
                case DIODE_BLOCK_OFF:
                    Diode diode = (Diode) target.getState().getData();
                    if (face == diode.getFacing() || face == diode.getFacing().getOppositeFace()) {
                        connections.add(face);
                    }
                    break;
                case REDSTONE_BLOCK:
                case REDSTONE_TORCH_ON:
                case REDSTONE_TORCH_OFF:
                case REDSTONE_WIRE:
                case WOOD_BUTTON:
                case STONE_BUTTON:
                case LEVER:
                    connections.add(face);
                    break;
                default:
                    if (target.getType().isSolid() && !block.getRelative(BlockFace.UP).getType().isSolid()
                            && target.getRelative(BlockFace.UP).getType() == Material.REDSTONE_WIRE) {
                        connections.add(face);
                    } else if (!target.getType().isSolid()
                            && target.getRelative(BlockFace.DOWN).getType() == Material.REDSTONE_WIRE) {
                        connections.add(face);
                    }
                    break;
            }
        }

        if (connections.isEmpty()) {
            value.addAll(Arrays.asList(SIDES));
        } else {
            value.addAll(connections);
            if (connections.size() == 1) {
                value.add(connections.get(0).getOppositeFace());
            }
        }

        return value;
    }

    @Override
    public boolean canPlaceAt(GlowBlock block, BlockFace against) {
        if (block.getRelative(BlockFace.DOWN).getType().isSolid()) {
            return true;
        }

        GlowBlock target = block.getRelative(BlockFace.DOWN);
        switch (target.getType()) {
            case WOOD_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case SMOOTH_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case QUARTZ_STAIRS:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
                return ((Stairs) target.getState().getData()).isInverted();
            case STEP:
            case WOOD_STEP:
                return ((Step) target.getState().getData()).isInverted();
            case GLOWSTONE:
                return true;
        }

        return false;
    }

    @Override
    public void afterPlace(GlowPlayer player, GlowBlock block, ItemStack holding, GlowBlockState oldState) {
        updatePhysics(block);
    }

    @Override
    public void onNearBlockChanged(GlowBlock block, BlockFace face, GlowBlock changedBlock, Material oldType, byte oldData, Material newType, byte newData) {
        updatePhysics(block);
    }

    @Override
    public void updatePhysics(GlowBlock me) {
        super.updatePhysics(me);

        for (BlockFace face : ADJACENT) {
            GlowBlock target = me.getRelative(face);

            switch (target.getType()) {
                case LEVER:
                    Lever lever = (Lever) target.getState().getData();
                    if (lever.isPowered()) {
                        if (me.getData() != 15) {
                            me.setData((byte) 15);
                            extraUpdate(me);
                        }
                        return;
                    }
                    break;
                case STONE_BUTTON:
                case WOOD_BUTTON:
                    Button button = (Button) target.getState().getData();
                    if (button.isPowered()) {
                        if (me.getData() != 15) {
                            me.setData((byte) 15);
                            extraUpdate(me);
                        }
                        return;
                    }
                    break;
                case DIODE_BLOCK_ON:
                    Diode diode = (Diode) target.getState().getData();
                    if (face == diode.getFacing().getOppositeFace()) {
                        if (me.getData() != 15) {
                            me.setData((byte) 15);
                            extraUpdate(me);
                        }
                        return;
                    }
                    break;
                case REDSTONE_BLOCK:
                case REDSTONE_TORCH_ON:
                    if (me.getData() != 15) {
                        me.setData((byte) 15);
                        extraUpdate(me);
                    }
                    return;
                default:
                    if (target.getType().isSolid() && target.getRelative(BlockFace.DOWN).getType() == Material.REDSTONE_TORCH_ON) {
                        if (me.getData() != 15) {
                            me.setData((byte) 15);
                            extraUpdate(me);
                        }
                        return;
                    }
                    if (target.getType().isSolid()) {
                        for (BlockFace face2 : ADJACENT) {
                            GlowBlock target2 = target.getRelative(face2);
                            if (target2.getType() == Material.DIODE_BLOCK_ON
                                    && ((Diode) target2.getState().getData()).getFacing() == target2.getFace(target)) {
                                if (me.getData() != 15) {
                                    me.setData((byte) 15);
                                    extraUpdate(me);
                                }
                                return;
                            } else if (target2.getType() == Material.STONE_BUTTON
                                    || target2.getType() == Material.WOOD_BUTTON) {
                                Button button2 = (Button) target2.getState().getData();
                                if (button2.isPowered() && button2.getAttachedFace() == target2.getFace(target)) {
                                    if (me.getData() != 15) {
                                        me.setData((byte) 15);
                                        extraUpdate(me);
                                    }
                                    return;
                                }
                            } else if (target2.getType() == Material.LEVER) {
                                Lever lever2 = (Lever) target2.getState().getData();
                                if (lever2.isPowered() && lever2.getAttachedFace() == target2.getFace(target)) {
                                    if (me.getData() != 15) {
                                        me.setData((byte) 15);
                                        extraUpdate(me);
                                    }
                                    return;
                                }
                            }
                        }
                    }
            }
        }

        byte power = 0;

        for (BlockFace face : calculateConnections(me)) {

            if (face == BlockFace.DOWN) {
                continue;
            }

            GlowBlock target = me.getRelative(face);
            if (target.getType() != Material.REDSTONE_WIRE) {
                if (!target.getType().isSolid()) {
                    target = target.getRelative(BlockFace.DOWN);
                } else if (!me.getRelative(BlockFace.UP).getType().isSolid()) {
                    target = target.getRelative(BlockFace.UP);
                }

                if (target.getType() != Material.REDSTONE_WIRE) {
                    // There is no redstone wire here..
                    continue;
                }
            }

            if (target.getData() > power) {
                power = (byte) (target.getData() - 1);
            }
        }

        if (power != me.getData()) {
            me.setData(power);
            extraUpdate(me);
            new PulseTask(me, true, 1, true).startPulseTask();
        }
    }

    private void extraUpdate(GlowBlock block) {
        ItemTable itemTable = ItemTable.instance();
        for (BlockFace face : calculateConnections(block)) {
            GlowBlock target = block.getRelative(face);
            if (target.getType().isSolid()) {
                for (BlockFace face2 : ADJACENT) {
                    GlowBlock target2 = target.getRelative(face2);
                    BlockType notifyType = itemTable.getBlock(target2.getTypeId());
                    if (notifyType != null) {
                        if (target2.getFace(block) == null) {
                            notifyType.onNearBlockChanged(target2, BlockFace.SELF, block, block.getType(), block.getData(), block.getType(), block.getData());
                        }
                        notifyType.onRedstoneUpdate(target2);
                    }
                }
            }
        }
    }

    @Override
    public void receivePulse(GlowBlock me) {
        BlockChangeMessage bcmsg = new BlockChangeMessage(me.getX(), me.getY(), me.getZ(), me.getTypeId(), me.getData());
        for (GlowPlayer p : me.getWorld().getRawPlayers()) {
            p.sendBlockChange(bcmsg);
        }
    }
}
