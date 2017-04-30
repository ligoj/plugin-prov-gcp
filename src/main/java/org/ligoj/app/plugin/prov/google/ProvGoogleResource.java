package org.ligoj.app.plugin.prov.google;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ligoj.app.plugin.prov.AbstractProvResource;
import org.ligoj.app.plugin.prov.ProvResource;
import org.springframework.stereotype.Service;

/**
 * The provisioning service for Google. There is complete quote configuration
 * along the subscription.
 */
@Service
@Path(ProvGoogleResource.SERVICE_URL)
@Produces(MediaType.APPLICATION_JSON)
public class ProvGoogleResource extends AbstractProvResource {

	/**
	 * Plug-in key.
	 */
	public static final String SERVICE_URL = ProvResource.SERVICE_URL + "/google";

	/**
	 * Plug-in key.
	 */
	public static final String SERVICE_KEY = SERVICE_URL.replace('/', ':').substring(1);

	@Override
	public String getKey() {
		return SERVICE_KEY;
	}
}
