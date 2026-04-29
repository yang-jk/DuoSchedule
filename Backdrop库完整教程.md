# Backdrop库完整教程

## Get started

The **Backdrop** library (GitHub) can draw a copy of the **background (backdrop)** to a **foreground** with various effects.
You can achieve amazing **liquid glass** effect with the library.

## Installation

settings.gradle.kts

```kotlin

dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}
```

build.gradle.kts

```kotlin

dependencies {
    implementation("com.github.Kyant0:AndroidLiquidGlass:<version>")
}
```

## Tutorials

### Liquid Glass

🦖 You must read and practise these tutorials before using the library.

#### Glass Bottom Bar

Create a glass bottom bar

##### Goals

- Create a glass bottom bar over the `MainNavHost`.

```kotlin

Box(Modifier.fillMaxSize()) {
    MainNavHost()
}
```

Here is what `MainNavHost` looks like:
[图片：MainNavHost 效果]

##### What you will learn

- Create and draw backdrops

- Apply effects to the backdrops

- Handle the background drawing correctly

- Ensure the readability

##### Steps

1. **Draw backdrop and add lens effect**

```kotlin

Box(Modifier.fillMaxSize()) {
    val backdrop = rememberLayerBackdrop()

    MainNavHost(
        modifier = Modifier.layerBackdrop(backdrop)
    )

    Box(
        Modifier
            .safeContentPadding()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = {
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                }
            )
            .height(64f.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    )
}
```

[图片：初始效果]
Noooo! The effect is wrong! There are transparent pixels in the bottom bar. Because we only draw the `MainNavHost`, **the background outside of** `MainNavHost` **should be drawn too**.

1. **Draw the background to the backdrop (optional)**

```kotlin

val backgroundColor = Color.White
val backdrop = rememberLayerBackdrop {
    drawRect(backgroundColor)
    drawContent()
}
```

[图片：修复后的效果]
Nice work!
Try to adjust the lens effect and observe what will happen.

1. **Add blur effect**

```kotlin

Modifier.drawBackdrop(
    backdrop = backdrop,
    shape = { CircleShape },
    effects = {
        vibrancy()
        blur(4f.dp.toPx())
        lens(16f.dp.toPx(), 32f.dp.toPx())
    }
)
```

The use of `vibrancy()` enhances the saturation, giving us more visual impact.
[图片：添加模糊效果后的效果]

1. **Add surface for readability**

```kotlin

Modifier.drawBackdrop(
    backdrop = backdrop,
    shape = { CircleShape },
    effects = {
        vibrancy()
        blur(4f.dp.toPx())
        lens(16f.dp.toPx(), 32f.dp.toPx())
    },
    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
)
```

[图片：添加表面后的效果]
The readability has increased. **You must balance between beauty and readability.**

1. **Final code**

```kotlin

Box(Modifier.fillMaxSize()) {
    val backgroundColor = Color.White
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    MainNavHost(
        modifier = Modifier.layerBackdrop(backdrop)
    )

    Box(
        Modifier
            .safeContentPadding()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
            )
            .height(64f.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    )
}
```

##### Exercise: Add a tinted glass icon button

[图片：带 tint 按钮的效果]
Final code

```kotlin

Box(Modifier.fillMaxSize()) {
    val backgroundColor = Color.White
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    MainNavHost(
        modifier = Modifier.layerBackdrop(backdrop)
    )

    Row(
        Modifier
            .safeContentPadding()
            .height(64f.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(4f.dp.toPx())
                        lens(16f.dp.toPx(), 32f.dp.toPx())
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
                )
                .fillMaxHeight()
                .weight(1f)
        )
        Box(
            Modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(4f.dp.toPx())
                        lens(16f.dp.toPx(), 32f.dp.toPx())
                    },
                    onDrawSurface = {
                        val tint = Color(0xFF0088FF)
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.75f))
                    }
                )
                .aspectRatio(1f)
        )
    }
}
```

It is recommended to use `BlendMode.Hue`, so that the hue of backdrop will adapt to the tint color.

#### Interactive Glass Bottom Bar

Add interactive feedbacks for the glass bottom bar
[图片：交互式玻璃底部栏效果]

##### Goals

- Add "press to scale" animation to the glass bottom bar

##### What you will learn

- Handle the transformations (scale, rotation) correctly

- Make use of `layerBlock` parameter of the `drawBackdrop` modifier

##### Steps

1. **Press to scale**
Update the code for the glass bottom bar.

