package org.wisdom.everest.controller;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wisdom.api.content.JacksonModuleRepository;

/**
 *
 */
@Component(immediate = true)
@Instantiate
public class EverestModule {

    private final Bundle bundle;
    private final SimpleModule module;
    @Requires
    private JacksonModuleRepository repository;

    public EverestModule(BundleContext context) {
        bundle = context.getBundle();
        module = new SimpleModule("Everest-Module", version());
        module.addSerializer(new PathSerializer());
        module.addSerializer(new ResourceSerializer());
    }

    @Validate
    public void start() {
        repository.register(module);
    }

    @Invalidate
    public void stop() {
        repository.unregister(module);
    }

    public final Version version() {
        return new Version(bundle.getVersion().getMajor(), bundle.getVersion().getMinor(),
                bundle.getVersion().getMicro(), null, null, null);
    }
}
