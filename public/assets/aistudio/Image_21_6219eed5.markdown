---
name: Nocturne & Quill
colors:
  surface: '#0d1226'
  surface-dim: '#0d1226'
  surface-bright: '#33384e'
  surface-container-lowest: '#080d21'
  surface-container-low: '#161a2f'
  surface-container: '#1a1f33'
  surface-container-high: '#24293e'
  surface-container-highest: '#2f3449'
  on-surface: '#dde1fd'
  on-surface-variant: '#cac4d0'
  inverse-surface: '#dde1fd'
  inverse-on-surface: '#2b2f45'
  outline: '#948f99'
  outline-variant: '#49454f'
  surface-tint: '#cebefa'
  primary: '#cebefa'
  on-primary: '#35285a'
  primary-container: '#9d8ec7'
  on-primary-container: '#332658'
  inverse-primary: '#64568b'
  secondary: '#e6c27c'
  on-secondary: '#402d00'
  secondary-container: '#5e4509'
  on-secondary-container: '#d7b46f'
  tertiary: '#c9c6c0'
  on-tertiary: '#31302c'
  tertiary-container: '#999690'
  on-tertiary-container: '#302f2a'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e8ddff'
  primary-fixed-dim: '#cebefa'
  on-primary-fixed: '#1f1243'
  on-primary-fixed-variant: '#4c3f72'
  secondary-fixed: '#ffdea2'
  secondary-fixed-dim: '#e6c27c'
  on-secondary-fixed: '#261900'
  on-secondary-fixed-variant: '#5b4307'
  tertiary-fixed: '#e6e2db'
  tertiary-fixed-dim: '#c9c6c0'
  on-tertiary-fixed: '#1c1c18'
  on-tertiary-fixed-variant: '#484742'
  background: '#0d1226'
  on-background: '#dde1fd'
  surface-variant: '#2f3449'
typography:
  display-lg:
    fontFamily: EB Garamond
    fontSize: 48px
    fontWeight: '400'
    lineHeight: 56px
  display-lg-mobile:
    fontFamily: EB Garamond
    fontSize: 36px
    fontWeight: '400'
    lineHeight: 44px
  headline-lg:
    fontFamily: EB Garamond
    fontSize: 32px
    fontWeight: '500'
    lineHeight: 40px
  headline-md:
    fontFamily: EB Garamond
    fontSize: 26px
    fontWeight: '500'
    lineHeight: 34px
  headline-sm:
    fontFamily: EB Garamond
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 30px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 30px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 18px
  code-md:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  timer-lg:
    fontFamily: JetBrains Mono
    fontSize: 36px
    fontWeight: '500'
    lineHeight: 44px
  timer-lg-mobile:
    fontFamily: JetBrains Mono
    fontSize: 28px
    fontWeight: '500'
    lineHeight: 36px
  meta-sm:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 1rem
  margin: 3rem
  margin-mobile: 1.25rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

This design system channels the warmth of an independent, rain-streaked book café tucked inside the turret library of an ancient, moonlit citadel. It is crafted for solitary thinkers, late-night writers, and slow-paced scholars seeking sanctuary from modern algorithmic noise. 

The aesthetic is **Enchanted Editorial & Tactile Midnight**: combining deep astrological gloom with warm candlelight, physical paper tones, and refined vintage typography. Interfaces feel like tangible stationery layered on aged stone desks rather than cold software panels.

Key Brand Pillars:
- **Atmospheric Solitude:** Deep celestial blues and indigo shadows instill calm, deliberate pacing.
- **Warmth Within Stone:** Champagne gold and parchment cream provide welcoming pockets of hearth-like illumination.
- **Archival Precision:** Literary editorial contrast paired with crisp monospaced tracking for timeless focus.

## Colors

The palette is tuned specifically for sustained nocturnal immersion and offline focus. It relies on ambient midnight darkness accented by soft stellar illumination and aged gold highlights.

- **Primary Accent (`#9D8EC7`, deeper `#7A6BA3`):** Muted Lavender represents magical resonance, focus indicators, active states, and quiet progress markers.
- **Secondary Highlight (`#E5C17B`, lighter `#F2D69B`):** Champagne Gold provides candlelight warmth, marking key achievements, bookmarks, high-priority interactive cues, and celestial accents.
- **Base Canvas (`#12172B`):** Midnight Blue provides the foundational void—darker than common dark modes, reducing eye strain during late-night study.
- **Elevated Canvas (`#1B233E`):** Secondary surface representing carved masonry, card surfaces, and reading panes.
- **Dividers & Outlines (`#253052`):** Soft astronomic hairline borders mimicking brass instrument engravings and bookbinding seams.
- **Editorial Typography (`#FBF7F0`, muted `#F2EBE0`):** Cream tones reminiscent of heavy archival parchment, eliminating the harsh contrast of stark white.