```kotlin

val animationScope = rememberCoroutineScope()
val progressAnimation = remember { Animatable(0f) }

Box(
    Modifier
        .graphicsLayer {
            val progress = progressAnimation.value
            val maxScale = (size.width + 16f.dp.toPx()) / size.width
            val scale = lerp(1f, maxScale, progress)
            scaleX = scale
            scaleY = scale
        }
        .drawBackdrop(
            backdrop = backdrop,
            shape = { CircleShape },
            effects = {
                vibrancy()
                blur(4f.dp.toPx())
                lens(16f.dp.toPx(), 32f.dp.toPx())
            },
            onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
        )
        .clickable {}
        .pointerInput(animationScope) {
            val animationSpec = spring(0.5f, 300f, 0.001f)
            awaitEachGesture {
                // press
                awaitFirstDown()
                animationScope.launch {
                    progressAnimation.animateTo(1f, animationSpec)
                }

                // release
                waitForUpOrCancellation()
                animationScope.launch {
                    progressAnimation.animateTo(0f, animationSpec)
                }
            }
        }
        .fillMaxHeight()
        .weight(1f)
)
```

Press the bottom bar.
[图片：缩放错误的效果]
Oops! The backdrop is misplaced, it will also scale with the bottom bar. **But the backdrop shouldn't scale**.

1. **Prevent backdrop from scaling**
Move the code in `graphicsLayer` to `layerBlock` in the `drawBackdrop` modifier.

```kotlin

val animationScope = rememberCoroutineScope()
val progressAnimation = remember { Animatable(0f) }

Box(
    Modifier
        .drawBackdrop(
            backdrop = backdrop,
            shape = { CircleShape },
            effects = {
                vibrancy()
                blur(4f.dp.toPx())
                lens(16f.dp.toPx(), 32f.dp.toPx())
            },
            layerBlock = {
                val progress = progressAnimation.value
                val maxScale = (size.width + 16f.dp.toPx()) / size.width
                val scale = lerp(1f, maxScale, progress)
                scaleX = scale
                scaleY = scale
            },
            onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
        )
        .clickable(interactionSource = null, indication = null) {}
        .pointerInput(animationScope) {
            val animationSpec = spring(0.5f, 300f, 0.001f)
            awaitEachGesture {
                // press
                awaitFirstDown()
                animationScope.launch {
                    progressAnimation.animateTo(1f, animationSpec)
                }

                // release
                waitForUpOrCancellation()
                animationScope.launch {
                    progressAnimation.animateTo(0f, animationSpec)
                }
            }
        }
        .fillMaxHeight()
        .weight(1f)
)
```

Press the bottom bar again.
[图片：修复后的缩放效果]
It works correctly! The content will scale but the backdrop won't scale.

##### Final code

```kotlin

Box(Modifier.fillMaxSize()) {
    val backgroundColor = Color.White
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    MainNavHost(
        modifier = Modifier.layerBackdrop(backdrop)
    )

    Row(
        Modifier
            .safeContentPadding()
            .height(64f.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val animationScope = rememberCoroutineScope()
        val progressAnimation = remember { Animatable(0f) }

        Box(
            Modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(4f.dp.toPx())
                        lens(16f.dp.toPx(), 32f.dp.toPx())
                    },
                    layerBlock = {
                        val progress = progressAnimation.value
                        val maxScale = (size.width + 16f.dp.toPx()) / size.width
                        val scale = lerp(1f, maxScale, progress)
                        scaleX = scale
                        scaleY = scale
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
                )
                .clickable(interactionSource = null, indication = null) {}
                .pointerInput(animationScope) {
                    val animationSpec = spring(0.5f, 300f, 0.001f)
                    awaitEachGesture {
                        // press
                        awaitFirstDown()
                        animationScope.launch {
                            progressAnimation.animateTo(1f, animationSpec)
                        }

                        // release
                        waitForUpOrCancellation()
                        animationScope.launch {
                            progressAnimation.animateTo(0f, animationSpec)
                        }
                    }
                }
                .fillMaxHeight()
                .weight(1f)
        )
        Box(
            Modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousCapsule },
                    effects = {
                        vibrancy()
                        blur(4f.dp.toPx())
                        lens(16f.dp.toPx(), 32f.dp.toPx())
                    },
                    onDrawSurface = {
                        val tint = Color(0xFF0088FF)
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.75f))
                    }
                )
                .aspectRatio(1f)
        )
    }
}
```

#### Glass Bottom Sheet

Create a glass bottom sheet

##### Goals

- Create a glass bottom sheet based on the code:

```kotlin

Box(Modifier.fillMaxSize()) {
    val backgroundColor = Color.White
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    MainNavHost(
        modifier = Modifier.layerBackdrop(backdrop)
    )

    GlassBottomSheet(backdrop = backdrop)
}
```

##### What you will learn

- Handle the case of "glass on glass"

