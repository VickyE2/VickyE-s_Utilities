package org.vicky.forge.forgeplatform.datagen;

import com.mojang.logging.LogUtils;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.vicky.forge.VickyUtilitiesForge;
import org.vicky.platform.items.ItemDescriptor;
import org.vicky.platform.items.ItemModel;
import org.vicky.platform.utils.ResourceLocation;

import java.util.Map;

public final class DescriptorItemModelProvider
        extends ItemModelProvider {

    public DescriptorItemModelProvider(
            PackOutput output,
            String modId,
            ExistingFileHelper existingFileHelper
    ) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (Map.Entry<ItemDescriptor, ResourceLocation> entry :
                VickyUtilitiesForge.FACTORY.getDescriptorIds().entrySet()) {

            ItemDescriptor descriptor = entry.getKey();
            ResourceLocation id = entry.getValue();

            generate(id, descriptor);
        }
    }

    private void generate(
            ResourceLocation id,
            ItemDescriptor descriptor
    ) {
        ItemModel model = descriptor.getModel();

        if (model == null)
            return;

        LogUtils.getLogger().info("Generating ItemModel for {}", id);

        if (model instanceof ItemModel.ItemModelDefinition definition) {
            generateDefinition(id, definition);
            return;
        }

        if (model instanceof ItemModel.MinecraftItemModel minecraft) {
            // generateMinecraft(id, minecraft);
        }
    }

    private void generateDefinition(
            ResourceLocation id,
            ItemModel.ItemModelDefinition definition
    ) {
        ModelBuilder<?> builder =
                getBuilder(id.getPath())
                        .parent(
                                new ModelFile.UncheckedModelFile(
                                        definition.getParent()
                                )
                        );

        definition.getTextures().forEach(
                (key, texture) ->
                        builder.texture(
                                key,
                                texture.toString()
                        )
        );
    }
}