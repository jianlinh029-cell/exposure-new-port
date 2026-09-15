package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.ExposureClient;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PlatformHelperClientImpl {
    private static final Map<Identifier, ExtraModelKey<BlockStateModel>> EXTRA_MODELS = new HashMap<>();

    public static void registerExtraBlockModels(ModelLoadingPlugin.Context context) {
        for (Identifier model : ExposureClient.Models.BLOCK_MODELS) {
            ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(model::toString);
            EXTRA_MODELS.put(model, key);
            context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(model));
        }
    }

    public static BlockStateModel getModel(Identifier model) {
        ExtraModelKey<BlockStateModel> key = EXTRA_MODELS.get(model);
        if (key == null) {
            return Minecraft.getInstance().getModelManager().getMissingBlockStateModel();
        }
        BlockStateModel baked = ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(key);
        // During a manual F3+T/resource reload Fabric can expose the extra-model
        // key before its replacement has finished baking. Never hand a null model
        // to Indigo's block renderer; use vanilla's missing model until the reload completes.
        return baked != null ? baked : Minecraft.getInstance().getModelManager().getMissingBlockStateModel();
    }
}