- Make use of `exportedBackdrop` parameter of the `drawBackdrop` modifier

##### Steps

1. **Create a GlassBottomSheet**
GlassBottomSheet.kt

```kotlin

// your package

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
fun BoxScope.GlassBottomSheet(backdrop: Backdrop) {
    Column(
        Modifier
            .safeContentPadding()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(44f.dp) },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(24f.dp.toPx(), 48f.dp.toPx(), true)
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
            )
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        Spacer(Modifier.height(256f.dp))
        // glass button
        Box(
            Modifier
                .padding(16f.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    shadow = null,
                    effects = {
                        vibrancy()
                        blur(4f.dp.toPx())
                        lens(16f.dp.toPx(), 32f.dp.toPx())
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
                )
                .height(56f.dp)
                .fillMaxWidth()
        )
    }
}
```

[图片：初始玻璃底页效果]
The backdrop for the glass button is `backdrop`, but we want to include the bottom sheet.

You will get a crash:
Fatal signal 11 (SIGSEGV), code 2 (SEGV\*ACCERR), fault addr 0x\*_ in tid \*\* (RenderThread), pid \*\*
Because the `layerBackdrop` modifier will draw the content to the `bottomSheetBackdrop`, and the content will draw the `bottomSheetBackdrop`, **it's a loop**!

1. **Use the bottom sheet as a backdrop for the glass button (CORRECT code)**
Use `exportedBackdrop` in `drawBackdrop` modifier, **it will skip drawing the content**.

```kotlin

val bottomSheetBackdrop = rememberLayerBackdrop()

Column(
    Modifier
        .drawBackdrop(
            backdrop = backdrop,
            exportedBackdrop = bottomSheetBackdrop,
        )
) {
    // glass button
    Box(
        Modifier
            .drawBackdrop(
                backdrop = bottomSheetBackdrop,
            )
    )
}
```

[图片：修复后的玻璃底页效果]

##### Final code

GlassBottomSheet.kt

```kotlin

// your package

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
fun BoxScope.GlassBottomSheet(backdrop: Backdrop) {
    val bottomSheetBackdrop = rememberLayerBackdrop()
    Column(
        Modifier
            .safeContentPadding()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(44f.dp) },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(24f.dp.toPx(), 48f.dp.toPx(), true)
                },
                exportedBackdrop = bottomSheetBackdrop,
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
            )
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        Spacer(Modifier.height(256f.dp))
        Box(
            Modifier
                .padding(16f.dp)
                .drawBackdrop(
                    backdrop = bottomSheetBackdrop,
                    shape = { CircleShape },
                    shadow = null,
                    effects = {
                        vibrancy()
                        blur(4f.dp.toPx())
                        lens(16f.dp.toPx(), 32f.dp.toPx())
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
                )
                .height(56f.dp)
                .fillMaxWidth()
        )
    }
}
```

#### Glass Slider

Create a glass slider
[图片：玻璃滑块效果]

##### Goals

- Create a glass slider based on the code:

```kotlin

Box(Modifier.fillMaxSize()) {
    val backgroundColor = Color.White
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    MainNavHost(
        modifier = Modifier.layerBackdrop(backdrop)
    )

    GlassSlider(backdrop = backdrop)
}
```

##### What you will learn

- Combine multiple backdrops by using the `rememberCombinedBackdrop` function

##### Steps

1. **Create a GlassSlider**
GlassSlider.kt

```kotlin

// your package

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.lens

@Composable
fun GlassSlider(
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier
            .padding(horizontal = 24f.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        val trackBackdrop = rememberLayerBackdrop()

        // track
        Box(
            Modifier
                .layerBackdrop(trackBackdrop)
                .background(Color(0xFF0088FF), CircleShape)
                .height(6f.dp)
                .fillMaxWidth()
        )

        // thumb
        Box(
            Modifier
                .offset(x = maxWidth / 2f - 28f.dp)
                .drawBackdrop(
                    // We want to draw both of `backdrop` and `trackBackdrop`
                    backdrop = trackBackdrop,
                    shape = { CircleShape },
                    effects = {
                        lens(
                            refractionHeight = 12f.dp.toPx(),
                            refractionAmount = 16f.dp.toPx(),
                            chromaticAberration = true
                        )
                    }
                )
                .size(56f.dp, 32f.dp)
        )
    }
}
```

1. **Replace line 51 in GlassSlider.kt with**

- `trackBackdrop`
[图片：仅 trackBackdrop 效果]

- `backdrop`
[图片：仅 backdrop 效果]

- `rememberCombinedBackdrop(backdrop, trackBackdrop)`
[图片：组合 backdrop 效果]
Background and track are refracted by thumb simultaneously.

#### Smoother rounded corners

