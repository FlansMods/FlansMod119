package com.flansmod.client.render.models.baked;

import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.animation.ESmoothSetting;
import com.flansmod.client.render.animation.FlanimationDefinition;
import com.flansmod.client.render.animation.PoseCache;
import com.flansmod.client.render.animation.elements.KeyframeDefinition;
import com.flansmod.client.render.animation.elements.SequenceDefinition;
import com.flansmod.client.render.animation.elements.SequenceEntryDefinition;
import com.flansmod.client.render.models.ETurboRenderMaterial;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.nodes.AnimationAction;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.textures.UnitTextureAtlasSprite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.flansmod.client.render.models.TurboRenderUtility.flanItemRenderType;

public record BakedTurboRig(@Nonnull Map<String, BakedModel> iconModels,
							@Nonnull Map<String, BakedTurboSection> sections,
							@Nonnull Map<String, BakedAttachPoint> attachPoints,
							@Nonnull Map<String, ResourceLocation> textures,
							@Nonnull Map<String, Float> floatParameters,
							@Nonnull ItemTransforms transforms) implements BakedModel
{
	public static final String AP_CORE = "body";

	@Nonnull
	public BakedAttachPoint getRootAP() { return attachPoints.get(AP_CORE); }
	@Nonnull
	public BakedAttachPoint getAP(@Nonnull String apName) { return attachPoints.getOrDefault(apName, BakedAttachPoint.invalid); }
	@Nonnull
	public BakedAttachPoint getAP(@Nonnull EAttachmentType attachmentType, int index)
	{
		if(index == 0 && attachPoints.containsKey(attachmentType.unindexedName(index)))
			return attachPoints.get(attachmentType.unindexedName(index));
		if(attachPoints.containsKey(attachmentType.indexedName(index)))
			return attachPoints.get(attachmentType.indexedName(index));
		return BakedAttachPoint.invalid;
	}
	@Nonnull
	public String getAPKey(@Nonnull EAttachmentType attachmentType, int index)
	{
		if(index == 0)
		{
			String unindexedName = attachmentType.unindexedName(index);
			if(attachPoints.containsKey(unindexedName))
				return unindexedName;
		}
		return attachmentType.indexedName(index);
	}
	@Nullable
	public BakedTurboSection getSection(@Nonnull String sectionName) { return sections.get(sectionName); }
	public boolean hasSection(@Nonnull String sectionName) { return sections.containsKey(sectionName); }
	@Nullable
	public BakedModel getIconModel(@Nonnull String key) { return iconModels.get(key); }
	@Nullable
	public ResourceLocation getSkin(@Nonnull String key) { return textures.get(key); }

	@Nonnull
	public Transform getTransform(@Nonnull ItemDisplayContext transformType)
	{
		return Transform.fromItem(transforms.getTransform(transformType));
	}





	@Override
	public boolean useAmbientOcclusion() { return false; }
	@Override
	public boolean isGui3d() { return true; }
	@Override
	public boolean usesBlockLight() { return false; }
	@Override
	public boolean isCustomRenderer() { return true; }
	@Override @Nonnull
	public TextureAtlasSprite getParticleIcon() { return UnitTextureAtlasSprite.INSTANCE; }
	@Override @Nonnull
	public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }

	@Override @Nonnull
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource)
	{
		return Collections.emptyList();
	}


	// Simple render, all parts, no culling or animation
	public void render(@Nonnull VertexConsumer vc,
					   @Nonnull TransformStack transformStack,
					   int light,
					   int overlay,
					   float scale)
	{
		for(BakedTurboSection section : sections.values())
		{
			section.render(vc, transformStack, light, overlay, scale);
		}
	}


	@Nonnull
	private VertexConsumer selectVertexConsumer(@Nonnull ResourceLocation texture,
												@Nonnull ETurboRenderMaterial turboMaterial,
									  			@Nonnull MultiBufferSource buffers)
	{
		return buffers.getBuffer(flanItemRenderType(texture, turboMaterial));
	}

	public void forEachChild(@Nonnull String partName, @Nonnull Consumer<String> childFunc)
	{
		for (var kvp : attachPoints.entrySet())
			if (partName.equals(kvp.getValue().parent()))
				childFunc.accept(kvp.getKey());
	}
	public void forEachChild(@Nonnull String partName, @Nonnull BiConsumer<String, BakedAttachPoint> childFunc)
	{
		for (var kvp : attachPoints.entrySet())
			if (partName.equals(kvp.getValue().parent()))
				childFunc.accept(kvp.getKey(), kvp.getValue());
	}

	public void renderSection(@Nonnull String sectionName,
							  @Nonnull Function<String, ResourceLocation> textureFunc,
							  @Nonnull RenderContext renderContext)
	{
		BakedTurboSection section = getSection(sectionName);
		if(section != null)
		{
			ResourceLocation textureForSection = textureFunc.apply(sectionName);
			VertexConsumer vc = selectVertexConsumer(textureForSection, section.material(), renderContext.Buffers);

			section.render(vc, renderContext.Transforms, renderContext.Light, renderContext.Overlay, 0.0625f);
		}

		//Minecraft.getInstance().getItemRenderer().renderQuadList(
		//		transformStack.top().toNewPoseStack(),
		//		vc,
		//		bakedModel.getQuads(
		//				null,
		//				null,
		//				Minecraft.getInstance().font.random),
		//		ItemStack.EMPTY,
		//		light,
		//		overlay
		//);
	}
	public void renderSectionIteratively(@Nonnull RenderContext renderContext,
										 @Nonnull String partName,
										 @Nonnull Function<String, ResourceLocation> textureFunc,
										 @Nonnull BiFunction<String, RenderContext, Boolean> preRenderFunc,
										 @Nonnull BiConsumer<String, RenderContext> postRenderFunc)
	{
		renderContext.Transforms.push();
		{
			boolean shouldRender = preRenderFunc.apply(partName, renderContext);
			if(shouldRender)
			{
				renderSection(partName, textureFunc, renderContext);
				forEachChild(partName, (childName, childAP) -> {
					renderContext.Transforms.push();
					renderContext.Transforms.add(childAP.offset());
					renderSectionIteratively(renderContext, childName, textureFunc, preRenderFunc, postRenderFunc);
					renderContext.Transforms.pop();
				});
			}
			postRenderFunc.accept(partName, renderContext);
		}
		renderContext.Transforms.pop();
	}


	@Nonnull
	public Transform getPose(@Nonnull String partName,
							 @Nonnull ResourceLocation modelLocation,
							 @Nonnull FlanimationDefinition animationSet,
							 @Nullable ActionStack actionStack)
	{
		if(actionStack != null)
		{
			if(!animationSet.IsValid())
				return Transform.error("Missing animation set");

			List<AnimationAction> animActions = new ArrayList<>();
			for(ActionGroupInstance group : actionStack.GetActiveActionGroups())
				for(ActionInstance action : group.GetActions())
				{
					if (action instanceof AnimationAction animAction)
						animActions.add(animAction);
				}

			List<Transform> poses = new ArrayList<>();
			for (AnimationAction animAction : animActions)
			{
				SequenceDefinition sequence = animationSet.GetSequence(animAction.Def.anim);
				if (sequence == null)
				{
					FlansMod.LOGGER.warn("Could not find animation sequence " + animAction.Def.anim + " in anim set " + animationSet.Location);
					continue;
				}

				// Make sure we scale the sequence (which can be played at any speed) with the target duration of this specific animation action
				float progress = animAction.AnimFrame + Minecraft.getInstance().getPartialTick();
				float animMultiplier = sequence.Duration() / (animAction.Def.duration * 20f);
				progress *= animMultiplier;

				// Find the segment of this animation that we need
				SequenceEntryDefinition[] segment = sequence.GetSegment(progress);
				float segmentDuration = segment[1].tick - segment[0].tick;

				// If it is valid, let's animate it
				if (segmentDuration >= 0.0f)
				{
					KeyframeDefinition from = animationSet.GetKeyframe(segment[0]);
					KeyframeDefinition to = animationSet.GetKeyframe(segment[1]);
					if (from != null && to != null)
					{
						float linearParameter = (progress - segment[0].tick) / segmentDuration;
						linearParameter = Maths.clamp(linearParameter, 0f, 1f);
						float outputParameter = linearParameter;

						// Instant transitions take priority first
						if (segment[0].exit == ESmoothSetting.instant)
							outputParameter = 1.0f;
						if (segment[1].entry == ESmoothSetting.instant)
							outputParameter = 0.0f;

						// Then apply smoothing?
						if (segment[0].exit == ESmoothSetting.smooth)
						{
							// Smoothstep function
							if (linearParameter < 0.5f)
								outputParameter = linearParameter * linearParameter * (3f - 2f * linearParameter);
						}
						if (segment[1].entry == ESmoothSetting.smooth)
						{
							// Smoothstep function
							if (linearParameter > 0.5f)
								outputParameter = linearParameter * linearParameter * (3f - 2f * linearParameter);
						}

						//PoseDefinition fromPose = animationSet.GetPoseForPart(from, partName);
						//PoseDefinition toPose = animationSet.GetPoseForPart(to, partName);

						poses.add(PoseCache.Lerp(modelLocation,
								animationSet.Location,
								from.name,
								to.name,
								partName,
								outputParameter));
					}
				}
			}

			Transform resultPose = poses.size() > 0 ? Transform.interpolate(poses) : Transform.identity();
			//BakedTurboSection model = getSection(partName);
			//if (model != null)
			//{
				//return resultPose.Translate(model.offset.x, model.offset.y, model.offset.z);
			//}
			// else
			return resultPose;
		}

		return Transform.identity();
	}

}
