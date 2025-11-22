<div align="center">

# YoinkGUI

[![Download on Modrinth](https://raw.githubusercontent.com/intergrav/devins-badges/c7fd18efdadd1c3f12ae56b49afd834640d2d797/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/mod/yoinkgui)
[![Download on CurseForge](https://raw.githubusercontent.com/intergrav/devins-badges/c7fd18efdadd1c3f12ae56b49afd834640d2d797/assets/cozy/available/curseforge_vector.svg)](https://www.curseforge.com/minecraft/mc-mods/yoinkgui)
[![fapi-badge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg)](https://modrinth.com/mod/fabric-api)
[![sinytra-connector](https://github.com/Sinytra/.github/blob/main/badges/connector/cozy.svg)](https://modrinth.com/mod/connector)

![Build Status](https://github.com/ThatOneDevil/yoinkgui/actions/workflows/build.yml/badge.svg)
[![Modrinth Donwloads](https://img.shields.io/modrinth/dt/yoinkgui?color=00AF5C&label=downloads&logo=modrinth)](https://modrinth.com/mod/yoinkgui)
[![CurseForge Downloads](https://cf.way2muchnoise.eu/full_yoinkgui_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/yoinkgui)
[![Discord](https://img.shields.io/discord/1405856687851704420?color=blue&logo=discord&label=Discord)](https://discord.gg/kcegGvZvpC)

**A Fabric mod that allows users to easily copy the name and lore of items from any GUI**  
Intended strictly for convenience and development purposes.

</div>


---

## 🐛 Issues
Report bugs here:  
[https://github.com/ThatOneDevil/yoinkgui/issues](https://github.com/ThatOneDevil/yoinkgui/issues)

---

## 📦 Dependencies
- Fabric API
- Fabric Language Kotlin
- Yet Another Config Lib
- ModMenu

---

## ✨ Features

- **Full Minecraft formatting code support**:
  - **Color codes**
  - **Bold**, *Italic*, __Underline__, ~~Strikethrough~~, and obfuscated text
- **Hex and gradient support** (MiniMessage format):
  - Hex colors: `<color:#FF00AA>`
  - Gradients: `<gradient:#8968CD:#FFA6CA>`

---

## 📖 How to Use

1. Install the mod with the correct dependencies.  
2. Open any GUI in-game.  
3. Click the **"Yoink and Parse NBT into file"** button in the top-left corner.  
4. The output `.txt` file will appear in your `config` folder.  
5. The file path is also displayed in chat, as shown below

**Example of the path message:**

![Example of the path message](https://cdn.modrinth.com/data/cached_images/681a4eb24635ac98ae987f4a72a741c27c4c3c87.png)

![Example item with lore](https://cdn.modrinth.com/data/cached_images/acb2558b12d52504936e9d1e1afd8f54a93d04ef.png)

![Parsed Lore](https://cdn.modrinth.com/data/cached_images/c468d843708f68328354a1c17ea48f4d8b4dd297.png)
