package io.forestframework.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;

public class ForestConfigurationFactory extends XmlConfigurationFactory {
    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
        return new ForestConfiguration(loggerContext, source);
    }
}

class ForestConfiguration extends XmlConfiguration {
    public ForestConfiguration(LoggerContext loggerContext, ConfigurationSource configSource) {
        super(loggerContext, configSource);
    }

    @Override
    protected void doConfigure() {
        super.doConfigure();
        getRootLogger().setLevel(Level.INFO);
    }
}