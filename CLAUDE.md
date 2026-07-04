# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Java library for creating custom GUIs in Minecraft using inventory screens, working on both Paper (plugin) and Fabric (mod). The core trick: custom visuals are encoded as a generated resource pack with a custom bitmap font (`praeter_gui:font`). Images drawn on a GUI are registered as font characters, and the inventory title is set to a sequence of those characters, painting arbitrary pixels over the screen. Items and slots are real inventory items.

## Commands

Requires a JDK matching `javaVersion` in `gradle.properties` (Gradle toolchains will look for it; there is no toolchain resolver plugin, so it must be installed).

```sh
./gradlew build                 # build all modules, run tests and javadoc
./gradlew :common:test          # run common module tests
./gradlew :common:test --tests "ca.bkaw.praeter.gui.slot.SlotInteractionHandlerTest"   # single test class
```

Javadoc errors fail the build, so keep `@link` references valid.

Tests write inspectable output to `common/.test_run/` (gitignored): `standalone_render_test.png` is an offline render of a test gui for visual verification, and `resource_pack.zip` is the generated pack.

In-game testing uses `test-plugin` on Paper: `/example example1` opens the example gui, `/praeter-reload` re-runs all gui type setups, rebuilds assets and re-sends the pack.

## Module structure

- `common` — all logic, platform-agnostic. Depends only on Adventure, Gson, SLF4J, Netty (compileOnly).
- `paper` — Paper plugin integration (Bukkit events, Bukkit `ItemStack`).
- `fabric` — Fabric mod integration. Uses **Mojang mappings** (note: in current Minecraft versions `ResourceLocation` is named `Identifier`, and the click type enum is `ContainerInput`). When unsure about Minecraft signatures, `javap` the mapped jar in `~/.gradle/caches/fabric-loom/minecraftMaven/`.
- `test-plugin` — Paper plugin for in-game testing.

The guiding principle: as much as possible lives in `common`, abstracted from the game. Platform modules translate native events/types at the boundary and apply results back.

## Architecture

### Rendering pipeline

1. `CustomGuiType` (one per gui type) is registered via `CustomGuiRegistry`, which runs the type's setup function with a `RenderContextImpl`.
2. Static draws are baked into a background texture (`GuiBackgroundPainter`); draws inside `renderIf` become `RenderStep`s producing `FontSequence`s (characters in the generated font).
3. When a gui renders, `RenderDispatcher` runs the render steps and the platform builds the inventory title from the font sequences. Re-rendering with a changed title recreates and reopens the inventory.
4. The render context is closed after setup; any later use throws. Setup functions run once at startup and again on `/praeter-reload`.
5. `StandaloneRender` simulates rendering offline for tests, reading the generated font from the in-memory pack.

### State

`useState` during setup returns a `Ref<T>`; values are created per `CustomGui` instance (one instance per opened gui, shared by its viewers). Invariants: every `Ref` comes from `useState` (no hidden Ref implementations), and there is no generic mutable ref — mutable state lives in user-defined classes reached through a `Ref`.

### Item movement (slots)

All slot behavior is simulated in `common`, mirroring vanilla logic (`SlotInteractionHandler`). Platforms cancel/replace all native handling:

- Raw slot index space covers the whole screen: `[0, height*9)` top gui, then 27 player inventory slots, then 9 hotbar slots. Same numbering on both platforms and in the protocol.
- Platforms translate native events into `SlotInteraction` records (Paper: `PaperGuiListener` from Bukkit events; Fabric: `PraeterChestMenu extends ChestMenu` overriding `clicked`, accumulating drag phases), build a `GuiScreenState` snapshot, call `SlotInteractionHandler.handle`, then apply the `SlotInteractionResult` (player inventory changes, cursor, offhand, drops, gui re-render).
- Custom slot contents are gui state (source of truth in common); the player inventory region is read fresh from the game each interaction — the game stays authoritative for state that outlives the gui.
- `GuiItem` is the immutable platform item handle (`PaperGuiItem`/`FabricGuiItem`); empty is the `GuiItem.empty()` singleton, never null, never a platform wrapper.
- Top positions that are not slots are filled with invisible filler items (empty item model `praeter_gui:empty` + hidden tooltip) so client-side prediction sees them as occupied.
- `BottomRegionType.CUSTOM` (gui content instead of the player inventory in the bottom region) is designed for in common but not yet supported by platforms.

Paper quirk: cursor changes from cancelled drag events must be applied one tick later (see `PaperGuiListener`).

### User-facing API conventions

- Hooks classes are the user vocabulary, intended for static import: `CommonHooks` (`useState`, `drawImage`, `renderIf`, `renderItem`, `hoverText`), plus `PaperHooks`/`FabricHooks` containing **only** platform-specific variants (`hoverText` with components, `renderItemStack`, `getSlotItem`/`setSlotItem`). Do not duplicate common hooks into platform hooks. Both are imported together; avoid same-name methods across them with same-arity functional parameters (ambiguous for implicitly typed lambdas — this is why `renderItemStack` is not named `renderItem`).
- Every hook takes the `RenderContext` as first parameter. The `use` prefix is reserved for methods that allocate per-instance state.
- Components are static methods on their own classes (`Slot.slot`, `Button.button`, `Panel.panel`), not re-exported by hooks.
- `RenderContext` is the SPI for component authors: `addSlot`, `addItemRenderer`, `addRenderStep` are low-level registration methods.
- Platform types must not leak into common; consumers should see platform types (Bukkit/Mojang `ItemStack`, `HumanEntity`) through platform hooks and interfaces like `PaperSlotBehavior` rather than converting manually.

### Resource pack

`PraeterGuiAssets` builds the pack at startup: vanilla assets are downloaded/extracted once (version pinned in `PraeterGuiAssets.VANILLA_ASSETS_VERSION`), plugin/mod jar assets are included, and generated assets (font, textures, the empty item model) are written in. The pack is served to players by a built-in TCP sender using Netty channel injection.
