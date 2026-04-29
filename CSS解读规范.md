# CSS 解读规范

本文档用于规范 CSS 代码的解读方式，确保准确提取图层信息、颜色信息、位置信息、透明度、圆角、大小等关键属性。

---

## 一、图层信息解读

### 1.1 图层识别规则

通过以下特征识别图层结构：

| 特征 | 说明 |
|------|------|
| `/* 注释 */` | 注释标识图层名称，如 `/* BG */`、`/* Text */` |
| `z-index` | 数值越大，图层越靠上 |
| `order` | Flex 布局中的顺序，数值越小越靠前 |
| `isolation: isolate` | 创建独立堆叠上下文，内部图层不影响外部 |

### 1.2 图层结构模板

```
组件
├── [z-0] Layer 0（底层）
│   ├── 子层 A
│   └── 子层 B
├── [z-1] Layer 1
│   └── 子层 C
└── [z-2] Layer 2（顶层）
```

### 1.3 图层属性提取

每个图层需提取以下属性：

```
- position: 定位方式
- left/right/top/bottom: 位置坐标
- width/height: 尺寸
- z-index: 层级
- flex order: Flex 顺序
```

---

## 二、颜色信息解读

### 2.1 纯色填充 vs 渐变填充

**判断规则：**

| CSS 写法 | 类型 | 说明 |
|----------|------|------|
| `linear-gradient(0deg, #XXX, #XXX)` | **纯色填充** | 起始颜色 = 结束颜色 |
| `linear-gradient(0deg, #AAA, #BBB)` | **渐变填充** | 起始颜色 ≠ 结束颜色 |
| `#XXXXXX` | **纯色填充** | 直接十六进制 |
| `rgba(R, G, B, A)` | **纯色填充** | 带透明度的颜色 |

### 2.2 多层填充叠加

当 `background` 属性包含多个值时，表示多层填充叠加：

```css
background: 
  linear-gradient(0deg, #0091FF, #0091FF),  /* 第1层：纯色填充 */
  linear-gradient(0deg, #999999, #999999),  /* 第2层：纯色填充 */
  rgba(255, 255, 255, 0.75);                /* 第3层：纯色填充 */
```

**解读方式：**

| 层级 | 颜色值 | 透明度 | 混合模式 |
|------|--------|--------|----------|
| 第1层 | `#0091FF` | 100% | 对应 background-blend-mode 第1个值 |
| 第2层 | `#999999` | 100% | 对应 background-blend-mode 第2个值 |
| 第3层 | `#FFFFFF` | 75% | 对应 background-blend-mode 第3个值 |

### 2.3 透明度提取

**透明度来源：**

1. **rgba 格式**：直接读取 alpha 值
   - `rgba(255, 255, 255, 0.75)` → 透明度 75%

2. **十六进制格式**：默认 100%，除非是 8 位格式
   - `#FFFFFF` → 透明度 100%
   - `#FFFFFFBF` → 透明度 75%（BF = 191/255 ≈ 75%）

### 2.4 颜色信息输出模板

```
#### 纯色填充（Fill）
| 颜色值 | 用途 | 透明度 |
|--------|------|--------|
| `#XXXXXX` | 描述 | XX% |

#### 渐变填充（Gradient）
| 渐变类型 | 起始颜色 | 结束颜色 | 角度 |
|----------|----------|----------|------|
| linear-gradient | `#AAAAAA` | `#BBBBBB` | 0deg |

