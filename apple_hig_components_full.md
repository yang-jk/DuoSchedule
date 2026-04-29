# Apple Human Interface Guidelines - Components

> Auto-generated from Apple Developer Documentation
> Source: https://developer.apple.com/design/human-interface-guidelines/components

---

## Table of Contents

- [Content](#content)
  - [Charts](#charts)
  - [Image Views](#image-views)
  - [Text Views](#text-views)
  - [Web Views](#web-views)
- [Layout and organization](#layout-and-organization)
  - [Boxes](#boxes)
  - [Collections](#collections)
  - [Column Views](#column-views)
  - [Disclosure Controls](#disclosure-controls)
  - [Labels](#labels)
  - [Lists And Tables](#lists-and-tables)
  - [Lockups](#lockups)
  - [Outline Views](#outline-views)
  - [Split Views](#split-views)
  - [Tab Views](#tab-views)
- [Menus and actions](#menus-and-actions)
  - [Activity Views](#activity-views)
  - [Buttons](#buttons)
  - [Context Menus](#context-menus)
  - [Dock Menus](#dock-menus)
  - [Edit Menus](#edit-menus)
  - [Home Screen Quick Actions](#home-screen-quick-actions)
  - [Menus](#menus)
  - [Ornaments](#ornaments)
  - [Pop Up Buttons](#pop-up-buttons)
  - [Pull Down Buttons](#pull-down-buttons)
  - [The Menu Bar](#the-menu-bar)
  - [Toolbars](#toolbars)
- [Navigation and search](#navigation-and-search)
  - [Path Controls](#path-controls)
  - [Search Fields](#search-fields)
  - [Sidebars](#sidebars)
  - [Tab Bars](#tab-bars)
  - [Token Fields](#token-fields)
- [Presentation](#presentation)
  - [Action Sheets](#action-sheets)
  - [Alerts](#alerts)
  - [Page Controls](#page-controls)
  - [Panels](#panels)
  - [Popovers](#popovers)
  - [Scroll Views](#scroll-views)
  - [Sheets](#sheets)
  - [Windows](#windows)
- [Selection and input](#selection-and-input)
  - [Color Wells](#color-wells)
  - [Combo Boxes](#combo-boxes)
  - [Digit Entry Views](#digit-entry-views)
  - [Image Wells](#image-wells)
  - [Pickers](#pickers)
  - [Segmented Controls](#segmented-controls)
  - [Sliders](#sliders)
  - [Steppers](#steppers)
  - [Text Fields](#text-fields)
  - [Toggles](#toggles)
  - [Virtual Keyboards](#virtual-keyboards)
- [Status](#status)
  - [Activity Rings](#activity-rings)
  - [Gauges](#gauges)
  - [Progress Indicators](#progress-indicators)
  - [Rating Indicators](#rating-indicators)
- [System experiences](#system-experiences)
  - [App Shortcuts](#app-shortcuts)
  - [Complications](#complications)
  - [Controls](#controls)
  - [Live Activities](#live-activities)
  - [Notifications](#notifications)
  - [Status Bars](#status-bars)
  - [Top Shelf](#top-shelf)
  - [Watch Faces](#watch-faces)
  - [Widgets](#widgets)

---

## Content

### Charts

**Source**: [https://developer.apple.com/design/human-interface-guidelines/charts](https://developer.apple.com/design/human-interface-guidelines/charts)


# Charts
An effective chart highlights a few key pieces of information in a dataset, helping people gain insights and make decisions. For example, people might use a chart to:

- Learn how upcoming weather conditions might affect their plans.
- Analyze stock prices to understand past performance and discover trends.
- Review fitness data to monitor their progress and set new goals.
To learn about designing charts to enhance your experience, see Charting data; for developer guidance, see Creating a chart using Swift Charts.


## Anatomy
A chart comprises several graphical elements that depict the values in a dataset and convey information about them.

A mark is a visual representation of a data value. You create a chart by supplying one or more series of data values, assigning each value to a mark. To specify the style of chart you want to display — such as bar chart, line chart, or scatter plot — you choose a mark type, such as bar, line, or point (for guidance, see Marks). The general task of depicting individual data values in a chart is called plotting, and the area that contains the marks is called the plot area.

To depict a value, each type of mark uses visual attributes that are determined by a scale, which maps data values like numbers, dates, or categories to visual characteristics like position, color, or height. For example, a bar mark can use a particular height to represent the magnitude of a value and a particular position to represent the time at which the value occurred.

To give people the context they need to interpret a chart’s visual characteristics, you supply descriptive content that can take a few different forms.

You can use an axis to help define a frame of reference for the data represented by a set of marks. Many charts display a pair of axes at the edges of the plot area — one horizontal and one vertical — where each axis represents a variable like time, amount, or category.

An axis can include ticks, which are reference points that help people visually locate the position of important values along the axis, such as a 0, 50%, and 100%. Many charts display grid lines that each extend from a tick across the plot area to help people visually estimate a data value when its mark isn’t near an axis.

You also have multiple ways to describe chart elements to help people interpret the data and to highlight the key information you want to communicate. For example, you can supply labels that name items like axes, grid lines, ticks, or marks, and accessibility labels that describe chart elements for people who use assistive technologies. To provide context and additional details, you can create descriptive titles, subtitles, and annotations. When needed, you can also create a legend, which describes chart properties that aren’t related to a mark’s position, such as the use of color or shape to denote different value categories.

Clear, accurate descriptions can help make a chart more approachable and accessible; to learn about additional ways to improve the accessibility of your chart, see Enhancing the accessibility of a chart.


## Marks
Choose a mark type based on the information you want to communicate about the data. Some of the most familiar mark types are bar, line, and point; for developer guidance on these and other mark types, see Swift Charts.

Bar marks work well in charts that help people compare values in different categories or view the relative proportions of various parts in a whole. When used to help people understand data that changes over time, bar charts work especially well when each value can represent a sum, like the total number of steps taken in a day.

Line marks can also show how values change over time. In a line chart, a line connects all data values in one series of data. The slope of the line reveals the magnitude of change between data values and can help people visualize overall trends.

Point marks help you depict individual data values as visually distinct marks. A set of point marks can show how two different properties of your data relate to each other, helping people inspect individual data values and identify outliers and clusters.

Consider combining mark types when it adds clarity to your chart. For example, if you use a line chart to show a change over time, you might want to add point marks on top of the line to highlight individual data points. By combining points with a line, you can help people understand the overall trend while also drawing their attention to individual values.


## Axes
Use a fixed or dynamic axis range depending on the meaning of your chart. In a fixed range, the upper and lower bounds of the axis never change, whereas in a dynamic range, the upper and lower bounds can vary with the current data. Consider using a fixed range when specific minimum and maximum values are meaningful for all possible data values. For example, people expect a chart that shows a battery’s current charge to have a minimum value of 0% (completely empty) and a maximum value of 100% (completely full).

In contrast, consider using a dynamic range when the possible data values can vary widely and you want the marks to fill the available plot area. For example, the upper bound of the Y axis range in the Health app’s Steps chart varies so that the largest number of steps in a particular time period is close to the top of the chart.

Define the value of the lower bound based on mark type and chart usage. For example, bar charts can work well when you use zero for the lower bound of the Y axis, because doing so lets people visually compare the relative heights of individual bars to get a reasonable estimate of their values. In contrast, defining a lower bound of zero can sometimes make meaningful differences between values more difficult to discern. For example, a heart rate chart that always uses zero for the lower bound could obscure important differences between resting and active readings because the differences occur in a range that’s far from zero.

Prefer familiar sequences of values in the tick and grid-line labels for an axis. For example, if you use a common number sequence like 0, 5, 10, etc., people are likely to know at a glance that each tick value equals the previous value plus five. Even though a sequence like 1, 6, 11, etc., follows the same rule, it’s not common, so most people are likely to spend extra time thinking about the interval between values.

Tailor the appearance of grid lines and labels to a chart’s use cases. Too many grid lines can be visually overwhelming, distracting people from the data; too few grid lines can make it difficult to estimate a mark’s value. To help you determine the appropriate density and visual weight of these elements, consider a chart’s context in the interface, the interactions you support, and the tasks people can do in the chart. For example, if people can inspect individual data points by interacting with a chart, you might use fewer grid lines and light label colors to ensure the data remains visually prominent.


## Descriptive content
Write descriptions that help people understand what a chart does before they view it. When you provide information-rich titles and labels that describe the purpose and functionality of a chart, you give people the context they need before they dive in and examine the details. Providing context in this way is especially important for VoiceOver users and those with certain types of cognitive disabilities because they rely on your descriptions to understand the purpose and primary message of your chart before they decide to investigate it further.

Summarize the main message of your chart to help make it approachable and useful for everyone. Although a primary reason to use a chart is to display the data that supports the main message, it’s essential to summarize key information so that people can grasp it quickly. For example, Weather provides a title and subtitle that succinctly describe the expected precipitation for the next hour, giving people the most important information without requiring them to examine the details of the chart.


## Best practices
Establish a consistent visual hierarchy that helps communicate the relative importance of various chart elements. Typically, you want the data itself to be most prominent, while letting the descriptions and axes provide additional context without competing with the data.

In a compact environment, maximize the width of the plot area to give people enough space to comfortably examine a chart. To help important data fit well in a given width, ensure that labels on a vertical axis are as short as possible without losing clarity. You might also consider describing units in other areas of the chart, such as in a title, and placing a longer axis label, such as a category name, inside the plot area when doing so doesn’t obscure important information.

Make every chart in your app accessible. Charts — like all infographics — need to be fully accessible to everyone, regardless of how they perceive content. For example, it’s essential to support VoiceOver, which describes onscreen content to help people get information and navigate without needing to see the screen (to learn more about VoiceOver, see Vision). In addition to supplying accessibility labels that describe the components of your chart, you can enhance the VoiceOver experience by also using Audio Graphs. Audio graphs provides chart information to VoiceOver, which constructs a set of tones that audibly represent a chart’s data values and their trend; it also lets you present high-level text summaries that provide additional context. For guidance, see Enhancing the accessibility of a chart.

Let people interact with the data when it makes sense, but don’t require interaction to reveal critical information. In Stocks, for example, people are often most interested in a stock’s performance over time, so the app displays a line graph that depicts performance during the time period people choose, such as one day, three months, or five years. If people want to explore additional details, they can drag a vertical indicator through the line graph, revealing the value at the selected time.

Make it easy for everyone to interact with a chart. Sometimes, chart marks are too small to target with a finger or a pointer, making your chart hard to use for people with reduced motor control and uncomfortable for everyone. When this is the case, consider expanding the hit target to include the entire plot area, letting people scrub across the area to reveal various values.

Make an interactive chart easy to navigate when using keyboard commands (including full keyboard access) or Switch Control. By default, these input types tend to visit individual onscreen elements in a linear sequence, such as the sequence of values in a data file. If you want to provide a custom navigation experience in your chart, here are two main ways to do so. The first way is to use accessibility APIs (such as accessibilityRespondsToUserInteraction(_:)) to specify a logical and predictable path through your chart’s information. For example, you might want to let people navigate along the X axis instead of jumping back and forth. The second way — which is particularly useful if you need to present a very large dataset — is to let people move focus among subsets of values instead of navigating through all individual data points. Note that both of these customizations can also enhance the VoiceOver experience, even when your chart isn’t interactive. For guidance, see Accessibility.

Help people notice important changes in a chart. For example, if people don’t notice when marks or axes change, they can misread a chart. Animating such changes can help people notice them, but you need to highlight the changes in other ways, too, to ensure that VoiceOver users and people who turn off animations know about them. For developer guidance, see UIAccessibility.Notification (UIKit) or NSAccessibility.Notification (AppKit).

Align a chart with surrounding interface elements. For example, it often works well to align the leading edge of a chart with the leading edge of other views in a screen. One way to maintain a clean leading edge in a chart is to display the label for each vertical grid line on its trailing side. You might also consider shifting the Y axis to the trailing side of the chart so that its tick labels don’t protrude past the chart’s leading edge. If you end up with a label that doesn’t appear to be associated with anything, you can use a tick to anchor it to a grid line.


## Color
As in all other parts of your interface, using color in a chart can help you clarify information, evoke your brand, and provide visual continuity. For general guidance on using color in ways that everyone can appreciate, see Inclusive color.

Avoid relying solely on color to differentiate between different pieces of data or communicate essential information in a chart. Using meaningful color in a chart works well to highlight differences and elevate key details, but it’s crucial to include alternative ways to convey this information so that people can use your chart regardless of whether they can discern colors. One way to supplement color is to use different shapes or patterns to depict different parts of data. For example, in addition to using red and black or red and white colors, Health uses two different shapes in the point marks that represent the two components of blood pressure.

Aid comprehension by adding visual separation between contiguous areas of color. For example, in a bar chart that stacks marks in a single row or column, it’s common to assign a different color to each mark. In this design, adding separators between the marks can help people distinguish individual ones.


## Enhancing the accessibility of a chart
When you use Swift Charts to create a chart, you get a default implementation of Audio graphs, in addition to a default accessibility element for each mark (or group of marks) that describes its value.

Consider using Audio Graphs to give VoiceOver users more information about your chart. You can customize the default Audio Graphs implementation that Swift Charts provides by supplying a chart title and descriptive summary that VoiceOver speaks to help people understand the purpose and main features of your chart. If you don’t use Audio Graphs, you need to provide an overview of the chart’s structure and purpose. For example, you need to identify the chart’s type — such as bar or line — explain what each axis represents, and describe details like the upper and lower axis bounds.

Unlike an image — which requires one descriptive accessibility label — a chart often needs to offer an accessibility label for each important or interactive element. Depending on the purpose of your chart and the scope and density of its marks, you need to decide whether it’s essential to describe each mark or whether it improves the accessibility experience to describe groups of marks. In some cases, it can make sense to use a single accessibility label that provides a succinct, high-level description of the chart, such as when you use a small version of a chart in a button that reveals a more detailed version.

Write accessibility labels that support the purpose of your chart. For example, Maps shows elevation for a cycling route using a chart that represents the change in elevation over the course of the route. The purpose of the chart is to give people a sense of the terrain for the entire route, not to provide individual elevations. For this reason, Maps provides accessibility labels that summarize the elevation changes over a portion of the route, rather than providing labels for each individual moment. In contrast, Health offers an accessibility label for each bar in the Steps chart, because the purpose of the chart is to give people their actual step count for each tracking period.

For the focused section of this cycling elevation chart, VoiceOver provides information about that portion of the route, including distance and elevation changes.

The following guidelines can help you write useful accessibility labels for chart elements.

- Avoid using subjective terms. Subjective words — like rapidly, gradually, and almost — communicate your interpretation of the data. To help people form their own interpretations, use actual values in your descriptions.
- Maximize clarity in data descriptions by avoiding potentially ambiguous formats and abbreviations. For example, using “June 6” is clearer than using “6/6”; similarly, spelling out “60 minutes” or “60 meters” is clearer than using the abbreviation “60m.”
- Describe what the chart’s details represent, not what they look like. Consider a chart that uses red and blue colors to help people visually distinguish two different data series. It’s crucial to create accessibility labels that identify what each series represents, but describing the colors that visually represent them can add unnecessary information and be distracting.
- Be consistent throughout your app when referring to a specific axis. For example, if you always mention the X axis first, people can spend less time figuring out which axis is relevant in a description.
Hide visible text labels for axes and ticks from assistive technologies. Axis and tick labels help people visually assess trends in a chart and estimate mark values. VoiceOver users can get mark values and trend information through accessibility labels and Audio Graphs, so they don’t generally need the content in the visible labels.


## Platform considerations
No additional considerations for iOS, iPadOS, macOS, tvOS, visionOS.


### watchOS
In general, avoid requiring complex chart interactions in your watchOS app. As much as possible, prefer displaying useful information people can get at a glance and supporting simple interactions when they add value. If you also offer a version of your app in another platform, consider using it to display more details and to support additional interactions with your chart. For example, Heart Rate in watchOS displays a chart of the wearer’s heart-rate data for the current day, whereas the Health app on iPhone displays heart-rate data for several different periods of time and lets people examine individual marks.


## Resources

#### Related

#### Developer documentation

#### Videos

## Change log

---

### Image Views

**Source**: [https://developer.apple.com/design/human-interface-guidelines/image-views](https://developer.apple.com/design/human-interface-guidelines/image-views)


# Image views
Within an image view, you can stretch, scale, size to fit, or pin the image to a specific location. Image views are typically not interactive.


## Best practices
Use an image view when the primary purpose of the view is simply to display an image. In rare cases where you might want an image to be interactive, configure a system-provided button to display the image instead of adding button behaviors to an image view.

If you want to display an icon in your interface, consider using a symbol or interface icon instead of an image view. SF Symbols provides a large library of streamlined, vector-based images that you can render with various colors and opacities. An icon (also called a glyph or template image) is typically a bitmap image in which the nontransparent pixels can receive color. Both symbols and interface icons can use the accent colors people choose.


## Content
An image view can contain rich image data in various formats, like PNG, JPEG, and PDF. For more guidance, see Images.

Take care when overlaying text on images. Compositing text on top of images can decrease both the clarity of the image and the legibility of the text. To help improve the results, ensure the text contrasts well with the image, and consider ways to make the text object stand out, like adding a text shadow or background layer.

Aim to use a consistent size for all images in an animated sequence. When you prescale images to fit the view, the system doesn’t have to perform any scaling. In cases where the system must do the scaling, performance is generally better when all images are the same size and shape.


## Platform considerations
No additional considerations for iOS or iPadOS.


### macOS
If your app needs an editable image view, use an image well. An image well is an image view that supports copying, pasting, dragging, and using the Delete key to clear its content.

Use an image button instead of an image view to make a clickable image. An image button contains an image or icon, appears in a view, and initiates an instantaneous app-specific action.


### tvOS
Many tvOS images combine multiple layers with transparency to create a feeling of depth. For guidance, see Layered images.


### visionOS
Windows in visionOS apps and games can use image views to display 2D and stereoscopic images, as well as spatial photos. If your app uses RealityKit, you can also display images of any type outside of image views next to 3D content, or generate a spatial scene from an existing 2D image. For design guidance, see Images > visionOS; for developer guidance, see ImagePresentationComponent.

For guidance on presenting other 3D content in a window or volume, see Windows > visionOS.


### watchOS
Use SwiftUI to create animations when possible. Alternatively, you can use WatchKit to animate a sequence of images within an image element if necessary. For developer guidance, see WKImageAnimatable.


## Resources

#### Related

#### Developer documentation

#### Videos

## Change log
Updated to include guidance for visionOS.


---

### Text Views

**Source**: [https://developer.apple.com/design/human-interface-guidelines/text-views](https://developer.apple.com/design/human-interface-guidelines/text-views)


# Text views
Text views can be any height and allow scrolling when the content extends outside of the view. By default, content within a text view is aligned to the leading edge and uses the system label color. In iOS, iPadOS, and visionOS, if a text view is editable, a keyboard appears when people select the view.


## Best practices
Use a text view when you need to display text that’s long, editable, or in a special format. Text views differ from text fields and labels in that they provide the most options for displaying specialized text and receiving text input. If you need to display a small amount of text, it’s simpler to use a label or — if the text is editable — a text field.

Keep text legible. Although you can use multiple fonts, colors, and alignments in creative ways, it’s essential to maintain the readability of your content. It’s a good idea to adopt Dynamic Type so your text still looks good if people change text size on their device. Be sure to test your content with accessibility options turned on, such as bold text. For guidance, see Accessibility and Typography.

Make useful text selectable. If a text view contains useful information such as an error message, a serial number, or an IP address, consider letting people select and copy it for pasting elsewhere.


## Platform considerations
No additional considerations for macOS, visionOS, or watchOS.


### iOS, iPadOS
Show the appropriate keyboard type. Several different keyboard types are available, each designed to facilitate a different type of input. To streamline data entry, the keyboard you display when editing a text view needs to be appropriate for the type of content. For guidance, see Virtual keyboards.


### tvOS
You can display text in tvOS using a text view. Because text input in tvOS is minimal by design, tvOS uses text fields for editable text instead.


## Resources

#### Related

#### Developer documentation

## Change log
Updated guidance to reflect changes in watchOS 10.


---

### Web Views

**Source**: [https://developer.apple.com/design/human-interface-guidelines/web-views](https://developer.apple.com/design/human-interface-guidelines/web-views)


# Web views
For example, Mail uses a web view to show HTML content in messages.


## Best practices
Support forward and back navigation when appropriate. Web views support forward and back navigation, but this behavior isn’t available by default. If people are likely to use your web view to visit multiple pages, allow forward and back navigation, and provide corresponding controls to initiate these features.

Avoid using a web view to build a web browser. Using a web view to let people briefly access a website without leaving the context of your app is fine, but Safari is the primary way people browse the web. Attempting to replicate the functionality of Safari in your app is unnecessary and discouraged.


## Platform considerations
No additional considerations for iOS, iPadOS, macOS, or visionOS. Not supported in tvOS or watchOS.


## Resources

#### Related

#### Developer documentation

#### Videos

---

## Layout and organization

### Boxes

**Source**: [https://developer.apple.com/design/human-interface-guidelines/boxes](https://developer.apple.com/design/human-interface-guidelines/boxes)


# Boxes
By default, a box uses a visible border or background color to separate its contents from the rest of the interface. A box can also include a title.


## Best practices
Prefer keeping a box relatively small in comparison with its containing view. As a box’s size gets close to the size of the containing window or screen, it becomes less effective at communicating the separation of grouped content, and it can crowd other content.

Consider using padding and alignment to communicate additional grouping within a box. A box’s border is a distinct visual element — adding nested boxes to define subgroups can make your interface feel busy and constrained.


## Content
Provide a succinct introductory title if it helps clarify the box’s contents. The appearance of a box helps people understand that its contents are related, but it might make sense to provide more detail about the relationship. Also, a title can help VoiceOver users predict the content they encounter within the box.

If you need a title, write a brief phrase that describes the contents. Use sentence-style capitalization. Avoid ending punctuation unless you use a box in a settings pane, where you append a colon to the title.


## Platform considerations
No additional considerations for visionOS. Not supported in tvOS or watchOS.


### iOS, iPadOS
By default, iOS and iPadOS use the secondary and tertiary background colors in boxes.


### macOS
By default, macOS displays a box’s title above it.


## Resources

#### Related

#### Developer documentation

---

### Collections

**Source**: [https://developer.apple.com/design/human-interface-guidelines/collections](https://developer.apple.com/design/human-interface-guidelines/collections)


# Collections
Generally speaking, collections are ideal for showing image-based content.


## Best practices
Use the standard row or grid layout whenever possible. Collections display content by default in a horizontal row or a grid, which are simple, effective appearances that people expect. Avoid creating a custom layout that might confuse people or draw undue attention to itself.

Consider using a table instead of a collection for text. It’s generally simpler and more efficient to view and digest textual information when it’s displayed in a scrollable list.

Make it easy to choose an item. If it’s too difficult to get to an item in your collection, people will get frustrated and lose interest before reaching the content they want. Use adequate padding around images to keep focus or hover effects easy to see and prevent content from overlapping.

Add custom interactions when necessary. By default, people can tap to select, touch and hold to edit, and swipe to scroll. If your app requires it, you can add more gestures for performing custom actions.

Consider using animations to provide feedback when people insert, delete, or reorder items. Collections support standard animations for these actions, and you can also use custom animations.


## Platform considerations
No additional considerations for macOS, tvOS, or visionOS. Not supported in watchOS.


### iOS, iPadOS
Use caution when making dynamic layout changes. The layout of a collection can change dynamically. Be sure any changes make sense and are easy to track. If possible, try to avoid changing the layout while people are viewing and interacting with it, unless it’s in response to an explicit action.


## Resources

#### Related

#### Developer documentation
UICollectionView — UIKit

NSCollectionView — AppKit


---

### Column Views

**Source**: [https://developer.apple.com/design/human-interface-guidelines/column-views](https://developer.apple.com/design/human-interface-guidelines/column-views)


# Column views
Each column represents one level of the hierarchy and contains horizontal rows of data items. Within a column, any parent item that contains nested child items is marked with a triangle icon. When people select a parent, the next column displays its children. People can continue navigating in this way until they reach an item with no children, and can also navigate back up the hierarchy to explore other branches of data.

If you need to manage the presentation of hierarchical content in your iPadOS or visionOS app, consider using a split view.


## Best practices
Consider using a column view when you have a deep data hierarchy in which people tend to navigate back and forth frequently between levels, and you don’t need the sorting capabilities that a list or table provides. For example, Finder offers a column view (in addition to icon, list, and gallery views) for navigating directory structures.

Show the root level of your data hierarchy in the first column. People know they can quickly scroll back to the first column to begin navigating the hierarchy from the top again.

Consider showing information about the selected item when there are no nested items to display. The Finder, for example, shows a preview of the selected item and information like the creation date, modification date, file type, and size.

Let people resize columns. This is especially important if the names of some data items are too long to fit within the default column width.


## Platform considerations
Not supported in iOS, iPadOS, tvOS, visionOS, or watchOS.


## Resources

#### Related

#### Developer documentation

---

### Disclosure Controls

**Source**: [https://developer.apple.com/design/human-interface-guidelines/disclosure-controls](https://developer.apple.com/design/human-interface-guidelines/disclosure-controls)


# Disclosure controls

## Best practices
Use a disclosure control to hide details until they’re relevant. Place controls that people are most likely to use at the top of the disclosure hierarchy so they’re always visible, with more advanced functionality hidden by default. This organization helps people quickly find the most essential information without overwhelming them with too many detailed options.


## Disclosure triangles
A disclosure triangle shows and hides information and functionality associated with a view or a list of items. For example, Keynote uses a disclosure triangle to show advanced options when exporting a presentation, and the Finder uses disclosure triangles to progressively reveal hierarchy when navigating a folder structure in list view.

- Collapsed
- Expanded
A disclosure triangle points inward from the leading edge when its content is hidden and down when its content is visible. Clicking or tapping the disclosure triangle switches between these two states, and the view expands or collapses accordingly to accommodate the content.

Provide a descriptive label when using a disclosure triangle. Make sure your labels indicate what is disclosed or hidden, like “Advanced Options.”

For developer guidance, see NSButton.BezelStyle.disclosure.


## Disclosure buttons
A disclosure button shows and hides functionality associated with a specific control. For example, the macOS Save sheet shows a disclosure button next to the Save As text field. When people click or tap this button, the Save dialog expands to give advanced navigation options for selecting an output location for their document.

A disclosure button points down when its content is hidden and up when its content is visible. Clicking or tapping the disclosure button switches between these two states, and the view expands or collapses accordingly to accommodate the content.

Place a disclosure button near the content that it shows and hides. Establish a clear relationship between the control and the expanded choices that appear when a person clicks or taps a button.

Use no more than one disclosure button in a single view. Multiple disclosure buttons add complexity and can be confusing.

For developer guidance, see NSButton.BezelStyle.pushDisclosure.


## Platform considerations
No additional considerations for macOS. Not supported in tvOS or watchOS.


### iOS, iPadOS, visionOS
Disclosure controls are available in iOS, iPadOS, and visionOS with the SwiftUI DisclosureGroup view.


## Resources

#### Related

#### Developer documentation
DisclosureGroup — SwiftUI

NSButton.BezelStyle.disclosure — AppKit

NSButton.BezelStyle.pushDisclosure — AppKit


#### Videos

---

### Labels

**Source**: [https://developer.apple.com/design/human-interface-guidelines/labels](https://developer.apple.com/design/human-interface-guidelines/labels)


# Labels
Labels display text throughout the interface, in buttons, menu items, and views, helping people understand the current context and what they can do next.

The term label refers to uneditable text that can appear in various places. For example:

- Within a button, a label generally conveys what the button does, such as Edit, Cancel, or Send.
- Within many lists, a label can describe each item, often accompanied by a symbol or an image.
- Within a view, a label might provide additional context by introducing a control or describing a common action or task that people can perform in the view.
To display uneditable text, SwiftUI defines two components: Label and Text.

The guidance below can help you use a label to display text. In some cases, guidance for specific components — such as action buttons, menus, and lists and tables — includes additional recommendations for using text.


## Best practices
Use a label to display a small amount of text that people don’t need to edit. If you need to let people edit a small amount of text, use a text field. If you need to display a large amount of text, and optionally let people edit it, use a text view.

Prefer system fonts. A label can display plain or styled text, and it supports Dynamic Type (where available) by default. If you adjust the style of a label or use custom fonts, make sure the text remains legible.

Use system-provided label colors to communicate relative importance. The system defines four label colors that vary in appearance to help you give text different levels of visual importance. For additional guidance, see Color.

iOS, iPadOS, tvOS, visionOS

A subheading or supplemental text

Text that describes an unavailable item or behavior

Make useful label text selectable. If a label contains useful information — like an error message, a location, or an IP address — consider letting people select and copy it for pasting elsewhere.


## Platform considerations
No additional considerations for iOS, iPadOS, tvOS, or visionOS.


### macOS
To display uneditable text in a label, use the isEditable property of NSTextField.


### watchOS
Date and time text components (shown below on the left) display the current date, the current time, or a combination of both. You can configure a date text component to use a variety of formats, calendars, and time zones. A countdown timer text component (shown below on the right) displays a precise countdown or count-up timer. You can configure a timer text component to display its count value in a variety of formats.

When you use the system-provided date and timer text components, watchOS automatically adjusts the label’s presentation to fit the available space. The system also updates the content without further input from your app.

Consider using date and timer components in complications. For design guidance, see Complications; for developer guidance, see Text.


## Resources

#### Related

#### Developer documentation

## Change log
Updated guidance to reflect changes in watchOS 10.


---

### Lists And Tables

**Source**: [https://developer.apple.com/design/human-interface-guidelines/lists-and-tables](https://developer.apple.com/design/human-interface-guidelines/lists-and-tables)


# Lists and tables
A table or list can represent data that’s organized in groups or hierarchies, and it can support user interactions like selecting, adding, deleting, and reordering. Apps and games in all platforms can use tables to present content and options; many apps use lists to express an overall information hierarchy and help people navigate it. For example, iOS Settings uses a hierarchy of lists to help people choose options, and several apps — such as Mail in iPadOS and macOS — use a table within a split view.

Sometimes, people need to work with complex data in a multicolumn table or a spreadsheet. Apps that offer productivity tasks often use a table to represent various characteristics or attributes of the data in separate, sortable columns.


## Best practices
Prefer displaying text in a list or table. A table can include any type of content, but the row-based format is especially well suited to making text easy to scan and read. If you have items that vary widely in size — or you need to display a large number of images — consider using a collection instead.

Let people edit a table when it makes sense. People appreciate being able to reorder a list, even if they can’t add or remove items. In iOS and iPadOS, people must enter an edit mode before they can select table items.

Provide appropriate feedback when people select a list item. The feedback can vary depending on whether selecting the item reveals a new view or toggles the item’s state. In general, a table that helps people navigate through a hierarchy persistently highlights the selected row to clarify the path people are taking. In contrast, a table that lists options often highlights a row only briefly before adding an image — such as a checkmark — indicating that the item is selected.


## Content
Keep item text succinct so row content is comfortable to read. Short, succinct text can help minimize truncation and wrapping, making text easier to read and scan. If each item consists of a large amount of text, consider alternatives that help you avoid displaying over-large table rows. For example, you could list item titles only, letting people choose an item to reveal its content in a detail view.

Consider ways to preserve readability of text that might otherwise get clipped or truncated. When a table is narrow — for example, if people can vary its width — you want content to remain recognizable and easy to read. Sometimes, an ellipsis in the middle of text can make an item easier to distinguish because it preserves both the beginning and the end of the content.

Use descriptive column headings in a multicolumn table. Use nouns or short noun phrases with title-style capitalization, and don’t add ending punctuation. If you don’t include a column heading in a single-column table view, use a label or a header to help people understand the context.


## Style
Choose a table or list style that coordinates with your data and platform. Some styles use visual details to help communicate grouping and hierarchy or to provide specific experiences. In iOS and iPadOS, for example, the grouped style uses headers, footers, and additional space to separate groups of data; the elliptical style available in watchOS makes items appear as if they’re rolling off a rounded surface as people scroll; and macOS defines a bordered style that uses alternating row backgrounds to help make large tables easier to use. For developer guidance, see ListStyle.

Choose a row style that fits the information you need to display. For example, you might need to display a small image in the leading end of a row, followed by a brief explanatory label. Some platforms provide built-in row styles you can use to arrange content in list rows, such as the UIListContentConfiguration API you can use to lay out content in a list’s rows, headers, and footers in iOS, iPadOS, and tvOS.


## Platform considerations

### iOS, iPadOS, visionOS
Use an info button only to reveal more information about a row’s content. An info button — called a detail disclosure button when it appears in a list row — doesn’t support navigation through a hierarchical table or list. If you need to let people drill into a list or table row’s subviews, use a disclosure indicator accessory control. For developer guidance, see UITableViewCell.AccessoryType.disclosureIndicator.

An info button shows details about a list item; it doesn’t support navigation.

A disclosure indicator reveals the next level in a hierarchy; it doesn’t show details about the item.

Avoid adding an index to a table that displays controls — like disclosure indicators — in the trailing ends of its rows. An index typically consists of the letters in an alphabet, displayed vertically at the trailing side of a list. People can jump to a specific section in the list by choosing the index letter that maps to it. Because both the index and elements like disclosure indicators appear on the trailing side of a list, it can be difficult for people to use one element without activating the other.


### macOS
When it provides value, let people click a column heading to sort a table view based on that column. If people click the heading of a column that’s already sorted, re-sort the data in the opposite direction.

Let people resize columns. Data displayed in a table view often varies in width. People appreciate resizing columns to help them concentrate on different areas or reveal clipped data.

Consider using alternating row colors in a multicolumn table. Alternating colors can help people track row values across columns, especially in a wide table.

Use an outline view instead of a table view to present hierarchical data. An outline view looks like a table view, but includes disclosure triangles for exposing nested levels of data. For example, an outline view might display folders and the items they contain.


### tvOS
Confirm that images near a table still look good as each row highlights and slightly increases in size when it becomes focused. A focused row’s corners can also become rounded, which may affect the appearance of images on either side of it. Account for this effect as you prepare images, and don’t add your own masks to round the corners.


### watchOS
When possible, limit the number of rows. Short lists are easier for people to scan, but sometimes people expect a long list of items. For example, if people subscribe to a large number of podcasts, they might think something’s wrong if they can’t view all their items. You can help make a long list more manageable by listing the most relevant items and providing a way for people to view more.

Constrain the length of detail views if you want to support vertical page-based navigation. People use vertical page-based navigation to swipe vertically among the detail items of different list rows. Navigating in this way saves time because people don’t need to return to the list to tap a new detail item, but it works only when detail views are short. If your detail views scroll, people won’t be able to use vertical page-based navigation to swipe among them.


## Resources

#### Related

#### Developer documentation

#### Videos

## Change log
Updated to include guidance for visionOS.

Updated guidance to reflect changes in watchOS 10.


---

### Lockups

**Source**: [https://developer.apple.com/design/human-interface-guidelines/lockups](https://developer.apple.com/design/human-interface-guidelines/lockups)


# Lockups
Each lockup consists of a content view, a header, and a footer. Headers appear above the main content for a lockup, and footers appear below the main content. All three views expand and contract together as the lockup gets focus.

According to the needs of your app, you can combine four types of lockup: cards, caption buttons, monograms, and posters.


## Best practices
Allow adequate space between lockups. A focused lockup expands in size, so leave enough room between lockups to avoid overlapping or displacing other lockups. For guidance, see Layout.

Use consistent lockup sizes within a row or group. A group of buttons or a row of content images is more visually appealing when the widths and heights of all elements match.

For developer guidance, see TVLockupView and TVLockupHeaderFooterView.


## Cards
A card combines a header, footer, and content view to present ratings and reviews for media items.

For developer guidance, see TVCardView.


## Caption buttons
A caption button can include a title and a subtitle beneath the button. A caption button can contain either an image or text.

Make sure that when people focus on them, caption buttons tilt with the motion that they swipe. When aligned vertically, caption buttons tilt up and down. When aligned horizontally, caption buttons tilt left and right. When displayed in a grid, caption buttons tilt both vertically and horizontally.

For developer guidance, see TVCaptionButtonView.


## Monograms
Monograms identify people, usually the cast and crew for a media item. Each monogram consists of a circular picture of the person and their name. If an image isn’t available, the person’s initials appear in place of an image.

Prefer images over initials. An image of a person creates a more intimate connection than text.

For developer guidance, see TVMonogramContentView.


## Posters
Posters consist of an image and an optional title and subtitle, which are hidden until the poster comes into focus. Posters can be any size, but the size needs to be appropriate for their content. For related guidance, see Image views.

For developer guidance, see TVPosterView.


## Platform considerations
Not supported in iOS, iPadOS, macOS, visionOS, or watchOS.


## Resources

#### Related

#### Developer documentation
TVLockupView — TVUIKit

TVLockupHeaderFooterView — TVUIKit


---

### Outline Views

**Source**: [https://developer.apple.com/design/human-interface-guidelines/outline-views](https://developer.apple.com/design/human-interface-guidelines/outline-views)


# Outline views
An outline view includes at least one column that contains primary hierarchical data, such as a set of parent containers and their children. You can add columns, as needed, to display attributes that supplement the primary data; for example, sizes and modification dates. Parent containers have disclosure triangles that expand to reveal their children.

Finder windows offer an outline view for navigating the file system.


## Best practices
Outline views work well to display text-based content and often appear in the leading side of a split view, with related content on the opposite side.

Use a table instead of an outline view to present data that’s not hierarchical. For guidance, see Lists and tables.

Expose data hierarchy in the first column only. Other columns can display attributes that apply to the hierarchical data in the primary column.

Use descriptive column headings to provide context. Use nouns or short noun phrases with title-style capitalization and no punctuation; in particular, avoid adding a trailing colon. Always provide column headings in a multi-column outline view. If you don’t include a column heading in a single-column outline view, use a label or other means to make sure there’s enough context.

Consider letting people click column headings to sort an outline view. In a sortable outline view, people can click a column heading to perform an ascending or descending sort based on that column. You can implement additional sorting based on secondary columns behind the scenes, if necessary. If people click the primary column heading, sorting occurs at each hierarchy level. For example, in the Finder, all top-level folders are sorted, then the items within each folder are sorted. If people click the heading of a column that’s already sorted, the folders and their contents are sorted again in the opposite direction.

Let people resize columns. Data displayed in an outline view often varies in width. It’s important to let people adjust column width as needed to reveal data that’s wider than the column.

Make it easy for people to expand or collapse nested containers. For example, clicking a disclosure triangle for a folder in a Finder window expands only that folder. However, Option-clicking the disclosure triangle expands all of its subfolders.

Retain people’s expansion choices. If people expand various levels of an outline view to reach a specific item, store the state so you can display it again the next time. This way, people won’t need to navigate back to the same place again.

Consider using alternating row colors in multi-column outline views. Alternating colors can make it easier for people to track row values across columns, especially in wide outline views.

Let people edit data if it makes sense in your app. In an editable outline view cell, people expect to be able to single-click a cell to edit its contents. Note that a cell can respond differently to a double click. For example, an outline view listing files might let people single-click a file’s name to edit it, but double-click a file’s name to open the file. You can also let people reorder, add, and remove rows if it would be useful.

Consider using a centered ellipsis to truncate cell text instead of clipping it. An ellipsis in the middle preserves the beginning and end of the cell text, which can make the content more distinct and recognizable than clipped text.

Consider offering a search field to help people find values quickly in a lengthy outline view. Windows with an outline view as the primary feature often include a search field in the toolbar. For guidance, see Search fields.


## Platform considerations
Not supported in iOS, iPadOS, tvOS, visionOS, or watchOS.


## Resources

#### Related

#### Developer documentation
OutlineGroup — SwiftUI

NSOutlineView — AppKit


#### Videos

---

### Split Views

**Source**: [https://developer.apple.com/design/human-interface-guidelines/split-views](https://developer.apple.com/design/human-interface-guidelines/split-views)


# Split views
Typically, you use a split view to show multiple levels of your app’s hierarchy at once and support navigation between them. In this scenario, selecting an item in the view’s primary pane displays the item’s contents in the secondary pane. Similarly, a split view can display a tertiary pane if items in the secondary pane contain additional content.

It’s common to use a split view to display a sidebar for navigation, where the leading pane lists the top-level items or collections in an app, and the secondary and optional tertiary panes can present child collections and item details. Rarely, you might also use a split view to provide groups of functionality that supplement the primary view — for example, Keynote in macOS uses split view panes to present the slide navigator, the presenter notes, and the inspector pane in areas that surround the main slide canvas.


## Best practices
To support navigation, persistently highlight the current selection in each pane that leads to the detail view. The selected appearance clarifies the relationship between the content in various panes and helps people stay oriented.

Consider letting people drag and drop content between panes. Because a split view provides access to multiple levels of hierarchy, people can conveniently move content from one part of your app to another by dragging items to different panes. For guidance, see Drag and drop.


## Platform considerations

### iOS
Prefer using a split view in a regular — not a compact — environment. A split view needs horizontal space in which to display multiple panes. In a compact environment, such as iPhone in portrait orientation, it’s difficult to display multiple panes without wrapping or truncating the content, making it less legible and harder to interact with.


### iPadOS
In iPadOS, a split view can include either two vertical panes, like Mail, or three vertical panes, like Keynote.

Account for narrow, compact, and intermediate window widths. Since iPad windows are fluidly resizable, it’s important to consider the design of a split view layout at multiple widths. In particular, ensure that it’s possible to navigate between the various panes in a logical way. For guidance, see Layout. For developer guidance, see NavigationSplitView and UISplitViewController.


### macOS
In macOS, you can arrange the panes of a split view vertically, horizontally, or both. A split view includes dividers between panes that can support dragging to resize them. For developer guidance, see VSplitView and HSplitView.

- Vertical
- Horizontal
- Multiple
Set reasonable defaults for minimum and maximum pane sizes. If people can resize the panes in your app’s split view, make sure to use sizes that keep the divider visible. If a pane gets too small, the divider can seem to disappear, becoming difficult to use.

Consider letting people hide a pane when it makes sense. If your app includes an editing area, for example, consider letting people hide other panes to reduce distractions or allow more room for editing — in Keynote, people can hide the navigator and presenter notes panes when they want to edit slide content.

Provide multiple ways to reveal hidden panes. For example, you might provide a toolbar button or a menu command — including a keyboard shortcut — that people can use to restore a hidden pane.

Prefer the thin divider style. The thin divider measures one point in width, giving you maximum space for content while remaining easy for people to use. Avoid using thicker divider styles unless you have a specific need. For example, if both sides of a divider present table rows that use strong linear elements that might make a thin divider hard to distinguish, it might work to use a thicker divider. For developer guidance, see NSSplitView.DividerStyle.


### tvOS
In tvOS, a split view can work well to help people filter content. When people choose a filter category in the primary pane, your app can display the results in the secondary pane.

Choose a split view layout that keeps the panes looking balanced. By default, a split view devotes a third of the screen width to the primary pane and two-thirds to the secondary pane, but you can also specify a half-and-half layout.

Display a single title above a split view, helping people understand the content as a whole. People already know how to use a split view to navigate and filter content; they don’t need titles that describe what each pane contains.

Choose the title’s alignment based on the type of content the secondary pane contains. Specifically, when the secondary pane contains a content collection, consider centering the title in the window. In contrast, if the secondary pane contains a single main view of important content, consider placing the title above the primary view to give the content more room.


### visionOS
To display supplementary information, prefer a split view instead of a new window. A split view gives people convenient access to more information without leaving the current context, whereas a new window may confuse people who are trying to navigate or reposition content. Opening more windows also requires you to carefully manage the relationship between views in your app or game. If you need to request a small amount of information or present a simple task that someone must complete before returning to their main task, use a sheet.


### watchOS
In watchOS, the split view displays either the list view or a detail view as a full-screen view.

Automatically display the most relevant detail view. When your app launches, show people the most pertinent information. For example, display information relevant to their location, the time, or their recent actions.

If your app displays multiple detail pages, place the detail views in a vertical tab view. People can then use the Digital Crown to scroll between the detail view’s tabs. watchOS also displays a page indicator next to the Digital Crown, indicating the number of tabs and the currently selected tab.


## Resources

#### Related

#### Developer documentation
NavigationSplitView — SwiftUI

UISplitViewController — UIKit

NSSplitViewController — AppKit


#### Videos

## Change log
Added iOS and iPadOS platform considerations.

Added guidance for split views in visionOS.

Added guidance for split views in watchOS.


---

### Tab Views

**Source**: [https://developer.apple.com/design/human-interface-guidelines/tab-views](https://developer.apple.com/design/human-interface-guidelines/tab-views)


# Tab views

## Best practices
Use a tab view to present closely related areas of content. The appearance of a tab view provides a strong visual indication of enclosure. People expect each tab to display content that is in some way similar or related to the content in the other tabs.

Make sure the controls within a pane affect content only in the same pane. Panes are mutually exclusive, so ensure they’re fully self-contained.

Provide a label for each tab that describes the contents of its pane. A good label helps people predict the contents of a pane before clicking or tapping its tab. In general, use nouns or short noun phrases for tab labels. A verb or short verb phrase may make sense in some contexts. Use title-style capitalization for tab labels.

Avoid using a pop-up button to switch between tabs. A tabbed control is efficient because it requires a single click or tap to make a selection, whereas a pop-up button requires two. A tabbed control also presents all choices onscreen at the same time, whereas people must click a pop-up button to see its choices. Note that a pop-up button can be a reasonable alternative in cases where there are too many panes of content to reasonably display with tabs.

Avoid providing more than six tabs in a tab view. Having more than six tabs can be overwhelming and create layout issues. If you need to present six or more tabs, consider another way to implement the interface. For example, you could instead present each tab as a view option in a pop-up button menu.

For developer guidance, see NSTabView.


## Anatomy
The tabbed control appears on the top edge of the content area. You can choose to hide the control, which is appropriate for an app that switches between panes programmatically.

When you hide the tabbed control, the content area can be borderless, bezeled, or bordered with a line. A borderless view can be solid or transparent.

In general, inset a tab view by leaving a margin of window-body area on all sides of a tab view. This layout looks clean and leaves room for additional controls that aren’t directly related to the contents of the tab view. You can extend a tab view to meet the window edges, but this layout is unusual.


## Platform considerations
Not supported in iOS, iPadOS, tvOS, or visionOS.


### iOS, iPadOS
For similar functionality, consider using a segmented control instead.


### watchOS
watchOS displays tab views using page controls. For developer guidance, see TabView and verticalPage.


## Resources

#### Related

#### Developer documentation

## Change log
Added guidance for using tab views in watchOS.


---

## Menus and actions

### Activity Views

**Source**: [https://developer.apple.com/design/human-interface-guidelines/activity-views](https://developer.apple.com/design/human-interface-guidelines/activity-views)


# Activity views
Activity views present sharing activities like messaging and actions like Copy and Print, in addition to quick access to frequently used apps. People typically reveal a share sheet by choosing an Action button while viewing a page or document, or after they’ve selected an item. An activity view can appear as a sheet or a popover, depending on the device and orientation.

You can provide app-specific activities that can appear in a share sheet when people open it within your app or game. For example, Photos provides app-specific actions like Copy Photo, Add to Album, and Adjust Location. By default, the system lists app-specific actions before actions — such as Add to Files or AirPlay — that are available in multiple apps or throughout the system. People can edit the list of actions to ensure that it displays the ones they use most and to add new ones.

You can also create app extensions to provide custom share and action activities that people can use in other apps. (An app extension is code you provide that people can install and use outside of your app.) For example, you might create a custom share activity that people can install to help them share a webpage with a specific social media service. Even though macOS doesn’t provide an activity view, you can create share and action app extensions that people can use on a Mac. For guidance, see Share and action extensions.


## Best practices
Avoid creating duplicate versions of common actions that are already available in the activity view. For example, providing a duplicate Print action is unnecessary and confusing because people wouldn’t know how to distinguish your action from the system-provided one. If you need to provide app-specific functionality that’s similar to an existing action, give it a custom title. For example, if you let people use custom formatting to print a bank transaction, use a title that helps people understand what your print activity does, like “Print Transaction.”

Consider using a symbol to represent your custom activity. SF Symbols provides a comprehensive set of configurable symbols you can use to communicate items and concepts in an activity view. If you need to create a custom interface icon, center it in an area measuring about 70x70 pixels. For guidance, see Icons.

Write a succinct, descriptive title for each custom action you provide. If a title is too long, the system wraps it and may truncate it. Prefer a single verb or a brief verb phrase that clearly communicates what the action does. Avoid including your company or product name in an action title. In contrast, the share sheet displays the title of a share activity — typically a company name — below the icon that represents it.

Make sure activities are appropriate for the current context. Although you can’t reorder system-provided tasks in an activity view, you can exclude tasks that aren’t applicable to your app. For example, if it doesn’t make sense to print from within your app, you can exclude the Print activity. You can also identify which custom tasks to show at any given time.

Use the Share button to display an activity view. People are accustomed to accessing system-provided activities when they choose the Share button. Avoid confusing people by providing an alternative way to do the same thing.


## Share and action extensions
Share extensions give people a convenient way to share information from the current context with apps, social media accounts, and other services. Action extensions let people initiate content-specific tasks — like adding a bookmark, copying a link, editing an inline image, or displaying selected text in another language — without leaving the current context.

The system presents share and action extensions differently depending on the platform:

- In iOS and iPadOS, share and action extensions are displayed in the share sheet that appears when people choose an Action button.
- In macOS, people access share extensions by clicking a Share button in the toolbar or choosing Share in a context menu. People can access an action extension by holding the pointer over certain types of embedded content — like an image they add to a Mail compose window — clicking a toolbar button, or choosing a quick action in a Finder window.
If necessary, create a custom interface that feels familiar to people. For a share extension, prefer the system-provided composition view because it provides a consistent sharing experience that people already know. For an action extension, include your app name. If you need to present an interface, include elements of your app’s interface to help people understand that your extension and your app are related.

Streamline and limit interaction. People appreciate extensions that let them perform a task in just a few steps. For example, a share extension might immediately post an image to a social media account with a single tap or click.

Avoid placing a modal view above your extension. By default, the system displays an extension within a modal view. While it might be necessary to display an alert above an extension, avoid displaying additional modal views.

If necessary, provide an image that communicates the purpose of your extension. A share extension automatically uses your app icon, helping give people confidence that your app provided the extension. For an action extension, prefer using a symbol or creating an interface icon that clearly identifies the task.

Use your main app to denote the progress of a lengthy operation. An activity view dismisses immediately after people complete the task in your share or action extension. If a task is time-consuming, continue it in the background, and give people a way to check the status in your main app. Although you can use a notification to tell people about a problem, don’t notify them simply because the task completes.


## Platform considerations
No additional considerations for iOS, iPadOS, or visionOS. Not supported in macOS, tvOS, or watchOS.


## Resources

#### Related

#### Developer documentation
UIActivityViewController — UIKit

App Extension Support — Foundation


#### Videos

---

### Buttons

**Source**: [https://developer.apple.com/design/human-interface-guidelines/buttons](https://developer.apple.com/design/human-interface-guidelines/buttons)


# Buttons
Versatile and highly customizable, buttons give people simple, familiar ways to do tasks in your app. In general, a button combines three attributes to clearly communicate its function:

- Style. A visual style based on size, color, and shape.
- Content. A symbol (or icon), text label, or both that a button displays to convey its purpose.
- Role. A system-defined role that identifies a button’s semantic meaning and can affect its appearance.
There are also many button-like components that have distinct appearances and behaviors for specific use cases, like toggles, pop-up buttons, and segmented controls.


## Best practices
When buttons are instantly recognizable and easy to understand, an app tends to feel intuitive and well designed.

Make buttons easy for people to use. It’s essential to include enough space around a button so that people can visually distinguish it from surrounding components and content. Giving a button enough space is also critical for helping people select or activate it, regardless of the method of input they use. As a general rule, a button needs a hit region of at least 44x44 pt — in visionOS, 60x60 pt — to ensure that people can select it easily, whether they use a fingertip, a pointer, their eyes, or a remote.

Always include a press state for a custom button. Without a press state, a button can feel unresponsive, making people wonder if it’s accepting their input.


## Style
System buttons offer a range of styles that support customization while providing built-in interaction states, accessibility support, and appearance adaptation. Different platforms define different styles that help you communicate hierarchies of actions in your app.

In general, use a button that has a prominent visual style for the most likely action in a view. To draw people’s attention to a specific button, use a prominent button style so the system can apply an accent color to the button’s background. Buttons that use color tend to be the most visually distinctive, helping people quickly identify the actions they’re most likely to use. Keep the number of prominent buttons to one or two per view. Presenting too many prominent buttons increases cognitive load, requiring people to spend more time considering options before making a choice.

Use style — not size — to visually distinguish the preferred choice among multiple options. When you use buttons of the same size to offer two or more options, you signal that the options form a coherent set of choices. By contrast, placing two buttons of different sizes near each other can make the interface look confusing and inconsistent. If you want to highlight the preferred or most likely option in a set, use a more prominent button style for that option and a less prominent style for the remaining ones.

Avoid applying a similar color to button labels and content layer backgrounds. If your app already has bright, colorful content in the content layer, prefer using the default monochromatic appearance of button labels. For more guidance, see Liquid Glass color.


## Content
Ensure that each button clearly communicates its purpose. Depending on the platform, a button can contain a symbol (or icon), a text label, or both to help people understand what it does.

In macOS and visionOS, the system displays a tooltip after people hover over a button for a moment. A tooltip displays a brief phrase that explains what a button does; for guidance, see Offering help.

Try to associate familiar actions with familiar icons. For example, people can predict that a button containing the square.and.arrow.up symbol will help them perform share-related activities. If it makes sense to use an icon in your button, consider using an existing or customized symbol. For a list of symbols that represent common actions, see Standard icons.

Consider using text when a short label communicates more clearly than an icon. To use text, write a few words that succinctly describe what the button does. Using title-style capitalization, consider starting the label with a verb to help convey the button’s action — for example, a button that lets people add items to their shopping cart might use the label “Add to Cart.”


## Role
A system button can have one of the following roles:

- Normal. No specific meaning.
- Primary. The button is the default button — the button people are most likely to choose.
- Cancel. The button cancels the current action.
- Destructive. The button performs an action that can result in data destruction.
A button’s role can have additional effects on its appearance. For example, a primary button uses an app’s accent color, whereas a destructive button uses the system red color.

Assign the primary role to the button people are most likely to choose. When a primary button responds to the Return key, it makes it easy for people to quickly confirm their choice. In addition, when the button is in a temporary view — like a sheet, an editable view, or an alert — assigning it the primary role means that the view can automatically close when people press Return.

Don’t assign the primary role to a button that performs a destructive action, even if that action is the most likely choice. Because of its visual prominence, people sometimes choose a primary button without reading it first. Help people avoid losing content by assigning the primary role to nondestructive buttons.


## Platform considerations
No additional considerations for tvOS.


### iOS, iPadOS
Configure a button to display an activity indicator when you need to provide feedback about an action that doesn’t instantly complete. Displaying an activity indicator within a button can save space in your user interface while clearly communicating the reason for the delay. To help clarify what’s happening, you can also configure the button to display a different label alongside the activity indicator. For example, the label “Checkout” could change to “Checking out…” while the activity indicator is visible. When a delay occurs after people click or tap your configured button, the system displays the activity indicator next to the original or alternative label, hiding the button image, if there is one.


### macOS
Several specific button types are unique to macOS.


#### Push buttons
The standard button type in macOS is known as a push button. You can configure a push button to display text, a symbol, an icon, or an image, or a combination of text and image content. Push buttons can act as the default button in a view and you can tint them.

Use a flexible-height push button only when you need to display tall or variable height content. Flexible-height buttons support the same configurations as regular push buttons — and they use the same corner radius and content padding — so they look consistent with other buttons in your interface. If you need to present a button that contains two lines of text or a tall icon, use a flexible-height button; otherwise, use a standard push button. For developer guidance, see NSButton.BezelStyle.flexiblePush.

Append a trailing ellipsis to the title when a push button opens another window, view, or app. Throughout the system, an ellipsis in a control title signals that people can provide additional input. For example, the Edit buttons in the AutoFill pane of Safari Settings include ellipses because they open other views that let people modify autofill values.

Consider supporting spring loading. On systems with a Magic Trackpad, spring loading lets people activate a button by dragging selected items over it and force clicking — that is, pressing harder — without dropping the selected items. After force clicking, people can continue dragging the items, possibly to perform additional actions.


#### Square buttons
A square button (also known as a gradient button) initiates an action related to a view, like adding or removing rows in a table.

Square buttons contain symbols or icons — not text — and you can configure them to behave like push buttons, toggles, or pop-up buttons. The buttons appear in close proximity to their associated view — usually within or beneath it — so people know which view the buttons affect.

Use square buttons in a view, not in the window frame. Square buttons aren’t intended for use in toolbars or status bars. If you need a button in a toolbar, use a toolbar item.

Prefer using a symbol in a square button. SF Symbols provides a wide range of symbols that automatically receive appropriate coloring in their default state and in response to user interaction.

Avoid using labels to introduce square buttons. Because square buttons are closely connected with a specific view, their purpose is generally clear without the need for descriptive text.

For developer guidance, see NSButton.BezelStyle.smallSquare.


#### Help buttons
A help button appears within a view and opens app-specific help documentation.

Help buttons are circular, consistently sized buttons that contain a question mark. For guidance on creating help documentation, see Offering help.

Use the system-provided help button to display your help documentation. People are familiar with the appearance of the standard help button and know that choosing it opens help content.

When possible, open the help topic that’s related to the current context. For example, the help button in the Rules pane of Mail settings opens the Mail User Guide to a help topic that explains how to change these settings. If no specific help topic applies directly to the current context, open the top level of your app’s help documentation when people choose a help button.

Include no more than one help button per window. Multiple help buttons in the same context make it hard for people to predict the result of clicking one.

Position help buttons where people expect to find them. Use the following locations for guidance.

Dialog with dismissal buttons (like OK and Cancel)

Lower corner, opposite to the dismissal buttons and vertically aligned with them

Dialog without dismissal buttons

Lower-left or lower-right corner

Settings window or pane

Use a help button within a view, not in the window frame. For example, avoid placing a help button in a toolbar or status bar.

Avoid displaying text that introduces a help button. People know what a help button does, so they don’t need additional descriptive text.


#### Image buttons
An image button appears in a view and displays an image, symbol, or icon. You can configure an image button to behave like a push button, toggle, or pop-up button.

Use an image button in a view, not in the window frame. For example, avoid placing an image button in a toolbar or status bar. If you need to use an image as a button in a toolbar, use a toolbar item. See Toolbars.

Include about 10 pixels of padding between the edges of the image and the button edges. An image button’s edges define its clickable area even when they aren’t visible. Including padding ensures that a click registers correctly even if it’s not precisely within the image. In general, avoid including a system-provided border in an image button; for developer guidance, see isBordered.

If you need to include a label, position it below the image button. For related guidance, see Labels.


### visionOS
A visionOS button typically includes a visible background that can help people see it, and the button plays sound to provide feedback when people interact with it.

There are three standard button shapes in visionOS. Typically, an icon-only button uses a circle shape, a text-only button uses a roundedRectangle or capsule shape, and a button that includes both an icon and text uses the capsule shape.

visionOS buttons use different visual styles to communicate four different interaction states.

In visionOS, buttons don’t support custom hover effects.

In addition to the four states shown above, a button can also reveal a tooltip when people look at it for a brief time. In general, buttons that contain text don’t need to display a tooltip because the button’s descriptive label communicates what it does.

In visionOS, buttons can have the following sizes.

Capsule (text and icon)

Prefer buttons that have a discernible background shape and fill. It tends to be easier for people to see a button when it’s enclosed in a shape that uses a contrasting background fill. The exception is a button in a toolbar, context menu, alert, or ornament where the shape and material of the larger component make the button comfortably visible. The following guidelines can help you ensure that a button looks good in different contexts:

- When a button appears on top of a glass window, use the thin material as the button’s background.
- When a button appears floating in space, use the glass material for its background.
Avoid creating a custom button that uses a white background fill and black text or icons. The system reserves this visual style to convey the toggled state.

In general, prefer circular or capsule-shape buttons. People’s eyes tend to be drawn toward the corners in a shape, making it difficult to keep looking at the shape’s center. The more rounded a button’s shape, the easier it is for people to look steadily at it. When you need to display a button by itself, prefer a capsule-shape button.

Provide enough space around a button to make it easy for people to look at it. Aim to place buttons so their centers are always at least 60 pts apart. If your buttons measure 60 pts or larger, add 4 pts of padding around them to keep the hover effect from overlapping. Also, it’s usually best to avoid displaying small or mini buttons in a vertical stack or horizontal row.

Choose the right shape if you need to display text-labeled buttons in a stack or row. Specifically, prefer the rounded-rectangle shape in a vertical stack of buttons and prefer the capsule shape in a horizontal row of buttons.

Use standard controls to take advantage of the audible feedback sounds people already know. Audible feedback is especially important in visionOS, because the system doesn’t play haptics.


### watchOS
watchOS displays all inline buttons using the capsule button shape. When you place a button inline with content, it gains a material effect that contrasts with the background to ensure legibility.

Use a toolbar to place buttons in the corners. The system automatically moves the time and title to accommodate toolbar buttons. The system also applies the Liquid Glass appearance to toolbar buttons, providing a clear visual distinction from the content beneath them.

Prefer buttons that span the width of the screen for primary actions in your app. Full-width buttons look better and are easier for people to tap. If two buttons must share the same horizontal space, use the same height for both, and use images or short text titles for each button’s content.

Use toolbar buttons to provide either navigation to related areas or contextual actions for the view’s content. These buttons provide access to additional information or secondary actions for the view’s content.

Use the same height for vertical stacks of one- and two-line text buttons. As much as possible, use identical button heights for visual consistency.


## Resources

#### Related

#### Developer documentation

## Change log
Updated guidance for Liquid Glass.

Updated guidance for button styles and content.

Noted that visionOS buttons don’t support custom hover effects.

Clarified some terminology and guidance for buttons in visionOS.

Updated to include guidance for visionOS.

Updated guidance for using buttons in watchOS.


---

### Context Menus

**Source**: [https://developer.apple.com/design/human-interface-guidelines/context-menus](https://developer.apple.com/design/human-interface-guidelines/context-menus)


# Context menus
Although a context menu provides convenient access to frequently used items, it’s hidden by default, so people might not know it’s there. To reveal a context menu, people generally choose a view or select some content and then perform an action, using the input modes their current configuration supports. For example:

- The system-defined touch or pinch and hold gesture in visionOS, iOS, and iPadOS
- Pressing the Control key while clicking a pointing device in macOS and iPadOS
- Using a secondary click on a Magic Trackpad in macOS or iPadOS

## Best practices
Prioritize relevancy when choosing items to include in a context menu. A context menu isn’t for providing advanced or rarely used items; instead, it helps people quickly access the commands they’re most likely to need in their current context. For example, the context menu for a Mail message in the Inbox includes commands for replying and moving the message, but not commands for editing message content, managing mailboxes, or filtering messages.

Aim for a small number of menu items. A context menu that’s too long can be difficult to scan and scroll.

Support context menus consistently throughout your app. If you provide context menus for items in some places but not in others, people won’t know where they can use the feature and may think there’s a problem.

Always make context menu items available in the main interface, too. For example, in Mail in iOS and iPadOS, the context menu items that are available for a message in the Inbox are also available in the toolbar of the message view. In macOS, an app’s menu bar menus list all the app’s commands, including those in various context menus.

If you need to use submenus to manage a menu’s complexity, keep them to one level. A submenu is a menu item that reveals a secondary menu of logically related commands. Although submenus can shorten a context menu and clarify its commands, more than one level of submenu complicates the experience and can be difficult for people to navigate. If you need to include a submenu, give it an intuitive title that helps people predict its contents without opening it. For guidance, see Submenus.

Hide unavailable menu items, don’t dim them. Unlike a regular menu, which helps people discover actions they can perform even when the action isn’t available, a context menu displays only the actions that are relevant to the currently selected view or content. In macOS, the exceptions are the Cut, Copy, and Paste menu items, which may appear unavailable if they don’t apply to the current context.

Aim to place the most frequently used menu items where people are likely to encounter them first. When a context menu opens, people often read it starting from the part that’s closest to where their finger or pointer revealed it. Depending on the location of the selected content, a context menu might open above or below it, so you might also need to reverse the order of items to match the position of the menu.

Show keyboard shortcuts in your app’s main menus, not in context menus. Context menus already provide a shortcut to task-specific commands, so it’s redundant to display keyboard shortcuts too.

Follow best practices for using separators. As with other types of menus, you can use separators to group items in a context menu and help people scan the menu more quickly. In general, you don’t want more than about three groups in a context menu. For guidance, see Menus.

In iOS, iPadOS, and visionOS, warn people about context menu items that can destroy data. If you need to include potentially destructive items in your context menu — such as Delete or Remove — list them at the end of the menu and identify them as destructive (for developer guidance, see destructive). The system can display a destructive menu item using a red text color.


## Content
A context menu seldom displays a title. In contrast, each item in a context menu needs to display a short label that clearly describes what it does. For guidance, see Menus > Labels.

Include a title in a context menu only if doing so clarifies the menu’s effect. For example, when people select multiple Mail messages and tap the Mark toolbar button in iOS and iPadOS, the resulting context menu displays a title that states the number of selected messages, reminding people that the command they choose affects all the messages they selected.

Represent menu item actions with familiar icons. Icons help people recognize common actions throughout your app. Use the same icons as the system to represent actions such as Copy, Share, and Delete, wherever they appear. For a list of icons that represent common actions, see Standard icons. For additional guidance, see Menus.


## Platform considerations
No additional considerations for tvOS. Not supported in watchOS.


### iOS, iPadOS
Provide either a context menu or an edit menu for an item, but not both. If you provide both features for the same item, it can be confusing to people — and difficult for the system to detect their intent. See Edit menus.

In iPadOS, consider using a context menu to let people create a new object in your app. iPadOS lets you reveal a context menu when people perform a long press on the touchscreen or use a secondary click with an attached trackpad or keyboard. For example, Files lets people create a new folder by revealing a context menu in an area between existing files and folders.

In iOS and iPadOS, a context menu can display a preview of the current content near the list of commands. People can choose a command in the menu or — in some cases — they can tap the preview to open it or drag it to another area.

Prefer a graphical preview that clarifies the target of a context menu’s commands. For example, when people reveal a context menu on a list item in Notes or Mail, the preview shows a condensed version of the actual content to help people confirm that they’re working with the item they intend.

Ensure that your preview looks good as it animates. As people reveal a context menu on an onscreen object, the system animates the preview image as it emerges from the content, dimming the screen behind the preview and the menu. It’s important to adjust the preview’s clipping path to match the shape of the preview image so that its contours, such as the rounded corners, don’t appear to change during animation. For developer guidance, see UIContextMenuInteractionDelegate.


### macOS
On a Mac, a context menu is sometimes called a contextual menu.


### visionOS
Consider using a context menu instead of a panel or inspector window to present frequently used functionality. Minimizing the number of separate views or windows your app opens can help people keep their space uncluttered.

In general, avoid letting a context menu’s height exceed the height of the window. In visionOS, a window includes system-provided components above and below its top and bottom edges, such as window-management controls and the Share menu, so a context menu that’s too tall could obscure them. As you consider the number of items to include, be guided by the ways people are likely to use your app. For example, people who use an app to accomplish in-depth, specialist tasks often expect to spend time learning a large number of sophisticated commands and might appreciate contextual access to them. On the other hand, people who use an app to perform a few simple actions may appreciate short contextual menus that are quick to scan and use.


## Resources

#### Related

#### Developer documentation
contextMenu(menuItems:) — SwiftUI

UIContextMenuInteraction — UIKit

popUpContextMenu(_:with:for:) — AppKit


## Change log
Added guidance on hiding unavailable menu items.

Updated to include guidance for visionOS.

Refined guidance on including a submenu and added a guideline on using a context menu to support object creation in an iPadOS app.


---

### Dock Menus

**Source**: [https://developer.apple.com/design/human-interface-guidelines/dock-menus](https://developer.apple.com/design/human-interface-guidelines/dock-menus)


# Dock menus
The system-provided Dock menu items can vary depending on whether the app is open. For example, the Dock menu for Safari includes menu items for actions like viewing a current window or creating a new window.

Although iOS and iPadOS don’t support a Dock menu, people can reveal a similar menu of system-provided and custom items — called Home Screen quick actions — when they long press an app icon on the Home Screen or in the Dock. For guidance, see Home Screen quick actions.


## Best practices
As with all menus, you need to label Dock menu items succinctly and organize them logically. For guidance, see Menus.

Make custom Dock menu items available in other places, too. Not everyone uses a Dock menu, so it’s important to offer the same commands elsewhere, like in your menu bar menus or within your interface.

Prefer high-value custom items for your Dock menu. For example, a Dock menu can list all currently or recently open windows, making it a convenient way to jump to the window people want. Also consider listing a few of the actions that are most likely to be useful when your app isn’t frontmost or when there are no open windows. For example, Mail includes items for getting new mail and composing a new message in addition to listing all open windows.


## Platform considerations
Not supported in iOS, iPadOS, tvOS, visionOS, or watchOS.


## Resources

#### Related
Home Screen quick actions


#### Developer documentation
applicationDockMenu(_:) — AppKit


---

### Edit Menus

**Source**: [https://developer.apple.com/design/human-interface-guidelines/edit-menus](https://developer.apple.com/design/human-interface-guidelines/edit-menus)


# Edit menus
In addition to text, an edit menu’s commands can apply to many types of selectable content, such as images, files, and objects like contact cards, charts, or map locations. In iOS, iPadOS, and visionOS, the system automatically detects the data type of a selected item, which can result in the addition of a related action to the edit menu. For example, selecting an address can add an item like Get directions to the edit menu.

Edit menus can look and behave slightly differently in different platforms.

- In iOS, the edit menu displays commands in a compact, horizontal list that appears when people touch and hold or double-tap to select content in a view. People can tap a chevron on the trailing edge to expand it into a context menu.
- In iPadOS, the edit menu looks different depending on how people reveal it. When people use touch interactions to reveal the menu, it uses the compact, horizontal appearance. In contrast, when people use a keyboard or pointing device to reveal it, the edit menu opens directly in a context menu.
- In macOS, people can access editing commands in a context menu they can reveal while in an editing task, as well as through the app’s Edit menu in the menu bar.
- In visionOS, people use the standard pinch and hold gesture to open the edit menu as a horizontal bar, or they can open it in a context menu.
Editing content is rare in tvOS and watchOS experiences, so the system doesn’t provide an edit menu in these platforms.


## Best practices
Prefer the system-provided edit menu. People are familiar with the contents and behavior of the system-provided component, so creating a custom menu that presents the same commands is redundant and likely to be confusing. For a list of standard edit menu commands, see UIResponderStandardEditActions.

Let people reveal an edit menu using the system-defined interactions they already know. For example, people expect to touch and hold on a touchscreen, pinch and hold in visionOS, or use a secondary click with an attached trackpad or keyboard. Although the interactions to reveal an edit menu can differ based on platform, people don’t appreciate having to learn a custom interaction to perform a standard task.

Offer commands that are relevant in the current context, removing or dimming commands that don’t apply. For example, if nothing is selected, avoid showing options that require a selection, such as Copy or Cut. Similarly, avoid showing a Paste option when there’s nothing to paste.

List custom commands near relevant system-provided ones. For example, if you offer custom formatting commands, you can help maintain the ordering people expect by listing them after the system-provided commands in the format section. Avoid overwhelming people with too many custom commands.

When it makes sense, let people select and copy noneditable text. People appreciate being able to paste static content — such as an image caption or social media status — into a message, note, or web search. In general, let people copy content text, but not control labels.

Support undo and redo when possible. Like all menus, an edit menu doesn’t require confirmation before performing its actions, so people can easily use undo and redo to recover a previous state. For guidance, see Undo and redo.

In general, avoid implementing other controls that perform the same functions as edit menu items. People typically expect to choose familiar edit commands in an edit menu, or use standard keyboard shortcuts. Offering redundant controls can crowd your interface, giving you less space for presenting actions that people might not already know about.

Differentiate different types of deletion commands when necessary. For example, a Delete menu item behaves the same as pressing a Delete key, but a Cut menu item copies the selected content to the system pasteboard before deleting it.


## Content
Create short labels for custom commands. Use verbs or short verb phrases that succinctly describe the action your command performs. For guidance, see Labels.


## Platform considerations
No additional considerations for visionOS. Not supported in tvOS or watchOS.


### iOS, iPadOS
Ensure your edit menu works well in both styles. The system displays the compact, horizontal style when people use Multi-Touch gestures to reveal the edit menu, and the vertical style when people use a keyboard or pointing device to reveal it. For guidance using the vertical menu layout, see Menus > iOS, iPadOS.

Adjust an edit menu’s placement, if necessary. Depending on available space, the default menu position is above or below the insertion point or selection. The system also displays a visual indicator that points to the targeted content. Although you can’t change the shape of the menu or its pointer, you can change the menu’s position. For example, you might need to move the menu to prevent it from covering important content or parts of your interface.


### macOS
To learn about the order of items in a macOS app’s Edit menu, see Edit menu.


## Resources

#### Related

#### Developer documentation
UIEditMenuInteraction — UIKit


## Change log
Updated to include guidance for visionOS.

Added guidance on supporting both edit-menu styles in iPadOS.


---

### Home Screen Quick Actions

**Source**: [https://developer.apple.com/design/human-interface-guidelines/home-screen-quick-actions](https://developer.apple.com/design/human-interface-guidelines/home-screen-quick-actions)


# Home Screen quick actions
People can get a menu of available quick actions when they touch and hold an app icon (on a 3D Touch device, people can press on the icon with increased pressure to see the menu). For example, Mail includes quick actions that open the Inbox or the VIP mailbox, initiate a search, and create a new message. In addition to app-specific actions, a Home Screen quick action menu also lists items for removing the app and editing the Home Screen.

Each Home Screen quick action includes a title, an interface icon on the left or right (depending on your app’s position on the Home Screen), and an optional subtitle. The title and subtitle are always left-aligned in left-to-right languages. Your app can even dynamically update its quick actions when new information is available. For example, Messages provides quick actions for opening your most recent conversations.


## Best practices
Create quick actions for compelling, high-value tasks. For example, Maps lets people search near their current location or get directions home without first opening the Maps app. People tend to expect every app to provide at least one useful quick action; you can provide a total of four.

Avoid making unpredictable changes to quick actions. Dynamic quick actions are a great way to keep actions relevant. For example, it may make sense to update quick actions based on the current location or recent activities in your app, time of day, or changes in settings. Make sure that actions change in ways that people can predict.

For each quick action, provide a succinct title that instantly communicates the results of the action. For example, titles like “Directions Home,” “Create New Contact,” and “New Message” can help people understand what happens when they choose the action. If you need to give more context, provide a subtitle too. Mail uses subtitles to indicate whether there are unread messages in the Inbox and VIP folder. Don’t include your app name or any extraneous information in the title or subtitle, keep the text short to avoid truncation, and take localization into account as you write the text.

Provide a familiar interface icon for each quick action. Prefer using SF Symbols to represent actions. For a list of icons that represent common actions, see Standard icons; for additional guidance, see Menus.

If you design your own interface icon, use the Quick Action Icon Template that’s included with Apple Design Resources for iOS and iPadOS.

Don’t use an emoji in place of a symbol or interface icon. Emojis are full color, whereas quick action symbols are monochromatic and change appearance in Dark Mode to maintain contrast.


## Platform considerations
No additional considerations for iOS or iPadOS. Not supported in macOS, tvOS, visionOS, or watchOS.


## Resources

#### Related

#### Developer documentation
Add Home Screen quick actions — UIKit


---

### Menus

**Source**: [https://developer.apple.com/design/human-interface-guidelines/menus](https://developer.apple.com/design/human-interface-guidelines/menus)


# Menus
Menus are ubiquitous in apps and games, so most people already know how to use them. Whether you use system-provided components or custom ones, people expect menus to behave in familiar ways. For example, people understand that opening a menu reveals one or more menu items, each of which represents a command, option, or state that affects the current selection or context. The guidance for labeling and organizing menu items applies to all types of menus in all experiences.

Several system-provided components also include menus that support specific use cases. For example, a pop-up button or pull-down button can reveal a menu of options directly related to its action; a context menu lets people access a small number of frequently used actions relevant to their current view or task; and in macOS and iPadOS, menu bar menus contain all the commands people can perform in the app or game.


## Labels
A menu item’s label describes what it does and may include a symbol if it helps to clarify meaning. In an app, a menu item can also display the associated keyboard command, if there is one; in a game, a menu item rarely displays a keyboard command because a game typically needs to handle input from a wider range of devices and may offer game-specific mappings for various keys.

Depending on menu layout, an iOS, iPadOS, or visionOS app can display a few unlabeled menu items that use only symbols or icons to identify them. For guidance, see visionOS and iOS, iPadOS.

For each menu item, write a label that clearly and succinctly describes it. In general, label a menu item that initiates an action using a verb or verb phrase that describes the action, such as View, Close, or Select. For guidance labeling menu items that show and hide something in the interface or show the currently selected state of something, see Toggled items. As with all the copy you write, let your app’s or game’s communication style guide the tone of the menu-item labels you create.

To be consistent with platform experiences, use title-style capitalization. Although a game might have a different writing style, generally prefer using title-style capitalization, which capitalizes every word except articles, coordinating conjunctions, and short prepositions, and capitalizes the last word in the label, regardless of the part of speech. For complete guidance on this style of capitalization in English, see title-style capitalization.

Remove articles like a, an, and the from menu-item labels to save space. In English, articles always lengthen labels, but rarely enhance understanding. For example, changing a menu-item label from View Settings to View the Settings doesn’t provide additional clarification.

Show people when a menu item is unavailable. An unavailable menu item often appears dimmed and doesn’t respond to interactions. If all of a menu’s items are unavailable, the menu itself needs to remain available so people can open it and learn about the commands it contains.

Append an ellipsis to a menu item’s label when the action requires more information before it can complete. The ellipsis character (…) signals that people need to input information or make additional choices, typically within another view.


## Icons
Represent menu item actions with familiar icons. Icons help people recognize common actions throughout your app. Use the same icons as the system to represent actions such as Copy, Share, and Delete, wherever they appear. For a list of icons that represent common actions, see Standard icons.

Don’t display an icon if you can’t find one that clearly represents the menu item. Not all menu items need an icon. Be careful when adding icons for custom menu items to avoid confusion with other existing actions, and don’t add icons just for the sake of ornamentation.

Use a single icon to introduce a group of similar items. Instead of adding individual icons for each action, or reusing the same icon for all of them, establish a common theme with the symbol for the first item and rely on the menu item text to keep the remaining items distinct.


## Organization
Organizing menu items in ways that reflect how people use your app or game can make your experience feel straightforward and easy to use.

Prefer listing important or frequently used menu items first. People tend to start scanning a menu from the top, so listing high-priority items first often means that people can find what they want without reading the entire menu.

Consider grouping logically related items. For example, grouping editing commands like Copy, Cut, and Paste or camera commands like Look Up, Look Down, and Look Left can help people remember where to find them. To help people visually distinguish such groups, use a separator. Depending on the platform and type of menu, a separator appears between groups of items as a horizontal line or a short gap in the menu’s background appearance.

Prefer keeping all logically related commands in the same group, even if the commands don’t all have the same importance. For example, people generally use Paste and Match Style much less often than they use Paste, but they expect to find both commands in the same group that contains more frequently used editing commands like Copy and Cut.

Be mindful of menu length. People need more time and attention to read a long menu, which means they may miss the command they want. If a menu is too long, consider dividing it into separate menus. Alternatively, you might be able to use a submenu to shorten the list, such as listing difficulty levels in a submenu of a New Game menu item. The exception is when a menu contains user-defined or dynamically generated content, like the History and Bookmarks menus in Safari. People expect such a menu to accommodate all the items they add to it, so a long menu is fine, and scrolling is acceptable.


## Submenus
Sometimes, a menu item can reveal a set of closely related items in a subordinate list called a submenu. A menu item indicates the presence of a submenu by displaying a symbol — like a chevron — after its label. Submenus are functionally identical to menus, aside from their hierarchical positioning.

Use submenus sparingly. Each submenu adds complexity to the interface and hides the items it contains. You might consider creating a submenu when a term appears in more than two menu items in the same group. For example, instead of offering separate menu items for Sort by Date, Sort by Score, and Sort by Time, a game could present a menu item that uses a submenu to list the sorting options Date, Score, and Time. It generally works well to use the repeated term — in this case, Sort by — in the menu item’s label to help people predict the contents of the submenu.

Limit the depth and length of submenus. It can be difficult for people to reveal multiple levels of hierarchical submenus, so it’s generally best to restrict them to a single level. Also, if a submenu contains more than about five items, consider creating a new menu.

Make sure a submenu remains available even when its nested menu items are unavailable. A submenu item — like all menu items — needs to let people open it and learn about the commands it contains.

Prefer using a submenu to indenting menu items. Using indentation is inconsistent with the system and doesn’t clearly express the relationships between the menu items.


## Toggled items
Menu items often represent attributes or objects that people can turn on or off. If you want to avoid listing a separate menu item for each state, it can be efficient to create a single, toggled menu item that communicates the current state and lets people change it.

Consider using a changeable label that describes an item’s current state. For example, instead of listing two menu items like Show Map and Hide Map, you could include one menu item whose label changes from Show Map to Hide Map, depending on whether the map is visible.

Include a verb if a changeable label isn’t clear enough. For example, people might not know whether the changeable labels HDR On and HDR Off describe actions or states. If you needed to clarify that these items represent actions, you could add verbs to the labels, like Turn HDR On and Turn HDR Off.

If necessary, display both menu items instead of one toggled item. Sometimes, it helps people to view both actions or states at the same time. For example, a game could list both Take Account Online and Take Account Offline items, so when someone’s account is online, only the Take Account Offline menu item appears available.

Consider using a checkmark to show that an attribute is currently in effect. It’s easy for people to scan for checkmarks in a list of attributes to find the ones that are selected. For example, in the standard Format > Font menu, checkmarks can make it easy for people notice the styles that apply to selected text.

Consider offering a menu item that makes it easy to remove multiple toggled attributes. For example, if you let people apply several styles to selected text, it can work well to provide a menu item — such as Plain — that removes all applied formatting attributes at one time.


## In-game menus
In-game menus give players ways to control gameplay as well as determine settings for the game as a whole.

Let players navigate in-game menus using the platform’s default interaction method. People expect to use the same interactions to navigate your menus as they use for navigating other menus on the device. For example, players expect to navigate your game menus using touch in iOS and iPadOS, and direct and indirect gestures in visionOS.

Make sure your menus remain easy to open and read on all platforms you support. Each platform defines specific sizes that work best for fonts and interaction targets. Sometimes, scaling your game content to display on a different screen — especially a mobile device screen — can make in-game menus too small for people to read or interact with. If this happens, modify the size of the tap targets and consider alternative ways to communicate the menu’s content. For guidance, see Typography and Touch controls.


## Platform considerations
No additional considerations for macOS, tvOS, or watchOS.


### iOS, iPadOS
In iOS and iPadOS, a menu can display items in one of the following three layouts.

- Small. A row of four items appears at the top of the menu, above a list that contains the remaining items. For each item in the top row, the menu displays a symbol or icon, but no label.
- Medium. A row of three items appears at the top of the menu, above a list that contains the remaining items. For each item in the top row, the menu displays a symbol or icon above a short label.
- Large (the default). The menu displays all items in a list.
For developer guidance, see preferredElementSize.

Choose a small or medium menu layout when it can help streamline people’s choices. Consider using the medium layout if your app has three important actions that people often want to perform. For example, Notes uses the medium layout to give people a quick way to perform the Scan, Lock, and Pin actions. Use the small layout only for closely related actions that typically appear as a group, such as Bold, Italic, Underline, and Strikethrough. For each action, use a recognizable symbol that helps people identify the action without a label.


### visionOS
In visionOS, a menu can display items using the small or large layout styles that iOS and iPadOS define (for guidance, see iOS, iPadOS). You can present a menu in your app or game from 3D content using a SwiftUI view. To ensure that your menu is always visible to people, even when other content occludes it, you can apply a breakthrough effect. As in macOS, an open menu in a visionOS window can appear outside of the window’s boundaries.

Prefer displaying a menu near the content it controls. Because people need to look at a menu item before tapping it, they might miss the item’s effect if the content it controls is too far away.

Prefer the subtle breakthrough effect in most cases. This effect blends the presentation with its surrounding content, to maintain legibility and usability while preserving the depth and context of the scene. When you select automatic for the breakthrough effect of a menu that overlaps with 3D content, the system applies subtle by default. You can use prominent if it’s important to display a menu prominently over the entire scene in your app or game, but this can disrupt the experience for people and potentially cause discomfort. Alternatively, you can use none to fully occlude your menu behind other 3D content — for example, in a puzzle game that requires people to navigate around barriers — but this may make it difficult for people to see and access the menu.


## Resources

#### Related

#### Developer documentation
Menus and shortcuts — UIKit


## Change log
Added guidance for presenting menus with breakthrough effects in visionOS.

Added guidance for representing menu items with icons.

Added guidance for in-game menus and included game-specific examples.

Updated to include guidance for visionOS.

Added guidelines for using the small, medium, and large menu layouts in iPadOS.


---

### Ornaments

**Source**: [https://developer.apple.com/design/human-interface-guidelines/ornaments](https://developer.apple.com/design/human-interface-guidelines/ornaments)


# Ornaments
An ornament floats in a plane that’s parallel to its associated window and slightly in front of it along the z-axis. If the associated window moves, the ornament moves with it, maintaining its relative position; if the window’s contents scroll, the controls or information in the ornament remain unchanged.

Ornaments can appear on any edge of a window and can contain UI components like buttons, segmented controls, and other views. The system uses ornaments to create and manage components like toolbars, tab bars, and video playback controls; you can use an ornament to create a custom component.


## Best practices
Consider using an ornament to present frequently needed controls or information in a consistent location that doesn’t clutter the window. Because an ornament stays close to its window, people always know where to find it. For example, Music uses an ornament to offer Now Playing controls, ensuring that these controls remain in a predictable location that’s easy to find.

In general, keep an ornament visible. It can make sense to hide an ornament when people dive into a window’s content — for example, when they watch a video or view a photo — but in most cases, people appreciate having consistent access to an ornament’s controls.

If you need to display multiple ornaments, prioritize the overall visual balance of the window. Ornaments help elevate important actions, but they can sometimes distract from your content. When necessary, consider constraining the total number of ornaments to avoid increasing a window’s visual weight and making your app feel more complicated. If you decide to remove an ornament, you can relocate its elements into the main window.

Aim to keep an ornament’s width the same or narrower than the width of the associated window. If an ornament is wider than its window, it can interfere with a tab bar or other vertical content on the window’s side.

Consider using borderless buttons in an ornament. By default, an ornament’s background is glass, so if you place a button directly on the background, it may not need a visible border. When people look at a borderless button in an ornament, the system automatically applies the hover affect to it (for guidance, see Eyes).

Use system-provided toolbars and tab bars unless you need to create custom components. In visionOS, toolbars and tab bars automatically appear as ornaments, so you don’t need to use an ornament to create these components. For developer guidance, see Toolbars and TabView.


## Platform considerations
Not supported in iOS, iPadOS, macOS, tvOS, or watchOS.


## Resources

#### Related

#### Developer documentation
ornament(visibility:attachmentAnchor:contentAlignment:ornament:) — SwiftUI


#### Videos

## Change log
Added guidance on using multiple ornaments.

Removed a statement about using ornaments to present supplementary items.


---

### Pop Up Buttons

**Source**: [https://developer.apple.com/design/human-interface-guidelines/pop-up-buttons](https://developer.apple.com/design/human-interface-guidelines/pop-up-buttons)


# Pop-up buttons
After people choose an item from a pop-up button’s menu, the menu closes, and the button can update its content to indicate the current selection.


## Best practices
Use a pop-up button to present a flat list of mutually exclusive options or states. A pop-up button helps people make a choice that affects their content or the surrounding view. Use a pull-down button instead if you need to:

- Offer a list of actions
- Let people select multiple items
- Include a submenu
Provide a useful default selection. A pop-up button can update its content to identify the current selection, but if people haven’t made a selection yet, it shows the default item you specify. When possible, make the default selection an item that most people are likely to want.

Give people a way to predict a pop-up button’s options without opening it. For example, you can use an introductory label or a button label that describes the button’s effect, giving context to the options.

Consider using a pop-up button when space is limited and you don’t need to display all options all the time. Pop-up buttons are a space-efficient way to present a wide array of choices.

If necessary, include a Custom option in a pop-up button’s menu to provide additional items that are useful in some situations. Offering a Custom option can help you avoid cluttering the interface with items or controls that people need only occasionally. You can also display explanatory text below the list to help people understand how the options work.


## Platform considerations
No additional considerations for iOS, macOS, or visionOS. Not supported in tvOS or watchOS.


### iPadOS
Within a popover or modal view, consider using a pop-up button instead of a disclosure indicator to present multiple options for a list item. For example, people can quickly choose an option from the pop-up button’s menu without navigating to a detail view. Consider using a pop-up button in this scenario when you have a fairly small, well-defined set of options that work well in a menu.


## Resources

#### Related

#### Developer documentation
MenuPickerStyle — SwiftUI

changesSelectionAsPrimaryAction — UIKit

NSPopUpButton — AppKit


## Change log
Added a guideline on using a pop-up button in a popover or modal view in iPadOS.


---

### Pull Down Buttons

**Source**: [https://developer.apple.com/design/human-interface-guidelines/pull-down-buttons](https://developer.apple.com/design/human-interface-guidelines/pull-down-buttons)


# Pull-down buttons
After people choose an item in a pull-down button’s menu, the menu closes, and the app performs the chosen action.


## Best practices
Use a pull-down button to present commands or items that are directly related to the button’s action. The menu lets you help people clarify the button’s target or customize its behavior without requiring additional buttons in your interface. For example:

- An Add button could present a menu that lets people specify the item they want to add.
- A Sort button could use a menu to let people select an attribute on which to sort.
- A Back button could let people choose a specific location to revisit instead of opening the previous one.
If you need to provide a list of mutually exclusive choices that aren’t commands, use a pop-up button instead.

Avoid putting all of a view’s actions in one pull-down button. A view’s primary actions need to be easily discoverable, so you don’t want to hide them in a pull-down button that people have to open before they can do anything.

Balance menu length with ease of use. Because people have to interact with a pull-down button before they can view its menu, listing a minimum of three items can help the interaction feel worthwhile. If you need to list only one or two items, consider using alternative components to present them, such as buttons to perform actions and toggles or switches to present selections. In contrast, listing too many items in a pull-down button’s menu can slow people down because it takes longer to find a specific item.

Display a succinct menu title only if it adds meaning. In general, a pull-down button’s content — combined with descriptive menu items — provides all the context people need, making a menu title unnecessary.

Let people know when a pull-down button’s menu item is destructive, and ask them to confirm their intent. Menus use red text to highlight actions that you identify as potentially destructive. When people choose a destructive action, the system displays an action sheet (iOS) or popover (iPadOS) in which they can confirm their choice or cancel the action. Because an action sheet appears in a different location from the menu and requires deliberate dismissal, it can help people avoid losing data by mistake.

Include an interface icon with a menu item when it provides value. If you need to clarify an item’s meaning, you can display an icon or image after its label. Using SF Symbols for this purpose can help you provide a familiar experience while ensuring that the symbol remains aligned with the text at every scale.


## Platform considerations
No additional considerations for macOS or visionOS. Not supported in tvOS or watchOS.


### iOS, iPadOS
You can also let people reveal a pull-down menu by performing a specific gesture on a button. For example, in iOS 14 and later, Safari responds to a touch and hold gesture on the Tabs button by displaying a menu of tab-related actions, like New Tab and Close All Tabs.

Consider using a More pull-down button to present items that don’t need prominent positions in the main interface. A More button can help you offer a range of items where space is constrained, but it can also hinder discoverability. Although people generally understand that a More button offers additional functionality related to the current context, the ellipsis icon doesn’t necessarily help them predict its contents. To design an effective More button, weigh the convenience of its size against its impact on discoverability to find a balance that works in your app.


## Resources

#### Related

#### Developer documentation
MenuPickerStyle — SwiftUI

showsMenuAsPrimaryAction — UIKit


## Change log
Refined guidance on designing a useful menu length.


---

### The Menu Bar

**Source**: [https://developer.apple.com/design/human-interface-guidelines/the-menu-bar](https://developer.apple.com/design/human-interface-guidelines/the-menu-bar)


# The menu bar
Mac users are very familiar with the macOS menu bar, and they rely on it to help them learn what an app does and find the commands they need. To help your app or game feel at home in macOS, it’s essential to provide a consistent menu bar experience.

Menu bar menus on iPad are similar to those on Mac, appearing in the same order and with familiar sets of menu items. When you adopt the menu structure that people expect from their experience on Mac, it helps them immediately understand and take advantage of the menu bar on iPad as well.

Keyboard shortcuts in iPadOS use the same patterns as in macOS. For guidance, see Standard keyboard shortcuts.

Menus in the menu bar share most of the appearance and behavior characteristics that all menu types have. To learn about menus in general — and how to organize and label menu items — see Menus.


## Anatomy
When present in the menu bar, the following menus appear in the order listed below.

- YourAppName (you supply a short version of your app’s name for this menu’s title)
- File
- Edit
- Format
- View
- App-specific menus, if any
- Window
- Help
In addition, the macOS menu bar includes the Apple menu on the leading side and menu bar extras on the trailing side. See macOS Platform considerations for guidance.


## Best practices
Support the default system-defined menus and their ordering. People expect to find menus and menu items in an order they’re familiar with. In many cases, the system implements the functionality of standard menu items so you don’t have to. For example, when people select text in a standard text field, the system makes the Edit > Copy menu item available.

Always show the same set of menu items. Keeping menu items visible helps people learn what actions your app supports, even if they’re unavailable in the current context. If a menu bar item isn’t actionable, disable the action instead of hiding it from the menu.

Represent menu item actions with familiar icons. Icons help people recognize common actions throughout your app. Use the same icons as the system to represent actions such as Copy, Share, and Delete, wherever they appear. For a list of icons that represent common actions, see Standard icons. For additional guidance, see Menus.

Support the keyboard shortcuts defined for the standard menu items you include. People expect to use the keyboard shortcuts they already know for standard menu items, like Copy, Cut, Paste, Save, and Print. Define custom keyboard shortcuts only when necessary. For guidance, see Standard keyboard shortcuts.

Prefer short, one-word menu titles. Various factors — like different display sizes and the presence of menu bar extras — can affect the spacing and appearance of your menus. One-word menu titles work especially well in the menu bar because they take little space and are easy for people to scan. If you need to use more than one word in the menu title, use title-style capitalization.


## App menu
The app menu lists items that apply to your app or game as a whole, rather than to a specific task, document, or window. To help people quickly identify the active app, the menu bar displays your app name in bold.

The app menu typically contains the following menu items listed in the following order.

Displays the About window for your app, which includes copyright and version information.

Prefer a short name of 16 characters or fewer. Don’t include a version number.

Opens your settings window, or your app’s page in iPadOS Settings.

Use only for app-level settings. If you also offer document-specific settings, put them in the File menu.

Optional app-specific items

Performs custom app-level setting or configuration actions.

List custom app-configuration items after the Settings item and within the same group.

Services (macOS only)

Displays a submenu of services from the system and other apps that apply to the current context.

Hide YourAppName (macOS only)

Hides your app and all of its windows, and then activates the most recently used app.

Use the same short app name you supply for the About item.

Hide Others (macOS only)

Hides all other open apps and their windows.

Show All (macOS only)

Shows all other open apps and their windows behind your app’s windows.

Quits your app. Pressing Option changes Quit YourAppName to Quit and Keep Windows.

Display the About menu item first. Include a separator after the About menu item so that it appears by itself in a group.


## File menu
The File menu contains commands that help people manage the files or documents an app supports. If your app doesn’t handle any types of files, you can rename or eliminate this menu.

The File menu typically contains the following menu items listed in the following order.

Creates a new document, file, or window.

For Item, use a term that names the type of item your app creates. For example, Calendar uses Event and Calendar.

Can open the selected item or present an interface in which people select an item to open.

If people need to select an item in a separate interface, an ellipsis follows the command to indicate that more input is required.

Displays a submenu that lists recently opened documents and files that people can select, and typically includes a Clear Menu item.

List document and filenames that people recognize in the submenu; don’t display file paths. List the documents in the order people last opened them, with the most recently opened document first.

Closes the current window or document. Pressing Option changes Close to Close All. For a tab-based window, Close Tab replaces Close.

In a tab-based window, consider adding a Close Window item to let people close the entire window with one click or tap.

Closes the current tab in a tab-based window. Pressing Option changes Close Tab to Close Other Tabs.

Closes the current file and all its associated windows.

Consider supporting this menu item if your app can open multiple views of the same file.

Saves the current document or file.

Automatically save changes periodically as people work so they don’t need to keep choosing File > Save. For a new document, prompt people for a name and location. If you need to let people save a file in multiple formats, prefer a pop-up menu that lets people choose a format in the Save sheet.

Saves all open documents.

Duplicates the current document, leaving both documents open. Pressing Option changes Duplicate to Save As.

Prefer Duplicate to menu items like Save As, Export, Copy To, and Save To because these items don’t clarify the relationship between the original file and the new one.

Lets people change the name of the current document.

Prompts people to choose a new location for the document.

Prompts people for a name, output location, and export file format. After exporting the file, the current document remains open; the exported file doesn’t open.

Reserve the Export As item for when you need to let people export content in a format your app doesn’t typically handle.

When people turn on autosaving, displays a submenu that lists recent document versions and an option to display the version browser. After people choose a version to restore, it replaces the current document.

Opens a panel for specifying printing parameters like paper size and printing orientation. A document can save the printing parameters that people specify.

Include the Page Setup item if you need to support printing parameters that apply to a specific document. Parameters that are global in nature, like a printer’s name, or that people change frequently, like the number of copies to print, belong in the Print panel.

Opens the standard Print panel, which lets people print to a printer, send a fax, or save as a PDF.


## Edit menu
The Edit menu lets people make changes to content in the current document or text container, and provides commands for interacting with the Clipboard. Because many editing commands apply to any editable content, the Edit menu is useful even in apps that aren’t document-based.

Determine whether Find menu items belong in the Edit menu. For example, if your app lets people search for files or other types of objects, Find menu items might be more appropriate in the File menu.

The Edit menu typically contains the following top-level menu items, listed in the following order.

Reverses the effect of the previous user operation.

Clarify the target of the undo. For example, if people just selected a menu item, you can append the item’s title, such as Undo Paste and Match Style. For a text entry operation, you might append the word Typing to give Undo Typing.

Reverses the effect of the previous Undo operation.

Clarify the target of the redo. For example, if people just reversed a menu item selection, you can append the item’s title, such as Redo Paste and Match Style. For a text entry operation, you might append the word Typing to give Redo Typing.

Removes the selected data and stores it on the Clipboard, replacing the previous contents of the Clipboard.

Duplicates the selected data and stores it on the Clipboard.

Inserts the contents of the Clipboard at the current insertion point. The Clipboard contents remain unchanged, permitting people to choose Paste multiple times.

Paste and Match Style

Inserts the contents of the Clipboard at the current insertion point, matching the style of the inserted text to the surrounding text.

Removes the selected data, but doesn’t place it on the Clipboard.

Provide a Delete menu item instead of an Erase or Clear menu item. Choosing Delete is the equivalent of pressing the Delete key, so it’s important for the naming to be consistent.

Highlights all selectable content in the current document or text container.

Displays a submenu containing menu items for performing search operations in the current document or text container. Standard submenus include: Find, Find and Replace, Find Next, Find Previous, Use Selection for Find, and Jump to Selection.

Displays a submenu containing menu items for checking for and correcting spelling and grammar in the current document or text container. Standard submenus include: Show Spelling and Grammar, Check Document Now, Check Spelling While Typing, Check Grammar With Spelling, and Correct Spelling Automatically.

Displays a submenu containing items that let people toggle automatic substitutions while they type in a document or text container. Standard submenus include: Show Substitutions, Smart Copy/Paste, Smart Quotes, Smart Dashes, Smart Links, Data Detectors, and Text Replacement.

Displays a submenu containing items that transform selected text. Standard submenus include: Make Uppercase, Make Lowercase, and Capitalize.

Displays a submenu containing Start Speaking and Stop Speaking items, which control when the system audibly reads selected text.

Opens the dictation window and converts spoken words into text that’s added at the current insertion point. The system automatically adds the Start Dictation menu item at the bottom of the Edit menu.

Displays a Character Viewer, which includes emoji, symbols, and other characters people can insert at the current insertion point. The system automatically adds the Emoji & Symbols menu item at the bottom of the Edit menu.


## Format menu
The Format menu lets people adjust text formatting attributes in the current document or text container. You can exclude this menu if your app doesn’t support formatted text editing.

The Format menu typically contains the following top-level menu items, listed in the following order.

Displays a submenu containing items for adjusting font attributes of the selected text. Standard submenus include: Show Fonts, Bold, Italic, Underline, Bigger, Smaller, Show Colors, Copy Style, and Paste Style.

Displays a submenu containing items for adjusting text attributes of the selected text. Standard submenus include: Align Left, Align Center, Justify, Align Right, Writing Direction, Show Ruler, Copy Ruler, and Paste Ruler.


## View menu
The View menu lets people customize the appearance of all an app’s windows, regardless of type.

The View menu doesn’t include items for navigating between or managing specific windows; the Window menu provides these commands.

Provide a View menu even if your app supports only a subset of the standard view functions. For example, if your app doesn’t include a tab bar, toolbar, or sidebar, but does support full-screen mode, provide a View menu that includes only the Enter/Exit Full Screen menu item.

Ensure that each show/hide item title reflects the current state of the corresponding view. For example, when the toolbar is hidden, provide a Show Toolbar menu item; when the toolbar is visible, provide a Hide Toolbar menu item.

The View menu typically contains the following top-level menu items, listed in the following order.

Toggles the visibility of the tab bar above the body area in a tab-based window

Show All Tabs/Exit Tab Overview

Enters and exits a view (similar to Mission Control) that provides an overview of all open tabs in a tab-based window

In a window that includes a toolbar, toggles the toolbar’s visibility

In a window that includes a toolbar, opens a view that lets people customize toolbar items

In a window that includes a sidebar, toggles the sidebar’s visibility

Enter/Exit Full Screen

In an app that supports a full-screen experience, opens the window at full-screen size in a new space


## App-specific menus
Your app’s custom menus appear in the menu bar between the View menu and the Window menu. For example, Safari’s menu bar includes app-specific History and Bookmarks menus.

Provide app-specific menus for custom commands. People look in the menu bar when searching for app-specific commands, especially when using an app for the first time. Even when commands are available elsewhere in your app, it’s important to list them in the menu bar. Putting commands in the menu bar makes them easier for people to find, lets you assign keyboard shortcuts to them, and makes them more accessible to people using Full Keyboard Access. Excluding commands from the menu bar — even infrequently used or advanced commands — risks making them difficult for everyone to find.

As much as possible, reflect your app’s hierarchy in app-specific menus. For example, Mail lists the Mailbox, Message, and Format menus in an order that mirrors the relationships of these items: mailboxes contain messages, and messages contain formatting.

Aim to list app-specific menus in order from most to least general or commonly used. People tend to expect menus in the leading end of a list to be more specialized than menus in the trailing end.


## Window menu
The Window menu lets people navigate, organize, and manage an app’s windows.

The Window menu doesn’t help people customize the appearance of windows or close them. To customize a window, people use commands in the View menu; to close a window, people choose Close in the File menu.

Provide a Window menu even if your app has only one window. Include the Minimize and Zoom menu items so people using Full Keyboard Access can use the keyboard to invoke these functions.

Consider including menu items for showing and hiding panels. A panel provides information, configuration options, or tools for interacting with content in a primary window, and typically appears only when people need it. There’s no need to provide access to the font panel or text color panel because the Format menu lists these panels.

The Window menu typically contains the following top-level menu items, listed in the following order.

Minimizes the active window to the Dock. Pressing the Option key changes this item to Minimize All.

Toggles between a predefined size appropriate to the window’s content and the window size people set. Pressing the Option key changes this item to Zoom All.

Avoid using Zoom to enter or exit full-screen mode. The View menu supports these functions.

Shows the tab before the current tab in a tab-based window.

Shows the tab after the current tab in a tab-based window.

Move Tab to New Window

Opens the current tab in a new window.

Combines all open windows into a single tabbed window.

In an app that supports a full-screen experience, opens the window at full-screen size in a new space.

Include this item in the Window menu only if your app doesn’t have a View menu. In this scenario, continue to provide separate Minimize and Zoom menu items.

Brings all an app’s open windows to the front, maintaining their onscreen location, size, and layering order. (Clicking the app icon in the Dock has the same effect.) Pressing the Option key changes this item to Arrange in Front, which brings an app’s windows to the front in a neatly tiled arrangement.

Name of an open app-specific window

Brings the selected window to the front.

List the currently open windows in alphabetical order for easy scanning. Avoid listing panels or other modal views.


## Help menu
The Help menu — located at the trailing end of the menu bar — provides access to an app’s help documentation. When you use the Help Book format for this documentation, macOS automatically includes a search field at the top of the Help menu.

Send YourAppName Feedback to Apple

Opens the Feedback Assistant, in which people can provide feedback.

When the content uses the Help Book format, opens the content in the built-in Help Viewer.

Use a separator between your primary help documentation and additional items, which might include registration information or release notes. Keep the total the number of items you list in the Help menu small to avoid overwhelming people with too many choices when they need help. Alternatively, consider linking to additional items from within your help documentation.

For guidance, see Offering help; for developer guidance, see NSHelpManager.


## Dynamic menu items
In rare cases, it can make sense to present a dynamic menu item, which is a menu item that changes its behavior when people choose it while pressing a modifier key (Control, Option, Shift, or Command). For example, the Minimize item in the Window menu changes to Minimize All when people press the Option key.

Avoid making a dynamic menu item the only way to accomplish a task. Dynamic menu items are hidden by default, so they’re best suited to offer shortcuts to advanced actions that people can accomplish in other ways. For example, if someone hasn’t discovered the Minimize All dynamic menu item in the Window menu, they can still minimize each open window.

Use dynamic menu items primarily in menu bar menus. Adding a dynamic menu item to contextual or Dock menus can make the item even harder for people to discover.

Require only a single modifier key to reveal a dynamic menu item. It can be physically awkward to press more than one key while simultaneously opening a menu and choosing a menu item, in addition to reducing the discoverability of the dynamic behavior. For developer guidance, see isAlternate.

macOS automatically sets the width of a menu to hold the widest item, including dynamic menu items.


## Platform considerations
Not supported in iOS, tvOS, visionOS, or watchOS.


### iPadOS
The menu bar displays the top-level menus for your app or game, including both system-provided menus and any custom ones you choose to add. People reveal the menu bar by moving the pointer to the top edge of the screen, or swiping down from it. When visible, the menu bar occupies the same vertical space as the status bar at the top edge of the screen.

As with the macOS menu bar, the iPadOS menu bar provides a familiar way for people to learn what an app does, find the commands they need, and discover keyboard shortcuts. While they are similar in most respects, there are a few key differences between the menu bars on each platform.

Hidden until revealed

System default and custom

In the menu bar when the app is full screen

Never in the menu bar

About, Services, and app visibility-related items not available

Because the menu bar is often hidden when running an app full screen, ensure that people can access all of your app’s functions through its UI. In particular, always offer other ways to accomplish tasks assigned to dynamic menu items, since these are only available when a hardware keyboard is connected. Avoid using the menu bar as a catch-all location for functionality that doesn’t fit in elsewhere.

Reserve the YourAppName > Settings menu item for opening your app’s page in iPadOS Settings. If your app includes its own internal preferences area, link to it with a separate menu item beneath Settings in the same group. Place any other custom app-wide configuration options in this section as well.

For apps with tab-style navigation, consider adding each tab as a menu item in the View menu. Since each tab is a different view of the app, the View menu is a natural place to offer an additional way to navigate between tabs. If you do this, consider assigning key bindings to each tab to make navigation even more convenient.

Consider grouping menu items into submenus to conserve vertical space. Menu item rows on iPad use more space than on Mac to make them easier to tap. Because of this, and the smaller screen sizes of some iPads, it can be helpful to group related items into submenus more frequently than in the menu bar on Mac.


### macOS
The menu bar in macOS includes the Apple menu, which is always the first item on the leading side of the menu bar. The Apple menu includes system-defined menu items that are always available, and you can’t modify or remove it. Space permitting, the system can also display menu bar extras in the trailing end of the menu bar. For guidance, see Menu bar extras.

When menu bar space is constrained, the system prioritizes the display of menus and essential menu bar extras. To ensure that menus remain readable, the system may decrease the space between the titles, truncating them if necessary.

When people enter full-screen mode, the menu bar typically hides until they reveal it by moving the pointer to the top of the screen. For guidance, see Going full screen.


#### Menu bar extras
A menu bar extra exposes app-specific functionality using an icon that appears in the menu bar when your app is running, even when it’s not the frontmost app. Menu bar extras are on the opposite side of the menu bar from your app’s menus. For developer guidance, see MenuBarExtra.

When necessary, the system hides menu bar extras to make room for app menus. Similarly, if there are too many menu bar extras, the system may hide some to avoid crowding app menus.

Consider using a symbol to represent your menu bar extra. You can create an icon or you can choose one of the SF Symbols, using it as-is or customizing it to suit your needs. Both interface icons and symbols use black and clear colors to define their shapes; the system can apply other colors to the black areas in each image so it looks good on both dark and light menu bars, and when your menu bar extra is selected. The menu bar’s height is 24 pt.

Display a menu — not a popover — when people click your menu bar extra. Unless the app functionality you want to expose is too complex for a menu, avoid presenting it in a popover.

Let people — not your app — decide whether to put your menu bar extra in the menu bar. Typically, people add a menu bar extra to the menu bar by changing a setting in an app’s settings window. To ensure discoverability, however, consider giving people the option of doing so during setup.

Avoid relying on the presence of menu bar extras. The system hides and shows menu bar extras regularly, and you can’t be sure which other menu bar extras people have chosen to display or predict the location of your menu bar extra.

Consider exposing app-specific functionality in other ways, too. For example, you can provide a Dock menu that appears when people Control-click your app’s Dock icon. People can hide or choose not to use your menu bar extra, but a Dock menu is aways available when your app is running.


## Resources

#### Related
Standard keyboard shortcuts


#### Developer documentation
CommandMenu — SwiftUI

Adding menus and shortcuts to the menu bar and user interface — UIKit


#### Videos

## Change log
Added guidance for the menu bar in iPadOS.


---

### Toolbars

**Source**: [https://developer.apple.com/design/human-interface-guidelines/toolbars](https://developer.apple.com/design/human-interface-guidelines/toolbars)


# Toolbars
A toolbar consists of one or more sets of controls arranged horizontally along the top or bottom edge of the view, grouped into logical sections.

Toolbars act on content in the view, facilitate navigation, and help orient people in the app. They include three types of content:

- The title of the current view
- Navigation controls, like back and forward, and search fields
- Actions, or bar items, like buttons and menus
In contrast to a toolbar, a tab bar is specifically for navigating between areas of an app.


## Best practices
Choose items deliberately to avoid overcrowding. People need to be able to distinguish and activate each item, so you don’t want to put too many items in the toolbar. To accommodate variable view widths, define which items move to the overflow menu as the toolbar becomes narrower.

The system automatically adds an overflow menu in macOS or iPadOS when items no longer fit. Don’t add an overflow menu manually, and avoid layouts that cause toolbar items to overflow by default.

Add a More menu to contain additional actions. Prioritize less important actions for inclusion in the More menu. Try to include all actions in the toolbar if possible, and only add this menu if you really need it.

- Standard
- Compact
The standard toolbar in macOS Notes includes a More menu with extra commands.

As the window narrows, the More menu moves into an overflow menu along with other toolbar items that no longer fit.

In iPadOS and macOS apps, consider letting people customize the toolbar to include their most common items. Toolbar customization is especially useful in apps that provide a lot of items — or that include advanced functionality that not everyone needs — and in apps that people tend to use for long periods of time. For example, it works well to make a range of editing actions available for toolbar customization, because people often use different types of editing commands based on their work style and their current project.

Reduce the use of toolbar backgrounds and tinted controls. Any custom backgrounds and appearances you use might overlay or interfere with background effects that the system provides. Instead, use the content layer to inform the color and appearance of the toolbar, and use a ScrollEdgeEffectStyle when necessary to distinguish the toolbar area from the content area. This approach helps your app express its unique personality without distracting from content.

Avoid applying a similar color to toolbar item labels and content layer backgrounds. If your app already has bright, colorful content in the content layer, prefer using the default monochromatic appearance of toolbars. For more guidance, see Liquid Glass color.

Prefer using standard components in a toolbar. By default, standard buttons, text fields, headers, and footers have corner radii that are concentric with bar corners. If you need to create a custom component, ensure that its corner radius is also concentric with the bar’s corners.

Consider temporarily hiding toolbars for a distraction-free experience. Sometimes people appreciate a minimal interface to reduce distractions or reveal more content. If you support this, do so contextually when it makes the most sense, and offer ways to reliably restore hidden interface elements. For guidance, see Going full screen. For guidance specific to visionOS, see Immersive experiences.


## Titles
Provide a useful title for each window. A title helps people confirm their location as they navigate your app, and differentiates between the content of multiple open windows. If titling a toolbar seems redundant, you can leave the title area empty. For example, Notes doesn’t title the current note when a single window is open, because the first line of content typically supplies sufficient context. However, when opening notes in separate windows, the system titles them with the first line of content so people can tell them apart.

Don’t title windows with your app name. Your app’s name doesn’t provide useful information about your content hierarchy or any window or area in your app, so it doesn’t work well as a title.

Write a concise title. Aim for a word or short phrase that distills the purpose of the window or view, and keep the title under 15 characters long so you leave enough room for other controls.


## Navigation
A toolbar with navigation controls appears at the top of a window, helping people move through a hierarchy of content. A toolbar also often contains a search field for quick navigation between areas or pieces of content. In iOS, a navigation-specific toolbar is sometimes called a navigation bar.

Use the standard Back and Close buttons. People know that the standard Back button lets them retrace their steps through a hierarchy of information, and the standard Close button closes a modal view. Prefer the standard symbols for each, and don’t use a text label that says Back or Close. If you create a custom version of either, make sure it still looks the same, behaves as people expect, and matches the rest of your interface, and ensure you consistently implement it throughout your app or game. For guidance, see Icons.


## Actions
Provide actions that support the main tasks people perform. In general, prioritize the commands that people are most likely to want. These commands are often the ones people use most frequently, but in some apps it might make sense to prioritize commands that map to the highest level or most important objects people work with.

Make sure the meaning of each control is clear. Don’t make people guess or experiment to figure out what a toolbar item does. Prefer simple, recognizable symbols for items instead of text, except for actions like edit that aren’t well-represented by symbols. For guidance on symbols that represent common actions, see Standard icons.

Prefer system-provided symbols without borders. System-provided symbols are familiar, automatically receive appropriate coloring and vibrancy, and respond consistently to user interactions. Borders (like outlined circle symbols) aren’t necessary because the section provides a visible container, and the system defines hover and selection state appearances automatically. For guidance, see SF Symbols.

Use the .prominent style for key actions such as Done or Submit. This separates and tints the action so there’s a clear focal point. Only specify one primary action, and put it on the trailing side of the toolbar.


## Item groupings
You can position toolbar items in three locations: the leading edge, center area, and trailing edge of the toolbar. These areas provide familiar homes for navigation controls, window or document titles, common actions, and search.

- Center area. Common, useful controls appear in the center area, and the view title can appear here if it’s not on the leading edge. In macOS and iPadOS, people can add, remove, and rearrange items here if you let them customize the toolbar, and items in this section automatically collapse into the system-managed overflow menu when the window shrinks enough in size.
- Trailing edge. The trailing edge contains important items that need to remain available, buttons that open nearby inspectors, an optional search field, and the More menu that contains additional items and supports toolbar customization. It also includes a primary action like Done when one exists. Items on the trailing edge remain visible at all window sizes.
To position items in the groupings you want, pin them to the leading edge, center, or trailing edge, and insert space between buttons or other items where appropriate.

Group toolbar items logically by function and frequency of use. For example, Keynote includes several sections that are based on functionality, including one for presentation-level commands, one for playback commands, and one for object insertion.

Group navigation controls and critical actions like Done, Close, or Save in dedicated, familiar, and visually distinct sections. This reflects their importance and helps people discover and understand these actions.

Keep consistent groupings and placement across platforms. This helps people develop familiarity with your app and trust that it behaves similarly regardless of where they use it.

Minimize the number of groups. Too many groups of controls can make a toolbar feel cluttered and confusing, even with the added space on iPad and Mac. In general, aim for a maximum of three.

Keep actions with text labels separate. Placing an action with a text label next to an action with a symbol can create the illusion of a single action with a combined text and symbol, leading to confusion and misinterpretation. If your toolbar includes multiple text-labeled buttons, the text of those buttons may appear to run together, making the buttons indistinguishable. Add separation by inserting fixed space between the buttons. For developer guidance, see UIBarButtonItem.SystemItem.fixedSpace.


## Platform considerations
No additional considerations for tvOS.


### iOS
Prioritize only the most important items for inclusion in the main toolbar area. Because space is so limited, carefully consider which actions are essential to your app and include those first. Create a More menu to include additional items.

Use a large title to help people stay oriented as they navigate and scroll. By default, a large title transitions to a standard title as people begin scrolling the content, and transitions back to large when people scroll to the top, reminding them of their current location. For developer guidance, see prefersLargeTitles.


### iPadOS
Consider combining a toolbar with a tab bar. In iPadOS, a toolbar and a tab bar can coexist in the same horizontal space at the top of the view. This is particularly useful for layouts where you want to navigate between a few main app areas while keeping the full width of the window available for content. For guidance, see Layout and Windows.


### macOS
In a macOS app, the toolbar resides in the frame at the top of a window, either below or integrated with the title bar. Note that window titles can display inline with controls, and toolbar items don’t include a bezel.

Make every toolbar item available as a command in the menu bar. Because people can customize the toolbar or hide it, it can’t be the only place that presents a command. In contrast, it doesn’t make sense to provide a toolbar item for every menu item, because not all menu commands are important enough or used often enough to warrant space in the toolbar.


### visionOS
In visionOS, the system-provided toolbar appears along the bottom edge of a window, above the window-management controls, and in a parallel plane that’s slightly in front of the window along the z-axis.

To maintain the legibility of toolbar items as content scrolls behind them, visionOS uses a variable blur in the bar background. The variable blur anchors the bar above the scrolling content while letting the view’s glass material remain uniform and undivided.

In visionOS, you can supply either a symbol or a text label for each toolbar item. When people look at a toolbar item that contains a symbol, visionOS reveals the text label, providing additional information.

Prefer using a system-provided toolbar. The standard toolbar has a consistent and familiar appearance and is optimized to work well with eye and hand input. In addition, the system automatically places a standard toolbar in the correct position in relation to its window.

Avoid creating a vertical toolbar. In visionOS, tab bars are vertical, so presenting a vertical toolbar could confuse people.

Try to prevent windows from resizing below the width of the toolbar. visionOS doesn’t include a menu bar where each app lists all its actions, so it’s important for the toolbar to provide reliable access to essential controls regardless of a window’s size.

If your app can enter a modal state, consider offering contextually relevant toolbar controls. For example, a photo-editing app might enter a modal state to help people perform a multistep editing task. In this scenario, the controls in the modal editing view are different from the controls in the main window. Be sure to reinstate the window’s standard toolbar controls when the app exits the modal state.

Avoid using a pull-down menu in a toolbar. A pull-down menu lets you offer additional actions related to a toolbar item, but can be difficult for people to discover and may clutter your interface. Because a toolbar is located at the bottom edge of a window in visionOS, a pull-down menu might obscure the standard window controls that appear below the bottom edge. For guidance, see Pull-down buttons.


### watchOS
A toolbar button lets you offer important app functionality in a view that displays related content. You can place toolbar buttons in the top corners or along the bottom. If you place these buttons above scrolling content, the buttons always remain visible, as the content scrolls under them.

Bottom toolbar buttons

For developer guidance, see topBarLeading, topBarTrailing, or bottomBar.

You can also place a button in the scrolling view. By default, a scrolling toolbar button remains hidden until people reveal it by scrolling up. People frequently scroll to the top of a scrolling view, so discovering a toolbar button is automatic.

Toolbar button hidden

For developer guidance, see primaryAction.

Use a scrolling toolbar button for an important action that isn’t a primary app function. A toolbar button gives you the flexibility to offer important functionality in a view whose primary purpose is related to that functionality, but may not be the same. For example, Mail provides the essential New Message action in a toolbar button at the top of the Inbox view. The primary purpose of the Inbox is to display a scrollable list of email messages, so it makes sense to offer the closely related compose action in a toolbar button at the top of the view.


## Resources

#### Related
Apple Design Resources


#### Developer documentation

#### Videos

## Change log
Updated guidance for Liquid Glass.

Added guidance for grouping bar items, updated guidance for using symbols, and incorporated navigation bar guidance.

Updated to include guidance for visionOS.

Updated guidance for using toolbars in watchOS.


---

## Navigation and search

### Path Controls

**Source**: [https://developer.apple.com/design/human-interface-guidelines/path-controls](https://developer.apple.com/design/human-interface-guidelines/path-controls)


# Path controls
For example, choosing View > Show Path Bar in the Finder displays a path bar at the bottom of the window. It shows the path of the selected item, or the path of the window’s folder if nothing is selected.

There are two styles of path control.

Standard. A linear list that includes the root disk, parent folders, and selected item. Each item appears with an icon and a name. If the list is too long to fit within the control, it hides names between the first and last items. If you make the control editable, people can drag an item onto the control to select the item and display its path in the control.

Pop up. A control similar to a pop-up button that shows the icon and name of the selected item. People can click the item to open a menu containing the root disk, parent folders, and selected item. If you make the control editable, the menu contains an additional Choose command that people can use to select an item and display it in the control. They can also drag an item onto the control to select it and display its path.


## Best practices
Use a path control in the window body, not the window frame. Path controls aren’t intended for use in toolbars or status bars. Note that the path control in the Finder appears at the bottom of the window body, not in the status bar.


## Platform considerations
Not supported in iOS, iPadOS, tvOS, visionOS, or watchOS.


## Resources

#### Related

#### Developer documentation
NSPathControl — AppKit


---

### Search Fields

**Source**: [https://developer.apple.com/design/human-interface-guidelines/search-fields](https://developer.apple.com/design/human-interface-guidelines/search-fields)


# Search fields
A search field is an editable text field that displays a Search icon, a Clear button, and placeholder text where people can enter what they are searching for. Search fields can use a scope control as well as tokens to help filter and refine the scope of their search. Across each platform, there are different patterns for accessing search based on the goals and design of your app.

For developer guidance, see Adding a search interface to your app; for guidance related to systemwide search, see Searching.


## Best practices
Display placeholder text that describes the type of information people can search for. For example, the Apple TV app includes the placeholder text Shows, Movies, and More. Avoid using a term like Search for placeholder text because it doesn’t provide any helpful information.

If possible, start search immediately when a person types. Searching while someone types makes the search experience feel more responsive because it provides results that are continuously refined as the text becomes more specific.

Consider showing suggested search terms before search begins, or as a person types. This can help someone search faster by suggesting common searches, even when the search itself doesn’t begin immediately.

Simplify search results. Provide the most relevant search results first to minimize the need for someone to scroll to find what they’re looking for. In addition to prioritizing the most likely results, consider categorizing them to help people find what they want.

Consider letting people filter search results. For example, you can include a scope control in the search results content area to help people quickly and easily filter search results.


## Scope controls and tokens
Scope controls and tokens are components you can use to let someone narrow the parameters of a search either before or after they make it.

- A scope control acts like a segmented control for choosing a category for the search.
- A token is a visual representation of a search term that someone can select and edit, and acts as a filter for any additional terms in the search.
Use a scope control to filter among clearly defined search categories. A scope control can help someone move from a broader scope to a narrower one. For example, in Mail on iPhone, a scope control helps people move from searching their entire mailbox to just the specific mailbox they’re viewing. For developer guidance, see Scoping a search operation.

Default to a broader scope and let people refine it as they need. A broader scope provides context for the full set of available results, which helps guide people in a useful direction when they choose to narrow the scope.

Use tokens to filter by common search terms or items. When you define a token, the term it represents gains a visual treatment that encapsulates it, indicating that people can select and edit it as a single item. Tokens can clarify a search term, like filtering by a specific contact in Mail, or focus a search to a specific set of attributes, like filtering by photos in Messages. For the related macOS component, see Token fields.

Consider pairing tokens with search suggestions. People may not know which tokens are available, so pairing them with search suggestions can help people learn how to use them.


## Platform considerations
No additional considerations for visionOS.


### iOS
There are three main places you can position the entry point for search:

- In a tab bar at the bottom of the screen
- In a toolbar at the bottom or top of the screen
- Directly inline with content
Where search makes the most sense depends on the layout, content, and navigation of your app.


#### Search in a tab bar
You can place search as a visually distinct tab on the trailing side of a tab bar, which keeps search visible and always available as people switch between the sections of your app.

When someone navigates to the search tab, the search field that appears can start as focused or unfocused.

Start with the search field focused to help people quickly find what they need. When the search field starts focused, the keyboard immediately appears with the search field above it, ready to begin the search. This provides a more transient experience that brings people directly back to their previous tab after they exit search, and is ideal when you want search to resolve quickly and seamlessly.

Start with the search field unfocused to promote discovery and exploration. When the search field starts unfocused, the search tab expands into an unselected field at the bottom of the screen. This provides space on the rest of the screen for additional discovery or navigation before someone taps the field to begin the search. This is great for an app with a large collection of content to showcase, like Music or TV.


#### Search in a toolbar
As an alternative to search in a tab bar, you can also place search in a toolbar either at the bottom or top of the screen.

- You can include search in a bottom toolbar either as an expanded field or as a toolbar button, depending on how much space is available and how important search is to your app. When someone taps it, it animates into a search field above the keyboard so they can begin typing.
- You can include search in a top toolbar, also called a navigation bar, where it appears as a toolbar button. When someone taps it, it animates into a search field that appears either above the keyboard or inline at the top if there isn’t space at the bottom.
Search in a bottom toolbar

Search in a top toolbar

Place search at the bottom if there’s room. You can either add a search field to an existing toolbar, or as a new toolbar where search is the only item. Search at the bottom is useful in any situation where search is a priority, since it keeps the search experience easy to reach. Examples of apps with search at the bottom in various toolbar layouts include Settings, where it’s the only item, and Mail and Notes, where it fits alongside other important controls.

Place search at the top when itʼs important to defer to content at the bottom of the screen, or thereʼs no bottom toolbar. Use search at the top in cases where covering the content might interfere with a primary function of the app. The Wallet app, for example, includes event passes in a stack at the bottom of the screen for easy access and viewing at a glance.


#### Search as an inline field
In some cases you might want your app to include a search field inline with content.

Place search as an inline field when its position alongside the content it searches strengthens that relationship. When you need to filter or search within a single view, it can be helpful to have search appear directly next to content to illustrate that the search applies to it, rather than globally. For example, although the main search in the Music app is in the tab bar, people can navigate to their library and use an inline search field to filter their songs and albums.

Prefer placing search at the bottom. Generally, even for search that applies to a subset of your app’s content, it’s better to locate search where people can reach it easily. The Settings app, for example, places search at the bottom both for its top-level search and for search in the section for individual apps. If there isn’t space at the bottom (because it’s occupied by a tab bar or other important UI, for example), it’s okay to place search inline at the top.

When at the top, position an inline search field above the list it searches, and pin it to the top toolbar when scrolling. This helps keep it distinct from search that appears in other locations.


### iPadOS, macOS
The placement and behavior of the search field in iPadOS and macOS is similar; on both platforms, clearing the field exits search and dismisses the keyboard if present. If your app is available on both iPad and Mac, try to keep the search experience as consistent as possible across both platforms.

Put a search field at the trailing side of the toolbar for many common uses. Many apps benefit from the familiar pattern of search in the toolbar, particularly apps with split views or apps that navigate between multiple sources, like Mail, Notes, and Voice Memos. The persistent availability of search at the side of the toolbar gives it a global presence within your app, so it’s generally appropriate to start with a global scope for the initial search.

Include search at the top of the sidebar when filtering content or navigation there. Apps such as Settings take advantage of search to quickly filter the sidebar and expose sections that may be multiple levels deep, providing a simple way for people to search, preview, and navigate to the section or setting they’re looking for.

Include search as an item in the sidebar or tab bar when you want an area dedicated to discovery. If your search is paired with rich suggestions, categories, or content that needs more space, it can be helpful to have a dedicated area for it. This is particularly true for apps where browsing and search go hand in hand, like Music and TV, where it provides a unified location to highlight suggested content, categories, and recent searches. A dedicated area also ensures search is always available as people navigate and switch sections of your app.

In a search field in a dedicated area, consider immediately focusing the field when a person navigates to the section to help people search faster and locate the field itself more easily. An exception to this is on iPad when only a virtual keyboard is available, in which case it’s better to leave the field unfocused to prevent the keyboard from unexpectedly covering the view.

Account for window resizing with the placement of the search field. On iPad, the search field fluidly resizes with the app window like it does on Mac. However, for compact views on iPad, itʼs important to ensure that search is available where it’s most contextually useful. For example, Notes and Mail place search above the column for the content list when they resize down to a compact view.


### tvOS
A search screen is a specialized keyboard screen that helps people enter search text, displaying search results beneath the keyboard in a fully customizable view. For developer guidance, see UISearchController.

Provide suggestions to make searching easier. People typically don’t want to do a lot of typing in tvOS. To improve the search experience, provide popular and context-specific search suggestions, including recent searches when available. For developer guidance, see Using suggested searches with a search controller.


### watchOS
When someone taps the search field, the system displays a text-input control that covers the entire screen. The app only returns to the search field after they tap the Cancel or Search button.


## Resources

#### Related

#### Developer documentation
Adding a search interface to your app — SwiftUI

searchable(text:placement:prompt:) — SwiftUI

UISearchTextField — UIKit

NSSearchField — AppKit


#### Videos

## Change log
Updated guidance for search placement in iOS, consolidated iPadOS and macOS platform considerations, and added guidance for tokens.

Combined guidance common to all platforms.

Added guidance for using search fields in watchOS.


---

### Sidebars

**Source**: [https://developer.apple.com/design/human-interface-guidelines/sidebars](https://developer.apple.com/design/human-interface-guidelines/sidebars)


# Sidebars
A sidebar floats above content without being anchored to the edges of the view. It provides a broad, flat view of an app’s information hierarchy, giving people access to several peer content areas or modes at the same time.

A sidebar requires a large amount of vertical and horizontal space. When space is limited or you want to devote more of the screen to other information or functionality, a more compact control such as a tab bar may provide a better navigation experience. For guidance, see Layout.


## Best practices
Extend content beneath the sidebar. In iOS, iPadOS, and macOS, as with other controls such as toolbars and tab bars, sidebars float above content in the Liquid Glass layer. To reinforce the separation and floating appearance of the sidebar, extend content beneath it either by letting it horizontally scroll or applying a background extension view, which mirrors adjacent content to give the impression of stretching it under the sidebar. For developer guidance, see backgroundExtensionEffect().

When possible, let people customize the contents of a sidebar. A sidebar lets people navigate to important areas in your app, so it works well when people can decide which areas are most important and in what order they appear.

Group hierarchy with disclosure controls if your app has a lot of content. Using disclosure controls helps keep the sidebar’s vertical space to a manageable level.

Consider using familiar symbols to represent items in the sidebar. SF Symbols provides a wide range of customizable symbols you can use to represent items in your app. If you need to use a custom icon, consider creating a custom symbol rather than using a bitmap image. Download the SF Symbols app from Apple Design Resources.

Consider letting people hide the sidebar. People sometimes want to hide the sidebar to create more room for content details or to reduce distraction. When possible, let people hide and show the sidebar using the platform-specific interactions they already know. For example, in iPadOS, people expect to use the built-in edge swipe gesture; in macOS, you can include a show/hide button or add Show Sidebar and Hide Sidebar commands to your app’s View menu. In visionOS, a window typically expands to accommodate a sidebar, so people rarely need to hide it. Avoid hiding the sidebar by default to ensure that it remains discoverable.

In general, show no more than two levels of hierarchy in a sidebar. When a data hierarchy is deeper than two levels, consider using a split view interface that includes a content list between the sidebar items and detail view.

If you need to include two levels of hierarchy in a sidebar, use succinct, descriptive labels to title each group. To help keep labels short, omit unnecessary words.


## Platform considerations
No additional considerations for tvOS. Not supported in watchOS.


### iOS
Avoid using a sidebar. A sidebar takes up a lot of space in landscape orientation and isn’t available in portrait orientation. Instead, consider using a tab bar, which takes less space and remains visible in both orientations.


### iPadOS
When you use the sidebarAdaptable style of tab view to present a sidebar, you choose whether to display a sidebar or a tab bar when your app opens. Both variations include a button that people can use to switch between them. This style also responds automatically to rotation and window resizing, providing a version of the control that’s appropriate to the width of the view.

To display a sidebar only, use NavigationSplitView to present a sidebar in the primary pane of a split view, or use UISplitViewController.

Consider using a tab bar first. A tab bar provides more space to feature content, and offers enough flexibility to navigate between many apps’ main areas. If you need to expose more areas than fit in a tab bar, the tab bar’s convertible sidebar-style appearance can provide access to content that people use less frequently. For guidance, see Tab bars.

If necessary, apply the correct appearance to a sidebar. If you’re not using SwiftUI to create a sidebar, you can use the UICollectionLayoutListConfiguration.Appearance.sidebar appearance of a collection view list layout. For developer guidance, see UICollectionLayoutListConfiguration.Appearance.


### macOS
A sidebar’s row height, text, and glyph size depend on its overall size, which can be small, medium, or large. You can set the size programmatically, but people can also change it by selecting a different sidebar icon size in General settings.

Avoid stylizing your app by specifying a fixed color for all sidebar icons. By default, sidebar icons use the current accent color and people expect to see their chosen accent color throughout all the apps they use. Although a fixed color can help clarify the meaning of an icon, you want to make sure that most sidebar icons display the color people choose.

Consider automatically hiding and revealing a sidebar when its container window resizes. For example, reducing the size of a Mail viewer window can automatically collapse its sidebar, making more room for message content.

Avoid putting critical information or actions at the bottom of a sidebar. People often relocate a window in a way that hides its bottom edge.


### visionOS
If your app’s hierarchy is deep, consider using a sidebar within a tab in a tab bar. In this situation, a sidebar can support secondary navigation within the tab. If you do this, be sure to prevent selections in the sidebar from changing which tab is currently open.


## Resources

#### Related

#### Developer documentation
sidebarAdaptable — SwiftUI

NavigationSplitView — SwiftUI

UICollectionLayoutListConfiguration — UIKit

NSSplitViewController — AppKit


#### Videos

## Change log
Added guidance for extending content beneath the sidebar.

Updated guidance to include the SwiftUI adaptable sidebar style.

Added artwork for iPadOS.

Updated to include guidance for visionOS.


---

### Tab Bars

**Source**: [https://developer.apple.com/design/human-interface-guidelines/tab-bars](https://developer.apple.com/design/human-interface-guidelines/tab-bars)


# Tab bars
Tab bars help people understand the different types of information or functionality that an app provides. They also let people quickly switch between sections of the view while preserving the current navigation state within each section.


## Best practices
Use a tab bar to support navigation, not to provide actions. A tab bar lets people navigate among different sections of an app, like the Alarm, Stopwatch, and Timer tabs in the Clock app. If you need to provide controls that act on elements in the current view, use a toolbar instead.

Make sure the tab bar is visible when people navigate to different sections of your app. If you hide the tab bar, people can forget which area of the app they’re in. The exception is when a modal view covers the tab bar, because a modal is temporary and self-contained.

Use the appropriate number of tabs required to help people navigate your app. As a representation of your app’s hierarchy, it’s important to weigh the complexity of additional tabs against the need for people to frequently access each section; keep in mind that it’s generally easier to navigate among fewer tabs. Where available, consider a sidebar or a tab bar that adapts to a sidebar as an alternative for an app with a complex information structure.

Avoid overflow tabs. Depending on device size and orientation, the number of visible tabs can be smaller than the total number of tabs. If horizontal space limits the number of visible tabs, the trailing tab becomes a More tab in iOS and iPadOS, revealing the remaining items in a separate list. The More tab makes it harder for people to reach and notice content on tabs that are hidden, so limit scenarios in your app where this can happen.

Don’t disable or hide tab bar buttons, even when their content is unavailable. Having tab bar buttons available in some cases but not others makes your app’s interface appear unstable and unpredictable. If a section is empty, explain why its content is unavailable.

Include tab labels to help with navigation. A tab label appears beneath or beside a tab bar icon, and can aid navigation by clearly describing the type of content or functionality the tab contains. Use single words whenever possible.

Consider using SF Symbols to provide familiar, scalable tab bar icons. When you use SF Symbols, tab bar icons automatically adapt to different contexts. For example, the tab bar can be regular or compact, depending on the device and orientation. Tab bar icons appear above tab labels in compact views, whereas in regular views, the icons and labels appear side by side. Prefer filled symbols or icons for consistency with the platform.

If you’re creating custom tab bar icons, see Apple Design Resources for tab bar icon dimensions.

Use a badge to indicate that critical information is available. You can display a badge — a red oval containing white text and either a number or an exclamation point — on a tab to indicate that there’s new or updated information in the section that warrants a person’s attention. Reserve badges for critical information so you don’t dilute their impact and meaning. For guidance, see Notifications.

Avoid applying a similar color to tab labels and content layer backgrounds. If your app already has bright, colorful content in the content layer, prefer a monochromatic appearance for tab bars, or choose an accent color with sufficient visual differentiation. For more guidance, see Liquid Glass color.


## Platform considerations
No additional considerations for macOS. Not supported in watchOS.


### iOS
A tab bar floats above content at the bottom of the screen. Its items rest on a Liquid Glass background that allows content beneath to peek through.

For tab bars with an attached accessory, like the MiniPlayer in Music, you can choose to minimize the tab bar and move the accessory inline with it when a person scrolls down. A person can exit the minimized state by tapping a tab or scrolling to the top of the view. For developer guidance, see TabBarMinimizeBehavior and UITabBarController.MinimizeBehavior.

A tab bar with an attached accessory, expanded

A tab bar with an attached accessory, minimized

A tab bar can include a distinct search item at the trailing end. For guidance, see Search fields.


### iPadOS
The system displays a tab bar near the top of the screen. You can choose to have the tab bar appear as a fixed element, or with a button that converts it to a sidebar. For developer guidance, see tabBarOnly and sidebarAdaptable.

- Tab bar
- Sidebar
To present a sidebar without the option to convert it to a tab bar, use a navigation split view instead of a tab view. For guidance, see Sidebars.

Prefer a tab bar for navigation. A tab bar provides access to the sections of your app that people use most. If your app is more complex, you can provide the option to convert the tab bar to a sidebar so people can access a wider set of navigation options.

Let people customize the tab bar. In apps with a lot of sections that people might want to access, it can be useful to let people select items that they use frequently and add them to the tab bar, or remove items that they use less frequently. For example, in the Music app, a person can choose a favorite playlist to display in the tab bar. If you let people select their own tabs, aim for a default list of five or fewer to preserve continuity between compact and regular view sizes. For developer guidance, see TabViewCustomization and UITab.Placement.


### tvOS
A tab bar is highly customizable. For example, you can:

- Specify a tint, color, or image for the tab bar background
- Choose a font for tab items, including a different font for the selected item
- Specify tints for selected and unselected items
- Add button icons, like settings and search
By default, a tab bar is translucent, and only the selected tab is opaque. When people use the remote to focus on the tab bar, the selected tab includes a drop shadow that emphasizes its selected state. The height of a tab bar is 68 points, and its top edge is 46 points from the top of the screen; you can’t change either of these values.

If there are more items than can fit in the tab bar, the system truncates the rightmost item by applying a fade effect that begins at the right side of the tab bar. If there are enough items to cause scrolling, the system also applies a truncating fade effect that starts from the left side.

Be aware of tab bar scrolling behaviors. By default, people can scroll the tab bar offscreen when the current tab contains a single main view. You can see examples of this behavior in the Watch Now, Movies, TV Show, Sports, and Kids tabs in the TV app. The exception is when a screen contains a split view, such as the TV app’s Library tab or an app’s Settings screen. In this case, the tab bar remains pinned at the top of the view while people scroll the content within the primary and secondary panes of the split view. Regardless of a tab’s contents, focus always returns to the tab bar at the top of the page when people press Menu on the remote.

In a live-viewing app, organize tabs in a consistent way. For the best experience, organize content in live-streaming apps with tabs in the following order:

- Live content
- Cloud DVR or other recorded content
- Other content
For additional guidance, see Live-viewing apps.


### visionOS
In visionOS, a tab bar is always vertical, floating in a position that’s fixed relative to the window’s leading side. When people look at a tab bar, it automatically expands; to open a specific tab, people look at the tab and tap. While a tab bar is expanded, it can temporarily obscure the content behind it.

Supply a symbol and a text label for each tab. A tab’s symbol is always visible in the tab bar. When people look at the tab bar, the system reveals tab labels, too. Even though the tab bar expands, you need to keep tab labels short so people can read them at a glance.

If it makes sense in your app, consider using a sidebar within a tab. If your app’s hierarchy is deep, you might want to use a sidebar to support secondary navigation within a tab. If you do this, be sure to prevent selections in the sidebar from changing which tab is currently open.


## Resources

#### Related

#### Developer documentation
TabViewBottomAccessoryPlacement — SwiftUI

Enhancing your app’s content with tab navigation — SwiftUI

Elevating your iPad app with a tab bar and sidebar — UIKit


#### Videos

## Change log
Updated guidance for Liquid Glass.

Added guidance for Liquid Glass.

Added art representing the tab bar in iPadOS 18.

Updated with guidance for the tab bar in iPadOS 18.

Updated to include guidance for visionOS.


---

### Token Fields

**Source**: [https://developer.apple.com/design/human-interface-guidelines/token-fields](https://developer.apple.com/design/human-interface-guidelines/token-fields)


# Token fields
For example, Mail uses token fields for the address fields in the compose window. As people enter recipients, Mail converts the text that represents each recipient’s name into a token. People can select these recipient tokens and drag to reorder them or move them into a different field.

You can configure a token field to present people with a list of suggestions as they enter text into the field. For example, Mail suggests recipients as people type in an address field. When people select a suggested recipient, Mail inserts the recipient into the field as a token.

An individual token can also include a contextual menu that offers information about the token or editing options. For example, a recipient token in Mail includes a contextual menu with commands for editing the recipient name, marking the recipient as a VIP, and viewing the recipient’s contact card, among others.

Tokens can also represent search terms in some situations; for guidance, see Search fields.


## Best practices
Add value with a context menu. People often benefit from a context menu with additional options or information about a token.

Consider providing additional ways to convert text into tokens. By default, text people enter turns into a token whenever they type a comma. You can specify additional shortcuts, such as pressing Return, that also invoke this action.

Consider customizing the delay the system uses before showing suggested tokens. By default, suggestions appear immediately. However, suggestions that appear too quickly may distract people while they’re typing. If your app suggests tokens, consider adjusting the delay to a comfortable level.


## Platform considerations
Not supported in iOS, iPadOS, tvOS, visionOS, and watchOS.


## Resources

#### Related

#### Developer documentation
NSTokenField — AppKit


---

## Presentation

### Action Sheets

**Source**: [https://developer.apple.com/design/human-interface-guidelines/action-sheets](https://developer.apple.com/design/human-interface-guidelines/action-sheets)


# Action sheets
When you use SwiftUI, you can offer action sheet functionality in all platforms by specifying a presentation modifier for a confirmation dialog. If you use UIKit, you use the UIAlertController.Style.actionSheet to display an action sheet in iOS, iPadOS, and tvOS.


## Best practices
Use an action sheet — not an alert — to offer choices related to an intentional action. For example, when people cancel the message they’re editing in Mail on iPhone, an action sheet provides two choices: delete the draft, or save the draft. Although an alert can also help people confirm or cancel an action that has destructive consequences, it doesn’t provide additional choices related to the action. More importantly, an alert is usually unexpected, generally telling people about a problem or a change in the current situation that might require them to act. For guidance, see Alerts.

Use action sheets sparingly. Action sheets give people important information and choices, but they interrupt the current task to do so. To encourage people to pay attention to action sheets, avoid using them more than necessary.

Aim to keep titles short enough to display on a single line. A long title is difficult to read quickly and might get truncated or require people to scroll.

Provide a message only if necessary. In general, the title — combined with the context of the current action — provides enough information to help people understand their choices.

If necessary, provide a Cancel button that lets people reject an action that might destroy data. Place the Cancel button at the bottom of the action sheet (or in the upper-left corner of the sheet in watchOS). A SwiftUI confirmation dialog includes a Cancel button by default.

Make destructive choices visually prominent. Use the destructive style for buttons that perform destructive actions, and place these buttons at the top of the action sheet where they tend to be most noticeable. For developer guidance, see destructive (SwiftUI) or UIAlertAction.Style.destructive (UIKit).


## Platform considerations
No additional considerations for macOS or tvOS. Not supported in visionOS.


### iOS, iPadOS
Use an action sheet — not a menu — to provide choices related to an action. People are accustomed to having an action sheet appear when they perform an action that might require clarifying choices. In contrast, people expect a menu to appear when they choose to reveal it.

Avoid letting an action sheet scroll. The more buttons an action sheet has, the more time and effort it takes for people to make a choice. Also, scrolling an action sheet can be hard to do without inadvertently tapping a button.


### watchOS
The system-defined style for action sheets includes a title, an optional message, a Cancel button, and one or more additional buttons. The appearance of this interface is different depending on the device.

Each button has an associated style that conveys information about the button’s effect. There are three system-defined button styles:

The button has no special meaning.

The button destroys user data or performs a destructive action in the app.

The button dismisses the view without taking any action.

Avoid displaying more than four buttons in an action sheet, including the Cancel button. When there are fewer buttons onscreen, it’s easier for people to view all their options at once. Because the Cancel button is required, aim to provide no more than three additional choices.


## Resources

#### Related

#### Developer documentation
confirmationDialog(_:isPresented:titleVisibility:actions:) — SwiftUI

UIAlertController.Style.actionSheet — UIKit


---

### Alerts

**Source**: [https://developer.apple.com/design/human-interface-guidelines/alerts](https://developer.apple.com/design/human-interface-guidelines/alerts)


# Alerts
For example, an alert can tell people about a problem, warn them when their action might destroy data, and give them an opportunity to confirm a purchase or another important action they initiated.


## Best practices
Use alerts sparingly. Alerts give people important information, but they interrupt the current task to do so. Encourage people to pay attention to your alerts by making certain that each one offers only essential information and useful actions.

Avoid using an alert merely to provide information. People don’t appreciate an interruption from an alert that’s informative, but not actionable. If you need to provide only information, prefer finding an alternative way to communicate it within the relevant context. For example, when a server connection is unavailable, Mail displays an indicator that people can choose to learn more.

Avoid displaying alerts for common, undoable actions, even when they’re destructive. For example, you don’t need to alert people about data loss every time they delete an email or file because they do so with the intention of discarding data, and they can undo the action. In comparison, when people take an uncommon destructive action that they can’t undo, it’s important to display an alert in case they initiated the action accidentally.

Avoid showing an alert when your app starts. If you need to inform people about new or important information the moment they open your app, design a way to make the information easily discoverable. If your app detects a problem at startup, like no network connection, consider alternative ways to let people know. For example, you could show cached or placeholder data and a nonintrusive label that describes the problem.


## Anatomy
An alert is a modal view that can look different in different platforms and devices.

- iOS
- macOS
- tvOS
- visionOS
- watchOS

## Content
In all platforms, alerts display a title, optional informative text, and up to three buttons. On some platforms, alerts can include additional elements.

- In iOS, iPadOS, macOS, and visionOS, an alert can include a text field.
- Alerts in macOS and visionOS can include an icon and an accessory view.
- macOS alerts can add a suppression checkbox and a Help button.
In all alert copy, be direct, and use a neutral, approachable tone. Alerts often describe problems and serious situations, so avoid being oblique or accusatory, or masking the severity of the issue.

Write a title that clearly and succinctly describes the situation. You need to help people quickly understand the situation, so be complete and specific, without being verbose. As much as possible, describe what happened, the context in which it happened, and why. Avoid writing a title that doesn’t convey useful information — like “Error” or “Error 329347 occurred” — but also avoid overly long titles that wrap to more than two lines. If the title is a complete sentence, use sentence-style capitalization and appropriate ending punctuation. If the title is a sentence fragment, use title-style capitalization, and don’t add ending punctuation.

Include informative text only if it adds value. If you need to add an informative message, keep it as short as possible, using complete sentences, sentence-style capitalization, and appropriate punctuation.

Avoid explaining alert buttons. If your alert text and button titles are clear, you don’t need to explain what the buttons do. In rare cases where you need to provide guidance on choosing a button, use a term like choose to account for people’s current device and interaction method, and refer to a button using its exact title without quotes. For guidance, see Buttons.

If supported, include a text field only if you need people’s input to resolve the situation. For example, you might need to present a secure text field to receive a password.


## Buttons
Create succinct, logical button titles. Aim for a one- or two-word title that describes the result of selecting the button. Prefer verbs and verb phrases that relate directly to the alert text — for example, “View All,” “Reply,” or “Ignore.” In informational alerts only, you can use “OK” for acceptance, avoiding “Yes” and “No.” Always use “Cancel” to title a button that cancels the alert’s action. As with all button titles, use title-style capitalization and no ending punctuation.

Avoid using OK as the default button title unless the alert is purely informational. The meaning of “OK” can be unclear even in alerts that ask people to confirm that they want to do something. For example, does “OK” mean “OK, I want to complete the action” or “OK, I now understand the negative results my action would have caused”? A specific button title like “Erase,” “Convert,” “Clear,” or “Delete” helps people understand the action they’re taking.

Place buttons where people expect. In general, place the button people are most likely to choose on the trailing side in a row of buttons or at the top in a stack of buttons. Always place the default button on the trailing side of a row or at the top of a stack. Cancel buttons are typically on the leading side of a row or at the bottom of a stack.

Use the destructive style to identify a button that performs a destructive action people didn’t deliberately choose. For example, when people deliberately choose a destructive action — such as Empty Trash — the resulting alert doesn’t apply the destructive style to the Empty Trash button because the button performs the person’s original intent. In this scenario, the convenience of pressing Return to confirm the deliberately chosen Empty Trash action outweighs the benefit of reaffirming that the button is destructive. In contrast, people appreciate an alert that draws their attention to a button that can perform a destructive action they didn’t originally intend.

If there’s a destructive action, include a Cancel button to give people a clear, safe way to avoid the action. Always use the title “Cancel” for a button that cancels an alert’s action. Note that you don’t want to make a Cancel button the default button. If you want to encourage people to read an alert and not just automatically press Return to dismiss it, avoid making any button the default button. Similarly, if you must display an alert with a single button that’s also the default, use a Done button, not a Cancel button.

Provide alternative ways to cancel an alert when it makes sense. In addition to choosing a Cancel button, people appreciate using keyboard shortcuts or other quick ways to cancel an onscreen alert. For example:

Exit to the Home Screen

Pressing Escape (Esc) or Command-Period (.) on an attached keyboard

iOS, iPadOS, macOS, visionOS

Pressing Menu on the remote


## Platform considerations
No additional considerations for tvOS or watchOS.


### iOS, iPadOS
Use an action sheet — not an alert — to offer choices related to an intentional action. For example, when people cancel the Mail message they’re editing, an action sheet provides three choices: delete the edits (or the entire draft), save the draft, or return to editing. Although an alert can also help people confirm or cancel an action that has destructive consequences, it doesn’t provide additional choices related to the action. For guidance, see Action sheets.

When possible, avoid displaying an alert that scrolls. Although an alert might scroll if the text size is large enough, be sure to minimize the potential for scrolling by keeping alert titles short and including a brief message only when necessary.

macOS automatically displays your app icon in an alert, but you can supply an alternative icon or symbol. In addition, macOS lets you:

- Configure repeating alerts to let people suppress subsequent occurrences of the same alert.
- Append a custom view if it’s necessary to provide additional information (for developer guidance, see accessoryView).
- Include a Help button that opens your help documentation (see Help buttons).
Use a caution symbol sparingly. Using a caution symbol like exclamationmark.triangle too frequently in your alerts diminishes its significance. Use the symbol only when extra attention is really needed, as when confirming an action that might result in unexpected loss of data. Don’t use the symbol for tasks whose only purpose is to overwrite or remove data, such as a save or empty trash.

When your app is running in the Shared Space, visionOS displays an alert in front of the app’s window, slightly forward along the z-axis.

If someone moves a window without dismissing its alert, the alert remains anchored to the window. If your app is running in a Full Space, the system displays the alert centered in the wearer’s field of view.

If you need to display an accessory view in a visionOS alert, create a view that has a maximum height of 154 pt and a 16-pt corner radius.


## Resources

#### Related

#### Developer documentation
alert(_:isPresented:actions:) — SwiftUI

UIAlertController — UIKit


## Change log
Enhanced guidance for using default and Cancel buttons.

Added anatomy artwork for visionOS.

Updated to include guidance for visionOS.


---

### Page Controls

**Source**: [https://developer.apple.com/design/human-interface-guidelines/page-controls](https://developer.apple.com/design/human-interface-guidelines/page-controls)


# Page controls
The scrolling row of indicators helps people navigate the list to find the page they want. Page controls can handle an arbitrary number of pages, making them particularly useful in situations where people can create custom lists.

Page controls appear as a series of small indicator dots by default, representing the available pages. A solid dot denotes the current page. Visually, these dots are always equidistant, and are clipped if there are too many to fit in the window.


## Best practices
Use page controls to represent movement between an ordered list of pages. Page controls don’t represent hierarchical or nonsequential page relationships. For more complex navigation, consider using a sidebar or split view instead.

Center a page control at the bottom of the view or window. To ensure people always know where to find a page control, center it horizontally and position it near the bottom of the view.

Although page controls can handle any number of pages, don’t display too many. More than about 10 dots are hard to count at a glance. If your app needs to display more than 10 pages as peers, consider using a different arrangement‚ such as a grid, that lets people navigate the content in any order.


## Customizing indicators
By default, a page control uses the system-provided dot image for all indicators, but it can also display a unique image to help people identify a specific page. For example, Weather uses the location.fill symbol to distinguish the current location’s page.

If it enhances your app or game, you can provide a custom image to use as the default image for all indicators and you can also supply a different image for a specific page. For developer guidance, see preferredIndicatorImage and setIndicatorImage(_:forPage:).

Make sure custom indicator images are simple and clear. Avoid complex shapes, and don’t include negative space, text, or inner lines, because these details can make an icon muddy and indecipherable at very small sizes. Consider using simple SF Symbols as indicators or design your own icons. For guidance, see Icons.

Customize the default indicator image only when it enhances the page control’s overall meaning. For example, if every page you list contains bookmarks, you might use the bookmark.fill symbol as the default indicator image.

Avoid using more than two different indicator images in a page control. If your list contains one page with special meaning — like the current-location page in Weather — you can make the page easy to find by giving it a unique indicator image. In contrast, a page control that uses several unique images to mark several important pages is hard to use because people must memorize the meaning of each image. A page control that displays more than two types of indicator images tends to look messy and haphazard, even when each image is clear.

Using several different indicators can make a page control look busy and difficult to use.

Using only two different indicators looks well-organized and provides a consistent experience.

Avoid coloring indicator images. Custom colors can reduce the contrast that differentiates the current-page indicator and makes the page control visible on the screen. To ensure that your page control is easy to use and looks good in different contexts, let the system automatically color the indicators.


## Platform considerations
Not supported in macOS.


### iOS, iPadOS
A page control can adjust the appearance of indicators to provide more information about the list. For example, the control highlights the indicator of the current page so people can estimate the page’s relative position in the list. When there are more indicators than fit in the space, the control can shrink indicators at both sides to suggest that more pages are available.

People interact with page controls by tapping or scrubbing (to scrub, people touch the control and drag left or right). Tapping on the leading or trailing side of the current-page indicator reveals the next or previous page; in iPadOS, people can also use the pointer to target a specific indicator. Scrubbing opens pages in sequence, and scrubbing past the leading or trailing edge of the control helps people quickly reach the first or last page.

In the API, tapping is a discrete interaction, whereas scrubbing is a continuous interaction; for developer guidance, see UIPageControl.InteractionState.

Avoid animating page transitions during scrubbing. People can scrub very quickly, and using the scrolling animation for every transition can make your app lag and cause distracting visual flashes. Use the animated scrolling transition only for tapping.

A page control can include a translucent, rounded-rectangle background appearance that provides visual contrast for the indicators. You can choose one of the following background styles:

- Automatic — Displays the background only when people interact with the control. Use this style when the page control isn’t the primary navigational element in the UI.
- Prominent — Always displays the background. Use this style only when the control is the primary navigational control in the screen.
- Minimal — Never displays the background. Use this style when you just want to show the position of the current page in the list and you don’t need to provide visual feedback during scrubbing.
For developer guidance, see backgroundStyle.

Avoid supporting the scrubber when you use the minimal background style. The minimal style doesn’t provide visual feedback during scrubbing. If you want to let people scrub a list of pages in your app, use the automatic or prominent background styles.


### tvOS
Use page controls on collections of full-screen pages. A page control is designed to operate in a full-screen environment where multiple content-rich pages are peers in the page hierarchy. Inclusion of additional controls makes it difficult to maintain focus while moving between pages.


### visionOS
In visionOS, page controls represent available pages and indicate the current page, but people don’t interact with them.


### watchOS
In watchOS, page controls can be displayed at the bottom of the screen for horizontal pagination, or next to the Digital Crown when presenting a vertical tab view. When using vertical tab views, the page indicator shows people where they are in the navigation, both within the current page and within the set of pages. The page control transitions between scrolling through a page’s content and scrolling to other pages.

Vertical page control

Horizontal page control

Use vertical pagination to separate multiple views into distinct, purposeful pages. Give each page a clear purpose, and let people scroll through the pages using the Digital Crown. In watchOS, this design is more effective than horizontal pagination or many levels of hierarchical navigation.

Consider limiting the content of an individual page to a single screen height. Embracing this constraint encourages each page to serve a clear and distinct purpose and results in a more glanceable design. Use variable-height pages judiciously and, if possible, only place them after fixed-height pages in your app design.


## Resources

#### Related

#### Developer documentation
PageTabViewStyle — SwiftUI

UIPageControl — UIKit


## Change log
Updated to include guidance for visionOS.

Updated guidance for using page controls in watchOS.


---

### Panels

**Source**: [https://developer.apple.com/design/human-interface-guidelines/panels](https://developer.apple.com/design/human-interface-guidelines/panels)


# Panels
In general, a panel has a less prominent appearance than an app’s main window. When the situation calls for it, a panel can also use a dark, translucent style to support a heads-up display (or HUD) experience.

When your app runs in other platforms, consider using a modal view to present supplementary content that’s relevant to the current task or selection. For guidance, see Modality.


## Best practices
Use a panel to give people quick access to important controls or information related to the content they’re working with. For example, you might use a panel to provide controls or settings that affect the selected item in the active document or window.

Consider using a panel to present inspector functionality. An inspector displays the details of the currently selected item, automatically updating its contents when the item changes or when people select a new item. In contrast, if you need to present an Info window — which always maintains the same contents, even when the selected item changes — use a regular window, not a panel. Depending on the layout of your app, you might also consider using a split view pane to present an inspector.

Prefer simple adjustment controls in a panel. As much as possible, avoid including controls that require typing text or selecting items to act upon because these actions can require multiple steps. Instead, consider using controls like sliders and steppers because these components can give people more direct control.

Write a brief title that describes the panel’s purpose. Because a panel often floats above other open windows in your app, it needs a title bar so people can position it where they want. Create a short title using a noun — or a noun phrase with title-style capitalization — that can help people recognize the panel onscreen. For example, macOS provides familiar panels titled “Fonts” and “Colors,” and many apps use the title “Inspector.”

Show and hide panels appropriately. When your app becomes active, bring all of its open panels to the front, regardless of which window was active when the panel opened. When your app is inactive, hide all of its panels.

Avoid including panels in the Window menu’s documents list. It’s fine to include commands for showing or hiding panels in the Window menu, but panels aren’t documents or standard app windows, and they don’t belong in the Window menu’s list.

In general, avoid making a panel’s minimize button available. People don’t usually need to minimize a panel, because it displays only when needed and disappears when the app is inactive.

Refer to panels by title in your interface and in help documentation. In menus, use the panel’s title without including the term panel: for example, “Show Fonts,” “Show Colors,” and “Show Inspector.” In help documentation, it can be confusing to introduce “panel” as a different type of window, so it’s generally best to refer to a panel by its title or — when it adds clarity — by appending window to the title. For example, the title “Inspector” often supplies enough context to stand on its own, whereas it can be clearer to use “Fonts window” and “Colors window” instead of just “Fonts” and “Colors.”


## HUD-style panels
A HUD-style panel serves the same function as a standard panel, but its appearance is darker and translucent. HUDs work well in apps that present highly visual content or that provide an immersive experience, such as media editing or a full-screen slide show. For example, QuickTime Player uses a HUD to display inspector information without obstructing too much content.

Prefer standard panels. People can be distracted or confused by a HUD when there’s no logical reason for its presence. Also, a HUD might not match the current appearance setting. In general, use a HUD only:

- In a media-oriented app that presents movies, photos, or slides
- When a standard panel would obscure essential content
- When you don’t need to include controls — with the exception of the disclosure triangle, most system-provided controls don’t match a HUD’s appearance.
Maintain one panel style when your app switches modes. For example, if you use a HUD when your app is in full-screen mode, prefer maintaining the HUD style when people take your app out of full-screen mode.

Use color sparingly in HUDs. Too much color in the dark appearance of a HUD can be distracting. Often, you need only small amounts of high-contrast color to highlight important information in a HUD.

Keep HUDs small. HUDs are designed to be unobtrusively useful, so letting them grow too large defeats their primary purpose. Don’t let a HUD obscure the content it adjusts, and make sure it doesn’t compete with the content for people’s attention.

For developer guidance, see hudWindow.


## Platform considerations
Not supported in iOS, iPadOS, tvOS, visionOS, or watchOS.


## Resources

#### Related

#### Developer documentation

---

### Popovers

**Source**: [https://developer.apple.com/design/human-interface-guidelines/popovers](https://developer.apple.com/design/human-interface-guidelines/popovers)


# Popovers

## Best practices
Use a popover to expose a small amount of information or functionality. Because a popover disappears after people interact with it, limit the amount of functionality in the popover to a few related tasks. For example, a calendar event popover makes it easy for people to change the date or time of an event, or to move it to another calendar. The popover disappears after the change, letting people continue reviewing the events on their calendar.

Consider using popovers when you want more room for content. Views like sidebars and panels take up a lot of space. If you need content only temporarily, displaying it in a popover can help streamline your interface.

Position popovers appropriately. Make sure a popover’s arrow points as directly as possible to the element that revealed it. Ideally, a popover doesn’t cover the element that revealed it or any essential content people may need to see while using it.

Use a Close button for confirmation and guidance only. A Close button, including Cancel or Done, is worth including if it provides clarity, like exiting with or without saving changes. Otherwise, a popover generally closes when people click or tap outside its bounds or select an item in the popover. If multiple selections are possible, make sure the popover remains open until people explicitly dismiss it or they click or tap outside its bounds.

Always save work when automatically closing a nonmodal popover. People can unintentionally dismiss a nonmodal popover by clicking or tapping outside its bounds. Discard people’s work only when they click or tap an explicit Cancel button.

Show one popover at a time. Displaying multiple popovers clutters the interface and causes confusion. Never show a cascade or hierarchy of popovers, in which one emerges from another. If you need to show a new popover, close the open one first.

Don’t show another view over a popover. Make sure nothing displays on top of a popover, except for an alert.

When possible, let people close one popover and open another with a single click or tap. Avoiding extra gestures is especially desirable when several different bar buttons each open a popover.

Avoid making a popover too big. Make a popover only big enough to display its contents and point to the place it came from. If necessary, the system can adjust the size of a popover to ensure it fits well in the interface.

Provide a smooth transition when changing the size of a popover. Some popovers provide both condensed and expanded views of the same information. If you adjust the size of a popover, animate the change to avoid giving the impression that a new popover replaced the old one.

Avoid using the word popover in help documentation. Instead, refer to a specific task or selection. For example, instead of “Select the Show button at the bottom of the popover,” you might write “Select the Show button.”

Avoid using a popover to show a warning. People can miss a popover or accidentally close it. If you need to warn people, use an alert instead.


## Platform considerations
No additional considerations for visionOS. Not supported in tvOS or watchOS.


### iOS, iPadOS
Avoid displaying popovers in compact views. Make your app or game dynamically adjust its layout based on the size class of the content area. Reserve popovers for wide views; for compact views, use all available screen space by presenting information in a full-screen modal view like a sheet instead. For related guidance, see Modality.


### macOS
You can make a popover detachable in macOS, which becomes a separate panel when people drag it. The panel remains visible onscreen while people interact with other content.

- Attached popover
- Detached popover
Consider letting people detach a popover. People might appreciate being able to convert a popover into a panel if they want to view other information while the popover remains visible.

Make minimal appearance changes to a detached popover. A panel that looks similar to the original popover helps people maintain context.


## Resources

#### Related

#### Developer documentation
popover(isPresented:attachmentAnchor:arrowEdge:content:) — SwiftUI

UIPopoverPresentationController — UIKit


---

### Scroll Views

**Source**: [https://developer.apple.com/design/human-interface-guidelines/scroll-views](https://developer.apple.com/design/human-interface-guidelines/scroll-views)


# Scroll views
The scroll view itself has no appearance, but it can display a translucent scroll indicator that typically appears after people begin scrolling the view’s content. Although the appearance and behavior of scroll indicators can vary per platform, all indicators provide visual feedback about the scrolling action. For example, in iOS, iPadOS, macOS, visionOS, and watchOS, the indicator shows whether the currently visible content is near the beginning, middle, or end of the view.


## Best practices
Support default scrolling gestures and keyboard shortcuts. People are accustomed to the systemwide scrolling behavior and expect it to work everywhere. If you build custom scrolling for a view, make sure your scroll indicators use the elastic behavior that people expect.

Make it apparent when content is scrollable. Because scroll indicators aren’t always visible, it can be helpful to make it obvious when content extends beyond the view. For example, displaying partial content at the edge of a view indicates that there’s more content in that direction. Although most people immediately try scrolling a view to discover if additional content is available, it’s considerate to draw their attention to it.

Avoid putting a scroll view inside another scroll view with the same orientation. Nesting scroll views that have the same orientation can create an unpredictable interface that’s difficult to control. It’s alright to place a horizontal scroll view inside a vertical scroll view (or vice versa), however.

Consider supporting page-by-page scrolling if it makes sense for your content. In some situations, people appreciate scrolling by a fixed amount of content per interaction instead of scrolling continuously. On most platforms, you can define the size of such a page — typically the current height or width of the view — and define an interaction that scrolls one page at a time. To help maintain context during page-by-page scrolling, you can define a unit of overlap, such as a line of text, a row of glyphs, or part of a picture, and subtract the unit from the page size. For developer guidance, see PagingScrollTargetBehavior.

In some cases, scroll automatically to help people find their place. Although people initiate almost all scrolling, automatic scrolling can be helpful when relevant content is no longer in view, such as when:

- Your app performs an operation that selects content or places the insertion point in an area that’s currently hidden. For example, when your app locates text that people are searching for, scroll the content to bring the new selection into view.
- People start entering information in a location that’s not currently visible. For example, if the insertion point is on one page and people navigate to another page, scroll back to the insertion point as soon as they begin to enter text.
- The pointer moves past the edge of the view while people are making a selection. In this case, follow the pointer by scrolling in the direction it moves.
- People select something and scroll to a new location before acting on the selection. In this case, scroll until the selection is in view before performing the operation.
In all cases, automatically scroll the content only as much as necessary to help people retain context. For example, if part of a selection is visible, you don’t need to scroll the entire selection into view.

If you support zoom, set appropriate maximum and minimum scale values. For example, zooming in on text until a single character fills the screen doesn’t make sense in most situations.


## Scroll edge effects
In iOS, iPadOS, and macOS, a scroll edge effect is a variable blur that provides a transition between a content area and an area with Liquid Glass controls, such as toolbars. In most cases, the system applies a scroll edge effect automatically when a pinned element overlaps with scrolling content. If you use custom controls or layouts, the effect might not appear, and you may need to add it manually. For developer guidance, see ScrollEdgeEffectStyle and UIScrollEdgeEffect.

There are two styles of scroll edge effect: soft and hard.

- Use a soft edge effect in most cases, especially in iOS and iPadOS, to provide a subtle transition that works well for toolbars and interactive elements like buttons.
- Use a hard edge effect primarily in macOS for a stronger, more opaque boundary that’s ideal for interactive text, backless controls, or pinned table headers that need extra clarity.
Only use a scroll edge effect when a scroll view is adjacent to floating interface elements. Scroll edge effects aren’t decorative. They don’t block or darken like overlays; they exist to clarify where controls and content meet.

Apply one scroll edge effect per view. In split view layouts on iPad and Mac, each pane can have its own scroll edge effect; in this case, keep them consistent in height to maintain alignment.


## Platform considerations

### iOS, iPadOS
Consider showing a page control when a scroll view is in page-by-page mode. Page controls show how many pages, screens, or other chunks of content are available and indicates which one is currently visible. For example, Weather uses a page control to indicate movement between people’s saved locations. If you show a page control with a scroll view, don’t show the scrolling indicator on the same axis to avoid confusing people with redundant controls.


### macOS
In macOS, a scroll indicator is commonly called a scroll bar.

If necessary, use small or mini scroll bars in a panel. When space is tight, you can use smaller scroll bars in panels that need to coexist with other windows. Be sure to use the same size for all controls in such a panel.


### tvOS
Views in tvOS can scroll, but they aren’t treated as distinct objects with scroll indicators. Instead, when content exceeds the size of the screen, the system automatically scrolls the interface to keep focused items visible.


### visionOS
In visionOS, the scroll indicator has a small, fixed size to help communicate that people can scroll efficiently without making large movements. To make it easy to find, the scroll indicator always appears in a predictable location with respect to the window: vertically centered at the trailing edge during vertical scrolling and horizontally centered at the window’s bottom edge during horizontal scrolling.

When people begin swiping content in the direction they want it to scroll, the scroll indicator appears at the window’s edge, visually reinforcing the effect of their gesture and providing feedback about the content’s current position and overall length. When people look at the scroll indicator and begin a drag gesture, the indicator enables a jog bar experience that lets people manipulate the scrolling speed instead of the content’s position. In this experience, the scroll indicator reveals tick marks that speed up or slow down as people make small adjustments to their gesture, providing visual feedback that helps people precisely control scrolling acceleration.

If necessary, account for the size of the scroll indicator. Although the indicator’s overall size is small, it’s a little thicker than the same component in iOS. If your content uses tight margins, consider increasing them to prevent the scroll indicator from overlapping the content.


### watchOS
Prefer vertically scrolling content. People are accustomed to using the Digital Crown to navigate to and within apps on Apple Watch. If your app contains a single list or content view, rotating the Digital Crown scrolls vertically when your app’s content is taller than the height of the display.

Use tab views to provide page-by-page scrolling. watchOS displays tab views as pages. If you place tab views in a vertical stack, people can rotate the Digital Crown to move vertically through full-screen pages of content. In this scenario, the system displays a page indicator next to the Digital Crown that shows people where they are in the content, both within the current page and within a set of pages. For guidance, see Tab views.

When displaying paged content, consider limiting the content of an individual page to a single screen height. Embracing this constraint clarifies the purpose of each page, helping you create a more glanceable design. However, if your app has long pages, people can still use the Digital Crown both to navigate between shorter pages and to scroll content in a longer page because the page indicator expands into a scroll indicator when necessary. Use variable-height pages judiciously and place them after fixed-height pages when possible.


## Resources

#### Related

#### Developer documentation

## Change log
Added guidance for scroll edge effects.

Added artwork showing the behavior of the visionOS scroll indicator.

Described the visionOS scroll indicator and added guidance for integrating it with window layout.

Updated guidance for using scroll views in watchOS.


---

### Sheets

**Source**: [https://developer.apple.com/design/human-interface-guidelines/sheets](https://developer.apple.com/design/human-interface-guidelines/sheets)


# Sheets
By default, a sheet is modal, presenting a targeted experience that prevents people from interacting with the parent view until they dismiss the sheet (for more on modal presentation, see Modality). A modal sheet is useful for requesting specific information from people or presenting a simple task that they can complete before returning to the parent view. For example, a sheet might let people supply information needed to complete an action, such as attaching a file, choosing the location for a move or save, or specifying the format for a selection.

In macOS, visionOS, and watchOS, a sheet is always modal, but in iOS and iPadOS, a sheet can also be nonmodal. When a nonmodal sheet is onscreen, people use its functionality to directly affect the current task in the parent view without dismissing the sheet. For example, Notes on iPhone and iPad uses a nonmodal sheet to help people apply different formatting to various text selections as they edit a note.

The Notes format sheet lets people apply formatting to selected text in the editing view.

Because the sheet is nonmodal, people can make additional text selections without dismissing the sheet.


## Best practices
Use a sheet to present simple content or tasks. A sheet allows some of the parent view to remain visible, helping people retain their original context as they interact with the sheet.

For complex or prolonged user flows, consider alternatives to sheets. For example, iOS and iPadOS offer a full-screen style of modal view that can work well to display content like videos, photos, or camera views or to help people perform multistep tasks like document or photo editing. (For developer guidance, see UIModalPresentationStyle.fullScreen.) In a macOS experience, you might want to open a new window or let people enter full-screen mode instead of using a sheet. For example, a self-contained task like editing a document tends to work well in a separate window, whereas going full screen can help people view media. In visionOS, you can give people a way to transition your app to a Full Space where they can dive into content or a task; for guidance, see Immersive experiences.

Display only one sheet at a time from the main interface. When people close a sheet, they expect to return to the parent view or window. If closing a sheet takes people back to another sheet, they can lose track of where they are in your app. If something people do within a sheet results in another sheet appearing, close the first sheet before displaying the new one. If necessary, you can display the first sheet again after people dismiss the second one.

Use a nonmodal view when you want to present supplementary items that affect the main task in the parent view. To give people access to information and actions they need while continuing to interact with the main window, consider using a split view in visionOS or a panel in macOS; in iOS and iPadOS, you can use a nonmodal sheet for this workflow. For guidance, see iOS, iPadOS.


## Platform considerations
No additional considerations for tvOS.


### iOS, iPadOS
A resizable sheet expands when people scroll its contents or drag the grabber, which is a small horizontal indicator that can appear at the top edge of a sheet. Sheets resize according to their detents, which are particular heights at which a sheet naturally rests. Designed for iPhone, detents specify particular heights at which a sheet naturally rests. The system defines two detents: large is the height of a fully expanded sheet and medium is about half of the fully expanded height.

Sheets automatically support the large detent. Adding the medium detent allows the sheet to rest at both heights, whereas specifying only medium prevents the sheet from expanding to full height. For developer guidance, see detents.

In an iPhone app, consider supporting the medium detent to allow progressive disclosure of the sheet’s content. For example, a share sheet displays the most relevant items within the medium detent, where they’re visible without resizing. To view more items, people can scroll or expand the sheet. In contrast, you might not want to support the medium detent if a sheet’s content is more useful when it displays at full height. For example, the compose sheets in Messages and Mail display only at full height to give people enough room to create content.

Include a grabber in a resizable sheet. A grabber shows people that they can drag the sheet to resize it; they can also tap it to cycle through the detents. In addition to providing a visual indicator of resizability, a grabber also works with VoiceOver so people can resize the sheet without seeing the screen. For developer guidance, see prefersGrabberVisible.

Support swiping to dismiss a sheet. People expect to swipe vertically to dismiss a sheet instead of tapping a dismiss button. If people have unsaved changes in the sheet when they begin swiping to dismiss it, use an action sheet to let them confirm their action.

Position Done and Cancel buttons as people expect. Typically, a Done or Dismiss button belongs in a sheet’s top-right corner in a left-to-right layout. The Cancel button belongs in a sheet’s top-left corner.

The exception to this is for sheets with additional subviews, where the Cancel button belongs in the top-right; this provides room for the Back button in the top-left on pages after the first. At the end of the navigation flow, replace the Cancel button with the Done button.

Placement of the Cancel button when it appears by itself

Placement of the Cancel button when it appears as part of a multi-step flow

Prefer using the page or form sheet presentation styles in an iPadOS app. Each style uses a default size for the sheet, centering its content on top of a dimmed background view and providing a consistent experience. For developer guidance, see UIModalPresentationStyle.


### macOS
In macOS, a sheet is a cardlike view with rounded corners that floats on top of its parent window. The parent window is dimmed while the sheet is onscreen, signaling that people can’t interact with it until they dismiss the sheet. However, people expect to interact with other app windows before dismissing a sheet.

Present a sheet in a reasonable default size. People don’t generally expect to resize sheets, so it’s important to use a size that’s appropriate for the content you display. In some cases, however, people appreciate a resizable sheet — such as when they need to expand the contents for a clearer view — so it’s a good idea to support resizing.

Let people interact with other app windows without first dismissing a sheet. When a sheet opens, you bring its parent window to the front — if the parent window is a document window, you also bring forward its modeless document-related panels. When people want to interact with other windows in your app, make sure they can bring those windows forward even if they haven’t dismissed the sheet yet.

Position a sheet’s dismiss buttons as people expect. People expect to find all buttons that dismiss a sheet — including Done, OK, and Cancel — at the bottom of the view, in the trailing corner.

Use a panel instead of a sheet if people need to repeatedly provide input and observe results. A find and replace panel, for example, might let people initiate replacements individually, so they can observe the result of each search for correctness. For guidance, see Panels.


### visionOS
While a sheet is visible in a visionOS app, it floats in front of its parent window, dimming it, and becoming the target of people’s interactions with the app.

Avoid displaying a sheet that emerges from the bottom edge of a window. To help people view the sheet, prefer centering it in their field of view.

Present a sheet in a default size that helps people retain their context. Avoid displaying a sheet that covers most or all of its window, but consider letting people resize the sheet if they want.


### watchOS
In watchOS, a sheet is a full-screen view that slides over your app’s current content. The sheet is semitransparent to help maintain the current context, but the system applies a material to the background that blurs and desaturates the covered content.

Use a sheet only when your modal task requires a custom title or custom content presentation. If you need to give people important information or present a set of choices, consider using an alert or action sheet.

Keep sheet interactions brief and occasional. Use a sheet only as a temporary interruption to the current workflow, and only to facilitate an important task. Avoid using a sheet to help people navigate your app’s content.

Change the default label of the dismiss control only if it makes sense in your app. By default, the sheet displays a round Cancel button in the upper left corner. Use this button when the sheet lets people make changes to the app’s behavior or to their data. If your sheet simply presents information without enabling a task, use the standard Done button instead. You can use a toolbar to display multiple buttons.

The standard Done button

If you change the default label, prefer using SF Symbols to represent the action. Avoid using a label that might mislead people into thinking that the sheet is part of a hierarchical navigation interface. Also, if the text in the top-leading corner looks like a page or app title, people won’t know how to dismiss the sheet. For guidance, see Standard icons.


## Resources

#### Related

#### Developer documentation
sheet(item:onDismiss:content:) — SwiftUI

UISheetPresentationController — UIKit

presentAsSheet(_:) — AppKit


## Change log
Added guidance to use form or page sheet styles in iPadOS apps.

Recommended using a split view to offer supplementary items in a visionOS app.

Updated to include guidance for visionOS.

Updated guidance for using sheets in watchOS.


---

### Windows

**Source**: [https://developer.apple.com/design/human-interface-guidelines/windows](https://developer.apple.com/design/human-interface-guidelines/windows)


# Windows
In iPadOS, macOS, and visionOS, windows help define the visual boundaries of app content and separate it from other areas of the system, and enable multitasking workflows both within and between apps. Windows include system-provided interface elements such as frames and window controls that let people open, close, resize, and relocate them.

Conceptually, apps use two types of windows to display content:

- A primary window presents the main navigation and content of an app, and actions associated with them.
- An auxiliary window presents a specific task or area in an app. Dedicated to one experience, an auxiliary window doesn’t allow navigation to other app areas, and it typically includes a button people use to close it after completing the task.
For guidance laying out content within a window on any platform, see Layout; for guidance laying out content in Apple Vision Pro space, see Spatial layout. For developer guidance, see Windows.


## Best practices
Make sure that your windows adapt fluidly to different sizes to support multitasking and multiwindow workflows. For guidance, see Layout and Multitasking.

Choose the right moment to open a new window. Opening content in a separate window is great for helping people multitask or preserve context. For example, Mail opens a new window whenever someone selects the Compose action, so both the new message and the existing email are visible at the same time. However, opening new windows excessively creates clutter and can make navigating your app more confusing. Avoid opening new windows as default behavior unless it makes sense for your app.

Consider providing the option to view content in a new window. While it’s best to avoid opening new windows as default behavior unless it benefits your user experience, it’s also great to give people the flexibility of viewing content in multiple ways. Consider letting people view content in a new window using a command in a context menu or in the File menu. For developer guidance, see OpenWindowAction.

Avoid creating custom window UI. System-provided windows look and behave in a way that people understand and recognize. Avoid making custom window frames or controls, and don’t try to replicate the system-provided appearance. Doing so without perfectly matching the system’s look and behavior can make your app feel broken.

Use the term window in user-facing content. The system refers to app windows as windows regardless of type. Using different terms — including scene, which refers to window implementation — is likely to confuse people.


## Platform considerations
Not supported in iOS, tvOS, or watchOS.


### iPadOS
Windows present in one of two ways depending on a person’s choice in Multitasking & Gestures settings.

- Full screen. App windows fill the entire screen, and people switch between them — or between multiple windows of the same app — using the app switcher.
- Windowed. People can freely resize app windows. Multiple windows can be onscreen at once, and people can reposition them and bring them to the front. The system remembers window size and placement even when an app is closed.
- Full screen
- Windowed
Make sure window controls don’t overlap toolbar items. When windowed, app windows include window controls at the leading edge of the toolbar. If your app has toolbar buttons at the leading edge, they might be hidden by window controls when they appear. To prevent this, instead of placing buttons directly on the leading edge, move them inward when the window controls appear.

Consider letting people use a gesture to open content in a new window. For example, people can use the pinch gesture to expand a Notes item into a new window. For developer guidance, see collectionView(_:sceneActivationConfigurationForItemAt:point:) (to transition from a collection view item), or UIWindowScene.ActivationInteraction (to transition from an item in any other view).

If you only need to let people view one file, you can present it without creating your own window, but you must support multiple windows in your app. For developer guidance, see QLPreviewSceneActivationConfiguration.


### macOS
In macOS, people typically run several apps at the same time, often viewing windows from multiple apps on one desktop and switching frequently between different windows — moving, resizing, minimizing, and revealing the windows to suit their work style.

To learn about setting up a window to display your game in macOS, see Managing your game window for Metal in macOS.


#### macOS window anatomy
A macOS window consists of a frame and a body area. People can move a window by dragging the frame and can often resize the window by dragging its edges.

The frame of a window appears above the body area and can include window controls and a toolbar. In rare cases, a window can also display a bottom bar, which is a part of the frame that appears below body content.


#### macOS window states
A macOS window can have one of three states:

- Main. The frontmost window that people view is an app’s main window. There can be only one main window per app.
- Inactive. A window that’s not in the foreground is an inactive window.
The system gives main, key, and inactive windows different appearances to help people visually identify them. For example, the key window uses color in the title bar options for closing, minimizing, and zooming; inactive windows and main windows that aren’t key use gray in these options. Also, inactive windows don’t use vibrancy (an effect that can pull color into a window from the content underneath it), which makes them appear subdued and seem visually farther away than the main and key windows.

Some windows — typically, panels like Colors or Fonts — become the key window only when people click the window’s title bar or a component that requires keyboard input, such as a text field.

Make sure custom windows use the system-defined appearances. People rely on the visual differences between windows to help them identify the foreground window and know which window will accept their input. When you use system-provided components, a window’s background and button appearances update automatically when the window changes state; if you use custom implementations, you need to do this work yourself.

Avoid putting critical information or actions in a bottom bar, because people often relocate a window in a way that hides its bottom edge. If you must include one, use it only to display a small amount of information directly related to a window’s contents or to a selected item within it. For example, Finder uses a bottom bar (called the status bar) to display the total number of items in a window, the number of selected items, and how much space is available on the disk. A bottom bar is small, so if you have more information to display, consider using an inspector, which typically presents information on the trailing side of a split view.


### visionOS
visionOS defines two main window styles: default and volumetric. Both a default window (called a window) and a volumetric window (called a volume) can display 2D and 3D content, and people can view multiple windows and volumes at the same time in both the Shared Space and a Full Space.

visionOS also defines the plain window style, which is similar to the default style, except that the upright plane doesn’t use the glass background. For developer guidance, see PlainWindowStyle.

The system defines the initial position of the first window or volume people open in your app or game. In both the Shared Space and a Full Space, people can move windows and volumes to new locations.


#### visionOS windows
The default window style consists of an upright plane that uses an unmodifiable background material called glass and includes a close button, window bar, and resize controls that let people close, move, and resize the window. A window can also include a Share button, tab bar, toolbar, and one or more ornaments. By default, visionOS uses dynamic scale to help a window’s size appear to remain consistent regardless of its proximity to the viewer. For developer guidance, see DefaultWindowStyle.

Prefer using a window to present a familiar interface and to support familiar tasks. Help people feel at home in your app by displaying an interface they’re already comfortable with, reserving more immersive experiences for the meaningful content and activities you offer. If you want to showcase bounded 3D content like a game board, consider using a volume.

Retain the window’s glass background. The default glass background helps your content feel like part of people’s surroundings while adapting dynamically to lighting and using specular reflections and shadows to communicate the window’s scale and position. Removing the glass material tends to cause UI elements and text to become less legible and to no longer appear related to each other; using an opaque background obscures people’s surroundings and can make a window feel constricting and heavy.

Choose an initial window size that minimizes empty areas within it. By default, a window measures 1280x720 pt. When a window first opens, the system places it about two meters in front of the wearer, giving it an apparent width of about three meters. Too much empty space inside a window can make it look unnecessarily large while also obscuring other content in people’s space.

Aim for an initial shape that suits a window’s content. For example, a default Keynote window is wide because slides are wide, whereas a default Safari window is tall because most webpages are much longer than they are wide. For games, a tower-building game is likely to open in a taller window than a driving game.

Choose a minimum and maximum size for each window to help keep your content looking great. People appreciate being able to resize windows as they customize their space, but you need to make sure your layout adjusts well across all sizes. If you don’t set a minimum and maximum size for a window, people could make it so small that UI elements overlap or so large that your app or game becomes unusable. For developer guidance, see Positioning and sizing windows.

A window containing 3D content

Minimize the depth of 3D content you display in a window. The system adds highlights and shadows to the views and controls within a window, giving them the appearance of depth and helping them feel more substantial, especially when people view the window from an angle. Although you can display 3D content in a window, the system clips it if the content extends too far from the window’s surface. To display 3D content that has greater depth, use a volume.


#### visionOS volumes
You can use a volume to display 2D or 3D content that people can view from any angle. A volume includes window-management controls just like a window, but unlike in a window, a volume’s close button and window bar shift position to face the viewer as they move around the volume. For developer guidance, see VolumetricWindowStyle.

Prefer using a volume to display rich, 3D content. In contrast, if you want to present a familiar, UI-centric interface, it generally works best to use a window.

Place 2D content so it looks good from multiple angles. Because a person’s perspective changes as they move around a volume, the location of 2D content within it might appear to change in ways that don’t make sense. To pin 2D content to specific areas of 3D content inside a volume, you can use an attachment.

In general, use dynamic scaling. Dynamic scaling helps a volume’s content remain comfortably legible and easy to interact with, even when it’s far away from the viewer. On the other hand, if you want a volume’s content to represent a real-world object, like a product in a retail app, you can use fixed scaling (this is the default).

Take advantage of the default baseplate appearance to help people discern the edges of a volume. In visionOS 2 and later, the system automatically makes a volume’s horizontal “floor,” or baseplate, visible by displaying a gentle glow around its border when people look at it. If your content doesn’t fill the volume, the system-provided glow can help people become aware of the volume’s edges, which can be particularly useful in keeping the resize control easy to find. On the other hand, if your content is full bleed or fills the volume’s bounds — or if you display a custom baseplate appearance — you may not want the default glow.

Consider offering high-value content in an ornament. In visionOS 2 and later, a volume can include an ornament in addition to a toolbar and tab bar. You can use an ornament to reduce clutter in a volume and elevate important views or controls. When you use an attachment anchor to specify the ornament’s location, such as topBack or bottomFront, the ornament remains in the same position, relative to the viewer’s perspective, as they move around the volume. Be sure to avoid placing an ornament on the same edge as a toolbar or tab bar, and prefer creating only one additional ornament to avoid overshadowing the important content in your volume. For developer guidance, see ornament(visibility:attachmentAnchor:contentAlignment:ornament:).

Choose an alignment that supports the way people interact with your volume. As people move a volume, the baseplate can remain parallel to the floor of a person’s surroundings, or it can tilt to match the angle at which a person is looking. In general, a volume that remains parallel to the floor works well for content that people don’t interact with much, whereas a volume that tilts to match where a person is looking can keep content comfortably usable, even when the viewer is reclining.


## Resources

#### Related

#### Developer documentation
WindowGroup — SwiftUI


#### Videos

## Change log
Added best practices, and updated with guidance for resizable windows in iPadOS.

Updated to include guidance for using volumes in visionOS 2 and added game-specific examples.

Updated to include guidance for visionOS.


---

## Selection and input

### Color Wells

**Source**: [https://developer.apple.com/design/human-interface-guidelines/color-wells](https://developer.apple.com/design/human-interface-guidelines/color-wells)


# Color wells
A color well displays a color picker when people tap or click it. This color picker can be the system-provided one or a custom interface that you design.


## Best practices
Consider the system-provided color picker for a familiar experience. Using the built-in color picker provides a consistent experience, in addition to letting people save a set of colors they can access from any app. The system-defined color picker can also help provide a familiar experience when developing apps across iOS, iPadOS, and macOS.


## Platform considerations
No additional considerations for iOS, iPadOS, or visionOS. Not supported in tvOS or watchOS.


### macOS
When people click a color well, it receives a highlight to provide visual confirmation that it’s active. It then opens a color picker so people can choose a color. After they make a selection, the color well updates to show the new color.

Color wells also support drag and drop, so people can drag colors from one color well to another, and from the color picker to a color well.


## Resources

#### Related

#### Developer documentation
UIColorPickerViewController — UIKit

Color Programming Topics


---

### Combo Boxes

**Source**: [https://developer.apple.com/design/human-interface-guidelines/combo-boxes](https://developer.apple.com/design/human-interface-guidelines/combo-boxes)


# Combo boxes
People can enter a custom value into the field or click the button to choose from a list of predefined values. When people enter a custom value, it’s not added to the list of choices.


## Best practices
Populate the field with a meaningful default value from the list. Although the field can be empty by default, it’s best when the default value refers to the hidden choices. The default value doesn’t have to be the first item in the list.

Use an introductory label to let people know what types of items to expect. Generally, use title-style capitalization for labels and end them with a colon. For related guidance, see Labels.

Provide relevant choices. People appreciate the ability to enter a custom value, as well as the convenience of choosing from a list of the most likely choices.

Make sure list items aren’t wider than the text field. If an item is too wide, the text field might truncate it, which is hard for people to read.

For guidance, see Text fields and Pull-down buttons.


## Platform considerations
Not supported in iOS, iPadOS, tvOS, visionOS, or watchOS.


## Resources

#### Related

#### Developer documentation

---

### Digit Entry Views

**Source**: [https://developer.apple.com/design/human-interface-guidelines/digit-entry-views](https://developer.apple.com/design/human-interface-guidelines/digit-entry-views)


# Digit entry views
You can add an optional title and prompt above the line of digits.


## Best practices
Use secure digit fields. Secure digit fields display asterisks instead of the entered digit onscreen. Always use a secure digit field when your app asks for sensitive data.

Clearly state the purpose of the digit entry view. Use a title and prompt that explains why someone needs to enter digits.


## Platform considerations
Not supported in iOS, iPadOS, macOS, visionOS, or watchOS.


## Resources

#### Related

#### Developer documentation
TVDigitEntryViewController — TVUIKit


---

### Image Wells

**Source**: [https://developer.apple.com/design/human-interface-guidelines/image-wells](https://developer.apple.com/design/human-interface-guidelines/image-wells)


# Image wells
After selecting an image well, people can copy and paste its image or delete it. People can also drag a new image into an image well without selecting it first.


## Best practices
Revert to a default image when necessary. If your image well requires an image, display the default image again if people clear the content of the image well.

If your image well supports copy and paste, make sure the standard copy and paste menu items are available. People generally expect to choose these menu items — or use the standard keyboard shortcuts — to interact with an image well. For guidance, see Edit menu.

For related guidance, see Image views.


## Platform considerations
Not supported in iOS, iPadOS, tvOS, visionOS, or watchOS.


## Resources

#### Related

#### Developer documentation

---

### Pickers

**Source**: [https://developer.apple.com/design/human-interface-guidelines/pickers](https://developer.apple.com/design/human-interface-guidelines/pickers)


# Pickers
The system provides several styles of pickers, each of which offers different types of selectable values and has a different appearance. The exact values shown in a picker, and their order, depend on the device language.

Pickers help people enter information by letting them choose single or multipart values. Date pickers specifically offer additional ways to choose values, like selecting a day in a calendar view or entering dates and times using a numeric keypad.


## Best practices
Consider using a picker to offer medium-to-long lists of items. If you need to display a fairly short list of choices, consider using a pull-down button instead of a picker. Although a picker makes it easy to scroll quickly through many items, it may add too much visual weight to a short list of items. On the other hand, if you need to present a very large set of items, consider using a list or table. Lists and tables can adjust in height, and tables can include an index, which makes it much faster to target a section of the list.

Use predictable and logically ordered values. Before people interact with a picker, many of its values can be hidden. It’s best when people can predict what the hidden values are, such as with an alphabetized list of countries, so they can move through the items quickly.

Avoid switching views to show a picker. A picker works well when displayed in context, below or in proximity to the field people are editing. A picker typically appears at the bottom of a window or in a popover.

Consider providing less granularity when specifying minutes in a date picker. By default, a minute list includes 60 values (0 to 59). You can optionally increase the minute interval as long as it divides evenly into 60. For example, you might want quarter-hour intervals (0, 15, 30, and 45).


## Platform considerations
No additional considerations for visionOS.


### iOS, iPadOS
A date picker is an efficient interface for selecting a specific date, time, or both, using touch, a keyboard, or a pointing device. You can display a date picker in one of the following styles:

- Compact — A button that displays editable date and time content in a modal view.
- Inline — For time only, a button that displays wheels of values; for dates and times, an inline calendar view.
- Wheels — A set of scrolling wheels that also supports data entry through built-in or external keyboards.
- Automatic — A system-determined style based on the current platform and date picker mode.
A date picker has four modes, each of which presents a different set of selectable values.

- Date — Displays months, days of the month, and years.
- Time — Displays hours, minutes, and (optionally) an AM/PM designation.
- Date and time — Displays dates, hours, minutes, and (optionally) an AM/PM designation.
- Countdown timer — Displays hours and minutes, up to a maximum of 23 hours and 59 minutes. This mode isn’t available in the inline or compact styles.
The exact values shown in a date picker, and their order, depend on the device location.

Here are several examples of date pickers showing different combinations of style and mode.

- Compact
- Inline
- Wheels
In a compact layout, a picker opens as a popover over your content.

In an inline layout, a picker opens inline with your content.

Another example of an inline picker uses wheels to choose values for date and time.

Use a compact date picker when space is constrained. The compact style displays a button that shows the current value in your app’s accent color. When people tap the button, the date picker opens a modal view, providing access to a familiar calendar-style editor and time picker. Within the modal view, people can make multiple edits to dates and times before tapping outside the view to confirm their choices.


### macOS
Choose a date picker style that suits your app. There are two styles of date pickers in macOS: textual and graphical. The textual style is useful when you’re working with limited space and you expect people to make specific date and time selections. The graphical style is useful when you want to give people the option of browsing through days in a calendar or selecting a range of dates, or when the look of a clock face is appropriate for your app.

For developer guidance, see NSDatePicker.


### tvOS
Pickers are available in tvOS with SwiftUI. For developer guidance, see Picker.


### watchOS
Pickers display lists of items that people navigate using the Digital Crown, which helps people manage selections in a precise and engaging way.

A picker can display a list of items using the wheels style. watchOS can also display date and time pickers using the wheels style. For developer guidance, see Picker and DatePicker.

You can configure a picker to display an outline, caption, and scrolling indicator.

For longer lists, the navigation link displays the picker as a button. When someone taps the button, the system shows the list of options. The person can also scrub through the options using the Digital Crown without tapping the button. For developer guidance, see navigationLink.


## Resources

#### Related

#### Developer documentation
NSDatePicker — AppKit


## Change log
Updated guidance for using pickers in watchOS.


---

### Segmented Controls

**Source**: [https://developer.apple.com/design/human-interface-guidelines/segmented-controls](https://developer.apple.com/design/human-interface-guidelines/segmented-controls)


# Segmented controls
Within a segmented control, all segments are usually equal in width. Like buttons, segments can contain text or images. Segments can also have text labels beneath them (or beneath the control as a whole).

A segmented control offers a single choice from among a set of options, or in macOS, either a single choice or multiple choices. For example, in macOS Keynote people can select only one segment in the alignment options control to align selected text. In contrast, people can choose multiple segments in the font attributes control to combine styles like bold, italics, and underline. The toolbar of a Keynote window also uses a segmented control to let people show and hide various editing panes within the main window area.

In addition to representing the state of a single or multiple-choice selection, a segmented control can function as a set of buttons that perform actions without showing a selection state. For example, the Reply, Reply all, and Forward buttons in macOS Mail. For developer guidance, see isMomentary and NSSegmentedControl.SwitchTracking.momentary.


## Best practices
Use a segmented control to provide closely related choices that affect an object, state, or view. For example, a segmented control in an inspector could let people choose one or more attributes to apply to a selection, or a segmented control in a toolbar could offer a set of actions to perform on the current view.

In the iOS Health app, a segmented control provides a choice of time ranges for the activity graphs to display.

Consider a segmented control when it’s important to group functions together, or to clearly show their selection state. Unlike other button styles, segmented controls preserve their grouping regardless of the view size or where they appear. This grouping can also help people understand at a glance which controls are currently selected.

Keep control types consistent within a single segmented control. Don’t assign actions to segments in a control that otherwise represents selection state, and don’t show a selection state for segments in a control that otherwise performs actions.

Limit the number of segments in a control. Too many segments can be hard to parse and time-consuming to navigate. Aim for no more than about five to seven segments in a wide interface and no more than about five segments on iPhone.

In general, keep segment size consistent. When all segments have equal width, a segmented control feels balanced. To the extent possible, it’s best to keep icon and title widths consistent too.


## Content
Prefer using either text or images — not a mix of both — in a single segmented control. Although individual segments can contain text labels or images, mixing the two in a single control can lead to a disconnected and confusing interface.

As much as possible, use content with a similar size in each segment. Because all segments typically have equal width, it doesn’t look good if content fills some segments but not others.

Use nouns or noun phrases for segment labels. Write text that describes each segment and uses title-style capitalization. A segmented control that displays text labels doesn’t need introductory text.


## Platform considerations
Not supported in watchOS.


### iOS, iPadOS
Consider a segmented control to switch between closely related subviews. A segmented control can be useful as a way to quickly switch between related subviews. For example, the segmented control in Calendar’s New Event sheet switches between the subviews for creating a new event and a new reminder. For switching between completely separate sections of an app, use a tab bar instead.


### macOS
Consider using introductory text to clarify the purpose of a segmented control. When the control uses symbols or interface icons, you could also add a label below each segment to clarify its meaning. If your app includes tooltips, provide one for each segment in a segmented control.

Use a tab view in the main window area — instead of a segmented control — for view switching. A tab view supports efficient view switching and is similar in appearance to a box combined with a segmented control. Consider using a segmented control to help people switch views in a toolbar or inspector pane.

Consider supporting spring loading. On a Mac equipped with a Magic Trackpad, spring loading lets people activate a segment by dragging selected items over it and force clicking without dropping the selected items. People can also continue dragging the items after a segment activates.


### tvOS
Consider using a split view instead of a segmented control on screens that perform content filtering. People generally find it easy to navigate back and forth between content and filtering options using a split view. Depending on its placement, a segmented control may not be as easy to access.

Avoid putting other focusable elements close to segmented controls. Segments become selected when focus moves to them, not when people click them. Carefully consider where you position a segmented control relative to other interface elements. If other focusable elements are too close, people might accidentally focus on them when attempting to switch between segments.


### visionOS
When people look at a segmented control that uses icons, the system displays a tooltip that contains the descriptive text you supply.


## Resources

#### Related

#### Developer documentation
UISegmentedControl — UIKit

NSSegmentedControl — AppKit


## Change log
Updated to include guidance for visionOS.


---

### Sliders

**Source**: [https://developer.apple.com/design/human-interface-guidelines/sliders](https://developer.apple.com/design/human-interface-guidelines/sliders)


# Sliders
As a slider’s value changes, the portion of track between the minimum value and the thumb fills with color. A slider can optionally display left and right icons that illustrate the meaning of the minimum and maximum values.


## Best practices
Customize a slider’s appearance if it adds value. You can adjust a slider’s appearance — including track color, thumb image and tint color, and left and right icons — to blend with your app’s design and communicate intent. A slider that adjusts image size, for example, could show a small image icon on the left and a large image icon on the right.

Use familiar slider directions. People expect the minimum and maximum sides of sliders to be consistent in all apps, with minimum values on the leading side and maximum values on the trailing side (for horizontal sliders) and minimum values at the bottom and maximum values at the top (for vertical sliders). For example, people expect to be able to move a horizontal slider that represents a percentage from 0 percent on the leading side to 100 percent on the trailing side.

Consider supplementing a slider with a corresponding text field and stepper. Especially when a slider represents a wide range of values, people may appreciate seeing the exact slider value and having the ability to enter a specific value in a text field. Adding a stepper provides a convenient way for people to increment in whole values. For related guidance, see Text fields and Steppers.


## Platform considerations
Not supported in tvOS.


### iOS, iPadOS
Don’t use a slider to adjust audio volume. If you need to provide volume control in your app, use a volume view, which is customizable and includes a volume-level slider and a control for changing the active audio output device. For guidance, see Playing audio.


### macOS
Sliders in macOS can also include tick marks, making it easier for people to pinpoint a specific value within the range.

In a linear slider either with or without tick marks, the thumb is a narrow lozenge shape, and the portion of track between the minimum value and the thumb is filled with color. A linear slider often includes supplementary icons that illustrate the meaning of the minimum and maximum values.

In a circular slider, the thumb appears as a small circle. Tick marks, when present, appear as evenly spaced dots around the circumference of the slider.

Linear slider without tick marks

Linear slider with tick marks

Consider giving live feedback as the value of a slider changes. Live feedback shows people results in real time. For example, your Dock icons are dynamically scaled when adjusting the Size slider in Dock settings.

Choose a slider style that matches peoples’ expectations. A horizontal slider is ideal when moving between a fixed starting and ending point. For example, a graphics app might offer a horizontal slider for setting the opacity level of an object between 0 and 100 percent. Use circular sliders when values repeat or continue indefinitely. For example, a graphics app might use a circular slider to adjust the rotation of an object between 0 and 360 degrees. An animation app might use a circular slider to adjust how many times an object spins when animated — four complete rotations equals four spins, or 1440 degrees of rotation.

Consider using a label to introduce a slider. Labels generally use sentence-style capitalization and end with a colon. For guidance, see Labels.

Use tick marks to increase clarity and accuracy. Tick marks help people understand the scale of measurements and make it easier to locate specific values.

Consider adding labels to tick marks for even greater clarity. Labels can be numbers or words, depending on the slider’s values. It’s unnecessary to label every tick mark unless doing so is needed to reduce confusion. In many cases, labeling only the minimum and maximum values is sufficient. When the values of the slider are nonlinear, like in the Energy Saver settings pane, periodic labels provide context. It’s also a good idea to provide a tooltip that displays the value of the thumb when people hold their pointer over it.


### visionOS
Prefer horizontal sliders. It’s generally easier for people to gesture from side to side than up and down.


### watchOS
A slider is a horizontal track — appearing as a set of discrete steps or as a continuous bar — that represents a finite range of values. People can tap buttons on the sides of the slider to increase or decrease its value by a predefined amount.

If necessary, create custom glyphs to communicate what the slider does. The system displays plus and minus signs by default.


## Resources

#### Related

#### Developer documentation

## Change log
Updated to include guidance for visionOS.


---

### Steppers

**Source**: [https://developer.apple.com/design/human-interface-guidelines/steppers](https://developer.apple.com/design/human-interface-guidelines/steppers)


# Steppers
A stepper sits next to a field that displays its current value, because the stepper itself doesn’t display a value.


## Best practices
Make the value that a stepper affects obvious. A stepper itself doesn’t display any values, so make sure people know which value they’re changing when they use a stepper.

Consider pairing a stepper with a text field when large value changes are likely. Steppers work well by themselves for making small changes that require a few taps or clicks. By contrast, people appreciate the option to use a field to enter specific values, especially when the values they use can vary widely. On a printing screen, for example, it can help to have both a stepper and a text field to set the number of copies.


## Platform considerations
No additional considerations for iOS, iPadOS, or visionOS. Not supported in watchOS or tvOS.


### macOS
For large value ranges, consider supporting Shift-click to change the value quickly. If your app benefits from larger changes in a stepper’s value, it can be useful to let people Shift-click the stepper to change the value by more than the default increment (by 10 times the default, for example).


## Resources

#### Related

#### Developer documentation

---

### Text Fields

**Source**: [https://developer.apple.com/design/human-interface-guidelines/text-fields](https://developer.apple.com/design/human-interface-guidelines/text-fields)


# Text fields

## Best practices
Use a text field to request a small amount of information, such as a name or an email address. To let people input larger amounts of text, use a text view instead.

Show a hint in a text field to help communicate its purpose. A text field can contain placeholder text — such as “Email” or “Password” — when there’s no other text in the field. Because placeholder text disappears when people start typing, it can also be useful to include a separate label describing the field to remind people of its purpose.

Use secure text fields to hide private data. Always use a secure text field when your app asks for sensitive data, such as a password. For developer guidance, see SecureField.

To the extent possible, match the size of a text field to the quantity of anticipated text. The size of a text field helps people visually gauge the amount of information to provide.

Evenly space multiple text fields. If your layout includes multiple text fields, leave enough space between them so people can easily see which input field belongs with each introductory label. Stack multiple text fields vertically when possible, and use consistent widths to create a more organized layout. For example, the first and last name fields on an address form might be one width, while the address and city fields might be a different width.

Ensure that tabbing between multiple fields flows as people expect. When tabbing between fields, move focus in a logical sequence. The system attempts to achieve this result automatically, so you won’t need to customize this too often.

Validate fields when it makes sense. For example, if the only legitimate value for a field is a string of digits, your app needs to alert people if they’ve entered characters other than digits. The appropriate time to check the data depends on the context: when entering an email address, it’s best to validate when people switch to another field; when creating a user name or password, validation needs to happen before people switch to another field.

Use a number formatter to help with numeric data. A number formatter automatically configures the text field to accept only numeric values. It can also display the value in a specific way, such as with a certain number of decimal places, as a percentage, or as currency. Don’t assume the actual presentation of data, however, as formatting can vary significantly based on people’s locale.

Adjust line breaks according to the needs of the field. By default, the system clips any text extending beyond the bounds of a text field. Alternatively, you can set up a text field to wrap text to a new line at the character or word level, or to truncate (indicated by an ellipsis) at the beginning, middle, or end.

Consider using an expansion tooltip to show the full version of clipped or truncated text. An expansion tooltip behaves like a regular tooltip and appears when someone places the pointer over the field.

In iOS, iPadOS, tvOS, and visionOS apps, show the appropriate keyboard type. Several different keyboard types are available, each designed to facilitate a different type of input, such as numbers or URLs. To streamline data entry, display the keyboard that’s appropriate for the type of content people are entering. For guidance, see Virtual keyboards.

Minimize text entry in your tvOS and watchOS apps. Entering long passages of text or filling out numerous text fields is time-consuming on Apple TV and Apple Watch. Minimize text input and consider gathering information more efficiently, such as with buttons.


## Platform considerations
No additional considerations for tvOS or visionOS.


### iOS, iPadOS
Display a Clear button in the trailing end of a text field to help people erase their input. When this element is present, people can tap it to clear the text field’s contents, without having to keep tapping the Delete key.

Use images and buttons to provide clarity and functionality in text fields. You can display custom images in both ends of a text field, or you can add a system-provided button, such as the Bookmarks button. In general, use the leading end of a text field to indicate a field’s purpose and the trailing end to offer additional features, such as bookmarking.


### macOS
Consider using a combo box if you need to pair text input with a list of choices. For related guidance, see Combo boxes.


### watchOS
Present a text field only when necessary. Whenever possible, prefer displaying a list of options rather than requiring text entry.


## Resources

#### Related

#### Developer documentation
SecureField — SwiftUI


## Change log
Updated guidance to reflect changes in watchOS 10.


---

### Toggles

**Source**: [https://developer.apple.com/design/human-interface-guidelines/toggles](https://developer.apple.com/design/human-interface-guidelines/toggles)


# Toggles
A toggle can have various styles, such as switch and checkbox, and different platforms can use these styles in different ways. For guidance, see Platform considerations.

In addition to toggles, all platforms also support buttons that behave like toggles by using a different appearance for each state. For developer guidance, see ToggleStyle.


## Best practices
Use a toggle to help people choose between two opposing values that affect the state of content or a view. A toggle always lets people manage the state of something, so if you need to support other types of actions — such as choosing from a list of items — use a different component, like a pop-up button.

Clearly identify the setting, view, or content the toggle affects. In general, the surrounding context provides enough information for people to understand what they’re turning on or off. In some cases, often in macOS apps, you can also supply a label to describe the state the toggle controls. If you use a button that behaves like a toggle, you generally use an interface icon that communicates its purpose, and you update its appearance — typically by changing the background — based on the current state.

Make sure the visual differences in a toggle’s state are obvious. For example, you might add or remove a color fill, show or hide the background shape, or change the inner details you display — like a checkmark or dot — to show that a toggle is on or off. Avoid relying solely on different colors to communicate state, because not everyone can perceive the differences.


## Platform considerations
No additional considerations for tvOS, visionOS, or watchOS.


### iOS, iPadOS
Use the switch toggle style only in a list row. You don’t need to supply a label in this situation because the content in the row provides the context for the state the switch controls.

Change the default color of a switch only if necessary. The default green color tends to work well in most cases, but you might want to use your app’s accent color instead. Be sure to use a color that provides enough contrast with the uncolored appearance to be perceptible.

Standard switch color

Outside of a list, use a button that behaves like a toggle, not a switch. For example, the Phone app uses a toggle on the filter button to let users filter their recent calls. The app adds a blue highlight to indicate when the toggle is active, and removes it when the toggle is inactive.

The Phone app uses a toggle to switch between all recent calls and various filter options. When someone chooses a filter, the toggle appears with a custom background drawn behind the symbol.

When someone returns to the main Recents view, the toggle appears without anything behind the symbol.

Avoid supplying a label that explains the button’s purpose. The interface icon you create — combined with the alternative background appearances you supply — help people understand what the button does. For developer guidance, see changesSelectionAsPrimaryAction.


### macOS
In addition to the switch toggle style, macOS supports the checkbox style and also defines radio buttons that can provide similar behaviors.

Use switches, checkboxes, and radio buttons in the window body, not the window frame. In particular, avoid using these components in a toolbar or status bar.


#### Switches
Prefer a switch for settings that you want to emphasize. A switch has more visual weight than a checkbox, so it looks better when it controls more functionality than a checkbox typically does. For example, you might use a switch to let people turn on or off a group of settings, instead of just one setting. For developer guidance, see switch.

Within a grouped form, consider using a mini switch to control the setting in a single row. The height of a mini switch is similar to the height of buttons and other controls, resulting in rows that have a consistent height. If you need to present a hierarchy of settings within a grouped form, you can use a regular switch for the primary setting and mini switches for the subordinate settings. For developer guidance, see GroupedFormStyle and ControlSize.

In general, don’t replace a checkbox with a switch. If you’re already using a checkbox in your interface, it’s probably best to keep using it.


#### Checkboxes
A checkbox is a small, square button that’s empty when the button is off, contains a checkmark when the button is on, and can contain a dash when the button’s state is mixed. Typically, a checkbox includes a title on its trailing side. In an editable checklist, a checkbox can appear without a title or any additional content.

Use a checkbox instead of a switch if you need to present a hierarchy of settings. The visual style of checkboxes helps them align well and communicate grouping. By using alignment — generally along the leading edge of the checkboxes — and indentation, you can show dependencies, such as when the state of a checkbox governs the state of subordinate checkboxes.

Consider using radio buttons if you need to present a set of more than two mutually exclusive options. When people need to choose from options in addition to just “on” or “off,” using multiple radio buttons can help you clarify each option with a unique label.

Consider using a label to introduce a group of checkboxes if their relationship isn’t clear. Describe the set of options, and align the label’s baseline with the first checkbox in the group.

Accurately reflect a checkbox’s state in its appearance. A checkbox’s state can be on, off, or mixed. If you use a checkbox to globally turn on and off multiple subordinate checkboxes, show a mixed state when the subordinate checkboxes have different states. For example, you might need to present a text-style setting that turns all styles on or off, but also lets people choose a subset of individual style settings like bold, italic, or underline. For developer guidance, see allowsMixedState.


#### Radio buttons
A radio button is a small, circular button followed by a label. Typically displayed in groups of two to five, radio buttons present a set of mutually exclusive choices.

A radio button’s state is either selected (a filled circle) or deselected (an empty circle). Although a radio button can also display a mixed state (indicated by a dash), this state is rarely useful because you can communicate multiple states by using additional radio buttons. If you need to show that a setting or item has a mixed state, consider using a checkbox instead.

Prefer a set of radio buttons to present mutually exclusive options. If you need to let people choose multiple options in a set, use checkboxes instead.

Avoid listing too many radio buttons in a set. A long list of radio buttons takes up a lot of space in the interface and can be overwhelming. If you need to present more than about five options, consider using a component like a pop-up button instead.

To present a single setting that can be on or off, prefer a checkbox. Although a single radio button can also turn something on or off, the presence or absence of the checkmark in a checkbox can make the current state easier to understand at a glance. In rare cases where a single checkbox doesn’t clearly communicate the opposing states, you can use a pair of radio buttons, each with a label that specifies the state it controls.

Use consistent spacing when you display radio buttons horizontally. Measure the space needed to accommodate the longest button label, and use that measurement consistently.


## Resources

#### Related

#### Developer documentation
NSButton.ButtonType.toggle — AppKit


## Change log
Enhanced guidance for using switches in macOS apps, clarified when a checkbox has a title, and added artwork for radio buttons.


---

### Virtual Keyboards

**Source**: [https://developer.apple.com/design/human-interface-guidelines/virtual-keyboards](https://developer.apple.com/design/human-interface-guidelines/virtual-keyboards)


# Virtual keyboards
A virtual keyboard can provide a specific set of keys that are optimized for the current task; for example, a keyboard that supports entering email addresses can include the “@” character and a period or even “.com”. A virtual keyboard doesn’t support keyboard shortcuts.

When it makes sense in your app, you can replace the system-provided keyboard with a custom view that supports app-specific data entry. In iOS, iPadOS, and tvOS, you can also create an app extension that offers a custom keyboard people can install and use in place of the standard keyboard.


## Best practices
Choose a keyboard that matches the type of content people are editing. For example, you can help people enter numeric data by providing the numbers and punctuation keyboard. When you specify a semantic meaning for a text input area, the system can automatically provide a keyboard that matches the type of input you expect, potentially using this information to refine the keyboard corrections it offers. For developer guidance, see keyboardType(_:) (SwiftUI), textContentType(_:)(SwiftUI), UIKeyboardType (UIKit), and UITextContentType (UIKit).

- ASCII capable
- ASCII capable number pad
- Decimal pad
- Default
- Email address
- Name phone pad
- Number pad
- Numbers and punctuation
- Phone pad
- Twitter
- URL
- Web search
Consider customizing the Return key type if it helps clarify the text-entry experience. The Return key type is based on the keyboard type you choose, but you can change this if it makes sense in your app. For example, if your app initiates a search, you can use a search Return key type rather than the standard one so the experience is consistent with other places people initiate search. For developer guidance, see submitLabel(_:) (SwiftUI) and UIReturnKeyType (UIKit).


## Custom input views
In some cases, you can create an input view if you want to provide custom functionality that enhances data-entry tasks in your app. For example, Numbers provides a custom input view for entering numeric values while editing a spreadsheet. A custom input view replaces the system-provided keyboard while people are in your app. For developer guidance, see ToolbarItemPlacement (SwiftUI) and inputViewController (UIKit).

Make sure your custom input view makes sense in the context of your app. In addition to making data entry simple and intuitive, you want people to understand the benefits of using your custom input view. Otherwise, they may wonder why they can’t regain the system keyboard while in your app.

Play the standard keyboard sound while people type. The keyboard sound provides familiar feedback when people tap a key on the system keyboard, so they’re likely to expect the same sound when they tap keys in your custom input view. People can turn keyboard sounds off for all keyboard interactions in Settings > Sounds. For developer guidance, see playInputClick() (UIKit).


## Custom keyboards
In iOS, iPadOS, and tvOS, you can provide a custom keyboard that replaces the system keyboard by creating an app extension. An app extension is code you provide that people can install and use to extend the functionality of a specific area of the system; to learn more, see App extensions.

After people choose your custom keyboard in Settings, they can use it for text entry within any app, except when editing secure text fields and phone number fields. People can choose multiple custom keyboards and switch between them at any time. For developer guidance, see Creating a custom keyboard.

Custom keyboards make sense when you want to expose unique keyboard functionality systemwide, such as a novel way of inputting text or the ability to type in a language the system doesn’t support. If you want to provide a custom keyboard for people to use only while they’re in your app, consider creating a custom input view instead.

Provide an obvious and easy way to switch between keyboards. People know that the Globe key on the standard keyboard — which replaces the dedicated Emoji key when multiple keyboards are available — quickly switches to other keyboards, and they expect a similarly intuitive experience in your keyboard.

Avoid duplicating system-provided keyboard features. On some devices, the Emoji/Globe key and Dictation key automatically appear beneath the keyboard, even when people are using custom keyboards. Your app can’t affect these keys, and it’s likely to be confusing if you repeat them in your keyboard.

Consider providing a keyboard tutorial in your app. People are used to the standard keyboard, and learning how to use a new keyboard can take time. You can help make the process easier by providing usage instructions in your app — for example, you might tell people how to choose your keyboard, activate it during text entry, use it, and switch back to the standard keyboard. Avoid displaying help content within the keyboard itself.


## Platform considerations
Not supported in macOS.


### iOS, iPadOS
Use the keyboard layout guide to make the keyboard feel like an integrated part of your interface. Using the layout guide also helps you keep important parts of your interface visible while the virtual keyboard is onscreen. For developer guidance, see Adjusting your layout with keyboard layout guide.

The keyboard layout guide helps ensure that app UI and the keyboard work well together.

Without the layout guide, the keyboard could make entering text more difficult.

Without the layout guide, the keyboard could make tapping a button more difficult.

Place custom controls above the keyboard thoughtfully. Some apps position an input accessory view containing custom controls above the keyboard to offer app-specific functionality related to the data people are working with. For example, Numbers displays controls that help people apply standard or custom calculations to spreadsheet data. If your app offers custom controls that augment the keyboard, make sure they’re relevant to the current task. If other views in your app use Liquid Glass, or if your view looks out of place above the keyboard, apply Liquid Glass to the view that contains your controls to maintain consistency. If you use a standard toolbar to contain your controls, it automatically adopts Liquid Glass. Use the keyboard layout guide and standard padding to ensure the system positions your controls as expected within the view. For developer guidance, see ToolbarItemPlacement (SwiftUI), inputAccessoryView (UIKit), and UIKeyboardLayoutGuide (UIKit).


### tvOS
tvOS displays a linear virtual keyboard when people select a text field using the Siri Remote.

A grid keyboard screen appears when people use devices other than the Siri Remote, and the layout of content automatically adapts to the keyboard.

When people activate a digit entry view, tvOS displays a digit-specific keyboard. For guidance, see Digit entry views.


### visionOS
In visionOS, the system-provided virtual keyboard supports both direct and indirect gestures and appears in a separate window that people can move where they want. You don’t need to account for the location of the keyboard in your layouts.


### watchOS
On Apple Watch, a text field can show a keyboard if the device screen is large enough. Otherwise, the system lets people use dictation or Scribble to enter information. You can’t change the keyboard type in watchOS, but you can set the content type of the text field. The system uses this information to make text entry easier, such as by offering suggestions. For developer guidance, see textContentType(_:) (SwiftUI).

People can also use a nearby paired iPhone to enter text on Apple Watch.


## Resources

#### Related

#### Developer documentation
keyboardType(_:) — SwiftUI

textContentType(_:) — SwiftUI

UIKeyboardType — UIKit


## Change log
Added guidance for displaying custom controls above the keyboard, and updated to reflect virtual keyboard availability in watchOS.

Clarified the virtual keyboard’s support for direct and indirect gestures in visionOS.

Added artwork for visionOS.

Changed page title from Onscreen keyboards and updated to include guidance for visionOS.


---

## Status

### Activity Rings

**Source**: [https://developer.apple.com/design/human-interface-guidelines/activity-rings](https://developer.apple.com/design/human-interface-guidelines/activity-rings)


# Activity rings
In watchOS, the Activity ring element always contains three rings, whose colors and meanings match those the Activity app provides. In iOS, the Activity ring element contains either a single Move ring representing an approximation of activity, or all three rings if an Apple Watch is paired.


## Best practices
Display Activity rings when they’re relevant to the purpose of your app. If your app is related to health or fitness, and especially if it contributes information to HealthKit, people generally expect to find Activity rings in your interface. For example, if you structure a workout or health session around the completion of Activity rings, consider displaying the element on a workout metrics screen so that people can track their progress during their session. Similarly, if you provide a summary screen that appears at the conclusion of a workout, you could display Activity rings to help people check on their progress toward their daily goals.

Use Activity rings only to show Move, Exercise, and Stand information. Activity rings are designed to consistently represent progress in these specific areas. Don’t replicate or modify Activity rings for other purposes. Never use Activity rings to display other types of data. Never show Move, Exercise, and Stand progress in another ring-like element.

Use Activity rings to show progress for a single person. Never use Activity rings to represent data for more than one person, and make sure it’s obvious whose progress you’re showing by using a label, a photo, or an avatar.

Always keep the visual appearance of Activity rings the same, regardless of where you display them. Follow these guidelines to provide a consistent experience:

- Never change the colors of the rings; for example, don’t use filters or modify opacity.
- Always display Activity rings on a black background.
- Prefer enclosing the rings and background within a circle. To do this, adjust the corner radius of the enclosing view rather than applying a circular mask.
- Ensure that the black background remains visible around the outermost ring. If necessary, add a thin, black stroke around the outer edge of the ring, and avoid including a gradient, shadow, or any other visual effect.
- Always scale the rings appropriately so they don’t seem disconnected or out of place.
- When necessary, design the surrounding interface to blend with the rings; never change the rings to blend with the surrounding interface.
To display a label or value that’s directly associated with an Activity ring, use the colors that match it. To display the ring-specific labels Move, Exercise, and Stand, or to display a person’s current and goal values for each ring, use the following colors, specified as RGB values.

Maintain Activity ring margins. An Activity ring element must include a minimum outer margin of no less than the distance between rings. Never allow other elements to crop, obstruct, or encroach upon this margin or the rings themselves.

Differentiate other ring-like elements from Activity rings. Mixing different ring styles can lead to a visually confusing interface. If you must include other rings, use padding, lines, or labels to separate them from Activity rings. Color and scale can also help provide visual separation.

Don’t send notifications that repeat the same information the Activity app sends. The system already delivers Move, Exercise, and Stand progress updates, so it’s confusing for people to receive redundant information from your app. Also, don’t show an Activity ring element in your app’s notifications. It’s fine to reference Activity progress in a notification, but do so in a way that’s unique to your app and doesn’t replicate the same information the system provides.

Don’t use Activity rings for decoration. Activity rings provide information to people; they don’t just embellish your app’s design. Never display Activity rings in labels or background graphics.

Don’t use Activity rings for branding. Use Activity rings strictly to display Activity progress in your app. Never use Activity rings in your app’s icon or marketing materials.


## Platform considerations
No additional considerations for iPadOS or watchOS. Not supported in macOS, tvOS, or visionOS.


### iOS
Activity rings are available in iOS with HKActivityRingView. The appearance of the Activity ring element changes automatically depending on whether an Apple Watch is paired:

- With an Apple Watch paired, iOS shows all three Activity rings.
- Without an Apple Watch paired, iOS shows the Move ring only, which represents an approximation of a person’s activity based on their steps and workout information from other apps.
No Apple Watch paired

Because iOS shows Activity rings whether or not an Apple Watch is paired, activity history can include a combination of both styles. For example, Activity rings in Fitness have three rings when a person exercises with their Apple Watch paired, and only the Move ring when they exercise without their Apple Watch.


## Resources

#### Related

#### Developer documentation
HKActivityRingView — HealthKit


#### Videos

## Change log
Enhanced guidance for displaying Activity rings and listed specific colors for displaying related content.

Added artwork representing Activity rings in iOS.


---

### Gauges

**Source**: [https://developer.apple.com/design/human-interface-guidelines/gauges](https://developer.apple.com/design/human-interface-guidelines/gauges)


# Gauges
In addition to indicating the current value in a range, a gauge can provide more context about the range itself. For example, a temperature gauge can use text to identify the highest and lowest temperatures in the range and display a spectrum of colors that visually reinforce the changing values.


## Anatomy
A gauge uses a circular or linear path to represent a range of values, mapping the current value to a specific point on the path. A standard gauge displays an indicator that shows the current value’s location; a gauge that uses the capacity style displays a fill that stops at the value’s location on the path.

Circular and linear gauges in both standard and capacity styles are also available in a variant that’s visually similar to watchOS complications. This variant — called accessory — works well in iOS Lock Screen widgets and anywhere you want to echo the appearance of complications.

In addition to gauges, macOS also supports level indicators, some of which have visual styles that are similar to gauges. For guidance, see macOS.


## Best practices
Write succinct labels that describe the current value and both endpoints of the range. Although not every gauge style displays all labels, VoiceOver reads the visible labels to help people understand the gauge without seeing the screen.

Consider filling the path with a gradient to help communicate the purpose of the gauge. For example, a temperature gauge might use colors that range from red to blue to represent temperatures that range from hot to cold.


## Platform considerations
No additional considerations for iOS, iPadOS, visionOS, or watchOS. Not supported in tvOS.


### macOS
In addition to supporting gauges, macOS also defines a level indicator that displays a specific numerical value within a range. You can configure a level indicator to convey capacity, rating, or — rarely — relevance.

The capacity style can depict discrete or continuous values.

Continuous. A horizontal translucent track that fills with a solid bar to indicate the current value.

Discrete. A horizontal row of separate, equally sized, rectangular segments. The number of segments matches the total capacity, and the segments fill completely — never partially — with color to indicate the current value.

Consider using the continuous style for large ranges. A large value range can make the segments of a discrete capacity indicator too small to be useful.

Consider changing the fill color to inform people about significant parts of the range. By default, the fill color for both capacity indicator styles is green. If it makes sense in your app, you can change the fill color when the current value reaches certain levels, such as very low, very high, or just past the middle. You can change the fill color of the entire indicator or you can use the tiered state to show a sequence of several colors in one indicator, as shown below.

Tiered level appearance

For guidance using the rating style to help people rank something, see Rating indicators.

Although rarely used, the relevance style can communicate relevancy using a shaded horizontal bar. For example, a relevance indicator might appear in a list of search results, helping people visualize the relevancy of the results when sorting or comparing multiple items.


## Resources

#### Related

#### Developer documentation
NSLevelIndicator — AppKit


## Change log

---

### Progress Indicators

**Source**: [https://developer.apple.com/design/human-interface-guidelines/progress-indicators](https://developer.apple.com/design/human-interface-guidelines/progress-indicators)


# Progress indicators
Some progress indicators also give people a way to estimate how long they have to wait for something to complete. All progress indicators are transient, appearing only while an operation is ongoing and disappearing after it completes.

Because the duration of an operation is either known or unknown, there are two types of progress indicators:

- Determinate, for a task with a well-defined duration, such as a file conversion
- Indeterminate, for unquantifiable tasks, such as loading or synchronizing complex data
Both determinate and indeterminate progress indicators can have different appearances depending on the platform. A determinate progress indicator shows the progress of a task by filling a linear or circular track as the task completes. Progress bars include a track that fills from the leading side to the trailing side. Circular progress indicators have a track that fills in a clockwise direction.

Circular progress indicator

An indeterminate progress indicator — also called an activity indicator — uses an animated image to indicate progress. All platforms support a circular image that appears to spin; however, macOS also supports an indeterminate progress bar.

For developer guidance, see ProgressView.


## Best practices
When possible, use a determinate progress indicator. An indeterminate progress indicator shows that a process is occurring, but it doesn’t help people estimate how long a task will take. A determinate progress indicator can help people decide whether to do something else while waiting for the task to complete, restart the task at a different time, or abandon the task.

Be as accurate as possible when reporting advancement in a determinate progress indicator. Consider evening out the pace of advancement to help people feel confident about the time needed for the task to complete. Showing 90 percent completion in five seconds and the last 10 percent in 5 minutes can make people wonder if your app is still working and can even feel deceptive.

Keep progress indicators moving so people know something is continuing to happen. People tend to associate a stationary indicator with a stalled process or a frozen app. If a process stalls for some reason, provide feedback that helps people understand the problem and what they can do about it.

When possible, switch a progress bar from indeterminate to determinate. If an indeterminate process reaches a point where you can determine its duration, switch to a determinate progress bar. People generally prefer a determinate progress indicator, because it helps them gauge what’s happening and how long it will take.

Don’t switch from the circular style to the bar style. Activity indicators (also called spinners) and progress bars are different shapes and sizes, so transitioning between them can disrupt your interface and confuse people.

If it’s helpful, display a description that provides additional context for the task. Be accurate and succinct. Avoid vague terms like loading or authenticating because they seldom add value.

Display a progress indicator in a consistent location. Choosing a consistent location for a progress indicator helps people reliably find the status of an operation across platforms or within or between apps.

When it’s feasible, let people halt processing. If people can interrupt a process without causing negative side effects, include a Cancel button. If interrupting the process might cause negative side effects — such as losing the downloaded portion of a file — it can be useful to provide a Pause button in addition to a Cancel button.

Let people know when halting a process has a negative consequence. When canceling a process results in lost progress, it’s helpful to provide an alert that includes an option to confirm the cancellation or resume the process.


## Platform considerations
No additional considerations for tvOS or visionOS.


### iOS, iPadOS

#### Refresh content controls
A refresh control lets people immediately reload content, typically in a table view, without waiting for the next automatic content update to occur. A refresh control is a specialized type of activity indicator that’s hidden by default, becoming visible when people drag down the view they want to reload. In Mail, for example, people can drag down the list of Inbox messages to check for new messages.

Perform automatic content updates. Although people appreciate being able to do an immediate content refresh, they also expect automatic refreshes to occur periodically. Don’t make people responsible for initiating every update. Keep data fresh by updating it regularly.

Supply a short title only if it adds value. Optionally, a refresh control can include a title. In most cases, this is unnecessary, as the animation of the control indicates that content is loading. If you do include a title, don’t use it to explain how to perform a refresh. Instead, provide information of value about the content being refreshed. A refresh control in Podcasts, for example, uses a title to tell people when the last podcast update occurred.

For developer guidance, see UIRefreshControl.


### macOS
In macOS, an indeterminate progress indicator can have a bar or circular appearance. Both versions use an animated image to indicate that the app is performing a task.

Indeterminate progress bar

Indeterminate circular progress indicator

Prefer an activity indicator (spinner) to communicate the status of a background operation or when space is constrained. Spinners are small and unobtrusive, so they’re useful for asynchronous background tasks, like retrieving messages from a server. Spinners are also good for communicating progress within a small area, such as within a text field or next to a specific control, such as a button.

Avoid labeling a spinning progress indicator. Because a spinner typically appears when people initiate a process, a label is usually unnecessary.


### watchOS
By default the system displays the progress indicators in white over the scene’s background color. You can change the color of the progress indicator by setting its tint color.


## Resources

#### Developer documentation
ProgressView — SwiftUI

UIProgressView — UIKit

UIActivityIndicatorView — UIKit

UIRefreshControl — UIKit

NSProgressIndicator — AppKit


## Change log
Combined guidance common to all platforms.

Updated guidance to reflect changes in watchOS 10.


---

### Rating Indicators

**Source**: [https://developer.apple.com/design/human-interface-guidelines/rating-indicators](https://developer.apple.com/design/human-interface-guidelines/rating-indicators)


# Rating indicators
A rating indicator doesn’t display partial symbols; it rounds the value to display complete symbols only. Within a rating indicator, symbols are always the same distance apart and don’t expand or shrink to fit the component’s width.


## Best practices
Make it easy to change rankings. When presenting a list of ranked items, let people adjust the rank of individual items inline without navigating to a separate editing screen.

If you replace the star with a custom symbol, make sure that its purpose is clear. The star is a very recognizable ranking symbol, and people may not associate other symbols with a rating scale.


## Platform considerations
No additional considerations for macOS. Not supported in iOS, iPadOS, tvOS, visionOS, or watchOS.


## Resources

#### Related

#### Developer documentation
NSLevelIndicator.Style.rating — AppKit


## Change log

---

## System experiences

### App Shortcuts

**Source**: [https://developer.apple.com/design/human-interface-guidelines/app-shortcuts](https://developer.apple.com/design/human-interface-guidelines/app-shortcuts)


# App Shortcuts
People can initiate App Shortcuts using features like Siri, Spotlight, and the Shortcuts app; using hardware features like the Action button on iPhone or Apple Watch; or by squeezing Apple Pencil.

Because App Shortcuts are part of your app, they are available immediately when installation finishes. For example, a journaling app could offer an App Shortcut for making a new journal entry that’s available before a person opens the app for the first time. Once someone starts using your app, its App Shortcuts can reflect their choices, like those from FaceTime for calling recent contacts.

App Shortcuts use App Intents to define actions within your app to make available to the system. Each App Shortcut includes one or more actions that represent a set of steps people might want to perform to accomplish a task. For example, a home security app might combine the two common actions of turning off the lights and locking exterior doors when a person goes to sleep at night into a single App Shortcut. Each app can include up to 10 App Shortcuts.

When you use App Intents to make your app’s actions available to the system, in addition to the App Shortcuts that your app provides, people can also make their own custom shortcuts by combining actions in the Shortcuts app. Custom shortcuts give people flexibility to configure the behavior of actions, and enable workflows that perform tasks across multiple apps. For additional guidance, see the Shortcuts User Guide.


## Best practices
Offer App Shortcuts for your app’s most common and important tasks. Straightforward tasks that people can complete without leaving their current context work best, but you can also open your app if it helps people complete multistep tasks more easily.

Add flexibility by letting people choose from a set of options. An App Shortcut can include a single optional value, or parameter, if it makes sense. For example, a meditation app could offer an App Shortcut that lets someone begin a specific type of meditation: “Start [morning, daily, sleep] meditation.” Include predictable and familiar values as options, because people won’t have the list in front of them for reference. For developer guidance, see Adding parameters to an app intent.

Ask for clarification in response to a request that’s missing optional information. For example, someone might say “Start meditation” without specifying the type (morning, daily, or sleep); you could follow up by suggesting the one they used most recently, or one based on the current time of day. If one option is most likely, consider presenting it as the default, and provide a short list of alternatives to choose from if a person doesn’t want the default choice.

Keep voice interactions simple. If your phrase feels too complicated when you say it aloud, it’s probably too difficult to remember or say correctly. For example, “Start [sleep] meditation with nature sounds” appears to have two possible parameters: the meditation type, and the accompanying sound. If additional information is absolutely required, ask for it in a subsequent step. For additional guidance on writing dialogue text for voice interactions, see Siri.

Make App Shortcuts discoverable in your app. People are most likely to remember and use App Shortcuts for tasks they do often, once they know the shortcut is available. Consider showing occasional tips in your app when people perform common actions to let them know an App Shortcut exists. For developer guidance, see SiriTipUIView.


### Responding to App Shortcuts
As a person engages with an App Shortcut, your app can respond in a variety of ways, including with dialogue that Siri speaks aloud and custom visuals like snippets and Live Activities.

- Snippets are great for custom views that display static information or dialog options, like showing the weather at a person’s location or confirming an order. For developer guidance, see ShowsSnippetView.
- Live Activities offer continuous access to information that’s likely to remain relevant and change over a period of time, and are great for timers and countdowns that appear until an event is complete. For developer guidance, see LiveActivityIntent.
Provide enough detail for interaction on audio-only devices. People can receive responses on audio-only devices such as AirPods and HomePod too, and may not always be able to see content onscreen. Include all critical information in the full dialogue text of your App Shortcuts. For developer guidance, see init(full:supporting:systemImageName:).


## Editorial guidelines
Provide brief, memorable activation phrases and natural variants. Because an App Shortcut phrase (or a variant you define) is what people say to run an App Shortcut with Siri, it’s important to keep it brief to make it easier to remember. You have to include your app name, but you can be creative with it. For example, Keynote accepts both “Create a Keynote” and “Add a new presentation in Keynote” as App Shortcut phrases for creating a new document. For developer guidance, see AppShortcutPhrase.

When referring to App Shortcuts or the Shortcuts app, always use title case and make sure that Shortcuts is plural. For example, MyApp integrates with Shortcuts to provide a quick way to get things done with just a tap or by asking Siri, and offers App Shortcuts you can place on the Action button.

When referring to individual shortcuts (not App Shortcuts or the Shortcuts app), use lowercase. For example, Run a shortcut by asking Siri or tapping a suggestion on the Lock Screen.


## Platform considerations
No additional considerations for visionOS or watchOS. Not supported in tvOS.


### iOS, iPadOS
App Shortcuts can appear in the Top Hit area of Spotlight when people search for your app, or in the Shortcuts area below. Each App Shortcut includes a symbol from SF Symbols that you choose to represent its functionality, or a preview image of an item that the shortcut links to directly.

Order shortcuts based on importance. The order you choose determines how App Shortcuts initially appear in both Spotlight and the Shortcuts app, so it’s helpful to include the most generally useful ones first. Once people start using your App Shortcuts, the system updates to prioritize the ones they use most frequently.

Offer an App Shortcut that starts a Live Activity. Live Activities allow people to track an event or the progress of a task in glanceable locations across their devices. For example, a cooking app could offer a Live Activity to show the time left until a dish is ready to take out of the oven. To make it easy for people to start a cooking timer, the app offers an App Shortcut that people can place on the Action button. For more information about Live Activities, see Live Activities.


### macOS
App Shortcuts aren’t supported in macOS. However, actions you create for your app using App Intents are supported, and people can build custom shortcuts using them with the Shortcuts app on Mac.


## Resources

#### Related

#### Developer documentation
Making actions and content discoverable and widely available — App Intents

Integrating custom data types into your intents — App Intents


#### Videos

## Change log
Updated and streamlined guidance.


---

### Complications

**Source**: [https://developer.apple.com/design/human-interface-guidelines/complications](https://developer.apple.com/design/human-interface-guidelines/complications)


# Complications
People often prefer apps that provide multiple, powerful complications, because it gives them quick ways to view the data they care about, even when they don’t open the app.

Most watch faces can display at least one complication; some can display four or more.

Starting in watchOS 9, the system organizes complications (also known as accessories) into several families — like circular and inline — and defines some recommended layouts you can use to display your complication data. A watch face can specify the family it supports in each complication slot. Complications that work in earlier versions of watchOS can use the legacy templates, which define nongraphic complication styles that don’t take on a wearer’s selected color.

Prefer using WidgetKit to develop complications for watchOS 9 and later. For guidance, see Migrating ClockKit complications to WidgetKit. To support earlier versions of watchOS, continue to implement the ClockKit complication data source protocol (see CLKComplicationDataSource).


## Best practices
Identify essential, dynamic content that people want to view at a glance. Although people can use a complication to quickly launch an app, the complication behavior they appreciate more is the display of relevant information that always feels up to date. A static complication that doesn’t display meaningful data may be less likely to remain in a prominent position on the watch face.

Support all complication families when possible. Supporting more families means that your complications are available on more watch faces. If you can’t display useful information for a particular complication family, provide an image that represents your app — like your app icon — that still lets people launch your app from the watch face.

Consider creating multiple complications for each family. Supporting multiple complications helps you take advantage of shareable watch faces and lets people configure a watch face that’s centered on an app they love. For example, an app that helps people train for triathlons could offer three circular complications — one for each segment of the race — each of which deep-links to the segment-specific area in the app. This app could also offer a shareable watch face that’s preconfigured to include its swimming, biking, and running complications and to use its custom images and colors. When people choose this watch face, they don’t have to do any configuration before they can start using it. For guidance, see Watch faces.

Define a different deep link for each complication you support. It works well when each complication opens your app to the most relevant area. If all the complications you support open the same area in your app, they can seem less useful.

Keep privacy in mind. With the Always-On Retina display, information on the watch face might be visible to people other than the wearer. Make sure you help people prevent potentially sensitive information from being visible to others. For guidance, see Always On.

Carefully consider when to update data. You provide a complication’s data in the form of a timeline where each entry has a value that specifies the time at which to display your data on the watch face. Different data sets might require different time values. For example, a meeting app might display information about an upcoming meeting an hour before the meeting starts, but a weather app might display forecast information at the time those conditions are expected to occur. You can update the timeline a limited number of times each day, and the system stores a limited number of timeline entries for each app, so you need to choose times that enhance the usefulness of your data. For developer guidance, see Migrating ClockKit complications to WidgetKit.


## Visual design
Choose a ring or gauge style based on the data you need to display. Many families support a ring or gauge layout that provides consistent ways to represent numerical values that can change over time. For example:

- The closed style can convey a value that’s a percentage of a whole, such as for a battery gauge.
- The open style works well when the minimum and maximum values are arbitrary — or don’t represent a percentage of the whole — like for a speed indicator.
- Similar to the open style, the segmented style also displays values within an app-defined range, and can convey rapid value changes, such as in the Noise complication.
Make sure images look good in tinted mode. In tinted mode, the system applies a solid color to a complication’s text, gauges, and images, and desaturates full-color images unless you provide tinted versions of them. For developer guidance, see WidgetRenderingMode. (If you’re using legacy templates, tinted mode applies only to graphic complications.) To help your complications perform well in tinted mode:

- Avoid using color as the only way to communicate important information. You want people to get the same information in tinted mode as they do in nontinted mode.
- When necessary, provide an alternative tinted-mode version of a full-color image. If your full-color image doesn’t look good when it’s desaturated, you can supply a different version of the image for the system to use in tinted mode.
Recognize that people might prefer to use tinted mode for complications, instead of viewing them in full color. When people choose tinted mode, the system automatically desaturates your complication, converting it to grayscale and tinting its images, gauges, and text using a single color that’s based on the wearer’s selected color.

When creating complication content, generally use line widths of two points or greater. Thinner lines can be difficult to see at a glance, especially when the wearer is in motion. Use line weights that suit the size and complexity of the image.

Provide a set of static placeholder images for each complication you support. The system uses placeholder images when there’s no other content to display for your complication’s data. For example, when people first install your app, the system can display a static placeholder while it checks to see if your app can generate a localized placeholder to use instead. Placeholder images can also appear in the carousel from which people select complications. Note that complication image sizes vary per layout (and per legacy template) and the size of a placeholder image may not match the size of the actual image you supply for that complication. For developer guidance, see placeholder(in:).


## Circular
Circular layouts can include text, gauges, and full-color images in circular areas on the Infograph and Infograph Modular watch faces. The circular family also defines extra-large layouts for displaying content on the X-Large watch face.

You can also add text to accompany a regular-size circular image, using a design that curves the text along the bezel of some watch faces, like Infograph. The text can fill nearly 180 degrees of the bezel before truncating.

As you design images for a regular-size circular complication, use the following values for guidance.

42x42 pt (84x84 px @2x)

44.5x44.5 pt (89x89 px @2x)

47x47 pt (94x94 px @2x)

50x50 pt (100x100 px @2x)

27x27 pt (54x54 px @2x)

28.5x28.5 pt (57x57 px @2x)

31x31 pt (62x62 px @2x)

32x32 pt (64x64 px @2x)

11x11 pt (22x22 px @2x)

11.5x11.5 pt (23x23 px @2x)

12x12 pt (24x24 px @2x)

13x13 pt (26x26 px @2x)

28x14 pt (56x28 px @2x)

29.5x15 pt (59X30 px @2x)

31x16 pt (62x32px @ 2x)

33.5x16.5 pt (67x33 px @2x)

The system applies a circular mask to each image.

A SwiftUI view that implements a regular-size circular complication uses the following default text values:

- Style: Rounded
- Weight: Medium
- Text size: 12 pt (40mm), 12.5 pt (41mm), 13 pt (44mm), 14.5 pt (45mm/49mm)
If you want to design an oversized treatment of important information that can appear on the X-Large watch face — for example, the Contacts complication, which features a contact photo — use the extra-large versions of the circular family’s layouts. The following layouts let you display full-color images, text, and gauges in a large circular region that fills most of the X-Large watch face. Some of the text fields can support multicolor text.

Use the following values for guidance as you create images for an extra-large circular complication.

120x120 pt (240x240 px @2x)

127x127 pt (254x254 px @2x)

132x132 pt (264x264 px @2x)

143x143 pt (286x286 px @2x)

33x33 pt (66x66 px @2x)

37x37 pt (74x74 px @2x)

77x77 pt (154x154 px @2x)

81.5x81.5 (163x163 px @2x)

87x87 pt (174x174 px @2x)

91.5x91.5 (183x183 px @2x)

80x40 pt (160x80 px @2x)

85x42 (170x84 px @2x)

87x44 pt (174x88 px @2x)

95x48 pt (190x96 px @2x )

The system applies a circular mask to the circular, open-gauge, and closed-gauge images.

Use the following values to create no-content placeholder images for your circular-family complications.

A SwiftUI view that implements an extra-large circular layout uses the following default text values:

- Text size: 34.5 pt (40mm), 36.5 pt (41mm), 36.5 pt (44mm), 41 pt (45mm/49mm)

## Corner
Corner layouts let you display full-color images, text, and gauges in the corners of the watch face, like Infograph. Some of the templates also support multicolor text.

As you design images for a corner complication, use the following values for guidance.

34x34 pt (68x68 px @2x)

36x36 pt (72x72 px @2x)

38x38 pt (76x76 px @2x )

20x20 pt (40x40 px @2x)

21x21 pt (42x42 px @2x)

22x22 pt (44x44 px @2x)

24x24 pt (48x48 px @2x)

Use the following values to create no-content placeholder images for your corner-family complications.

A SwiftUI view that implements a corner layout uses the following default text values:

- Weight: Semibold
- Text size: 10 pt (40mm), 10.5 pt (41mm), 11 pt (44mm), 12 pt (45mm/49mm)

## Inline
Inline layouts include utilitarian small and large layouts.

Utilitarian small layouts are intended to occupy a rectangular area in the corner of a watch face, such as the Chronograph and Simple watch faces. The content can include an image, interface icon, or a circular graph.

As you design images for a utilitarian small layout, use the following values for guidance.

9-21x9 pt (18-42x18 px @2x)

10-22x10 pt (20-44x20 px @2x)

10.5-23.5x21 pt (21-47x21 @2x)

12-26x12 pt (24-52x24 px @2x)

14x14 pt (28x28 px @2x)

15x15 pt (30x30 px @2x)

16x16 pt (32x32 px @2x)

16.5x16.5 pt (33x33 px @2x)

23.5x23.5 pt (47x47 px @2x)

25x25 pt (50x50 px @2x)

26x26 pt (52x52 px @2x)

The utilitarian large layout is primarily text-based, but also supports an interface icon placed on the leading side of the text. This layout spans the bottom of a watch face, like the Utility or Motion watch faces.

As you design images for a utilitarian large layout, use the following values for guidance.

10.5-23.5x10.5 pt (21-47x21 px @2x)


## Rectangular
Rectangular layouts can display full-color images, text, a gauge, and an optional title in a large rectangular region. Some of the text fields can support multicolor text.

The large rectangular region works well for showing details about a value or process that changes over time, because it provides room for information-rich charts, graphs, and diagrams. For example, the Heart Rate complication displays a graph of heart-rate values within a 24-hour period. The graph uses high-contrast white and red for the primary content and a lower-contrast gray for the graph lines and labels, making the data easy to understand at a glance.

Starting with watchOS 10, if you have created a rectangular layout for your watchOS app, the system may display it in the Smart Stack. You can optimize this presentation in a few ways:

- By supplying background color or content that communicates information or aids in recognition
- By using intents to specify relevancy, and help ensure that your widget is displayed in the Smart Stack at times that are most appropriate and useful to people
- By creating a custom layout of your information that is optimized for the Smart Stack
For developer guidance, see WidgetFamily.accessoryRectangular. See Widgets for additional guidance on designing widgets for the Smart Stack.

Use the following values for guidance as you create images for a rectangular layout.

Large image with title *

150x47 pt (300x94 px @2x)

159x50 pt (318x100 px @2x)

171x54 pt (342x108 px @2x)

178.5x56 pt (357x112 px @2x)

Large image without title *

162x69 pt (324x138 px @2x)

171.5x73 pt (343x146 px @2x)

184x78 pt (368x156 px @2x)

193x82 pt (386x164 px @2x)

12.5x12.5 pt (25x25 px @2x)

13.5x13.5 pt (27x27 px @2x)

14.5x14.5 pt (29x29 px @2x)

Both large-image layouts automatically include a four-point corner radius.

A SwiftUI view that implements a rectangular layout uses the following default text values:

- Text size: 16.5 pt (40mm), 17.5 pt (41mm), 18 pt (44mm), 19.5 pt (45mm/49mm)

## Legacy templates

### Circular small
Circular small templates display a small image or a few characters of text. They appear in the corner of the watch face (for example, in the Color watch face).

As you design images for a circular small complication, use the following values for guidance.

18x18 pt (36x36 px @2x)

19x19 pt (38x38 px @2x)

21.5x21.5 pt (43x43 px @2x)

16x7 pt (32x14 px @2x)

17x8 pt (34x16 px @2x)

18x8.5 pt (36x17 px @2x)

19x9 pt (38x18 px @2x)

19x9.5 pt (38x19 px @2x)

18x18x pt (36x36 px @2x)

In each stack measurement, the width value represents the maximum size.


### Modular small
Modular small templates display two stacked rows consisting of an icon and content, a circular graph, or a single larger item (for example, the bottom row of complications on the Modular watch face).

As you design icons and images for a modular small complication, use the following values for guidance.

22.5x22.5 pt (45x45 px @2x)

29x29 pt (58x58 px @2x)

30.5x30.5 pt (61x61 px @2x)

34.5x34.5 pt (69x69 px @2x)

26x14 pt (52x28 px @2x)

29x15 pt (58x30 px @2x)

30.5x16 pt (61x32 px @2x)

32x17 pt (64x34 px @2x)

34.5x18 pt (69x36 px @2x)


### Modular large
Modular large templates offer a large canvas for displaying up to three rows of content (for example, in the center of the Modular watch face).

As you design icons and images for a modular large complication, use the following values for guidance.

11-32x11 pt (22-64x22 px @2x)

12-37x12 pt (24-74x24 px @2x)

12.5-39x12.5 pt (25-78x25 px @2x)

14-42x14 pt (28-84x28 px @2x)

14.5-44x14.5 pt (29-88x29 px @2x)


### Extra large
Extra large templates display larger text and images (for example, on the X-Large watch faces).

As you design icons and images for an extra large complication, use the following values for guidance.

63x63 pt (126x126 px @2x)

66.5x66.5 pt (133x133 px @2x)

70.5x70.5 pt (141x141 px @2x)

73x73 pt (146x146 px @2x)

79x79 pt (158x158 px @2x)

91x91 pt (182x182 px @2x)

101.5x101.5 pt (203x203 px @2x)

107.5x107.5 pt (215x215 px @2x)

112x112 pt (224x224 px @2x)

121x121 pt (242x242 px @2x )

78x42 pt (156x84 px @2x)

87x45 pt (174x90 px @2x)

92x47.5 pt (184x95 px @2x)

96x51 pt (192x102 px @2x)

103.5x53.5 pt (207x107 px @2x)

121x121 pt (242x242 px @2x)


## Platform considerations
Not supported in iOS, iPadOS, macOS, tvOS, or visionOS.


## Resources

#### Related

#### Developer documentation

#### Videos

## Change log
Replaced links to deprecated ClockKit documentation with links to WidgetKit documentation.

Updated guidance for rectangular complications to support them as widgets in the Smart Stack.

Added specifications for Apple Watch Ultra.


---

### Controls

**Source**: [https://developer.apple.com/design/human-interface-guidelines/controls](https://developer.apple.com/design/human-interface-guidelines/controls)


# Controls
A control is a button or toggle that provides quick access to your app’s features from other areas of the system. Control buttons perform an action, link to a specific area of your app, or launch a camera experience on a locked device. Control toggles switch between two states, such as on and off.

People can add controls to Control Center by pressing and holding in an empty area of Control Center, to the Lock Screen by customizing their Lock Screen, and to the Action button by configuring the Action button in the Settings app.


## Anatomy
Controls contain a symbol image, a title, and, optionally, a value. The symbol visually represents what the control does and can be a symbol from SF Symbols or a custom symbol. The title describes what the control relates to, and the value represents the state of the control. For example, the title can display the name of a light in a room, while the value can display whether it’s on or off.

Controls display their information differently depending on where they appear:

- In Control Center, a control displays its symbol and, at larger sizes, its title and value.
- On the Lock Screen, a control displays its symbol.
- On iPhone devices with a control assigned to the Action button, pressing and holding it displays the control’s symbol in the Dynamic Island, as well as its value (if present).
Control toggle in Control Center

Control toggle on the Lock Screen

Control toggle in the Dynamic Island
performed from the Action button


## Best practices
Offer controls for actions that provide the most benefit without having to launch your app. For example, launching a Live Activity from a control creates an easy and seamless experience that informs someone about progress without having to navigate to your app to stay up to date. For guidance, see Live Activities.

Update controls when someone interacts with them, when an action completes, or remotely with a push notification. Update the contents of a control to accurately reflect the state and show if an action is still in progress.

Choose a descriptive symbol that suggests the behavior of the control. Depending on where a person adds a control, it may not display the title and value, so the symbol needs to convey enough information about the control’s action. For control toggles, provide a symbol for both the on and off states. For example, use the SF Symbols door.garage.open and door.garage.closed to represent a control that opens and closes a garage door. For guidance, see SF Symbols.

Use symbol animations to highlight state changes. For control toggles, animate the transition between both on and off states. For control buttons with actions that have a duration, animate indefinitely while the action performs and stop animating when the action is complete. For developer guidance, see Symbols and SymbolEffect.

Select a tint color that works with your app’s brand. The system applies this tint color to a control toggle’s symbol in its on state. When a person performs the action of a control from the Action button, the system also uses this tint color to display the value and symbol in the Dynamic Island. For guidance, see Branding.

Nontinted control toggle in the off state

Tinted control toggle in the on state

Help people provide additional information the system needs to perform an action. A person may need to configure a control to perform a desired action — for example, select a specific light in a house to turn on and off. If a control requires configuration, prompt people to complete this step when they first add it. People can reconfigure the control at any time. For developer guidance, see promptsForUserConfiguration().

Provide hint text for the Action button. When a person presses the Action button, the system displays hint text to help them understand what happens when they press and hold. When someone presses and holds the Action button, the system performs the action configured to it. Use verbs to construct the hint text. For developer guidance, see controlWidgetActionHint(_:).

If your control title or value can vary, include a placeholder. Placeholder information tells people what your control does when the title and value are situational. The system displays this information when someone brings up the controls gallery in Control Center or the Lock Screen and chooses your control, or before they assign it to the Action button.

Hide sensitive information when the device is locked. When the device is locked, consider having the system redact the title and value to hide personal or security-related information. Specify if the system needs to redact the symbol state as well. If specified, the system redacts the title and value, and displays the symbol in its off state.

Control toggle with no information hidden

Control toggle with information hidden on a locked device

Require authentication for actions that affect security. For example, require people to unlock their device to access controls to lock or unlock the door to their house or start their car. For developer guidance, see IntentAuthenticationPolicy.


## Camera experiences on a locked device
If your app supports camera capture, starting with iOS 18 you can create a control that launches directly to your app’s camera experience while the device is locked. For any task beyond capture, a person must authenticate and unlock their device to complete the task in your app. For developer guidance, see LockedCameraCapture.

Use the same camera UI in your app and your camera experience. Sharing UI leverages people’s familiarity with the app. By using the same UI, the transition to the app is seamless when someone captures content and taps a button to perform additional tasks, such as posting to a social network or editing a photo.

Provide instructions for adding the control. Help people understand how to add the control that launches this camera experience.


## Platform considerations
No additional considerations for iOS or iPadOS. Not supported in macOS, watchOS, tvOS, or visionOS.


## Resources

#### Related

#### Developer documentation

## Change log

---

### Live Activities

**Source**: [https://developer.apple.com/design/human-interface-guidelines/live-activities](https://developer.apple.com/design/human-interface-guidelines/live-activities)


# Live Activities
Live Activities let people keep track of tasks and events in glanceable locations across devices. They go beyond push notifications, delivering frequent content and status updates over a few hours and letting people interact with the displayed information.

For example, a Live Activity might show the remaining time until a food delivery order arrives, live in-game information for a soccer match, or real-time fitness metrics and interactive controls to pause or cancel a workout.

Live Activities start on iPhone or iPad and automatically appear in system locations across a person’s devices:

Platform or system experience

Lock Screen, Home Screen, in the Dynamic Island and StandBy on iPhone


## Anatomy
Live Activities appear across the system in various locations like the Dynamic Island and the Lock Screen. It serves as a unified home for alerts and indicators of ongoing activity. Depending on the device and system location where a Live Activity appears, the system chooses a presentation style or a combination of styles to compose the appearance of your Live Activity. As a result, your Live Activity must support:

- Compact
- Minimal
- Expanded
- Lock Screen
In iOS and iPadOS, your Live Activity appears throughout the system using these presentations. Additionally, the system uses them to create default appearances for other contexts. For example, the compact presentation appears in the Dynamic Island on iPhone and consists of two elements that the system combines into a single view for Apple Watch and in CarPlay.

In the Dynamic Island, the system uses the compact presentation when only one Live Activity is active. The presentation consists of two separate elements: one on the leading side of the TrueDepth camera and one on the trailing side. Despite its limited space, the compact presentation displays up-to-date information about your app’s Live Activity.

For design guidance, see Compact presentation.

When multiple Live Activities are active, the system uses the minimal presentation to display two of them in the Dynamic Island. One appears attached to the Dynamic Island while the other appears detached. Depending on its content size, the detached minimal presentation appears circular or oval. As with the compact presentation, people tap the minimal presentation to open its app or touch and hold it to see the expanded presentation.

For design guidance, see Minimal presentation.

When people touch and hold a Live Activity in compact or minimal presentation, the system displays the expanded presentation.

For design guidance, see Expanded presentation.

The system uses the Lock Screen presentation to display a banner at the bottom of the Lock Screen. In this presentation, use a layout similar to the expanded presentation.

When you alert people about Live Activity updates on devices that don’t support the Dynamic Island, the Lock Screen presentation briefly appears as a banner that overlays the Home Screen or other apps.

For design guidance, see Lock Screen presentation.


### StandBy
On iPhone in StandBy, your Live Activity appears in the minimal presentation. When someone taps it, it transitions to the Lock Screen presentation, scaled up by 2x to fill the screen. If your Lock Screen presentation uses a custom background color, the system automatically extends it to the whole screen to create a seamless, full-screen design.

For design guidance, see StandBy presentation.


## Best practices
Offer Live Activities for tasks and events that have a defined beginning and end. Live Activities work best for tracking short to medium duration activities that don’t exceed eight hours.

Focus on important information that people need to see at a glance. Your Live Activity doesn’t need to display everything. Think about what information people find most useful and prioritize sharing it in a concise way. When a person wants to learn more, they can tap your Live Activity to open your app where you can provide additional detail.

Don’t use a Live Activity to display ads or promotions. Live Activities help people stay informed about ongoing events and tasks, so it’s important to display only information that’s related to those events and tasks.

Avoid displaying sensitive information. Live Activities are prominently visible and could be viewed by casual observers; for example, on the Lock Screen or in the Always-On display. For content people might consider sensitive or private, display an innocuous summary and let people tap the Live Activity to view the sensitive information in your app. Alternatively, redact views that may contain sensitive information and let people configure whether to show sensitive data. For developer guidance, see Creating a widget extension.

Create a Live Activity that matches your app’s visual aesthetic and personality in both dark and light appearances. This makes it easier for people to recognize your Live Activity and creates a visual connection to your app.

If you include a logo mark, display it without a container. This better integrates the logo mark with your Live Activity layout. Don’t use the entire app icon.

Don’t add elements to your app that draw attention to the Dynamic Island. Your Live Activity appears in the Dynamic Island while your app isn’t in use, and other items can appear in the Dynamic Island when your app is open.

Ensure text is easy to read. Use large, heavier-weight text — a medium weight or higher. Use small text sparingly and make sure key information is legible at a glance.


### Creating Live Activity layouts
Adapt to different screen sizes and presentations. Live Activities scale to fit various device screens. Create layouts and assets for various devices and scale factors, recognizing that the actual size on screen may vary or change. Ensure they look great everywhere by using the values in Specifications as guidance and providing appropriately sized content.

Adjust element size and placement for efficient use of space. Create a layout that only uses the space you need to clearly display its content. Adapt the size and placement of elements in your Live Activity so they fit well together.

Use familiar layouts for custom views and layouts. Templates with default system margins and recommended text sizes are available in Apple Design Resources. Using them helps your Live Activity remain legible at a glance and fit in with the visual language of its surroundings; for example, the Smart Stack on Apple Watch.

Use consistent margins and concentric placement. Use even, matching margins between rounded shapes and the edges of the Live Activity, including corners, to ensure a harmonious fit. This prevents elements from poking into the rounded shape of the Live Activity and creating visual tension. For example, when placing a rounded rectangle near a corner of your Live Activity, match its corner radius to the outer corner radius of the Live Activity by subtracting the margin and using a SwiftUI container to apply the correct corner radius. For developer guidance, see ContainerRelativeShape.

Keep content compact and snug within a margin that’s concentric to the outer edge of the Live Activity.

When separating a block of content, place it in an inset container shape or use a thick line. Don’t draw content all the way to the edge of the Dynamic Island.

To align nonrounded content in the rounded corners of the Live Activity view, it may be helpful to blur the nonrounded content in your drawing tool. When the content is blurred, it may be easier to find the positioning that best aligns with the outer perimeter of the view.

Dynamically change the height of your Live Activity on the Lock Screen or in the expanded presentation. When there’s less information to show, reduce the height of the Live Activity to only use the space needed for the content. When more information becomes available, increase the height to display additional content. For example, a rideshare app might display a more compact Live Activity without additional details while it locates a driver. The app’s height extends as more information is available to display the estimated pickup time, driver details, and so on.


### Choosing colors
Carefully consider using a custom background color and opacity. You can’t customize background colors for compact, minimal, and expanded presentations. However, you can use a custom background color for the Lock Screen presentation. If you set a custom background color or image for the Lock Screen presentation, ensure sufficient contrast — especially for tint colors on devices that feature an Always-On display with reduced luminance.

Use color to express the character and identity of your app. Live Activities in the Dynamic Island use a black opaque background. Consider using bold colors for text and objects to convey the personality and brand of your app. Bold colors make your Live Activity recognizable at a glance, stand out from other Live Activities, and feel like a small, glanceable part of your app. Additionally, bold colors can help reinforce the relationship between elements in the Live Activity itself.

Tint your Live Activity’s key line color so that it matches your content. When the background is dark — for example, in Dark Mode — a key line appears around the Dynamic Island to distinguish it from other content. Choose a key line color that’s consistent with the color of other elements in your Live Activity. For developer guidance, see Creating custom views for Live Activities.


### Adding transitions and animating content updates
In addition to extending and contracting transitions, Live Activities use system and custom animations with a maximum duration of two seconds. Note that the system doesn’t perform animations on Always-On displays with reduced luminance.

Use animations to reinforce the information you’re communicating and to bring attention to updates. In addition to moving the position of elements, you can animate elements in and out with the default content-replace transition, or create custom transitions using scale, opacity, and movement. For example, a sports app might use numeric content transitions for score changes or fade a timer in and out when it reaches zero.

Animate layout changes. Content updates can require a change to your Live Activity layout — for example, when it expands to fill the screen in StandBy or when more information becomes available. During the transition to a new layout, preserve as much of the existing layout as possible by animating existing elements to their new positions rather than removing and animating them back in.

Try to avoid overlapping elements. Sometimes, it’s best to animate out certain elements and then re-animate them in at a new position to avoid colliding with other parts of your transition. For example, when animating items in lists, only animate the element that moves to a new position and use fade-in-and-out transitions for the other list items.

For developer guidance, see Animating data updates in widgets and Live Activities.


### Offering interactivity
Make sure tapping the Live Activity opens your app at the right location. Take people directly to related details and actions — don’t make them navigate to find relevant information. For developer guidance on SwiftUI views that support deep linking to specific screens, see Linking to specific app scenes from your widget or Live Activity.

Focus on simple, direct actions. Buttons or toggles take up space that might otherwise display useful information. Only include interactive elements for essential functionality that’s directly related to your Live Activity and that people activate once or temporarily pause and resume, like music playback, workouts, or apps that access the microphone to record live audio. If you offer interactivity, prefer limiting it to a single element to help people avoid accidentally tapping the wrong control.

Consider letting people respond to event or progress updates. If an update to your Live Activity is something that a person could respond to, consider offering a button or toggle to let people take action. For example, the Live Activity of a rideshare app could include a button to contact the driver while waiting for a ride to arrive.


### Starting, updating, and ending a Live Activity
Start Live Activities at appropriate times, and make it easy for people to turn them off in your app. People expect Live Activities to start and provide important updates for a task at hand or at specific times, even automatically. For example, they might expect a Live Activity to start after a food order, making a rideshare request, or when their favorite sports team’s match begins. However, Live Activities that appear unexpectedly can be surprising or even unwanted. Consider offering controls that allow people to turn off a Live Activity in the app view that corresponds to the activity. For example, a sports app may offer a button that lets people unfollow a game or team. When people can’t easily control the appearance of Live Activities from your app, they may choose to turn off Live Activities in Settings altogether.

Offer an App Shortcut that starts your Live Activity. App Shortcuts expose functionality to the system, allowing access in various contexts. For example, create an App Shortcut that allows people to start your Live Activity using the Action button on iPhone. For more information, see App Shortcuts.

Update a Live Activity only when new content is available. If the underlying content or status remains the same, maintain the same display until the underlying content or status changes.

Alert people only for essential updates that require their attention. Live Activity alerts light up the screen and by default play the notification sound to alert people about updates they shouldn’t miss. Alerts also show the expanded presentation in the Dynamic Island or a banner on devices that don’t support the Dynamic Island. To ensure your Live Activities provide the most value, avoid alerting people too often or with updates that aren’t crucial, and don’t use push notifications alongside Live Activities for the same updates.

Let people track multiple events efficiently with a single Live Activity. Instead of creating separate Live Activities people need to jump between to track different events, prefer a single Live Activity that uses a dynamic layout and rotates through events. For example, a sports app could offer a single Live Activity that cycles through scored points, substitutions, and fouls across multiple matches.

Always end a Live Activity immediately when the task or event ends, and consider setting a custom dismissal time. When a Live Activity ends, the system immediately removes it from the Dynamic Island and in CarPlay. On the Lock Screen, in the Mac menu bar, and the watchOS Smart Stack, it remains for up to four hours. Depending on the Live Activity, showing a summary may only be relevant for a brief time after it ends. Consider choosing a custom dismissal time that’s proportional to the duration of your Live Activity. In most cases, 15 to 30 minutes is adequate. For example, a rideshare app could end its Live Activity when a ride completes and remain visible for 30 minutes to allow people to view the ride summary and leave a tip. For developer guidance, refer to Displaying live data with Live Activities.


## Presentation
Your Live Activity needs to support all locations, devices, and their corresponding appearances. Because it appears across systems at different dimensions, create Live Activity layouts that best support each place they appear.

Start with the iPhone design, then refine it for other contexts. Create standard designs for each presentation first. Then, depending on the functionality that your Live Activity provides, design additional custom layouts for specific contexts like iPhone in StandBy, CarPlay, or Apple Watch. For more information about custom layouts, refer to StandBy, CarPlay, and watchOS.


### Compact presentation
Focus on the most important information. Use the compact presentation to show dynamic, up-to-date information that’s essential to the Live Activity and easy to understand. For example, a sports app could display two team logos and the score.

Ensure unified information and design of the compact presentations in the Dynamic Island. Though the TrueDepth camera separates the leading and trailing elements, design them to read as a single piece of information, and use consistent color and typography to help create a connection between both elements.

Keep content as narrow as possible and ensure it’s snug against the TrueDepth camera. Try not to obscure key information in the status bar, and don’t add padding between content and the TrueDepth camera. Maintain a balanced layout with similarly sized views for both leading and trailing elements; for example, use shortened units or less precise data to maintain appropriate width and balance.

Link to relevant app content. When people tap a compact Live Activity, open your app directly to the related details. Ensure both leading and trailing elements link to the same screen.


### Minimal presentation
Ensure that your Live Activity is recognizable in the minimal presentation. If possible, display updated information rather than just a logo, while ensuring people can quickly recognize your app. For example, the Timer app’s minimal Live Activity presentation displays the remaining time instead of a static icon.


### Expanded presentation
Maintain the relative placement of elements to create a coherent layout between presentations. The expanded presentation is an enlarged version of the compact or minimal presentation. Ensure information and layouts expand predictably when the Live Activity expands.

Wrap content tightly around the TrueDepth camera. Arrange content close to the TrueDepth camera, and try to avoid leaving too much room around it to use space more efficiently and to help diminish the camera’s presence.


### Lock Screen presentation
Don’t replicate notification layouts. Create a unique layout that’s specific to the information that appears in the Live Activity.

Choose colors that work well on a personalized Lock Screen. People customize their Lock Screen with wallpapers, custom tint colors, and widgets. To make a Live Activity fit a custom Lock Screen aesthetic while remaining legible, use custom background or tint colors and opacity sparingly.

Make sure your design, assets, and colors look great and offer enough contrast in Dark Mode and on an Always-On display. By default, a Live Activity on the Lock Screen uses a light background color in the light appearance and a dark background color in the dark appearance. If you use a custom background color, choose a color that works well in both modes or a different color for each appearance. Verify your choices on a device with an Always-On display with reduced luminance because the system adapts colors as needed in this appearance. For guidance, see Dark Mode and Always On; for developer guidance, see About asset catalogs.

Verify the generated color of the dismiss button. The system automatically generates a matching dismiss button based on the background and foreground colors of your Live Activity. Verify that the generated color matches your design and adjust it if needed using activitySystemActionForegroundColor(_:).

Use standard margins to align your design with notifications. The standard layout margin for Live Activities on the Lock Screen is 14 points. While tighter margins may be appropriate for elements like graphics or buttons, avoid crowding the edges and creating a cluttered appearance. For developer guidance, see padding(_:_:).


### StandBy presentation
Update your layout for StandBy. Make sure assets look great at the larger scale, and consider creating a custom layout that makes use of the extra space. For developer guidance, see Creating custom views for Live Activities.

Consider using the default background color in StandBy. The default background color seamlessly blends your Live Activity with the device bezel, achieves a softer look that integrates with a person’s surroundings, and allows the system to scale the Live Activity slightly larger because it doesn’t need to account for the margins around the TrueDepth camera.

Use standard margins and avoid extending graphic elements to the edge of the screen. Without standard margins, content gets cut off as the Live Activity extends, making it feel broken.

Verify your design in Night Mode. In Night Mode, the system applies a red tint to your Live Activity. Check that your Live Activity design uses colors that provide enough contrast in Night Mode.


## CarPlay
In CarPlay, the system automatically combines the leading and trailing elements of the compact presentation into a single layout that appears on CarPlay Dashboard.

Your Live Activity design applies to both CarPlay and Apple Watch, so design for both contexts. While Live Activities on Apple Watch can be interactive, the system deactivates interactive elements in CarPlay. For more information, refer to watchOS below. For developer guidance, refer to Creating custom views for Live Activities.

Consider creating a custom layout if your Live Activity would benefit from larger text or additional information. Instead of using the default appearance in CarPlay, declare support for a ActivityFamily.small supplemental activity family.

Carefully consider including buttons or toggles in your custom layout. In CarPlay, the system deactivates interactive elements in your Live Activity. If people are likely to start or observe your Live Activity while driving, prefer displaying timely content rather than buttons and toggles.


## Platform considerations
No additional considerations for iOS or iPadOS. Not supported in tvOS or visionOS.


### macOS
Active Live Activities automatically appear in the Menu bar of a paired Mac using the compact, minimal, and expanded presentations. Clicking the Live Activity launches iPhone Mirroring to display your app.


### watchOS
When a Live Activity begins on iPhone, it appears on a paired Apple Watch at the top of the Smart Stack. By default, the view displayed in the Smart Stack combines the leading and trailing elements from the Live Activity’s compact presentation on iPhone.

If you offer a watchOS app and someone taps the Live Activity in the Smart Stack, it opens your watchOS app. Without a watchOS app, tapping opens a full-screen view with a button to open your app on the paired iPhone.

Consider creating a custom watchOS layout. While the system provides a default view automatically, a custom layout designed for Apple Watch can show more information and add interactive functionality like a button or toggle.

Carefully consider including buttons or toggles in your custom layout. The custom watchOS layout also applies to your Live Activity in CarPlay where the system deactivates interactive elements. If people are likely to start or observe your Live Activity while driving, don’t include buttons or toggles in your custom watchOS layout. For developer guidance, see Creating custom views for Live Activities.

Default Smart Stack view

Custom Smart Stack view

Focus on essential information and significant updates. Use space in the Smart Stack as efficiently as possible and think of the most useful information that a Live Activity can convey:

- Progress, like the estimated arrival time of a delivery
- Interactive elements, like stopwatch or timer controls
- Significant updates, like sports score changes

## Specifications
When you design your Live Activities, use the following values for guidance.


### CarPlay dimensions
The system may scale your Live Activity to best fit a vehicle’s screen size and resolution. Use the listed values to verify your design:

Live Activity size (pt)

Test your designs with the CarPlay Simulator and the following configurations for Smart Display Zoom — available in in Settings > Display in CarPlay:


### iOS dimensions
All values listed in the tables below are in points.

Screen dimensions (portrait)

Minimal (width given as a range)

Expanded (height given as a range)

Lock Screen (height given as a range)

The Dynamic Island uses a corner radius of 44 points, and its rounded corner shape matches the TrueDepth camera.

Dynamic Island width (pt)


### iPadOS dimensions
All values listed in the table below are in points.


### macOS dimensions
Use the provided iOS dimensions.


### watchOS dimensions
Live Activities in the Smart Stack use the same dimensions as watchOS widgets.

Size of a Live Activity in the Smart Stack (pt)


## Resources

#### Developer documentation
Developing a WidgetKit strategy — WidgetKit


#### Videos

## Change log
Updated guidance for all platforms, and added guidance for macOS and CarPlay.

Added guidance for Live Activities in watchOS.

Expanded and updated guidance and added new artwork.

Updated guidance to include features of iOS 17 and iPadOS 17.

Updated artwork and specifications.


---

### Notifications

**Source**: [https://developer.apple.com/design/human-interface-guidelines/notifications](https://developer.apple.com/design/human-interface-guidelines/notifications)


# Notifications
Before you can send any notifications to people, you have to get their consent (for developer guidance, see Asking permission to use notifications). After agreeing, people generally use settings to specify the styles of notification they want to receive, and to specify delivery times for notifications that have different levels of urgency. To learn more about the ways people can customize the notification experience, see Managing notifications.


## Anatomy
Depending on the platform, a notification can use various styles, such as:

- A banner or view on a Lock Screen, Home Screen, Home View, or desktop
- A badge on an app icon
- An item in Notification Center
In addition, a notification related to direct communication — like a phone call or message — can provide an interface that’s distinct from noncommunication notifications, featuring prominent contact images (or avatars) and group names instead of the app icon.


## Best practices
Provide concise, informative notifications. People turn on notifications to get quick updates, so you want to provide valuable information succinctly.

Avoid sending multiple notifications for the same thing, even if someone hasn’t responded. People attend to notifications at their convenience. If you send multiple notifications for the same thing, you fill up Notification Center, and people may turn off all notifications from your app.

Avoid sending a notification that tells people to perform specific tasks within your app. If it makes sense to offer simple tasks that people can perform without opening your app, you can provide notification actions. Otherwise, avoid telling people what to do because it’s hard for people to remember such instructions after they dismiss the notification.

Use an alert — not a notification — to display an error message. People are familiar with both alerts and notifications, so you don’t want to cause confusion by using the wrong component. For guidance, see Alerts.

Handle notifications gracefully when your app is in the foreground. Your app’s notifications don’t appear when your app is in the front, but your app still receives the information. In this scenario, present the information in a way that’s discoverable but not distracting or invasive, such as incrementing a badge or subtly inserting new data into the current view. For example, when a new message arrives in a mailbox that people are currently viewing, Mail simply adds it to the list of unread messages because sending a notification about it would be unnecessary and distracting.

Avoid including sensitive, personal, or confidential information in a notification. You can’t predict what people will be doing when they receive a notification, so it’s essential to avoid including private information that could be visible to others.


## Content
When a notification includes a title, the system displays it at the top where it’s most visible. In a notification related to direct communication, the system automatically displays the sender’s name in the title area; in a noncommunication notification, the system displays your app name if you don’t provide a title.

Create a short title if it provides context for the notification content. Prefer brief titles that people can read at a glance, especially on Apple Watch, where space is limited. When possible, take advantage of the prominent notification title area to provide useful information, like a headline, event name, or email subject. If you can only provide a generic title for a noncommunication notification — like New Document — it can be better to let the system display your app name instead. Use title-style capitalization and no ending punctuation.

Write succinct, easy-to-read notification content. Use complete sentences, sentence case, and proper punctuation, and don’t truncate your message — the system does this automatically when necessary.

Provide generically descriptive text to display when notification previews aren’t available. In Settings, people can choose to hide notification previews for all apps. In this situation, the system shows only your app icon and the default title Notification. To give people sufficient context to know whether they want to view the full notification, write body text that succinctly describes the notification content without revealing too many details, like “Friend request,” “New comment,” “Reminder,” or “Shipment” (for developer guidance, see hiddenPreviewsBodyPlaceholder). Use sentence-style capitalization for this text.

Avoid including your app name or icon. The system automatically displays a large version of your app icon at the leading edge of each notification; in a communication notification, the system displays the sender’s contact image badged with a small version of your icon.

Consider providing a sound to supplement your notifications. Sound can be a great way to distinguish your app’s notifications and get someone’s attention when they’re not looking at the device. You can create a custom sound that coordinates with the style of your app or use a system-provided alert sound. If you use a custom sound, make sure it’s short, distinctive, and professionally produced. A notification sound can enhance the user experience, but don’t rely on it to communicate important information, because people may not hear it. Although people might also want a vibration to accompany alert sounds, you can’t provide such a vibration programmatically. For developer guidance, see UNNotificationSound.


## Notification actions
A notification can present a customizable detail view that contains up to four buttons people use to perform actions without opening your app. For example, a Calendar event notification provides a Snooze button that postpones the event’s alarm for a few minutes. For developer guidance, see Handling notifications and notification-related actions.

Provide beneficial actions that make sense in the context of your notification. Prefer actions that let people perform common, time-saving tasks that eliminate the need to open your app. For each button, use a short, title-case term or phrase that clearly describes the result of the action. Don’t include your app name or any extraneous information in the button label, keep the text brief to avoid truncation, and take localization into account as you write it.

Avoid providing an action that merely opens your app. When people tap a notification or its preview, they expect your app to display related content, so presenting an action button that does the same thing clutters the detail view and can be confusing.

Prefer nondestructive actions. If you must provide a destructive action, make sure people have enough context to avoid unintended consequences. The system gives a distinct appearance to the actions you identify as destructive.

Provide a simple, recognizable interface icon for each notification action. An interface icon reinforces an action’s meaning, helping people instantly understand what it does. The system displays your interface icon on the trailing side of the action title. When you use SF Symbols, you can choose an existing symbol that represents your command or edit a related symbol to create a custom icon.


## Badging
A badge is a small, filled oval containing a number that can appear on an app icon to indicate the number of unread notifications that are available. After people address unread notifications, the badge disappears from the app icon, reappearing when new notifications arrive. People can choose whether to allow an app to display badges in their notification settings.

Use a badge only to show people how many unread notifications they have. Don’t use a badge to convey numeric information that isn’t related to notifications, such as weather-related data, dates and times, stock prices, or game scores.

Make sure badging isn’t the only method you use to communicate essential information. People can turn off badging for your app, so if you rely on it to show people when there’s important information, people can miss the message. Always make sure that you make important information easy for people to find as soon as they open your app.

Keep badges up to date. Update your app’s badge as soon as people open the corresponding notifications. You don’t want people to think there are new notifications available, only to find that they’ve already viewed them all. Note that reducing a badge’s count to zero removes all related notifications from Notification Center.

Avoid creating a custom image or component that mimics the appearance or behavior of a badge. People can turn off notification badges if they choose, and will become frustrated if they have done so and then see what appears to be a badge.


## Platform considerations
No additional considerations for iOS, iPadOS, macOS, tvOS, or visionOS.


### watchOS
On Apple Watch, notifications occur in two stages: short look and long look. People can also view notifications in Notification Center. On supported devices, people can double-tap to respond to notifications.

You can help people have a great notification experience by designing app-specific assets and actions that are relevant on Apple Watch. If your watchOS app has an iPhone companion that supports notifications, watchOS can automatically provide default short-look and long-look interfaces if necessary.


#### Short looks
A short look appears when the wearer’s wrist is raised and disappears when it’s lowered.

Avoid using a short look as the only way to communicate important information. A short look appears only briefly, giving people just enough time to see what the notification is about and which app sent it. If your notification information is critical, make sure you deliver it in other ways, too.

Keep privacy in mind. Short looks are intended to be discreet, so it’s important to provide only basic information. Avoid including potentially sensitive information in the notification’s title.


#### Long looks
Long looks provide more detail about a notification. If necessary, people can swipe vertically or use the Digital Crown to scroll a long look. After viewing a long look, people can dismiss it by tapping it or simply by lowering their wrist.

A custom long-look interface can be static or dynamic. The static interface lets you display a notification’s message along with additional static text and images. The dynamic interface gives you access to the notification’s full content and offers more options for configuring the appearance of the interface.

You can customize the content area for both static and dynamic long looks, but you can’t change the overall structure of the interface. The system-defined structure includes a sash at the top of the interface and a Dismiss button at the bottom, below all custom buttons.

Consider using a rich, custom long-look notification to let people get the information they need without launching your app. You can use SwiftUI Animations to create engaging, interruptible animations; alternatively, you can use SpriteKit or SceneKit.

At the minimum, provide a static interface; prefer providing a dynamic interface too. The system defaults to the static interface when the dynamic interface is unavailable, such as when there is no network or the iPhone companion app is unreachable. Be sure to create the resources for your static interface in advance and package them with your app.

Choose a background appearance for the sash. The system-provided sash, at the top of the long-look interface, displays your app icon and name. You can customize the sash’s color or give it a blurred appearance. If you display a photo at the top of the content area, you’ll probably want to use the blurred sash, which has a light, translucent appearance that gives the illusion of overlapping the image.

Choose a background color for the content area. By default, the long look’s background is transparent. If you want to match the background color of other system notifications, use white with 18% opacity; otherwise, you can use a custom color, such as a color within your brand’s palette.

Provide up to four custom actions below the content area. For each long look, the system uses the notification’s type to determine which of your custom actions to display as buttons in the notification UI. In addition, the system always displays a Dismiss button at the bottom of the long-look interface, below all custom buttons. If your watchOS app has an iPhone companion that supports notifications, the system shares the actionable notification types already registered by your iPhone app and uses them to configure your custom action buttons.


#### Double tap
People can double-tap to respond to notifications on supported devices. When a person responds to a notification with a double tap, the system selects the first nondestructive action as the response.

Keep double tap in mind when choosing the order of custom actions you present as responses to a notification. Because a double tap runs the first nondestructive action, consider placing the action that people use most frequently at the top of the list. For example, a parking app that provides custom actions for extending the time on a paid parking spot could offer options to extend the time by 5 minutes, 15 minutes, or an hour, with the most common choice listed first.


## Resources

#### Related
Managing notifications


#### Developer documentation
Asking permission to use notifications — User Notifications

User Notifications UI


#### Videos

## Change log
Updated watchOS platform considerations with guidance for presenting notification responses to double tap.


---

### Status Bars

**Source**: [https://developer.apple.com/design/human-interface-guidelines/status-bars](https://developer.apple.com/design/human-interface-guidelines/status-bars)


# Status bars

## Best practices
Obscure content under the status bar. By default, the background of the status bar is transparent, allowing content beneath to show through. This transparency can make it difficult to see information presented in the status bar. If controls are visible behind the status bar, people may attempt to interact with them and be unable to do so. Be sure to keep the status bar readable, and don’t imply that content behind it is interactive. Prefer using a scroll edge effect to place a blurred view behind the status bar. For developer guidance, see ScrollEdgeEffectStyle and UIScrollEdgeEffect.

Consider temporarily hiding the status bar when displaying full-screen media. A status bar can be distracting when people are paying attention to media. Temporarily hide these elements to provide a more immersive experience. The Photos app, for example, hides the status bar and other interface elements when people browse full-screen photos.

The Photos app with the status bar visible

The Photos app with the status bar hidden

Avoid permanently hiding the status bar. Without a status bar, people have to leave your app to check the time or see if they have a Wi-Fi connection. Let people redisplay a hidden status bar with a simple, discoverable gesture. For example, when browsing full-screen photos in the Photos app, a single tap shows the status bar again.


## Platform considerations
No additional considerations for iOS or iPadOS. Not supported in macOS, tvOS, visionOS, or watchOS.


## Resources

#### Developer documentation
UIStatusBarStyle — UIKit

preferredStatusBarStyle — UIKit


---

### Top Shelf

**Source**: [https://developer.apple.com/design/human-interface-guidelines/top-shelf](https://developer.apple.com/design/human-interface-guidelines/top-shelf)


# Top Shelf
When you support full-screen Top Shelf, people can swipe through multiple full-screen content views, play trailers and previews, and get more information about your content.

Top Shelf is a unique opportunity to highlight new, featured, or recommended content and let people jump directly to your app or game to view it. For example, when people select Apple TV in the Dock, full-screen previews immediately begin playing and soon the Dock slides away. As people watch previews for the first show, they can swipe through previews of all other featured shows, stopping to select Play or More Info for a preview that interests them.

The system defines several layout templates that you can use to give people a compelling Top Shelf experience when they select your app in the Dock. To help you position content, you can download these templates from Apple Design Resources.


## Best practices
Help people jump right into your content. Top Shelf provides a path to the content people care about most. Two of the system-provided layout templates — carousel actions and carousel details — each include two buttons by default: A primary button intended to begin playback and a More Info button intended to open your app to a view that displays details about the content.

Feature new content. For example, showcase new releases or episodes, highlight upcoming movies and shows, and avoid promoting content that people have already purchased, rented, or watched.

Personalize people’s favorite content. People typically put the apps they use most often into Top Shelf. You can personalize their experience by showing targeted recommendations in the Top Shelf content you supply, letting people resume media playback or jump back into active gameplay.

Avoid showing advertisements or prices. People put your app into Top Shelf because you’ve already sold them on it, so they may not appreciate seeing lots of ads from your app. Showing purchasable content in the Top Shelf is fine, but prefer putting the focus on new and exciting content, and consider displaying prices only when people show interest.

Showcase compelling dynamic content that can help draw people in and encourage them to view more. If necessary, you can supply static images, but people typically prefer a captivating, dynamic Top Shelf experience that features the newest or highest rated content. To provide this experience, prefer creating layered images to display in Top Shelf.

If you don’t provide the recommended full-screen content, supply at least one static image as a fallback. The system displays a static image when your app is in the Dock and in focus and full-screen content is unavailable. tvOS flips and blurs the image, ensuring that it fits into a width of 1920 pixels at the 16:9 aspect ratio. Use the following values for guidance.

2320x720 pt (2320x720 px @1x, 4640x1440 px @2x)

Avoid implying interactivity in a static image. A static Top Shelf image isn’t focusable, and you don’t want to make people think it’s interactive.


## Dynamic layouts
Dynamic Top Shelf images can appear in several ways:

- A carousel of full-screen video and images that includes two buttons and optional details
- A row of focusable content
- A set of scrolling banners

### Carousel actions
The carousel actions layout style focuses on full-screen video and images and includes a few unobtrusive controls that help people see more. This layout style works especially well to showcase content that people already know something about. For example, it’s great for displaying user-generated content, like photos, or new content from a franchise or show that people are likely to enjoy.

Provide a title. Include a succinct title, like the title of the show or movie or the title of a photo album. If necessary, you can also provide a brief subtitle. For example, a subtitle for a photo album could be a range of dates; a subtitle for an episode could be the name of the show.


### Carousel details
This layout style extends the carousel actions layout style, giving you the opportunity to include some information about the content. For example, you might provide information like a plot summary, cast list, and other metadata that helps people decide whether to choose the content.

Provide a title that identifies the currently playing content. The content title appears near the top of the screen so it’s easy for people to read it at a glance. Above the title, you can also provide a succinct phrase or app attribution, like “Featured on My App.”


### Sectioned content row
This layout style shows a single labeled row of sectioned content, which can work well to highlight recently viewed content, new content, or favorites. Row content is focusable, which lets people scroll quickly through it. A label appears when an individual piece of content comes into focus, and small movements on the remote’s Touch surface bring the focused image to life. You can also configure a sectioned content row to show multiple labels.

Provide enough content to constitute a complete row. At a minimum, load enough images in a sectioned content row to span the full width of the screen. In addition, include at least one label for greater platform consistency and to provide additional context.

You can use the following image sizes in a sectioned content row.


#### Poster (2:3)
404x608 pt (404x608 px @1x, 808x1216 px @2x)

Focused/Safe zone size

380x570 pt (380x570 px @1x, 760x1140 px @2x)

333x570 pt (333x570 px @1x, 666x1140 px @2x)


#### Square (1:1)
608x608 pt (608x608 px @1x, 1216x1216 px @2x)

570x570 pt (570x570 px @1x, 1140x1140 px @2x)

500x500 pt (500x500 px @1x, 1000x1000 px @2x)


#### 16:9
908x512 pt (908x512 px @1x, 1816x1024 px @2x)

852x479 pt (852x479 px @1x, 1704x958 px @2x)

782x440 pt (782x440 px @1x, 1564x880 px @2x)

Be aware of additional scaling when combining image sizes. If your Top Shelf design includes a mixture of image sizes, keep in mind that images will automatically scale up to match the height of the tallest image if necessary. For example, a 16:9 image scales to 500 pixels high if included in a row with a poster or square image.


#### Scrolling inset banner
This layout shows a series of large images, each of which spans almost the entire width of the screen. Apple TV automatically scrolls through these banners on a preset timer until people bring one into focus. The sequence circles back to the beginning after the final image is reached.

When a banner is in focus, a small, circular gesture on the remote’s Touch surface enacts the system focus effect, animating the item, applying lighting effects, and, if the banner contains layered images, producing a 3D effect. Swiping on the Touch surface pans to the next or previous banner in the sequence. Use this style to showcase rich, captivating content, such as a popular new movie.

Provide three to eight images. A minimum of three images is recommended for a scrolling banner to feel effective. More than eight images can make it hard to navigate to a specific image.

If you need text, add it to your image. This layout style doesn’t show labels under content, so all text must be part of the image itself. In layered images, consider elevating text by placing it on a dedicated layer above the others. Add the text to the accessibility label of the image too, so VoiceOver can read it.

Use the following size for a scrolling inset banner image:

1940x692 pt (1940x692 px @1x, 3880x1384 px @2x)

1740x620 pt (1740x620 px @1x, 3480x1240 px @2x)

1740x560 pt (1740x560 px @1x, 3480x1120 px @2x)


## Platform considerations
Not supported in iOS, iPadOS, macOS, visionOS, or watchOS.


## Resources

#### Related
Apple Design Resources


#### Videos

---

### Watch Faces

**Source**: [https://developer.apple.com/design/human-interface-guidelines/watch-faces](https://developer.apple.com/design/human-interface-guidelines/watch-faces)


# Watch faces
The watch face is at the heart of the watchOS experience. People choose a watch face they want to see every time they raise their wrist, and they customize it with their favorite complications. People can even customize different watch faces for different activities, so they can switch to the watch face that fits their current context.

In watchOS 7 and later, people can share the watch faces they configure. For example, a fitness instructor might configure a watch face to share with their students by choosing the Gradient watch face, customizing the color, and adding their favorite health and fitness complications. When the students add the shared watch face to their Apple Watch or the Watch app on their iPhone, they get a custom experience without having to configure it themselves.

You can also configure a watch face to share from within your app, on your website, or through Messages, Mail, or social media. Offering shareable watch faces can help you introduce more people to your complications and your app.


## Best practices
Help people discover your app by sharing watch faces that feature your complications. Ideally, you support multiple complications so that you can showcase them in a shareable watch face and provide a curated experience. For some watch faces, you can also specify a system accent color, images, or styles. If people add your watch face but haven’t installed your app, the system prompts them to install it.

Display a preview of each watch face you share. Displaying a preview that highlights the advantages of your watch face can help people visualize its benefits. You can get a preview by using the iOS Watch app to email the watch face to yourself. The preview includes an illustrated device bezel that frames the face and is suitable for display on websites and in watchOS and iOS apps. Alternatively, you can replace the illustrated bezel with a high-fidelity hardware bezel that you can download from Apple Design Resources and composite onto the preview. For developer guidance, see Sharing an Apple Watch face.

Aim to offer shareable watch faces for all Apple Watch devices. Some watch faces are available on Series 4 and later — such as California, Chronograph Pro, Gradient, Infograph, Infograph Modular, Meridian, Modular Compact, and Solar Dial — and Explorer is available on Series 3 (with cellular) and later. If you use one of these faces in your configuration, consider offering a similar configuration using a face that’s available on Series 3 and earlier. To help people make a choice, you can clearly label each shareable watch face with the devices it supports.

Respond gracefully if people choose an incompatible watch face. The system sends your app an error when people try to use an incompatible watch face on Series 3 or earlier. In this scenario, consider immediately offering an alternative configuration that uses a compatible face instead of displaying an error. Along with the previews you provide, help people understand that they might receive an alternative watch face if they choose a face that isn’t compatible with their Apple Watch.


## Platform considerations
Not supported in iOS, iPadOS, macOS, tvOS, or visionOS.


## Resources

#### Related
Apple Design Resources — Product Bezels


#### Developer documentation
Sharing an Apple Watch face — ClockKit


---

### Widgets

**Source**: [https://developer.apple.com/design/human-interface-guidelines/widgets](https://developer.apple.com/design/human-interface-guidelines/widgets)


# Widgets
Widgets help people organize and personalize their devices by displaying timely, glanceable content and offering specific functionality. They appear in various contexts for a consistent experience across platforms. For example, a person might place a Weather widget:

- On the Home Screen and Lock Screen of their iPhone and iPad
- On the desktop and Notification Center of their Mac
- On a horizontal or vertical surface when they wear Apple Vision Pro
- At a fixed position in the Smart Stack of Apple Watch

## Anatomy
Widgets come in different sizes, ranging from small accessory widgets on iPhone, iPad, and Apple Watch to system family widgets that include an extra large size on iPad, Mac, and Apple Vision Pro. Additionally, widgets adapt their appearance to the context in which they appear and respond to a person’s device customization. Consider the following aspects when you design widgets:

- The widget size to support
- The context — devices and system experiences — in which the widget may appear
- The rendering modes and color treatment that the widget receives based on the size and context
The WidgetKit framework provides default appearances and treatments for each widget size to fit the system experience or device where it appears. However, it’s important to consider creating a custom widget design that can provide the best experience for your content in each specific context.


### System family widgets
System family widgets offer a broad range of sizes and may include one or more interactive elements.

- Small
- Medium
- Large
- Extra large
- Extra large portrait
The following table shows supported contexts for each system family widget size:

Home Screen, Today View, StandBy, and CarPlay

Home Screen, Today View, and Lock Screen

Desktop and Notification Center

Horizontal and vertical surfaces

Home Screen and Today View

System extra large portrait


### Accessory widgets
Accessory widgets display a very limited amount of information because of their size.

- Accessory circular
- Accessory corner
- Accessory inline
- Accessory rectangular
They appear on the following devices:

Watch complications and in the Smart Stack


### Appearances
A widget can appear in full-color, in monochrome with a tint color, or in a clear, translucent style. Depending on the location, device, and a person’s customization, the system may apply a tinted or clear appearance to the widget and its included full-color images, symbols, and glyphs.

For example, a small system widget appears differently depending on the device and location:

- On Apple Vision Pro, the widget appears as a 3D object, surrounded by a frame. It takes on a full-color appearance with a glass- or paper-like coating layer that responds to lighting conditions. Additionally, people can choose a tinted appearance that applies a color from a set of system-provided color palettes.
- On the Lock Screen of iPad, the widget takes on a monochromatic appearance without a tint color.
- On the Lock Screen of iPhone in StandBy, the widget appears scaled up in size with the background removed. When the ambient light falls below a threshold, the system renders the widget with a monochromatic red tint.
iPhone in StandBy during low-light conditions

Similarly, a rectangular accessory widget appears as follows:

- On the Lock Screen of iPhone and iPad, it takes on a monochromatic appearance without a tint color.
- On Apple Watch, the widget can appear as a watch complication in both full-color and tinted appearances, and it can also appear in the Smart Stack.
- iPhone Lock Screen
- Watch complication
- Smart Stack on Apple Watch
Each appearance described above includes a rendering mode that depends on the platform and a person’s appearance settings:

- The system uses the full color rendering mode for system family widgets across all platforms to display your widget in full color. It doesn’t change the color of your views.
The following table lists the occurrences for each rendering mode per platform:

Home Screen, Today view, StandBy and CarPlay (with the background removed)

Home Screen and Today view

Lock Screen, StandBy in low-light conditions

Smart Stack, complications

For additional design guidance, see Rendering modes. For developer guidance, see Preparing widgets for additional platforms, contexts, and appearances and WidgetRenderingMode.


## Best practices
Choose simple ideas that relate to your app’s main purpose. Include timely content and relevant functionality. For example, people who use the Weather app are often most interested in the current high and low temperatures and weather conditions, so the Weather widgets prioritize this information.

Aim to create a widget that gives people quick access to the content they want. People appreciate widgets that display meaningful content and offer useful actions and deep links to key areas of your app. Replicating an app icon offers little additional value, and people may be less likely to keep it on their screens.

Prefer dynamic information that changes throughout the day. If a widget’s content never appears to change, people may not keep it in a prominent position. Although widgets don’t update from minute to minute, it’s important to find ways to keep their content fresh to invite frequent viewing.

Look for opportunities to surprise and delight. For example, you might design a unique visual treatment for your calendar widget to display on meaningful occasions, like birthdays or holidays.

Offer widgets in multiple sizes when doing so adds value. Small widgets use their limited space to typically show a single piece of information while larger sizes support additional layers of information and actions. Avoid expanding a smaller widget’s content to simply fill a larger area. It’s more important to create one widget in the size that best represents the content than providing the widget in all sizes.

Balance information density. Sparse layouts can make the widget seem unnecessary, while overly dense layouts are less glanceable. Create a layout that provides essential information at a glance and allows people to view additional details by taking a longer look. If your layout is too dense, consider improving its clarity by using a larger widget size or replacing text with graphics.

Display only the information that’s directly related to the widget’s main purpose. In larger widgets, you can display more data — or more detailed visualizations of the data — but you don’t want to lose sight of the widget’s primary purpose. For example, all Calendar widgets display a person’s upcoming events. In each size, the widget remains centered on events while expanding the range of information as the size increases.

Use brand elements thoughtfully. Incorporate brand colors, typefaces, and stylized glyphs to make your widget recognizable but don’t overpower useful information or make your widget look out of place. When you include brand elements, people seldom need your logo or app icon to help them recognize your widget. If your widget benefits from including a small logo — for example, if your widget displays content from multiple sources — a small logo in the top-right corner is sufficient.

Choose between automatically displaying content and letting people customize displayed information. In some cases, people need to configure a widget to ensure it displays the information that’s most useful for them. For example, the Stocks widget lets people select the stocks they wish to track. In contrast, some widgets — like the Podcasts widget — automatically display recent content, so people don’t need to customize them. For developer guidance, see Making a configurable widget.

Avoid mirroring your widget’s appearance within your app. Including an element in your app that looks like your widget but doesn’t behave like it can confuse people. Additionally, people may be less likely to try other ways to interact with such an element in your app because they expect it to behave like a widget.

Let people know when authentication adds value. If your widget provides additional functionality when someone is signed in to your app, make sure people know that. For example, an app that shows upcoming reservations might include a message like “Sign in to view reservations” when people are signed out.


### Updating widget content
To remain relevant and useful, widgets periodically refresh their information but don’t support continuous, real-time updates, and the system may adjust the limits for updates depending on various factors.

Keep your widget up to date. Finding the appropriate update frequency for your widget depends on knowing how often the data changes and estimating when people need to see new data. For example, a widget that provides information about tidal conditions at a beach is useful if it updates on an hourly basis even though conditions change constantly. If people are likely to check your widget more frequently than you can update it, consider displaying text that describes when the data was last updated.

Use system functionality to refresh dates and times in your widget. Because widget update frequency is limited, let the system automatically refresh date and time information to preserve update opportunities. Determine the update frequency that fits with the data you display and show content quickly without hiding stale data behind placeholder content. For developer guidance about widget updates, see Keeping a widget up to date.

Use animated transitions to bring attention to data updates. By default, many SwiftUI views animate content updates. Additionally, use standard and custom animations with a duration of up to two seconds to let people know when new information is available or when content displays differently. For developer guidance, see Animating data updates in widgets and Live Activities.


### Adding interactivity
People tap or click a widget to launch its corresponding app. It can also include buttons and toggles to offer additional functionality without launching the app. For example, the Reminders widget includes toggles to mark a task as completed. When people interact with your widget in areas that aren’t buttons or toggles, the interaction launches your app.

Offer simple, relevant functionality and reserve complexity for your app. Useful widgets offer an easy way to complete a task or action that’s directly related to its content.

Ensure that a widget interaction opens your app at the right location. Deep link to details and actions that directly relate to the widget’s content, and don’t make people navigate to the relevant area in the app. For example, when people click or tap a medium Stocks widget, the Stocks app opens to a page that displays information about the symbol.

Offer interactivity while remaining glanceable and uncluttered. Multiple interaction targets — SwiftUI links, buttons, and toggles — might make sense for your content, but avoid creating app-like layouts in your widgets. Pay attention to the size of targets and make sure people can tap or click them with confidence and without accidentally performing unintended interactions. Note that inline accessory widgets offer only one tap target.


### Choosing margins and padding
Widgets scale to adapt to the screen sizes of different devices and onscreen areas. Supply content at appropriate sizes to make sure that your widget looks great on every device and let the system resize or scale it as necessary. In iOS, the system ensures that your widget looks good on small devices by resizing the content you design for large devices. In iPadOS, the system renders your widget at a large size before scaling it down for display on the Home Screen.

As you design for various devices and scale factors, use the values listed in Specifications and the Apple Design Resources for guidance; for your production widget, use SwiftUI to ensure flexibility.

In general, use standard margins to ensure legibility. Use the standard margin width for widgets — 16 points for most widgets — to avoid crowding their edges and creating a cluttered appearance. If you need to use tighter margins — for example, to create content groupings for graphics, buttons, or background shapes — setting margins of 11 points can work well. Additionally, note that widgets use smaller margins on the desktop on Mac and on the Lock Screen, including in StandBy. For developer guidance, see padding(_:_:).

Coordinate the corner radius of your content with the corner radius of the widget. To ensure that your content looks good within a widget’s rounded corners, use a SwiftUI container to apply the correct corner radius. For developer guidance, see ContainerRelativeShape.


### Displaying text in widgets
Prefer using the system font, text styles, and SF Symbols. Using the system font helps your widget look at home on any platform, while making it easier for you to display great-looking text in a variety of weights, styles, and sizes. Use SF Symbols to align and scale symbols with text that uses the system font. If you use a custom font, do so sparingly, and be sure it’s easy for people to read at a glance. It often works well to use a custom font for the large text in a widget and SF Pro for the smaller text. For guidance, see Typography and SF Symbols.

Avoid very small font sizes. In general, display text using fonts at 11 points or larger. Text in a font that’s smaller than 11 points can be too hard for many people to read.

Avoid rasterizing text. Always use text elements and styles to ensure that your text scales well and to allow VoiceOver to speak your content.

In iOS, iPadOS, and visionOS, widgets support Dynamic Type sizes from Large to AX5 when you use Font to choose a system font or custom(_:size:) to choose a custom font. For more information about Dynamic Type sizes, see Supporting Dynamic Type.


### Using color
Use color to enhance a widget’s appearance without competing with its content. Beautiful colors draw the eye, but they’re best when they don’t prevent people from absorbing a widget’s information at a glance. In your asset catalog, you can also specify the colors you want the system to use as it generates your widget’s editing-mode user interface.

Convey meaning without relying on specific colors to represent information. Widgets can appear monochromatic (with or without a custom tint color), and in watchOS, the system may invert colors depending on the watch face a person chooses. Use text and iconography in addition to color to express meaning.

Use full-color images judiciously. When a person chooses a tinted or clear appearance for their widgets, the system by default desaturates full-color images. You can choose to render images in full-color, even when a person chooses a tinted or clear widget appearance. However, full-color images in these appearances draw special attention to the widget, which might make it feel as if the widget doesn’t belong to the platform. For example, a full-color image in a widget might appear out of place when a person chooses a clear widget appearance. Consider reserving full-color images to represent media content, such as album art for a music app’s widget, and use full-color images with smaller dimensions than the size of the widget.


## Rendering modes

### Full-color
Support light and dark appearances. Prefer light backgrounds for the light appearance and dark backgrounds for the dark appearance, and consider using the semantic system colors for text and backgrounds to let the colors dynamically adapt to the current appearance. You can also support different appearances by putting color variants in your asset catalog. For guidance, see Dark Mode; for developer guidance, see Asset management and Supporting Dark Mode in your interface.


### Accented
Group widget components into an accented and a primary group. The accented rendering mode divides the widget’s view hierarchy into an accent group and a primary group. On iPhone, iPad, and Mac, the system tints primary and accented content white. On Apple Watch, the system tints primary content white and accented content in the color of the watch face.

For developer guidance, see widgetAccentable(_:) and Optimizing your widget for accented rendering mode and Liquid Glass.


### Vibrant
Offer enough contrast to ensure legibility. In the vibrant rendering mode, the opacity of pixels within an image determines the strength of the blurred background material effect. Fully transparent pixels let the background material pass through as is. The brightness of pixels determines how vibrant they appear on the Lock Screen. Brighter gray values provide more contrast, and darker values provide less contrast.

Create optimized assets for the best vibrant effect. Render content like images, numbers, and text at full opacity. Use white or light gray for the most prominent content and darker grayscale values for secondary elements to establish hierarchy. Confirm that image content has sufficient contrast in grayscale, and use opaque grayscale values, rather than opacities of white, to achieve the best vibrant material effect.


## Previews and placeholders
Design a realistic preview to display in the widget gallery. Highlighting your widget’s capabilities — and clearly representing the experiences each widget type or size can provide — helps people make an informed decision. You can display real data in your widget preview, but if the data takes too long to generate or load, display realistic simulated data instead.

Design placeholder content that helps people recognize your widget. An installed widget displays placeholder content while its data loads. Create an effective placeholder appearance by combining static interface components with semi-opaque shapes that stand in for dynamic content. For example, use rectangles of different widths to suggest lines of text, and circles or squares in place of glyphs and images.

Write a succinct widget description. The widget gallery displays descriptions that help people understand what each widget does. Begin a description with an action verb — for example, “See the current weather conditions and forecast for a location” or “Keep track of your upcoming events and meetings.” Avoid including unnecessary phrases that reference the widget itself, like “This widget shows…,” “Use this widget to…,” or “Add this widget.” Use approachable language and sentence-style capitalization.

Group your widget’s sizes together, and provide a single description. If your widget is available in multiple sizes, group them together so people don’t think each size is a different widget. Provide a single description of your widget — regardless of how many sizes you offer — to avoid repetition and to help people understand how each size provides a slightly different perspective on the same content and functionality.

Consider coloring the Add button. After people choose your app in the widget gallery, an Add button appears below the group of widgets you offer. You can specify a color for this button to help remind people of your brand.


## Platform considerations
No additional considerations for macOS. Not supported in tvOS.


### iOS, iPadOS
Widgets on the Lock Screen are functionally similar to watch complications and follow design principles for Complications in addition to design principles for widgets. Provide useful information in your Lock Screen widget, and don’t treat it only as an additional way for people to launch into your app. In many cases, a design for complications also works well for widgets on the Lock Screen (and vice versa), so consider creating them in tandem.

Your app can offer widgets on the Lock Screen in three different shapes: as inline text that appears above the clock, and as circular and rectangular shapes that appear below the clock.

Support the Always-On display on iPhone. Devices with the Always-On display render widgets on the Lock Screen with reduced luminance. Use levels of gray that provide enough contrast in the Always-On display, and make sure your content remains legible.

For developer guidance, see Creating accessory widgets and watch complications.

Offer Live Activities to show real-time updates. Widgets don’t show real-time information. If your app allows people to track the progress of a task or event for a limited amount of time with frequent updates, consider offering Live Activities. Widgets and Live Activities use the same underlying frameworks and share design similarities. As a result, it can be a good idea to develop widgets and Live Activities in tandem and reuse code and design components for both features. For design guidance on Live Activities, see Live Activities; for developer guidance, see ActivityKit.


#### StandBy and CarPlay
On iPhone in StandBy, the system displays two small system family widgets side-by-side, scaled up so they fill the Lock Screen. By supporting StandBy, you also ensure your widgets work well in CarPlay. CarPlay and StandBy widgets both use the small system family widget with the background removed and scaled up to best fit the grid on the Widgets screen. Glanceable information and large text are especially important in CarPlay to make your widget easy to read on a car’s display.

Limit usage of rich images or color to convey meaning in StandBy. Instead, make use of the additional space by scaling up and rearranging text so people can glance at the widget content from a greater distance. To seamlessly blend with the black background, don’t use background colors for your widget when it appears in StandBy.

- Correct usage
- Incorrect usage
For developer guidance, see Displaying the right widget background.

On iPhone in StandBy in low-light conditions, the system renders widgets in a monochromatic look with a red tint.

iPhone in low-light conditions


### visionOS
Widgets in visionOS are 3D objects that people place on a horizontal or vertical surface. When a person places a widget on a surface, the widget persists in that location even when the person turns Apple Vision Pro off and back on. Widgets have a consistent, real-world scale. Their size, mounting style, and treatment style impact how a person perceives them.

visionOS widgets appear in full-color by default, but they appear in the accented rendering mode when people personalize them with tint colors using a range of system-provided color palettes. Additionally, people can customize the frame width of widgets that use the elevated mounting style, and custom options that are unique to the widget. For example, visionOS doesn’t provide systemwide light or dark appearances. However, the Music poster widget offers its own customization option that lets people choose between a light and a dark theme that the app generates from the displayed album art.

For developer guidance, see Updating your widgets for visionOS.

Adapt your design and content for the spatial experience Apple Vision Pro provides. In visionOS, widgets don’t float in isolation but are part of living rooms, kitchens, offices, and more. Consider this context early and think of widgets as part of someone’s surroundings when you bring your existing widgets to visionOS or design them from scratch. For example, the Music widget adapts to a poster-like appearance that’s glanceable across the room with large typography and a high-resolution image, and a productivity app might offer a small widget that easily fits on a desk.

Test your widgets across the full range of system color palettes and in different lighting conditions. Make sure your widget’s tone, contrast, and legibility remain consistent and intentional. If you choose to exclude UI elements from tinting, test your widget in every provided tint color palette to make sure the untinted elements remain legible when a person customizes their widgets with tint colors.


#### Thresholds and sizes
Widgets on Apple Vision Pro can adapt based on a person’s proximity, and visionOS provides widgets with two key thresholds to design for: the simplified threshold for when a person views a widget at a distance, and the default threshold when a person views it nearby.

Viewed from a distance

Because widgets can appear throughout a person’s environment, it’s also important to match a widget’s size to the type of content it contains, and to be aware of how it appears at a variety of distances.

Design a responsive layout that shows the right level of detail for each of the two thresholds. When a person views the widget at a distance, display a simplified version of your widget that shows fewer details and has a larger type size, and remove interactive elements like buttons or toggles. When a person views the widget from nearby, show more details and use a smaller type size. To create a smooth and consistent experience and help your layout feel continuous, maintain shared elements across both distance thresholds.

Offer widget family sizes that fit a person’s surroundings well. Widgets map to real-world dimensions and have a permanent presence in a person’s spatial environment. Think about where people might place your widget — mounted to a wall, placed on a sideboard, or sitting next to a workplace — and choose a widget family size that’s right for that context. For example, offer a small system widget with content that people might place on a desk or an extra large widget to let people decorate their surroundings with something visually rich, like artwork or photography.

Display content in a way that remains legible from a range of distances. To make a widget feel intentional and proportionate to where they place it, people can scale a widget from 75 to 125 percent in size. Use print design principles like clear hierarchy, strong typography, and scale to make sure your content remains glanceable. Include high-resolution assets that look good scaled up to every size.


#### Mounting styles
The way a widget appears on a surface plays a big role in how a person perceives it. To make it feel intentional and integrated into their surroundings, people place a widget on surfaces in distinct mounting styles.

- Recessed style. On vertical surfaces — for example, on a wall — the widget can appear recessed, with content set back into the surface, creating a depth effect that gives the illusion of a cutout in the surface. Horizontal surfaces don’t use the recessed mounting style.
By default, widgets use the elevated mounting style, because it works for horizontal and vertical surfaces.

Choose the mounting style that fits your content and the experience you want to create. By default, visionOS widgets use the elevated mounting style, which is ideal for content that you want to stand out and feel present, like reminders, media, or glanceable data. Recessed widgets are ideal for immersive or ambient content, like weather or editorial content, and people can only place them on a vertical surface. If a style doesn’t suit your widget, you can opt out of it for each widget. If you choose to only support the recessed mounting style, people can’t place the widget on a horizontal surface. For example, a weather app might only support the recessed mounting style to give the illusion of looking out of a window for its large and extra-large system family widgets, and only support the elevated style for its small system family widget.

Use the supportedMountingStyles(_:) property of your WidgetConfiguration to declare supported mounting styles — elevated, recessed, or both — for all widgets included in the configuration. To offer a widget that only supports one mounting style and other widgets that support both mounting styles, create separate widget configurations. For example, create one widget configuration for the widget that only supports the recessed mounting style, and a second configuration for the widgets that support both mounting styles.

Test your elevated widget designs with each system-provided frame width. People can choose from different system-defined frame widths for widgets that use the elevated mounting style. You can’t change your layout based on the frame width a person chooses, so make sure your widget layout stays visually balanced for each frame width.


#### Treatment styles
In addition to size and mounting style, the system applies one of two treatment styles to visionOS widgets. Choosing the right treatment for your widget helps reinforce the experience you want to create.

- The paper style creates a more grounded, print-like style that feels solid and makes the widget feel like part of its surroundings. When lighting conditions change, widgets in the paper style become darker or lighter in response.
- The glass style creates a lighter, layered look that adds depth and visual separation between foreground and background elements to emphasize clarity and contrast. The foreground elements always stay bright and legible, and don’t dim or brighten, even as ambient light changes.
Choose the paper style for a print-like look that feels more like a real object in the room. The entire widget responds to the ambient lighting and blends naturally into its surroundings. For example, the Music poster widget uses the paper style to display albums and playlists like framed artwork on a wall.

Choose the glass style for information-rich widgets. Glass visually separates foreground and background elements, allowing you to decide which parts of your interface adapt to the surroundings and which stay visually consistent. Foreground elements appear in full color, unaffected by ambient lighting, to make sure important content stays sharp and legible. For example, a News widget appears with editorial images in the background with a soft, print-like look. Its headlines stay in the foreground, crisp and easy to read.


### watchOS
Provide a colorful background that conveys meaning. By default, widgets in the Smart Stack use a black background. Consider using a custom background color that provides additional meaning. For example, the Stocks app uses a red background for falling stock values and a green background if a stock’s value rises.

Encourage the system to display or elevate the position of your watchOS widget in the Smart Stack. Relevancy information helps the system show your widget when people need it most. Relevance can be location-based or specific to ongoing system actions, like a workout. For developer guidance, see RelevanceKit.


## Specifications
As you design your widgets, use the following values for guidance.


### iOS dimensions
Screen size (portrait, pt)


### iPadOS dimensions
* When Display Zoom is set to More Space.


### visionOS dimensions
Size in mm (scaled to 100%)


### watchOS dimensions
Size of a widget in the Smart Stack (pt)


## Resources

#### Related

#### Developer documentation
Developing a WidgetKit strategy — WidgetKit


#### Videos

## Change log
Updated guidance for all platforms, and added guidance for visionOS and CarPlay.

Corrected watchOS widget dimensions.

Updated to include guidance for accented widgets in iOS 18 and iPadOS 18.

Updated guidance to include widgets in watchOS, widgets on the iPad Lock Screen, and updates for iOS 17, iPadOS 17, and macOS 14.

Added guidance for widgets on the iPhone Lock Screen and updated design comprehensives for iPhone 14, iPhone 14 Pro, and iPhone 14 Pro Max.


---
