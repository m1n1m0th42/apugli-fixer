# Apugli Fixer

A lightweight mixin compatibility shim for **Minecraft 1.20.1 (Fabric)** that fixes a crash when using [Apugli](https://modrinth.com/mod/apugli) 2.10.x with [Origins](https://modrinth.com/mod/origins) 1.10.2 (Apoli 2.9.2).

## The Problem

Apugli 2.10.4's `EntityConditionsMixin` uses `@Redirect` to intercept `EnchantmentHelper.getLevel()` and `EnchantmentHelper.getEquipmentLevel()` calls inside Apoli's `EntityConditions.lambda$register$47`. However, in Apoli 2.9.2, those calls live in `lambda$register$45` instead. Lambda method numbers are compiler-generated and shift between Apoli versions, so the redirect finds 0 targets and crashes:

```
InjectionError: Critical injection failure: Redirector apugli$useModifiedEnchantmentLevelSum
in apugli.fabric.mixins.json:common.EntityConditionsMixin from mod apugli failed injection
check, (0/1) succeeded. Scanned 0 target(s).
```

## The Fix

This mod uses a single `@Overwrite` mixin (priority 100, applied before Apugli's redirect at priority 1000) to replace `lambda$register$47` with:

1. The **original logic** (entity_on_block bientity condition), fully preserved
2. **No-op calls** to `EnchantmentHelper.getLevel()` and `EnchantmentHelper.getEquipmentLevel()`, which Apugli's redirect intercepts and replaces with its modified enchantment level system

The result: Apugli's redirect finds its targets, the game launches, and enchantment-based origin powers work correctly.

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.20.1 |
| Fabric Loader | >= 0.14.0 |
| Origins | 1.10.2 (Apoli 2.9.2) |
| Apugli | 2.10.0+ |

## Installation

Drop `apugli-fixer-4.0.2.jar` into your `mods/` folder alongside Origins and Apugli.

## Building

This project uses local jars for compile-only dependencies (Apoli, Calio, Apugli). To build from source:

1. Clone the repo
2. Download the required dependency jars and place them in `libs/`:
   - `Apoli` from [Modrinth](https://modrinth.com/mod/apoli) or extract from the Origins jar
   - `Calio` from `META-INF/jars/calio-*.jar` inside the Apoli jar
   - `Apugli` from [Modrinth](https://modrinth.com/mod/apugli)
3. Run `./gradlew build`
4. The output jar is in `build/libs/`

## How It Works

```
Mixin priority order when EntityConditions class loads:

100  [Apugli Fixer]   @Overwrite lambda$register$47
                       -> original logic + EnchantmentHelper calls

1000 [Apugli]          @Redirect on those calls
                       -> replaces with ModifyEnchantmentLevelPowerFactory

Result: redirect finds targets, no crash, modified enchantment levels work
```

## License

[MIT](LICENSE)
