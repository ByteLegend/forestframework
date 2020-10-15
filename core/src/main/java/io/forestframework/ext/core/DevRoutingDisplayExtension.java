package io.forestframework.ext.core;

import io.forestframework.ext.api.Extension;

/**
 * With this plugin applied, every HTTP response will contain a special response header:
 *
 * Dev-Page: http://xxx.com/dev/1a2b3c4f
 *
 * Which display the routing matching result. This is only enabled in dev environment.
 */
public class DevRoutingDisplayExtension implements Extension {
}
