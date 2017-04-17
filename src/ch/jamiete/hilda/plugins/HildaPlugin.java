package ch.jamiete.hilda.plugins;

import ch.jamiete.hilda.Hilda;

public abstract class HildaPlugin {

    private final Hilda hilda;
    private PluginData pluginData;

    public HildaPlugin(final Hilda hilda) {
        this.hilda = hilda;
    }

    public final Hilda getHilda() {
        return this.hilda;
    }

    public final PluginData getPluginData() {
        return this.pluginData;
    }

    public abstract void onDisable();

    public abstract void onEnable();

    public abstract void onLoad();

}
