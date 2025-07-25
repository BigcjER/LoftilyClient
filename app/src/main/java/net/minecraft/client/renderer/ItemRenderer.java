package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import loftily.Client;
import loftily.event.impl.render.RenderFireEvent;
import loftily.module.impl.combat.KillAura;
import loftily.module.impl.render.AnimationModule;
import loftily.module.impl.render.ViewModel;
import loftily.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.MapData;
import optifine.Config;
import optifine.DynamicLights;
import optifine.Reflector;
import optifine.ReflectorForge;
import shadersmod.client.Shaders;

import java.util.Objects;

public class ItemRenderer {
    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");
    
    /**
     * A reference to the Minecraft object.
     */
    private final Minecraft mc;
    private ItemStack itemStackMainHand = ItemStack.field_190927_a;
    private ItemStack itemStackOffHand = ItemStack.field_190927_a;
    private float equippedProgressMainHand;
    private float prevEquippedProgressMainHand;
    private float equippedProgressOffHand;
    private float prevEquippedProgressOffHand;
    private final RenderManager renderManager;
    private final RenderItem itemRenderer;
    
    public ItemRenderer(Minecraft mcIn) {
        this.mc = mcIn;
        this.renderManager = mcIn.getRenderManager();
        this.itemRenderer = mcIn.getRenderItem();
    }
    
