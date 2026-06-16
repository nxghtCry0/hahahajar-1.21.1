package hahaha.jar.client;

import hahaha.jar.LaughEchoEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LaughEchoRenderer extends HumanoidMobRenderer<LaughEchoEntity, HumanoidModel<LaughEchoEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("hahahajar:textures/entity/laugh_echo.png");

    public LaughEchoRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(LaughEchoEntity entity) {
        return TEXTURE;
    }
}
