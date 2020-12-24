package elucent.eidolon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import elucent.eidolon.world.LabStructure;
import elucent.eidolon.world.StrayTowerStructure;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.Dimension;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.IglooStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.template.RuleTest;
import net.minecraft.world.gen.feature.template.TagMatchRuleTest;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldGen {
    static List<ConfiguredFeature<?, ?>> ORES = new ArrayList<>();
    static ConfiguredFeature<?, ?> LEAD_ORE_GEN;
    static DeferredRegister<Structure<?>> STRUCTURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Eidolon.MODID);
    static List<Structure<?>> STRUCTURE_LIST = new ArrayList<>();
    static Map<ResourceLocation, StructureSeparationSettings> STRUCTURE_SETTINGS = new HashMap<>();
    static RuleTest IN_STONE = new TagMatchRuleTest(Tags.Blocks.STONE);


    static IStructurePieceType register(IStructurePieceType type, String name) {
        net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.STRUCTURE_PIECE, new ResourceLocation(Eidolon.MODID, name), type);
        return type;
    }

    static <C extends IFeatureConfig, S extends Structure<C>> StructureFeature<C, S> register(StructureFeature<C, S> feature, String name) {
        WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE, new ResourceLocation(Eidolon.MODID, name), feature);
        return feature;
    }

    static <C extends IFeatureConfig> RegistryObject<Structure<C>> addStructure(String name, Structure<C> structure, GenerationStage.Decoration stage, StructureSeparationSettings settings) {
        Structure.NAME_STRUCTURE_BIMAP.put(Eidolon.MODID + ":" + name, structure);
        Structure.STRUCTURE_DECORATION_STAGE_MAP.put(structure, stage);
        STRUCTURE_LIST.add(structure);
        STRUCTURE_SETTINGS.put(new ResourceLocation(Eidolon.MODID, name), settings);
        if (stage != GenerationStage.Decoration.UNDERGROUND_STRUCTURES) {
            Structure.field_236384_t_ = ImmutableList.<Structure<?>>builder().addAll(Structure.field_236384_t_).add(structure).build();
        }

        return STRUCTURES.register(name, () -> structure);
    }

    public static IStructurePieceType LAB_PIECE, STRAY_TOWER_PIECE;

    public static RegistryObject<Structure<NoFeatureConfig>> LAB_STRUCTURE = addStructure("lab",
        new LabStructure(NoFeatureConfig.field_236558_a_),
        GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
        new StructureSeparationSettings(7, 5, 1337));

    public static RegistryObject<Structure<NoFeatureConfig>> STRAY_TOWER_STRUCTURE = addStructure("stray_tower",
        new StrayTowerStructure(NoFeatureConfig.field_236558_a_),
        GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
        new StructureSeparationSettings(16, 8, 1341));

    public static StructureFeature<NoFeatureConfig, ? extends Structure<NoFeatureConfig>> LAB_FEATURE, STRAY_TOWER_FEATURE;

    public static void preInit() {
        STRUCTURES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static void init() {
        LEAD_ORE_GEN = Feature.ORE.withConfiguration(new OreFeatureConfig(IN_STONE,
            Registry.LEAD_ORE.get().getDefaultState(), Config.LEAD_VEIN_SIZE.get()))
            .square()
            .func_242731_b(Config.LEAD_VEIN_COUNT.get()) // per chunk
            .range(Config.LEAD_MAX_Y.get()) // maximum Y
            ;
        if (Config.LEAD_ENABLED.get()) ORES.add(LEAD_ORE_GEN);

        LAB_PIECE = register(LabStructure.Piece::new, "lab");
        LAB_FEATURE = register(LAB_STRUCTURE.get().withConfiguration(NoFeatureConfig.field_236559_b_), "lab");

        STRAY_TOWER_PIECE = register(StrayTowerStructure.Piece::new, "stray_tower");
        STRAY_TOWER_FEATURE = register(STRAY_TOWER_STRUCTURE.get().withConfiguration(NoFeatureConfig.field_236559_b_), "stray_tower");

        for (Structure<?> s : STRUCTURE_LIST) {
            DimensionStructuresSettings.field_236191_b_ = // Default structures
                ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
                    .putAll(DimensionStructuresSettings.field_236191_b_)
                    .put(s, STRUCTURE_SETTINGS.get(s.getRegistryName()))
                    .build();

            DimensionSettings.field_242740_q.getStructures().field_236193_d_.put(s, STRUCTURE_SETTINGS.get(s.getRegistryName()));
        }
    }

    @SubscribeEvent
    public void onBiomeLoad(BiomeLoadingEvent event) {
        for (ConfiguredFeature<?, ?> feature : ORES) {
            event.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, feature);
        }
        if (Config.LAB_ENABLED.get())
            event.getGeneration().withStructure(LAB_FEATURE);
        if (event.getCategory() == Biome.Category.ICY && Config.STRAY_TOWER_ENABLED.get())
            event.getGeneration().withStructure(STRAY_TOWER_FEATURE);
    }
}
