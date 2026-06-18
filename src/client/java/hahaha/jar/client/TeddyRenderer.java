package hahaha.jar.client;

import hahaha.jar.TeddyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TeddyRenderer extends MobRenderer<TeddyEntity, TeddyModel<TeddyEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("hahahajar:textures/entity/teddy.png");

    public TeddyRenderer(EntityRendererProvider.Context context) {
        super(context, new TeddyModel<>(context.bakeLayer(HahahaJarClient.TEDDY_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TeddyEntity entity) {
        return TEXTURE;
    }
}
