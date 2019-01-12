package org.bukkit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import org.bukkit.craftbukkit.CraftStatistic;
import org.bukkit.support.AbstractTestingBase;
import org.junit.Test;

import com.google.common.collect.HashMultiset;

public class StatisticsAndAchievementsTest extends AbstractTestingBase {

    @Test
    public void verifyStatisticMapping() throws Throwable {
        HashMultiset<Statistic> statistics = HashMultiset.create();
        for (StatBase statistic : StatList.ALL_STATS) {
            String name = statistic.statId;

            String message = String.format("org.bukkit.Statistic is missing: '%s'", name);

            Statistic subject = CraftStatistic.getBukkitStatistic(statistic);
            assertThat(message, subject, is(not(nullValue())));

            statistics.add(subject);
        }

        for (Statistic statistic : Statistic.values()) {
            String message = String.format("org.bukkit.Statistic.%s does not have a corresponding minecraft statistic", statistic.name());
            assertThat(message, statistics.remove(statistic, statistics.count(statistic)), is(greaterThan(0)));
        }
    }
}
