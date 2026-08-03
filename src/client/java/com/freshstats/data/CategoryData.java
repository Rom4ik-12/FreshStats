package com.freshstats.data;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CategoryData {
    private final StatCategory category;
    private long totalValue;
    private Text mainUnitText;
    private final List<DetailEntry> details = new ArrayList<>();

    public CategoryData(StatCategory category, Text mainUnitText) {
        this.category = category;
        this.mainUnitText = mainUnitText;
    }

    public StatCategory getCategory() {
        return category;
    }

    public long getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(long totalValue) {
        this.totalValue = totalValue;
    }

    public Text getMainUnitText() {
        return mainUnitText != null ? mainUnitText : category.getMainUnitText();
    }

    public String getMainUnit() {
        return getMainUnitText().getString();
    }

    public List<DetailEntry> getDetails() {
        return details;
    }

    public void addDetail(Text name, long count, ItemStack icon) {
        details.add(new DetailEntry(name, count, icon));
    }

    public static class DetailEntry {
        private final Text name;
        private final long count;
        private final ItemStack icon;

        public DetailEntry(Text name, long count, ItemStack icon) {
            this.name = name;
            this.count = count;
            this.icon = icon;
        }

        public Text getName() {
            return name;
        }

        public long getCount() {
            return count;
        }

        public ItemStack getIcon() {
            return icon;
        }
    }
}
