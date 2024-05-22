package fr.flaton.walkietalkie.block.entity;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import fr.flaton.walkietalkie.Util;
import fr.flaton.walkietalkie.WalkieTalkieVoiceChatPlugin;
import fr.flaton.walkietalkie.config.ModConfig;
import fr.flaton.walkietalkie.screen.ChannelValueBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class WarkieBlockEntity extends SmartBlockEntity {

    private static final List<WarkieBlockEntity> warkieBlockEntities = new ArrayList<>();

    public static final DirectionProperty FACING = Properties.FACING;

    public static String NBT_KEY_CHANNEL = "walkietalkie.channel";
    public static final String NBT_KEY_MUTE = "walkietalkie.mute";
    public static final String NBT_KEY_ACTIVATE = "walkietalkie.activate";

    protected final PropertyDelegate propertyDelegate;

    boolean activated = true;
    protected ChannelValueBehaviour channelBehaviour;

    private final UUID channelId;
    private LocationalAudioChannel channel = null;

    public WarkieBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARKIEBLOCK.get(), pos, state);
        warkieBlockEntities.add(this);

        channelId = UUID.randomUUID();

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> WarkieBlockEntity.this.activated ? 1 : 0;
                    case 1 -> WarkieBlockEntity.this.channelBehaviour.getValue();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> WarkieBlockEntity.this.activated = value == 1;
                    case 1 -> WarkieBlockEntity.this.channelBehaviour.setValue(value);
                    default -> {
                    }
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        channelBehaviour = new ChannelValueBehaviour(Lang.translateDirect("kinetics.creative_motor.rotation_speed"),
                this, new ChannelValueBox());
        channelBehaviour.setValue(1);
        behaviours.add(channelBehaviour);
    }

    class ChannelValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3d getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 12.5);
        }

    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        return super.onSyncedBlockEvent(type, data);
    }

    @Override
    protected void read(NbtCompound compound, boolean clientPacket) {
        this.activated = compound.getBoolean(NBT_KEY_ACTIVATE);
        this.channelBehaviour.setValue(compound.getInt(NBT_KEY_CHANNEL));
        super.read(compound, clientPacket);
    }

    @Override
    protected void write(NbtCompound compound, boolean clientPacket) {
        compound.putBoolean(NBT_KEY_ACTIVATE, this.activated);
        compound.putInt(NBT_KEY_CHANNEL, this.channelBehaviour.getValue()*-1); // IDK why it's negative but it is
        super.write(compound, clientPacket);
    }

    public static List<WarkieBlockEntity> getSpeakersActivatedInRange(int canal, World world, Vec3d pos, int range) {
        warkieBlockEntities.removeIf(BlockEntity::isRemoved);

        List<WarkieBlockEntity> list = new ArrayList<>();

        for (WarkieBlockEntity warkieBlock : warkieBlockEntities) {

            if (!warkieBlock.hasWorld()) {
                continue;
            }

            if (!ModConfig.crossDimensionsEnabled
                    && !world.getRegistryKey().getRegistry().equals(warkieBlock.getWorld().getRegistryKey().getRegistry())) {
                continue;
            }

            if (!warkieBlock.canBroadcastToSpeaker(world, pos, warkieBlock, range)) {
                continue;
            }

            if (warkieBlock.activated) {
                if (warkieBlock.channelBehaviour.getValue() == canal) {
                    list.add(warkieBlock);
                }
            }
        }

        return list;
    }

    public void playSound(VoicechatServerApi api, MicrophonePacketEvent event) {
        Position pos = api.createPosition(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());

        if (this.channel == null) {
            this.channel = api.createLocationalAudioChannel(this.channelId, api.fromServerLevel(this.getWorld()), pos);
            if (this.channel == null) {
                return;
            }
            this.channel.setCategory(WalkieTalkieVoiceChatPlugin.SPEAKER_CATEGORY);
            this.channel.setDistance(ModConfig.speakerDistance + 1F);
            if (!ModConfig.voiceDuplication) {
                this.channel.setFilter(serverPlayer -> !serverPlayer.getEntity().equals(event.getSenderConnection().getPlayer().getEntity()));
            }
        }
        this.channel.send(event.getPacket().getOpusEncodedData());
    }

    private boolean canBroadcastToSpeaker(World senderWorld, Vec3d senderPos, WarkieBlockEntity speaker, int range) {
        World receiverWorld = speaker.getWorld();

        if (receiverWorld == null) {
            return false;
        }

        return Util.canBroadcastToReceiver(senderWorld, receiverWorld, senderPos, speaker.pos.toCenterPos(), range);
    }
}
