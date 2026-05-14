# VitaHealth Forum - Design & Style Guide

## Quick Start for Developers

This guide outlines the UI/UX standards and design patterns used in the VitaHealth Forum interface.

## 1. Color System

### Primary Colors
```
Brand Primary Blue: #1e88e5
  - Used for: Primary actions, links, active states
  - Usage: Important buttons, selected navigation items
  
Brand Hover Blue: #1565c0
  - Used for: Hover states of primary buttons
  - Usage: Interactive feedback
```

### Neutral Colors
```
Text Primary: #050505
  - Used for: Main content text, headlines
  - Contrast ratio: 21:1 with white backgrounds
  
Text Secondary: #65676b
  - Used for: Meta information, descriptions
  - Contrast ratio: 8.5:1 with white backgrounds
  
Text Tertiary: #90949c
  - Used for: Labels, captions
  - Contrast ratio: 6.5:1 with white backgrounds

Background Primary: #f0f2f5
  - Used for: Page backgrounds
  
Background Secondary: #ffffff
  - Used for: Cards, containers
  
Border Color: #e4e6eb
  - Used for: Dividers, input borders
```

### Status Colors
```
Success: #27ae60 (Green)
  - Usage: Successful operations, approved states
  
Warning: #f39c12 (Orange)
  - Usage: Warnings, pending states
  
Error/Danger: #dc2626 (Red)
  - Usage: Errors, deletions, critical states
  
Info: #1e88e5 (Blue)
  - Usage: Information alerts, featured content
```

## 2. Typography System

### Font Family
```
Primary: 'Segoe UI', 'Helvetica Neue', Arial
Fallback: system-ui, sans-serif
```

### Type Scales

**Display (Extra Large)**
- Size: 24px
- Weight: Bold (700)
- Usage: Page titles, main headings

**Heading 1**
- Size: 20px
- Weight: Bold (700)
- Usage: Section titles
- Line Height: 1.4

**Heading 2**
- Size: 16px
- Weight: Bold (700)
- Usage: Subsection titles
- Line Height: 1.4

**Body**
- Size: 13-14px
- Weight: Regular (400)
- Usage: Main content, descriptions
- Line Height: 1.5

**Small**
- Size: 12px
- Weight: Regular (400)
- Usage: Meta information, labels
- Line Height: 1.4

**Extra Small**
- Size: 11px
- Weight: Bold (700)
- Usage: Section labels, captions
- Color: #90949c
- Text Transform: UPPERCASE

## 3. Spacing System

### Base Unit: 4px

**Common Spacing Values**
```
xs: 4px
sm: 8px
md: 12px
lg: 16px
xl: 20px
2xl: 24px
3xl: 32px
```

### Component Spacing
```
Card Padding: 20px
Section Padding: 20-25px
Input Padding: 8px (vertical) 12px (horizontal)
Button Padding: 10-12px (vertical) 20-30px (horizontal)
```

## 4. Corner Radius

```
Small: 4px (used for inputs, subtle elements)
Medium: 8px (used for buttons, small cards)
Large: 12px (used for cards, major components)
XL: 20px (used for avatar backgrounds)
```

## 5. Shadow System

### Drop Shadow (Standard)
```
Gaussian blur, rgba(0,0,0,0.06), radius 6, offset 0,1
Used for: Cards, elevated content
CSS: -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);
```

### Drop Shadow (Elevated)
```
Gaussian blur, rgba(0,0,0,0.1), radius 8, offset 0,2
Used for: Dropped-down menus, dialogs
CSS: -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);
```

## 6. Component Styles

### Buttons

#### Primary Action Button
```xml
<Button text="Action" styleClass="primary-action-button"/>
```
- Background: #1e88e5
- Text: White
- Padding: 12px 20px
- Border Radius: 8px
- Font Size: 14px, Bold

#### Secondary Button
```xml
<Button text="Action" styleClass="secondary-button"/>
```
- Background: #e4e6eb
- Text: #050505
- Padding: 8px 16px
- Border Radius: 8px
- Font Size: 13px

#### Interaction Button
```xml
<Button text="Like" styleClass="interaction-button"/>
```
- Background: transparent
- Text: #65676b
- Padding: 8px 12px
- Border Radius: 8px
- Font Size: 13px
- Hover: Background #f0f2f5

### Form Fields

#### Text Input
```xml
<TextField promptText="Enter text..." style="-fx-"/>
```
- Background: #ffffff
- Border: 1px solid #e4e6eb
- Padding: 8px 12px
- Border Radius: 6px
- Focus State: 2px solid #1e88e5

