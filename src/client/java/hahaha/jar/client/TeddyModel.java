package hahaha.jar.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;

public class TeddyModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart root;
    private final ModelPart model;
    private final ModelPart arms;

    public TeddyModel(ModelPart root) {
        this.root = root;
        this.model = root.getChild("model");
        this.arms = this.model.getChild("arms");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition model = partDefinition.addOrReplaceChild("model", CubeListBuilder.create().texOffs(0, 8).addBox(-4.0F, -8.0F, 0.0F, 5.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(0, 0).addBox(-6.5F, -14.0F, 1.0F, 10.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 24.0F, 0.0F));

        model.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(18, 8).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(18, 12).addBox(6.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -3.0F, 2.0F));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        this.root.render(poseStack, vertexConsumer, light, overlay, color);
    }
}
