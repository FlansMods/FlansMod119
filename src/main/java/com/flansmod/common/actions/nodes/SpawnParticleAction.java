package com.flansmod.common.actions.nodes;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.guns.elements.ESpreadPattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class SpawnParticleAction extends ActionInstance {
    public SpawnParticleAction(@NotNull ActionGroupInstance group, @NotNull ActionDefinition def) {
        super(group, def);
    }

    @Override
    public void OnTriggerClient(int triggerIndex) {

        FlansModClient.SpawnLocalParticles(this);
    }

    @Override
    public void OnTriggerServer(int triggerIndex) {

    }

    public String GetRelativeAPName() { return Group.Context.GetSibling(AttachPoint()); }

    @Nonnull
    public String AttachPoint() { return Group.Context.ModifyString(Constants.PARTICLE_ATTACH_POINT+Def.id, "shoot_origin"); }
    @Nonnull
    public ResourceLocation ParticleType() { return new ResourceLocation(Group.Context.ModifyString(Constants.PARTICLE_TYPE+Def.id, "minecraft:poof")); }
    @Nonnull
    public int ParticleCount() { return Math.round(Group.Context.ModifyFloat(Constants.PARTICLE_COUNT+Def.id).get());}

    public float ParticleSpread() { return Group.Context.ModifyFloat(Constants.PARTICLE_SPREAD+Def.id).get();}

    public float ParticleSpeed() { return Group.Context.ModifyFloat(Constants.PARTICLE_SPEED+Def.id).get();}

    public ESpreadPattern SpreadPattern() 	{ return (ESpreadPattern)Group.Context.ModifyEnum(Constants.PARTICLE_SPREAD_PATTERN+Def.id, ESpreadPattern.FilledCircle, ESpreadPattern.class); }

}
