# VitaHealth Forum - CSS Snippets & Components

Quick reference library for common UI patterns and reusable CSS snippets.

## Button Variants

### Primary Action Button
```css
-fx-background-color: #1e88e5;
-fx-text-fill: white;
-fx-font-size: 14px;
-fx-font-weight: bold;
-fx-padding: 12px 20px;
-fx-background-radius: 8px;
-fx-cursor: hand;
```

### Secondary Button
```css
-fx-background-color: #e4e6eb;
-fx-text-fill: #050505;
-fx-font-size: 13px;
-fx-padding: 8px 16px;
-fx-background-radius: 8px;
-fx-cursor: hand;
```

### Danger Button (Delete)
```css
-fx-background-color: #fee2e2;
-fx-text-fill: #dc2626;
-fx-font-size: 13px;
-fx-font-weight: bold;
-fx-padding: 8px 16px;
-fx-background-radius: 8px;
-fx-cursor: hand;
```

### Icon Button
```css
-fx-background-color: #f0f2f5;
-fx-background-radius: 20px;
-fx-min-width: 40px;
-fx-min-height: 40px;
-fx-font-size: 16px;
-fx-cursor: hand;
```

### Icon Button Hover
```css
-fx-background-color: #e4e6eb;
```

## Card Components

### Standard Card
```css
-fx-background-color: #ffffff;
-fx-padding: 20px;
-fx-background-radius: 12px;
-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);
```

### Card with Hover Effect
```css
-fx-background-color: #ffffff;
-fx-padding: 20px;
-fx-background-radius: 12px;
-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);

:hover {
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);
}
```

### Alert Card (Info)
```css
-fx-background-color: #e3f2fd;
-fx-padding: 15px;
-fx-background-radius: 12px;
-fx-border-color: #1e88e5;
-fx-border-width: 1px;
```

### Alert Card (Warning)
```css
-fx-background-color: #fff3cd;
-fx-padding: 15px;
-fx-background-radius: 12px;
-fx-border-color: #f39c12;
-fx-border-width: 1px;
```

### Alert Card (Error)
```css
-fx-background-color: #fee2e2;
-fx-padding: 15px;
-fx-background-radius: 12px;
-fx-border-color: #dc2626;
-fx-border-width: 1px;
```

### Alert Card (Success)
```css
-fx-background-color: #e8f5e9;
-fx-padding: 15px;
-fx-background-radius: 12px;
-fx-border-color: #27ae60;
-fx-border-width: 1px;
```

## Navigation & Sidebar

### Navigation Button (Inactive)
```css
-fx-background-color: transparent;
-fx-text-fill: #65676b;
-fx-font-size: 14px;
-fx-padding: 10px 15px;
-fx-alignment: left;
-fx-cursor: hand;
-fx-background-radius: 8px;
```

### Navigation Button (Hover)
```css
-fx-background-color: #e4e6eb;
```

### Navigation Button (Active)
```css
-fx-background-color: #e3f2fd;
-fx-text-fill: #1e88e5;
-fx-font-weight: bold;
```

### Sidebar Container
```css
-fx-background-color: #ffffff;
-fx-border-color: #e4e6eb;
-fx-border-width: 0 1 0 0;
```

## Form Components

### Text Input
```css
-fx-background-color: #ffffff;
-fx-border-color: #e4e6eb;
-fx-border-width: 1px;
-fx-border-radius: 6px;
-fx-padding: 8px 12px;
-fx-font-size: 13px;
```

### Text Input (Focused)
```css
-fx-border-color: #1e88e5;
-fx-border-width: 2px;
-fx-background-color: #ffffff;
```

### Text Area
```css
-fx-background-color: #ffffff;
-fx-border-color: #e4e6eb;
-fx-border-width: 1px;
-fx-border-radius: 6px;
-fx-padding: 10px 12px;
-fx-font-size: 13px;
-fx-control-inner-background: #ffffff;
```

### ComboBox
```css
-fx-background-color: #ffffff;
-fx-border-color: #e4e6eb;
-fx-border-width: 1px;
-fx-border-radius: 6px;
-fx-padding: 8px 12px;
-fx-font-size: 13px;
```

### Search Field
```css
-fx-background-color: #f0f2f5;
-fx-background-radius: 20px;
-fx-padding: 10px 15px;
-fx-pref-height: 40px;
-fx-font-size: 14px;
```

### Search Field (Focused)
```css
-fx-background-color: #ffffff;
-fx-border-color: #1e88e5;
-fx-border-width: 2px;
-fx-border-radius: 20px;
```

## Layout Components

### Top Navigation Bar
```xml
<HBox alignment="CENTER_LEFT" spacing="20.0" 
      style="-fx-background-color: #ffffff; 
              -fx-padding: 12 25; 
              -fx-border-color: #e4e6eb; 
              -fx-border-width: 0 0 1 0;">
```

### Main Content Padding
```xml
style="-fx-padding: 20 25;"
```

### Section Separator
```css
-fx-border-color: #e4e6eb;
-fx-padding: 0;
```

### Vertical Divider
```xml
<Separator orientation="VERTICAL" style="-fx-padding: 0 5;"/>
```

### Horizontal Divider
```xml
<Separator style="-fx-padding: 15 0;"/>
```

## Avatar & Circles

