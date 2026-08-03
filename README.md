<div align="center">

<img src="https://raw.githubusercontent.com/Rom4ik-12/FreshStats/main/assets/screenshots/icon.png" width="128" height="128" alt="FreshStats Logo">

# FreshStats

**Красивая интерактивная диаграмма-радар игровой статистики для Minecraft**  
*Beautiful & modern 6-axis radar statistics chart mod for Minecraft*

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.1%20--%201.21.1%2B-brightgreen.svg)](https://minecraft.net)
[![Mod Loader](https://img.shields.io/badge/Loader-Fabric%20%7C%20NeoForge-blue.svg)](https://fabricmc.net)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://raw.githubusercontent.com/Rom4ik-12/FreshStats/main/LICENSE)

---

*Read this in other languages: [English](#-english) | [Русский](#-русский)*

</div>

---

## 🇷🇺 Русский

**FreshStats** — это красивый и современный клиентский мод для Minecraft, добавляющий элегантную интерактивную диаграмму-радар для визуализации вашей игровой статистики.

Забудьте о громоздких текстовых списках ванильного меню статистики! FreshStats автоматически находит все ваши сохранения и сервера, анализирует каждый ваш шаг, нанесенный урон, установленные блоки и сделки с жителями, преобразуя их в наглядную графическую диаграмму-радар.

![Radar Chart](https://raw.githubusercontent.com/Rom4ik-12/FreshStats/main/assets/screenshots/radar_chart.png)

### ✨ Основные возможности

- 📊 **6-осевая диаграмма-радар**: Наглядный анализ статистики по 6 категориям:
  - 🚶 **Путешествия** (дистанция пешком, бег, полет на элитрах, лодки, лошади и др.)
  - ⚔️ **Боевка** (нанесенный и полученный урон, убийства мобов по видам, убийства игроков, смертей)
  - 🤝 **Торговля** (количество успешных сделок с жителями)
  - 🌾 **Сельское хозяйство** (выращенные животные, пойманная рыба, собранный урожай)
  - 🧱 **Строительство** (количество и виды установленных строительных блоков)
  - ⛏️ **Шахтерство** (добытые блоки и руды)
- 🌐 **Глобальная агрегация миров**: Мод автоматически сканирует все сохранения в папке `saves/` и сервера, суммируя статистику со всех ваших миров!
- 🔍 **Интерактивные детали**: Клик по любой точке диаграммы открывает стильное модальное окно с детальным списком достижений и иконками предметов.
- 🎨 **Плавная анимация**: Открытие экрана сопровождается анимацией распускания диаграммы (`Cubic Ease-Out`).
- ⌨️ **Удобные горячие клавиши**: Нажмите клавишу **O** в игре для быстрой открытии статистики.

---

### 📥 Установка

1. Скачайте нужную версию из раздела [Releases](https://github.com/Rom4ik-12/FreshStats/releases):
   - `freshstats-1.20.1-fabric.jar` (для Fabric 1.20.1)
   - `freshstats-1.21.1-26.x-fabric.jar` (для Fabric 1.21.1+)
   - `freshstats-1.20.1-neoforge.jar` (для NeoForge 1.20.1)
   - `freshstats-1.21.1-26.x-neoforge.jar` (для NeoForge 1.21.1+)
2. Поместите скачанный `.jar` файл в папку `mods/` вашего лаунчера.
3. Запустите игру и нажмите клавишу **O**!

---

## 🇬🇧 English

**FreshStats** is a clean, modern client-side Minecraft mod that introduces a sleek interactive radar chart GUI to visualize all your gameplay statistics.

Say goodbye to tedious wall-of-text vanilla stat lists! FreshStats automatically scans all your saved singleplayer worlds and servers, compiling every step walked, damage dealt, block placed, and item traded into an intuitive visual hexagon chart.

![Radar Chart](https://raw.githubusercontent.com/Rom4ik-12/FreshStats/main/assets/screenshots/radar_chart.png)

### ✨ Features

- 📊 **6-Axis Hexagonal Radar Chart**: Visually analyze stats across 6 curated categories:
  - 🚶 **Travel** (walking, sprinting, elytra flight, boats, horses, etc.)
  - ⚔️ **Combat** (damage dealt/taken, entity kills breakdown, player kills, deaths)
  - 🤝 **Trading** (villager trade counts)
  - 🌾 **Agriculture** (animals bred, fish caught, harvested crops)
  - 🧱 **Building** (placed blocks breakdown)
  - ⛏️ **Mining** (mined blocks and ores breakdown)
- 🌐 **Automatic World Aggregation**: FreshStats scans all singleplayer saves in your `saves/` folder and server entries, automatically merging stats across your entire play history!
- 🔍 **Interactive Detail Modals**: Click on any point to open a solid modal window with scrollable detail lists.
- 🎨 **Smooth Spring Opening Animation**: Modern cubic ease-out animation on screen open.
- ⌨️ **Customizable Keybind**: Press **O** in-game to toggle the visual statistics GUI.

---

### 📥 Installation

1. Download the required jar from [Releases](https://github.com/Rom4ik-12/FreshStats/releases):
   - `freshstats-1.20.1-fabric.jar` (Fabric 1.20.1)
   - `freshstats-1.21.1-26.x-fabric.jar` (Fabric 1.21.1+)
   - `freshstats-1.20.1-neoforge.jar` (Fabric 1.20.1)
   - `freshstats-1.21.1-26.x-neoforge.jar` (NeoForge 1.21.1+)
2. Put the `.jar` file into your `.minecraft/mods` directory.
3. Launch Minecraft and press **O**!

---

## 🛠️ Building from Source

```bash
# Clone the repository
git clone https://github.com/Rom4ik-12/FreshStats.git
cd FreshStats

# Build the mod
./gradlew build
```

Built artifacts will be generated in `build/libs/` and `builds/`.

---

## 📄 License

This project is licensed under the [MIT License](https://raw.githubusercontent.com/Rom4ik-12/FreshStats/main/LICENSE).