    public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform) {
        this.renderItemSide(entityIn, heldStack, transform, false);
    }
    
    public void renderItemSide(EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded) {
        if (!heldStack.isEmptyStack()) {
            Item item = heldStack.getItem();
            Block block = Block.getBlockFromItem(item);
            GlStateManager.pushMatrix();
            boolean flag = this.itemRenderer.shouldRenderItemIn3D(heldStack) && block.getBlockLayer() == BlockRenderLayer.TRANSLUCENT;
            
            if (flag && (!Config.isShaders() || !Shaders.renderItemKeepDepthMask)) {
                GlStateManager.depthMask(false);
            }
            
            this.itemRenderer.renderItem(heldStack, entitylivingbaseIn, transform, leftHanded);
            
            if (flag) {
                GlStateManager.depthMask(true);
            }
            
            GlStateManager.popMatrix();
        }
    }
    
    /**
     * Rotate the render around X and Y
     */
    private void rotateArroundXAndY(float angle, float angleY) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(angleY, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
    
    private void setLightmap() {
        AbstractClientPlayer abstractclientplayer = this.mc.player;
        int i = this.mc.world.getCombinedLight(new BlockPos(abstractclientplayer.posX, abstractclientplayer.posY + abstractclientplayer.getEyeHeight(), abstractclientplayer.posZ), 0);
        
        if (Config.isDynamicLights()) {
            i = DynamicLights.getCombinedLight(this.mc.getRenderViewEntity(), i);
        }
        
        float f = (float) (i & 65535);
        float f1 = (float) (i >> 16);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
    }
    
    private void rotateArm(float p_187458_1_) {
        EntityPlayerSP entityplayersp = this.mc.player;
        float f = entityplayersp.prevRenderArmPitch + (entityplayersp.renderArmPitch - entityplayersp.prevRenderArmPitch) * p_187458_1_;
        float f1 = entityplayersp.prevRenderArmYaw + (entityplayersp.renderArmYaw - entityplayersp.prevRenderArmYaw) * p_187458_1_;
        GlStateManager.rotate((entityplayersp.rotationPitch - f) * 0.1F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((entityplayersp.rotationYaw - f1) * 0.1F, 0.0F, 1.0F, 0.0F);
    }
    
    /**
     * Return the angle to render the Map
     */
    private float getMapAngleFromPitch(float pitch) {
        float f = 1.0F - pitch / 45.0F + 0.1F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        f = -MathHelper.cos(f * (float) Math.PI) * 0.5F + 0.5F;
        return f;
    }
    
    private void renderArms() {
        if (!this.mc.player.isInvisible()) {
            GlStateManager.disableCull();
            GlStateManager.pushMatrix();
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            this.renderArm(EnumHandSide.RIGHT);
            this.renderArm(EnumHandSide.LEFT);
            GlStateManager.popMatrix();
            GlStateManager.enableCull();
        }
    }
    
    private void renderArm(EnumHandSide p_187455_1_) {
        this.mc.getTextureManager().bindTexture(this.mc.player.getLocationSkin());
        Render<AbstractClientPlayer> render = this.renderManager.getEntityRenderObject(this.mc.player);
        RenderPlayer renderplayer = (RenderPlayer) render;
        GlStateManager.pushMatrix();
        float f = p_187455_1_ == EnumHandSide.RIGHT ? 1.0F : -1.0F;
        GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f * -41.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(f * 0.3F, -1.1F, 0.45F);
        
        if (p_187455_1_ == EnumHandSide.RIGHT) {
            renderplayer.renderRightArm(this.mc.player);
        } else {
            renderplayer.renderLeftArm(this.mc.player);
        }
        
        GlStateManager.popMatrix();
    }
    
    private void renderMapFirstPersonSide(float p_187465_1_, EnumHandSide p_187465_2_, float p_187465_3_, ItemStack p_187465_4_) {
        float f = p_187465_2_ == EnumHandSide.RIGHT ? 1.0F : -1.0F;
        GlStateManager.translate(f * 0.125F, -0.125F, 0.0F);
        
        if (!this.mc.player.isInvisible()) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(f * 10.0F, 0.0F, 0.0F, 1.0F);
            this.renderArmFirstPerson(p_187465_1_, p_187465_3_, p_187465_2_);
            GlStateManager.popMatrix();
        }
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(f * 0.51F, -0.08F + p_187465_1_ * -1.2F, -0.75F);
        float f1 = MathHelper.sqrt(p_187465_3_);
        float f2 = MathHelper.sin(f1 * (float) Math.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * MathHelper.sin(f1 * ((float) Math.PI * 2F));
        float f5 = -0.3F * MathHelper.sin(p_187465_3_ * (float) Math.PI);
        GlStateManager.translate(f * f3, f4 - 0.3F * f2, f5);
        GlStateManager.rotate(f2 * -45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f * f2 * -30.0F, 0.0F, 1.0F, 0.0F);
        this.renderMapFirstPerson(p_187465_4_);
        GlStateManager.popMatrix();
    }
    
    private void renderMapFirstPerson(float p_187463_1_, float p_187463_2_, float p_187463_3_) {
        float f = MathHelper.sqrt(p_187463_3_);
        float f1 = -0.2F * MathHelper.sin(p_187463_3_ * (float) Math.PI);
        float f2 = -0.4F * MathHelper.sin(f * (float) Math.PI);
        GlStateManager.translate(0.0F, -f1 / 2.0F, f2);
        float f3 = this.getMapAngleFromPitch(p_187463_1_);
        GlStateManager.translate(0.0F, 0.04F + p_187463_2_ * -1.2F + f3 * -0.5F, -0.72F);
        GlStateManager.rotate(f3 * -85.0F, 1.0F, 0.0F, 0.0F);
        this.renderArms();
        float f4 = MathHelper.sin(f * (float) Math.PI);
        GlStateManager.rotate(f4 * 20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        this.renderMapFirstPerson(this.itemStackMainHand);
    }
    
    private void renderMapFirstPerson(ItemStack stack) {
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.38F, 0.38F, 0.38F);
        GlStateManager.disableLighting();
        this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.translate(-0.5F, -0.5F, 0.0F);
        GlStateManager.scale(0.0078125F, 0.0078125F, 0.0078125F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        MapData mapdata = ReflectorForge.getMapData(Items.FILLED_MAP, stack, this.mc.world);
        
        if (mapdata != null) {
            this.mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
        }
        
        GlStateManager.enableLighting();
    }
    
    private void renderArmFirstPerson(float p_187456_1_, float p_187456_2_, EnumHandSide p_187456_3_) {
        boolean flag = p_187456_3_ != EnumHandSide.LEFT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = MathHelper.sqrt(p_187456_2_);
        float f2 = -0.3F * MathHelper.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * MathHelper.sin(f1 * ((float) Math.PI * 2F));
        float f4 = -0.4F * MathHelper.sin(p_187456_2_ * (float) Math.PI);
        GlStateManager.translate(f * (f2 + 0.64000005F), f3 - 0.6F + p_187456_1_ * -0.6F, f4 - 0.71999997F);
        GlStateManager.rotate(f * 45.0F, 0.0F, 1.0F, 0.0F);
        float f5 = MathHelper.sin(p_187456_2_ * p_187456_2_ * (float) Math.PI);
        float f6 = MathHelper.sin(f1 * (float) Math.PI);
        GlStateManager.rotate(f * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f * f5 * -20.0F, 0.0F, 0.0F, 1.0F);
        AbstractClientPlayer abstractclientplayer = this.mc.player;
        this.mc.getTextureManager().bindTexture(abstractclientplayer.getLocationSkin());
        GlStateManager.translate(f * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(f * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(f * 5.6F, 0.0F, 0.0F);
        RenderPlayer renderplayer = (RenderPlayer) this.renderManager.<AbstractClientPlayer>getEntityRenderObject(abstractclientplayer);
        GlStateManager.disableCull();
        
        if (flag) {
            renderplayer.renderRightArm(abstractclientplayer);
        } else {
            renderplayer.renderLeftArm(abstractclientplayer);
        }
        
        GlStateManager.enableCull();
    }
    
    private void transformEatFirstPerson(float p_187454_1_, EnumHandSide p_187454_2_, ItemStack p_187454_3_) {
        float f = (float) this.mc.player.getItemInUseCount() - p_187454_1_ + 1.0F;
        float f1 = f / (float) p_187454_3_.getMaxItemUseDuration();
        
        if (f1 < 0.8F) {
            float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * (float) Math.PI) * 0.1F);
            GlStateManager.translate(0.0F, f2, 0.0F);
        }
        
        float f3 = 1.0F - (float) Math.pow(f1, 27.0D);
        int i = p_187454_2_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(f3 * 0.6F * (float) i, f3 * -0.5F, f3 * 0.0F);
        GlStateManager.rotate((float) i * f3 * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((float) i * f3 * 30.0F, 0.0F, 0.0F, 1.0F);
    }
    
    private void transformFirstPerson(EnumHandSide p_187453_1_, float p_187453_2_) {
        int i = p_187453_1_ == EnumHandSide.RIGHT ? 1 : -1;
        float f = MathHelper.sin(p_187453_2_ * p_187453_2_ * (float) Math.PI);
        GlStateManager.rotate((float) i * (45.0F + f * -20.0F), 0.0F, 1.0F, 0.0F);
        float f1 = MathHelper.sin(MathHelper.sqrt(p_187453_2_) * (float) Math.PI);
        GlStateManager.rotate((float) i * f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((float) i * -45.0F, 0.0F, 1.0F, 0.0F);
    }
    
    private void transformSideFirstPerson(EnumHandSide p_187459_1_, float p_187459_2_) {
        int i = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        boolean swing1_8 = AnimationModule.getInstance().getSwingAnimation1_8().getValue();
        GlStateManager.translate((float) i * 0.56F, -0.52F + (p_187459_2_ * (swing1_8 && mc.player.getHeldItemMainhand().getItem() instanceof ItemSword ? 0 : -0.6F)), -0.72F);
    }
    
    /**
     * Renders the active item in the player's hand when in first person mode.
     */
    public void renderItemInFirstPerson(float partialTicks) {
        AbstractClientPlayer abstractclientplayer = this.mc.player;
        float f = abstractclientplayer.getSwingProgress(partialTicks);
        EnumHand enumhand = MoreObjects.firstNonNull(abstractclientplayer.swingingHand, EnumHand.MAIN_HAND);
        float f1 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
        float f2 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
        boolean flag = true;
        boolean flag1 = true;
        
        if (abstractclientplayer.isHandActive()) {
            ItemStack itemstack = abstractclientplayer.getActiveItemStack();
            
            if (!itemstack.isEmptyStack() && itemstack.getItem() == Items.BOW) {
                EnumHand enumhand1 = abstractclientplayer.getActiveHand();
                flag = enumhand1 == EnumHand.MAIN_HAND;
                flag1 = !flag;
            }
        }
        
        this.rotateArroundXAndY(f1, f2);
        this.setLightmap();
        this.rotateArm(partialTicks);
        GlStateManager.enableRescaleNormal();
        
        if (flag) {
            float f3 = enumhand == EnumHand.MAIN_HAND ? f : 0.0F;
            float f5 = 1.0F - (this.prevEquippedProgressMainHand + (this.equippedProgressMainHand - this.prevEquippedProgressMainHand) * partialTicks);
            
            if (!Reflector.ForgeHooksClient_renderSpecificFirstPersonHand.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_renderSpecificFirstPersonHand, EnumHand.MAIN_HAND, partialTicks, f1, f3, f5, this.itemStackMainHand)) {
                this.renderItemInFirstPerson(abstractclientplayer, partialTicks, f1, EnumHand.MAIN_HAND, f3, this.itemStackMainHand, f5);
            }
        }
        
        if (flag1) {
            float f4 = enumhand == EnumHand.OFF_HAND ? f : 0.0F;
            float f6 = 1.0F - (this.prevEquippedProgressOffHand + (this.equippedProgressOffHand - this.prevEquippedProgressOffHand) * partialTicks);
            
            if (!Reflector.ForgeHooksClient_renderSpecificFirstPersonHand.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_renderSpecificFirstPersonHand, EnumHand.OFF_HAND, partialTicks, f1, f4, f6, this.itemStackOffHand)) {
                this.renderItemInFirstPerson(abstractclientplayer, partialTicks, f1, EnumHand.OFF_HAND, f4, this.itemStackOffHand, f6);
            }
        }
        
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    private void transformFirstPersonItem(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.42F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float f1 = MathHelper.sin((float) (Math.sqrt(swingProgress) * 3.1415927F));
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(1.1F, 1.1F, 1.1F);
    }
    
    public void renderItemInFirstPerson(AbstractClientPlayer abstractClientPlayer, float partialTicks, float pitch, EnumHand hand, float swingProgress, ItemStack stack, float equippedProgress) {
        boolean isSwordBlocking = PlayerUtils.isBlocking(abstractClientPlayer);
        
        boolean hasShield = abstractClientPlayer.getHeldItemOffhand().getItem() instanceof ItemShield;
        boolean hasSword = abstractClientPlayer.getHeldItemMainhand().getItem() instanceof ItemSword;
        boolean blockAnimation = AnimationModule.getInstance().getBlockAnimation().getValue();
        
        //if ((hasShield && hasSword) && blockAnimation) return;
        if (hand == EnumHand.OFF_HAND && ((hasSword && hasShield) || PlayerUtils.isBlocking()) && blockAnimation) return;
        if (Config.isShaders() && Shaders.isSkipRenderHand(hand)) return;
        
        boolean viewModelToggled = ViewModel.getInstance().isToggled();
        boolean isMainHand = hand == EnumHand.MAIN_HAND;
        
        EnumHandSide enumHandSide = isMainHand ? abstractClientPlayer.getPrimaryHand() : abstractClientPlayer.getPrimaryHand().opposite();
        GlStateManager.pushMatrix();
        
        if (stack.isEmptyStack()) {
            if (isMainHand && !abstractClientPlayer.isInvisible()) {
                this.renderArmFirstPerson(equippedProgress, swingProgress, enumHandSide);
            }
        } else if (stack.getItem() instanceof ItemMap) {
            if (isMainHand && this.itemStackOffHand.isEmptyStack()) {
                this.renderMapFirstPerson(pitch, equippedProgress, swingProgress);
            } else {
                this.renderMapFirstPersonSide(equippedProgress, enumHandSide, swingProgress, stack);
            }
        } else {
            if (blockAnimation && (isMainHand && (isSwordBlocking || (hasSword && mc.player.isHandActive()) || Client.INSTANCE.getModuleManager().get(KillAura.class).isBlockingStatus()))) {
                //BlockAnimation
                
                float f = 1.0F - (this.prevEquippedProgressMainHand + (this.equippedProgressMainHand - this.prevEquippedProgressMainHand) * partialTicks);
                switch (AnimationModule.getInstance().getBlockAnimationMode().getValueByName()){
                    case "1.7Like":
                        transformFirstPersonItem(f, swingProgress);
                        GlStateManager.rotate(-90F, 15F, -0.3F,2F);
                        break;
                    case "None":
                        transformFirstPersonItem(f, 0);
                        GlStateManager.rotate(-85F, 160F, -40F,20F);
                        break;
                }
                
                if (viewModelToggled) {
                    double scale = ViewModel.getInstance().getItemMainHandScale().getValue();
                    
                    GlStateManager.translate(
                            ViewModel.getInstance().getItemMainHandPosXOffset().getValue(),
                            ViewModel.getInstance().getItemMainHandPosYOffset().getValue(),
                            ViewModel.getInstance().getItemMainHandPosZOffset().getValue());
                    
                    GlStateManager.scale(scale, scale, scale);
                }
                
                this.renderItemSide(abstractClientPlayer, stack, ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, false);
            } else {
                boolean isRightHand = enumHandSide == EnumHandSide.RIGHT;
                
                if (abstractClientPlayer.isHandActive() && abstractClientPlayer.getItemInUseCount() > 0 && abstractClientPlayer.getActiveHand() == hand) {
                    int handMultiplier = isRightHand ? 1 : -1;
                    
                    switch (stack.getItemUseAction()) {
                        case NONE:
                        
                        case BLOCK:
                            this.transformSideFirstPerson(enumHandSide, equippedProgress);
                            break;
                        
                        case EAT:
                        case DRINK:
                            this.transformEatFirstPerson(partialTicks, enumHandSide, stack);
                            this.transformSideFirstPerson(enumHandSide, equippedProgress);
                            break;
                        
                        case BOW:
                            this.transformSideFirstPerson(enumHandSide, equippedProgress);
                            GlStateManager.translate((float) handMultiplier * -0.2785682F, 0.18344387F, 0.15731531F);
                            GlStateManager.rotate(-13.935F, 1.0F, 0.0F, 0.0F);
                            GlStateManager.rotate((float) handMultiplier * 35.3F, 0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate((float) handMultiplier * -9.785F, 0.0F, 0.0F, 1.0F);
                            float f5 = (float) stack.getMaxItemUseDuration() - ((float) this.mc.player.getItemInUseCount() - partialTicks + 1.0F);
                            float f6 = f5 / 20.0F;
                            f6 = (f6 * f6 + f6 * 2.0F) / 3.0F;
                            
                            if (f6 > 1.0F) {
                                f6 = 1.0F;
                            }
                            
                            if (f6 > 0.1F) {
                                float f7 = MathHelper.sin((f5 - 0.1F) * 1.3F);
                                float f3 = f6 - 0.1F;
                                float f4 = f7 * f3;
                                GlStateManager.translate(f4 * 0.0F, f4 * 0.004F, f4 * 0.0F);
                            }
                            
                            GlStateManager.translate(f6 * 0.0F, f6 * 0.0F, f6 * 0.04F);
                            GlStateManager.scale(1.0F, 1.0F, 1.0F + f6 * 0.2F);
                            GlStateManager.rotate((float) handMultiplier * 45.0F, 0.0F, -1.0F, 0.0F);
                    }
                } else {
                    float f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI * 2.0F);
                    float f2 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
                    
                    
                    //int i = isRightHand ? 1 : -1;
                    GlStateManager.translate(f, f1, f2);
                    this.transformSideFirstPerson(enumHandSide, equippedProgress);
                    this.transformFirstPerson(enumHandSide, swingProgress);
                }
                
                if (viewModelToggled) {
                    
                    switch (hand) {
                        case MAIN_HAND: {
                            double scale = ViewModel.getInstance().getItemMainHandScale().getValue();
                            
                            GlStateManager.translate(
                                    ViewModel.getInstance().getItemMainHandPosXOffset().getValue(),
                                    ViewModel.getInstance().getItemMainHandPosYOffset().getValue(),
                                    ViewModel.getInstance().getItemMainHandPosZOffset().getValue());
                            
                            GlStateManager.scale(scale, scale, scale);
                            
                            break;
                        }
                        case OFF_HAND: {
                            double scale = ViewModel.getInstance().getItemOffHandScale().getValue();
                            
                            GlStateManager.translate(
                                    ViewModel.getInstance().getItemOffHandPosXOffset().getValue(),
                                    ViewModel.getInstance().getItemOffHandPosYOffset().getValue(),
                                    ViewModel.getInstance().getItemOffHandPosZOffset().getValue());
                            
                            GlStateManager.scale(scale, scale, scale);
                            
                            break;
                        }
                    }
                }
                this.renderItemSide(abstractClientPlayer, stack,
                        isRightHand ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                                : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !isRightHand);
            }
        }
        
        GlStateManager.popMatrix();
    }
    
    /**
     * Renders the overlays.
     */
    public void renderOverlays(float partialTicks) {
        GlStateManager.disableAlpha();
        
        if (this.mc.player.isEntityInsideOpaqueBlock()) {
            IBlockState iblockstate = this.mc.world.getBlockState(new BlockPos(this.mc.player));
            BlockPos blockpos = new BlockPos(this.mc.player);
            EntityPlayer entityplayer = this.mc.player;
            
            for (int i = 0; i < 8; ++i) {
                double d0 = entityplayer.posX + (((float) ((i) % 2) - 0.5F) * entityplayer.width * 0.8F);
                double d1 = entityplayer.posY + (((float) ((i >> 1) % 2) - 0.5F) * 0.1F);
                double d2 = entityplayer.posZ + (((float) ((i >> 2) % 2) - 0.5F) * entityplayer.width * 0.8F);
                BlockPos blockpos1 = new BlockPos(d0, d1 + entityplayer.getEyeHeight(), d2);
                IBlockState iblockstate1 = this.mc.world.getBlockState(blockpos1);
                
                if (iblockstate1.func_191058_s()) {
                    iblockstate = iblockstate1;
                    blockpos = blockpos1;
                }
            }
            
            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
                Object object = Reflector.getFieldValue(Reflector.RenderBlockOverlayEvent_OverlayType_BLOCK);
                
                if (!Reflector.callBoolean(Reflector.ForgeEventFactory_renderBlockOverlay, this.mc.player, partialTicks, object, iblockstate, blockpos)) {
                    this.renderBlockInHand(this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(iblockstate));
                }
            }
        }
        
        if (!this.mc.player.isSpectator()) {
            if (this.mc.player.isInsideOfMaterial(Material.WATER) && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderWaterOverlay, this.mc.player, partialTicks)) {
                this.renderWaterOverlayTexture(partialTicks);
            }
            
            if (this.mc.player.isBurning() && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderFireOverlay, this.mc.player, partialTicks)) {
                this.renderFireInFirstPerson();
            }
        }
        
        GlStateManager.enableAlpha();
    }
    
    /**
     * Render the block in the player's hand
     */
    private void renderBlockInHand(TextureAtlasSprite partialTicks) {
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(0.1F, 0.1F, 0.1F, 0.5F);
        GlStateManager.pushMatrix();
        float f6 = partialTicks.getMinU();
        float f7 = partialTicks.getMaxU();
        float f8 = partialTicks.getMinV();
        float f9 = partialTicks.getMaxV();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-1.0D, -1.0D, -0.5D).tex(f7, f9).endVertex();
        bufferbuilder.pos(1.0D, -1.0D, -0.5D).tex(f6, f9).endVertex();
        bufferbuilder.pos(1.0D, 1.0D, -0.5D).tex(f6, f8).endVertex();
        bufferbuilder.pos(-1.0D, 1.0D, -0.5D).tex(f7, f8).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    /**
     * Renders a texture that warps around based on the direction the player is looking. Texture needs to be bound
     * before being called. Used for the water overlay.
     */
    private void renderWaterOverlayTexture(float ignoredPartialTicks) {
        if (!Config.isShaders() || Shaders.isUnderwaterOverlay()) {
            this.mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            float f = this.mc.player.getBrightness();
            GlStateManager.color(f, f, f, 0.5F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            float f7 = -this.mc.player.rotationYaw / 64.0F;
            float f8 = this.mc.player.rotationPitch / 64.0F;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(-1.0D, -1.0D, -0.5D).tex((4.0F + f7), (4.0F + f8)).endVertex();
            bufferbuilder.pos(1.0D, -1.0D, -0.5D).tex((0.0F + f7), (4.0F + f8)).endVertex();
            bufferbuilder.pos(1.0D, 1.0D, -0.5D).tex((0.0F + f7), (0.0F + f8)).endVertex();
            bufferbuilder.pos(-1.0D, 1.0D, -0.5D).tex((4.0F + f7), (0.0F + f8)).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
        }
    }
    
    /**
     * Renders the fire on the screen for first person mode. Arg: partialTickTime
     */
    private void renderFireInFirstPerson() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
        GlStateManager.depthFunc(519);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        for (int i = 0; i < 2; ++i) {
            GlStateManager.pushMatrix();
            TextureAtlasSprite textureatlassprite = this.mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
            this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            float f1 = textureatlassprite.getMinU();
            float f2 = textureatlassprite.getMaxU();
            float f3 = textureatlassprite.getMinV();
            float f4 = textureatlassprite.getMaxV();
            
            float translateY = -0.3F;
            RenderFireEvent event = new RenderFireEvent(translateY);
            Client.INSTANCE.getEventManager().call(event);
            translateY = event.getTranslateY();
            
            GlStateManager.translate((float) (-(i * 2 - 1)) * 0.24F, translateY, 0.0F);
            GlStateManager.rotate((float) (i * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
            
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(-0.5D, -0.5D, -0.5D).tex(f2, f4).endVertex();
            bufferbuilder.pos(0.5D, -0.5D, -0.5D).tex(f1, f4).endVertex();
            bufferbuilder.pos(0.5D, 0.5D, -0.5D).tex(f1, f3).endVertex();
            bufferbuilder.pos(-0.5D, 0.5D, -0.5D).tex(f2, f3).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
    }
    
    public void updateEquippedItem() {
        this.prevEquippedProgressMainHand = this.equippedProgressMainHand;
        this.prevEquippedProgressOffHand = this.equippedProgressOffHand;
        EntityPlayerSP entityplayersp = this.mc.player;
        ItemStack itemstack = entityplayersp.getHeldItemMainhand();
        ItemStack itemstack1 = entityplayersp.getHeldItemOffhand();
        
        if (entityplayersp.isRowingBoat()) {
            this.equippedProgressMainHand = MathHelper.clamp(this.equippedProgressMainHand - 0.4F, 0.0F, 1.0F);
            this.equippedProgressOffHand = MathHelper.clamp(this.equippedProgressOffHand - 0.4F, 0.0F, 1.0F);
        } else {
            float f = entityplayersp.getCooledAttackStrength(1.0F);
            
            if (Reflector.ForgeHooksClient_shouldCauseReequipAnimation.exists()) {
                boolean flag = Reflector.callBoolean(Reflector.ForgeHooksClient_shouldCauseReequipAnimation, this.itemStackMainHand, itemstack, entityplayersp.inventory.currentItem);
                boolean flag1 = Reflector.callBoolean(Reflector.ForgeHooksClient_shouldCauseReequipAnimation, this.itemStackOffHand, itemstack1, -1);
                
                if (!flag && !Objects.equals(this.itemStackMainHand, itemstack)) {
                    this.itemStackMainHand = itemstack;
                }
                
                if (!flag && !Objects.equals(this.itemStackOffHand, itemstack1)) {
                    this.itemStackOffHand = itemstack1;
                }
                
                this.equippedProgressMainHand += MathHelper.clamp((!flag ? f * f * f : 0.0F) - this.equippedProgressMainHand, -0.4F, 0.4F);
                this.equippedProgressOffHand += MathHelper.clamp((float) (!flag1 ? 1 : 0) - this.equippedProgressOffHand, -0.4F, 0.4F);
            } else {
                this.equippedProgressMainHand += MathHelper.clamp((Objects.equals(this.itemStackMainHand, itemstack) ? f * f * f : 0.0F) - this.equippedProgressMainHand, -0.4F, 0.4F);
                this.equippedProgressOffHand += MathHelper.clamp((float) (Objects.equals(this.itemStackOffHand, itemstack1) ? 1 : 0) - this.equippedProgressOffHand, -0.4F, 0.4F);
            }
        }
        
        if (this.equippedProgressMainHand < 0.1F) {
            this.itemStackMainHand = itemstack;
            
            if (Config.isShaders()) {
                Shaders.setItemToRenderMain(this.itemStackMainHand);
            }
        }
        
        if (this.equippedProgressOffHand < 0.1F) {
            this.itemStackOffHand = itemstack1;
            
            if (Config.isShaders()) {
                Shaders.setItemToRenderOff(this.itemStackOffHand);
            }
        }
    }
    
    public void resetEquippedProgress(EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            this.equippedProgressMainHand = 0.0F;
        } else {
            this.equippedProgressOffHand = 0.0F;
        }
    }
}
