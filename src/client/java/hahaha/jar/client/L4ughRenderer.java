package hahaha.jar.client;

import hahaha.jar.L4ughEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.EntityType;

public class L4ughRenderer extends HumanoidMobRenderer<L4ughEntity, HumanoidModel<L4ughEntity>> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.parse("hahahajar:textures/entity/white.png");

    private final CowRenderer cowRenderer;
    private final SheepRenderer sheepRenderer;
    private final PigRenderer pigRenderer;
    private final ChickenRenderer chickenRenderer;
    private final VillagerRenderer villagerRenderer;

    private Cow dummyCow;
    private Sheep dummySheep;
    private Pig dummyPig;
    private Chicken dummyChicken;
    private Villager dummyVillager;

    public L4ughRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.cowRenderer = new CowRenderer(context);
        this.sheepRenderer = new SheepRenderer(context);
        this.pigRenderer = new PigRenderer(context);
        this.chickenRenderer = new ChickenRenderer(context);
        this.villagerRenderer = new VillagerRenderer(context);
    }

    @Override
    public ResourceLocation getTextureLocation(L4ughEntity entity) {
        return WHITE_TEXTURE;
    }

    @Override
    protected void scale(L4ughEntity entity, PoseStack poseStack, float partialTickTime) {
        if (!entity.isDisguised()) {
            poseStack.scale(1.5f, 2.7f, 1.5f);
        }
    }

    @Override
    public void render(L4ughEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isDisguised()) {
            net.minecraft.world.level.Level level = entity.level();
            int type = entity.getDisguiseType();
            if (type == 0) {
                if (dummyCow == null) {
                    dummyCow = new Cow(EntityType.COW, level);
                }
                updateDummy(dummyCow, entity);
                cowRenderer.render(dummyCow, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else if (type == 1) {
                if (dummySheep == null) {
                    dummySheep = new Sheep(EntityType.SHEEP, level);
                }
                updateDummy(dummySheep, entity);
                sheepRenderer.render(dummySheep, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else if (type == 2) {
                if (dummyPig == null) {
                    dummyPig = new Pig(EntityType.PIG, level);
                }
                updateDummy(dummyPig, entity);
                pigRenderer.render(dummyPig, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else if (type == 3) {
                if (dummyChicken == null) {
                    dummyChicken = new Chicken(EntityType.CHICKEN, level);
                }
                updateDummy(dummyChicken, entity);
                chickenRenderer.render(dummyChicken, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else {
                if (dummyVillager == null) {
                    dummyVillager = new Villager(EntityType.VILLAGER, level);
                }
                updateDummy(dummyVillager, entity);
                villagerRenderer.render(dummyVillager, entityYaw, partialTicks, poseStack, buffer, packedLight);
            }
        } else {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }

    private void updateDummy(net.minecraft.world.entity.Mob dummy, L4ughEntity parent) {
        dummy.setPos(parent.getX(), parent.getY(), parent.getZ());
        dummy.xo = parent.xo;
        dummy.yo = parent.yo;
        dummy.zo = parent.zo;
        dummy.setYRot(parent.getYRot());
        dummy.setXRot(parent.getXRot());
        dummy.yRotO = parent.yRotO;
        dummy.xRotO = parent.xRotO;
        dummy.yBodyRot = parent.yBodyRot;
        dummy.yBodyRotO = parent.yBodyRotO;
        dummy.yHeadRot = parent.yHeadRot;
        dummy.yHeadRotO = parent.yHeadRotO;
        dummy.walkAnimation.setSpeed(parent.walkAnimation.speed());
        dummy.walkAnimation.position(parent.walkAnimation.position());
        dummy.setOnGround(parent.onGround());
    }
}