### User Avatar (Gradient Background)
```xml
<Circle radius="24.0" style="-fx-fill: linear-gradient(#667eea, #764ba2);"/>
```

### Avatar Size Guide
- Small: 18px radius
- Medium: 24px radius
- Large: 32-40px radius

### Gradient Presets
- Purple: `linear-gradient(#667eea, #764ba2)`
- Rainbow: `linear-gradient(#f09433, #e6683c, #dc2743, #cc2366, #bc1888)`
- Blue: `linear-gradient(#0066ff, #0099ff)`
- Green: `linear-gradient(#27ae60, #2ecc71)`

## Text & Typography

### Large Heading
```xml
<Label text="Title" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #050505;"/>
```

### Medium Heading
```xml
<Label text="Subtitle" style="-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #050505;"/>
```

### Small Heading
```xml
<Label text="Section" style="-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #050505;"/>
```

### Body Text
```xml
<Label text="Content" style="-fx-font-size: 13px; -fx-text-fill: #050505;"/>
```

### Meta Text
```xml
<Label text="Meta" style="-fx-font-size: 12px; -fx-text-fill: #65676b;"/>
```

### Caption Text
```xml
<Label text="Caption" style="-fx-font-size: 11px; -fx-text-fill: #90949c;"/>
```

## Stats Cards

### Single Stat Card
```xml
<VBox style="-fx-background-color: #ffffff; 
            -fx-padding: 15; 
            -fx-background-radius: 10; 
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);" 
      HBox.hgrow="ALWAYS">
    <Label text="LABEL" style="-fx-font-size: 11px; -fx-text-fill: #90949c;"/>
    <Label text="123" style="-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #050505;"/>
    <Label text="Subtitle" style="-fx-font-size: 12px; -fx-text-fill: #27ae60;"/>
</VBox>
```

## List Components

### List Item Container
```xml
<HBox spacing="10.0" alignment="TOP_LEFT" 
      style="-fx-padding: 10; 
              -fx-background-color: #f8fafc; 
              -fx-background-radius: 8;">
```

### List Item Hover
```css
-fx-background-color: #e4e6eb;
-fx-padding: 10;
-fx-background-radius: 8;
```

## Table Styling

### Table Row
```css
-fx-padding: 0;
-fx-cell-size: 45px;
-fx-border-color: #e4e6eb;
-fx-border-width: 0 0 1 0;
```

### Table Row Hover
```css
-fx-background-color: #f8fafc;
```

### Table Header
```css
-fx-background-color: #f8fafc;
-fx-border-color: #e4e6eb;
-fx-border-width: 0 0 1 0;
-fx-font-weight: bold;
-fx-font-size: 12px;
-fx-text-fill: #050505;
-fx-padding: 12 8;
```

## Spacing Guide

### Common Values
- `4px` - Extra small spacing
- `8px` - Small spacing between elements
- `12px` - Medium spacing (default element gaps)
- `15px` - Card padding
- `20px` - Section padding
- `25px` - Horizontal page margins
- `32px` - Large spacing between sections

### Padding Shorthand (FXML)
- `5` = 5px all sides
- `10 20` = 10px vertical, 20px horizontal
- `12 20 12 20` = top, right, bottom, left

## Color Quick Reference

### Use in FXML Inline Styles
```xml
<!-- Text Colors -->
style="-fx-text-fill: #050505;"      <!-- Primary text -->
style="-fx-text-fill: #65676b;"      <!-- Secondary text -->
style="-fx-text-fill: #90949c;"      <!-- Tertiary text -->
style="-fx-text-fill: #1e88e5;"      <!-- Link/Primary color -->

<!-- Background Colors -->
style="-fx-background-color: #ffffff;" <!-- Card background -->
style="-fx-background-color: #f0f2f5;" <!-- Page background -->
style="-fx-background-color: #e4e6eb;" <!-- Hover state -->

<!-- Status Colors -->
style="-fx-text-fill: #27ae60;"      <!-- Success -->
style="-fx-text-fill: #f39c12;"      <!-- Warning -->
style="-fx-text-fill: #dc2626;"      <!-- Error -->
```

## Common Patterns

### Centered Container
```xml
<HBox alignment="CENTER" style="-fx-padding: 20;">
    <VBox alignment="CENTER" style="-fx-padding: 20;">
        <!-- Content -->
    </VBox>
</HBox>
```

### Right-Aligned Controls
```xml
<HBox alignment="CENTER_RIGHT" spacing="10.0">
    <Button text="Cancel" styleClass="secondary-button"/>
    <Button text="Save" styleClass="primary-action-button"/>
</HBox>
```

### Expandable Container
```xml
<VBox VBox.vgrow="ALWAYS" spacing="15.0">
    <!-- Content -->
</VBox>
```

### Growing Width Container
```xml
<HBox HBox.hgrow="ALWAYS" spacing="15.0">
    <!-- Content -->
</HBox>
```

## Interactive Element States

### Button States
```
Default → Hover → Pressed → Disabled
```

### Input States
```
Default → Focused → Filled → Error/Success
```

### Navigation States
```
Default (inactive) → Hover → Active → Current Page
```

---

**Usage**: Copy-paste these snippets into FXML or CSS files as needed. Always maintain consistency with established patterns.

**Last Updated**: May 7, 2026
**Version**: 1.0

