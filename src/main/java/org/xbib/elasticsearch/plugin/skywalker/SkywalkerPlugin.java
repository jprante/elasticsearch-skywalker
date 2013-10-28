
package org.xbib.elasticsearch.plugin.skywalker;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.xbib.elasticsearch.action.admin.cluster.state.ConsistencyCheckAction;
import org.xbib.elasticsearch.action.admin.cluster.state.TransportConsistencyCheckAction;
import org.xbib.elasticsearch.action.admin.indices.reconstruct.ReconstructIndexAction;
import org.xbib.elasticsearch.action.admin.indices.reconstruct.TransportReconstructAction;
import org.xbib.elasticsearch.action.skywalker.SkywalkerAction;
import org.xbib.elasticsearch.action.skywalker.TransportSkywalkerAction;
import org.xbib.elasticsearch.rest.action.skywalker.RestConsistencyCheckAction;
import org.xbib.elasticsearch.rest.action.skywalker.RestReconstructIndexAction;
import org.xbib.elasticsearch.rest.action.skywalker.RestSkywalkerAction;

/**
 *  Skywalker plugin
 */
public class SkywalkerPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "skywalker";
    }

    @Override
    public String description() {
        return "Skywalker - Luke for Elasticsearch";
    }

    public void onModule(RestModule module) {
        module.addRestAction(RestSkywalkerAction.class);
        module.addRestAction(RestConsistencyCheckAction.class);
        module.addRestAction(RestReconstructIndexAction.class);
    }

    public void onModule(ActionModule module) {
        module.registerAction(SkywalkerAction.INSTANCE, TransportSkywalkerAction.class);
        module.registerAction(ConsistencyCheckAction.INSTANCE, TransportConsistencyCheckAction.class);
        module.registerAction(ReconstructIndexAction.INSTANCE, TransportReconstructAction.class);
    }

}
