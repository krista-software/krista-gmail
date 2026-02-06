/*
 * Gmail Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.essentials.collaboration.gmail.api;

import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * JAX-RS Application class for Gmail extension REST endpoints.
 * Configures the REST application path and registers resource classes.
 */
@Service
@ApplicationPath("gmail")
@ContractsProvided(Application.class)
public class GmailApplication extends Application {

    /**
     * Returns the set of resource classes to be registered with this application.
     * Registers the AuthenticationResource for handling Gmail OAuth and webhook endpoints.
     *
     * @return Set containing the AuthenticationResource class
     */
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(AuthenticationResource.class);
    }

}
