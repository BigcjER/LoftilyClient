package net.minecraft.client.entity;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import loftily.Client;
import loftily.event.impl.client.ChatEvent;
import loftily.event.impl.player.RotationEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.slowdown.ItemSlowDownEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.exploit.disablers.GrimDisabler;
import loftily.utils.math.Rotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ElytraSound;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EntityPlayerSP extends AbstractClientPlayer {
    public final NetHandlerPlayClient connection;
    private final StatisticsManager statWriter;
    private final RecipeBook field_192036_cb;
    public MovementInput movementInput;
    /**
     * Ticks left before sprinting is disabled.
     */
    public int sprintingTicksLeft;
    public float renderArmYaw;
    public float renderArmPitch;
    public float prevRenderArmYaw;
    public float prevRenderArmPitch;
    /**
     * The amount of time an entity has been in a Portal
     */
    public float timeInPortal;
    /**
     * The amount of time an entity has been in a Portal the previous tick
     */
    public float prevTimeInPortal;
    protected Minecraft mc;
    /**
     * Used to tell if the player pressed forward twice. If this is at 0 and it's pressed (And they are allowed to
     * sprint, aka enough food on the ground etc) it sets this to 7. If it's pressed and it's greater than 0 enable
     * sprinting.
     */
    protected int sprintToggleTimer;
    private int permissionLevel = 0;
    /**
     * The last X position which was transmitted to the server, used to determine when the X position changes and needs
     * to be re-trasmitted
     */
    private double lastReportedPosX;
    /**
     * The last Y position which was transmitted to the server, used to determine when the Y position changes and needs
     * to be re-transmitted
     */
    private double lastReportedPosY;
    /**
     * The last Z position which was transmitted to the server, used to determine when the Z position changes and needs
     * to be re-transmitted
     */
    private double lastReportedPosZ;
    /**
     * The last yaw value which was transmitted to the server, used to determine when the yaw changes and needs to be
     * re-transmitted
     */
    private float lastReportedYaw;
    /**
     * The last pitch value which was transmitted to the server, used to determine when the pitch changes and needs to
     * be re-transmitted
     */
    private float lastReportedPitch;
    private boolean prevOnGround;
    /**
     * the last sneaking state sent to the server
     */
    private boolean serverSneakState;
    /**
     * the last sprinting state sent to the server
     */
    public boolean serverSprintState;
    /**
     * Reset to 0 every time position is sent to the server, used to send periodic updates every 20 ticks even when the
     * player is not moving.
     */
    private int positionUpdateTicks;
    private boolean hasValidHealth;
    private String serverBrand;
    private int horseJumpPowerCounter;
    private float horseJumpPower;
    public boolean handActive;
    private EnumHand activeHand;
    private boolean rowingBoat;
    private boolean autoJumpEnabled = true;
    private int autoJumpTime;
    private boolean wasFallFlying;
    
    public float renderPitchHead;
    public float prevRenderPitchHead;
    
    public int onGroundTicks, offGroundTicks;
    public int kills;
    
    public EntityPlayerSP(Minecraft p_i47378_1_, World p_i47378_2_, NetHandlerPlayClient p_i47378_3_, StatisticsManager p_i47378_4_, RecipeBook p_i47378_5_) {
        super(p_i47378_2_, p_i47378_3_.getGameProfile());
        this.connection = p_i47378_3_;
        this.statWriter = p_i47378_4_;
        this.field_192036_cb = p_i47378_5_;
        this.mc = p_i47378_1_;
        this.dimension = 0;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    /**
     * Heal living entity (param: amount of half-hearts)
     */
    public void heal(float healAmount) {
    }

    public boolean startRiding(Entity entityIn, boolean force) {
        if (!super.startRiding(entityIn, force)) {
            return false;
        } else {
            if (entityIn instanceof EntityMinecart) {
                this.mc.getSoundHandler().playSound(new MovingSoundMinecartRiding(this, (EntityMinecart) entityIn));
            }

            if (entityIn instanceof EntityBoat) {
                this.prevRotationYaw = entityIn.rotationYaw;
                this.rotationYaw = entityIn.rotationYaw;
                this.setRotationYawHead(entityIn.rotationYaw);
            }

            return true;
        }
    }

    public void dismountRidingEntity() {
        super.dismountRidingEntity();
        this.rowingBoat = false;
    }

    /**
     * interpolated look vector
     */
    public Vec3d getLook(float partialTicks) {
        return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate() {
        if (this.world.isBlockLoaded(new BlockPos(this.posX, 0.0D, this.posZ))) {
            UpdateEvent updateEvent = new UpdateEvent();
            Client.INSTANCE.getEventManager().call(updateEvent);
            if (updateEvent.isCancelled()) {
                return;
            }
            super.onUpdate();

            if (this.isRiding()) {
                this.connection.sendPacket(new CPacketPlayer.Rotation(this.rotationYaw, this.rotationPitch, this.onGround));
                this.connection.sendPacket(new CPacketInput(this.moveStrafing, this.field_191988_bg, this.movementInput.jump, this.movementInput.sneak));
                Entity entity = this.getLowestRidingEntity();

                if (entity != this && entity.canPassengerSteer()) {
                    this.connection.sendPacket(new CPacketVehicleMove(entity));
                }
            } else {
                this.onUpdateWalkingPlayer();
            }
        }
    }

    /**
     * called every tick when the player is on foot. Performs all the things that normally happen during movement.
     */
    private void onUpdateWalkingPlayer() {
        MotionEvent motionEvent = new MotionEvent(this.posX, this.getEntityBoundingBox().minY, this.posZ, this.onGround);
        Client.INSTANCE.getEventManager().call(motionEvent);
        if(motionEvent.isCancelled()){
            return;
        }
        
        boolean flag = this.isSprinting();

        if (flag != this.serverSprintState) {
            if (flag) {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
            } else {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            this.serverSprintState = flag;
        }

        boolean flag1 = this.isSneaking();

        if (flag1 != this.serverSneakState) {
            if (flag1) {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
            } else {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            this.serverSneakState = flag1;
        }

        if (this.isCurrentViewEntity()) {

            double x = motionEvent.getX();
            double y = motionEvent.getY();
            double z = motionEvent.getZ();
            boolean ground = motionEvent.isOnGround();

            double diffX = x - this.lastReportedPosX;
            double diffY = y - this.lastReportedPosY;
            double diffZ = z - this.lastReportedPosZ;

            RotationEvent rotationEvent = new RotationEvent(new Rotation(this.rotationYaw, this.rotationPitch));
            Client.INSTANCE.getEventManager().call(rotationEvent);
            float yaw = rotationEvent.getRotation().yaw;
            float pitch = rotationEvent.getRotation().pitch;

            double diffYaw = yaw - this.lastReportedYaw;
            double diffPitch = pitch - this.lastReportedPitch;
            boolean olderThanOrEqualTo1_8 = ViaLoadingBase.getInstance().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8);
            
            if (!olderThanOrEqualTo1_8)
                ++this.positionUpdateTicks;

            double zeroZero3 = ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_18_2) ? 4.0E-8D : 9.0E-4D;
            boolean isMovingSignificantly = diffX * diffX + diffY * diffY + diffZ * diffZ > zeroZero3 || this.positionUpdateTicks >= 20;
            boolean isRotating = diffYaw != 0.0D || diffPitch != 0.0D;
            
            if (olderThanOrEqualTo1_8) {
                if (!this.isRiding()) {
                    if (isMovingSignificantly && isRotating) {
                        this.connection.sendPacket(new CPacketPlayer.PositionRotation(x, y, z, yaw, pitch, ground));
                    } else if (isMovingSignificantly) {
                        this.connection.sendPacket(new CPacketPlayer.Position(x, y, z, ground));
                    } else if (isRotating) {
                        this.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, ground));
                    } else {
                        this.connection.sendPacket(new CPacketPlayer(ground));
                    }
                    GrimDisabler.INSTANCE.processPackets();
                }
            } else {
                if (this.isRiding()) {
                    this.connection.sendPacket(new CPacketPlayer.PositionRotation(this.motionX, -999.0D, this.motionZ, yaw, pitch, ground));
                    isMovingSignificantly = false;
                } else if (isMovingSignificantly && isRotating) {
                    this.connection.sendPacket(new CPacketPlayer.PositionRotation(x, y, z, yaw, pitch, ground));
                } else if (isMovingSignificantly) {
                    this.connection.sendPacket(new CPacketPlayer.Position(x, y, z, ground));
                } else if (isRotating) {
                    this.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, ground));
                } else if (this.prevOnGround != ground) {
                    this.connection.sendPacket(new CPacketPlayer(ground));
                }
                GrimDisabler.INSTANCE.processPackets();
            }
            
            if (olderThanOrEqualTo1_8)
                ++this.positionUpdateTicks;

            if (isMovingSignificantly) {
                this.lastReportedPosX = x;
                this.lastReportedPosY = y;
                this.lastReportedPosZ = z;
                this.positionUpdateTicks = 0;
            }

            if (isRotating) {
                this.lastReportedYaw = yaw;
                this.lastReportedPitch = pitch;
            }

            this.prevOnGround = ground;
            this.autoJumpEnabled = this.mc.gameSettings.autoJump;

           Client.INSTANCE.getEventManager().call(new MotionEvent());

        }
    }

    /**
     * Drop one item out of the currently selected stack if {@code dropAll} is false. If {@code dropItem} is true the
     * entire stack is dropped.
     */
    @Nullable
    public EntityItem dropItem(boolean dropAll) {
        CPacketPlayerDigging.Action cpacketplayerdigging$action = dropAll ? CPacketPlayerDigging.Action.DROP_ALL_ITEMS : CPacketPlayerDigging.Action.DROP_ITEM;
        this.connection.sendPacket(new CPacketPlayerDigging(cpacketplayerdigging$action, BlockPos.ORIGIN, EnumFacing.DOWN));
        return null;
    }

    protected ItemStack dropItemAndGetStack(EntityItem p_184816_1_) {
        return ItemStack.field_190927_a;
    }

    /**
     * Sends a chat message from the player.
     */
    public void sendChatMessage(String message) {
        ChatEvent event = new ChatEvent(message);
        Client.INSTANCE.getEventManager().call(event);
        if(event.isCancelled()) return;
        
        this.connection.sendPacket(new CPacketChatMessage(message));
    }

    public void swingArm(EnumHand hand) {
        super.swingArm(hand);
        this.connection.sendPacket(new CPacketAnimation(hand));
    }

    public void respawnPlayer() {
        this.connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
    }

    /**
     * Deals damage to the entity. This will take the armor of the entity into consideration before damaging the health
     * bar.
     */
    protected void damageEntity(DamageSource damageSrc, float damageAmount) {
        if (!this.isEntityInvulnerable(damageSrc)) {
            this.setHealth(this.getHealth() - damageAmount);
        }
    }

    /**
     * set current crafting inventory back to the 2x2 square
     */
    public void closeScreen() {
        this.connection.sendPacket(new CPacketCloseWindow(this.openContainer.windowId));
        this.closeScreenAndDropStack();
    }

    public void closeScreenAndDropStack() {
        this.inventory.setItemStack(ItemStack.field_190927_a);
        super.closeScreen();
        this.mc.displayGuiScreen(null);
    }

    /**
     * Updates health locally.
     */
    public void setPlayerSPHealth(float health) {
        if (this.hasValidHealth) {
            float f = this.getHealth() - health;

            if (f <= 0.0F) {
                this.setHealth(health);

                if (f < 0.0F) {
                    this.hurtResistantTime = this.maxHurtResistantTime / 2;
                }
            } else {
                this.lastDamage = f;
                this.setHealth(this.getHealth());
                this.hurtResistantTime = this.maxHurtResistantTime;
                this.damageEntity(DamageSource.generic, f);
                this.maxHurtTime = 10;
                this.hurtTime = this.maxHurtTime;
            }
        } else {
            this.setHealth(health);
            this.hasValidHealth = true;
        }
    }

    /**
     * Adds a value to a statistic field.
     */
    public void addStat(StatBase stat, int amount) {
        if (stat.isIndependent) {
            super.addStat(stat, amount);
        }
    }

    /**
     * Sends the player's abilities to the server (if there is one).
     */
    public void sendPlayerAbilities() {
        this.connection.sendPacket(new CPacketPlayerAbilities(this.capabilities));
    }

    /**
     * returns true if this is an EntityPlayerSP, or the logged in player.
     */
    public boolean isUser() {
        return true;
    }

    protected void sendHorseJump() {
        this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_RIDING_JUMP, MathHelper.floor(this.getHorseJumpPower() * 100.0F)));
    }

    public void sendHorseInventory() {
        this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.OPEN_INVENTORY));
    }

    /**
     * Gets the brand of the currently connected server. May be null if the server hasn't yet sent brand information.
     * Server brand information is sent over the {@code MC|Brand} plugin channel, and is used to identify modded servers
     * in crash reports.
     */
    public String getServerBrand() {
        return this.serverBrand;
    }

    /**
     * Sets the brand of the currently connected server. Server brand information is sent over the {@code MC|Brand}
     * plugin channel, and is used to identify modded servers in crash reports.
     */
    public void setServerBrand(String brand) {
        this.serverBrand = brand;
    }

    public StatisticsManager getStatFileWriter() {
        return this.statWriter;
    }

    public RecipeBook func_192035_E() {
        return this.field_192036_cb;
    }

    public void func_193103_a(IRecipe p_193103_1_) {
        if (this.field_192036_cb.func_194076_e(p_193103_1_)) {
            this.field_192036_cb.func_194074_f(p_193103_1_);
            this.connection.sendPacket(new CPacketRecipeInfo(p_193103_1_));
        }
    }

    public int getPermissionLevel() {
        return this.permissionLevel;
    }

    public void setPermissionLevel(int p_184839_1_) {
        this.permissionLevel = p_184839_1_;
    }

    public void addChatComponentMessage(ITextComponent chatComponent, boolean p_146105_2_) {
        if (p_146105_2_) {
            this.mc.ingameGUI.setRecordPlaying(chatComponent, false);
        } else {
            this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
        }
    }

    protected boolean pushOutOfBlocks(double x, double y, double z) {
        if (this.noClip) {
            return false;
        } else {
            BlockPos blockpos = new BlockPos(x, y, z);
            double d0 = x - (double) blockpos.getX();
            double d1 = z - (double) blockpos.getZ();

            if (!this.isOpenBlockSpace(blockpos)) {
                int i = -1;
                double d2 = 9999.0D;

                if (this.isOpenBlockSpace(blockpos.west()) && d0 < d2) {
                    d2 = d0;
                    i = 0;
                }

                if (this.isOpenBlockSpace(blockpos.east()) && 1.0D - d0 < d2) {
                    d2 = 1.0D - d0;
                    i = 1;
                }

                if (this.isOpenBlockSpace(blockpos.north()) && d1 < d2) {
                    d2 = d1;
                    i = 4;
                }

                if (this.isOpenBlockSpace(blockpos.south()) && 1.0D - d1 < d2) {
                    d2 = 1.0D - d1;
                    i = 5;
                }

                float f = 0.1F;

                if (i == 0) {
                    this.motionX = -0.10000000149011612D;
                }

                if (i == 1) {
                    this.motionX = 0.10000000149011612D;
                }

                if (i == 4) {
                    this.motionZ = -0.10000000149011612D;
                }

                if (i == 5) {
                    this.motionZ = 0.10000000149011612D;
                }
            }

            return false;
        }
    }

    /**
     * Returns true if the block at the given BlockPos and the block above it are NOT full cubes.
     */
    private boolean isOpenBlockSpace(BlockPos pos) {
        return !this.world.getBlockState(pos).isNormalCube() && !this.world.getBlockState(pos.up()).isNormalCube();
    }

    /**
     * Set sprinting switch for Entity.
     */
    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
        this.sprintingTicksLeft = 0;
    }

    /**
     * Sets the current XP, total XP, and level number.
     */
    public void setXPStats(float currentXP, int maxXP, int level) {
        this.experience = currentXP;
        this.experienceTotal = maxXP;
        this.experienceLevel = level;
    }

    /**
     * Send a chat message to the CommandSender
     */
    public void addChatMessage(ITextComponent component) {
        this.mc.ingameGUI.getChatGUI().printChatMessage(component);
    }

    /**
     * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
     */
    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return permLevel <= this.getPermissionLevel();
    }

    public void handleStatusUpdate(byte id) {
        if (id >= 24 && id <= 28) {
            this.setPermissionLevel(id - 24);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    /**
     * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
     * the coordinates 0, 0, 0
     */
    public BlockPos getPosition() {
        return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
    }

    public void playSound(SoundEvent soundIn, float volume, float pitch) {
        this.world.playSound(this.posX, this.posY, this.posZ, soundIn, this.getSoundCategory(), volume, pitch, false);
    }

    /**
     * Returns whether the entity is in a server world
     */
    public boolean isServerWorld() {
        return true;
    }

    public boolean isHandActive() {
        return this.handActive;
    }

    public void resetActiveHand() {
        super.resetActiveHand();
        this.handActive = false;
    }

    public EnumHand getActiveHand() {
        return this.activeHand;
    }

    public void setActiveHand(EnumHand hand) {
        ItemStack itemstack = this.getHeldItem(hand);

        if (!itemstack.isEmptyStack() && !this.isHandActive()) {
            super.setActiveHand(hand);
            this.handActive = true;
            this.activeHand = hand;
        }
    }

    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);

        if (HAND_STATES.equals(key)) {
            boolean flag = (this.dataManager.get(HAND_STATES).byteValue() & 1) > 0;
            EnumHand enumhand = (this.dataManager.get(HAND_STATES).byteValue() & 2) > 0 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

            if (flag && !this.handActive) {
                this.setActiveHand(enumhand);
            } else if (!flag && this.handActive) {
                this.resetActiveHand();
            }
        }

        if (FLAGS.equals(key) && this.isElytraFlying() && !this.wasFallFlying) {
            this.mc.getSoundHandler().playSound(new ElytraSound(this));
        }
    }

    public boolean isRidingHorse() {
        Entity entity = this.getRidingEntity();
        return this.isRiding() && entity instanceof IJumpingMount && ((IJumpingMount) entity).canJump();
    }

    public float getHorseJumpPower() {
        return this.horseJumpPower;
    }

    public void openEditSign(TileEntitySign signTile) {
        this.mc.displayGuiScreen(new GuiEditSign(signTile));
    }

    public void displayGuiEditCommandCart(CommandBlockBaseLogic commandBlock) {
        this.mc.displayGuiScreen(new GuiEditCommandBlockMinecart(commandBlock));
    }

    public void displayGuiCommandBlock(TileEntityCommandBlock commandBlock) {
        this.mc.displayGuiScreen(new GuiCommandBlock(commandBlock));
    }

    public void openEditStructure(TileEntityStructure structure) {
        this.mc.displayGuiScreen(new GuiEditStructure(structure));
    }

    public void openBook(ItemStack stack, EnumHand hand) {
        Item item = stack.getItem();

        if (item == Items.WRITABLE_BOOK) {
            this.mc.displayGuiScreen(new GuiScreenBook(this, stack, true));
        }
    }

    /**
     * Displays the GUI for interacting with a chest inventory.
     */
    public void displayGUIChest(IInventory chestInventory) {
        String s = chestInventory instanceof IInteractionObject ? ((IInteractionObject) chestInventory).getGuiID() : "minecraft:container";

        if ("minecraft:chest".equals(s)) {
            this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
        } else if ("minecraft:hopper".equals(s)) {
            this.mc.displayGuiScreen(new GuiHopper(this.inventory, chestInventory));
        } else if ("minecraft:furnace".equals(s)) {
            this.mc.displayGuiScreen(new GuiFurnace(this.inventory, chestInventory));
        } else if ("minecraft:brewing_stand".equals(s)) {
            this.mc.displayGuiScreen(new GuiBrewingStand(this.inventory, chestInventory));
        } else if ("minecraft:beacon".equals(s)) {
            this.mc.displayGuiScreen(new GuiBeacon(this.inventory, chestInventory));
        } else if (!"minecraft:dispenser".equals(s) && !"minecraft:dropper".equals(s)) {
            if ("minecraft:shulker_box".equals(s)) {
                this.mc.displayGuiScreen(new GuiShulkerBox(this.inventory, chestInventory));
            } else {
                this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
            }
        } else {
            this.mc.displayGuiScreen(new GuiDispenser(this.inventory, chestInventory));
        }
    }

    public void openGuiHorseInventory(AbstractHorse horse, IInventory inventoryIn) {
        this.mc.displayGuiScreen(new GuiScreenHorseInventory(this.inventory, inventoryIn, horse));
    }

    public void displayGui(IInteractionObject guiOwner) {
        String s = guiOwner.getGuiID();

        if ("minecraft:crafting_table".equals(s)) {
            this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.world));
        } else if ("minecraft:enchanting_table".equals(s)) {
            this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.world, guiOwner));
        } else if ("minecraft:anvil".equals(s)) {
            this.mc.displayGuiScreen(new GuiRepair(this.inventory, this.world));
        }
    }

    public void displayVillagerTradeGui(IMerchant villager) {
        this.mc.displayGuiScreen(new GuiMerchant(this.inventory, villager, this.world));
    }

    /**
     * Called when the entity is dealt a critical hit.
     */
    public void onCriticalHit(Entity entityHit) {
        this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT);
    }

    public void onEnchantmentCritical(Entity entityHit) {
        this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT_MAGIC);
    }

    /**
     * Returns if this entity is sneaking.
     */
    public boolean isSneaking() {
        boolean flag = this.movementInput != null && this.movementInput.sneak;
        return flag && !this.sleeping;
    }

    public void updateEntityActionState() {
        super.updateEntityActionState();

        if (this.isCurrentViewEntity()) {
            this.moveStrafing = this.movementInput.moveStrafe;
            this.field_191988_bg = this.movementInput.moveForward;
            this.isJumping = this.movementInput.jump;
            this.prevRenderArmYaw = this.renderArmYaw;
            this.prevRenderArmPitch = this.renderArmPitch;
            this.renderArmPitch = (float) ((double) this.renderArmPitch + (double) (this.rotationPitch - this.renderArmPitch) * 0.5D);
            this.renderArmYaw = (float) ((double) this.renderArmYaw + (double) (this.rotationYaw - this.renderArmYaw) * 0.5D);
        }
    }

    protected boolean isCurrentViewEntity() {
        return this.mc.getRenderViewEntity() == this;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate() {
        Client.INSTANCE.getEventManager().call(new PreUpdateEvent());

        ++this.sprintingTicksLeft;

        if (this.sprintToggleTimer > 0) {
            --this.sprintToggleTimer;
        }

        this.prevTimeInPortal = this.timeInPortal;

        if (this.inPortal) {
            if (this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame()) {
                if (this.mc.currentScreen instanceof GuiContainer) {
                    this.closeScreen();
                }

                this.mc.displayGuiScreen(null);
            }

            if (this.timeInPortal == 0.0F) {
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_PORTAL_TRIGGER, this.rand.nextFloat() * 0.4F + 0.8F));
            }

            this.timeInPortal += 0.0125F;

            if (this.timeInPortal >= 1.0F) {
                this.timeInPortal = 1.0F;
            }

            this.inPortal = false;
        } else if (this.isPotionActive(MobEffects.NAUSEA) && this.getActivePotionEffect(MobEffects.NAUSEA).getDuration() > 60) {
            this.timeInPortal += 0.006666667F;

            if (this.timeInPortal > 1.0F) {
                this.timeInPortal = 1.0F;
            }
        } else {
            if (this.timeInPortal > 0.0F) {
                this.timeInPortal -= 0.05F;
            }

            if (this.timeInPortal < 0.0F) {
                this.timeInPortal = 0.0F;
            }
        }

        if (this.timeUntilPortal > 0) {
            --this.timeUntilPortal;
        }

        boolean flag = this.movementInput.jump;
        boolean flag1 = this.movementInput.sneak;
        boolean flag2 = this.movementInput.moveForward >= 0.8F;
        this.movementInput.updatePlayerMoveState();
        this.mc.func_193032_ao().func_193293_a(this.movementInput);
        
        ItemSlowDownEvent event = new ItemSlowDownEvent(0.2F, 0.2F);
        Client.INSTANCE.getEventManager().call(event);
        
        if (this.isHandActive() && !this.isRiding() && !event.isCancelled()) {
            this.movementInput.moveStrafe *= event.getStrafeMultiplier();
            this.movementInput.moveForward *= event.getForwardMultiplier();
            if (event.getForwardMultiplier() < 1 && event.getStrafeMultiplier() < 1)
                this.sprintToggleTimer = 0;
        }

        boolean flag3 = false;

        if (this.autoJumpTime > 0) {
            --this.autoJumpTime;
            flag3 = true;
            this.movementInput.jump = true;
        }

        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ + (double) this.width * 0.35D);
        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ - (double) this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ - (double) this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ + (double) this.width * 0.35D);
        boolean flag4 = (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;

        if (this.onGround && !flag1 && !flag2 && this.movementInput.moveForward >= 0.8F && !this.isSprinting() && flag4 && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS)) {
            if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && this.movementInput.moveForward >= 0.8F && flag4 && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.setSprinting(true);
        }

        if (this.isSprinting() && (this.movementInput.moveForward < 0.8F || this.isCollidedHorizontally || !flag4)) {
            this.setSprinting(false);
        }

        Client.INSTANCE.getEventManager().call(new LivingUpdateEvent());

        if (this.capabilities.allowFlying) {
            if (this.mc.playerController.isSpectatorMode()) {
                if (!this.capabilities.isFlying) {
                    this.capabilities.isFlying = true;
                    this.sendPlayerAbilities();
                }
            } else if (!flag && this.movementInput.jump && !flag3) {
                if (this.flyToggleTimer == 0) {
                    this.flyToggleTimer = 7;
                } else {
                    this.capabilities.isFlying = !this.capabilities.isFlying;
                    this.sendPlayerAbilities();
                    this.flyToggleTimer = 0;
                }
            }
        }

        if (this.movementInput.jump && !flag && !this.onGround && this.motionY < 0.0D && !this.isElytraFlying() && !this.capabilities.isFlying) {
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isBroken(itemstack)) {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_FALL_FLYING));
            }
        }

        this.wasFallFlying = this.isElytraFlying();

        if (this.capabilities.isFlying && this.isCurrentViewEntity()) {
            if (this.movementInput.sneak) {
                this.movementInput.moveStrafe = (float) ((double) this.movementInput.moveStrafe / 0.3D);
                this.movementInput.moveForward = (float) ((double) this.movementInput.moveForward / 0.3D);
                this.motionY -= this.capabilities.getFlySpeed() * 3.0F;
            }

            if (this.movementInput.jump) {
                this.motionY += this.capabilities.getFlySpeed() * 3.0F;
            }
        }

        if (this.isRidingHorse()) {
            IJumpingMount ijumpingmount = (IJumpingMount) this.getRidingEntity();

            if (this.horseJumpPowerCounter < 0) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter == 0) {
                    this.horseJumpPower = 0.0F;
                }
            }

            if (flag && !this.movementInput.jump) {
                this.horseJumpPowerCounter = -10;
                ijumpingmount.setJumpPower(MathHelper.floor(this.getHorseJumpPower() * 100.0F));
                this.sendHorseJump();
            } else if (!flag && this.movementInput.jump) {
                this.horseJumpPowerCounter = 0;
                this.horseJumpPower = 0.0F;
            } else if (flag) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter < 10) {
                    this.horseJumpPower = (float) this.horseJumpPowerCounter * 0.1F;
                } else {
                    this.horseJumpPower = 0.8F + 2.0F / (float) (this.horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        } else {
            this.horseJumpPower = 0.0F;
        }

        super.onLivingUpdate();

        if (this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
            this.capabilities.isFlying = false;
            this.sendPlayerAbilities();
        }
    }

    /**
     * Handles updating while being ridden by an entity
     */
    public void updateRidden() {
        super.updateRidden();
        this.rowingBoat = false;

        if (this.getRidingEntity() instanceof EntityBoat) {
            EntityBoat entityboat = (EntityBoat) this.getRidingEntity();
            entityboat.updateInputs(this.movementInput.leftKeyDown, this.movementInput.rightKeyDown, this.movementInput.forwardKeyDown, this.movementInput.backKeyDown);
            this.rowingBoat |= this.movementInput.leftKeyDown || this.movementInput.rightKeyDown || this.movementInput.forwardKeyDown || this.movementInput.backKeyDown;
        }
    }

    public boolean isRowingBoat() {
        return this.rowingBoat;
    }

    @Nullable

    /**
     * Removes the given potion effect from the active potion map and returns it. Does not call cleanup callbacks for
     * the end of the potion effect.
     */
    public PotionEffect removeActivePotionEffect(@Nullable Potion potioneffectin) {
        if (potioneffectin == MobEffects.NAUSEA) {
            this.prevTimeInPortal = 0.0F;
            this.timeInPortal = 0.0F;
        }

        return super.removeActivePotionEffect(potioneffectin);
    }

    /**
     * Tries to move the entity towards the specified location.
     */
    public void moveEntity(MoverType moverType, double x, double y, double z) {
        double d0 = this.posX;
        double d1 = this.posZ;
        
        super.moveEntity(moverType, x, y, z);
        this.updateAutoJump((float) (this.posX - d0), (float) (this.posZ - d1));
    }

    public boolean isAutoJumpEnabled() {
        return this.autoJumpEnabled;
    }

    protected void updateAutoJump(float p_189810_1_, float p_189810_2_) {
        if (this.isAutoJumpEnabled()) {
            if (this.autoJumpTime <= 0 && this.onGround && !this.isSneaking() && !this.isRiding()) {
                Vec2f vec2f = this.movementInput.getMoveVector();

                if (vec2f.x != 0.0F || vec2f.y != 0.0F) {
                    Vec3d vec3d = new Vec3d(this.posX, this.getEntityBoundingBox().minY, this.posZ);
                    double d0 = this.posX + (double) p_189810_1_;
                    double d1 = this.posZ + (double) p_189810_2_;
                    Vec3d vec3d1 = new Vec3d(d0, this.getEntityBoundingBox().minY, d1);
                    Vec3d vec3d2 = new Vec3d(p_189810_1_, 0.0D, p_189810_2_);
                    float f = this.getAIMoveSpeed();
                    float f1 = (float) vec3d2.lengthSquared();

                    if (f1 <= 0.001F) {
                        float f2 = f * vec2f.x;
                        float f3 = f * vec2f.y;
                        float f4 = MathHelper.sin(this.rotationYaw * 0.017453292F);
                        float f5 = MathHelper.cos(this.rotationYaw * 0.017453292F);
                        vec3d2 = new Vec3d(f2 * f5 - f3 * f4, vec3d2.yCoord, f3 * f5 + f2 * f4);
                        f1 = (float) vec3d2.lengthSquared();

                        if (f1 <= 0.001F) {
                            return;
                        }
                    }

                    float f12 = (float) MathHelper.fastInvSqrt(f1);
                    Vec3d vec3d12 = vec3d2.scale(f12);
                    Vec3d vec3d13 = this.getForward();
                    float f13 = (float) (vec3d13.xCoord * vec3d12.xCoord + vec3d13.zCoord * vec3d12.zCoord);

                    if (f13 >= -0.15F) {
                        BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().maxY, this.posZ);
                        IBlockState iblockstate = this.world.getBlockState(blockpos);

                        if (iblockstate.getCollisionBoundingBox(this.world, blockpos) == null) {
                            blockpos = blockpos.up();
                            IBlockState iblockstate1 = this.world.getBlockState(blockpos);

                            if (iblockstate1.getCollisionBoundingBox(this.world, blockpos) == null) {
                                float f6 = 7.0F;
                                float f7 = 1.2F;

                                if (this.isPotionActive(MobEffects.JUMP_BOOST)) {
                                    f7 += (float) (this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.75F;
                                }

                                float f8 = Math.max(f * 7.0F, 1.0F / f12);
                                Vec3d vec3d4 = vec3d1.add(vec3d12.scale(f8));
                                float f9 = this.width;
                                float f10 = this.height;
                                AxisAlignedBB axisalignedbb = (new AxisAlignedBB(vec3d, vec3d4.addVector(0.0D, f10, 0.0D))).expand(f9, 0.0D, f9);
                                Vec3d lvt_19_1_ = vec3d.addVector(0.0D, 0.5099999904632568D, 0.0D);
                                vec3d4 = vec3d4.addVector(0.0D, 0.5099999904632568D, 0.0D);
                                Vec3d vec3d5 = vec3d12.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
                                Vec3d vec3d6 = vec3d5.scale(f9 * 0.5F);
                                Vec3d vec3d7 = lvt_19_1_.subtract(vec3d6);
                                Vec3d vec3d8 = vec3d4.subtract(vec3d6);
                                Vec3d vec3d9 = lvt_19_1_.add(vec3d6);
                                Vec3d vec3d10 = vec3d4.add(vec3d6);
                                List<AxisAlignedBB> list = this.world.getCollisionBoxes(this, axisalignedbb);

                                if (!list.isEmpty()) {
                                }

                                float f11 = Float.MIN_VALUE;
                                label86:

                                for (AxisAlignedBB axisalignedbb2 : list) {
                                    if (axisalignedbb2.intersects(vec3d7, vec3d8) || axisalignedbb2.intersects(vec3d9, vec3d10)) {
                                        f11 = (float) axisalignedbb2.maxY;
                                        Vec3d vec3d11 = axisalignedbb2.getCenter();
                                        BlockPos blockpos1 = new BlockPos(vec3d11);
                                        int i = 1;

                                        while (true) {
                                            if ((float) i >= f7) {
                                                break label86;
                                            }

                                            BlockPos blockpos2 = blockpos1.up(i);
                                            IBlockState iblockstate2 = this.world.getBlockState(blockpos2);
                                            AxisAlignedBB axisalignedbb1;

                                            if ((axisalignedbb1 = iblockstate2.getCollisionBoundingBox(this.world, blockpos2)) != null) {
                                                f11 = (float) axisalignedbb1.maxY + (float) blockpos2.getY();

                                                if ((double) f11 - this.getEntityBoundingBox().minY > (double) f7) {
                                                    return;
                                                }
                                            }

                                            if (i > 1) {
                                                blockpos = blockpos.up();
                                                IBlockState iblockstate3 = this.world.getBlockState(blockpos);

                                                if (iblockstate3.getCollisionBoundingBox(this.world, blockpos) != null) {
                                                    return;
                                                }
                                            }

                                            ++i;
                                        }
                                    }
                                }

                                if (f11 != Float.MIN_VALUE) {
                                    float f14 = (float) ((double) f11 - this.getEntityBoundingBox().minY);

                                    if (f14 > 0.5F && f14 <= f7) {
                                        this.autoJumpTime = 1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
