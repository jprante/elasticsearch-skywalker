package org.elasticsearch.plugin.skywalker;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.rest.RestModule;

import org.testng.annotations.Test;

public class SkywalkerTests {

    @Test
    public void test() {
        
        Settings settings = ImmutableSettings.settingsBuilder().build();        

        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)),
                new IndicesAnalysisModule())
                .createInjector();     
        
        ActionModule actionModule = new ActionModule(true);
        RestModule restModule = new RestModule(settings);
        
        SkywalkerPlugin plugin = new SkywalkerPlugin();

        plugin.onModule(actionModule) ;
        plugin.onModule(restModule);

    }
}
