package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;

public class TransformDefinition
{
    @JsonField
    public Vec3 position = new Vec3(0, 0, 0);
    @JsonField
    public Vec3 eulerAngles = new Vec3(0, 0, 0);
}
