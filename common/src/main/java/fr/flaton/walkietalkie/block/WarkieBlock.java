package fr.flaton.walkietalkie.block;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import fr.flaton.walkietalkie.block.entity.WarkieBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WarkieBlock extends BlockWithEntity implements IBE<WarkieBlockEntity> {

    protected WarkieBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Class<WarkieBlockEntity> getBlockEntityClass() {
        return WarkieBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WarkieBlockEntity> getBlockEntityType() {
        return null;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WarkieBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            if (world.getBlockEntity(pos) instanceof WarkieBlockEntity blockEntity) {
//                player.openHandledScreen(blockEntity);
            }
        }

        return ActionResult.CONSUME;
    }
}
