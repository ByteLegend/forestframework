package io.forestframework.ext.core;

import io.forestframework.core.Forest;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;


/**
 * Do nothing but display banner at startup.
 *
 * You can disable it by "forest.banner.enabled=false" configuration.
 */
public class BannerExtension implements Extension {
    private static final String DEFAULT_BANNER = "    ,---,.                                                  ___     \n" +
            "  ,'  .' |                                                ,--.'|_   \n" +
            ",---.'   |    ,---.     __  ,-.                           |  | :,'  \n" +
            "|   |   .'   '   ,'\\  ,' ,'/ /|              .--.--.      :  : ' :  \n" +
            ":   :  :    /   /   | '  | |' |    ,---.    /  /    '   .;__,'  /   \n" +
            ":   |  |-, .   ; ,. : |  |   ,'   /     \\  |  :  /`./   |  |   |    \n" +
            "|   :  ;/| '   | |: : '  :  /    /    /  | |  :  ;_     :__,'| :    \n" +
            "|   |   .' '   | .; : |  | '    .    ' / |  \\  \\    `.    '  : |__  \n" +
            "'   :  '   |   :    | ;  : |    '   ;   /|   `----.   \\   |  | '.'| \n" +
            "|   |  |    \\   \\  /  |  , ;    '   |  / |  /  /`--'  /   ;  :    ; \n" +
            "|   :  \\     `----'    ---'     |   :    | '--'.     /    |  ,   /  \n" +
            "|   | ,'                         \\   \\  /    `--'---'      ---`-'   \n" +
            "`----'                            `----'             version " + Forest.VERSION;

    @Override
    public void beforeInjector(StartupContext startupContext) {
        startupContext.getConfigProvider().addDefaultOptions("forest.banner", BannerOptions::new);
        if (startupContext.getConfigProvider().getInstance("forest.banner.enabled", Boolean.class)) {
            System.out.println(startupContext.getConfigProvider().getInstance("forest.banner.text", String.class));
        }
    }

    public static class BannerOptions {
        private boolean enabled = true;
        private String text = BannerExtension.DEFAULT_BANNER;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
