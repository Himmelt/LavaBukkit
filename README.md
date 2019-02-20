# LavaBukkit
<p align="center">
  <img width="400" height="400" src="https://cdn.discordapp.com/attachments/362654848711262208/544740693743108105/LB.png">
</p>

[![Build Status](https://img.shields.io/travis/com/MatrixDevTeam/LavaBukkit.svg?style=flat-square)](https://travis-ci.com/MatrixDevTeam/LavaBukkit)
![](https://img.shields.io/github/last-commit/MatrixDevTeam/LavaBukkit.svg?style=popout-square)
![](https://img.shields.io/github/stars/MatrixDevTeam/LavaBukkit.svg?label=Stars&style=popout-square)
[![](https://img.shields.io/discord/491638768831299584.svg?label=Join%20us%20on%20Discord&style=popout-square)](https://discord.gg/wBsB2vt)
![](https://img.shields.io/github/license/matrixdevteam/lavabukkit.svg?style=popout-square)

### What's LavaBukkit?
LavaBukkit is a minecraft server implementation for Forge mods and Spigot mods for 1.12.2. Sponge has come out for this type of implementation, but most features are not available or are simply not able to be done with their API. This also will be constantly updated when Forge or Spigot is updated. We also will not have game-breaking issues much like this type of implementation's 1.7.10 counterpart.

We hope to eliminate all issues with craftbukkit forge servers. In the end, we envision a seamless, low lag Spigot and Forge experience.

Advantages over Sponge or other implementations:
+ Lag-lowering optimizations
+ Better world protection (Forge stuff doesn't bypass Bukkit plugins but rather works with it!)
+ Full use of **all** Spigot plugins and Forge mods
+ Hyperthreaded performance
+ Use of Mixin - Unlike other impl.

## Does this actually work?
Yes, even though it really shouldn't.

[![Build Status](https://img.shields.io/travis/com/MatrixDevTeam/LavaBukkit.svg?style=flat-square)](https://travis-ci.com/MatrixDevTeam/LavaBukkit)

## Downloads
You can download pre-compiled jars [here](https://github.com/MatrixDevTeam/LavaBukkit/releases)

**LavaBukkit is still in beta and you may encounter issues in using it with your server. Please report any issues to our issues tab at the top of this page**

> Note: **PLEASE** look at the release notes before downloading! :wink:

## API Usage - **Work in progress**
The API is similar to the Bukkit API.

Some Changes include:
- Live Updates (Soon)
- Update function that runs every tick (Don't use this unless you know what you are doing! This can really lag your game!)
```java
public class MyAwesomePlugin extends LavaPlugin {
    @Override 
    public void onEnable() {
        // Run whenever the plugin is enabled
    }

    @Override
    public void onDisable() {
        // Run whenever the plugin is disabled
    }

    @Override
    @EnableMethod // Add this to the onUpdate method only if you are aware of the risks! 
                  // This enables the calling of this method every tick.
    public void onUpdate() {
        // Run every tick (if enabled)
    }
}
```

## Chat

Feel free to drop in on the [Matrix Development Discord](https://matrixdevteam.ml/discord).

## Donate/Support

You can pledge to support GMatrixGames and his team's work through a one-time [PayPal](http://paypal.me/GMatrixCodes) donation.
