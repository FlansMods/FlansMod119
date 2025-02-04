package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public interface IAcceleration
{
    boolean isApproxZero();
    @Nonnull IAcceleration inverse();
    boolean hasLinearComponent(@Nonnull Transform actingOn);
    @Nonnull LinearAcceleration getLinearComponent(@Nonnull Transform actingOn);
    boolean hasAngularComponent(@Nonnull Transform actingOn);
    @Nonnull AngularAcceleration getAngularComponent(@Nonnull Transform actingOn);
    @Nonnull Component toFancyString();
}