#### Text Area
```xml
<TextArea promptText="Enter text..." style="-fx-"/>
```
- Background: #ffffff
- Border: 1px solid #e4e6eb
- Padding: 10px 12px
- Border Radius: 6px
- Focus State: 2px solid #1e88e5

### Navigation

#### Nav Button (Inactive)
```xml
<Button text="Item" styleClass="nav-button"/>
```
- Background: transparent
- Text: #65676b
- Padding: 10px 15px
- Hover: Background #e4e6eb

#### Nav Button (Active)
```xml
<Button text="Item" styleClass="nav-button, active-nav"/>
```
- Background: #e3f2fd
- Text: #1e88e5
- Font Weight: bold

## 7. Layout Patterns

### TopBar
```xml
<HBox alignment="CENTER_LEFT" spacing="20.0" 
      style="-fx-background-color: #ffffff; 
              -fx-padding: 12 25; 
              -fx-border-color: #e4e6eb; 
              -fx-border-width: 0 0 1 0;">
  <!-- Content -->
</HBox>
```

### Card Container
```xml
<VBox style="-fx-background-color: #ffffff; 
            -fx-padding: 20; 
            -fx-background-radius: 12; 
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);">
  <!-- Content -->
</VBox>
```

### Sidebar
```xml
<VBox prefWidth="280.0" 
      style="-fx-background-color: #ffffff; 
              -fx-border-color: #e4e6eb; 
              -fx-border-width: 0 1 0 0;" 
      spacing="5.0">
  <!-- Navigation items -->
</VBox>
```

## 8. FXML Best Practices

### Always Use StyleClasses When Available
```xml
<!-- Good -->
<Button text="Action" styleClass="primary-action-button"/>

<!-- Avoid inline styles when possible -->
<Button text="Action" style="-fx-background-color: #1e88e5; ..."/>
```

### Structure for Consistency
```xml
1. Top navigation/header
2. Left sidebar (if applicable)
3. Main content area
4. Right sidebar (if applicable)
5. Bottom section (if applicable)
```

### Import Organization
```xml
<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.shape.Circle?>
```

## 9. Responsive Design Considerations

### Sidebar Width
- Desktop: 280px
- Tablet: 240px
- Mobile: Collapsed/Hidden

### Main Content Padding
- Desktop: 20-25px
- Tablet: 15px
- Mobile: 10px

### Card Width
- Desktop: Auto (responsive)
- Tablet: Single column with padding
- Mobile: Full width with safe area

## 10. Accessibility Standards

### Color Contrast
- Primary text: 21:1 ratio (AAA)
- Secondary text: 8.5:1 ratio (AA)
- UI Components: 4.5:1 ratio minimum

### Focus States
```css
:focus {
    -fx-focus-color: rgba(30, 136, 229, 0.3);
    -fx-faint-focus-color: rgba(30, 136, 229, 0.1);
}
```

### Size Targets
- Minimum touch target: 44x44px
- Button minimum padding: 10px vertical
- Line height: 1.4-1.5 for readability

## 11. Common Component Examples

### Stats Card Row
```xml
<HBox spacing="15.0">
    <VBox style="-fx-background-color: #ffffff; 
                  -fx-padding: 15; 
                  -fx-background-radius: 10; 
                  -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);" 
          HBox.hgrow="ALWAYS">
        <Label text="LABEL" style="-fx-font-size: 11px; -fx-text-fill: #90949c;"/>
        <Label text="123" style="-fx-font-size: 24px; -fx-font-weight: bold;"/>
    </VBox>
    <!-- More cards -->
</HBox>
```

### Post Input Card
```xml
<VBox spacing="12.0" 
      style="-fx-background-color: #ffffff; 
              -fx-padding: 20; 
              -fx-background-radius: 12; 
              -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);">
    <HBox spacing="12.0" alignment="TOP_LEFT">
        <Circle radius="24.0" style="-fx-fill: linear-gradient(#667eea, #764ba2);"/>
        <VBox HBox.hgrow="ALWAYS">
            <!-- Input fields -->
        </VBox>
    </HBox>
</VBox>
```

## 12. Theme Implementation

All styling is defined in:
- `src/main/resources/css/community-feed.css`

CSS classes available:
- `.primary-action-button`
- `.secondary-button`
- `.interaction-button`
- `.nav-button`
- `.active-nav`
- `.search-field`
- `.icon-button`
- `.card`
- `.post-card`
- `.story-item`
- And many more...

---

**Last Updated**: May 7, 2026
**Version**: 1.0
**Status**: Active