The [Capsule](https://github.com/Kyant0/Capsule) library allows you to create beautiful and smooth G2 continuous rounded rectangles.
In the previous tutorials, all shapes are created by the library.
Can you tell the difference?
[图片：圆角对比效果]
If you prefer the right one, the Capsule library is your right choice.

#### Progressive blur

Create progressive blur effect

##### Alpha-masked progressive blur

```kotlin

Modifier.drawPlainBackdrop(
    backdrop = backdrop,
    shape = { RectangleShape },
    effects = {
        blur(4f.dp.toPx())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            effect(
                RenderEffect.createRuntimeShaderEffect(
                    obtainRuntimeShader(
                        "AlphaMask",
                        """
uniform shader content;

uniform float2 size;
layout(color) uniform half4 tint;
uniform float tintIntensity;

half4 main(float2 coord) {
    float blurAlpha = smoothstep(size.y, size.y * 0.5, coord.y);
    float tintAlpha = smoothstep(size.y, size.y * 0.5, coord.y);
    return mix(content.eval(coord) * blurAlpha, tint * tintAlpha, tintIntensity);
}"""
                    ).apply {
                        setFloatUniform("size", size.width, size.height)
                        setColorUniform("tint", tintColor.toArgb())
                        setFloatUniform("tintIntensity", 0.8f)
                    },
                    "content"
                )
            )
        }
    }
)
```

##### Exact progressive blur

TBD.

## API

### Backdrops

#### Backdrop

`rememberBackdrop` can draw a backdrop with custom commands.

#### Layer backdrop

`rememberLayerBackdrop` must be used with `Modifier.layerBackdrop` to draw the Composable's content, or it's derived by using `exportedBackdrop` parameter of the `drawBackdrop` modifier. It is coordinates-dependent.

#### Combined backdrop

`rememberCombinedBackdrop` can merge multiple backdrops into one backdrop. It is useful to create components such as tabs and sliders.

#### Canvas backdrop

`rememberCanvasBackdrop` can draw custom content to a empty backdrop. It is coordinates-independent.

#### Empty backdrop

`emptyBackdrop` draws nothing.

### Backdrop effects

Backdrop effects are `RenderEffect`s radically. They only take effect with Android 12 and above. Some effects involving with `RuntimeShader` need Android 13 and above.
The order of effects matters. To create the right visual effects, you must apply them with the following order:
color filter ⇒ blur ⇒ lens

#### Color filter

##### Custom ColorFilter

```kotlin

colorFilter(colorFilter: (Android)ColorFilter)
```

```kotlin

colorFilter(colorFilter: (Compose)ColorFilter)
```

##### Opacity

```kotlin

opacity(alpha: Float)
```

##### Color controls (brightness, contrast, saturation)

```kotlin

colorControls(
    brightness: Float = 0f,
    contrast: Float = 1f,
    saturation: Float = 1f
)
```

[图片：颜色控制效果]

##### Vibrancy

Multiply saturation with 1.5. It is equivalent to `colorControls(saturation = 1.5f)`.

```kotlin

vibrancy()
```

##### Exposure adjustment

```kotlin

exposureAdjustment(ev: Float)
```

##### Gamma adjustment (Android 13+)

```kotlin

gammaAdjustment(power: Float)
```

#### Blur

##### Blur effect

```kotlin

blur(
    radius: Float,
    edgeTreatment: TileMode = TileMode.Clamp
)
```

#### Lens (Android 13+)

⚠️ To use the lens effect, your `shape` must be `CornerBasedShape`.

##### Lens effect

```kotlin

lens(
    refractionHeight: Float,
    refractionAmount: Float = height,
    depthEffect: Boolean = false,
    chromaticAberration: Boolean = false
)
```

- `height` must be in [0, `shape.minCornerRadius`]. If it exceeds, it will have discontinuities at some corners, but it's acceptable.

- `amount` must be in [0, `size.minDimension`].
[图片：透镜效果 1]
[图片：透镜效果 2]

#### RenderEffect

##### Custom RenderRffect

```kotlin

effect(effect: (Android)RenderEffect)
```

```kotlin

effect(effect: (Compose)RenderEffect)
```

## FAQ

### Why doesn't the effect work?

Because you may not use the `Modifier.layerBackdrop(backdrop: LayerBackdrop)` modifier, [相关文章](https://kyant.gitbook.io/backdrop) may help you.

### Why did it crash?

If you get crash: Fatal signal 11 (SIGSEGV), code 2 (SEGV\*ACCERR), fault addr 0x\*_ in tid \*\* (RenderThread), pid \*\*, [相关文章](https://kyant.gitbook.io/backdrop) may help you.
> （注：文档部分内容可能由 AI 生成）