#### 透明色
| 颜色值 | 用途 | 透明度 |
|--------|------|--------|
| `rgba(R, G, B, A)` | 描述 | XX% |
```

---

## 三、位置信息解读

### 3.1 定位方式

| position 值 | 说明 |
|-------------|------|
| `absolute` | 绝对定位，相对于最近的定位祖先 |
| `relative` | 相对定位，相对于自身原位置 |
| `fixed` | 固定定位，相对于视口 |
| `sticky` | 粘性定位 |

### 3.2 位置坐标

**绝对定位元素：**

```css
position: absolute;
left: 30px;
top: 110px;
```

解读：元素左边缘距父容器左边缘 30px，上边缘距父容器上边缘 110px

**拉伸定位元素：**

```css
position: absolute;
left: 0px;
right: 0px;
top: 0px;
bottom: 0px;
```

解读：元素填充整个父容器

### 3.3 Flex 布局位置

```css
display: flex;
flex-direction: row;
justify-content: center;
align-items: center;
gap: 4px;
```

解读：
- 水平排列（row）
- 主轴居中
- 交叉轴居中
- 子元素间距 4px

---

## 四、大小信息解读

### 4.1 尺寸属性

| 属性 | 说明 |
|------|------|
| `width` | 宽度 |
| `height` | 高度 |
| `min-width/max-width` | 最小/最大宽度 |
| `min-height/max-height` | 最小/最大高度 |

### 4.2 Flex 子元素尺寸

```css
flex: none;
flex-grow: 0;
flex-shrink: 0;
```

解读：元素不伸缩，保持原始尺寸

```css
flex: 1;
flex-grow: 1;
```

解读：元素可伸展填充剩余空间

---

## 五、圆角信息解读

### 5.1 圆角类型

| CSS 写法 | 形状 | 说明 |
|----------|------|------|
| `border-radius: 0` | 直角 | 无圆角 |
| `border-radius: 5px` | 小圆角 | 圆角半径 5px |
| `border-radius: 1000px` | 胶囊形 | 大圆角值形成胶囊 |
| `border-radius: 50%` | 圆形 | 正方形时为圆形 |

### 5.2 分角圆角

```css
border-radius: 10px 20px 30px 40px;
```

解读：左上 10px、右上 20px、右下 30px、左下 40px

---

## 六、阴影信息解读

### 6.1 box-shadow 语法

```css
box-shadow: offsetX offsetY blurRadius spreadRadius color;
```

| 参数 | 说明 |
|------|------|
| offsetX | 水平偏移（正值向右） |
| offsetY | 垂直偏移（正值向下） |
| blurRadius | 模糊半径 |
| spreadRadius | 扩展半径（可选） |
| color | 阴影颜色 |

### 6.2 示例解读

```css
box-shadow: 0px 8px 40px rgba(0, 0, 0, 0.12);
```

解读：
- 水平偏移：0px（无偏移）
- 垂直偏移：8px（向下）
- 模糊半径：40px
- 颜色：黑色，透明度 12%

---

## 七、混合模式解读

### 7.1 background-blend-mode

用于多层背景之间的混合：

```css
background-blend-mode: plus-darker, overlay, saturation, normal;
```

解读：对应 background 的每一层，从前往后依次应用

### 7.2 mix-blend-mode

用于元素与底层内容的混合：

```css
mix-blend-mode: plus-darker;
```

解读：整个元素与下方内容按 plus-darker 模式混合

### 7.3 常见混合模式

| 模式 | 效果 |
|------|------|
| `normal` | 正常，无混合 |
| `multiply` | 正片叠底，变暗 |
| `screen` | 滤色，变亮 |
| `overlay` | 叠加，对比增强 |
| `darken` | 变暗，取较暗值 |
| `lighten` | 变亮，取较亮值 |
| `color-burn` | 颜色加深 |
| `color-dodge` | 颜色减淡 |
| `plus-darker` | 加深混合 |
| `saturation` | 饱和度混合 |

---

## 八、文字信息解读

### 8.1 字体属性

```css
font-family: 'SF Pro';
font-style: normal;
font-weight: 510;
font-size: 17px;
line-height: 20px;
```

解读：
- 字体：SF Pro
- 样式：正常（非斜体）
- 字重：510（Medium）
- 字号：17px
- 行高：20px

### 8.2 文字对齐

```css
text-align: center;
display: flex;
align-items: center;
```

解读：水平居中 + 垂直居中

### 8.3 字体特性

```css
font-feature-settings: 'ss16' on;
```

解读：启用 OpenType 特性 ss16（特定字形变体）

---

## 九、完整解读示例

### 输入 CSS

```css
/* Button - Tinted */
display: flex;
flex-direction: row;
justify-content: center;
align-items: center;
padding: 6px 20px;
gap: 4px;
isolation: isolate;

position: absolute;
width: 85px;
height: 48px;
left: 30px;
top: 110px;

border-radius: 1000px;

/* Tint + Shadow */
position: absolute;
left: 0px;
right: 0px;
top: 0px;
bottom: 0px;

background: linear-gradient(0deg, #0091FF, #0091FF), 
            linear-gradient(0deg, #999999, #999999), 
            rgba(255, 255, 255, 0.75);
background-blend-mode: plus-darker, overlay, normal;
box-shadow: 0px 8px 40px rgba(0, 0, 0, 0.12);
border-radius: 1000px;
```

### 输出解读

#### 按钮主体
| 属性 | 值 |
|------|-----|
| 尺寸 | 85px × 48px |
| 位置 | left: 30px, top: 110px |
| 定位 | absolute |
| 圆角 | 1000px（胶囊形） |
| 布局 | Flex 水平居中 |
| 内边距 | 6px 20px |
| 间距 | gap: 4px |
| 隔离 | isolation: isolate |

#### Tint + Shadow 层
| 属性 | 值 |
|------|-----|
| 位置 | 填充整个按钮 |
| 背景 | 多层纯色填充叠加（3层） |
| | 第1层：`#0091FF`，透明度 100%，混合模式 plus-darker |
| | 第2层：`#999999`，透明度 100%，混合模式 overlay |
| | 第3层：`#FFFFFF`，透明度 75%，混合模式 normal |
| 阴影 | 0px 8px 40px rgba(0, 0, 0, 0.12) |
| 圆角 | 1000px |

---

## 十、解读流程清单

1. [ ] 识别组件结构（通过注释）
2. [ ] 提取图层层级（z-index、order）
3. [ ] 解析位置信息（position、坐标）
4. [ ] 解析大小信息（width、height）
5. [ ] 解析颜色信息（区分纯色/渐变）
6. [ ] 解析透明度（rgba、十六进制）
7. [ ] 解析圆角信息
8. [ ] 解析阴影信息
9. [ ] 解析混合模式
10. [ ] 解析文字信息（如有）
