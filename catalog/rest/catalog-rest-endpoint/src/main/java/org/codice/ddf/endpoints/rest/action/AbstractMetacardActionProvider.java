/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.endpoints.rest.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.configuration.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.action.Action;
import ddf.action.ActionProvider;
import ddf.catalog.data.Metacard;

public abstract class AbstractMetacardActionProvider implements ActionProvider {

    static final String UNKNOWN_TARGET = "0.0.0.0";

    static final String PATH = "/catalog/sources";

    private static final Logger LOGGER = LoggerFactory.getLogger(
            AbstractMetacardActionProvider.class);

    protected String actionProviderId;

    protected SystemInfo systemInfo;

    protected AbstractMetacardActionProvider(SystemInfo info) {
        this.systemInfo = info;
    }

    protected abstract Action getAction(String metacardId, String metacardSource);

    @Override
    public <T> Action getAction(T input) {

        if (input == null) {

            LOGGER.info("In order to receive url to Metacard, Metacard must not be null.");
            return null;
        }

        if (Metacard.class.isAssignableFrom(input.getClass())) {

            Metacard metacard = (Metacard) input;

            if (StringUtils.isBlank(metacard.getId())) {
                LOGGER.info("No id given. No action to provide.");
                return null;
            }

            if (isHostUnset(SystemBaseUrl.getHost())) {
                LOGGER.info("Host name/ip not set. Cannot create link for metacard.");
                return null;
            }

            String metacardId = null;
            String metacardSource = null;

            try {
                metacardId = URLEncoder.encode(metacard.getId(), CharEncoding.UTF_8);
                metacardSource = URLEncoder.encode(getSource(metacard), CharEncoding.UTF_8);
            } catch (UnsupportedEncodingException e) {
                LOGGER.info("Unsupported Encoding exception", e);
                return null;
            }

            return getAction(metacardId, metacardSource);

        }

        return null;
    }

    protected boolean isHostUnset(String host) {

        return (host == null || host.trim()
                .equals(UNKNOWN_TARGET));
    }

    protected String getSource(Metacard metacard) {

        if (StringUtils.isNotBlank(metacard.getSourceId())) {
            return metacard.getSourceId();
        }

        return systemInfo.getSiteName();
    }

    @Override
    public String getId() {
        return this.actionProviderId;
    }

}
