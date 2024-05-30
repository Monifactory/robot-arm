package screret.robotarm.data.entity;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import screret.robotarm.client.renderer.entity.FOUPEntityRenderer;
import screret.robotarm.entity.FOUPCartEntity;

import java.util.function.Consumer;

import static screret.robotarm.RobotArm.REGISTRATE;

public class RobotArmEntities {
    public static EntityEntry<FOUPCartEntity> FOUP = register("foup_cart", FOUPCartEntity::new, MobCategory.MISC,
            () -> FOUPEntityRenderer::new,
            RobotArmEntities.properties(8, 3, true, true, 1, 1));

    public static <T extends Entity> EntityEntry<T> register(String name,
                                                             EntityType.EntityFactory<T> factory,
                                                             MobCategory classification,
                                                             NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer,
                                                             Consumer<EntityBuilder<T, Registrate>> builderConsumer) {
        var builder = REGISTRATE.entity(name, factory, classification);
        builder.renderer(renderer);
        builderConsumer.accept(builder);
        return builder.register();
    }

    public static Consumer<EntityBuilder<FOUPCartEntity, Registrate>> properties(int clientTrackRange,
                                                                                 int updateFrequency,
                                                                                 boolean syncVelocity,
                                                                                 boolean immuneToFire,
                                                                                 float width, float height) {
        return builder -> builder.properties(b -> {
            if (immuneToFire) {
                b.fireImmune();
            }
            b.clientTrackingRange(8).updateInterval(updateFrequency).setShouldReceiveVelocityUpdates(syncVelocity)
                    .sized(width, height);
        });
    }

    public static void init() {

    }
}