## Typography

The typographic hierarchy establishes a dialogue between historical scholarship and modern legibility.

- **EB Garamond** commands titles, chapter headings, and quotes. It brings bookbinding elegance, literary grace, and classical presence to the sanctuary.
- **Plus Jakarta Sans** governs UI controls, body prose, and study notes. Its geometric balance and rounded humanist terminals keep long reading sessions legible and approachable without breaking the romantic mood.
- **JetBrains Mono** controls session clocks, reading speed metrics, footnotes, and cataloging indices, grounding magical aesthetics with scholarly rigor.

## Layout & Spacing

The layout is built around deliberate study zones using an 8-column layout for reading tablets and a 12-column fluid grid for desktop study sessions, collapsing to a single-column layout on mobile.

- **Generous Canvas Margins:** Substantial exterior buffers simulate the margins of an antique manuscript, preserving cognitive clarity and avoiding edge crowding.
- **Prose Columns:** Reading viewports default to an optimal measure of 60–75 characters per line (`max-width: 68ch`), centered like an illuminated page.
- **Rhythmic Gaps:** Structural elements leverage `space-lg` and `space-xl` to avoid typical SaaS density, allowing content cards to float with composure on the midnight backdrop.

## Elevation & Depth

Depth in this design system avoids stark synthetic dropshadows. Instead, elevation mirrors soft candlelight dispersing inside a dark stone room:

- **Base Floor (`#12172B`):** Deep background floor, absorbing light.
- **Stone Surface (`#1B233E`):** Resting plane for reading nooks, note sheets, and desk compartments.
- **Atmospheric Glow:** Floating modals and active cards receive a multi-stop ambient diffusion: `0 8px 32px -4px rgba(10, 13, 26, 0.7), 0 0 24px -2px rgba(157, 142, 199, 0.08)`.
- **Astronomical Hairlines:** Visual separation relies primarily on 1px borders using `#253052`, catching subtle rim lighting at top and left edges via translucent highlights (`rgba(242, 214, 155, 0.12)`).

## Shapes

The geometry balances soft hand-carved stone with ergonomic comfort:

- **Rounded Architectural Radius:** Primary containers, cards, and reading pods feature a balanced 1rem (`rounded-lg`) to 1.5rem (`rounded-xl` / rounded-2xl feel) corner treatment, evoking softened ashlar masonry and rounded leather-bound corners.
- **Pill Counters & Badges:** Session timers and tea/coffee status badges utilize circular pill boundaries for contrast against rectangular paper cards.
- **Celestial Iconography:** Visual breaks and decorative dividers weave subtle star constellations, crescent arcs, quills, and warm café steam lines directly into UI borders.

## Components

### Buttons & Interactive Triggers
- **Primary Sanctuary Action (e.g., Begin Focus, Brew Session):** Filled in Muted Lavender (`#9D8EC7`) with Midnight Blue (`#12172B`) bold label text. On hover, shifts softly toward `#7A6BA3` with a faint Champagne Gold outline.
- **Secondary / Archival:** Transparent background bounded by a 1px border (`#253052`), cream text (`#FBF7F0`). Hovers subtly illuminate background to `#1B233E`.
- **Wax Seal / Accent Action:** Rounded pill styled with Champagne Gold (`#E5C17B`) fill, dark ink lettering, and an inner inset stroke mimicking pressed sealing wax.

### Cards & Reading Panes
- Styled on `#1B233E` with rounded-2xl geometry and `#253052` borders.
- Bookmarks hang slightly over top borders with gold gradient tags (`#E5C17B` to `#F2D69B`).
- Inside padding leverages `space-lg` to provide a spacious parchment feel.

### Input Fields & Search Scrolls
- Recessed surface (`#12172B`) with a quiet `#253052` contour.
- Text enters in Cream (`#FBF7F0`), placeholder text renders in subdued slate-lavender (`#7A6BA3`).
- Focus transitions trigger a soft gold halo: `border-color: #E5C17B; box-shadow: 0 0 0 2px rgba(229, 193, 123, 0.2)`.

### Chips & Tea Tags
- Miniature category badges for book genres, music ambience, and herbal teas.
- Bordered in hairline slate with monospaced metadata text (`meta-sm`). Active chips receive a faint lavender wash (`rgba(157, 142, 199, 0.15)`).

### Timers & Study Trackers
- JetBrains Mono numerals styled in Champagne Gold (`#F2D69B`) on dark circular dials.
- Circular progress tracks render in deep `#253052`, filled dynamically with a smooth `#9D8EC7` stroke topped with a glowing crescent terminal point.

### Checkboxes & Milestones
- Custom diamond or star-shaped check targets instead of generic squares.
- Unchecked: `#253052` stone border. Checked: Filled with `#E5C17B` sporting an ink-blue interior constellation tick.